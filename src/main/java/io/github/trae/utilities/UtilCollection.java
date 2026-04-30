package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility methods for working with collections.
 */
@UtilityClass
public class UtilCollection {

    /**
     * Search a collection for an element, prioritising exact matches over partial matches.
     * <p>
     * Resolution order:
     * <ol>
     *   <li>Exact match ({@code equalsPredicate}) — returned immediately.</li>
     *   <li>Single partial match ({@code containsPredicate}) — returned after the full scan.</li>
     *   <li>Multiple partial matches — forwarded to {@code listConsumer}; an optional summary
     *       message (up to 100 names) is sent to {@code messageConsumer}.</li>
     *   <li>No matches — an optional "no matches" message is sent to {@code messageConsumer}.</li>
     * </ol>
     *
     * @param collection        collection to search
     * @param equalsPredicate   exact-match predicate, or {@code null} to skip
     * @param containsPredicate partial-match predicate, or {@code null} to skip
     * @param listConsumer      receives the partial-match list, or {@code null} to skip
     * @param messageConsumer   receives a formatted result message when {@code inform} is {@code true}, or {@code null} to skip
     * @param colorFunction     applies colour formatting to highlighted segments of the message
     * @param resultFunction    maps a matched element to its display name for the message
     * @param input             raw input string shown in the "no matches" message
     * @param inform            whether to produce a result message
     * @param <Type>            element type
     * @return the matched element, or {@link Optional#empty()} if zero or multiple matches were found
     */
    public static <Type> Optional<Type> search(final Collection<? extends Type> collection, final Predicate<Type> equalsPredicate, final Predicate<Type> containsPredicate, final Consumer<List<Type>> listConsumer, final Consumer<String> messageConsumer, final Function<String, String> colorFunction, final Function<Type, String> resultFunction, final String input, final boolean inform) {
        final List<Type> list = new ArrayList<>();

        for (final Type object : collection) {
            if (equalsPredicate != null && equalsPredicate.test(object)) {
                return Optional.of(object);
            }

            if (containsPredicate != null && containsPredicate.test(object)) {
                list.add(object);
            }
        }

        if (listConsumer != null) {
            listConsumer.accept(list);
        }

        if (list.size() == 1) {
            return Optional.of(list.getFirst());
        }

        if (inform) {
            String message = "No matches found [%s].".formatted(colorFunction.apply(input));

            if (!(list.isEmpty())) {
                final List<String> results = UtilJava.createCollection(new ArrayList<>(), resultList -> {
                    for (int i = 0; i < list.size(); i++) {
                        if (i == 100) {
                            break;
                        }

                        resultList.add(colorFunction.apply(resultFunction.apply(list.get(i))));
                    }
                });

                message = "%s matches found [%s]".formatted(colorFunction.apply(String.valueOf(list.size())), String.join("<gray>, </gray>", results));
            }

            if (messageConsumer != null) {
                messageConsumer.accept(message);
            }
        }

        return Optional.empty();
    }
}