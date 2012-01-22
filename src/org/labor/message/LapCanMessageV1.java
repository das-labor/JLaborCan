package org.labor.message;

import org.labor.message.Message;
import org.labor.can.RawCanMessage;

/**
 *
 * Raw Binary Packet Format:
 *
 * Byte 1
 *  2..7 -> port src 0..5
 *  0..1 -> port dst 4..5
 *
 * Byte 2
 *  5..6 -> port dst 2..3
 *  0..1 -> port dst 0..1
 *
 * Byte 3 -> source addr
 * 
 * Byte 4 -> dest addr
 *
 * Byte 5
 *  0..3 -> len
 *
 * Byte 6..13 -> data
 *
 *
 * @author hansinator
 */
public class LapCanMessageV1 extends Message {

    private final byte srcAddr;
    private final byte dstAddr;
    private final byte srcPort;
    private final byte dstPort;

    public LapCanMessageV1(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, int length, byte[] data) {
        super(length, data);
        
        this.srcAddr = srcAddr;
        this.dstAddr = srcAddr;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    public static LapCanMessageV1 fromRawCanMessage(RawCanMessage msg) {
        int id = msg.getId();

        byte srcAddr = (byte)((id >> 16) & 0xFF);
        byte dstAddr = (byte)((id >> 24) & 0xFF);
        byte srcPort = (byte)((id & 0xFF) >> 2);
        byte dstPort = (byte)(((id & 0x03) << 4) | ((id & 0x0C00) >> 11) | ((id & 0x0300) >> 8));

        return new LapCanMessageV1(srcAddr, srcPort, dstAddr, dstPort, msg.getLength(), msg.getPayload());
    }

    public RawCanMessage toRawCanMessage() {
        return new RawCanMessage((((srcPort & 0x1F) << 2) | ((dstPort & 0x30) >> 4) |
                ((int)(((dstPort & 0x0C) << 3) | (dstPort & 0x03)) << 8) |
                ((int)srcAddr << 16) |
                ((int)dstAddr << 24)), (byte)(length & 0x0F), data);
    }

    public byte getDstAddr() {
        return dstAddr;
    }

    public byte getDstPort() {
        return dstPort;
    }

    public byte getSrcAddr() {
        return srcAddr;
    }

    public byte getSrcPort() {
        return srcPort;
    }

    @Override
    public byte[] encode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
