package io.github.trae.utilities.enums;

import io.github.trae.utilities.UtilString;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an English indefinite article and the pronunciation prefixes
 * used to resolve whether a string should be preceded by {@code "a "} or
 * {@code "an "}.
 *
 * <p>The lookup is based on common English pronunciation rules rather than
 * simple vowel/consonant matching. Known pronunciation exceptions, such as
 * {@code "user"}, {@code "hour"}, and {@code "European"}, are handled using
 * a longest-prefix lookup.</p>
 *
 * <p>Prefix mappings are built once during class initialization. Resolved
 * first words are cached after their first lookup, making repeated calls for
 * the same leading word effectively constant-time.</p>
 */
@AllArgsConstructor
@Getter
public enum IndefiniteArticle {

    A(
            "a ",
            Set.of(
                    // "yoo" sound
                    "uni",
                    "unif",
                    "unio",
                    "unic",
                    "unit",
                    "use",
                    "user",
                    "usual",
                    "usu",
                    "util",
                    "uti",
                    "ufo",
                    "uk",
                    "eu",
                    "eur",
                    "euro",
                    "eul",
                    "eup",
                    "one",
                    "once",
                    "ouija",
                    "ubi",
                    "uran",
                    "urea"
            )
    ),

    AN(
            "an ",
            Set.of(
                    // Default vowel prefixes
                    "a",
                    "e",
                    "i",
                    "o",
                    "u",

                    // Silent h
                    "hour",
                    "hours",
                    "hon",
                    "honest",
                    "honesty",
                    "honor",
                    "honour",
                    "heir",
                    "hei",
                    "herb",

                    // Numbers
                    "8",
                    "11",
                    "18",
                    "80",
                    "800",

                    // Letter pronunciations
                    "A",
                    "E",
                    "F",
                    "H",
                    "I",
                    "L",
                    "M",
                    "N",
                    "O",
                    "R",
                    "S",
                    "X",

                    // Common acronym prefixes
                    "FB",
                    "FC",
                    "FD",
                    "FE",
                    "FH",
                    "FL",

                    "MR",
                    "ML",
                    "MS",

                    "NP",
                    "NS",
                    "NX",

                    "RPG",
                    "SQL",
                    "SOS",
                    "SMS",
                    "SSD",

                    "XP",
                    "XML",
                    "XSS"
            )
    );

    /**
     * Cache of resolved first words to their matching indefinite article.
     */
    private static final ConcurrentHashMap<String, IndefiniteArticle> CACHE_MAP = new ConcurrentHashMap<>(64);

    /**
     * Immutable lookup table of lowercase pronunciation prefixes to their
     * matching indefinite article.
     */
    private static final Map<String, IndefiniteArticle> BY_PREFIX_MAP;

    /**
     * Length of the longest configured pronunciation prefix.
     */
    private static final int MAX_PREFIX_LENGTH;

    /*
     * Builds the immutable prefix lookup table and records the longest
     * configured prefix length for bounded runtime lookups.
     */
    static {
        final Map<String, IndefiniteArticle> map = new HashMap<>(128);

        int longest = 0;

        for (final IndefiniteArticle article : values()) {
            for (final String prefix : article.prefixes) {
                final String lower = prefix.toLowerCase(Locale.ROOT);

                map.put(lower, article);
                longest = Math.max(longest, lower.length());
            }
        }

        BY_PREFIX_MAP = Map.copyOf(map);
        MAX_PREFIX_LENGTH = longest;
    }

    /**
     * The article text, including the trailing space.
     */
    private final String article;

    /**
     * Pronunciation prefixes that resolve to this article.
     */
    private final Set<String> prefixes;

    /**
     * Returns the correct indefinite article for the supplied string.
     *
     * <p>Only the first word of the input is inspected when determining the
     * appropriate article. The returned article includes its trailing space.</p>
     *
     * @param input the string to determine an indefinite article for
     * @return {@code "a "} or {@code "an "}, or an empty string if the input
     * is blank or {@code null}
     */
    public static String get(final String input) {
        if (UtilString.isEmpty(input)) {
            return "";
        }

        final int separator = input.indexOf(' ');

        final String firstWord = input.substring(0, separator == -1 ? input.length() : separator).toLowerCase(Locale.ROOT);

        return CACHE_MAP.computeIfAbsent(firstWord, key -> {
            for (int length = Math.min(MAX_PREFIX_LENGTH, key.length()); length > 0; length--) {
                final IndefiniteArticle indefiniteArticle = BY_PREFIX_MAP.get(key.substring(0, length));

                if (indefiniteArticle != null) {
                    return indefiniteArticle;
                }
            }

            return A;
        }).getArticle();
    }

    /**
     * Prepends the correct indefinite article ("a" or "an") to the supplied
     * string.
     *
     * <p>Only the first word of the input is inspected when determining the
     * appropriate article. The input itself is otherwise preserved exactly as
     * provided.</p>
     *
     * @param input the string to prepend an indefinite article to
     * @return the formatted string, or {@code null} if the input is blank or
     * {@code null}
     */
    public static String format(final String input) {
        return UtilString.isEmpty(input) ? input : get(input) + input;
    }
}