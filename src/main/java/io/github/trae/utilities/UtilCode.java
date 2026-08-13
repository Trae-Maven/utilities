package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

/**
 * Utility methods for generating random alphanumeric codes.
 *
 * <p>All codes are drawn from a shared {@link SecureRandom}, making them suitable
 * for security-sensitive uses such as verification codes, password-reset tokens,
 * and invite codes. {@link SecureRandom} is thread-safe, so a single instance is
 * shared across all calls.</p>
 *
 * <p>Characters are selected with {@link SecureRandom#nextInt(int)} rather than a
 * modulo of a wider value, so the distribution across the alphabet is uniform with
 * no bias toward its earlier characters.</p>
 *
 * <p><b>Entropy.</b> A code of {@code n} characters drawn from an alphabet of
 * {@code k} characters carries {@code n * log2(k)} bits. For the 36-character
 * uppercase alphabet that is roughly 5.17 bits per character; for the 62-character
 * mixed-case alphabet, roughly 5.95. Size the length against the use: a code that
 * can be guessed online needs either enough length or an attempt limit around it.</p>
 */
@UtilityClass
public class UtilCode {

    /**
     * Shared source of randomness for every generated code.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Uppercase letters and digits.
     *
     * <p>Suited to codes a person has to read aloud or retype, since case does not
     * need to be preserved. Note that it still contains the visually similar
     * {@code O}/{@code 0} and {@code I}/{@code 1} pairs.</p>
     */
    private static final char[] UPPERCASE_ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    /**
     * Mixed-case letters and digits.
     *
     * <p>Carries more entropy per character than {@link #UPPERCASE_ALPHANUMERIC},
     * so it suits machine-handled values such as tokens embedded in links.</p>
     */
    private static final char[] FULL_ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /**
     * Generates a random code of uppercase letters and digits.
     *
     * @param length the number of characters to generate.
     * @return the generated code.
     * @throws IllegalArgumentException if the length is not greater than zero.
     */
    public static String generateUpperCase(final int length) {
        return generate(length, UPPERCASE_ALPHANUMERIC);
    }

    /**
     * Generates a random code of mixed-case letters and digits.
     *
     * @param length the number of characters to generate.
     * @return the generated code.
     * @throws IllegalArgumentException if the length is not greater than zero.
     */
    public static String generateRandom(final int length) {
        return generate(length, FULL_ALPHANUMERIC);
    }

    /**
     * Generates a random code by drawing characters from the supplied alphabet.
     *
     * <p>Each position is chosen independently, so a character may repeat within a
     * single code.</p>
     *
     * @param length   the number of characters to generate.
     * @param alphabet the characters to draw from.
     * @return the generated code.
     * @throws IllegalArgumentException if the length is not greater than zero.
     */
    private static String generate(final int length, final char[] alphabet) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be > 0");
        }

        final char[] output = new char[length];

        for (int i = 0; i < length; i++) {
            output[i] = alphabet[SECURE_RANDOM.nextInt(alphabet.length)];
        }

        return new String(output);
    }
}