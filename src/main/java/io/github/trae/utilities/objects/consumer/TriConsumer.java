package io.github.trae.utilities.objects.consumer;

/**
 * Represents an operation that accepts three arguments and returns no result.
 *
 * @param <A> the type of the first argument
 * @param <B> the type of the second argument
 * @param <C> the type of the third argument
 */
@FunctionalInterface
public interface TriConsumer<A, B, C> {

    void accept(final A a, final B b, final C c);
}