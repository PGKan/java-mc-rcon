package org.pgstyle.mcrcon.control.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.pgstyle.mcrcon.rcon.RconController;

public class Execute extends Command {

    public Execute(RconController controller) {
        super(controller);
        commands = new HashMap<>();
        commands.put("exec", this);
        commands.put("execute", this);
        commands.put("help", new Help(controller));
        commands.put("remote", new RemoteCommand(controller));
        Command exit = new Exit(controller);
        commands.put("exit", exit);
        commands.put("disconnect", exit);
        commands.put("bye", exit);
        commands.put("quit", exit);
    }

    private final Map<String, Command> commands;

    @Override
    public CommandEntry execute(String args) {
        String command = args.substring(0, args.contains(" ") ? args.indexOf(" ") : args.length());
        args = args.substring(args.indexOf(" ") + 1).trim();
        String header = command.equals("remote") ? "remote" : "local";
        Command cmdRfe = this.commands.get(command);

        CommandEntry entry = new CommandEntry(header + ":" + command, args);
        if (!Objects.isNull(cmdRfe)) {
            try {
                entry.setResult(cmdRfe.execute(args).getResult());
            }
            catch (RuntimeException e) {
                Command.log.warn("exception occurred during command execution", e);
                entry.setResult("exception occurred during command execution, " + e.toString());
            }
        }
        else {
            entry.setResult("unknown command: " + command);
        }
        entry.setEndTime();

        return entry;
    }

}
