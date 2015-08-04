package nl.grontmij.smarttraffic.lane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map.Entry;

import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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

	public static HashMap<String, ConfigVri> readVlogConfigFiles(
			String dirConfigVri, String dirBase, String wegNummer,
			String[] vriNummer) throws IOException {
		// lijst met de configuratie van de vri's: naam kruispunt, detector
		// (index en naam) en signaalgroep (index en naam)
		HashMap<String, ConfigVri> configVriList = new HashMap<String, ConfigVri>();
		// read VRI config files
		for (String vri : vriNummer) {
			String vriName = wegNummer + vri;
			String vriLocation = "VRI" + vriName;
			if (URLResource.getResource(dirBase + dirConfigVri + vriLocation
					+ ".cfg") != null) {
				URL url = URLResource.getResource(dirBase + dirConfigVri
						+ vriLocation + ".cfg");
				BufferedReader bufferedReader = null;
				String path = url.getPath();
				bufferedReader = new BufferedReader(new FileReader(path));
				configVriList.put(vriName, readVLogConfigFile(bufferedReader));
			}
		}
		return configVriList;
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

	public static void readVlogFiles(HashMap<String, SensorLaneST> mapSensor,
			HashMap<String, ConfigVri> configVriList, Instant timeVLog,
			String dirLoggings, String wegNummer, String[] vriNummer)
			throws IOException {
		ZoneOffset offset = ZoneOffset.of("-00:00");
		LocalDateTime ldt = LocalDateTime.ofInstant(timeVLog, offset);
		ldt = LocalDateTime.ofInstant(timeVLog, offset);
		int year = ldt.getYear();
		int month = ldt.getMonthValue();
		int day = ldt.getDayOfMonth();
		int hour = ldt.getHour();
		int minute = ldt.getMinute();
		int second = ldt.getSecond();
		for (String vri : vriNummer) {
			boolean boolReadyToStartVLog = false;
			boolean boolReadFirstTimeStamp = false;
			boolean boolReadFirstDetectorStatus = false;
			boolean boolReadFirstSignalGroupStatus = false;
			String vriLocation = "VRI" + wegNummer + vri;
			String dayString = null;
			Instant timeFromVLog = null;
			long deltaTimeFromVLog = 0;
			String timeStampFile = String.format("%04d%02d%02d_%02d%02d%02d",
					year, month, day, hour, minute, second);
			// zoek de eerste "harde" tijdsaanduiding
			while (URLResource.getResource(dirLoggings + Integer.toString(day)
					+ "/" + vriLocation + "/" + vriLocation + "_"
					+ timeStampFile + ".vlg") != null) {
				URL url = URLResource.getResource(dirLoggings + dayString + "/"
						+ vriLocation + "/" + vriLocation + "_" + timeStampFile
						+ ".vlg");
				BufferedReader bufferedReader = null;
				String path = url.getPath();
				bufferedReader = new BufferedReader(new FileReader(path));
				if (!boolReadyToStartVLog) {
					readStatusVLogFile(mapSensor, bufferedReader, timeFromVLog,
							deltaTimeFromVLog, boolReadFirstTimeStamp,
							boolReadFirstDetectorStatus,
							boolReadFirstSignalGroupStatus,
							boolReadyToStartVLog, configVriList, wegNummer
									+ vri);
				}
				if (boolReadyToStartVLog) {
					readVLogFile(mapSensor, bufferedReader, timeFromVLog,
							deltaTimeFromVLog, configVriList, wegNummer + vri);
				}
				// increase time with one minute for next file
				timeVLog = timeVLog.plusSeconds(60);
				ldt = LocalDateTime.ofInstant(timeVLog, offset);
				day = ldt.getDayOfMonth();
				hour = ldt.getHour();
				minute = ldt.getMinute();
				second = ldt.getSecond();
				timeStampFile = String.format("%04d%02d%02d_%02d%02d%02d",
						year, month, day, hour, minute, second);
			}
		}
	}

	public static void readStatusVLogFile(
			HashMap<String, SensorLaneST> mapSensor,
			BufferedReader bufferedReader, Instant timeFromVLog,
			long deltaTimeFromVLog, boolean boolReadFirstTimeStamp,
			boolean boolReadFirstDetectorStatus,
			boolean boolReadFirstSignalGroupStatus,
			boolean boolReadyToStartVLog, HashMap<String, ConfigVri> vriList,
			String vriName) throws IOException {
		HashMap<Integer, Integer> mapStatus;
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			StringBuffer buffer = new StringBuffer(line);
			int typeBericht = parseTypebericht(buffer, 2);
			if (typeBericht == 1) {
				timeFromVLog = parseTijd(buffer);
				boolReadFirstTimeStamp = true;
			} else if (typeBericht == 5) {
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
				for (Entry<Integer, Integer> entry : mapStatus.entrySet()) {
					ConfigVri vri = vriList.get(vriName);
					String nameDetector = vri.getDetectors()
							.get(entry.getKey());
					Instant timeVLogNow = timeFromVLog
							.plusMillis(100*deltaTimeFromVLog);
					Long milliSecondsPassed = ChronoUnit.MILLIS.between(GTM.startTimeSimulation, timeVLogNow);
					mapSensor.get(vriName + nameDetector).addStatusByTime(
							new DoubleScalar.Rel<TimeUnit>(milliSecondsPassed,
									TimeUnit.MILLISECOND), entry.getValue());
				}
				boolReadFirstDetectorStatus = true;
			} else if (typeBericht == 13) {
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
				boolReadFirstSignalGroupStatus = true;
			}
			if (boolReadFirstTimeStamp && boolReadFirstDetectorStatus
					&& boolReadFirstSignalGroupStatus) {
				boolReadyToStartVLog = true;
				break;
			}
		}

	}

	public static void readVLogFile(HashMap<String, SensorLaneST> mapSensor,
			BufferedReader bufferedReader, Instant timeFromVLog,
			long deltaTimeFromVLog, HashMap<String, ConfigVri> vriList,
			String vriName) throws IOException {
		HashMap<Integer, Integer> mapStatus;
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			StringBuffer buffer = new StringBuffer(line);
			int typeBericht = parseTypebericht(buffer, 2);
			if (typeBericht == 1) {
				timeFromVLog = parseTijd(buffer);
			} else if (typeBericht == 5) {
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
			} else if (typeBericht == 6) {
				mapStatus = parseWijziging(buffer, deltaTimeFromVLog);
			} else if (typeBericht == 13) {
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
			} else if (typeBericht == 14) {
				mapStatus = parseWijziging(buffer, deltaTimeFromVLog);
			}

		}

	}

	private static int parseTypebericht(final StringBuffer s, int aantal) {
		return parseByte(s);
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

	private static HashMap<Integer, Integer> parseStatus(final StringBuffer s,
			Long deltaTimeFromVLog) {
		deltaTimeFromVLog = parseLong(s, 3);
		// lees reserve 4Bits
		parse4Bits(s);
		int aantal = parseByte(s);
		HashMap<Integer, Integer> mapDetectieStatus = new HashMap<Integer, Integer>();
		for (int i = 0; i < aantal; i++) {
			Integer detectorStatus = (int) parseLong(s, 1);
			mapDetectieStatus.put(i, detectorStatus);
		}
		return mapDetectieStatus;
	}

	private static HashMap<Integer, Integer> parseWijziging(
			final StringBuffer s, Long deltaTimeFromVLog) {
		deltaTimeFromVLog = parseLong(s, 3);
		int aantal = parse4Bits(s);
		HashMap<Integer, Integer> mapDetectieWijziging = new HashMap<Integer, Integer>();
		for (int i = 0; i < aantal; i++) {
			Integer detectorIndex = parseByte(s);
			Integer detectorStatus = parseByte(s);
			mapDetectieWijziging.put(detectorIndex, detectorStatus);
		}
		return mapDetectieWijziging;
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
