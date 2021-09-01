/**
 * Stable: TODO WIP
 * Document: TODO document needed
 */
package org.pgstyle.mcrcon_deprecated.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import org.pgstyle.mcrcon_deprecated.RconUtils;

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
        this.authenticated = false;
    }

    private final Socket connection;
    private boolean      authenticated;

    public boolean authenticate(String password) throws IOException {
        if (this.authenticated) {
            throw new IllegalStateException("already authenticated");
        }
        else if (this.isAlive()) {
            RconUtils.verbose(Session.UCI, "Authenticating...%n");
            Packet request = new Packet(PacketType.SERVERDATA_AUTH, password);
            Packet response = this.request(request);
            this.authenticated = response.getType().equals(PacketType.SERVERDATA_AUTH_RESPONSE) &&
                request.match(response) && !response.isAuthError();
            RconUtils.verbose(null, "Authentication %s%n", this.authenticated ? "success" : "failed");
            return this.authenticated;
        }
        else {
            RconUtils.err(null, "Connection is dead%n");
            throw new IOException("connection died");
        }
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        }
        catch (IOException e) {}
    }

    public String execute(String command) {
        Packet request = new Packet(PacketType.SERVERDATA_EXECCOMMAND, command);
        Packet response = this.request(request);
        return response != null ? response.getBody() : null;
    }

    public boolean isAlive() {
        if (this.connection.isConnected() && !this.connection.isClosed()) {
            return this.empty() != null;
        }
        return false;
    }

    public String empty() {
        Packet response = this.request(new Packet(PacketType.NULL, null));
        return String.format("%08x;%s;%s", response.getId(), response.getType().name(), response.getBody());
    }

    private Packet request(Packet packet) {
        try {
            this.connection.getOutputStream().write(packet.getBytes());
            return new Packet(this.connection.getInputStream());
        }
        catch (IOException e) {
            return null;
        }
    }

}
