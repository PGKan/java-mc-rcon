package org.pgstyle.mcrcon_deprecated.console;

import java.util.stream.IntStream;

import javafx.scene.paint.Color;

public final class ConsoleForegroundColour extends ConsoleFormat {

    public static final ConsoleForegroundColour DEFAULT = new ConsoleForegroundColour("39");
    public static final ConsoleForegroundColour BLACK = new ConsoleForegroundColour("30");
    public static final ConsoleForegroundColour RED = new ConsoleForegroundColour("31");
    public static final ConsoleForegroundColour GREEN = new ConsoleForegroundColour("32");
    public static final ConsoleForegroundColour YELLOW = new ConsoleForegroundColour("33");
    public static final ConsoleForegroundColour BLUE = new ConsoleForegroundColour("34");
    public static final ConsoleForegroundColour MAGENTA = new ConsoleForegroundColour("35");
    public static final ConsoleForegroundColour CYAN = new ConsoleForegroundColour("36");
    public static final ConsoleForegroundColour WHITE = new ConsoleForegroundColour("37");
    public static final ConsoleForegroundColour GRAY = new ConsoleForegroundColour("90");
    public static final ConsoleForegroundColour BRIGHT_RED = new ConsoleForegroundColour("91");
    public static final ConsoleForegroundColour BRIGHT_GREEN = new ConsoleForegroundColour("92");
    public static final ConsoleForegroundColour BRIGHT_YELLOW = new ConsoleForegroundColour("93");
    public static final ConsoleForegroundColour BRIGHT_BLUE = new ConsoleForegroundColour("94");
    public static final ConsoleForegroundColour BRIGHT_MAGENTA = new ConsoleForegroundColour("95");
    public static final ConsoleForegroundColour BRIGHT_CYAN = new ConsoleForegroundColour("96");
    public static final ConsoleForegroundColour BRIGHT_WHITE = new ConsoleForegroundColour("97");

    public static ConsoleForegroundColour ofTrueColour(int red, int green, int blue) {
        if (IntStream.of(red, green, blue).anyMatch(i -> i < 0 || i > 255)) {
            throw new IllegalArgumentException("colour value out of bound");
        }
        return new ConsoleForegroundColour(red, green, blue);
    }

    public static ConsoleForegroundColour of256Colour(int index) {
        if (index < 0 || index > 255) {
            throw new IllegalArgumentException("colour index out of bound");
        }
        return new ConsoleForegroundColour(index);
    }

    private static String colorToCode(Color color) {
        StringBuilder code = new StringBuilder("38;2;");
        code.append((int) (color.getRed() * 255))
            .append((int) (color.getGreen() * 255))
            .append((int) (color.getBlue() * 255));
        return code.toString();
    }

    private ConsoleForegroundColour(Color color) {
        super(ConsoleForegroundColour.colorToCode(color));
    }

    private ConsoleForegroundColour(int index) {
        super(String.format("38;5;%d", index));
    }

    private ConsoleForegroundColour(int red, int green, int blue) {
        this(Color.color(red / 255.0, green / 255.0, blue / 255.0));
    }

    private ConsoleForegroundColour(String code) {
        super(code);
    }
}
