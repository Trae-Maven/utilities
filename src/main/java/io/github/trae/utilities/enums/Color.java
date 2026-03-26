package io.github.trae.utilities.enums;

import io.github.trae.utilities.UtilString;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ANSI color codes for terminal output.
 *
 * <p>Each standard color has three variants: {@code DARK_}, standard, and {@code LIGHT_}.
 * Black, white, and gray are standalone shades. Extended colors use 256-color codes.</p>
 *
 * <p>Supports inline color tags (e.g. {@code <green>}, {@code <light_red>}) via
 * {@link #translate(String)} which replaces all tags with ANSI escape codes and
 * appends a reset at the end.</p>
 */
@AllArgsConstructor
@Getter
public enum Color {

    RESET("\033[0m"),

    // Shades
    BLACK("\033[30m"),
    GRAY("\033[90m"),
    LIGHT_GRAY("\033[37m"),
    WHITE("\033[97m"),

    // Red
    DARK_RED("\033[31m"),
    RED("\033[91m"),
    LIGHT_RED("\033[91;1m"),

    // Green
    DARK_GREEN("\033[32m"),
    GREEN("\033[92m"),
    LIGHT_GREEN("\033[92;1m"),

    // Yellow
    DARK_YELLOW("\033[33m"),
    YELLOW("\033[93m"),
    LIGHT_YELLOW("\033[93;1m"),

    // Blue
    DARK_BLUE("\033[34m"),
    BLUE("\033[94m"),
    LIGHT_BLUE("\033[94;1m"),

    // Purple
    DARK_PURPLE("\033[35m"),
    PURPLE("\033[95m"),
    LIGHT_PURPLE("\033[95;1m"),

    // Cyan
    DARK_CYAN("\033[36m"),
    CYAN("\033[96m"),
    LIGHT_CYAN("\033[96;1m"),

    // Orange
    DARK_ORANGE("\033[38;5;166m"),
    ORANGE("\033[38;5;208m"),
    LIGHT_ORANGE("\033[38;5;215m"),

    // Pink
    DARK_PINK("\033[38;5;162m"),
    PINK("\033[38;5;205m"),
    LIGHT_PINK("\033[38;5;218m"),

    // Magenta
    DARK_MAGENTA("\033[38;5;127m"),
    MAGENTA("\033[38;5;199m"),
    LIGHT_MAGENTA("\033[38;5;213m"),

    // Teal
    DARK_TEAL("\033[38;5;23m"),
    TEAL("\033[38;5;37m"),
    LIGHT_TEAL("\033[38;5;44m"),

    // Lime
    DARK_LIME("\033[38;5;28m"),
    LIME("\033[38;5;118m"),
    LIGHT_LIME("\033[38;5;155m"),

    // Indigo
    DARK_INDIGO("\033[38;5;54m"),
    INDIGO("\033[38;5;63m"),
    LIGHT_INDIGO("\033[38;5;105m"),

    // Violet
    DARK_VIOLET("\033[38;5;92m"),
    VIOLET("\033[38;5;135m"),
    LIGHT_VIOLET("\033[38;5;183m"),

    // Brown
    DARK_BROWN("\033[38;5;52m"),
    BROWN("\033[38;5;130m"),
    LIGHT_BROWN("\033[38;5;173m"),

    // Gold
    DARK_GOLD("\033[38;5;136m"),
    GOLD("\033[38;5;178m"),
    LIGHT_GOLD("\033[38;5;220m"),

    // Coral
    DARK_CORAL("\033[38;5;167m"),
    CORAL("\033[38;5;209m"),
    LIGHT_CORAL("\033[38;5;210m"),

    // Salmon
    DARK_SALMON("\033[38;5;131m"),
    SALMON("\033[38;5;174m"),
    LIGHT_SALMON("\033[38;5;216m"),

    // Turquoise
    DARK_TURQUOISE("\033[38;5;30m"),
    TURQUOISE("\033[38;5;43m"),
    LIGHT_TURQUOISE("\033[38;5;80m"),

    // Aqua
    DARK_AQUA("\033[38;5;38m"),
    AQUA("\033[38;5;51m"),
    LIGHT_AQUA("\033[38;5;123m"),

    // Mint
    DARK_MINT("\033[38;5;29m"),
    MINT("\033[38;5;48m"),
    LIGHT_MINT("\033[38;5;121m"),

    // Olive
    DARK_OLIVE("\033[38;5;58m"),
    OLIVE("\033[38;5;100m"),
    LIGHT_OLIVE("\033[38;5;143m"),

    // Maroon
    DARK_MAROON("\033[38;5;52m"),
    MAROON("\033[38;5;88m"),
    LIGHT_MAROON("\033[38;5;124m"),

    // Navy
    DARK_NAVY("\033[38;5;17m"),
    NAVY("\033[38;5;18m"),
    LIGHT_NAVY("\033[38;5;19m"),

    // Rose
    DARK_ROSE("\033[38;5;132m"),
    ROSE("\033[38;5;168m"),
    LIGHT_ROSE("\033[38;5;211m"),

    // Peach
    DARK_PEACH("\033[38;5;173m"),
    PEACH("\033[38;5;216m"),
    LIGHT_PEACH("\033[38;5;223m"),

    // Lavender
    DARK_LAVENDER("\033[38;5;97m"),
    LAVENDER("\033[38;5;141m"),
    LIGHT_LAVENDER("\033[38;5;189m");

    private static final Map<String, Color> TAG_MAP = new HashMap<>();

    static {
        for (final Color color : values()) {
            TAG_MAP.put(color.name().toUpperCase(Locale.ROOT), color);
            TAG_MAP.put(color.name().toLowerCase(Locale.ROOT), color);
        }
    }

    private final String code;

    /**
     * Translates color tags in the given string to ANSI escape codes.
     *
     * <p>Tags are case-insensitive and match enum names
     * (e.g. {@code <green>}, {@code <light_red>}, {@code <dark_blue>}).
     * A {@link #RESET} is appended at the end. The {@code <reset>} tag
     * can be used inline to manually reset color.</p>
     *
     * @param message the message containing color tags
     * @return the message with tags replaced by ANSI codes
     */
    public static String translate(final String message) {
        if (UtilString.isEmpty(message)) {
            return message;
        }

        String result = message;

        for (final Map.Entry<String, Color> entry : TAG_MAP.entrySet()) {
            result = result.replace("<%s>".formatted(entry.getKey()), entry.getValue().getCode());
            result = result.replace("</%s>".formatted(entry.getKey()), RESET.getCode());
        }

        return result + RESET.getCode();
    }

    @Override
    public String toString() {
        return this.getCode();
    }
}