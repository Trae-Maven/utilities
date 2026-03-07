package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cryptographic hashing and verification utility.
 *
 * <p>Provides methods for hashing (SHA-256, SHA-512, etc.), HMAC computation,
 * hex encoding/decoding, and constant-time hash verification.</p>
 *
 * <p>{@link MessageDigest} and {@link Mac} instances are cached as prototypes
 * and cloned per call, avoiding repeated provider lookups while remaining
 * thread-safe.</p>
 *
 * <p>All verification methods use {@link MessageDigest#isEqual(byte[], byte[])}
 * for constant-time comparison to prevent timing side-channel attacks.</p>
 */
@UtilityClass
public final class UtilHash {

    /**
     * Cached MessageDigest prototypes keyed by algorithm name.
     */
    private static final ConcurrentHashMap<String, MessageDigest> MESSAGE_DIGEST_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * Cached Mac prototypes keyed by algorithm name.
     */
    private static final ConcurrentHashMap<String, Mac> MAC_CACHE_MAP = new ConcurrentHashMap<>();

    // =============================
    // HASH
    // =============================

    /**
     * Hash a byte array using the specified algorithm.
     *
     * @param algorithm the hash algorithm (e.g. "SHA-256", "SHA-512")
     * @param bytes     the input bytes to hash
     * @return the raw hash bytes
     * @throws IllegalStateException if the algorithm is not supported
     */
    public static byte[] hashToBytes(final String algorithm, final byte[] bytes) {
        return getMessageDigest(algorithm).digest(bytes);
    }

    /**
     * Hash a string using the specified algorithm.
     *
     * @param algorithm the hash algorithm (e.g. "SHA-256", "SHA-512")
     * @param string    the input string to hash (encoded as UTF-8)
     * @return the raw hash bytes
     */
    public static byte[] hashToBytes(final String algorithm, final String string) {
        return hashToBytes(algorithm, string.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Hash a byte array and return the result as a lowercase hex string.
     *
     * @param algorithm the hash algorithm
     * @param bytes     the input bytes to hash
     * @return the hash as a lowercase hex string
     */
    public static String hashToString(final String algorithm, final byte[] bytes) {
        return toHex(hashToBytes(algorithm, bytes));
    }

    /**
     * Hash a string and return the result as a lowercase hex string.
     *
     * @param algorithm the hash algorithm
     * @param string    the input string to hash (encoded as UTF-8)
     * @return the hash as a lowercase hex string
     */
    public static String hashToString(final String algorithm, final String string) {
        return hashToString(algorithm, string.getBytes(StandardCharsets.UTF_8));
    }

    // =============================
    // VERIFY
    // =============================

    /**
     * Verify a plain byte array against a stored hash using
     * constant-time comparison to prevent timing attacks.
     *
     * @param algorithm   the hash algorithm used to produce the stored hash
     * @param plainBytes  the plain input bytes to verify
     * @param storedBytes the previously stored hash bytes to compare against
     * @return true if the hash of plainBytes matches storedBytes
     */
    public static boolean verify(final String algorithm, final byte[] plainBytes, final byte[] storedBytes) {
        return MessageDigest.isEqual(storedBytes, hashToBytes(algorithm, plainBytes));
    }

    /**
     * Verify a plain string against a stored hex hash using
     * constant-time comparison to prevent timing attacks.
     *
     * @param algorithm    the hash algorithm used to produce the stored hash
     * @param plainString  the plain input string to verify
     * @param storedString the previously stored hash as a hex string
     * @return true if the hash of plainString matches storedString
     */
    public static boolean verify(final String algorithm, final String plainString, final String storedString) {
        final byte[] storedBytes = fromHex(storedString);
        final byte[] computed = hashToBytes(algorithm, plainString);

        return MessageDigest.isEqual(storedBytes, computed);
    }

    // =============================
    // HMAC
    // =============================

    /**
     * Compute an HMAC and return the result as a Base64 string.
     *
     * @param algorithm the HMAC algorithm (e.g. "HmacSHA256", "HmacSHA512")
     * @param key       the HMAC secret key
     * @param input     the input string to authenticate
     * @return the HMAC result as a Base64-encoded string
     * @throws IllegalStateException if the algorithm is not supported
     *                               or the key is invalid
     */
    public static String hmac(final String algorithm, final String key, final String input) {
        try {
            final Mac mac = getMac(algorithm);

            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm));

            final byte[] hashBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            return UtilBase64.encodeToString(hashBytes);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to compute HMAC using %s".formatted(algorithm), e);
        }
    }

    // =============================
    // HEX
    // =============================

    /**
     * Encode a byte array as a lowercase hex string.
     *
     * <p>Uses {@link Character#forDigit(int, int)} for fast nibble-level
     * conversion without format string overhead.</p>
     *
     * @param bytes the bytes to encode
     * @return the lowercase hex string (length = bytes.length * 2)
     */
    public static String toHex(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (final byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }

        return sb.toString();
    }

    /**
     * Decode a hex string to a byte array.
     *
     * @param hex the hex string (must have even length)
     * @return the decoded bytes
     * @throws IllegalArgumentException if the hex string has odd length
     */
    public static byte[] fromHex(final String hex) {
        final int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }

        final byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }

        return out;
    }

    // =============================
    // INTERNAL
    // =============================

    /**
     * Returns a thread-safe {@link MessageDigest} instance for the given
     * algorithm. A prototype is cached on first access and cloned on
     * each subsequent call to avoid provider lookup overhead.
     *
     * @param algorithm the digest algorithm name
     * @return a fresh, cloned MessageDigest instance
     * @throws IllegalStateException if the algorithm is not supported
     *                               or cloning fails
     */
    private static MessageDigest getMessageDigest(final String algorithm) {
        try {
            return UtilJava.cast(MessageDigest.class, MESSAGE_DIGEST_CACHE_MAP.computeIfAbsent(algorithm.toUpperCase(Locale.ROOT), key -> {
                try {
                    return MessageDigest.getInstance(key);
                } catch (final NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Unsupported MessageDigest algorithm: %s".formatted(key), e);
                }
            }).clone());
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException("MessageDigest does not support cloning: %s".formatted(algorithm), e);
        }
    }

    /**
     * Returns a thread-safe {@link Mac} instance for the given algorithm.
     * A prototype is cached on first access and cloned on each subsequent
     * call to avoid provider lookup overhead.
     *
     * @param algorithm the MAC algorithm name
     * @return a fresh, cloned Mac instance
     * @throws IllegalStateException if the algorithm is not supported
     *                               or cloning fails
     */
    private static Mac getMac(final String algorithm) {
        try {
            return UtilJava.cast(Mac.class, MAC_CACHE_MAP.computeIfAbsent(algorithm.toUpperCase(Locale.ROOT), key -> {
                try {
                    return Mac.getInstance(key);
                } catch (final NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Unsupported Mac algorithm: %s".formatted(key), e);
                }
            }).clone());
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException("Mac does not support cloning: %s".formatted(algorithm), e);
        }
    }
}