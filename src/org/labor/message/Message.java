package org.labor.message;

public abstract class Message {

    protected final int length;
    protected final byte[] data;

    public Message(int length, byte payload[]) {
        this.length = length;
        this.data = payload;
    }

    public int getLength() {
        return length;
    }

    public byte[] getPayload() {
        return data;
    }

    public abstract byte[] encode();
}
