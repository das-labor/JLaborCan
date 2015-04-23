package de.hansinator.automation.lab;

import de.hansinator.automation.lap.LAPDevice;
import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class Bell extends LAPDevice {
	
	private static final byte[] MSG_BELL = new byte[]{5,0};

	public Bell(MessageBus<LAPMessage> bus) {
		this(bus, LabAddressBook.HAUPTSCHALTER);
	}

	public Bell(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(bus, deviceAddress, 0x00);
	}
	
	public void ring() {
		sendFrom(0x00, 0x01, MSG_BELL);
	}

	@Override
	protected boolean onMessageToDevice(LAPMessage message) {
		return false;
	}

	@Override
	protected boolean onMessageFromDevice(LAPMessage message) {
		return false;
	}
}
