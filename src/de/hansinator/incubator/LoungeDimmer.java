package de.hansinator.incubator;

import java.util.EventListener;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class LoungeDimmer extends LAPDevice {

	// pwm commando byte
	static byte CMD_PWM = 0x01;

	// switch commando byte
	static byte CMD_SWITCH = 0x04;
	
	// request state commando byte
	static byte CMD_REQ = 0x05;
	
	static final byte[] LAP_LOUNGE_REQUEST_STATE = new byte[] { CMD_REQ };

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

	// port of state message
	static final byte MSG_PORT_STATE = 0x03;

	private final int pwmVals[] = new int[4];

	private final boolean switchVals[] = new boolean[4];
	
	private final Object lock = new Object();

	private volatile LoungeStateUpdateListener listener = null;

	public interface LoungeStateUpdateListener extends EventListener {
		public void onUpdate(boolean[] switchVals, int[] pwmVals);
	}

	public LoungeDimmer(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(0x00, 0x00, deviceAddress, 0x01, bus);
	}

	public void setListener(LoungeStateUpdateListener listener) {
		synchronized(lock)
		{
			this.listener = listener;
		}
	}

	@Override
	public boolean onMessageReceived(LAPMessage msg) {
		// state message from device
		if ((msg.getSrcAddr() == devAddr) && (msg.getDstPort() == MSG_PORT_STATE) && (msg.getLength() == 5)) {
			final byte[] pl = msg.getPayload();

			// decode switch state and save  pwm vals
			for (int i = 0; i < switchVals.length; i++)
			{
				switchVals[i] = (pl[0] & (1 << i)) == 1;
				pwmVals[i] = (int)pl[i+1] & 0xff;
			}

			synchronized(lock)
			{
				if (listener != null)
					listener.onUpdate(switchVals, pwmVals);
			}
			return true;
		}

		return false;
	}
	
	public void requestState() {
		byte[] msg = LAP_LOUNGE_REQUEST_STATE.clone();
		sendTo(msg);
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
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_NEON;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
		dimNeonTube(0x7F);
	}

	public void switchSpot1(boolean state) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_1;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
		dimSpot1(0x7F);
	}

	public void switchSpot2(boolean state) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_2;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
		dimSpot2(0x7F);
	}

	public void switchSpot3(boolean state) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_SWITCH.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_3;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
		dimSpot3(0x7F);
	}

	public void dimNeonTube(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_NEON;
		msg[2] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void dimSpot1(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_1;
		msg[2] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void dimSpot2(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_2;
		msg[2] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void dimSpot3(int value) {
		byte[] msg = LAP_LOUNGE_LIGHT_DIMMER_PWM.clone();
		msg[1] = LAP_LOUNGE_LIGHT_SPOTS_3;
		msg[2] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public static void main(String[] args) {
		// create message bus
		MessageBus<LAPMessage> bus = new MessageBus<LAPMessage>();

		LoungeDimmer loungeDimmerWall = new LoungeDimmer(bus, 0x61);
		LoungeDimmer loungeDimmerDoor = new LoungeDimmer(bus, 0x60);

		// create gateway
		LAPTCPCanGateway gateway = LAPTCPCanGateway.makeGateway(bus, "10.0.1.2", 2342, true);
		if (gateway.up(10000, true)) {
			loungeDimmerWall.setListener(new LoungeStateUpdateListener() {

				@Override
				public void onUpdate(boolean[] switchVals, int[] pwmVals) {
					for (int i = 0; i < 4; i++)
						System.out.println("wall " + i + ": " + (switchVals[i] ? "on" : "off") + ", " + pwmVals[i]);
				}
			});
			loungeDimmerDoor.setListener(new LoungeStateUpdateListener() {

				@Override
				public void onUpdate(boolean[] switchVals, int[] pwmVals) {
					for (int i = 0; i < 4; i++)
						System.out.println("door " + i + ": " + (switchVals[i] ? "on" : "off") + ", " + pwmVals[i]);
				}
			});
			while(true);
		} else
			System.out.println("failed to up gateway");
	}
}
