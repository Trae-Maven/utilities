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
        return TimeUnit.format(duration * timeUnit.getDuration(), trim, true);
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
        return TimeUnit.format(duration, trim, true);
    }

    /**
     * Formats a millisecond duration into a human-readable string using the best-fit {@link TimeUnit}.
     *
     * @param duration the duration in milliseconds
     * @return a formatted string such as {@code "5 minutes"}
     */
    public static String getTime(final long duration) {
        return TimeUnit.format(duration, 0, true);
    }

    /**
     * Formats a duration into a human-readable string using the best-fit {@link TimeUnit}, never
     * expressing it in milliseconds.
     * <p>
     * The same as {@link #getTime(long, TimeUnit, int)} except that a sub-second duration is
     * expressed in seconds rather than milliseconds, which reads better anywhere a person sees the
     * result rather than a log.
     *
     * @param duration the raw duration value
     * @param timeUnit the {@link TimeUnit} of {@code duration}
     * @param trim     decimal places to include; {@code 0} produces whole numbers
     * @return a formatted string such as {@code "5 minutes"} or {@code "5.0 minutes"}
     */
    public static String getReadableTime(final long duration, final TimeUnit timeUnit, final int trim) {
        return TimeUnit.format(duration * timeUnit.getDuration(), trim, false);
    }

    /**
     * Formats a duration into a human-readable string with no decimal places, never expressing it in
     * milliseconds.
     *
     * @param duration the raw duration value
     * @param timeUnit the {@link TimeUnit} of {@code duration}
     * @return a formatted string such as {@code "5 minutes"}
     */
    public static String getReadableTime(final long duration, final TimeUnit timeUnit) {
        return getReadableTime(duration, timeUnit, 0);
    }

    /**
     * Formats a millisecond duration into a human-readable string using the best-fit
     * {@link TimeUnit}, never expressing it in milliseconds.
     *
     * @param duration the duration in milliseconds
     * @param trim     decimal places to include; {@code 0} produces whole numbers
     * @return a formatted string such as {@code "5.0 minutes"}
     */
    public static String getReadableTime(final long duration, final int trim) {
        return TimeUnit.format(duration, trim, false);
    }

    /**
     * Formats a millisecond duration into a human-readable string using the best-fit
     * {@link TimeUnit}, never expressing it in milliseconds.
     *
     * @param duration the duration in milliseconds
     * @return a formatted string such as {@code "5 minutes"}
     */
    public static String getReadableTime(final long duration) {
        return TimeUnit.format(duration, 0, false);
    }

    /**
     * Returns whether the required number of milliseconds has elapsed since {@code from}.
     *
     * <p>Two values are treated as sentinels rather than durations: {@code -1} never elapses, which
     * is how something permanent is expressed, and {@code 0} has always elapsed. Either value in
     * either parameter short-circuits before the clock is read.
     *
     * @param from     the start timestamp in milliseconds ({@link System#currentTimeMillis()})
     * @param required the required elapsed duration in milliseconds
     * @return {@code true} if the elapsed time is greater than or equal to {@code required}
     */
    public static boolean elapsed(final long from, final long required) {
        if (from == -1L || required == -1L) {
            return false;
        }

        if (from == 0L || required == 0L) {
            return true;
        }

        return (System.currentTimeMillis() - from) >= required;
    }

    /**
     * Returns the number of milliseconds remaining until {@code duration} has elapsed since {@code systemTime}.
     *
     * <p>No sentinel handling here, so a permanent duration of {@code -1} produces a figure with no
     * meaning. Callers that support one should check for it first, as
     * {@link io.github.trae.utilities.mixins.RemainingMixin} does.
     *
     * @param systemTime the start timestamp in milliseconds ({@link System#currentTimeMillis()})
     * @param duration   the total duration in milliseconds
     * @return milliseconds remaining; negative if already elapsed
     */
    public static long getRemaining(final long systemTime, final long duration) {
        return (systemTime + duration) - System.currentTimeMillis();
    }
}