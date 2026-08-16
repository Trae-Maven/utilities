package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility methods for number formatting and mathematical calculations.
 */
@UtilityClass
public class UtilNumber {

    /**
     * Formats a number using the specified {@link java.text.DecimalFormat} pattern.
     *
     * <p>Formatting is locale-independent: {@link java.util.Locale#ROOT} symbols are used, so the
     * decimal separator is always {@code .} and the grouping separator always {@code ,}, regardless
     * of the default locale of the host.
     *
     * @param format the decimal format pattern (e.g. {@code "#.##"}, {@code "#,###.00"})
     * @param value  the number to format
     * @return the formatted number as a string
     * @throws IllegalArgumentException if {@code format} is not a valid pattern
     * @throws NullPointerException     if {@code format} or {@code value} is {@code null}
     */
    public static String format(final String format, final Number value) {
        return new DecimalFormat(format, DecimalFormatSymbols.getInstance(Locale.ROOT)).format(value);
    }

    /**
     * Constrains a value to lie within an inclusive range.
     *
     * @param minimum the lower bound (inclusive)
     * @param maximum the upper bound (inclusive)
     * @param value   the value to clamp
     * @param <T>     a {@link Number} type that is also {@link Comparable} with itself
     * @return {@code minimum} if {@code value} is below the range, {@code maximum} if above, otherwise {@code value}
     * @throws IllegalArgumentException if {@code minimum}, {@code maximum}, or {@code value} is {@code null}
     */
    public static <T extends Number & Comparable<T>> T clamp(final T minimum, final T maximum, final T value) {
        if (minimum == null) {
            throw new IllegalArgumentException("Minimum cannot be null.");
        }

        if (maximum == null) {
            throw new IllegalArgumentException("Maximum cannot be null.");
        }

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }

        if (value.compareTo(minimum) < 0) {
            return minimum;
        }

        if (value.compareTo(maximum) > 0) {
            return maximum;
        }

        return value;
    }

    /**
     * Generates a pseudo-random number of the requested type within the half-open range
     * {@code [minimum, maximum)}, using the current thread's {@link ThreadLocalRandom}.
     *
     * <p>Supported types are {@link Integer}, {@link Long}, {@link Double}, and {@link Float}.
     *
     * @param clazz   the numeric type to generate; determines which range method is used
     * @param minimum the lower bound (inclusive)
     * @param maximum the upper bound (exclusive)
     * @param <T>     a {@link Number} type matching {@code clazz}
     * @return a random value of type {@code T} within the specified range
     * @throws IllegalArgumentException if {@code clazz} is not a supported numeric type,
     *                                  or if {@code minimum} is not less than {@code maximum}
     */
    public static <T extends Number> T getRandomNumber(final Class<T> clazz, final T minimum, final T maximum) {
        final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

        if (Integer.class.equals(clazz)) {
            return UtilJava.cast(clazz, threadLocalRandom.nextInt(minimum.intValue(), maximum.intValue()));
        }

        if (Long.class.equals(clazz)) {
            return UtilJava.cast(clazz, threadLocalRandom.nextLong(minimum.longValue(), maximum.longValue()));
        }

        if (Double.class.equals(clazz)) {
            return UtilJava.cast(clazz, threadLocalRandom.nextDouble(minimum.doubleValue(), maximum.doubleValue()));
        }

        if (Float.class.equals(clazz)) {
            return UtilJava.cast(clazz, threadLocalRandom.nextFloat(minimum.floatValue(), maximum.floatValue()));
        }

        throw new IllegalArgumentException("Unsupported numeric type: %s".formatted(clazz.getName()));
    }
}