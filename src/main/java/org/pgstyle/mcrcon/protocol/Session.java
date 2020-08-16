/**
 * Stable: TODO WIP
 * Document: TODO document needed
 */
package org.pgstyle.mcrcon.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import org.pgstyle.mcrcon.RconUtils;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public class Session implements Closeable {

    public static final String UCI = "rcon-session";

    public static Session createSession(String server, int port, int maxRetry) {
        for (int i = 1;; i++) {
            RconUtils.verbose(Session.UCI, "Connecting to %s:%d (try %d)%n", server, port, i);
            try {
                Session session = new Session(new Socket(server, port));
                RconUtils.verbose(Session.UCI, "Connection established%n");
                return session;
            }
            catch (IOException e) {
                RconUtils.verbose(Session.UCI, "Failed to establish connection: %s%n", e.getMessage());
                if (i >= maxRetry) {
                    return null;
                }
                try {
                    RconUtils.verbose(Session.UCI, "Wait 1 second before next retry...");
                    Thread.sleep(1000);
                }
                catch (InterruptedException ne) {}
                RconUtils.verbose(null, "Retry connection%n");
            }
        }
    }

    private Session(Socket socket) {
        this.connection = socket;
    }

    private final Socket connection;

    public boolean authenticate(String password) {
        RconUtils.verbose(Session.UCI, "Authenticating...");
        boolean success = !this.request(new Packet(PacketType.SERVERDATA_AUTH, password)).isAuthError();
        RconUtils.verbose(null, "%s%n", success ? "success" : "failed");
        return success;
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        }
        catch (IOException e) {}
    }

    public String execute(String command) {
        Packet response = this.request(new Packet(PacketType.SERVERDATA_EXECCOMMAND, command));
        return response != null ? response.getBody() : null;
    }

    public boolean isAlive() {
        try {
            if (this.connection.isConnected() && !this.connection.isClosed()) {
                this.connection.getOutputStream().write(new byte[] { 10, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0 });
                RconUtils.readString(this.connection.getInputStream(), RconUtils.readInt(this.connection.getInputStream(), false, 500), 500);
            }
            return true;
        }
        catch (IOException e) {
            RconUtils.verbose(null, "died%n");
            return false;
        }
    }

    private Packet request(Packet packet) {
        if (!this.isAlive()) {
            return null;
        }
        try {
            this.connection.getOutputStream().write(packet.getBytes());
            return new Packet(this.connection.getInputStream());
        }
        catch (IOException e) {
            RconUtils.verbose(Session.UCI, "Error occurred, clossing session...%n");
            try {
                this.connection.close();
            }
            catch (IOException ne) {}
            return null;
        }
    }

}
