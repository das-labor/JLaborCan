package de.hansinator.incubator;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class LightMaster {

	static byte POWERCMD_ADDR = 0x02;
	static byte POWERCMD_PORT = 0x01;
	static byte BASTELCMD_ADDR = -87;
	static byte BASTELCMD_PORT = 0x01;

	static byte PCMD_SW = 0x00;
	static byte PCMD_PWM = 0x01;
	static byte PCMD_VIRT = 0x02;

	// some LAP device proxies
	private final LAPDevice powerCommander;

	private final LAPDevice bastelCommander;

	public final LoungeDimmer loungeDimmerWall;

	public final LoungeDimmer loungeDimmerDoor;

	/*
	 * lecture constants
	 */

	// light lecture switch template
	static final byte[] LAP_LECTURE_LIGHT_SWITCH = new byte[] { PCMD_SW, 0, 0, 0 };

	// light lecture pwm template
	static final byte[] LAP_LECTURE_LIGHT_PWM = new byte[] { PCMD_PWM, 0, 0, 0 };

	// light lecture virtual template
	static final byte[] LAP_LECTURE_VIRT = new byte[] { PCMD_VIRT, 0, 0, 0 };

	// light lecture virtual switch all object
	static final byte LAP_LECTURE_VIRT_SWITCH_LIGHT_ALL = 0x01;

	// light lecture virtual pwm all object
	static final byte LAP_LECTURE_VIRT_PWM_LIGHT_ALL = 0x02;

	// light lecture blackboard pwm object
	static final byte LAP_LECTURE_SWITCH_BLACKBOARD = 0x08;

	// light lecture beamer switch object
	static final byte LAP_LECTURE_SWITCH_BEAMER = 0x09;

	// light lecture locker switch object
	static final byte LAP_LECTURE_SWITCH_LOCKER = 0x0a;

	// light lecture flipper switch object
	static final byte LAP_LECTURE_SWITCH_FLIPPER = 0x0b;

	// light lecture blackboard pwm object
	static final byte LAP_LECTURE_PWM_BLACKBOARD = 0x00;

	// light lecture beamer pwm object
	static final byte LAP_LECTURE_PWM_BEAMER = 0x01;

	// light lecture locker pwm object
	static final byte LAP_LECTURE_PWM_LOCKER = 0x02;

	// light lecture flipper pwm object
	static final byte LAP_LECTURE_PWM_FLIPPER = 0x03;

	/*
	 * kitchen constants
	 */

	// light kitchen on
	static final byte[] LAP_KITCHEN_SWITCH = new byte[] { PCMD_SW, 0, 0, 0 };

	// light kitchen dim
	static final byte[] LAP_KITCHEN_PWM = new byte[] { PCMD_PWM, 0x05, 0, 0 };

	/*
	 * bastel constants
	 */

	// light bastel switch template
	static final byte[] LAP_BASTEL_LIGHT_SWITCH = new byte[] { PCMD_SW, 0, 0 };

	// light bastel pwm template
	static final byte[] LAP_BASTEL_LIGHT_PWM = new byte[] { PCMD_PWM, 0, 0 };

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
		powerCommander = new LAPDevice(0x00, 0x00, 0x02, 0x01, bus);
		bastelCommander = new LAPDevice(0x00, 0x00, 0xA9, 0x01, bus);
		loungeDimmerWall = new LoungeDimmer(bus, 0x61);
		loungeDimmerDoor = new LoungeDimmer(bus, 0x60);
	}

	public void switchKitchen(boolean state) {
		byte[] msg = LAP_KITCHEN_SWITCH.clone();
		msg[2] = (byte) (state ? 1 : 0);

		powerCommander.send(msg);
	}

	public void dimKitchen(int value) {
		byte[] msg = LAP_KITCHEN_PWM.clone();
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		powerCommander.send(msg);
	}

	public void switchLectureAll(boolean state) {
		byte[] msg = LAP_LECTURE_VIRT.clone();
		msg[1] = LAP_LECTURE_VIRT_SWITCH_LIGHT_ALL;
		msg[2] = (byte) (state ? 1 : 0);

		powerCommander.send(msg);
	}

	public void dimLectureAll(int value) {
		byte[] msg = LAP_LECTURE_VIRT.clone();
		msg[1] = LAP_LECTURE_VIRT_PWM_LIGHT_ALL;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		powerCommander.send(msg);
	}

	public void switchLectureBlackboard(boolean state) {
		byte[] msg = LAP_LECTURE_LIGHT_SWITCH.clone();
		msg[1] = LAP_LECTURE_SWITCH_BLACKBOARD;
		msg[2] = (byte) (state ? 1 : 0);

		powerCommander.send(msg);
	}

	public void dimLectureBlackboard(int value) {
		byte[] msg = LAP_LECTURE_LIGHT_PWM.clone();
		msg[1] = LAP_LECTURE_PWM_BLACKBOARD;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		powerCommander.send(msg);
	}

	public void switchLectureBeamer(boolean state) {
		byte[] msg = LAP_LECTURE_LIGHT_SWITCH.clone();
		msg[1] = LAP_LECTURE_SWITCH_BEAMER;
		msg[2] = (byte) (state ? 1 : 0);

		powerCommander.send(msg);
	}

	public void dimLectureBeamer(int value) {
		byte[] msg = LAP_LECTURE_LIGHT_PWM.clone();
		msg[1] = LAP_LECTURE_PWM_BEAMER;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		powerCommander.send(msg);
	}

	public void switchLectureLocker(boolean state) {
		byte[] msg = LAP_LECTURE_LIGHT_SWITCH.clone();
		msg[1] = LAP_LECTURE_SWITCH_LOCKER;
		msg[2] = (byte) (state ? 1 : 0);

		powerCommander.send(msg);
	}

	public void dimLectureLocker(int value) {
		byte[] msg = LAP_LECTURE_LIGHT_PWM.clone();
		msg[1] = LAP_LECTURE_PWM_LOCKER;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		powerCommander.send(msg);
	}

	public void switchLectureFlipper(boolean state) {
		byte[] msg = LAP_LECTURE_LIGHT_SWITCH.clone();
		msg[1] = LAP_LECTURE_SWITCH_FLIPPER;
		msg[2] = (byte) (state ? 1 : 0);

		powerCommander.send(msg);
	}

	public void dimLectureFlipper(int value) {
		byte[] msg = LAP_LECTURE_LIGHT_PWM.clone();
		msg[1] = LAP_LECTURE_PWM_FLIPPER;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		powerCommander.send(msg);
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

		bastelCommander.send(msg);
	}

	public void switchBastelPrinter2(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_PRINTER_2;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.send(msg);
	}

	public void switchBastelHelmer1(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_HELMER_1;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.send(msg);
	}

	public void switchBastelHelmer2(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_HELMER_2;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.send(msg);
	}

	public void switchBastelBanner(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_BANNER;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.send(msg);
	}

	public void switchBastelOrgatable(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_ORGATABLE;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.send(msg);
	}

	public void switchBastelWindow(boolean state) {
		byte[] msg = LAP_BASTEL_LIGHT_SWITCH.clone();
		msg[1] = LAP_BASTEL_SWITCH_WINDOW;
		msg[2] = (byte) (state ? 1 : 0);

		bastelCommander.send(msg);
	}

	public void dimBastelBanner(int value) {
		byte[] msg = LAP_BASTEL_LIGHT_PWM.clone();
		msg[1] = LAP_BASTEL_PWM_BANNER;
		msg[2] = (byte) (value & 0xFF);

		bastelCommander.send(msg);
	}

	public void dimBastelOrgatable(int value) {
		byte[] msg = LAP_BASTEL_LIGHT_PWM.clone();
		msg[1] = LAP_BASTEL_PWM_ORGATABLE;
		msg[2] = (byte) (value & 0xFF);

		bastelCommander.send(msg);
	}

	public void dimBastelWindow(int value) {
		byte[] msg = LAP_BASTEL_LIGHT_PWM.clone();
		msg[1] = LAP_BASTEL_PWM_WINDOW;
		msg[2] = (byte) (value & 0xFF);

		bastelCommander.send(msg);
	}
}
