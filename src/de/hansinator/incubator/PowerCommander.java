package de.hansinator.incubator;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class PowerCommander extends LAPDevice {

	// default power commander port
	final static byte POWERCMD_PORT = 0x01;

	// switch command
	final static byte CMD_SW = 0x00;
	
	// pwm command
	final static byte CMD_PWM = 0x01;
	
	// virtual channel command
	final static byte CMD_VIRT = 0x02;

	// light lecture switch template
	static final byte[] POWERCMD_MSG_SWITCH = new byte[] { CMD_SW, 0, 0, 0 };

	// light lecture pwm template
	static final byte[] POWERCMD_MSG_PWM = new byte[] { CMD_PWM, 0, 0, 0 };

	// light lecture virtual template
	static final byte[] POWERCMD_MSG_VIRT = new byte[] { CMD_VIRT, 0, 0, 0 };

	// light lecture virtual switch all object
	static final byte LECTURE_VIRT_SWITCH_LIGHT_ALL = 0x01;

	// light lecture virtual pwm all object
	static final byte LECTURE_VIRT_PWM_LIGHT_ALL = 0x02;

	// light lecture blackboard pwm object
	static final byte LECTURE_SWITCH_BLACKBOARD = 0x08;

	// light lecture beamer switch object
	static final byte LECTURE_SWITCH_BEAMER = 0x09;

	// light lecture locker switch object
	static final byte LECTURE_SWITCH_LOCKER = 0x0a;

	// light lecture flipper switch object
	static final byte LECTURE_SWITCH_FLIPPER = 0x0b;

	// light lecture blackboard pwm object
	static final byte LECTURE_PWM_BLACKBOARD = 0x00;

	// light lecture beamer pwm object
	static final byte LECTURE_PWM_BEAMER = 0x01;

	// light lecture locker pwm object
	static final byte LECTURE_PWM_LOCKER = 0x02;

	// light lecture flipper pwm object
	static final byte LECTURE_PWM_FLIPPER = 0x03;

	// light kitchen switch object
	static final byte KITCHEN_SWITCH = 0x00;

	// light kitchen pwm object
	static final byte KITCHEN_PWM = 0x05;

	public PowerCommander(MessageBus<LAPMessage> bus) {
		this(bus, LAPAddressBook.POWERCOMMANDER);
	}

	public PowerCommander(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(deviceAddress, POWERCMD_PORT, 0x00, 0x00, bus);
	}
	
	public void switchKitchen(boolean state) {
		byte[] msg = POWERCMD_MSG_SWITCH.clone();
		msg[1] = KITCHEN_SWITCH;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void dimKitchen(int value) {
		byte[] msg = POWERCMD_MSG_PWM.clone();
		msg[1] = KITCHEN_PWM;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void switchLectureAll(boolean state) {
		byte[] msg = POWERCMD_MSG_VIRT.clone();
		msg[1] = LECTURE_VIRT_SWITCH_LIGHT_ALL;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void dimLectureAll(int value) {
		byte[] msg = POWERCMD_MSG_VIRT.clone();
		msg[1] = LECTURE_VIRT_PWM_LIGHT_ALL;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void switchLectureBlackboard(boolean state) {
		byte[] msg = POWERCMD_MSG_SWITCH.clone();
		msg[1] = LECTURE_SWITCH_BLACKBOARD;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void dimLectureBlackboard(int value) {
		byte[] msg = POWERCMD_MSG_PWM.clone();
		msg[1] = LECTURE_PWM_BLACKBOARD;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void switchLectureBeamer(boolean state) {
		byte[] msg = POWERCMD_MSG_SWITCH.clone();
		msg[1] = LECTURE_SWITCH_BEAMER;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void dimLectureBeamer(int value) {
		byte[] msg = POWERCMD_MSG_PWM.clone();
		msg[1] = LECTURE_PWM_BEAMER;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void switchLectureLocker(boolean state) {
		byte[] msg = POWERCMD_MSG_SWITCH.clone();
		msg[1] = LECTURE_SWITCH_LOCKER;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void dimLectureLocker(int value) {
		byte[] msg = POWERCMD_MSG_PWM.clone();
		msg[1] = LECTURE_PWM_LOCKER;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void switchLectureFlipper(boolean state) {
		byte[] msg = POWERCMD_MSG_SWITCH.clone();
		msg[1] = LECTURE_SWITCH_FLIPPER;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void dimLectureFlipper(int value) {
		byte[] msg = POWERCMD_MSG_PWM.clone();
		msg[1] = LECTURE_PWM_FLIPPER;
		msg[2] = 0x00;
		msg[3] = (byte) (value & 0xFF);

		sendTo(msg);
	}
}
