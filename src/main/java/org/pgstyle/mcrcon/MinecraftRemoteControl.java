/**
 * Stable: TODO WIP
 * Document: TODO document needed
 */
package org.pgstyle.mcrcon;

import java.io.IOException;

import org.pgstyle.mcrcon.console.ConsoleForegroundColour;
import org.pgstyle.mcrcon.console.ConsoleFormat;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public class MinecraftRemoteControl {

    public static final int EXIT      = 0;
    public static final int EXIT_AUTH = 1;
    public static final int EXIT_ERR  = 2;

    private static boolean help;
    private static boolean noHead;
    private static boolean silent;
    private static boolean verbose;

    private static String host;
    private static int    port = 25575;

    /**
     * @param args
     */
    public static void main(String[] args) {
        String password = MinecraftRemoteControl.processArguments(args);

        if (MinecraftRemoteControl.help) {
            try {
                RconUtils.put(null, new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/help")), RconUtils.UTF8));
            }
            catch (IOException e) {
                RconUtils.err(null, e.getMessage());
            }
            System.exit(MinecraftRemoteControl.EXIT);
        }

        if (!MinecraftRemoteControl.noHead) {
            try {
                RconUtils.put(null, new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/head")), RconUtils.UTF8));
                RconUtils.put(null, "\u001b[1m" + ConsoleForegroundColour.of256Colour(9).apply(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/secure-warning")), RconUtils.UTF8)));
            }
            catch (IOException e) {
                RconUtils.err(null, e.getMessage());
            }
        }

        RemoteController.setSilent(MinecraftRemoteControl.silent);
        RemoteController.setVerbose(MinecraftRemoteControl.verbose);

        if (!RemoteController.session(MinecraftRemoteControl.host != null ? MinecraftRemoteControl.host : "localhost", MinecraftRemoteControl.port, password)) {
            RconUtils.err(null, "Authentication failed%n");
            System.exit(MinecraftRemoteControl.EXIT_AUTH);
        }
        if (RemoteController.prompt()) {
            System.exit(MinecraftRemoteControl.EXIT);
        }
        else {
            System.exit(MinecraftRemoteControl.EXIT_ERR);
        }
    }

    public static String processArguments(String[] args) {
        String password = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                switch (args[i].substring(2)) {
                case "help":
                    MinecraftRemoteControl.help = true;
                    break;
                case "no-head":
                    MinecraftRemoteControl.noHead = true;
                    break;
                case "silent":
                    MinecraftRemoteControl.silent = true;
                    break;
                case "verbose":
                    MinecraftRemoteControl.verbose = true;
                    break;
                }
            }
            else if (args[i].startsWith("-")) {
                if (args[i].contains("h")) {
                    MinecraftRemoteControl.help = true;
                }
                if (args[i].contains("H")) {
                    MinecraftRemoteControl.noHead = true;
                }
                if (args[i].contains("s")) {
                    MinecraftRemoteControl.silent = true;
                }
                if (args[i].contains("v")) {
                    MinecraftRemoteControl.verbose = true;
                }
                if (args[i].contains("p")) {
                    password = args[++i];
                }
            }
            else {
                System.out.println(args[i]);
                if (MinecraftRemoteControl.host == null) {
                    MinecraftRemoteControl.host = args[i];
                }
                else {
                    try {
                        MinecraftRemoteControl.port = Integer.parseInt(args[i]);
                    }
                    catch (NumberFormatException e) {
                        RconUtils.err(null, "Wrong argument type for port");
                        System.exit(MinecraftRemoteControl.EXIT_ERR);
                    }
                }
            }
        }
        return password;
    }

    /** The class {@code MinecraftRemoteControl} is unnewable. */
    private MinecraftRemoteControl() {}

}
