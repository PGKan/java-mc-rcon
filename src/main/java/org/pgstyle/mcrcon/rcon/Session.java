package org.pgstyle.mcrcon.rcon;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

import org.pgstyle.mcrcon.log.Log;
import org.pgstyle.mcrcon.rcon.Packet.PacketType;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public class Session implements Closeable {

    private static final Log log = Log.getLog("rcon-session");

    public static Session createSession(String server, int port, int maxRetry) {
        for (int i = 1; i <= maxRetry; i++) {
            Session.log.debug("Connecting to {}:{} (try {})", server, port, i);
            try {
                Session session = new Session(new Socket(server, port));
                Session.log.debug("Connection established");
                return session;
            }
            catch (IOException e) {
                Session.log.debug("Failed to establish connection: {}", e.getMessage());
            }
        }
        Session.log.debug("Retry count exceeds limit ({}), retire", maxRetry);
        return null;
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
            Session.log.debug("Authenticating...");
            Packet request = new Packet(PacketType.SERVERDATA_AUTH, password);
            Packet response = this.request(request);
            this.authenticated = response.getType().equals(PacketType.SERVERDATA_AUTH_RESPONSE) &&
                request.match(response) && !response.isAuthError();
            Session.log.debug("Authentication {}", this.authenticated ? "success" : "failed");
            return this.authenticated;
        }
        else {
            Session.log.error("Connection is dead");
            throw new IOException("connection died");
        }
    }

    @Override
    public void close() {
        Session.log.debug("Disconnect session");
        try {
            this.connection.close();
        }
        catch (IOException e) {
            Session.log.warn("Connection is dead already");
            // probably already closed
        }
        Session.log.debug("Session ended");
    }

    public String execute(String command) {
        Packet request = new Packet(PacketType.SERVERDATA_EXECCOMMAND, command);
        Packet response = this.request(request);
        return response.isNull() ? response.getBody() : null;
    }

    public boolean isAlive() {
        if (this.connection.isConnected() && !this.connection.isClosed()) {
            Session.log.debug("Check connection");
            return this.empty() != null;
        }
        return false;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public String empty() {
        Packet response = this.request(Packet.NULL);
        return Optional.ofNullable(response)
                       .map(r -> String.format("%08x;%s;%s", response.getId(), response.getType().name(), response.getBody()))
                       .orElse(null);
    }

    private Packet request(Packet packet) {
        try {
            this.connection.getOutputStream().write(packet.getBytes());
            return new Packet(this.connection.getInputStream());
        }
        catch (IOException e) {
            return Packet.NULL;
        }
    }

}
