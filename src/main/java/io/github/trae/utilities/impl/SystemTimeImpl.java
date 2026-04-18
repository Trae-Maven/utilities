package io.github.trae.utilities.impl;

/**
 * Implemented by objects that carry a system timestamp representing when they were created or started.
 *
 * <p>The returned value is expected to be a {@link System#currentTimeMillis()} snapshot.
 */
public interface SystemTimeImpl {

    long getSystemTime();
}