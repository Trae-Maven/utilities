package io.github.trae.utilities;

import io.github.trae.utilities.cache.CachedFile;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance file reading utility with automatic caching.
 *
 * <p>Files are read using memory-mapped IO via {@link MappedByteBuffer},
 * bypassing kernel-to-userspace copying for maximum read throughput.
 * Results are cached by normalized path and automatically invalidated
 * when the file's last-modified timestamp changes.</p>
 *
 * <p>Files under 2GB are mapped in a single pass. Larger files are
 * mapped in chunks of {@link Integer#MAX_VALUE} bytes to stay within
 * the {@link MappedByteBuffer} size limit.</p>
 */
@UtilityClass
public class UtilFile {

    /**
     * Path-keyed cache storing file contents alongside their last-modified timestamp.
     */
    private static final ConcurrentHashMap<Path, CachedFile> CACHED_FILE_MAP = new ConcurrentHashMap<>();

    /**
     * Reads all lines from the given path. Returns cached results on
     * subsequent calls unless the file has been modified on disk.
     *
     * @param path the path to the file
     * @return an unmodifiable list of lines
     * @throws RuntimeException if the file cannot be read
     */
    public static List<String> read(final Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null.");
        }

        final Path normalizedPath = path.toAbsolutePath().normalize();

        final CachedFile cachedFile = CACHED_FILE_MAP.compute(normalizedPath, (__, existing) -> {
            final long lastModified = normalizedPath.toFile().lastModified();

            if (lastModified == 0L) {
                throw new RuntimeException("File does not exist, removed from cache: %s".formatted(normalizedPath));
            }

            if (existing != null && existing.lastModified() == lastModified) {
                return existing;
            }

            return new CachedFile(readFromDisk(normalizedPath), lastModified);
        });

        return cachedFile.lines();
    }

    /**
     * @see #read(Path)
     */
    public static List<String> read(final File file) {
        return read(file.toPath());
    }

    /**
     * @see #read(Path)
     */
    public static List<String> read(final String filePath) {
        return read(Paths.get(filePath));
    }

    /**
     * Reads all lines from disk using memory-mapped IO. Maps the file
     * directly into userspace memory via {@link FileChannel#map} for
     * the fastest possible sequential read.
     *
     * <p>Small files (under 2GB) are mapped in a single operation.
     * Larger files are mapped in sequential chunks to respect the
     * {@link MappedByteBuffer} size constraint.</p>
     *
     * @param path the normalized, absolute path to the file
     * @return an unmodifiable list of lines decoded as UTF-8
     * @throws RuntimeException if the file cannot be opened or read
     */
    private static List<String> readFromDisk(final Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null.");
        }

        try (final FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            final long size = channel.size();

            if (size == 0) {
                return Collections.emptyList();
            }

            if (size <= Integer.MAX_VALUE) {
                final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);

                return StandardCharsets.UTF_8.decode(buffer).toString().lines().toList();
            }

            final StringBuilder stringBuilder = new StringBuilder(Integer.MAX_VALUE);

            long position = 0;
            while (position < size) {
                final long remaining = size - position;
                final long mapSize = Math.min(Integer.MAX_VALUE, remaining);

                final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, mapSize);
                stringBuilder.append(StandardCharsets.UTF_8.decode(buffer));

                position += mapSize;
            }

            return stringBuilder.toString().lines().toList();
        } catch (final IOException e) {
            throw new RuntimeException("Could not read file: %s".formatted(path.toAbsolutePath()), e);
        }
    }
}