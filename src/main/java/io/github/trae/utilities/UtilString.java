package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe string utility methods with built-in caching.
 *
 * <p>Provides common string transformations such as title-casing,
 * stripping delimiters, and splitting camelCase/PascalCase into
 * human-readable form. Results are cached in {@link ConcurrentHashMap}
 * instances for repeated lookups.</p>
 *
 * <p><b>Note:</b> Caches are unbounded. Best suited for a finite,
 * predictable set of input strings (e.g. enum names, config keys).</p>
 */
@UtilityClass
public class UtilString {

    /**
     * Cache for {@link #clean(String)} results, keyed by lowercase (ROOT locale) input.
     */
    private static final ConcurrentHashMap<String, String> CLEAN_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for {@link #slice(String)} results, keyed by raw input.
     */
    private static final ConcurrentHashMap<String, String> SLICE_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for {@link #unSlice(String)} results, keyed by raw input.
     */
    private static final ConcurrentHashMap<String, String> UN_SLICE_CACHE = new ConcurrentHashMap<>();

    /**
     * Converts a string into title case, treating underscores and
     * whitespace as word separators.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "hello_world"} → {@code "Hello World"}</li>
     *   <li>{@code "SOME_CONSTANT"} → {@code "Some Constant"}</li>
     *   <li>{@code "already clean"} → {@code "Already Clean"}</li>
     * </ul>
     *
     * @param string the input string to clean
     * @return the title-cased string, or {@code null} if the input is blank/null
     */
    public static String clean(final String string) {
        if (isEmpty(string)) {
            return null;
        }

        final String key = string.toLowerCase(Locale.ROOT);

        return CLEAN_CACHE.computeIfAbsent(key, k -> {
            final String replaced = k.replace("_", " ");
            return Arrays.stream(replaced.split("\\s+"))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
        });
    }

    /**
     * Strips all spaces, underscores, and dots from a string.
     *
     * <p>Useful for normalising identifiers or keys that may use
     * mixed delimiter styles.</p>
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "some_key.name"} → {@code "somekeyname"}</li>
     *   <li>{@code "hello world"} → {@code "helloworld"}</li>
     * </ul>
     *
     * @param string the input string to slice
     * @return the stripped string, or {@code null} if the input is blank/null
     */
    public static String slice(final String string) {
        if (isEmpty(string)) {
            return null;
        }

        return SLICE_CACHE.computeIfAbsent(string, k -> k.replaceAll("[ _.]", ""));
    }

    /**
     * Expands a camelCase or PascalCase string into space-separated words,
     * also replacing underscores with spaces.
     *
     * <p>Handles consecutive uppercase runs intelligently so that
     * acronyms stay grouped (e.g. "XML" remains together).</p>
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "helloWorld"} → {@code "hello World"}</li>
     *   <li>{@code "XMLParser"} → {@code "XML Parser"}</li>
     *   <li>{@code "some_key"} → {@code "some key"}</li>
     * </ul>
     *
     * @param string the input string to un-slice
     * @return the expanded string, or {@code null} if the input is blank/null
     */
    public static String unSlice(final String string) {
        if (isEmpty(string)) {
            return null;
        }

        return UN_SLICE_CACHE.computeIfAbsent(string, k -> {
            final String replaced = k.replace("_", " ");
            final StringBuilder builder = new StringBuilder();
            final char[] characters = replaced.toCharArray();

            for (int index = 0; index < characters.length; index++) {
                final char current = characters[index];

                // Insert a space before an uppercase letter when it marks a new word boundary.
                // A boundary exists when the previous char is lowercase ("helloWorld"),
                // or when the next char is lowercase, and we're mid-acronym ("XMLParser").
                if (index > 0
                        && Character.isUpperCase(current)
                        && !Character.isWhitespace(characters[index - 1])) {

                    final boolean previousIsLower = Character.isLowerCase(characters[index - 1]);
                    final boolean nextIsLower = index + 1 < characters.length
                            && Character.isLowerCase(characters[index + 1]);

                    if (previousIsLower || nextIsLower) {
                        builder.append(' ');
                    }
                }

                builder.append(current);
            }

            return builder.toString();
        });
    }

    /**
     * Checks whether a string is null, empty, or contains only whitespace.
     *
     * @param string the string to check
     * @return {@code true} if the string is null, empty, or blank
     */
    public static boolean isEmpty(final String string) {
        return string == null || string.isBlank();
    }

    /**
     * Formats a key-value pair as {@code "key: value"}.
     *
     * <p>Intended as a quick helper for building human-readable
     * diagnostic or display strings.</p>
     *
     * @param key   the label
     * @param value the value
     * @return the formatted pair string
     */
    public static String pair(final String key, final String value) {
        return "%s: %s".formatted(key, value);
    }
}