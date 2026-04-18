package io.github.trae.utilities.impl;

import io.github.trae.utilities.UtilTime;

/**
 * Implemented by objects that carry a duration in milliseconds.
 *
 * <p>Provides a default {@link #getDurationString()} method that formats the duration
 * into a human-readable string using the best-fit {@link io.github.trae.utilities.enums.TimeUnit}.
 */
public interface DurationImpl {

    long getDuration();

    /**
     * Returns the duration formatted as a human-readable string.
     *
     * <p>Uses one decimal place and the best-fit time unit, e.g. {@code "5.0 minutes"}.
     *
     * @return a formatted duration string
     */
    default String getDurationString() {
        return UtilTime.getTime(this.getDuration(), 1);
    }
}