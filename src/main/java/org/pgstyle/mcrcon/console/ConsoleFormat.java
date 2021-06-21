package org.pgstyle.mcrcon.console;

public class ConsoleFormat {
    
    public static final ConsoleFormat RESET = new ConsoleFormat("0");
    public static final ConsoleFormat BOLD = new ConsoleFormat("1");
    public static final ConsoleFormat FAINT = new ConsoleFormat("2");
    public static final ConsoleFormat ITALIC = new ConsoleFormat("3");
    public static final ConsoleFormat UDDERLINE = new ConsoleFormat("4");
    public static final ConsoleFormat INVERT = new ConsoleFormat("7");
    public static final ConsoleFormat CONCEAL = new ConsoleFormat("8");
    public static final ConsoleFormat DELETED = new ConsoleFormat("9");
    public static final ConsoleFormat STOP_CURSOR = new ConsoleFormat("25");
    public static final ConsoleFormat RESET_STRENGTH = new ConsoleFormat("22");
    public static final ConsoleFormat RESET_ITALIC = new ConsoleFormat("23");
    public static final ConsoleFormat RESET_UNDERLINE = new ConsoleFormat("24");
    public static final ConsoleFormat RESET_INVERT = new ConsoleFormat("27");
    public static final ConsoleFormat RESET_CONCEAL = new ConsoleFormat("28");
    public static final ConsoleFormat RESET_DELETED = new ConsoleFormat("29");
    public static final ConsoleFormat RESET_CURSOR = new ConsoleFormat("5");

    protected ConsoleFormat(String code) {
        this.ansiEscapeCode = code;
    }

    protected final String ansiEscapeCode;

    public String active() {
        return "\u001b[" + this.ansiEscapeCode + 'm';
    }

    public String apply(String string) {
        return this.active() + string + ConsoleFormat.RESET.active();
    }

    public String toString() {
        return "\\e[" + this.ansiEscapeCode + 'm';
    }

}
