package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 encoding and decoding utility.
 *
 * <p>Wraps the JDK's {@link Base64} encoder and decoder with convenience methods
 * for common input/output type combinations (byte[], String).</p>
 *
 * <p>Encoder and decoder instances are cached as static finals — they are
 * thread-safe and reused across all calls.</p>
 */
@UtilityClass
public class UtilBase64 {

    /**
     * Cached thread-safe Base64 encoder instance.
     */
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    /**
     * Cached thread-safe Base64 decoder instance.
     */
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    // =============================
    // ENCODE
    // =============================

    /**
     * Encode a byte array to Base64 bytes.
     *
     * @param input the raw bytes to encode
     * @return the Base64-encoded bytes
     */
    public static byte[] encodeToBytes(final byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        return ENCODER.encode(input);
    }

    /**
     * Encode a string to Base64 bytes using UTF-8.
     *
     * @param input the string to encode
     * @return the Base64-encoded bytes
     */
    public static byte[] encodeToBytes(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        return encodeToBytes(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encode a byte array to a Base64 string.
     *
     * @param input the raw bytes to encode
     * @return the Base64-encoded string
     */
    public static String encodeToString(final byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        return ENCODER.encodeToString(input);
    }

    /**
     * Encode a string to a Base64 string using UTF-8.
     *
     * @param input the string to encode
     * @return the Base64-encoded string
     */
    public static String encodeToString(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        return encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    // =============================
    // DECODE
    // =============================

    /**
     * Decode Base64 bytes to raw bytes.
     *
     * @param input the Base64-encoded bytes
     * @return the decoded raw bytes
     */
    public static byte[] decodeToBytes(final byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        return DECODER.decode(input);
    }

    /**
     * Decode a Base64 string to raw bytes.
     *
     * @param input the Base64-encoded string
     * @return the decoded raw bytes
     */
    public static byte[] decodeToBytes(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        return decodeToBytes(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode Base64 bytes to a UTF-8 string.
     *
     * @param input the Base64-encoded bytes
     * @return the decoded string
     */
    public static String decodeToString(final byte[] input) {
        return new String(decodeToBytes(input), StandardCharsets.UTF_8);
    }

    /**
     * Decode a Base64 string to a UTF-8 string.
     *
     * @param input the Base64-encoded string
     * @return the decoded string
     */
    public static String decodeToString(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }

        return decodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}