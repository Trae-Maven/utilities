package io.github.trae.utilities.enums;

import io.github.trae.utilities.UtilString;
import io.github.trae.utilities.enums.interfaces.ITimeUnit;
import lombok.Getter;

import java.util.Locale;
import java.util.Optional;

/**
 * Represents a unit of time with its equivalent duration in milliseconds.
 *
 * <p>Provides best-fit resolution and human-readable formatting of millisecond durations.
 */
@Getter
public enum TimeUnit implements ITimeUnit {

    MILLISECONDS("ms", 1L),
    SECONDS("s", 1_000L),
    MINUTES("m", 60_000L),
    HOURS("h", 3_600_000L),
    DAYS("d", 86_400_000L),
    WEEKS("w", 604_800_000L),
    MONTHS("mo", 2_629_800_000L),
    YEARS("y", 31_557_600_000L);

    private final String name, suffix;
    private final long duration;

    TimeUnit(final String suffix, final long duration) {
        this.name = UtilString.clean(this.name());
        this.suffix = suffix;
        this.duration = duration;
    }

    /**
     * Returns the largest {@link TimeUnit} whose duration fits into {@code duration}.
     *
     * @param duration a duration in milliseconds
     * @return the best-fit {@link TimeUnit}
     */
    public static TimeUnit getByDuration(final long duration) {
        TimeUnit bestTimeUnit = MILLISECONDS;

        for (final TimeUnit timeUnit : values()) {
            if (duration >= timeUnit.getDuration()) {
                bestTimeUnit = timeUnit;
            }
        }

        return bestTimeUnit;
    }

    /**
     * Formats a millisecond duration into a human-readable string.
     *
     * @param duration a duration in milliseconds
     * @param trim     decimal places to include; {@code 0} produces whole numbers
     * @return a formatted string such as {@code "5 minutes"} or {@code "1 hour"}
     */
    public static String format(final long duration, final int trim) {
        final TimeUnit timeUnit = getByDuration(duration);

        final double value = (double) duration / timeUnit.getDuration();
        final long rounded = Math.round(value);

        final String label = timeUnit.label(trim == 0 ? rounded : value);

        return trim <= 0 ? "%s %s".formatted(rounded, label) : ("%." + trim + "f %s").formatted(value, label);
    }

    /**
     * Parses a duration string into its equivalent milliseconds using
     * the suffix defined on each {@link TimeUnit}.
     *
     * <p>The input is matched against each unit's {@link #suffix} in
     * declaration order. The numeric portion preceding the suffix is
     * multiplied by the unit's {@link #duration}.</p>
     *
     * <p>Examples: {@code "1h"} → {@code 3600000}, {@code "30m"} →
     * {@code 1800000}, {@code "500ms"} → {@code 500}.</p>
     *
     * @param input the duration string to parse (e.g. {@code "2d"}, {@code "45s"})
     * @return an {@link Optional} containing the millisecond value,
     * or empty if the input is null, empty, or malformed
     */
    public static Optional<Long> parseByInput(final String input) {
        if (UtilString.isEmpty(input)) {
            return Optional.empty();
        }

        final String trimmed = input.trim().toLowerCase(Locale.ROOT);

        for (final TimeUnit timeUnit : values()) {
            if (!(trimmed.endsWith(timeUnit.getSuffix().toLowerCase(Locale.ROOT)))) {
                continue;
            }

            try {
                final long value = Long.parseLong(trimmed.substring(0, trimmed.length() - timeUnit.getSuffix().length()));

                return Optional.of(value * timeUnit.getDuration());
            } catch (final Exception ignored) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the singular or plural label for this unit based on {@code value}.
     *
     * @param value the numeric value to test for singularity
     * @return {@code "minute"} for {@code 1.0}, {@code "minutes"} otherwise
     */
    @Override
    public String label(final double value) {
        final String label = this.name().toLowerCase();

        return value == 1.0D ? label.substring(0, label.length() - 1) : label;
    }
}