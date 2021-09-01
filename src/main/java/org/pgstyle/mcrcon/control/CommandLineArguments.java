package org.pgstyle.mcrcon.control;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.pgstyle.mcrcon.log.Log;

public class CommandLineArguments {

    public static CommandLineArguments fromArgs(String[] args) {
        return new CommandLineArguments(CommandLineArguments.processArguments(args));
    }

    private static Map<String, String> processArguments(String[] args) {
        Map<String, String> argMap = CommandLineArguments.processArguments(Arrays.stream(args).collect(Collectors.toList()).iterator());
        argMap.putIfAbsent("Host", "localhost");
        argMap.putIfAbsent("Port", "25575");
        return argMap;
    }

    private static Map<String, String> processArguments(Iterator<String> args) {
        Map<String, String> argMap = new HashMap<>();
        boolean setHost = true;
        while (args.hasNext()) {
            String arg = args.next();
            if (arg == null) {
                continue;
            }
            if (arg.startsWith("-")) {
                String name = CommandLineArguments.getFlagName(arg);
                if (name.equals("Password")) {
                    argMap.put(name, args.next());
                }
                else if (name.length() > 0) {
                    argMap.put(name, name);
                }
                else {
                    throw new IllegalArgumentException("unknown argument: " + arg);
                }
            }
            else {
                if (setHost) {
                    argMap.put("Host", arg);
                    setHost = false;
                }
                else {
                    try {
                        argMap.put("Port", Integer.toString(Integer.parseInt(arg)));
                    }
                    catch (NumberFormatException e) {
                        Log.out.error("Wrong argument type for port");
                        throw new IllegalArgumentException("port argument type != int", e);
                    }
                }
            }
        }
        return argMap;
    }

    private static String getFlagName(String key) {
        switch (key) {
        case "-h":
        case "--help":
            return "Help";
        case "-H":
        case "--no-head":
            return "NoHead";
        case "-s":
        case "--silent":
            return "Silent";
        case "-v":
        case "--verbose":
            return "Verbose";
        case "-p":
        case "--password":
            return "Password";
        default:
            return "";
        }
    }

    private Map<String, String> arguments;

    public CommandLineArguments(Map<String, String> arugments) {
        this.arguments = new HashMap<>();
        this.arguments.putAll(arugments);
    }

    public boolean isFlagSet(String key) {
        return arguments.containsKey(key);
    }

    public String getArgument(String key) {
        return this.arguments.get(key);
    }

    public boolean help() {
        return this.isFlagSet("Help");
    }

    public boolean noHead() {
        return this.isFlagSet("NoHead");
    }

    public boolean silent() {
        return this.isFlagSet("Silent");
    }

    public boolean verbose() {
        return this.isFlagSet("Verbose");
    }

    public String host() {
        return this.getArgument("Host");
    }

    public int port() {
        return Integer.parseInt(this.getArgument("Port"));
    }

    public String password() {
        return this.arguments.remove("Password");
    }

}
