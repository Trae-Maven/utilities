package io.github.trae.utilities;

import io.github.trae.utilities.cache.CachedGenericKey;
import lombok.experimental.UtilityClass;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves concrete generic type arguments from a class hierarchy at runtime.
 *
 * <p>Given a {@code sourceClass}, a generic {@code targetClass} (an interface or
 * superclass somewhere above it), and a type-parameter index on that target, this
 * utility walks the hierarchy and returns the {@link Class} the parameter was
 * ultimately bound to in {@code sourceClass}.</p>
 *
 * <p>Resolution traverses both implemented interfaces and the superclass chain,
 * threading the actual type arguments seen at each parameterized layer down through
 * the type variables they bind. Type variables are followed transitively until they
 * resolve to a concrete {@link Class}; a parameter that remains an unbound type
 * variable (i.e. never made concrete in {@code sourceClass}) resolves to
 * {@code null}.</p>
 *
 * <p>Raw (non-parameterized) interfaces contribute no mappings of their own but are
 * still traversed, since a parameterized layer that binds the target may sit above a
 * raw interface in the chain.</p>
 *
 * <p>Results are cached in a {@link ConcurrentHashMap} keyed by
 * {@code (sourceClass, targetClass, typeIndex)} so repeated lookups are cheap.</p>
 *
 * @see CachedGenericKey
 */
@UtilityClass
public class UtilGeneric {

    /**
     * Memoizes resolved parameters keyed by source class, target class, and index.
     */
    private static final ConcurrentHashMap<CachedGenericKey, Class<?>> CACHE = new ConcurrentHashMap<>();

    /**
     * Resolves the concrete {@link Class} bound to a generic type parameter of
     * {@code targetClass} as seen from {@code sourceClass}.
     *
     * <p>The result is cached; subsequent calls with the same arguments return the
     * memoized value without re-walking the hierarchy.</p>
     *
     * @param sourceClass the concrete class whose hierarchy is inspected
     * @param targetClass the generic interface or superclass declaring the parameter
     * @param typeIndex   the zero-based index of the type parameter on {@code targetClass}
     * @return the concrete class the parameter is bound to, or {@code null} if it cannot
     * be resolved (e.g. the parameter remains an unbound type variable)
     */
    public static Class<?> getGenericParameter(final Class<?> sourceClass, final Class<?> targetClass, final int typeIndex) {
        return CACHE.computeIfAbsent(new CachedGenericKey(sourceClass, targetClass, typeIndex), key -> resolve(key.sourceClass(), key.targetClass(), key.index(), new HashMap<>()));
    }

    /**
     * Recursively searches {@code sourceClass}'s implemented interfaces and superclass
     * chain for {@code targetInterface}, carrying accumulated type-variable bindings.
     *
     * <p>Each implemented interface is examined via
     * {@link #resolveType(Type, Class, int, Map)}. When an interface is raw (a plain
     * {@link Class} with no type arguments) it cannot itself bind the target, but its
     * own super-interfaces may, so the search recurses into it with the current
     * bindings. The superclass chain is then walked the same way.</p>
     *
     * @param sourceClass     the class currently being inspected
     * @param targetInterface the generic type whose parameter is sought
     * @param typeIndex       the index of the parameter on {@code targetInterface}
     * @param resolvedTypes   type-variable bindings accumulated from outer layers
     * @return the resolved class, or {@code null} if not found on this branch
     */
    private static Class<?> resolve(final Class<?> sourceClass, final Class<?> targetInterface, final int typeIndex, final Map<TypeVariable<?>, Type> resolvedTypes) {
        for (final Type type : sourceClass.getGenericInterfaces()) {
            final Class<?> result = resolveType(type, targetInterface, typeIndex, resolvedTypes);
            if (result != null) {
                return result;
            }

            // A raw (non-parameterized) interface contributes no type mappings, but its own
            // super-interfaces may still parameterize the target. Walk into it carrying the
            // current mappings so deeper parameterized layers are not skipped.
            if (type instanceof final Class<?> rawInterface) {
                final Class<?> recursed = resolve(rawInterface, targetInterface, typeIndex, resolvedTypes);
                if (recursed != null) {
                    return recursed;
                }
            }
        }

        final Type superType = sourceClass.getGenericSuperclass();
        if (superType != null && superType != Object.class) {
            final Class<?> resolveType = resolveType(superType, targetInterface, typeIndex, resolvedTypes);
            if (resolveType != null) {
                return resolveType;
            }

            if (superType instanceof final Class<?> superClass) {
                return resolve(superClass, targetInterface, typeIndex, resolvedTypes);
            }
        }

        return null;
    }

    /**
     * Inspects a single {@link Type} from the hierarchy, recording its actual type
     * arguments and either resolving the target parameter or recursing upward.
     *
     * <p>Non-parameterized types are ignored (they carry no argument bindings and are
     * handled by {@link #resolve(Class, Class, int, Map)}). For a
     * {@link ParameterizedType}, each of the raw type's type variables is mapped to the
     * corresponding actual argument and merged with the bindings inherited from outer
     * layers. If the raw type is {@code targetInterface}, the requested parameter is
     * resolved to a concrete class; otherwise the search continues up the raw type's
     * own hierarchy with the merged bindings.</p>
     *
     * @param type            the hierarchy type to inspect
     * @param targetInterface the generic type whose parameter is sought
     * @param typeIndex       the index of the parameter on {@code targetInterface}
     * @param parentTypes     type-variable bindings inherited from outer layers
     * @return the resolved class, or {@code null} if this type does not yield it
     */
    private static Class<?> resolveType(final Type type, final Class<?> targetInterface, final int typeIndex, final Map<TypeVariable<?>, Type> parentTypes) {
        if (!(type instanceof final ParameterizedType parameterizedType)) {
            return null;
        }

        final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        final TypeVariable<?>[] typeVariables = rawType.getTypeParameters();
        final Type[] actualTypes = parameterizedType.getActualTypeArguments();

        final Map<TypeVariable<?>, Type> currentTypes = new HashMap<>(parentTypes);
        for (int i = 0; i < typeVariables.length; i++) {
            currentTypes.put(typeVariables[i], actualTypes[i]);
        }

        if (rawType == targetInterface) {
            return resolveToClass(currentTypes.get(typeVariables[typeIndex]), currentTypes);
        }

        return resolve(rawType, targetInterface, typeIndex, currentTypes);
    }

    /**
     * Reduces a {@link Type} to its erasure {@link Class}, following type variables
     * through the supplied binding map until a concrete type is reached.
     *
     * <p>Type variables are dereferenced transitively via {@code map}. A concrete
     * {@link Class} is returned directly; a {@link ParameterizedType} resolves to its
     * raw class. If the chain terminates in an unbound type variable or any other
     * unresolvable type, {@code null} is returned.</p>
     *
     * @param type the type to reduce (may be a type variable, class, or parameterized type)
     * @param map  the type-variable bindings used to dereference variables
     * @return the erasure class, or {@code null} if it cannot be resolved
     */
    private static Class<?> resolveToClass(Type type, final Map<TypeVariable<?>, Type> map) {
        while (type instanceof final TypeVariable<?> typeVariable) {
            type = map.get(typeVariable);
        }

        if (type instanceof final Class<?> typeClass) {
            return typeClass;
        }

        if (type instanceof final ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof final Class<?> parameterizedRawTypeClass) {
            return parameterizedRawTypeClass;
        }

        return null;
    }
}