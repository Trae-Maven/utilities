package io.github.trae.utilities.search;

import io.github.trae.utilities.UtilJava;
import io.github.trae.utilities.UtilString;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Base class for name-style lookups over a supplied collection.
 * <p>
 * A search runs in a single pass over the collection returned by the supplier. Each candidate is
 * first tested against an optional filter, then against {@link #isExact(Object, String)}, which
 * short-circuits the search and returns immediately. Remaining candidates that satisfy
 * {@link #isMatching(Object, String)} are collected as fuzzy matches, and a lone fuzzy match is
 * treated as the result.
 * <p>
 * When the search is ambiguous or empty, and informing is enabled, a message is dispatched to the
 * receiver describing either the absence of matches or the matches that were found, capped at
 * {@code getMaxResultLimit()} entries.
 * <p>
 * Subclasses must supply the matching and formatting rules, and may override the presentation hooks
 * to integrate with a platform-specific receiver, such as a command sender.
 *
 * @param <Type>     the type being searched for
 * @param <Receiver> the type that receives informational messages
 */
@CustomLog
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractSearchEngine<Type, Receiver> {

    /**
     * Message format used when no candidate matched the input.
     */
    private static final String NOT_FOUND_MESSAGE = "No matches found [%s].";

    /**
     * Message format used when the input matched more than one candidate.
     */
    private static final String FOUND_MESSAGE = "%s matches found [%s].";

    /**
     * Maximum number of matches listed in an ambiguous-result message. Shared across all engines.
     */
    @Getter
    @Setter
    private static int maxResultLimit = 100;

    /**
     * Prefix applied to informational messages, or empty for no prefix.
     */
    private final String name;

    /**
     * Supplies the candidates to search, evaluated on every call to
     * {@link #find(Object, String, boolean, Predicate)}.
     */
    private final Supplier<Collection<? extends Type>> collectionSupplier;

    /**
     * Searches the supplied collection for the candidate identified by the given input.
     * <p>
     * An exact match wins immediately. Failing that, a single fuzzy match is returned. Anything else,
     * meaning no matches or several, yields an empty result and optionally informs the receiver.
     *
     * @param receiver      the receiver to inform, may be null if {@code inform} is false or the
     *                      overriding {@link #message(Object, String, String)} ignores it
     * @param input         the search input
     * @param inform        whether to message the receiver when the search fails to resolve
     * @param typePredicate an optional filter applied before matching, or null to consider every
     *                      candidate
     * @return the resolved candidate, or {@link Optional#empty()} if the search was empty or ambiguous
     */
    public Optional<Type> find(final Receiver receiver, final String input, final boolean inform, final Predicate<Type> typePredicate) {
        final List<Type> resultList = new ArrayList<>();

        for (final Type type : this.collectionSupplier.get()) {
            if (typePredicate != null && !typePredicate.test(type)) {
                continue;
            }

            if (this.isExact(type, input)) {
                return Optional.of(type);
            }

            if (this.isMatching(type, input)) {
                resultList.add(type);
            }
        }

        this.updateList(resultList);

        if (resultList.size() == 1) {
            return Optional.of(resultList.getFirst());
        }

        if (inform) {
            String message = NOT_FOUND_MESSAGE.formatted(this.getColorFormat(input));

            if (!resultList.isEmpty()) {
                final List<String> matchList = UtilJava.createCollection(new ArrayList<>(), list -> {
                    for (int i = 0; i < Math.min(maxResultLimit, resultList.size()); i++) {
                        list.add(this.getTypeFormat(resultList.get(i)));
                    }
                });

                message = FOUND_MESSAGE.formatted(
                        this.getColorFormat(String.valueOf(resultList.size())),
                        String.join(this.getMatchSeparator(), matchList)
                );
            }

            this.message(receiver, this.name, message);
        }

        return Optional.empty();
    }

    /**
     * Hook invoked on the fuzzy match list before it is resolved or reported, allowing subclasses to
     * sort, deduplicate or prune the results in place. Does nothing by default.
     *
     * @param resultList the mutable list of fuzzy matches
     */
    protected void updateList(final List<Type> resultList) {
    }

    /**
     * Separator placed between formatted matches in an ambiguous-result message.
     *
     * @return the separator, {@code ", "} by default
     */
    protected String getMatchSeparator() {
        return ", ";
    }

    /**
     * Applies emphasis to a value interpolated into a message, such as a colour code on a chat
     * platform. Returns the input unchanged by default.
     *
     * @param string the value to format
     * @return the formatted value
     */
    protected String getColorFormat(final String string) {
        return string;
    }

    /**
     * Dispatches an informational message to the receiver, prefixing it with the engine name when one
     * is set. Logs at info level by default, so subclasses should override this to reach a real
     * receiver.
     *
     * @param receiver the receiver to message
     * @param prefix   the engine name, may be null or empty
     * @param message  the message body
     */
    protected void message(final Receiver receiver, final String prefix, final String message) {
        String format = message;

        if (!UtilString.isEmpty(prefix)) {
            format = "[%s] %s".formatted(prefix, message);
        }

        LOGGER.info(format);
    }

    /**
     * Formats a candidate for display in an ambiguous-result message.
     *
     * @param type the candidate to format
     * @return the display form of the candidate
     */
    protected abstract String getTypeFormat(final Type type);

    /**
     * Tests whether a candidate is an exact match for the input, short-circuiting the search.
     *
     * @param type   the candidate to test
     * @param result the search input
     * @return true if the candidate is an exact match
     */
    protected abstract boolean isExact(final Type type, final String result);

    /**
     * Tests whether a candidate is a partial match for the input, such as a prefix or substring hit.
     *
     * @param type   the candidate to test
     * @param result the search input
     * @return true if the candidate is a partial match
     */
    protected abstract boolean isMatching(final Type type, final String result);
}