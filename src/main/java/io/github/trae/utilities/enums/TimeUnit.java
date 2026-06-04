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

    private final String name, shortName;
    private final long duration;

    TimeUnit(final String shortName, final long duration) {
        this.name = UtilString.clean(this.name());
        this.shortName = shortName;
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
     * Formats a millisecond duration into a human-readable string, choosing the
     * largest {@link TimeUnit} that fits the duration.
     *
     * <p>The {@code trim} parameter controls the output format:</p>
     * <ul>
     *   <li>{@code -1} — compact form using the unit's short name, rounded to a
     *       whole number (e.g. {@code "500ms"}, {@code "5s"}, {@code "3h"},
     *       {@code "2y"}).</li>
     *   <li>{@code 0} — whole-number form using the unit's full label, rounded
     *       (e.g. {@code "5 seconds"}, {@code "1 minute"}, {@code "3 hours"},
     *       {@code "2 years"}).</li>
     *   <li>{@code > 0} — decimal form with the given number of decimal places,
     *       using the unit's full label (e.g. for {@code trim = 1}:
     *       {@code "5.0 seconds"}, {@code "1.5 hours"}, {@code "2.3 days"}).</li>
     * </ul>
     *
     * <p>The unit is selected automatically via {@link #getByDuration(long)}, so
     * the same call adapts across the full range — {@code 500} formats in
     * milliseconds, {@code 90_000} in minutes, {@code 7_200_000} in hours, and
     * {@code 63_115_200_000L} in years.</p>
     *
     * @param duration a duration in milliseconds
     * @param trim     the output format: {@code -1} for short name, {@code 0} for
     *                 a rounded whole number, or a positive value for that many
     *                 decimal places
     * @return a formatted string such as {@code "500ms"}, {@code "5 seconds"},
     * {@code "1 minute"}, {@code "1.5 hours"}, or {@code "2 years"}
     */
    public static String format(final long duration, final int trim) {
        final TimeUnit timeUnit = getByDuration(duration);

        final double value = (double) duration / timeUnit.getDuration();
        final long rounded = Math.round(value);

        return switch (trim) {
            case -1 -> rounded + timeUnit.getShortName();
            case 0 -> "%s %s".formatted(rounded, timeUnit.label(rounded));
            default -> ("%." + trim + "f %s").formatted(value, timeUnit.label(value));
        };
    }

    /**
     * Parses a duration string into its equivalent milliseconds using
     * the suffix defined on each {@link TimeUnit}.
     *
     * <p>The input is matched against each unit's {@link #shortName} in
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
            if (!(trimmed.endsWith(timeUnit.getShortName().toLowerCase(Locale.ROOT)))) {
                continue;
            }

            try {
                final long value = Long.parseLong(trimmed.substring(0, trimmed.length() - timeUnit.getShortName().length()));

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
     * <p>The label is the lowercased unit name; a value of exactly {@code 1.0}
     * drops the trailing {@code "s"} to give the singular form. For example:
     * {@code 1.0} yields {@code "second"}, {@code "minute"}, {@code "hour"},
     * {@code "day"}, {@code "year"}; any other value yields {@code "seconds"},
     * {@code "minutes"}, {@code "hours"}, {@code "days"}, {@code "years"}.</p>
     *
     * @param value the numeric value to test for singularity
     * @return the singular label for {@code 1.0}, the plural label otherwise
     */
    @Override
    public String label(final double value) {
        final String label = this.name().toLowerCase();

        return value == 1.0D ? label.substring(0, label.length() - 1) : label;
    }
}