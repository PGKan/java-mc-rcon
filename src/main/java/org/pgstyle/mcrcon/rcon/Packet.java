package org.pgstyle.mcrcon.rcon;

import java.io.IOException;
import java.io.InputStream;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public class Packet {

    public enum PacketType {

        SERVERDATA_AUTH(3, true, false),
        SERVERDATA_AUTH_RESPONSE(2, false, true),
        SERVERDATA_EXECCOMMAND(2, true, false),
        SERVERDATA_RESPONSE_VALUE(0, false, true),
        NULL(0, false, false),
        UNKNOWN(-1, false, false);
    
        private final int     value;
        private final boolean response;
        private final boolean request;
    
        private PacketType(int value, boolean request, boolean response) {
            this.value = value;
            this.response = response;
            this.request = request;
        }
    
        public int value() {
            return this.value;
        }
    
        public boolean isInvalid() {
            return this.isRequest() ^ this.isResponse();
        }
    
        public boolean isResponse() {
            return this.response;
        }
    
        public boolean isRequest() {
            return this.request;
        }
    
        public static PacketType decode(int value, boolean response) {
            if (response) {
                if (value == 0) {
                    return SERVERDATA_RESPONSE_VALUE;
                }
                else if (value == 2) {
                    return SERVERDATA_AUTH_RESPONSE;
                }
            }
            else {
                if (value == 2) {
                    return SERVERDATA_EXECCOMMAND;
                }
                else if (value == 3) {
                    return SERVERDATA_AUTH;
                }
            }
            return UNKNOWN;
        }
    }

    public static final Packet NULL = new Packet(PacketType.NULL, null);

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
        int length = RconUtils.readInt(inputStream, false, 3000);
        this.id = RconUtils.readInt(inputStream, false, 500);
        this.type = PacketType.decode(RconUtils.readInt(inputStream, false, 500), true);
        this.body = length > 10 ? RconUtils.readString(inputStream, length - 10, 500) : "";
        RconUtils.readTerminater(inputStream, 500);
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

    public boolean isNull() {
        return this.type == null || PacketType.NULL.equals(this.type);
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
