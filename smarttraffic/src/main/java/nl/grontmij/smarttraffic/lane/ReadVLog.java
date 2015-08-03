package nl.grontmij.smarttraffic.lane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import nl.grontmij.smarttraffic.lane.ConfigVri;
import nl.tudelft.simulation.language.io.URLResource;

public class ReadVLog {

	/*
	 * Functions to read (streaming) V-log files: concentrates on detector and
	 * traffic light information
	 */
	private ReadVLog() {
		// cannot be instantiated.
	}

	public static void main(String[] args) throws IOException {
		// lijst met de configuratie van de vri's: naam kruispunt, detector
		// (index en naam) en signaalgroep (index en naam)
		HashMap<String, ConfigVri> configVriList = new HashMap<String, ConfigVri>();

		String mapConfigVri = "configVRI/";
		System.out.println(parseLong(new StringBuffer("0201"), 4));
		String mapBase = "C:/Users/p070518/Documents/Grontmij/Projecten/OpdrachtenLopend/343090 Smart Traffic N201/VRI-loggings/";
		String wegNummer = "201";
		String[] vriNummer = { "225", "231", "234", "239", "245", "249", "291",
				"297", "302", "308", "311", "314" };

		// read VRI config files
		for (String vri : vriNummer) {
			String vriName = wegNummer + vri;
			String vriLocation = "VRI" + vriName;
			if (URLResource.getResource(mapBase + mapConfigVri + vriLocation
					+ ".cfg") != null) {
				URL url = URLResource.getResource(mapBase + mapConfigVri
						+ vriLocation + ".cfg");
				BufferedReader bufferedReader = null;
				String path = url.getPath();
				bufferedReader = new BufferedReader(new FileReader(path));
				configVriList.put(vriName, readVLogConfigFile(bufferedReader));
			}
		}

		String mapMonth = "juni/";
		String mapDay = "1";
		// start met inlezen files vanaf tijdstip ....
		int year = 2015;
		int month = 6;
		int day = 1;
		long hour = 0;
		int minute = 0;
		int second = 0;
		int tenth = 0;
		boolean readFirstTimeStamp = false;
		String timeStampFile = String.format("%04d%02d%02d_%02d%02d%02d", year,
				month, day, hour, minute, second);
		Instant timeStamp = Instant.parse(String.format(
				"%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day, hour,
				minute, second, tenth));
		String vriLocation = "VRI" + wegNummer + vriNummer[0];
		while (URLResource.getResource(mapBase + mapMonth + day + "/"
				+ vriLocation + "/" + vriLocation + "_" + timeStampFile
				+ ".vlg") != null) {
			URL url = URLResource.getResource(mapBase + mapMonth + day + "/"
					+ vriLocation + "/" + vriLocation + "_" + timeStampFile
					+ ".vlg");
			BufferedReader bufferedReader = null;
			String path = url.getPath();
			bufferedReader = new BufferedReader(new FileReader(path));
			readVLogFile(bufferedReader, readFirstTimeStamp);
			// increase time with one minute for next file
			timeStamp = timeStamp.plusSeconds(60);
			ZoneOffset offset = ZoneOffset.of("-00:00");
			LocalDateTime ldt = LocalDateTime.ofInstant(timeStamp, offset);
			day = ldt.getDayOfMonth();
			hour = ldt.getHour();
			minute = ldt.getMinute();
			second = ldt.getSecond();
			timeStampFile = String.format("%04d%02d%02d_%02d%02d%02d", year,
					month, day, hour, minute, second);
		}

	}

	private static ConfigVri readVLogConfigFile(BufferedReader bufferedReader)
			throws IOException {
		String line = "";
		String nameVRI = null;
		HashMap<Integer, String> detectors = new HashMap<Integer, String>();
		HashMap<Integer, String> signalGroups = new HashMap<Integer, String>();

		while ((line = bufferedReader.readLine()) != null) {
			StringBuffer buffer = new StringBuffer(line);
			// zoek //DP
			if (buffer.length() > 0) {
				// //SYS
				// SYS,"201225"
				if (buffer.length() >= 5) {
					if (buffer.substring(0, 5).contentEquals("//SYS")) {
						line = bufferedReader.readLine();
						String weg = line.substring(0, 3);
						String vri = line.substring(3, 3);
						nameVRI = weg + vri;
					}

				} else if (buffer.substring(0, 4).contentEquals("//DP")) {
					line = bufferedReader.readLine();
					while ((line = bufferedReader.readLine()) != null) {
						if (line.length() > 0) {
							if (line.substring(0, 2).contentEquals("DP"))
								readDetectorSettings(line, detectors);
						} else if (line.length() >= 2) {
							if (line.substring(0, 2).contentEquals("//")) {
								break;
							}
						}
					}
				} else if (buffer.substring(0, 4).contentEquals("//IS")) {
					// TODO: add IS info als nodig

				} else if (buffer.substring(0, 4).contentEquals("//FC")) {
					line = bufferedReader.readLine();
					while ((line = bufferedReader.readLine()) != null) {
						if (line.length() > 0) {
							if (line.substring(0, 2).contentEquals("FC"))
								readTrafficlightSettings(line, signalGroups);
						} else if (line.length() >= 2) {
							if (line.substring(0, 2).contentEquals("//")) {
								break;
							}
						}
					}
				}
			}
		}
		return new ConfigVri(nameVRI, detectors, signalGroups);
	}

	private static void readDetectorSettings(String line,
			HashMap<Integer, String> detectors) {
		String[] buffer = new String[4];
		buffer = line.split(",");
		int count = Integer.parseInt(buffer[1]);
		String name = buffer[2];
		long l = Long.parseLong(buffer[3]);
		String strHex = String.format("0x%04X", l);
		detectors.put(count, name);
		// if ()
	}

	private static void readTrafficlightSettings(String line,
			HashMap<Integer, String> signalGroups) {
		String[] buffer = new String[4];
		buffer = line.split(",");
		int count = Integer.parseInt(buffer[1]);
		String name = buffer[2];
		long l = Long.parseLong(buffer[3]);
		// String strHex = String.format("0x%04X", l);
		// if ()
		signalGroups.put(count, name);
	}

	public static void readVLogFile(BufferedReader bufferedReader,
			boolean readFirstTimeStamp) throws IOException {
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			StringBuffer buffer = new StringBuffer(line);
			int typeBericht = parseTypebericht(buffer, 2);
			if (typeBericht == 1) {
				System.out
						.println("status tijdsaanduiding resterende string = "
								+ line);
				parseTijd(buffer);
			} else if (typeBericht == 5) {
				System.out.println("status detectoren = " + line);
				parseStatus(buffer);
			} else if (typeBericht == 6) {
				System.out.println("wijziging detectoren = " + line);
				parseWijziging(buffer);
			} else if (typeBericht == 13) {
				System.out.println("status SignaalGroep = " + line);
				parseStatus(buffer);
			} else if (typeBericht == 14) {
				System.out.println("wijziging SignaalGroep = " + line);
				parseWijziging(buffer);
			}

		}

	}

	private static int parseTypebericht(final StringBuffer s, int aantal) {
		return parseByte(s);
	}

	private static HashMap<Integer, Integer> parseStatus(final StringBuffer s) {
		long deltaTijd = parseLong(s, 3);
		// lees reserve 4Bits
		parse4Bits(s);
		int aantal = parseByte(s);
		HashMap<Integer, Integer> mapDetectieStatus = new HashMap<Integer, Integer>();
		for (int i = 0; i < aantal; i++) {
			Integer detectorStatus = (int) parseLong(s, 3);
			mapDetectieStatus.put(i, detectorStatus);
		}
		return mapDetectieStatus;
	}

	private static HashMap<Integer, Integer> parseWijziging(final StringBuffer s) {
		long deltaTijd = parseLong(s, 3);
		int aantal = parse4Bits(s);
		HashMap<Integer, Integer> mapDetectieWijziging = new HashMap<Integer, Integer>();
		for (int i = 0; i < aantal; i++) {
			Integer detectorIndex = parseByte(s);
			Integer detectorStatus = parseByte(s);
			mapDetectieWijziging.put(detectorIndex, detectorStatus);
		}
		return mapDetectieWijziging;
	}

	private static Instant parseTijd(final StringBuffer s) {
		// System.out.println(type + "  " + s);
		int year = parseNibble(s) * 1000 + parseNibble(s) * 100
				+ parseNibble(s) * 10 + parseNibble(s);
		int month = parseNibble(s) * 10 + parseNibble(s);
		int day = parseNibble(s) * 10 + parseNibble(s);
		int hour = parseNibble(s) * 10 + parseNibble(s);
		int minute = parseNibble(s) * 10 + parseNibble(s);
		int second = parseNibble(s) * 10 + parseNibble(s);
		int tenth = parseNibble(s);
		Instant timeStamp = Instant.parse(String.format(
				"%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day, hour,
				minute, second, tenth));
		System.out.println(timeStamp);
		return timeStamp;
	}

	private static int parseNibble(final StringBuffer s) {
		byte b0 = parseChar(s.charAt(0));
		s.delete(0, 1);
		return b0;
	}

	private static long parseLong(final StringBuffer s, int count) {
		long result = 0;
		for (int i = 0; i < count; i++) {
			int b = parseChar(s.charAt(i));
			result += b * Math.pow(16, (count - i - 1));
		}
		s.delete(0, count);
		return result;
	}

	private static int parseByte(final StringBuffer s) {
		byte b0 = parseChar(s.charAt(0));
		byte b1 = parseChar(s.charAt(1));
		s.delete(0, 2);
		return (byte) ((b0 * 16) + b1);
	}

	private static int parse4Bits(final StringBuffer s) {
		byte b0 = parseChar(s.charAt(0));
		s.delete(0, 1);
		return b0;
	}

	private static byte parseChar(final char c) {
		if (c >= '0' && c <= '9') {
			return (byte) (c - '0');
		}
		if (c >= 'A' && c <= 'F') {
			return (byte) ((byte) (c - 'A') + 10);
		}
		throw new RuntimeException("parseChar: character not hex: " + c);
	}

	class DetectorType {
		public static final String DETECTIELUS = "0x0001";
		public static final String DRUKKNOP = "0x0002";
		public static final String KOPLUS = "0x0100";
		public static final String LANGELUS = "0x0200";
		public static final String VERWEGLUS = "0x0400";
		public static final String DLKOP = "0x0101";
		public static final String DLLNG = "0x0201";
		public static final String DLVER = "0x0401";
	}

	enum DetectorTypes {
		DETECTIELUS, DRUKKNOP, KOPLUS, LANGELUS, VERWEGLUS, DLKOPLUS, DLLNG, DLVER
	};

}
