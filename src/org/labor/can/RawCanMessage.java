package org.labor.can;

import org.labor.message.Message;

/**
 *
 * @author hansinator
 */
public class RawCanMessage extends Message {
    
    public static int DATA_MAX_LENGTH = 8;

    private final int id;

    public RawCanMessage(int id, int length, byte data[]) {
        super(length, data);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public byte[] encode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RawCanMessage other = (RawCanMessage) obj;
        if (this.id != other.id) {
            return false;
        }
        return super.equals(other); //TODO: test if this works
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id;
        hash = 53 * hash + super.hashCode();
        return hash;
    }
}
