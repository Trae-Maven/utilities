package io.github.trae.utilities.cache;

public record CachedGenericKey(Class<?> sourceClass, Class<?> targetClass, int index) {
}