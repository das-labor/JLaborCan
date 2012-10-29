package de.hansinator.automation.lab;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Labor device address assignments, as in https://www.das-labor.org/wiki/Automatisierung_des_Labors
 * 
 * @author hansinator
 *
 */
public class LabAddressBook {
	public final static byte CAN_GATEWAY = 0x00;
	public final static byte POWERCOMMANDER = 0x02;
	public final static byte POWERCOMMANDER_TEST = 0x03;
	public final static byte HAUPTSCHALTER = 0x04;
	public final static byte POWERMETER_LAB = 0x05;
	public final static byte POWERMETER_AUSSEN = 0x06;
	public final static byte CANIR = 0x10;
	public final static byte KITCHENCAN = 0x23;
	public final static byte LSBORG = 0x24;
	public final static byte TREPPENBLINK = 0x25;
	public final static byte TOILETSTATUS = 0x2A;
	public final static byte MOODBAR = 0x31;
	public final static byte SPOTCONTROL = 0x35;
	public final static byte BORG3D = 0x3D;
	public final static byte LOUNGEBORG = 0x42;
	public final static byte BORGJACKET = 0x43;
	public final static byte MOODLIGHT = 0x51;
	public final static byte CANRFM12 = 0x53;
	public final static byte LOUNGEDIMMER_DOOR = 0x60;
	public final static byte LOUNGEDIMMER_WALL = 0x61;
	public final static byte BASTELCONTROL = (byte)0xA9;
	public final static byte ROLLOCONTROL = (byte)0xC0;
	public final static byte TOUCH_TEST = (byte)0xD0;
	public final static byte BROADCAST = (byte)0xFF;
	
	public final static ConcurrentHashMap<Byte, String> names;
	
	static {
		names = new ConcurrentHashMap<Byte, String>();
		names.put(CAN_GATEWAY, "CAN Gateway");
		names.put(POWERCOMMANDER, "PowerCommander");
		names.put(POWERCOMMANDER_TEST, "PowerCommander Testboard");
		names.put(HAUPTSCHALTER, "Hauptschalter");
		names.put(POWERMETER_LAB, "Powermeter LAB");
		names.put(POWERMETER_AUSSEN, "Powermeter Drehstromkiste");
		names.put(CANIR, "canir");
		names.put(KITCHENCAN, "KuechenCAN");
		names.put(LSBORG, "Laufschrift Borg");
		names.put(TREPPENBLINK, "Treppen Blink");
		names.put(TOILETSTATUS, "Toilettenstatus");
		names.put(MOODBAR, "Moodbar");
		names.put(SPOTCONTROL, "SpotControl (Metall-Gehäuse) ");
		names.put(BORG3D, "Borg 3D");
		names.put(LOUNGEBORG, "Lounge Borg");
		names.put(BORGJACKET, "Borgjacke");
		names.put(MOODLIGHT, "Moodlicht");
		names.put(CANRFM12, "CANRFM12");
		names.put(LOUNGEDIMMER_DOOR, "Lounge Dimmer Tür");
		names.put(LOUNGEDIMMER_WALL, "Lounge Dimmer Wand");
		names.put(BASTELCONTROL, "Bastelraum Control");
		names.put(ROLLOCONTROL, "Rollosteuergerät");
		names.put(TOUCH_TEST, "Touchpanel Testboard 1");
		names.put(BROADCAST, "Broadcast");
	}
}
