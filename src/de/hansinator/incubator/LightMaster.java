package de.hansinator.incubator;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class LightMaster {

	public final PowerCommander powerCommander;

	public final BastelControl bastelControl;

	public final LoungeDimmer loungeDimmerWall;

	public final LoungeDimmer loungeDimmerDoor;

	public LightMaster(MessageBus<LAPMessage> bus) {
		powerCommander = new PowerCommander(bus);
		bastelControl = new BastelControl(bus);
		loungeDimmerWall = new LoungeDimmer(bus, LAPAddressBook.LOUNGEDIMMER_WALL);
		loungeDimmerDoor = new LoungeDimmer(bus, LAPAddressBook.LOUNGEDIMMER_DOOR);
	}

	public void switchLoungeAll(boolean state) {
		switchLoungeNeonAll(state);
		switchLoungeSpotsAll(state);
	}
	
	public void switchLoungeNeonAll(boolean state) {
		loungeDimmerWall.switchNeonTube(state);
		loungeDimmerDoor.switchNeonTube(state);
	}
	

	public void switchLoungeSpotsAll(boolean state) {
		loungeDimmerWall.switchAllSpots(state);
		loungeDimmerDoor.switchAllSpots(state);
	}

	public void dimLoungeAll(int value) {
		dimLoungeNeonAll(value);
		dimLoungeSpotsAll(value);
	}

	public void dimLoungeSpotsAll(int value) {
		loungeDimmerWall.dimAllSpots(value);
		loungeDimmerDoor.dimAllSpots(value);
	}
	
	public void dimLoungeNeonAll(int value) {
		loungeDimmerWall.dimNeonTube(value);
		loungeDimmerDoor.dimNeonTube(value);
	}
}
