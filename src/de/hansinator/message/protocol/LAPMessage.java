package de.hansinator.message.protocol;

import java.io.IOException;
import java.io.InputStream;
import de.hansinator.message.io.MessageFactory;

/**
 * @author hansinator
 */
public class LAPMessage extends CANMessage {
	
	public static final int MAX_PORT = 63;
	
	public static final int MASK_PORT = 0x3F;
	
	public static final int MAX_ADDR = 255;
	
	public static final int MASK_ADDR = 0xFF;

    private final byte srcAddr;
    private final byte dstAddr;
    private final byte srcPort;
    private final byte dstPort;

    /**
     * Constructs a new immutable LAP message.
     * 
     * LAP messages may never be remote frames by definition.
     * 
     * @param srcAddr source address
     * @param srcPort source port (6 bit)
     * @param dstAddr destination address
     * @param dstPort destination port (6 bit)
     * @param data payload
     */
    public LAPMessage(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, byte[] data) {
        super(((srcPort & 0x3F) << 23) | ((dstPort & 0x30) << 17)
                | ((int) ((dstPort & 0x0F) << 16)
                | (((int) srcAddr & 0xFF) << 8)
                | ((int) dstAddr) & 0xFF), data, false);

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

    	//XXX: this must be a loop that discards remote frames, otherwise clients might get confused
        public LAPMessage assemble(InputStream in) throws IOException {
            CANMessage msg = CANMessage.factory.assemble(in);
            byte srcAddr = (byte) ((msg.id >> 8) & 0xFF);
            byte dstAddr = (byte) (msg.id & 0xFF);
            byte srcPort = (byte) ((msg.id >> 23) & 0x3F);
            byte dstPort = (byte) (((msg.id >> 16) & 0x0F) | ((msg.id >> 17) & 0x30));

            return new LAPMessage(srcAddr, srcPort, dstAddr, dstPort, msg.getPayload());
        }
    };
}
