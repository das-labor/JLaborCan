package de.hansinator.incubator;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class LightMaster {

	// some LAP device proxies
	public final PowerCommander powerCommander;

	private final LAPDevice bastelCommander;

	public final LoungeDimmer loungeDimmerWall;

	public final LoungeDimmer loungeDimmerDoor;


	/*
	 * bastel constants
	 */
	
	static byte BASTELCMD_ADDR = -87;
	static byte BASTELCMD_PORT = 0x01;
	static byte CMD_SW = 0x00;
	static byte CMD_PWM = 0x01;

	// light bastel switch template
	static final byte[] LAP_BASTEL_LIGHT_SWITCH = new byte[] { CMD_SW, 0, 0 };

	// light bastel pwm template
	static final byte[] LAP_BASTEL_LIGHT_PWM = new byte[] { CMD_PWM, 0, 0 };

	// light bastel printer switch 1 object
	static final byte LAP_BASTEL_SWITCH_PRINTER_1 = 1;

	// light bastel printer switch 2 object
	static final byte LAP_BASTEL_SWITCH_PRINTER_2 = 2;

	// light bastel helmer switch 1 object
	static final byte LAP_BASTEL_SWITCH_HELMER_1 = 3;

	// light bastel helmer switch 2 object
	static final byte LAP_BASTEL_SWITCH_HELMER_2 = 4;

	// light bastel window switch object
	static final byte LAP_BASTEL_SWITCH_WINDOW = 5;

	// light bastel banner switch object
	static final byte LAP_BASTEL_SWITCH_BANNER = 6;

	// light bastel orgatable pwm object
	static final byte LAP_BASTEL_SWITCH_ORGATABLE = 7;

	// light bastel orgatable pwm object
	static final byte LAP_BASTEL_PWM_WINDOW = 0;

	// light bastel orgatable pwm object
	static final byte LAP_BASTEL_PWM_BANNER = 1;

	// light bastel orgatable pwm object
	static final byte LAP_BASTEL_PWM_ORGATABLE = 3;

	public LightMaster(MessageBus<LAPMessage> bus) {
		powerCommander = new PowerCommander(bus);
		bastelCommander = new LAPDevice(0xA9, 0x01, 0x00, 0x00, bus);
		loungeDimmerWall = new LoungeDimmer(bus, 0x61);
		loungeDimmerDoor = new LoungeDimmer(bus, 0x60);
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
	
	public void switchBastelAll(boolean state) {
		switchBastelPrinter1(state);
		switchBastelPrinter2(state);
		switchBastelHelmer1(state);
		switchBastelHelmer2(state);
		switchBastelBanner(state);
		switchBastelOrgatable(state);
		switchBastelWindow(state);
	}

	public void dimBastelAll(int value) {
		dimBastelBanner(value);
		dimBastelOrgatable(value);
		dimBastelWindow(value);
	}

	public void switchBastelPrinter1(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_PRINTER_1;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.sendTo(msg);
	}

	public void switchBastelPrinter2(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_PRINTER_2;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.sendTo(msg);
	}

	public void switchBastelHelmer1(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_HELMER_1;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.sendTo(msg);
	}

	public void switchBastelHelmer2(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_HELMER_2;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.sendTo(msg);
	}

	public void switchBastelBanner(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_BANNER;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.sendTo(msg);
	}

	public void switchBastelOrgatable(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_ORGATABLE;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.sendTo(msg);
	}

	public void switchBastelWindow(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_WINDOW;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.sendTo(msg);
	}

	public void dimBastelBanner(int value) {
		byte[] msg = LAP_BASTEL_LIGHT_PWM.clone();
		msg[1] = LAP_BASTEL_PWM_BANNER;
		msg[2] = (byte) (value & 0xFF);

		bastelCommander.sendTo(msg);
	}

	public void dimBastelOrgatable(int value) {
		byte[] msg = LAP_BASTEL_LIGHT_PWM.clone();
		msg[1] = LAP_BASTEL_PWM_ORGATABLE;
		msg[2] = (byte) (value & 0xFF);

		bastelCommander.sendTo(msg);
	}

	public void dimBastelWindow(int value) {
		byte[] msg = LAP_BASTEL_LIGHT_PWM.clone();
		msg[1] = LAP_BASTEL_PWM_WINDOW;
		msg[2] = (byte) (value & 0xFF);

		bastelCommander.sendTo(msg);
	}
}
