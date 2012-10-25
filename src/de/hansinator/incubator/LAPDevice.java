package de.hansinator.incubator;

import de.hansinator.message.protocol.LAPMessage;

/**
 * Something like a builder/factory for lap messages
 * 
 * @author hansinator
 * 
 */
public class LAPDevice implements MessageNode<LAPMessage> {

	private final byte dstAddr;

	private final byte dstPort;

	private final byte srcAddr;

	private final byte srcPort;

	private final MessageBus<LAPMessage> bus;

	public LAPDevice(int srcAddr, int srcPort, int dstAddr, int dstPort, MessageBus<LAPMessage> bus) {
		testAddr(srcAddr, "src");
		testAddr(dstAddr, "dst");
		testPort((byte) srcPort, "src");
		testPort((byte) dstPort, "dst");

		if (bus == null)
			throw new IllegalArgumentException("Bus object is null");

		this.srcAddr = (byte) (srcAddr & LAPMessage.MASK_ADDR);
		this.srcPort = (byte) (srcPort & LAPMessage.MASK_PORT);
		this.dstAddr = (byte) (dstAddr & LAPMessage.MASK_ADDR);
		this.dstPort = (byte) (dstPort & LAPMessage.MASK_PORT);
		this.bus = bus;
	}

	public LAPDevice(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, MessageBus<LAPMessage> bus) {
		testPort((byte) srcPort, "src");
		testPort((byte) dstPort, "dst");
		if (bus == null)
			throw new IllegalArgumentException("Bus object is null");

		this.srcAddr = srcAddr;
		this.srcPort = (byte) (srcPort & LAPMessage.MASK_PORT);
		this.dstAddr = dstAddr;
		this.dstPort = (byte) (dstPort & LAPMessage.MASK_PORT);
		this.bus = bus;
	}

	public void send(byte[] data) {
		send(data, false);
	}

	public void send(byte[] data, boolean remote) {
		sendInternal(srcAddr, srcPort, dstAddr, dstPort, data, remote);
	}

	public void send(int dstPort, byte[] data, boolean remote) {
		testPort((byte) dstPort, "dst");

		sendInternal(srcAddr, srcPort, dstAddr, srcPort, data, remote);
	}

	public void sendFrom(int srcAddr, int srcPort, byte[] data, boolean remote) {
		testAddr(srcPort, "src");
		testPort((byte) srcPort, "src");

		sendInternal((byte) (srcAddr & LAPMessage.MASK_ADDR), (byte) (srcPort & LAPMessage.MASK_PORT), dstAddr, dstPort,
				data, remote);
	}

	public void sendFrom(int srcAddr, int srcPort, int dstPort, byte[] data, boolean remote) {
		testAddr(srcPort, "src");
		testPort((byte) srcPort, "src");
		testPort((byte) dstPort, "dst");

		sendInternal((byte) (srcAddr & LAPMessage.MASK_ADDR), (byte) (srcPort & LAPMessage.MASK_PORT), dstAddr,
				(byte) (srcPort & LAPMessage.MASK_PORT), data, remote);
	}

	private synchronized void sendInternal(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, byte[] data, boolean remote) {
		bus.sendMessage(new LAPMessage(srcAddr, srcPort, dstAddr, dstPort, data, remote));
	}

	private void testAddr(int addr, String prefix) {
		if ((addr & LAPMessage.MASK_ADDR) != addr)
			throw new IllegalArgumentException(prefix + " address out of range");
	}

	private void testPort(byte port, String prefix) {
		if ((port & LAPMessage.MASK_PORT) != port)
			throw new IllegalArgumentException("port out of range");
	}

	@Override
	public boolean onMessageReceived(LAPMessage msg) {
		return false;
	}
}
