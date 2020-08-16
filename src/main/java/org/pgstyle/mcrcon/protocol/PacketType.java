/**
 * Stable: TODO WIP
 * Document: TODO document needed
 */
package org.pgstyle.mcrcon.protocol;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public enum PacketType {

    SERVERDATA_AUTH(3, true, false),
    SERVERDATA_AUTH_RESPONSE(2, false, true),
    SERVERDATA_EXECCOMMAND(2, true, false),
    SERVERDATA_RESPONSE_VALUE(0, false, true),
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

    public boolean isResponse() {
        return this.response;
    }

    public boolean isRequest() {
        return this.request;
    }

    public static PacketType decode(int value, boolean response) {
        if (response) {
            switch (value) {
            case 0:
                return SERVERDATA_RESPONSE_VALUE;
            case 2:
                return SERVERDATA_AUTH_RESPONSE;
            default:
                return UNKNOWN;
            }
        }
        else {
            switch (value) {
            case 2:
                return SERVERDATA_EXECCOMMAND;
            case 3:
                return SERVERDATA_AUTH;
            default:
                return UNKNOWN;
            }
        }
    }
}
