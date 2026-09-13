package io.github.trae.utilities.mixins;

import io.github.trae.utilities.UtilTime;

/**
 * Implemented by objects that can report how much time remains on their duration.
 *
 * <p>Composes {@link SystemTimeMixin} and {@link DurationMixin} to delegate remaining
 * time calculation to {@link UtilTime#getRemaining(long, long)}.
 */
public interface RemainingMixin extends SystemTimeMixin, DurationMixin {

    /**
     * Returns the number of milliseconds remaining until the duration elapses.
     *
     * <p>Returns a negative value if the duration has already elapsed, and {@link Long#MAX_VALUE}
     * for a permanent duration of {@code -1}, since the arithmetic against a sentinel would
     * otherwise produce a figure with no meaning.
     *
     * @return milliseconds remaining
     */
    default long getRemaining() {
        if (this.getDuration() == -1L) {
            return Long.MAX_VALUE;
        }

        return UtilTime.getRemaining(this.getSystemTime(), this.getDuration());
    }

    /**
     * Returns the remaining duration formatted as a human-readable string.
     *
     * <p>Uses one decimal place and the best-fit time unit, e.g. {@code "2.0 minutes"}. A permanent
     * duration reads as {@code "Permanent"} rather than as a figure.
     *
     * @return a formatted remaining duration string
     */
    default String getRemainingString() {
        if (this.getDuration() == -1L) {
            return "Permanent";
        }

        return UtilTime.getReadableTime(this.getRemaining(), 1);
    }
}