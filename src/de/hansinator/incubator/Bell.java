package de.hansinator.incubator;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class Bell extends LAPDevice {
	
	private static final byte[] MSG_BELL = new byte[]{5,0};

	public Bell(MessageBus<LAPMessage> bus) {
		this(bus, 0x04);
	}

	public Bell(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(deviceAddress, 0x00, 0x00, 0x01, bus);
	}
	
	public void ring() {
		sendFrom(MSG_BELL);
	}
}
