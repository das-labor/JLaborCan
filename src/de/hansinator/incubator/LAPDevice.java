package de.hansinator.incubator;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.bus.MessageNode;
import de.hansinator.message.protocol.LAPMessage;

/**
 * A LAP device on a bus 
 * 
 * @author hansinator
 * 
 */
public class LAPDevice implements MessageNode<LAPMessage> {

	protected final byte devAddr;

	protected final byte devPort;

	protected final byte dstAddr;

	protected final byte dstPort;

	private final MessageBus<LAPMessage> bus;

	public LAPDevice(int deviceAddress, int devicePort, int defaultDstAddr, int defaultDstPort, MessageBus<LAPMessage> bus) {
		testAddr(defaultDstAddr, "dst");
		testAddr(deviceAddress, "dev");
		testPort((byte) defaultDstPort, "dst");
		testPort((byte) devicePort, "dev");

		if (bus == null)
			throw new IllegalArgumentException("Bus object is null");

		this.dstAddr = (byte) (defaultDstAddr & LAPMessage.MASK_ADDR);
		this.dstPort = (byte) (defaultDstPort & LAPMessage.MASK_PORT);
		this.devAddr = (byte) (deviceAddress & LAPMessage.MASK_ADDR);
		this.devPort = (byte) (devicePort & LAPMessage.MASK_PORT);
		this.bus = bus;

		bus.addMessageNode(this);
	}

	public LAPDevice(byte deviceAddress, byte devicePort, byte defaultDstAddr, byte defaultDstPort, MessageBus<LAPMessage> bus) {
		testPort((byte) devicePort, "dev");
		testPort((byte) defaultDstPort, "dst");
		if (bus == null)
			throw new IllegalArgumentException("Bus object is null");

		this.dstAddr = defaultDstAddr;
		this.dstPort = (byte) (defaultDstPort & LAPMessage.MASK_PORT);
		this.devAddr = deviceAddress;
		this.devPort = (byte) (devicePort & LAPMessage.MASK_PORT);
		this.bus = bus;
	}

	/**
	 * Lookup the devices name in the associated address book.
	 * 
	 * @return the device name or "unknown" if not found
	 */
	public String getName() {
		String name = LAPAddressBook.names.get(devAddr);
		if (name == null)
			return "unknown";
		else
			return name;
	}

	@Override
	public boolean onMessageReceived(LAPMessage msg) {
		return false;
	}

	/**
	 * Send a message from device default port to default address:port.
	 * 
	 * @param data
	 *            message payload
	 */
	public void sendFrom(byte[] data) {
		sendInternal(devAddr, devPort, dstAddr, dstPort, data);
	}

	/**
	 * Send a message from the device default port.
	 * 
	 * @param dstAddr
	 *            destination address
	 * @param dstPort
	 *            destination port
	 * @param data
	 *            message payload
	 */
	public void sendFrom(int dstAddr, int dstPort, byte[] data) {
		testAddr(dstPort, "dst");
		testPort((byte) dstPort, "dst");

		sendInternal(devAddr, devPort, (byte) (dstAddr & LAPMessage.MASK_ADDR), (byte) (dstPort & LAPMessage.MASK_PORT), data);
	}

	/**
	 * Send a message from the device.
	 * 
	 * @param dstAddr
	 *            destination address
	 * @param dstPort
	 *            destination port
	 * @param srcPort
	 *            the device source port
	 * @param data
	 *            message payload
	 */
	public void sendFrom(int dstAddr, int dstPort, int srcPort, byte[] data) {
		testAddr(dstAddr, "dst");
		testPort((byte) dstPort, "dst");
		testPort((byte) srcPort, "src");

		sendInternal(devAddr, (byte) (srcPort & LAPMessage.MASK_PORT), (byte) (dstAddr & LAPMessage.MASK_ADDR),
				(byte) (dstPort & LAPMessage.MASK_PORT), data);
	}

	/**
	 * Send a message from default address:port to device default port.
	 * 
	 * @param data
	 *            message payload
	 */
	public void sendTo(byte[] data) {
		sendInternal(dstAddr, dstPort, devAddr, devPort, data);
	}

	/**
	 * Send a message to the device default port.
	 * 
	 * @param srcAddr
	 *            source address
	 * @param srcPort
	 *            source port
	 * @param data
	 *            the message payload
	 */
	public void sendTo(int srcAddr, int srcPort, byte[] data) {
		testAddr(srcPort, "src");
		testPort((byte) srcPort, "src");

		sendInternal((byte) (srcAddr & LAPMessage.MASK_ADDR), (byte) (srcPort & LAPMessage.MASK_PORT), devAddr, devPort, data);
	}

	/**
	 * Send a message to the device.
	 * 
	 * @param srcAddr
	 *            source address
	 * @param srcPort
	 *            source port
	 * @param dstPort
	 *            the devices destination port
	 * @param data
	 *            message payload
	 */
	public void sendTo(int srcAddr, int srcPort, int dstPort, byte[] data) {
		testAddr(srcPort, "src");
		testPort((byte) srcPort, "src");
		testPort((byte) dstPort, "dst");

		sendInternal((byte) (srcAddr & LAPMessage.MASK_ADDR), (byte) (srcPort & LAPMessage.MASK_PORT), devAddr,
				(byte) (dstPort & LAPMessage.MASK_PORT), data);
	}

	protected synchronized void sendInternal(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, byte[] data) {
		bus.sendMessage(this, new LAPMessage(srcAddr, srcPort, dstAddr, dstPort, data));
	}

	private void testAddr(int addr, String prefix) {
		if ((addr & LAPMessage.MASK_ADDR) != addr)
			throw new IllegalArgumentException(prefix + " address out of range");
	}

	private void testPort(byte port, String prefix) {
		if ((port & LAPMessage.MASK_PORT) != port)
			throw new IllegalArgumentException("port out of range");
	}
}
