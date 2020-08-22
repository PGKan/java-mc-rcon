/**
 * Stable: TODO WIP
 * Document: TODO document needed
 */
package org.pgstyle.mcrcon;

import java.io.IOException;

import org.pgstyle.mcrcon.protocol.Session;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public class RemoteController {

    public static final String UCI = "rcon";

    private static boolean verbose = false;
    private static boolean silent  = false;

    private static Session session;

    public static boolean isSilent() {
        return RemoteController.silent;
    }

    public static boolean isVerbose() {
        return RemoteController.verbose;
    }

    public static boolean prompt() {
        while (RemoteController.session != null && RemoteController.session.isAlive()) {
            RconUtils.say(null, ": ");

            String command = System.console().readLine().trim();
            if (command.length() > 0) {
                if (command.startsWith("$remote:") || !command.startsWith("$local:") && !command.startsWith("$")) {
                    command = command.startsWith("$remote:") ? command.substring(8) : command;
                    RconUtils.verbose(RemoteController.UCI, "$remote:%s%n", command);
                    if (RemoteController.session != null && RemoteController.session.isAlive()) {
                        String message = RemoteController.session.execute(command);
                        if (message != null && message.length() > 0) {
                            RconUtils.say(null, message + "%n");
                        }
                        RconUtils.say(null, "%n");
                    }
                }
                else {
                    command = command.startsWith("$local:") ? command.substring(7) : command;
                    command = command.startsWith("$") ? command.substring(1) : command;
                    command = command.trim().split(" +")[0];
                    RconUtils.verbose(RemoteController.UCI, "$local:%s%n", command);
                    switch (command) {
                    case "exit":
                        RemoteController.session.close();
                        return true;
                    default:
                        RconUtils.err(null, "Unknown command: %s%n", command);
                    }
                }
            }
        }

        if (RemoteController.session == null) {
            RconUtils.err(null, "Connection failed%n");
            return false;
        }
        else {
            RconUtils.say(null, "Connection closed%n");
            return true;
        }
    }

    public static boolean session(String host, int port, String password) {
        RemoteController.session = Session.createSession(host, port, 5);
        if (RemoteController.session != null) {
            boolean autenticated = false;
            if (password != null) {
                RconUtils.verbose(RemoteController.UCI, "Found preloaded password, use preloaded password instead of prompting for input%n");
                try {
                    autenticated = RemoteController.session.authenticate(password);
                }
                catch (IOException e) {
                    brokenSession();
                    return true;
                }
                if (!autenticated) {
                    RconUtils.verbose(RemoteController.UCI, "Authentication failed, prompt for password input%n");
                }
            }
            for (int i = 0; !autenticated && i < 3; i++) {
                RconUtils.say(null, "Input password...%n: ");
                try {
                    autenticated = RemoteController.session.authenticate(new String(System.console().readPassword()));
                }
                catch (IOException e) {
                    brokenSession();
                    return true;
                }
                if (!autenticated) {
                    RconUtils.say(null, "Authentication failed, please try again%n");
                }
                else {
                    RconUtils.say(null, "Authentication finished%n");
                }
            }

            return autenticated;
        }
        else {
            RconUtils.err(null, "Connection failed%");
            return true;
        }
    }

    private static void brokenSession() {
        RconUtils.verbose(RemoteController.UCI, "Connection is dead, not continue session%n");
        RemoteController.session = null;
    }

    public static void setSilent(boolean silent) {
        RemoteController.silent = silent;
    }

    public static void setVerbose(boolean verbose) {
        RemoteController.verbose = verbose;
    }

}
