/**
 * Stable: TODO WIP
 * Document: TODO document needed
 */
package org.pgstyle.mcrcon.protocol;

import java.io.IOException;
import java.io.InputStream;

import org.pgstyle.mcrcon.RconUtils;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public class Packet {

    private static final byte[] TERMINATOR = new byte[] { 0, 0 };

    public Packet(PacketType type, String body) {
        if (type.isResponse()) {
            throw new IllegalArgumentException("cannot create response packet with Packet.<init>(PacketType, String)");
        }
        this.id = RconUtils.randi();
        this.type = type;
        this.body = body != null ? body : "";
    }

    public Packet(InputStream inputStream) throws IOException {
        int length = RconUtils.readInt(inputStream, false, 1000);
        this.id = RconUtils.readInt(inputStream, false, 1000);
        this.type = PacketType.decode(RconUtils.readInt(inputStream, false, 1000), true);
        this.body = length > 10 ? RconUtils.readString(inputStream, length - 10, 1000) : "";
        RconUtils.readTerminater(inputStream, 1000);
    }

    private final int        id;
    private final PacketType type;
    private final String     body;

    public boolean match(Packet other) {
        return this.type.isRequest() ^ other.type.isRequest() && this.id == other.id;
    }

    public String getBody() {
        return this.body;
    }

    public PacketType getType() {
        return this.type;
    }

    public int getId() {
        return this.id;
    }

    public boolean isAuthError() {
        return this.id == -1;
    }

    public byte[] getBytes() {
        byte[] payload = this.body.getBytes(RconUtils.UTF8);
        byte[] bytes = RconUtils.fromInt(10 + payload.length, false);
        bytes = RconUtils.merge(bytes, RconUtils.fromInt(this.id, false));
        bytes = RconUtils.merge(bytes, RconUtils.fromInt(this.type.value(), false));
        bytes = RconUtils.merge(bytes, payload);
        bytes = RconUtils.merge(bytes, Packet.TERMINATOR);
        return bytes;
    }

}
