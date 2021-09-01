package org.pgstyle.mcrcon.control.command;

import org.pgstyle.mcrcon.log.Log;
import org.pgstyle.mcrcon.rcon.RconController;

public abstract class Command {

    public static final Log log = Log.getLog("rcon-cmd");

    protected Command(RconController controller) {
        this.controller = controller;
    }

    private RconController controller;

    protected RconController getConteoller() {
        return this.controller;
    }

    public abstract CommandEntry execute(String args);

}
