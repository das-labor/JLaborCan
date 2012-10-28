package de.hansinator.incubator;

import java.util.EventListener;

import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class BastelControl extends LAPDevice {
	// command port
	static byte PORT_CMD = 0x01;

	// state message port
	static byte PORT_STATE = 0x03;

	// switch command byte
	static byte CMD_SW = 0x00;

	// pwm command byte
	static byte CMD_PWM = 0x01;

	// set motion threshold command
	static byte CMD_SET_MOTION_THRESH = 0x02;

	// request state command
	static byte CMD_REQ = 0x03;

	// request state template message
	static final byte[] BASTEL_MSG_REQUESTSTATE = new byte[] { CMD_REQ };

	// light bastel switch template
	static final byte[] BASTEL_MSG_SWITCH = new byte[] { CMD_SW, 0, 0 };

	// light bastel pwm template
	static final byte[] BASTEL_MSG_PWM = new byte[] { CMD_PWM, 0, 0 };

	// light bastel printer switch 1 object
	static final byte BASTEL_SWITCH_PRINTER_1 = 1;

	// light bastel printer switch 2 object
	static final byte BASTEL_SWITCH_PRINTER_2 = 2;

	// light bastel helmer switch 1 object
	static final byte BASTEL_SWITCH_HELMER_1 = 3;

	// light bastel helmer switch 2 object
	static final byte BASTEL_SWITCH_HELMER_2 = 4;

	// light bastel window switch object
	static final byte BASTEL_SWITCH_WINDOW = 5;

	// light bastel banner switch object
	static final byte BASTEL_SWITCH_BANNER = 6;

	// light bastel orgatable pwm object
	static final byte BASTEL_SWITCH_ORGATABLE = 7;

	// light bastel orgatable pwm object
	static final byte BASTEL_PWM_WINDOW = 0;

	// light bastel orgatable pwm object
	static final byte BASTEL_PWM_BANNER = 1;

	// light bastel orgatable pwm object
	static final byte BASTEL_PWM_ORGATABLE = 3;

	private final int pwmVals[] = new int[3];

	private final boolean switchVals[] = new boolean[8];

	private final Object lock = new Object();

	private volatile BastelStateUpdateListener listener = null;

	public interface BastelStateUpdateListener extends EventListener {
		public void onUpdate(boolean[] switchVals, int[] pwmVals);
	}

	public BastelControl(MessageBus<LAPMessage> bus) {
		this(bus, LAPAddressBook.BASTELCONTROL & 0xFF);
	}

	public BastelControl(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(deviceAddress, 0x01, 0x00, 0x00, bus);
	}

	public void setListener(BastelStateUpdateListener listener) {
		synchronized (lock) {
			this.listener = listener;
		}
	}

	@Override
	public boolean onMessageReceived(LAPMessage msg) {
		// state message from device
		if ((msg.getSrcAddr() == devAddr) && (msg.getDstPort() == PORT_STATE) && (msg.getLength() == 4)) {
			final byte[] pl = msg.getPayload();

			// decode switch state and save pwm vals
			for (int i = 0; i < switchVals.length; i++)
				switchVals[i] = (pl[0] & (1 << i)) == 1;

			// save pwm vals
			for (int i = 0; i < pwmVals.length; i++)
				pwmVals[i] = (int) pl[i + 1] & 0xff;

			synchronized (lock) {
				if (listener != null)
					listener.onUpdate(switchVals, pwmVals);
			}
			return true;
		}

		return false;
	}

	public void requestState() {
		byte[] msg = BASTEL_MSG_REQUESTSTATE.clone();
		sendTo(msg);
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
		byte[] msg = BASTEL_MSG_SWITCH.clone();
		msg[1] = BASTEL_SWITCH_PRINTER_1;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void switchBastelPrinter2(boolean state) {
		byte[] msg = BASTEL_MSG_SWITCH.clone();
		msg[1] = BASTEL_SWITCH_PRINTER_2;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void switchBastelHelmer1(boolean state) {
		byte[] msg = BASTEL_MSG_SWITCH.clone();
		msg[1] = BASTEL_SWITCH_HELMER_1;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void switchBastelHelmer2(boolean state) {
		byte[] msg = BASTEL_MSG_SWITCH.clone();
		msg[1] = BASTEL_SWITCH_HELMER_2;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void switchBastelBanner(boolean state) {
		byte[] msg = BASTEL_MSG_SWITCH.clone();
		msg[1] = BASTEL_SWITCH_BANNER;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void switchBastelOrgatable(boolean state) {
		byte[] msg = BASTEL_MSG_SWITCH.clone();
		msg[1] = BASTEL_SWITCH_ORGATABLE;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void switchBastelWindow(boolean state) {
		byte[] msg = BASTEL_MSG_SWITCH.clone();
		msg[1] = BASTEL_SWITCH_WINDOW;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(msg);
	}

	public void dimBastelBanner(int value) {
		byte[] msg = BASTEL_MSG_PWM.clone();
		msg[1] = BASTEL_PWM_BANNER;
		msg[2] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void dimBastelOrgatable(int value) {
		byte[] msg = BASTEL_MSG_PWM.clone();
		msg[1] = BASTEL_PWM_ORGATABLE;
		msg[2] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public void dimBastelWindow(int value) {
		byte[] msg = BASTEL_MSG_PWM.clone();
		msg[1] = BASTEL_PWM_WINDOW;
		msg[2] = (byte) (value & 0xFF);

		sendTo(msg);
	}

	public static void main(String[] args) {
		// create message bus
		MessageBus<LAPMessage> bus = new MessageBus<LAPMessage>();

		BastelControl bastelControl = new BastelControl(bus);

		// create gateway
		LAPTCPCanGateway gateway = LAPTCPCanGateway.makeGateway(bus, "10.0.1.2", 2342, true);
		if (gateway.up(10000, true)) {
			bastelControl.setListener(new BastelStateUpdateListener() {

				@Override
				public void onUpdate(boolean[] switchVals, int[] pwmVals) {
					for (int i = 0; i < switchVals.length; i++)
						System.out.println("switch " + i + ": " + (switchVals[i] ? "on" : "off"));
					for (int i = 0; i < pwmVals.length; i++)
						System.out.println("pwm " + i + ": " + pwmVals[i]);
				}
			});
			while (true)
				;
		} else
			System.out.println("failed to up gateway");
	}
}
