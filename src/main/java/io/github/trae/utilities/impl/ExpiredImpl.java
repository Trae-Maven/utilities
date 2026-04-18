package io.github.trae.utilities.impl;

import io.github.trae.utilities.UtilTime;

/**
 * Implemented by objects that can determine whether their duration has elapsed.
 *
 * <p>Composes {@link SystemTimeImpl} and {@link DurationImpl} to delegate expiry
 * checking to {@link UtilTime#elapsed(long, long)}.
 */
public interface ExpiredImpl extends SystemTimeImpl, DurationImpl {

    /**
     * Returns whether the duration has fully elapsed since {@link #getSystemTime()}.
     *
     * <p>Equivalent to {@code System.currentTimeMillis() >= getSystemTime() + getDuration()}.
     *
     * @return {@code true} if the duration has elapsed; {@code false} otherwise
     */
    default boolean hasExpired() {
        return UtilTime.elapsed(this.getSystemTime(), this.getDuration());
    }
}