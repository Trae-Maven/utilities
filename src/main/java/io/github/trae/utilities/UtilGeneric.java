package io.github.trae.utilities;

import io.github.trae.utilities.cache.CachedGenericKey;
import lombok.experimental.UtilityClass;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class UtilGeneric {

    private static final ConcurrentHashMap<CachedGenericKey, Class<?>> CACHE = new ConcurrentHashMap<>();

    public static Class<?> getGenericParameter(final Class<?> sourceClass, final Class<?> targetClass, final int typeIndex) {
        return CACHE.computeIfAbsent(new CachedGenericKey(sourceClass, targetClass, typeIndex), key -> resolve(key.sourceClass(), key.targetClass(), key.index(), new HashMap<>()));
    }

    private static Class<?> resolve(final Class<?> sourceClass, final Class<?> targetInterface, final int typeIndex, final Map<TypeVariable<?>, Type> resolvedTypes) {
        for (final Type type : sourceClass.getGenericInterfaces()) {
            final Class<?> result = resolveType(type, targetInterface, typeIndex, resolvedTypes);
            if (result != null) {
                return result;
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