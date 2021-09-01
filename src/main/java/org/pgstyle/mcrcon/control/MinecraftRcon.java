package org.pgstyle.mcrcon.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.pgstyle.mcrcon.control.command.Command;
import org.pgstyle.mcrcon.control.command.CommandEntry;
import org.pgstyle.mcrcon.control.command.Execute;
import org.pgstyle.mcrcon.log.Log;
import org.pgstyle.mcrcon.rcon.RconController;
import org.pgstyle.mcrcon.rcon.RconUtils;

public class MinecraftRcon implements Callable<Integer> {

    public static final int SUCCESS   = 0;
    public static final int FAIL_AUTH = 1;
    public static final int FAIL_ERR  = 2;

    private static final Map<String, Boolean> flags = new HashMap<>();

    private static String host = "localhost";
    private static int    port = 25575;

    public static void main(String[] args) {
        String password = null;
        if (MinecraftRcon.flags.containsKey("Help")) {
            try {
                Log.out.info(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/help")), RconUtils.UTF8));
            }
            catch (IOException e) {
                Log.out.error(e.getMessage());
            }
            System.exit(MinecraftRcon.SUCCESS);
        }
        if (!MinecraftRcon.flags.containsKey("NoHead")) {
            try {
                Log.out.info(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/head")), RconUtils.UTF8));
                Log.out.warn(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/security-warning")), RconUtils.UTF8));
            }
            catch (IOException e) {
                Log.out.error(e.getMessage());
            }
        }

        if (MinecraftRcon.flags.containsKey("Verbose")) {
            ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggerConfig("").setLevel(Level.ALL);
            ((LoggerContext) LogManager.getContext(false)).updateLoggers();
        }

        RconController controller = new RconController();
        if (!controller.crateSession(MinecraftRcon.host, MinecraftRcon.port, 3)) {
            System.exit(MinecraftRcon.FAIL_ERR);
        }
        if (!controller.authenticate(password)) {
            Log.out.error("failed to authenticate");
            System.exit(MinecraftRcon.FAIL_AUTH);
        }
        MinecraftRcon.loop(controller);

    }

    private static void loop(RconController controller) {
        while (controller.isAlive()) {
            Log.out.info(": ");
            String command = System.console().readLine().trim();
            if (command.length() > 0) {
                boolean remote = command.startsWith("$remote:") || !command.startsWith("$");
                command = prepareCommand(command);
                String result;
                if (remote) {
                    Log.out.debug("$exec remote %s%n", command);
                    result = MinecraftRcon.executeRemote(controller, command);
                }
                else {
                    Log.out.debug("$exec local %s%n", command);
                    result = MinecraftRcon.executeLocal(controller, command);
                }
            }
        }
        Log.out.info("connection closed");
    }

    private static String prepareCommand(String command) {  
        command = command.startsWith("$remote:") ? command.substring(8) : command;
        command = command.startsWith("$local:") ? command.substring(7) : command;
        command = command.startsWith("$") ? command.substring(1) : command;
        return command;
    }

    private static String executeRemote(RconController controller, String command) {
        return controller.execute(command);
    }

    private static String executeLocal(RconController controller, String command) {
        Log.out.debug("$local:%s%n", command);
        command = command.trim().split(" +")[0];
        switch (command) {
        case "exit":
            controller.closeSession();
            return "connection terminated";
        case "help":
            MinecraftRcon.help();
            return "";
        default:
            Log.out.error("Unknown command %s", command);
            return "unknown command";
        }
    }

    public MinecraftRcon(CommandLineArguments cmdlArgs) {
        this.cmdlArgs = cmdlArgs;
    }

    private CommandLineArguments cmdlArgs;

    @Override
    public Integer call() {
        if (this.cmdlArgs.verbose()) {
            LoggerContext lc = (LoggerContext) LogManager.getContext(false);
            lc.getConfiguration().getLoggerConfig("").setLevel(Level.ALL);
            lc.updateLoggers();
        }
        if (this.cmdlArgs.help()) {
            MinecraftRcon.help();
            return MinecraftRcon.SUCCESS;
        }
        if (!this.cmdlArgs.noHead()) {
            MinecraftRcon.head();
        }
        MinecraftRcon.warning();

        RconController controller = new RconController();
        if (!controller.crateSession(this.cmdlArgs.host(), this.cmdlArgs.port(), 3)) {
            return MinecraftRcon.FAIL_ERR;
        }
        if (!controller.authenticate(this.cmdlArgs.password())) {
            Log.out.error("Failed to authenticate");
            return MinecraftRcon.FAIL_AUTH;
        }

        return this.main(controller);
    }

    private int main(RconController controller) {
        Command execute = new Execute(controller);
        while (controller.isAlive()) {
            Log.prompt(": ");
            String command = System.console().readLine().trim();
            if (command.length() > 0) {
                boolean remote = command.startsWith("$remote:") || !command.startsWith("$");
                command = (remote ? "exec remote " : "exec ") + MinecraftRcon.prepareCommand(command);
                Log.out.debug("${}", command);
                CommandEntry entry = execute.execute(command);
                Optional.ofNullable(entry).ifPresent(Log::command);
                if (!this.cmdlArgs.silent()) {
                    Log.out.info(entry.getResult());
                }
            }
        }
        Log.out.info("connection closed");
        return MinecraftRcon.SUCCESS;
    }

    private static void head() {
        try {
            Log.out.info(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/head")), RconUtils.UTF8));
        }
        catch (IOException e) {
            Log.out.error(e.getMessage());
        }
    }

    private static void help() {
        try {
            Log.out.info(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/help")), RconUtils.UTF8));
        }
        catch (IOException e) {
            Log.out.error("failed to fetch help document", e);
        }
    }

    private static void warning() {
        try {
            Log.out.warn(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/security-warning")), RconUtils.UTF8));
        }
        catch (IOException e) {
            Log.out.error(e.getMessage());
        }
    }

}
