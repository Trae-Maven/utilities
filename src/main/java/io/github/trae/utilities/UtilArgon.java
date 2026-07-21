package io.github.trae.utilities;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.github.trae.utilities.objects.consumer.Consumer;
import io.github.trae.utilities.objects.consumer.TriConsumer;
import io.github.trae.utilities.objects.function.BiFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Utility methods for hashing and verifying passwords using the Argon2id algorithm.
 *
 * <p>This class combines the supplied pepper, plain-text password, and salt into a
 * single byte array before performing Argon2 operations. Password verification
 * results may be cached through a pluggable cache implementation to reduce the
 * cost of repeatedly verifying the same credentials.</p>
 *
 * <p>The default cache implementation uses an in-memory
 * {@link ConcurrentHashMap}, but callers may replace the getter, setter, and
 * invalidator with implementations backed by Redis, Caffeine, or any other
 * caching solution.</p>
 *
 * <p>To prevent excessive CPU and memory usage under heavy load, concurrent
 * Argon2 operations are limited by a {@link Semaphore}.</p>
 */
@UtilityClass
public class UtilArgon {

    /**
     * Default in-memory cache of password verification results.
     *
     * <p>The outer map is keyed by the stored Argon2 hash, while the inner map
     * is keyed by the SHA-256 digest of the combined pepper, password, and salt.
     * Each value represents whether the verification succeeded.</p>
     */
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> VERIFY_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached verification result.
     *
     * <p>The default implementation reads from the in-memory cache. This may be
     * replaced to integrate with an external cache implementation.</p>
     */
    @Getter
    @Setter
    private static BiFunction<String, String, Boolean> verifyGetter = (storedHash, digestKey) -> {
        final ConcurrentHashMap<String, Boolean> map = VERIFY_CACHE_MAP.get(storedHash);

        return map == null ? null : map.get(digestKey);
    };

    /**
     * Stores a verification result in the cache.
     *
     * <p>The default implementation stores results in the in-memory cache. This
     * may be replaced to integrate with an external cache implementation.</p>
     */
    @Getter
    @Setter
    private static TriConsumer<String, String, Boolean> verifySetter = (storedHash, digestKey, verified) -> {
        VERIFY_CACHE_MAP.computeIfAbsent(storedHash, __ -> new ConcurrentHashMap<>()).put(digestKey, verified);
    };

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
    private static int iterations = 3;

    /**
     * The number of parallel threads used by Argon2.
     */
    @Getter
    @Setter
    private static int parallelism = 2;

    /**
     * The amount of memory, in kilobytes, allocated for Argon2.
     */
    @Getter
    @Setter
    private static int memoryKb = 1024 * 64;

    /**
     * Limits the number of concurrent Argon2 operations.
     */
    private static final Semaphore ARGON_SEMAPHORE = new Semaphore(8);

    /**
     * Creates a new Argon2id instance.
     *
     * @return a new {@link Argon2} instance.
     */
    private static Argon2 createArgon() {
        return Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    /**
     * Hashes a password using Argon2id.
     *
     * @param plainPassword the plain-text password.
     * @param pepper        the application-wide pepper.
     * @param salt          the user-specific salt.
     * @return the encoded Argon2 hash.
     * @throws IllegalStateException if the hashing thread is interrupted.
     */
    public static String hash(final String plainPassword, final String pepper, final String salt) {
        final byte[] combinedInput = buildInput(plainPassword, pepper, salt);

        try {
            ARGON_SEMAPHORE.acquire();
            try {
                return createArgon().hash(iterations, memoryKb, parallelism, combinedInput);
            } finally {
                ARGON_SEMAPHORE.release();
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
     * <p>A SHA-256 digest of the combined pepper, password, and salt is used as
     * the cache key. If a cached verification result exists, Argon2 verification
     * is skipped.</p>
     *
     * @param plainPassword the plain-text password.
     * @param pepper        the application-wide pepper.
     * @param salt          the user-specific salt.
     * @param storedHash    the stored Argon2 hash.
     * @return {@code true} if the password is valid; otherwise {@code false}.
     * @throws IllegalStateException if SHA-256 is unavailable or the verification
     *                               thread is interrupted.
     */
    public static boolean verify(final String plainPassword, final String pepper, final String salt, final String storedHash) {
        final byte[] combinedInput = buildInput(plainPassword, pepper, salt);

        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(combinedInput);
            final String digestKey = Base64.getEncoder().encodeToString(digest);

            Arrays.fill(digest, (byte) 0);

            final Boolean cachedResult = verifyGetter.apply(storedHash, digestKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            ARGON_SEMAPHORE.acquire();
            try {
                final boolean result = createArgon().verify(storedHash, combinedInput);
                verifySetter.accept(storedHash, digestKey, result);
                return result;
            } finally {
                ARGON_SEMAPHORE.release();
            }
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
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
     * @param salt          the user-specific salt.
     * @param storedHash    the existing Argon2 hash.
     * @return a new Argon2 hash if a rehash is required; otherwise {@code null}.
     */
    public static String tryReHash(final String plainPassword, final String pepper, final String salt, final String storedHash) {
        if (!(createArgon().needsRehash(storedHash, iterations, memoryKb, parallelism))) {
            return null;
        }

        return hash(plainPassword, pepper, salt);
    }

    /**
     * Combines the pepper, password, and salt into a single byte array suitable
     * for Argon2 hashing and verification.
     *
     * <p>Temporary byte arrays are cleared before returning.</p>
     *
     * @param plainPassword the plain-text password.
     * @param pepper        the application-wide pepper.
     * @param salt          the user-specific salt.
     * @return the combined byte array.
     */
    private static byte[] buildInput(final String plainPassword, final String pepper, final String salt) {
        final byte[] passwordBytes = plainPassword.getBytes(StandardCharsets.UTF_8);
        final byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        final byte[] pepperBytes = pepper.getBytes(StandardCharsets.UTF_8);

        final byte[] combinedInput = new byte[pepperBytes.length + passwordBytes.length + saltBytes.length];

        System.arraycopy(pepperBytes, 0, combinedInput, 0, pepperBytes.length);
        System.arraycopy(passwordBytes, 0, combinedInput, pepperBytes.length, passwordBytes.length);
        System.arraycopy(saltBytes, 0, combinedInput, pepperBytes.length + passwordBytes.length, saltBytes.length);

        Arrays.fill(passwordBytes, (byte) 0);
        Arrays.fill(saltBytes, (byte) 0);
        Arrays.fill(pepperBytes, (byte) 0);

        return combinedInput;
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
}