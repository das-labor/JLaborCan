package org.labor.can;

import java.io.IOException;
import java.io.InputStream;
import org.labor.message.MessageFactory;
import org.labor.message.MessageInputStream;
import org.labor.message.MessageObject;
import org.labor.message.MessageOutputStream;

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
public class LAPMessage extends CANMessage {

    private final byte srcAddr;
    private final byte dstAddr;
    private final byte srcPort;
    private final byte dstPort;

    public LAPMessage(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, byte[] data, boolean remote) {
        super((((srcPort & 0x1F) << 2) | ((dstPort & 0x30) >> 4)
                | ((int) (((dstPort & 0x0C) << 3) | (dstPort & 0x03)) << 8)
                | ((int) srcAddr << 16)
                | ((int) dstAddr << 24)), data, remote);

        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
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
    public final static MessageFactory<LAPMessage> factory = new MessageFactory<LAPMessage>() {

        public LAPMessage assemble(InputStream in) throws IOException {
            CANMessage msg = CANMessage.factory.assemble(in);
            byte srcAddr = (byte) ((msg.id >> 8) & 0xFF);
            byte dstAddr = (byte) (msg.id & 0xFF);
            byte srcPort = (byte) ((msg.id >> 23) & 0x3F);
            byte dstPort = (byte) (((msg.id >> 16) & 0x0F) | ((msg.id >> 17) & 0x30));

            return new LAPMessage(srcAddr, srcPort, dstAddr, dstPort, msg.getPayload(), msg.remote);
        }
    };
}
