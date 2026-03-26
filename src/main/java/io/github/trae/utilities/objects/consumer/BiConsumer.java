package io.github.trae.utilities.objects.consumer;

/**
 * Represents an operation that accepts two arguments and returns no result.
 *
 * @param <A> the type of the first argument
 * @param <B> the type of the second argument
 */
@FunctionalInterface
public interface BiConsumer<A, B> {

    void accept(final A a, final B b);
}