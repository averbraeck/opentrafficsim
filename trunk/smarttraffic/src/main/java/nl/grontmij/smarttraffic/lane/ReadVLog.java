package nl.grontmij.smarttraffic.lane;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map.Entry;

import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

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
			String vriName = vri;
			String vriLocation = "VRI" + wegNummer + vriName;
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

	public static ConfigVri readVLogConfigFile(BufferedReader bufferedReader)
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
				if (buffer.length() >= 4) {
					if (buffer.substring(0, 4).contentEquals("//SY")) {
						line = bufferedReader.readLine();
						line = line.replaceAll("\"", "");
						String[] info = line.split(",");
						String weg = info[1].substring(0, 3);
						String vri = info[1].substring(3, 6);
						nameVRI = vri;
					}

					else if (buffer.substring(0, 4).contentEquals("//DP")) {
						while ((line = bufferedReader.readLine()) != null) {
							if (line.length() > 0) {
								if (line.substring(0, 2).contentEquals("DP")) {
									readDetectorSettings(line, detectors);
								} else if (line.length() >= 2) {
									if (line.substring(0, 2)
											.contentEquals("//")) {
										break;
									}
								}
							}
						}
					} else if (buffer.substring(0, 4).contentEquals("//IS")) {
						// TODO: add IS info als nodig

					} else if (buffer.substring(0, 4).contentEquals("//FC")) {
						while ((line = bufferedReader.readLine()) != null) {
							if (line.length() > 0) {
								if (line.substring(0, 2).contentEquals("FC")) {
									readTrafficlightSettings(line, signalGroups);
								} else if (line.length() >= 2) {
									if (line.substring(0, 2)
											.contentEquals("//")) {
										break;
									}
								}
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
			Instant timeVLogStart = timeVLog;
			Boolean[] boolReadyToStartVLog = new Boolean[] { new Boolean(false) };
			String vriLocation = "VRI" + wegNummer + vri;
			Instant timeFromVLog[] = new Instant[] { null };
			Long deltaTimeFromVLog[] = new Long[] { (long) 0 };
			String timeStampFile = String.format("%04d%02d%02d_%02d%02d%02d",
					year, month, day, hour, minute, second);
			// zoek de eerste "harde" tijdsaanduiding
			// Om alle dagen te simularen gebruik dan de volgende regel:
			// while (day < 27) {
			while (hour < 20) {
				String file = dirLoggings + Integer.toString(day) + "/"
						+ vriLocation + "/" + vriLocation + "_" + timeStampFile
						+ ".vlg";
				if (URLResource.getResource(file) != null) {
					URL url = URLResource.getResource(file);
					BufferedReader bufferedReader = null;
					String path = url.getPath();
					try {
						bufferedReader = new BufferedReader(
								new FileReader(path));
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// in de vlog bestanden wordt om de x minuten een regel met
					// de "harde" tijd gelogd
					// vervolgens worden meldingen gedaan met een delta_tijd
					// vanaf die "harde" tijd
					// in de volgende module wordt eerst een harde starttijd
					// gezocht en worden vervolgens de initiele waarden van de
					// detectoren en van de signaalgroepen ingelezen
					if (!boolReadyToStartVLog[0]) {
						readStatusVLogFile(mapSensor, bufferedReader,
								timeFromVLog, deltaTimeFromVLog,
								boolReadyToStartVLog, configVriList, vri);

					}
					// als er eenmaal een referentie naar de tijd is gevonden
					// kan vervolgens verder worden gelezen met alleen de
					// wijzigingen
					// de regels met de statusberichten kunnen wordn
					// overgeslagen
					if (boolReadyToStartVLog[0]) {
						readVLogFile(mapSensor, bufferedReader, timeFromVLog,
								deltaTimeFromVLog, configVriList, vri);
					}
				}
				// increase time with one minute for next file
				//
				timeVLogStart = timeVLogStart.plusSeconds(60);
				ldt = LocalDateTime.ofInstant(timeVLogStart, offset);
				day = ldt.getDayOfMonth();
				hour = ldt.getHour();
				minute = ldt.getMinute();
				second = ldt.getSecond();
				timeStampFile = String.format("%04d%02d%02d_%02d%02d%02d",
						year, month, day, hour, minute, second);
			}
		}
	}

	// start met het inlezen van de VLOG data
	// daarbij worden zowel pulsen van detectoren en signaalgroepen ingelezen
	public static void readStatusVLogFile(
			HashMap<String, SensorLaneST> mapSensor,
			BufferedReader bufferedReader, Instant[] timeFromVLog,
			Long[] deltaTimeFromVLog, Boolean[] boolReadyToStartVLog,
			HashMap<String, ConfigVri> vriList, String vriName)
			throws IOException {
		boolean boolReadFirstTimeStamp = false;
		boolean boolReadFirstDetectorStatus = false;
		boolean boolReadFirstSignalGroupStatus = false;

		HashMap<Integer, Integer> mapStatus;
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			StringBuffer buffer = new StringBuffer(line);
			int typeBericht = parseTypebericht(buffer, 2);
			// type "1": de harde tijd
			if (typeBericht == 1) {
				timeFromVLog[0] = parseTijd(buffer);
				boolReadFirstTimeStamp = true;
			} else if (typeBericht == 5) {
				// status detectoren (alle detectoren)
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
				ReadStatusDetector(mapSensor, mapStatus, vriList, vriName,
						timeFromVLog, deltaTimeFromVLog);
				boolReadFirstDetectorStatus = true;
			} else if (typeBericht == 13) {
				// status signaalgroepen (alle SG's)
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
				ReadStatusSignalGroup(mapSensor, mapStatus, vriList, vriName,
						timeFromVLog, deltaTimeFromVLog);
				boolReadFirstSignalGroupStatus = true;
			}
			// als alle initiele gegevens beschikbaar zijn, gaan we naar de
			// volgende module, die alleen de wijzigingsberichten verwerkt
			if (boolReadFirstTimeStamp && boolReadFirstDetectorStatus
					&& boolReadFirstSignalGroupStatus) {
				boolReadyToStartVLog[0] = true;
				break;
			}
		}

	}

	// alleen de wijzigingsberichten lezen
	public static void readVLogFile(HashMap<String, SensorLaneST> mapSensor,
			BufferedReader bufferedReader, Instant[] timeFromVLog,
			Long[] deltaTimeFromVLog, HashMap<String, ConfigVri> vriList,
			String vriName) throws IOException {
		HashMap<Integer, Integer> mapStatus;
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			StringBuffer buffer = new StringBuffer(line);
			int typeBericht = parseTypebericht(buffer, 2);
			if (typeBericht == 1) {
				timeFromVLog[0] = parseTijd(buffer);
			} else if (typeBericht == 5) {
				// alleen om te checken of de status nog klopt
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
				CheckStatusDetector(mapSensor, mapStatus, vriList, vriName);
			} else if (typeBericht == 6) {
				// wijzigingsberichten inlezen van de detectoren
				mapStatus = parseWijziging(buffer, deltaTimeFromVLog);
				ReadStatusDetector(mapSensor, mapStatus, vriList, vriName,
						timeFromVLog, deltaTimeFromVLog);
			} else if (typeBericht == 13) {
				// alleen om te checken of de status nog klopt
				mapStatus = parseStatus(buffer, deltaTimeFromVLog);
				CheckStatusSignalGroup(mapSensor, mapStatus, vriList, vriName);
			} else if (typeBericht == 14) {
				// wijzigingsberichten inlezen van de signaalgroepen
				mapStatus = parseWijziging(buffer, deltaTimeFromVLog);
				ReadStatusSignalGroup(mapSensor, mapStatus, vriList, vriName,
						timeFromVLog, deltaTimeFromVLog);
			}
		}
	}

	// leest de status van de detectoren (1 regel uit een VLog bestand)
	public static void ReadStatusDetector(
			HashMap<String, SensorLaneST> mapSensor,
			HashMap<Integer, Integer> mapStatus,
			HashMap<String, ConfigVri> vriList, String vriName,
			Instant[] timeFromVLog, Long[] deltaTimeFromVLog) {
		// in de mapStatus staan de detector indices met de detectorwaarden
		for (Entry<Integer, Integer> entry : mapStatus.entrySet()) {
			ConfigVri vri = vriList.get(vriName);
			// zoek de naam van de detector uit de array
			String nameDetector = vri.getDetectors().get(entry.getKey());
			// koplus heeft een prefix "K": die halen we eruit
			if (nameDetector.startsWith("K")) {
				nameDetector = nameDetector.substring(1);
			}
			// het tijdstip
			Instant timeVLogNow = timeFromVLog[0]
					.plusMillis(100 * deltaTimeFromVLog[0]);
			// tijd (in miliseconden) sinds de start van de simulatie
			Long milliSecondsPassed = ChronoUnit.MILLIS.between(
					GTM.startTimeSimulation, timeVLogNow);
			// zoek de bijbehorende detector
			String searchFor = vriName + "_" + nameDetector;
			if (mapSensor.get(searchFor) != null) {
				mapSensor.get(searchFor).addStatusByTime(
						new DoubleScalar.Abs<TimeUnit>(milliSecondsPassed,
								TimeUnit.MILLISECOND), entry.getValue());
			} else {
				System.out.println("Sensor not in network");
			}
		}
	}

	// lees de status van de signaalgroepen
	public static void ReadStatusSignalGroup(
			HashMap<String, SensorLaneST> mapSensor,
			HashMap<Integer, Integer> mapStatus,
			HashMap<String, ConfigVri> vriList, String vriName,
			Instant[] timeFromVLog, Long[] deltaTimeFromVLog) {
		// zie ook vorige methode (zelfde aanpak)
		for (Entry<Integer, Integer> entry : mapStatus.entrySet()) {
			ConfigVri vri = vriList.get(vriName);
			String nameSignalGroup = vri.getSignalGroups().get(entry.getKey());
			Instant timeVLogNow = timeFromVLog[0]
					.plusMillis(100 * deltaTimeFromVLog[0]);
			Long milliSecondsPassed = ChronoUnit.MILLIS.between(
					GTM.startTimeSimulation, timeVLogNow);
			// in de GTM.mapLaneToStopLineLane wordt de relatie tussen de
			// naam van de signaalgroep en de stopstreep
			for (Entry stopLine : GTM.mapSignalGroupToStopLineAtJunction
					.entrySet()) {
				// in navolgende naam staat de naam van het kruispunt, de
				// signaalgroep en de weg
				String name = (String) stopLine.getKey();
				String splitted[] = name.split("_");
				if (splitted[0].contentEquals(vriName)
						&& splitted[2].contentEquals(nameSignalGroup)) {
					// koppel de waarde van de signaalgroep aan de stopstreep en
					// koppel het tjdstip
					StopLineLane stopLineFound = (StopLineLane) stopLine
							.getValue();
					stopLineFound.addMapStopTrafficState(
							new DoubleScalar.Abs<TimeUnit>(milliSecondsPassed,
									TimeUnit.MILLISECOND), entry.getValue());
				}
			}

		}
	}

	// HIERONDER volgen een aantal methoden om de regels van de VLog bestanden
	// in te lezen

	// het type bericht (letter 1 en 2)
	public static int parseTypebericht(final StringBuffer s, int aantal) {
		return parseByte(s);
	}

	// de tijd
	public static Instant parseTijd(final StringBuffer s) {
		// System.out.println(type + "  " + s);
		int year = parseNibble(s) * 1000 + parseNibble(s) * 100
				+ parseNibble(s) * 10 + parseNibble(s);
		int month = parseNibble(s) * 10 + parseNibble(s);
		int day = parseNibble(s) * 10 + parseNibble(s);
		int hour = parseNibble(s) * 10 + parseNibble(s);
		//LET OP: LIJKT DAT ER EEN FOUT ZIT IN VLOG
		hour -= 2;
		int minute = parseNibble(s) * 10 + parseNibble(s);
		int second = parseNibble(s) * 10 + parseNibble(s);
		int tenth = parseNibble(s);
		Instant timeStamp = Instant.parse(String.format(
				"%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day, hour,
				minute, second, tenth));
		System.out.println(timeStamp);
		return timeStamp;
	}

	// lees bij een statusbericht de waarden van de detectoren/signaalgroepen
	public static HashMap<Integer, Integer> parseStatus(final StringBuffer s,
			Long[] deltaTimeFromVLog) {
		deltaTimeFromVLog[0] = parseLong(s, 3);
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

	// lees bij een wijzigigingsbericht de gewijzigde waarden van de
	// detectoren/signaalgroepen
	public static HashMap<Integer, Integer> parseWijziging(
			final StringBuffer s, Long[] deltaTimeFromVLog) {
		deltaTimeFromVLog[0] = parseLong(s, 3);
		int aantal = parse4Bits(s);
		HashMap<Integer, Integer> mapDetectieWijziging = new HashMap<Integer, Integer>();
		for (int i = 0; i < aantal; i++) {
			Integer detectorIndex = parseByte(s);
			Integer detectorStatus = parseByte(s);
			mapDetectieWijziging.put(detectorIndex, detectorStatus);
		}
		return mapDetectieWijziging;
	}

	// lees bij een statusbericht de waarden van de detectoren/signaalgroepen
	public static void readDetectorSettings(String line,
			HashMap<Integer, String> detectors) {
		String[] buffer = new String[4];
		buffer = line.split(",");
		int count = Integer.parseInt(buffer[1]);
		String name = buffer[2].replaceAll("\"", "");
		String part1;
		String part2 = name.substring(2, 3);
		if (name.startsWith("0")) {
			part1 = name.substring(1, 2);
		} else {
			part1 = name.substring(0, 2);
		}
		name = part1 + "." + part2;
		long l = Long.parseLong(buffer[3]);
		String strHex = String.format("0x%04X", l);
		if (strHex.substring(3, 4).contentEquals("1")
				&& (strHex.substring(5, 6).contentEquals("1") || strHex
						.substring(5, 6).contentEquals("4"))) {
			name = "K" + name;
		}
		detectors.put(count, name);
		// if ()
	}

	// lees bij een wijzigigingsbericht de gewijzigde waarden van de
	// detectoren/signaalgroepen
	public static void readTrafficlightSettings(String line,
			HashMap<Integer, String> signalGroups) {
		String[] buffer = new String[4];
		buffer = line.split(",");
		int count = Integer.parseInt(buffer[1]);
		String name = buffer[2].replaceAll("\"", "");
		long l = Long.parseLong(buffer[3]);
		// String strHex = String.format("0x%04X", l);
		// if ()
		signalGroups.put(count, name);
	}

	// toetsen of de dynamisch bepaalde situatie met detectoren overeenkomt met
	// een statusbericht
	public static void CheckStatusDetector(
			HashMap<String, SensorLaneST> mapSensor,
			HashMap<Integer, Integer> mapStatus,
			HashMap<String, ConfigVri> vriList, String vriName) {
		for (Entry<Integer, Integer> entry : mapStatus.entrySet()) {
			ConfigVri vri = vriList.get(vriName);
			String nameDetector = vri.getDetectors().get(entry.getKey());
			if (mapSensor.get(vriName + nameDetector) != null) {
				HashMap<DoubleScalar.Abs<TimeUnit>, Integer> map = mapSensor
						.get(vriName + nameDetector).getStatusByTime();
				// compare value of latest change and this status
				Entry<DoubleScalar.Abs<TimeUnit>, Integer> maxEntry = null;
				for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entry1 : map
						.entrySet()) {
					if (maxEntry == null
							|| entry1.getKey().getSI() > maxEntry.getValue()) {
						maxEntry = entry1;
					}
				}
				if (entry.getValue() != maxEntry.getValue()) {
					System.out
							.println("Status detector verkeerd ingelezen!!!!!");
				}
			}

		}
	}

	public static void CheckStatusSignalGroup(
			HashMap<String, SensorLaneST> mapSensor,
			HashMap<Integer, Integer> mapStatus,
			HashMap<String, ConfigVri> vriList, String vriName) {
		for (Entry<Integer, Integer> entry : mapStatus.entrySet()) {
			ConfigVri vri = vriList.get(vriName);
			String nameSignalGroup = vri.getSignalGroups().get(entry.getKey());
			if (mapSensor.get(vriName + nameSignalGroup) != null) {
				HashMap<DoubleScalar.Abs<TimeUnit>, Integer> map = mapSensor
						.get(vriName + nameSignalGroup).getStatusByTime();
				// compare value of latest change and this status
				Entry<DoubleScalar.Abs<TimeUnit>, Integer> maxEntry = null;
				for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entry1 : map
						.entrySet()) {
					if (maxEntry == null
							|| entry1.getKey().getSI() > maxEntry.getValue()) {
						maxEntry = entry1;
					}
				}
				if (entry.getValue() != maxEntry.getValue()) {
					System.out
							.println("Status signaalgroep verkeerd ingelezen!!!!!");
				}
			}
		}
	}

	public static int parseNibble(final StringBuffer s) {
		if (s.length()==0) {
			System.out.println();
		}
		byte b0 = parseChar(s.charAt(0));
		s.delete(0, 1);
		return b0;
	}

	public static long parseLong(final StringBuffer s, int count) {
		long result = 0;
		for (int i = 0; i < count; i++) {
			int b = parseChar(s.charAt(i));
			result += b * Math.pow(16, (count - i - 1));
		}
		s.delete(0, count);
		return result;
	}

	public static int parseByte(final StringBuffer s) {
		byte b0 = parseChar(s.charAt(0));
		byte b1 = parseChar(s.charAt(1));
		s.delete(0, 2);
		return (byte) ((b0 * 16) + b1);
	}

	public static int parse4Bits(final StringBuffer s) {
		byte b0 = parseChar(s.charAt(0));
		s.delete(0, 1);
		return b0;
	}

	public static byte parseChar(final char c) {
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
