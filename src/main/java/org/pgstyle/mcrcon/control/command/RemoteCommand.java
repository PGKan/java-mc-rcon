package org.pgstyle.mcrcon.control.command;

import org.pgstyle.mcrcon.rcon.RconController;

public class RemoteCommand extends Command {

    public RemoteCommand(RconController controller) {
        super(controller);
    }

    @Override
    public CommandEntry execute(String args) {
        String command = args.substring(0, args.indexOf(" "));
        args = args.substring(args.indexOf(" ") + 1).trim();

        CommandEntry entry = new CommandEntry("remote/" + command, args);
        try {
            entry.setResult(this.getConteoller().execute(command + " " + args));
        }
        catch (IllegalStateException e) {
            Command.log.warn("execution is unavailable", e);
            entry.setResult("execution is unavailable, " + e.toString());
        }
        entry.setEndTime();

        return entry;
    }

}
