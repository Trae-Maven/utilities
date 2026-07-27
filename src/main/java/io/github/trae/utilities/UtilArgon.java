package io.github.trae.utilities;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.github.trae.utilities.cache.CachedArgonVerification;
import io.github.trae.utilities.objects.consumer.Consumer;
import io.github.trae.utilities.objects.consumer.TriConsumer;
import io.github.trae.utilities.objects.function.BiFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Utility methods for hashing and verifying passwords using the Argon2id algorithm.
 *
 * <p>This class combines the supplied pepper, plain-text password, and salt into a
 * single byte array before performing Argon2 operations. Successful password
 * verifications may be cached through a pluggable cache implementation to reduce
 * the cost of repeatedly verifying the same credentials.</p>
 *
 * <p>The default cache implementation uses an in-memory
 * {@link ConcurrentHashMap}, but callers may replace the getter, setter, and
 * invalidator with implementations backed by Redis, Caffeine, or any other
 * caching solution.</p>
 *
 * <p><b>Cache keys are HMAC-SHA256 digests</b> computed under a random key that is
 * generated at class-initialization and never leaves the process. A plain SHA-256
 * digest of the credential would be a fast, unsalted fingerprint of the password
 * itself: anyone who obtained the cache contents (and, for an external cache, that
 * includes anyone who dumps Redis) could brute-force it on a GPU at billions of
 * guesses per second, bypassing Argon2 entirely. Keying the digest means a leaked
 * cache is inert. It also means cache entries do not survive a restart, which is
 * the correct trade-off here.</p>
 *
 * <p><b>Only successful verifications are cached.</b> Caching failures would let an
 * unauthenticated caller grow the cache without bound by replaying distinct wrong
 * passwords against one account. Because a stored hash has exactly one correct
 * credential, the cache holds at most one entry per hash and is therefore bounded
 * by the number of accounts that have recently authenticated.</p>
 *
 * <p><b>Memory budget.</b> Peak native memory used for hashing is roughly
 * {@code maximumConcurrency * memoryKb}. At the defaults that is
 * {@value #DEFAULT_MEMORY_KB} KB per operation; size {@link #setMaximumConcurrency(int)}
 * against the memory actually available on the host, not just against CPU count.</p>
 */
@UtilityClass
public class UtilArgon {

    /**
     * The default number of Argon2 iterations.
     */
    public static final int DEFAULT_ITERATIONS = 3;

    /**
     * The default number of parallel threads used by Argon2.
     */
    public static final int DEFAULT_PARALLELISM = 2;

    /**
     * The default amount of memory, in kilobytes, allocated per Argon2 operation.
     */
    public static final int DEFAULT_MEMORY_KB = 1024 * 64;

    /**
     * The default upper bound on entries held by the in-memory verification cache.
     */
    public static final int DEFAULT_MAXIMUM_CACHE_ENTRIES = 4_096;

    /**
     * The algorithm used to derive cache keys from credentials.
     */
    private static final String CACHE_DIGEST_ALGORITHM = "HmacSHA256";

    /**
     * Process-local key for {@link #CACHE_DIGEST_ALGORITHM}.
     *
     * <p>Regenerated on every start, so cached digests are meaningless outside the
     * running JVM.</p>
     */
    private static final SecretKeySpec CACHE_DIGEST_KEY = createCacheDigestKey();

    /**
     * Shared Argon2id instance. The implementation is stateless and safe for
     * concurrent use, so there is no need to build one per call.
     */
    private static final Argon2 ARGON = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    /**
     * Default in-memory cache of successful password verifications, keyed by the
     * stored Argon2 hash.
     */
    private static final ConcurrentHashMap<String, CachedArgonVerification> VERIFY_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * Invalidates all cached verification results associated with a stored
     * Argon2 password hash.
     *
     * <p>This should be invoked when a password hash changes to prevent stale
     * verification results from remaining in the cache.</p>
     */
    @Getter
    @Setter
    private static Consumer<String> verifyInvalidator = VERIFY_CACHE_MAP::remove;

    /**
     * The number of Argon2 iterations.
     */
    @Getter
    @Setter
    private static int iterations = DEFAULT_ITERATIONS;

    /**
     * The number of parallel threads used by Argon2.
     */
    @Getter
    @Setter
    private static int parallelism = DEFAULT_PARALLELISM;

    /**
     * The amount of memory, in kilobytes, allocated per Argon2 operation.
     */
    @Getter
    @Setter
    private static int memoryKb = DEFAULT_MEMORY_KB;

    /**
     * How long a successful verification stays cached, in milliseconds.
     */
    @Getter
    @Setter
    private static long cacheTtlMs = TimeUnit.MINUTES.toMillis(15L);

    /**
     * The upper bound on entries held by the default in-memory cache.
     */
    @Getter
    @Setter
    private static int maximumCacheEntries = DEFAULT_MAXIMUM_CACHE_ENTRIES;

    /**
     * The number of Argon2 operations permitted to run concurrently.
     */
    @Getter
    private static int maximumConcurrency = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    /**
     * Limits the number of concurrent Argon2 operations.
     */
    private static volatile Semaphore argonSemaphore = new Semaphore(maximumConcurrency);

    /**
     * Retrieves a cached verification result.
     *
     * <p>Returns {@link Boolean#TRUE} when the supplied digest matches a live cache
     * entry, and {@code null} otherwise so the caller falls through to a full Argon2
     * verification. The default implementation never returns {@link Boolean#FALSE},
     * because failures are not cached.</p>
     */
    @Getter
    @Setter
    private static BiFunction<String, String, Boolean> verifyGetter = (storedHash, digestKey) -> {
        final CachedArgonVerification cached = VERIFY_CACHE_MAP.get(storedHash);
        if (cached == null) {
            return null;
        }

        if (cached.expiresAt() <= System.currentTimeMillis()) {
            VERIFY_CACHE_MAP.remove(storedHash, cached);
            return null;
        }

        final boolean matches = MessageDigest.isEqual(cached.digestKey().getBytes(StandardCharsets.UTF_8), digestKey.getBytes(StandardCharsets.UTF_8));

        return matches ? Boolean.TRUE : null;
    };

    /**
     * Stores a verification result in the cache.
     *
     * <p>The default implementation ignores unsuccessful verifications, sweeps
     * expired entries when the cache is full, and declines to store anything if the
     * cache is still full after the sweep.</p>
     */
    @Getter
    @Setter
    private static TriConsumer<String, String, Boolean> verifySetter = (storedHash, digestKey, verified) -> {
        if (!(Boolean.TRUE.equals(verified))) {
            return;
        }

        if (VERIFY_CACHE_MAP.size() >= maximumCacheEntries) {
            evictExpiredCacheEntries();

            if (VERIFY_CACHE_MAP.size() >= maximumCacheEntries) {
                return;
            }
        }

        VERIFY_CACHE_MAP.put(storedHash, new CachedArgonVerification(digestKey, System.currentTimeMillis() + cacheTtlMs));
    };

    /**
     * Sets the number of Argon2 operations permitted to run concurrently.
     *
     * <p>Peak native memory for hashing is approximately
     * {@code maximumConcurrency * memoryKb}. Call this during startup, before any
     * hashing begins; replacing the semaphore while operations are in flight will
     * briefly allow more than the new limit.</p>
     *
     * @param newMaximumConcurrency the number of concurrent operations to permit.
     * @throws IllegalArgumentException if the value is less than one.
     */
    public static void setMaximumConcurrency(final int newMaximumConcurrency) {
        if (newMaximumConcurrency < 1) {
            throw new IllegalArgumentException("Maximum concurrency must be at least one.");
        }

        maximumConcurrency = newMaximumConcurrency;
        argonSemaphore = new Semaphore(newMaximumConcurrency);
    }

    /**
     * Hashes a password using Argon2id.
     *
     * @param plainPassword the plain-text password.
     * @param pepper        the application-wide pepper.
     * @param salt          the per-user salt mixed into the input.
     * @return the encoded Argon2 hash.
     * @throws IllegalStateException if the hashing thread is interrupted.
     */
    public static String hash(final String plainPassword, final String pepper, final String salt) {
        final byte[] combinedInput = buildInput(plainPassword, pepper, salt);

        try {
            final Semaphore semaphore = argonSemaphore;

            semaphore.acquire();
            try {
                return ARGON.hash(iterations, memoryKb, parallelism, combinedInput);
            } finally {
                semaphore.release();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while hashing password.", e);
        } finally {
            Arrays.fill(combinedInput, (byte) 0);
        }
    }

    /**
     * Verifies a password against an existing Argon2 hash.
     *
     * <p>A keyed digest of the combined pepper, password, and salt is used as the
     * cache key. If a cached successful verification exists for the stored hash and
     * the digests match, Argon2 verification is skipped.</p>
     *
     * @param plainPassword the plain-text password.
     * @param pepper        the application-wide pepper.
     * @param salt          the per-user salt mixed into the input.
     * @param storedHash    the stored Argon2 hash.
     * @return {@code true} if the password is valid; otherwise {@code false}.
     * @throws IllegalStateException if HMAC-SHA256 is unavailable or the
     *                               verification thread is interrupted.
     */
    public static boolean verify(final String plainPassword, final String pepper, final String salt, final String storedHash) {
        final byte[] combinedInput = buildInput(plainPassword, pepper, salt);

        try {
            final String digestKey = digest(combinedInput);

            final Boolean cachedResult = verifyGetter.apply(storedHash, digestKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            final Semaphore semaphore = argonSemaphore;

            semaphore.acquire();
            try {
                final boolean result = ARGON.verify(storedHash, combinedInput);
                verifySetter.accept(storedHash, digestKey, result);
                return result;
            } finally {
                semaphore.release();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while verifying password.", e);
        } finally {
            Arrays.fill(combinedInput, (byte) 0);
        }
    }

    /**
     * Rehashes a password if the current Argon2 parameters indicate that the
     * stored hash is outdated.
     *
     * @param plainPassword the plain-text password.
     * @param pepper        the application-wide pepper.
     * @param salt          the per-user salt mixed into the input.
     * @param storedHash    the existing Argon2 hash.
     * @return a new Argon2 hash if a rehash is required; otherwise {@code null}.
     */
    public static String tryReHash(final String plainPassword, final String pepper, final String salt, final String storedHash) {
        if (!(ARGON.needsRehash(storedHash, iterations, memoryKb, parallelism))) {
            return null;
        }

        return hash(plainPassword, pepper, salt);
    }

    /**
     * Invalidates all cached verification results associated with the supplied
     * password hash.
     *
     * <p>This should be invoked whenever the stored Argon2 password hash changes,
     * such as after a password change or password reset, to remove stale cached
     * verification results.</p>
     *
     * @param passwordHash the previous stored Argon2 password hash.
     */
    public static void invalidateVerifyCache(final String passwordHash) {
        verifyInvalidator.accept(passwordHash);
    }

    /**
     * Combines the pepper, password, and salt into a single byte array suitable
     * for Argon2 hashing and verification.
     *
     * <p>Each component is length-prefixed so that different splits of the same
     * concatenated bytes cannot collide. Temporary byte arrays are cleared before
     * returning; the {@link String} arguments themselves remain resident until the
     * garbage collector reclaims them, which is unavoidable without a
     * {@code char[]}-based API.</p>
     *
     * @param plainPassword the plain-text password.
     * @param pepper        the application-wide pepper.
     * @param salt          the per-user salt mixed into the input.
     * @return the combined byte array.
     */
    private static byte[] buildInput(final String plainPassword, final String pepper, final String salt) {
        final byte[] passwordBytes = plainPassword.getBytes(StandardCharsets.UTF_8);
        final byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        final byte[] pepperBytes = pepper.getBytes(StandardCharsets.UTF_8);

        final byte[] combinedInput = new byte[12 + pepperBytes.length + passwordBytes.length + saltBytes.length];

        int offset = 0;

        offset = writeSegment(combinedInput, offset, pepperBytes);
        offset = writeSegment(combinedInput, offset, passwordBytes);

        writeSegment(combinedInput, offset, saltBytes);

        Arrays.fill(passwordBytes, (byte) 0);
        Arrays.fill(saltBytes, (byte) 0);
        Arrays.fill(pepperBytes, (byte) 0);

        return combinedInput;
    }

    /**
     * Writes a big-endian length prefix followed by the segment itself.
     *
     * @param target  the destination array.
     * @param offset  the offset to write at.
     * @param segment the bytes to write.
     * @return the offset immediately after the written segment.
     */
    private static int writeSegment(final byte[] target, final int offset, final byte[] segment) {
        final int segmentLength = segment.length;

        target[offset] = (byte) (segmentLength >>> 24);
        target[offset + 1] = (byte) (segmentLength >>> 16);
        target[offset + 2] = (byte) (segmentLength >>> 8);
        target[offset + 3] = (byte) segmentLength;

        System.arraycopy(segment, 0, target, offset + 4, segmentLength);

        return offset + 4 + segmentLength;
    }

    /**
     * Derives the cache key for a combined credential input.
     *
     * @param combinedInput the combined pepper, password, and salt.
     * @return a Base64-encoded keyed digest.
     * @throws IllegalStateException if HMAC-SHA256 is unavailable.
     */
    private static String digest(final byte[] combinedInput) {
        final byte[] digest;

        try {
            final Mac mac = Mac.getInstance(CACHE_DIGEST_ALGORITHM);

            mac.init(CACHE_DIGEST_KEY);

            digest = mac.doFinal(combinedInput);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available.", e);
        }

        try {
            return Base64.getEncoder().encodeToString(digest);
        } finally {
            Arrays.fill(digest, (byte) 0);
        }
    }

    /**
     * Generates the process-local key used to derive cache keys.
     *
     * @return the generated key.
     * @throws IllegalStateException if HMAC-SHA256 is unavailable.
     */
    private static SecretKeySpec createCacheDigestKey() {
        final byte[] keyBytes = new byte[32];

        new SecureRandom().nextBytes(keyBytes);

        try {
            return new SecretKeySpec(keyBytes, CACHE_DIGEST_ALGORITHM);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    /**
     * Removes every expired entry from the default in-memory cache.
     */
    private static void evictExpiredCacheEntries() {
        final long now = System.currentTimeMillis();

        VERIFY_CACHE_MAP.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
    }
}