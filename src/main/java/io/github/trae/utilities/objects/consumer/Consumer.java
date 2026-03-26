package io.github.trae.utilities.objects.consumer;

/**
 * Represents an operation that accepts one argument and returns no result.
 *
 * @param <A> the type of the first argument
 */
@FunctionalInterface
public interface Consumer<A> {

    void accept(final A a);
}