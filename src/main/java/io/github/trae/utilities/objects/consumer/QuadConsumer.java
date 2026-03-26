package io.github.trae.utilities.objects.consumer;

/**
 * Represents an operation that accepts four arguments and returns no result.
 *
 * @param <A> the type of the first argument
 * @param <B> the type of the second argument
 * @param <C> the type of the third argument
 * @param <D> the type of the fourth argument
 */
@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {

    void accept(final A a, final B b, final C c, final D d);
}