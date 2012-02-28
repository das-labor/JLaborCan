package de.hansinator.message;

public abstract class MessageObject {

    protected final byte[] data;

    public MessageObject(byte payload[]) {
        this.data = payload;
    }

    public int getLength() {
        return data!=null?data.length:0;
    }

    public byte[] getPayload() {
        return data;
    }

    public abstract byte[] encode();
}
