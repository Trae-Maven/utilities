package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;

/**
 * Utility methods for number formatting and mathematical calculations.
 */
@UtilityClass
public class UtilNumber {

    /**
     * Formats a number using the specified {@link java.text.DecimalFormat} pattern.
     *
     * @param format the decimal format pattern (e.g. {@code "#.##"}, {@code "#,###.00"})
     * @param value  the number to format
     * @return the formatted number as a string
     */
    public static String format(final String format, final Number value) {
        return new DecimalFormat(format).format(value);
    }
}