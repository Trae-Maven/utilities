package io.github.trae.utilities.mixins;

/**
 * Implemented by objects that carry a system timestamp representing when they were created or started.
 *
 * <p>The returned value is expected to be a {@link System#currentTimeMillis()} snapshot.
 */
public interface SystemTimeMixin {

    long getSystemTime();
}