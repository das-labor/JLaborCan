package de.hansinator.automation.lab;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import de.hansinator.automation.lab.LoungeDimmer.Objects;
import de.hansinator.automation.lap.LAPDevice;
import de.hansinator.automation.lap.LAPTCPCanGateway;
import de.hansinator.message.bus.MessageBus;
import de.hansinator.message.protocol.LAPMessage;

public class BastelControl extends LAPDevice {
	// ports
	static byte //
			// command
			PORT_CMD = 0x01,
			// state
			PORT_STATE = 0x03;

	// device command bytes
	static byte //
			// switch command byte
			CMD_SW = 0x00,
			// pwm command byte
			CMD_PWM = 0x01,
			// set motion threshold command
			CMD_SET_MOTION_THRESH = 0x02,
			// request state command
			CMD_REQ = 0x03;

	// device message templates
	static final byte[] //
			// request state template message
			MSG_REQUESTSTATE = new byte[] { CMD_REQ },
			// switch control template message
			MSG_SWITCH = new byte[] { CMD_SW, 0, 0 },
			// pwm control template message
			MSG_PWM = new byte[] { CMD_PWM, 0, 0 };

	// switch object identifiers
	static final byte //
			// printer switch 1 object
			SW_PRINTER_1 = 1,
			// printer switch 2 object
			SW_PRINTER_2 = 2,
			// helmer switch 1 object
			SW_HELMER_1 = 3,
			// helmer switch 2 object
			SW_HELMER_2 = 4,
			// window switch object
			SW_WINDOW = 5,
			// banner switch object
			SW_BANNER = 6,
			// orgatable switch object
			SW_ORGATABLE = 7;

	// device object identifiers
	static final byte //
			// window pwm object
			PWM_WINDOW = 0,
			// banner pwm object
			PWM_BANNER = 1,
			// orgatable pwm object
			PWM_ORGATABLE = 3;

	// the current and last pwm object values
	private final int pwmVals[] = new int[3], lastPwmVals[] = new int[3];

	// the current and last switch object values
	private final boolean switchVals[] = new boolean[7], lastSwitchVals[] = new boolean[7];

	// map device switch objects to object identifiers
	public static final Map<Byte, Integer> omap_switch = new HashMap<Byte, Integer>();

	// map object identifiers to device pwm objects
	public static final Map<Byte, Integer> omap_pwm = new HashMap<Byte, Integer>();

	// init maps
	static {
		omap_switch.put(new Byte(SW_PRINTER_1), Objects.switch_printer1.ordinal());
		omap_switch.put(new Byte(SW_PRINTER_2), Objects.switch_printer2.ordinal());
		omap_switch.put(new Byte(SW_HELMER_1), Objects.switch_helmer1.ordinal());
		omap_switch.put(new Byte(SW_HELMER_2), Objects.switch_helmer2.ordinal());
		omap_switch.put(new Byte(SW_WINDOW), Objects.switch_window.ordinal());
		omap_switch.put(new Byte(SW_BANNER), Objects.switch_banner.ordinal());
		omap_switch.put(new Byte(SW_ORGATABLE), Objects.switch_orgatable.ordinal());
		omap_pwm.put((byte)0, Objects.pwm_window.ordinal());
		omap_pwm.put((byte)1, Objects.pwm_banner.ordinal());
		omap_pwm.put((byte)2, Objects.pwm_orgatable.ordinal());
	}

	public enum Objects {
		pwm_window, pwm_banner, pwm_orgatable, switch_printer1, switch_printer2, switch_helmer1, switch_helmer2, switch_window, switch_banner, switch_orgatable;
	};

	public interface BastelStateUpdateListener extends EventListener {
		public void onUpdate(boolean[] switchVals, int[] pwmVals);
	}

	public BastelControl(MessageBus<LAPMessage> bus) {
		this(bus, LabAddressBook.BASTELCONTROL & 0xFF);
	}

	public BastelControl(MessageBus<LAPMessage> bus, int deviceAddress) {
		super(bus, deviceAddress, 0x01);
	}

	@Override
	protected boolean onMessageFromDevice(LAPMessage msg) {
		// state message from device
		if ((msg.getDstPort() == PORT_STATE) && (msg.getLength() == 4)) {
			final byte[] pl = msg.getPayload();

			// decode switch state and save pwm vals
			for (int i = 0; i < switchVals.length; i++) {
				boolean ob;
				//ignore lsb, it's not connected
				switchVals[i] = (pl[0] & (1 << (i+1))) == (1 << (i+1));
				if (switchVals[i] != lastSwitchVals[i]) {
					ob = lastSwitchVals[i];
					lastSwitchVals[i] = switchVals[i];
					notifyListeners(omap_switch.get((byte)i), switchVals[i], ob);
				}
			}

			// save pwm vals
			for (int i = 0; i < pwmVals.length; i++) {
				int oi;
				pwmVals[i] = (int) pl[i + 1] & 0xff;
				if (pwmVals[i] != lastPwmVals[i]) {
					oi = lastPwmVals[i];
					lastPwmVals[i] = pwmVals[i];
					notifyListeners(omap_pwm.get((byte)i), pwmVals[i], oi);
				}
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
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = SW_PRINTER_1;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(0x00, 0x00, msg);
	}

	public void switchBastelPrinter2(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = SW_PRINTER_2;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(0x00, 0x00, msg);
	}

	public void switchBastelHelmer1(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = SW_HELMER_1;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(0x00, 0x00, msg);
	}

	public void switchBastelHelmer2(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = SW_HELMER_2;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(0x00, 0x00, msg);
	}

	public void switchBastelBanner(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = SW_BANNER;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(0x00, 0x00, msg);
	}

	public void switchBastelOrgatable(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = SW_ORGATABLE;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(0x00, 0x00, msg);
	}

	public void switchBastelWindow(boolean state) {
		byte[] msg = MSG_SWITCH.clone();
		msg[1] = SW_WINDOW;
		msg[2] = (byte) (state ? 1 : 0);

		sendTo(0x00, 0x00, msg);
	}

	public void dimBastelBanner(int value) {
		byte[] msg = MSG_PWM.clone();
		msg[1] = PWM_BANNER;
		msg[2] = (byte) (value & 0xFF);

		sendTo(0x00, 0x00, msg);
	}

	public void dimBastelOrgatable(int value) {
		byte[] msg = MSG_PWM.clone();
		msg[1] = PWM_ORGATABLE;
		msg[2] = (byte) (value & 0xFF);

		sendTo(0x00, 0x00, msg);
	}

	public void dimBastelWindow(int value) {
		byte[] msg = MSG_PWM.clone();
		msg[1] = PWM_WINDOW;
		msg[2] = (byte) (value & 0xFF);

		sendTo(0x00, 0x00, msg);
	}

	public static void main(String[] args) {
		// create message bus
		MessageBus<LAPMessage> bus = new MessageBus<LAPMessage>();

		BastelControl bastelControl = new BastelControl(bus);

		// create gateway
		LAPTCPCanGateway gateway = LAPTCPCanGateway.makeGateway(bus, "10.0.1.2", 2342, true);
		if (gateway.up(10000, true)) {
			bastelControl.setListener(new LAPStateUpdateListener() {

				@Override
				public void onUpdate(int key, Object value, Object lastValue) {
					System.out.println(Objects.values()[key].toString() + ": " + value.toString());
				}
			});
			bastelControl.requestState();
			while (true)
				Thread.yield();
		} else
			System.out.println("failed to up gateway");
	}
}
