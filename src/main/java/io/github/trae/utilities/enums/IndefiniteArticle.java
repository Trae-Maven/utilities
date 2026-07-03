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
 * Represents an English indefinite article ("a" or "an") together with
 * the pronunciation prefixes that determine when it should be used.
 *
 * <p>The lookup is based on common English pronunciation rules rather
 * than simple vowel/consonant matching. Known pronunciation exceptions
 * (such as {@code "user"}, {@code "hour"}, and {@code "European"}) are
 * resolved using a longest-prefix lookup and cached for subsequent
 * requests.</p>
 *
 * <p>Prefix lookups are computed once during class initialization and
 * stored in an immutable lookup table. The first word of each formatted
 * string is cached after its initial lookup, making repeated formatting
 * effectively constant time.</p>
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
     * Cache of previously resolved first words to their corresponding
     * indefinite article.
     */
    private static final ConcurrentHashMap<String, IndefiniteArticle> CACHE_MAP = new ConcurrentHashMap<>(64);

    /**
     * Immutable lookup table mapping lowercase pronunciation prefixes
     * to their corresponding indefinite article.
     */
    private static final Map<String, IndefiniteArticle> BY_PREFIX_MAP;

    /**
     * Length of the longest registered pronunciation prefix.
     */
    private static final int MAX_PREFIX_LENGTH;

    /*
     * Builds the immutable pronunciation prefix lookup table and determines
     * the length of the longest registered prefix.
     *
     * Each configured prefix is normalised to lowercase and mapped to its
     * corresponding indefinite article. The longest prefix length is cached
     * so runtime lookups only inspect the minimum number of characters
     * required.
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
     * The article text, including a trailing space.
     */
    private final String article;

    /**
     * Pronunciation prefixes that indicate this article should be used.
     */
    private final Set<String> prefixes;

    /**
     * Prepends the correct indefinite article ("a" or "an") to the supplied
     * string.
     *
     * <p>Only the first word of the input is inspected when determining the
     * appropriate article. The remaining characters are preserved exactly as
     * provided.</p>
     *
     * <p>Resolved first words are cached, making subsequent calls for the
     * same leading word effectively constant-time.</p>
     *
     * @param input the string to prepend an indefinite article to
     * @return the formatted string, or {@code null} if the input is blank or
     * {@code null}
     */
    public static String format(final String input) {
        if (UtilString.isEmpty(input)) {
            return input;
        }

        final int separator = input.indexOf(' ');

        final String firstWord = input.substring(0, separator == -1 ? input.length() : separator).toLowerCase(Locale.ROOT);

        return CACHE_MAP.computeIfAbsent(firstWord, key -> {
            for (int length = Math.min(MAX_PREFIX_LENGTH, key.length()); length > 0; length--) {
                final IndefiniteArticle article = BY_PREFIX_MAP.get(key.substring(0, length));

                if (article != null) {
                    return article;
                }
            }

            return A;
        }).getArticle() + input;
    }
}