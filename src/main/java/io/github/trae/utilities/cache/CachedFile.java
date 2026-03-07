package io.github.trae.utilities.cache;

import java.util.List;

public record CachedFile(List<String> lines, long lastModified) {
}