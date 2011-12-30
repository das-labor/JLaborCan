package org.labor.can.message;

/**
 *
 * @author hansinator
 */
public class Rs232CanMessage {
    public static final int DATA_MAX_LENGTH = BaseCanMessage.DATA_MAX_LENGTH + 5;
    protected final byte length;
    protected final byte data[];
    private final byte cmd;

    public Rs232CanMessage(byte cmd, byte length, byte data[]) {
        if(data == null) {
            throw new NullPointerException("Data must not be null.");
        }
        else if(data.length > DATA_MAX_LENGTH || length > DATA_MAX_LENGTH ||
                data.length < 0 || length < 0) {
            throw new IllegalArgumentException("Packet either too long or too short.");
        }

        this.length = length;

        if(length ==  0) {
            this.data = null;
        } else {
            this.data = data;
        }
        
        this.cmd = cmd;
    }

    public byte[] getData() {
        return data;
    }

    public byte getLength() {
        return length;
    }

    public byte getCmd() {
        return cmd;
    }
}
