package io.github.trae.utilities.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A loosely typed bag of named values.
 *
 * <p>For attaching arbitrary state to something that cannot be given a field for
 * it, such as a framework object a consumer wants to hang its own data off. The
 * cost of that freedom is that nothing checks a key against a type at compile
 * time, so a caller reading a key someone else wrote has no guarantee it holds
 * what they expect.</p>
 *
 * <p>A read names the type it wants, and a key holding anything else is treated
 * as unset rather than throwing, so a reader is never broken by a writer it does
 * not control. Prefer a real field wherever one is possible.</p>
 *
 * <p>Not thread safe. A value shared across threads needs its own
 * synchronisation.</p>
 */
public class Data {

    /**
     * The values, by key. Untyped, which is the point.
     */
    private final Map<String, Object> map = new HashMap<>();

    /**
     * Stores a value, replacing whatever the key held before.
     *
     * @param key   the name to store it under
     * @param value the value to store
     */
    public final void put(final String key, final Object value) {
        this.map.put(key, value);
    }

    /**
     * Removes a key, doing nothing if it was never set.
     *
     * @param key the name to remove
     */
    public final void remove(final String key) {
        this.map.remove(key);
    }

    /**
     * Reads a value of the expected type, falling back to the given default.
     *
     * <p>The fallback covers both an unset key and one holding another type,
     * since neither gives the caller what it asked for.</p>
     *
     * @param <T>          the expected type
     * @param clazz        the class to match against
     * @param key          the name to read
     * @param defaultValue what to use when the key is unset or holds another
     *                     type, may be {@code null}
     * @return the value, or the default, or empty when that default is
     * {@code null}
     */
    public final <T> Optional<T> get(final Class<T> clazz, final String key, final T defaultValue) {
        return Optional.ofNullable(this.map.get(key))
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .or(() -> Optional.ofNullable(defaultValue));
    }

    /**
     * Reads a value of the expected type, with no fallback.
     *
     * @param <T>   the expected type
     * @param clazz the class to match against
     * @param key   the name to read
     * @return the value, or empty if the key is unset or holds another type
     */
    public final <T> Optional<T> get(final Class<T> clazz, final String key) {
        return this.get(clazz, key, null);
    }

    /**
     * Whether a key has been set, whatever it holds.
     *
     * <p>Answers a different question from {@link #get(Class, String)} returning
     * empty, which also covers a key set to the wrong type.</p>
     *
     * @param key the name to check
     * @return whether the key is set
     */
    public final boolean contains(final String key) {
        return this.map.containsKey(key);
    }
}