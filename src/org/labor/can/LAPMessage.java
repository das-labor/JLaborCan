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
public class LAPMessage extends MessageObject {

    private final byte srcAddr;
    private final byte dstAddr;
    private final byte srcPort;
    private final byte dstPort;

    public LAPMessage(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, byte[] data) {
        super(data);

        this.srcAddr = srcAddr;
        this.dstAddr = srcAddr;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    public static LAPMessage fromCANMessage(CANMessage msg) {
        int id = msg.getId();

        byte srcAddr = (byte) ((id >> 16) & 0xFF);
        byte dstAddr = (byte) ((id >> 24) & 0xFF);
        byte srcPort = (byte) ((id & 0xFF) >> 2);
        byte dstPort = (byte) (((id & 0x03) << 4) | ((id & 0x0C00) >> 11) | ((id & 0x0300) >> 8));

        return new LAPMessage(srcAddr, srcPort, dstAddr, dstPort, msg.getPayload());
    }

    public CANMessage toCANMessage() {
        return new CANMessage((((srcPort & 0x1F) << 2) | ((dstPort & 0x30) >> 4)
                | ((int) (((dstPort & 0x0C) << 3) | (dstPort & 0x03)) << 8)
                | ((int) srcAddr << 16)
                | ((int) dstAddr << 24)), data, false);
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
    public final static MessageFactory<LAPMessage> factory = new MessageFactory<LAPMessage>() {

        public LAPMessage assemble(InputStream in) throws IOException {
            return LAPMessage.fromCANMessage(CANMessage.factory.assemble(in));
        }
    };

    public static class CANMessageInputAdapter implements MessageInputStream<LAPMessage> {

        final MessageInputStream<CANMessage> source;

        public CANMessageInputAdapter(MessageInputStream<CANMessage> in) {
            source = in;
        }

        public LAPMessage read() throws IOException {
            return LAPMessage.fromCANMessage(source.read());
        }
    }
    
    public static class CANMessageOutputAdapter implements MessageOutputStream<LAPMessage> {

        final MessageOutputStream<CANMessage> sink;

        public CANMessageOutputAdapter(MessageOutputStream<CANMessage> out) {
            sink = out;
        }

        public void write(LAPMessage message) throws IOException {
            sink.write(message.toCANMessage());
        }

    }
}