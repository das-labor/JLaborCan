package de.hansinator.automation.lap;

import java.util.EventListener;

import de.hansinator.automation.lab.LabAddressBook;
import de.hansinator.message.bus.BaseNode;
import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

/**
 * An abstract LAP device on a bus. Concrete devices implement behavior for message retrieval and
 * methods to send messages. Each "public" value-bearing object of the lap device shall be
 * represented by a unique object key that is used to notify listeners of value changes.
 * 
 * @author hansinator
 * 
 */
public abstract class LAPDevice extends BaseNode<LAPMessage> {

	protected final byte devAddr;

	protected final byte devPort;

	protected final Object listenerLock = new Object();

	private volatile LAPStateUpdateListener listener = null;

	/**
	 * State update listener interface
	 * 
	 * @author hansinator
	 * 
	 */
	public interface LAPStateUpdateListener extends EventListener {
		/**
		 * Called when the device state is being updated, i.e. a given key-value pair of the devices
		 * variable/object-set is changed by an external message.
		 * 
		 * @param key
		 *            object index
		 * @param value
		 *            new object value
		 * @param lastValue
		 *            previous value
		 */
		public void onUpdate(int key, Object value, Object lastValue);
	}

	/**
	 * Construct a LAP device on a bus with the given address and default port.
	 * 
	 * @param bus
	 *            message bus
	 * @param deviceAddress
	 *            device address
	 * @param devicePort
	 *            default port
	 */
	public LAPDevice(MessageBus<LAPMessage> bus, int deviceAddress, int devicePort) {
		super(bus);
		testAddr(deviceAddress, "dev");
		testPort((byte) devicePort, "dev");
		if (bus == null)
			throw new IllegalArgumentException("Bus object is null");

		this.devAddr = (byte) (deviceAddress & LAPMessage.MASK_ADDR);
		this.devPort = (byte) (devicePort & LAPMessage.MASK_PORT);
	}

	/**
	 * Construct a LAP device on a bus with the given address and default port.
	 * 
	 * @param bus
	 *            message bus
	 * @param deviceAddress
	 *            device address
	 * @param devicePort
	 *            default port
	 */
	public LAPDevice(MessageBus<LAPMessage> bus, byte deviceAddress, byte devicePort) {
		super(bus);
		testPort((byte) devicePort, "dev");
		if (bus == null)
			throw new IllegalArgumentException("Bus object is null");

		this.devAddr = deviceAddress;
		this.devPort = (byte) (devicePort & LAPMessage.MASK_PORT);
	}

	/**
	 * Lookup the devices name in the associated address book.
	 * 
	 * @return the device name or "unknown" if not found
	 */
	public String getName() {
		String name = LabAddressBook.names.get(devAddr);
		if (name == null)
			return "unknown";
		else
			return name;
	}

	public void setListener(LAPStateUpdateListener listener) {
		synchronized (listenerLock) {
			this.listener = listener;
		}
	}

	protected void notifyListeners(int key, Object value, Object lastValue) {
		synchronized (listenerLock) {
			if (listener != null)
				listener.onUpdate(key, value, lastValue);
		}
	}

	@Override
	final public boolean onMessageReceived(LAPMessage message) {
		// message from device
		if (message.getSrcAddr() == devAddr)
			return onMessageFromDevice(message);
		// message to device
		else if (message.getDstAddr() == devAddr)
			return onMessageToDevice(message);
		return false;
	}

	/**
	 * Called when a bus message targets this device.
	 * 
	 * @param message
	 *            message object
	 * @return true if the message has been processed
	 */
	abstract protected boolean onMessageToDevice(LAPMessage message);

	/**
	 * Called when a message from this device has been seen on the bus.
	 * 
	 * @param message
	 *            message object
	 * @return true if the message has been processed
	 */
	abstract protected boolean onMessageFromDevice(LAPMessage message);

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
	final protected void sendFrom(int dstAddr, int dstPort, byte[] data) {
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
	final protected void sendFrom(int dstAddr, int dstPort, int srcPort, byte[] data) {
		testAddr(dstAddr, "dst");
		testPort((byte) dstPort, "dst");
		testPort((byte) srcPort, "src");

		sendInternal(devAddr, (byte) (srcPort & LAPMessage.MASK_PORT), (byte) (dstAddr & LAPMessage.MASK_ADDR),
				(byte) (dstPort & LAPMessage.MASK_PORT), data);
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
	final protected void sendTo(int srcAddr, int srcPort, byte[] data) {
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
	final protected void sendTo(int srcAddr, int srcPort, int dstPort, byte[] data) {
		testAddr(srcPort, "src");
		testPort((byte) srcPort, "src");
		testPort((byte) dstPort, "dst");

		sendInternal((byte) (srcAddr & LAPMessage.MASK_ADDR), (byte) (srcPort & LAPMessage.MASK_PORT), devAddr,
				(byte) (dstPort & LAPMessage.MASK_PORT), data);
	}

	/**
	 * Wrap {@link #sendMessage(LAPMessage)} for LAP messages.
	 * 
	 * @param srcAddr
	 *            source address
	 * @param srcPort
	 *            source port
	 * @param dstAddr
	 *            destination address
	 * @param dstPort
	 *            destination port
	 * @param data
	 *            message payload
	 */
	final protected synchronized void sendInternal(byte srcAddr, byte srcPort, byte dstAddr, byte dstPort, byte[] data) {
		sendMessage(new LAPMessage(srcAddr, srcPort, dstAddr, dstPort, data));
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
