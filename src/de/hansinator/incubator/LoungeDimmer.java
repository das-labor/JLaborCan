package de.hansinator.incubator;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class LoungeDimmer extends LAPDevice {

	// pwm commando byte
	static byte CMD_PWM = 0x01;

	// switch commando byte
	static byte CMD_SWITCH = 0x04;

	// light lounge dimmer switch template
	static final byte[] LAP_LOUNGE_LIGHT_DIMMER_SWITCH = new byte[] { CMD_SWITCH, 0, 0 };

	// light lounge dimmer pwm template
	static final byte[] LAP_LOUNGE_LIGHT_DIMMER_PWM = new byte[] { CMD_PWM, 0, 0 };

	// light lounge spots pwm 1 object
	static final byte LAP_LOUNGE_LIGHT_SPOTS_1 = 0x00;

	// light lounge spots pwm 2 object
	static final byte LAP_LOUNGE_LIGHT_SPOTS_2 = 0x01;

	// light lounge spots pwm 3 object
	static final byte LAP_LOUNGE_LIGHT_SPOTS_3 = 0x02;

	// light lounge neon pwm object
	static final byte LAP_LOUNGE_LIGHT_NEON = 0x03;

	public LoungeDimmer(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(0x00, 0x00, deviceAddress, 0x01, bus);
	}

	public void switchNeonTube(boolean state) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_NEON;
		msg[2] = (byte) (state ? 1 : 0);

		send(msg);
		dimNeonTube(0x7F);
	}

	public void switchSpot1(boolean state) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_1;
		msg[2] = (byte) (state ? 1 : 0);

		send(msg);
		dimSpot1(0x7F);
	}

	public void switchSpot2(boolean state) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_2;
		msg[2] = (byte) (state ? 1 : 0);

		send(msg);
		dimSpot2(0x7F);
	}

	public void switchSpot3(boolean state) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_3;
		msg[2] = (byte) (state ? 1 : 0);

		send(msg);
		dimSpot3(0x7F);
	}

	public void dimNeonTube(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_NEON;
		msg[2] = (byte) (value & 0xFF);

		send(msg);
	}

	public void dimSpot1(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_1;
		msg[2] = (byte) (value & 0xFF);

		send(msg);
	}

	public void dimSpot2(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_2;
		msg[2] = (byte) (value & 0xFF);

		send(msg);
	}

	public void dimSpot3(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_3;
		msg[2] = (byte) (value & 0xFF);

		send(msg);
	}

}
