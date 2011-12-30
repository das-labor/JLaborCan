package org.labor.can.message;

import java.util.Arrays;

/**
 *
 * @author hansinator
 */
public abstract class BaseCanMessage {

    public static final int DATA_MAX_LENGTH = 8;
    protected final byte length;
    protected final byte data[];

    BaseCanMessage(byte length, byte data[]) {
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
    }

    public byte[] getData() {
        return data;
    }

    public byte getLength() {
        return length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseCanMessage other = (BaseCanMessage) obj;
        if (this.length != other.length) {
            return false;
        }
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.length;
        hash = 13 * hash + Arrays.hashCode(this.data);
        return hash;
    }
}
