package org.pgstyle.mcrcon.control.command;

import java.io.IOException;

import org.pgstyle.mcrcon.log.Log;
import org.pgstyle.mcrcon.rcon.RconController;
import org.pgstyle.mcrcon.rcon.RconUtils;

public class Help extends Command {

    protected Help(RconController controller) {
        super(controller);
    }

    @Override
    public CommandEntry execute(String args) {
        CommandEntry entry = new CommandEntry("help", args);
        
        try {
            Log.out.info(new String(RconUtils.read(RconUtils.class.getResourceAsStream("/META-INF/mc-rcon/cmd-help")), RconUtils.UTF8));
        } catch (IOException e) {
            Log.out.error("failed to fetch help document", e);
        }

        entry.setEndTime();
        return entry;
    }
    
}
