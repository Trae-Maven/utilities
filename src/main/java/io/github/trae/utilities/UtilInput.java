package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * Utility methods for parsing string input into typed values.
 */
@UtilityClass
public class UtilInput {

    /**
     * Attempts to construct an instance of the given type from a string by invoking its
     * single-argument {@link String} constructor (e.g. {@code new Integer(String)}).
     *
     * <p>If the type has no {@code String} constructor or construction fails (for example,
     * the input is not parseable), an empty {@link Optional} is returned rather than throwing.
     *
     * @param clazz the type to construct; must declare a public {@code (String)} constructor
     * @param input the string to parse
     * @param <T>   the target type
     * @return an {@link Optional} containing the parsed value, or empty if parsing failed
     */
    public static <T> Optional<T> getInput(final Class<T> clazz, final String input) {
        try {
            return Optional.of(clazz.getConstructor(String.class).newInstance(input));
        } catch (final Exception ignored) {
        }

        return Optional.empty();
    }

    /**
     * Parses a string into a number of the given type and clamps it to an inclusive range.
     *
     * @param clazz        the numeric type to parse; must declare a public {@code (String)} constructor
     * @param minimumValue the lower bound (inclusive)
     * @param maximumValue the upper bound (inclusive)
     * @param input        the string to parse
     * @param <T>          a {@link Number} type that is also {@link Comparable} with itself
     * @return an {@link Optional} containing the parsed value clamped to {@code [minimumValue, maximumValue]},
     * or empty if parsing failed
     * @see UtilNumber#clamp(Number, Number, Number)
     */
    public static <T extends Number & Comparable<T>> Optional<T> getNumber(final Class<T> clazz, final T minimumValue, final T maximumValue, final String input) {
        return getInput(clazz, input).map(number -> UtilNumber.clamp(minimumValue, maximumValue, number));
    }
}