package io.github.trae.utilities;

import io.github.trae.utilities.enums.TimeUnit;
import lombok.experimental.UtilityClass;

/**
 * Utility methods for time formatting and elapsed time checks.
 */
@UtilityClass
public class UtilTime {

    /**
     * Formats a duration into a human-readable string using the best-fit {@link TimeUnit}.
     *
     * @param duration the raw duration value
     * @param timeUnit the {@link TimeUnit} of {@code duration}
     * @param trim     decimal places to include; {@code 0} produces whole numbers
     * @return a formatted string such as {@code "5 minutes"} or {@code "5.0 minutes"}
     */
    public static String getTime(final long duration, final TimeUnit timeUnit, final int trim) {
        return TimeUnit.format(duration * timeUnit.getDuration(), trim);
    }

    /**
     * Formats a duration into a human-readable string with no decimal places.
     *
     * @param duration the raw duration value
     * @param timeUnit the {@link TimeUnit} of {@code duration}
     * @return a formatted string such as {@code "5 minutes"}
     */
    public static String getTime(final long duration, final TimeUnit timeUnit) {
        return getTime(duration, timeUnit, 0);
    }

    /**
     * Formats a millisecond duration into a human-readable string using the best-fit {@link TimeUnit}.
     *
     * @param duration the duration in milliseconds
     * @param trim     decimal places to include; {@code 0} produces whole numbers
     * @return a formatted string such as {@code "5.0 minutes"}
     */
    public static String getTime(final long duration, final int trim) {
        return TimeUnit.format(duration, trim);
    }

    /**
     * Formats a millisecond duration into a human-readable string using the best-fit {@link TimeUnit}.
     *
     * @param duration the duration in milliseconds
     * @return a formatted string such as {@code "5 minutes"}
     */
    public static String getTime(final long duration) {
        return TimeUnit.format(duration, 0);
    }

    /**
     * Returns whether the required number of milliseconds has elapsed since {@code from}.
     *
     * @param from     the start timestamp in milliseconds ({@link System#currentTimeMillis()})
     * @param required the required elapsed duration in milliseconds
     * @return {@code true} if the elapsed time is greater than or equal to {@code required}
     */
    public static boolean elapsed(final long from, final long required) {
        return (System.currentTimeMillis() - from) >= required;
    }

    /**
     * Returns the number of milliseconds remaining until {@code duration} has elapsed since {@code systemTime}.
     *
     * @param systemTime the start timestamp in milliseconds ({@link System#currentTimeMillis()})
     * @param duration   the total duration in milliseconds
     * @return milliseconds remaining; negative if already elapsed
     */
    public static long getRemaining(final long systemTime, final long duration) {
        return (systemTime + duration) - System.currentTimeMillis();
    }
}