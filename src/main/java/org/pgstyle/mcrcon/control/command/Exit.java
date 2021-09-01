package org.pgstyle.mcrcon.control.command;

import org.pgstyle.mcrcon.rcon.RconController;

public class Exit extends Command {

    protected Exit(RconController controller) {
        super(controller);
    }

    @Override
    public CommandEntry execute(String args) {
        CommandEntry entry = new CommandEntry("exit", args);
        this.getConteoller().closeSession();
        entry.setResult("connection closed");
        entry.setEndTime();
        return entry;
    }
    
}
