package de.hansinator.incubator;

import java.io.IOException;
import java.io.InputStream;

import de.hansinator.message.io.MessageFactory;
import de.hansinator.message.protocol.CANMessage;

/**
 * @author hansinator
 */
public class LCAPMessage extends CANMessage {

	public static final int MAX_SUBADDR = 1023;

	public static final int MASK_PORT = 0x3FF;

	public static final int MAX_ADDR = 255;

	public static final int MASK_ADDR = 0xFF;

	private final byte srcAddr;
	private final byte dstAddr;
	private final int subAddr;
	private final boolean ack;
	private final boolean large;
	private final boolean req;

	/**
	 * Constructs a new immutable LAP message.
	 * 
	 * LAP messages may never be remote frames by definition.
	 * 
	 * @param srcAddr
	 *            source address
	 * @param srcPort
	 *            source port (6 bit)
	 * @param dstAddr
	 *            destination address
	 * @param dstPort
	 *            destination port (6 bit)
	 * @param data
	 *            payload
	 */
	public LCAPMessage(byte srcAddr, byte dstAddr, int subAddr, boolean ack, boolean large, byte[] data) {
		this(srcAddr, dstAddr, subAddr, ack, large, false, data);
	}

	public LCAPMessage(byte srcAddr, byte dstAddr, int subAddr, boolean ack, byte[] data, boolean request) {
		this(srcAddr, dstAddr, subAddr, ack, false, request, data);
	}

	private LCAPMessage(byte srcAddr, byte dstAddr, int subAddr, boolean ack, boolean large, boolean remote, byte[] data) {
		super((((int) dstAddr & 0xFF) << 21) | (((int) subAddr & 0xFF) << 11) | (((int) srcAddr & 0xFF) << 3)
				| (ack ? 1 << 2 : 0) | (large ? 1 << 1 : 0), data, remote);

		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
		this.subAddr = subAddr;
		this.ack = ack;
		this.large = false;
		this.req = remote;
	}

	public byte getDstAddr() {
		return dstAddr;
	}

	public byte getSrcAddr() {
		return srcAddr;
	}

	public int getSubAddr() {
		return subAddr;
	}

	public boolean isAck() {
		return ack;
	}

	public boolean isLarge() {
		return large;
	}

	public boolean isRequest() {
		return req;
	}

	public final static MessageFactory<LCAPMessage> factory = new MessageFactory<LCAPMessage>() {

		public LCAPMessage assemble(InputStream in) throws IOException {
			CANMessage msg = CANMessage.factory.assemble(in);
			byte dstAddr = (byte) ((msg.id >> 21) & 0xFF);
			byte subAddr = (byte) ((msg.id >> 11) & 0x3FF);
			byte srcAddr = (byte) ((msg.id >> 3) & 0xFF);
			boolean ack = (msg.id & 0x02) == 0x02;
			boolean large = (msg.id & 0x01) == 0x01;

			return new LCAPMessage(srcAddr, dstAddr, subAddr, ack, large, msg.isRemoteFrame(), msg.getPayload());
		}
	};
}
