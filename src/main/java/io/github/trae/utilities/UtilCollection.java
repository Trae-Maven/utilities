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
     * Search a collection for an element matching the given predicates.
     * <p>
     * Priority order:
     * <ol>
     *   <li>Exact match via {@code equalsPredicate} — returns immediately</li>
     *   <li>Single partial match via {@code containsPredicate} — returns that element</li>
     *   <li>Multiple partial matches — passes the list to {@code listConsumer} and optionally informs
     *       the caller with a formatted message showing up to 100 match names</li>
     *   <li>No matches — optionally informs the caller with a "no matches" message</li>
     * </ol>
     *
     * @param collection        the collection to search
     * @param equalsPredicate   predicate for exact matching, or {@code null} to skip
     * @param containsPredicate predicate for partial matching, or {@code null} to skip
     * @param listConsumer      receives the list of partial matches, or {@code null} to skip
     * @param messageConsumer   receives the formatted result message when {@code inform} is true, or {@code null} to skip
     * @param colorFunction     applies color formatting to highlighted segments of the result message
     * @param resultFunction    maps each matched element to its display name for the result message
     * @param input             the raw input string shown in the "no matches" message
     * @param inform            whether to send a result message via {@code messageConsumer}
     * @param <Type>            the element type
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

                        resultList.add(resultFunction.apply(list.get(i)));
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