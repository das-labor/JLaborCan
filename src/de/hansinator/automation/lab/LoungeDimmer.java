package de.hansinator.automation.lab;

import java.util.HashMap;
import java.util.Map;

import de.hansinator.automation.lap.LAPDevice;
import de.hansinator.automation.lap.LAPTCPCanGateway;
import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class LoungeDimmer extends LAPDevice {

	// ports
	static byte //
			// command
			PORT_CMD = 0x01,
			// state
			PORT_STATE = 0x03;

	// device command bytes
	static byte //
			// pwm
			CMD_PWM = 0x01,
			// switch
			CMD_SWITCH = 0x04,
			// request state
			CMD_REQ = 0x05;

	// device message templates
	static final byte[] //
			// request state template message
			MSG_REQUESTSTATE = new byte[] { CMD_REQ }, //
			// switch control template message
			MSG_SWITCH = new byte[] { CMD_SWITCH, 0, 0 }, //
			// pwm dimmer control template
			MSG_PWM = new byte[] { CMD_PWM, 0, 0 };

	/*
	 * device switch & pwm object identifier. for this device type, the switch and pwm IDs are
	 * equivalent
	 */
	static final byte //
			// spot pwm/switch 1 object
			OBJ_SPOTS_1 = 0x00, //
			// spot pwm/switch 2 object
			OBJ_SPOTS_2 = 0x01, //
			// spot pwm/switch 3 object
			OBJ_SPOTS_3 = 0x02, //
			// neon pwm/switch object
			OBJ_NEON = 0x03;

	// the current and last pwm object values
	private final int pwmVals[] = new int[4], lastPwmVals[] = new int[4];

	// the current and last switch object values
	private final boolean switchVals[] = new boolean[4], lastSwitchVals[] = new boolean[4];
	
	// map device switch objects to object identifiers
	public static final Map<Byte, Integer> omap_switch = new HashMap<Byte, Integer>();
	
	// map object identifiers to device pwm objects
	public static final Map<Byte, Integer> omap_pwm = new HashMap<Byte, Integer>();
	
	// init maps
	static {
		omap_switch.put(new Byte(OBJ_SPOTS_1), Objects.switch_spot1.ordinal());
		omap_switch.put(new Byte(OBJ_SPOTS_2), Objects.switch_spot2.ordinal());
		omap_switch.put(new Byte(OBJ_SPOTS_3), Objects.switch_spot3.ordinal());
		omap_switch.put(new Byte(OBJ_NEON), Objects.switch_neon.ordinal());
		omap_pwm.put(new Byte(OBJ_SPOTS_1), Objects.pwm_spot1.ordinal());
		omap_pwm.put(new Byte(OBJ_SPOTS_2), Objects.pwm_spot2.ordinal());
		omap_pwm.put(new Byte(OBJ_SPOTS_3), Objects.pwm_spot3.ordinal());
		omap_pwm.put(new Byte(OBJ_NEON), Objects.pwm_neon.ordinal());
	}

	public enum Objects {
		pwm_spot1, pwm_spot2, pwm_spot3, pwm_neon, switch_spot1, switch_spot2, switch_spot3, switch_neon;
	};

	public LoungeDimmer(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(bus, deviceAddress, PORT_CMD);
	}
	
	public int[] getPwmVals() {
		return pwmVals;
	}
	
	public boolean[] getSwitchVals() {
		return switchVals;
	}

	@Override
	public boolean onMessageFromDevice(LAPMessage msg) {
		// state message from device
		if ((msg.getDstPort() == PORT_STATE) && (msg.getLength() == 5)) {
			final byte[] pl = msg.getPayload();

			// decode switch state and save pwm vals
			for (int i = 0; i < switchVals.length; i++) {
				int oi = 0;
				boolean ob = false, ui = false, ub = false;

				// fetch new values
				switchVals[i] = (pl[0] & (1 << i)) == (1 << i);
				pwmVals[i] = (int) pl[i + 1] & 0xff;

				// if there is a change, cache and update last values
				if (switchVals[i] != lastSwitchVals[i]) {
					ob = lastSwitchVals[i];
					lastSwitchVals[i] = switchVals[i];
					ub = true;
				}
				if (pwmVals[i] != lastPwmVals[i]) {
					oi = lastPwmVals[i];
					lastPwmVals[i] = pwmVals[i];
					ui = true;
				}

				// notify listeners
				if (ub)
					notifyListeners(omap_switch.get((byte)i), switchVals[i], ob);
				if (ui)
					notifyListeners(omap_pwm.get((byte)i), pwmVals[i], oi);
			}

			return true;
		}
		return false;
	}

	@Override
	protected boolean onMessageToDevice(LAPMessage message) {
		return false;
	}

	public void requestState() {
		byte[] msg = MSG_REQUESTSTATE.clone();
		sendTo(0x00, 0x00, msg);
	}

	public void switchAll(boolean state) {
		switchNeonTube(state);
		switchAllSpots(state);
	}

	public void switchAllSpots(boolean state) {
		switchSpot1(state);
		switchSpot2(state);
		switchSpot3(state);
	}

	public void dimAll(int value) {
		dimNeonTube(value);
		dimAllSpots(value);
	}

	public void dimAllSpots(int value) {
		dimSpot1(value);
		dimSpot2(value);
		dimSpot3(value);
	}

	public void switchNeonTube(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = OBJ_NEON;
		msg[2] = (byte) (state ? 1 : 0);
		
		lastSwitchVals[3] = switchVals[3];
		switchVals[3] = state; 

		sendTo(0x00, 0x00, msg);
		dimNeonTube(0x7F);
	}

	public void switchSpot1(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = OBJ_SPOTS_1;
		msg[2] = (byte) (state ? 1 : 0);
		
		lastSwitchVals[0] = switchVals[0];
		switchVals[0] = state;

		sendTo(0x00, 0x00, msg);
		dimSpot1(0x7F);
	}

	public void switchSpot2(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = OBJ_SPOTS_2;
		msg[2] = (byte) (state ? 1 : 0);
		
		lastSwitchVals[1] = switchVals[1];
		switchVals[1] = state;

		sendTo(0x00, 0x00, msg);
		dimSpot2(0x7F);
	}

	public void switchSpot3(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = OBJ_SPOTS_3;
		msg[2] = (byte) (state ? 1 : 0);
		
		lastSwitchVals[2] = switchVals[2];
		switchVals[2] = state;

		sendTo(0x00, 0x00, msg);
		dimSpot3(0x7F);
	}

	public void dimNeonTube(int value) {
		byte[] msg = MSG_PWM.clone();
		msg[1] = OBJ_NEON;
		msg[2] = (byte) (value & 0xFF);
		
		lastPwmVals[3] = pwmVals[3];
		pwmVals[3] = value; 

		sendTo(0x00, 0x00, msg);
	}

	public void dimSpot1(int value) {
		byte[] msg = MSG_PWM.clone();
		msg[1] = OBJ_SPOTS_1;
		msg[2] = (byte) (value & 0xFF);
		
		lastPwmVals[0] = pwmVals[0];
		pwmVals[0] = value; 

		sendTo(0x00, 0x00, msg);
	}

	public void dimSpot2(int value) {
		byte[] msg = MSG_PWM.clone();
		msg[1] = OBJ_SPOTS_2;
		msg[2] = (byte) (value & 0xFF);
		
		lastPwmVals[1] = pwmVals[1];
		pwmVals[1] = value; 

		sendTo(0x00, 0x00, msg);
	}

	public void dimSpot3(int value) {
		byte[] msg = MSG_PWM.clone();
		msg[1] = OBJ_SPOTS_3;
		msg[2] = (byte) (value & 0xFF);
		
		lastPwmVals[2] = pwmVals[2];
		pwmVals[2] = value; 

		sendTo(0x00, 0x00, msg);
	}

	public static void main(String[] args) {
		// create message bus
		MessageBus<LAPMessage> bus = new MessageBus<LAPMessage>();

		LoungeDimmer loungeDimmerWall = new LoungeDimmer(bus, 0x61);
		LoungeDimmer loungeDimmerDoor = new LoungeDimmer(bus, 0x60);

		// create gateway
		LAPTCPCanGateway gateway = LAPTCPCanGateway.makeGateway(bus, "10.0.1.2", 2342, true);
		if (gateway.blockingStart(10000)) {
			loungeDimmerWall.setListener(new LAPStateUpdateListener() {

				@Override
				public void onUpdate(int key, Object value, Object lastValue) {
					System.out.println("wall " + Objects.values()[key] + ": " + value.toString());
				}
			});
			loungeDimmerDoor.setListener(new LAPStateUpdateListener() {

				@Override
				public void onUpdate(int key, Object value, Object lastValue) {
					System.out.println("door " + Objects.values()[key] + ": " + value.toString());
				}
			});
			loungeDimmerDoor.requestState();
			loungeDimmerWall.requestState();
			while (true)
				Thread.yield();
		} else
			System.out.println("failed to up gateway");
	}
}
