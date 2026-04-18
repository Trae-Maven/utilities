package io.github.trae.utilities.impl;

import io.github.trae.utilities.UtilTime;

/**
 * Implemented by objects that can report how much time remains on their duration.
 *
 * <p>Composes {@link SystemTimeImpl} and {@link DurationImpl} to delegate remaining
 * time calculation to {@link UtilTime#getRemaining(long, long)}.
 */
public interface RemainingImpl extends SystemTimeImpl, DurationImpl {

    /**
     * Returns the number of milliseconds remaining until the duration elapses.
     *
     * <p>Returns a negative value if the duration has already elapsed.
     *
     * @return milliseconds remaining
     */
    default long getRemaining() {
        return UtilTime.getRemaining(this.getSystemTime(), this.getDuration());
    }

    /**
     * Returns the remaining duration formatted as a human-readable string.
     *
     * <p>Uses one decimal place and the best-fit time unit, e.g. {@code "2.0 minutes"}.
     *
     * @return a formatted remaining duration string
     */
    default String getRemainingString() {
        return UtilTime.getTime(this.getRemaining(), 1);
    }
}