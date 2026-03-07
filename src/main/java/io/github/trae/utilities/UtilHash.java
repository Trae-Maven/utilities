package io.github.trae.utilities;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cryptographic hashing and verification utility.
 *
 * <p>Provides methods for hashing (SHA-256, SHA-512, etc.), HMAC computation,
 * hex encoding/decoding, and constant-time hash verification.</p>
 *
 * <p>All verification methods use {@link MessageDigest#isEqual(byte[], byte[])}
 * for constant-time comparison to prevent timing side-channel attacks.</p>
 */
public final class UtilHash {

    /**
     * Hash a byte array using the specified algorithm.
     *
     * @param algorithm the hash algorithm (e.g. "SHA-256", "SHA-512")
     * @param bytes     the input bytes to hash
     * @return the raw hash bytes
     * @throws IllegalArgumentException if the algorithm is not supported
     */
    public static byte[] hashToBytes(final String algorithm, final byte[] bytes) {
        try {
            return MessageDigest.getInstance(algorithm).digest(bytes);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm, e);
        }
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

    /**
     * Verify a plain byte array against a stored hash using constant-time comparison.
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
     * Verify a plain string against a stored hex hash using constant-time comparison.
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

    /**
     * Compute an HMAC using the specified algorithm and return the result as a Base64 string.
     *
     * @param algorithm the HMAC algorithm (e.g. "HmacSHA256", "HmacSHA512")
     * @param key       the HMAC secret key
     * @param input     the input string to authenticate
     * @return the HMAC result as a Base64-encoded string
     */
    public static String hmac(final String algorithm, final String key, final String input) {
        try {
            final Mac mac = Mac.getInstance(algorithm);

            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm));

            final byte[] hashBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            return UtilBase64.encodeToString(hashBytes);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to compute HMAC using %s".formatted(algorithm), e);
        }
    }

    /**
     * Encode a byte array as a lowercase hex string.
     *
     * <p>Uses {@link Character#forDigit(int, int)} for fast conversion
     * without format string overhead.</p>
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
    private static byte[] fromHex(final String hex) {
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
}