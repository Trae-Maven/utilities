package io.github.trae.utilities.mixins;

import io.github.trae.utilities.UtilTime;

/**
 * Implemented by objects that can determine whether their duration has elapsed.
 *
 * <p>Composes {@link SystemTimeMixin} and {@link DurationMixin} to delegate expiry
 * checking to {@link UtilTime#elapsed(long, long)}.
 */
public interface ExpiredMixin extends SystemTimeMixin, DurationMixin {

    /**
     * Returns whether the duration has fully elapsed since {@link #getSystemTime()}.
     *
     * <p>A duration of {@code -1} never elapses, and one of {@code 0} has always elapsed. Anything
     * else compares against the wall clock.
     *
     * @return {@code true} if the duration has elapsed; {@code false} otherwise
     */
    default boolean hasExpired() {
        return UtilTime.elapsed(this.getSystemTime(), this.getDuration());
    }
}