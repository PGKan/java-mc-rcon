package org.pgstyle.mcrcon.rcon;

import java.io.IOException;
import java.util.Optional;

import org.pgstyle.mcrcon.log.Log;

public class RconController {

    private static final Log log = Log.getLog("rcon");

    private Session session;

    public boolean crateSession(String host, int port, int maxRetry) {
        RconController.log.debug("Create session to {}:{}", host, port);
        if (Optional.ofNullable(this.session).map(Session::isAlive).orElse(false)) {
            Log.out.warn("Old session exists, disconnect before establishing new session");
            this.closeSession();
        }
        this.session = Session.createSession(host, port, maxRetry);
        if (this.session == null) {
            Log.out.error("Connection failed");
            return false;
        }
        return true;
    }

    public void closeSession() {
        RconController.log.debug("Closing session");
        Optional.ofNullable(this.session).ifPresent(Session::close);
        this.session = null;
    }

    public boolean authenticate(String password) {
        if (password != null) {
            RconController.log.debug("Found preloaded password, use preloaded password instead of prompting for input");
            try {
                this.session.authenticate(password);
            }
            catch (IOException e) {
                RconController.log.debug("Connection died while authenticating");
                this.closeSession();
                return false;
            }
            if (!this.session.isAuthenticated()) {
                RconController.log.info("Authentication failed using the infomation from command line argument, prompt for password input");
            }
        }
        for (int i = 3; !this.session.isAuthenticated() && i > 0; i--) {
            Log.out.info("Input password...");
            Log.prompt(": ");
            try {
                this.session.authenticate(new String(System.console().readPassword()));
            }
            catch (IOException e) {
                RconController.log.debug("Connection died while authenticating");
                this.closeSession();
                return false;
            }
            if (!this.session.isAuthenticated() && i != 0) {
                Log.out.info("Authentication failed, please try again");
            }
        }
        if (this.session.isAuthenticated()) {
            RconController.log.debug("Authentication finished");
        }
        else {
            RconController.log.debug("Authentication failed");
        }
        return this.session.isAuthenticated();
    }

    public String execute(String command) {
        if (!this.session.isAlive()) {
            RconController.log.debug("Connection died");
            this.closeSession();
            throw new IllegalStateException("Rcon connection is dead");
        }
        if (!this.session.isAuthenticated()) {
            RconController.log.debug("Not authenticated");
            throw new IllegalStateException("Rcon session is not authenticated");
        }
        if (command.length() > 0) {
            String result = this.session.execute(command);
            RconController.log.debug("execute command: ", command);
            RconController.log.debug("result length: ", result.length());
            return result;
        }
        return null;
    }

    public boolean isAlive() {
        return this.session.isAlive();
    }

}
