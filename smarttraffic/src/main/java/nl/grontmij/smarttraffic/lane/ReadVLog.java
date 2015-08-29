package nl.grontmij.smarttraffic.lane;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.lane.AbstractSensor;

/** Read vlog file. */
public class ReadVLog
{
    /** errors for sensors not existing. */
    private static Set<String> sensorNotFound = new HashSet<>();

    /** names of sensors existing. */
    private static Set<String> sensorFound = new HashSet<>();

    /**
     * Functions to read (streaming) V-log files: concentrates on detector and traffic light information
     */
    private ReadVLog()
    {
        // cannot be instantiated.
    }

    public static void
        readVlogZipFiles(HashMap<String, AbstractSensor> mapSensor, HashMap<String, ConfigVri> configVriList,
            Instant timeVLog, String dirLoggings, String vLogFileName, String wegNummer, String[] vriNummer,
            OTSDEVSSimulatorInterface simulator, Integer startAtHour, Integer stopAtHour,
            BufferedWriter outputFileLogReadSensor)
    {
        try
        {
            Map<String, ZipEntry> zipEntries = new HashMap<>();
            ZipFile zipFile = new ZipFile(dirLoggings + vLogFileName);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                zipEntries.put(entry.getName(), entry);
            }

            for (String vri : vriNummer)
            {
                ZoneOffset offset = ZoneOffset.of("-00:00");
                LocalDateTime ldt = LocalDateTime.ofInstant(timeVLog, offset);
                ldt = LocalDateTime.ofInstant(timeVLog, offset);
                int year = ldt.getYear();
                int month = ldt.getMonthValue();
                int day = ldt.getDayOfMonth();
                int hour = ldt.getHour();
                int minute = ldt.getMinute();
                int second = ldt.getSecond();
                Instant timeVLogStart = timeVLog;
                while (day <= Settings.getInt(simulator, "RUNDAYS"))
                {
                    Boolean[] boolReadyToStartVLog = new Boolean[]{new Boolean(false)};
                    String vriLocation = "VRI" + wegNummer + vri;
                    Instant timeFromVLog[] = new Instant[]{null};
                    Long deltaTimeFromVLog[] = new Long[]{(long) 0};
                    String timeStampFile =
                        String.format("%04d%02d%02d_%02d%02d%02d", year, month, day, hour, minute, second);
                    // zoek de eerste "harde" tijdsaanduiding
                    // Om alle dagen te simuleren gebruik dan de volgende regel:
                    // System.out.println(vri + " - " + timeStampFile);
                    while (hour < stopAtHour)
                    {
                        String fName =
                            Integer.toString(day) + "/" + vriLocation + "/" + vriLocation + "_" + timeStampFile + ".vlg";
                        ZipEntry entry = zipEntries.get(fName);
                        if (entry != null)
                        {
                            InputStream stream = zipFile.getInputStream(entry);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                            /*
                             * in de vlog bestanden wordt om de x minuten een regel met de "harde" tijd gelogd vervolgens worden
                             * meldingen gedaan met een delta_tijd vanaf die "harde" tijd in de volgende module wordt eerst een
                             * harde starttijd gezocht en worden vervolgens de initiele waarden van de detectoren en van de
                             * signaalgroepen ingelezen
                             */

                            if (!boolReadyToStartVLog[0])
                            {
                                readStatusVLogFile(mapSensor, bufferedReader, timeFromVLog, deltaTimeFromVLog,
                                    boolReadyToStartVLog, configVriList, vri, simulator, outputFileLogReadSensor);
                            }
                            /*
                             * als er eenmaal een referentie naar de tijd is gevonden kan vervolgens verder worden gelezen met
                             * alleen de wijzigingen de regels met de statusberichten kunnen worden overgeslagen
                             */

                            if (boolReadyToStartVLog[0])
                            {
                                readVLogFile(mapSensor, bufferedReader, timeFromVLog, deltaTimeFromVLog, configVriList, vri,
                                    simulator, outputFileLogReadSensor);
                            }
                            bufferedReader.close();
                            stream.close();
                        }

                        // increase time with one minute for next VLOG file (which goes by minute)
                        //

                        timeVLogStart = timeVLogStart.plusSeconds(60);
                        ldt = LocalDateTime.ofInstant(timeVLogStart, offset);
                        day = ldt.getDayOfMonth();
                        hour = ldt.getHour();
                        minute = ldt.getMinute();
                        second = ldt.getSecond();
                        timeStampFile = String.format("%04d%02d%02d_%02d%02d%02d", year, month, day, hour, minute, second);
                    }
                    day++;
                    hour = startAtHour;
                    minute = 0;
                    second = 0;
                }
            }
            zipFile.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            System.exit(-1);
        }
    }

    // start met het inlezen van de VLOG data
    // daarbij worden zowel pulsen van detectoren en signaalgroepen ingelezen
    public static void readStatusVLogFile(HashMap<String, AbstractSensor> mapSensor, BufferedReader bufferedReader,
        Instant[] timeFromVLog, Long[] deltaTimeFromVLog, Boolean[] boolReadyToStartVLog,
        HashMap<String, ConfigVri> vriList, String vriName, OTSDEVSSimulatorInterface simulator,
        BufferedWriter outputFileLogReadSensor) throws IOException
    {
        boolean boolReadFirstTimeStamp = false;
        boolean boolReadFirstDetectorStatus = false;
        boolean boolReadFirstSignalGroupStatus = false;

        HashMap<Integer, Integer> mapStatus;
        String line = "";
        while ((line = bufferedReader.readLine()) != null)
        {
            try
            {
                StringBuffer buffer = new StringBuffer(line);
                int typeBericht = parseTypebericht(buffer, 2);
                // type "1": de harde tijd
                if (typeBericht == 1)
                {
                    timeFromVLog[0] = parseTijd(buffer);
                    // System.out.println(timeFromVLog[0]);
                    boolReadFirstTimeStamp = true;
                }
                else if (typeBericht == 5)
                {
                    // status detectoren (alle detectoren)
                    mapStatus = parseStatus(buffer, deltaTimeFromVLog);
                    ReadStatusDetector(mapSensor, mapStatus, vriList, vriName, timeFromVLog, deltaTimeFromVLog,
                        outputFileLogReadSensor);
                    boolReadFirstDetectorStatus = true;
                }
                else if (typeBericht == 13)
                {
                    // status signaalgroepen (alle SG's)
                    mapStatus = parseStatus(buffer, deltaTimeFromVLog);
                    ReadStatusSignalGroup(mapSensor, mapStatus, vriList, vriName, timeFromVLog, deltaTimeFromVLog, simulator);
                    boolReadFirstSignalGroupStatus = true;
                }
                // als alle initiele gegevens beschikbaar zijn, gaan we naar de
                // volgende module, die alleen de wijzigingsberichten verwerkt
                if (boolReadFirstTimeStamp && boolReadFirstDetectorStatus && boolReadFirstSignalGroupStatus)
                {
                    boolReadyToStartVLog[0] = true;
                    break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // alleen de wijzigingsberichten lezen
    public static void readVLogFile(HashMap<String, AbstractSensor> mapSensor, BufferedReader bufferedReader,
        Instant[] timeFromVLog, Long[] deltaTimeFromVLog, HashMap<String, ConfigVri> vriList, String vriName,
        OTSDEVSSimulatorInterface simulator, BufferedWriter outputFileLogReadSensor) throws IOException
    {
        HashMap<Integer, Integer> mapStatus;
        String line = "";
        while ((line = bufferedReader.readLine()) != null)
        {
            try
            {
                StringBuffer buffer = new StringBuffer(line);
                int typeBericht = parseTypebericht(buffer, 2);
                if (typeBericht == 1)
                {
                    timeFromVLog[0] = parseTijd(buffer);
                    // System.out.println(timeFromVLog[0]);
                }
                else if (typeBericht == 5)
                {
                    // alleen om te checken of de status nog klopt
                    mapStatus = parseStatus(buffer, deltaTimeFromVLog);
                    CheckStatusDetector(mapSensor, mapStatus, vriList, vriName);
                }
                else if (typeBericht == 6)
                {
                    // wijzigingsberichten inlezen van de detectoren
                    mapStatus = parseWijziging(buffer, deltaTimeFromVLog);
                    ReadStatusDetector(mapSensor, mapStatus, vriList, vriName, timeFromVLog, deltaTimeFromVLog,
                        outputFileLogReadSensor);
                }
                else if (typeBericht == 13)
                {
                    // alleen om te checken of de status nog klopt
                    mapStatus = parseStatus(buffer, deltaTimeFromVLog);
                    CheckStatusSignalGroup(mapSensor, mapStatus, vriList, vriName);
                }
                else if (typeBericht == 14)
                {
                    // wijzigingsberichten inlezen van de signaalgroepen
                    mapStatus = parseWijziging(buffer, deltaTimeFromVLog);
                    ReadStatusSignalGroup(mapSensor, mapStatus, vriList, vriName, timeFromVLog, deltaTimeFromVLog, simulator);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // leest de status van de detectoren (1 regel uit een VLog bestand)
    public static void ReadStatusDetector(HashMap<String, AbstractSensor> mapSensor, HashMap<Integer, Integer> mapStatus,
        HashMap<String, ConfigVri> vriList, String vriName, Instant[] timeFromVLog, Long[] deltaTimeFromVLog,
        BufferedWriter outputFileLogReadSensor)
    {
        // in de mapStatus staan de detector indices met de detectorwaarden
        for (Entry<Integer, Integer> entry : mapStatus.entrySet())
        {
            ConfigVri vri = vriList.get(vriName);
            // zoek de naam van de detector uit de array
            String nameDetector = vri.getDetectors().get(entry.getKey());
            // koplus heeft een prefix "K": die halen we eruit
            if (nameDetector.startsWith("K") || nameDetector.startsWith("k"))
            {
                nameDetector = nameDetector.substring(1);
            }
            if (nameDetector.matches("\\d\\.\\d") || nameDetector.matches("\\d\\.\\d\\d"))
            {
                nameDetector = "0" + nameDetector;
            }
            // het tijdstip
            Instant timeVLogNow = timeFromVLog[0].plusMillis(100 * deltaTimeFromVLog[0]);
            // tijd (in miliseconden) sinds de start van de simulatie
            Long milliSecondsPassed = ChronoUnit.MILLIS.between(GTM.startTimeSimulation, timeVLogNow);
            // zoek de bijbehorende detector
            String searchFor = vriName + "_" + nameDetector;
            AbstractSensor sensor = mapSensor.get(searchFor);
            if (sensor != null)
            {
                if (sensor instanceof CheckSensor)
                {
                    if (!sensorFound.contains(searchFor))
                    {
                        sensorFound.add(searchFor);
                        // System.out.println("Sensor found: " + searchFor + " @ "
                        // + timeVLogNow.toString());
                        try
                        {
                            outputFileLogReadSensor.write("Sensor found: " + searchFor + " @ " + timeVLogNow.toString()
                                + "\n");
                        }
                        catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    ((CheckSensor) sensor).addStatusByTime(new DoubleScalar.Abs<TimeUnit>(milliSecondsPassed,
                        TimeUnit.MILLISECOND), entry.getValue());
                }
                else if (sensor instanceof GenerateSensor)
                {
                    ((GenerateSensor) sensor).addStatusByTime(new DoubleScalar.Abs<TimeUnit>(milliSecondsPassed,
                        TimeUnit.MILLISECOND), entry.getValue());
                }
                else
                {
                    // System.out.println("Sensor " + searchFor
                    // + " triggered -- ignored for now");
                    try
                    {
                        outputFileLogReadSensor.write("Sensor " + searchFor + " triggered -- ignored for now \n");
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
            else
            {
                if (!sensorNotFound.contains(searchFor))
                {
                    sensorNotFound.add(searchFor);
                    // System.out.println("Sensor " + searchFor
                    // + " not in network");
                    try
                    {
                        outputFileLogReadSensor.write("Sensor " + searchFor + " not in network \n");
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // lees de status van de signaalgroepen
    public static void ReadStatusSignalGroup(HashMap<String, AbstractSensor> mapSensor, HashMap<Integer, Integer> mapStatus,
        HashMap<String, ConfigVri> vriList, String vriName, Instant[] timeFromVLog, Long[] deltaTimeFromVLog,
        OTSDEVSSimulatorInterface simulator)
    {
        // zie ook vorige methode (zelfde aanpak)
        for (Entry<Integer, Integer> entry : mapStatus.entrySet())
        {
            ConfigVri vri = vriList.get(vriName);
            String nameSignalGroup = vri.getSignalGroups().get(entry.getKey());
            Instant timeVLogNow = timeFromVLog[0].plusMillis(100 * deltaTimeFromVLog[0]);
            Long milliSecondsPassed = ChronoUnit.MILLIS.between(GTM.startTimeSimulation, timeVLogNow);
            if (GTM.signalGroupToTrafficLights.containsKey(vri.getName() + "_" + nameSignalGroup))
            {
                for (TrafficLight trafficLight : GTM.signalGroupToTrafficLights.get(vri.getName() + "_" + nameSignalGroup))
                {
                    try
                    {
                        simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(milliSecondsPassed, TimeUnit.MILLISECOND),
                            trafficLight, trafficLight, "changeColor", new Object[]{entry.getValue()});
                        /*-
                        System.out.println(new DoubleScalar.Abs<TimeUnit>(milliSecondsPassed, TimeUnit.MILLISECOND) + " - "
                            + vri.getName() + ": " + timeVLogNow + ": " + vri.getName() + "_" + nameSignalGroup
                            + ", status[.,.] = [" + entry.getKey() + "," + entry.getValue() + "]");
                         */
                    }
                    catch (RemoteException | SimRuntimeException exception)
                    {
                        exception.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
            else
            {
                // System.out.println("TL nameSignalGroup not found: " +
                // vri.getName() + "_" + nameSignalGroup);
            }
        }
    }

    // toetsen of de dynamisch bepaalde situatie met detectoren overeenkomt met
    // een statusbericht
    public static void CheckStatusDetector(HashMap<String, AbstractSensor> mapSensor, HashMap<Integer, Integer> mapStatus,
        HashMap<String, ConfigVri> vriList, String vriName)
    {
        for (Entry<Integer, Integer> entry : mapStatus.entrySet())
        {
            ConfigVri vri = vriList.get(vriName);
            String nameDetector = vri.getDetectors().get(entry.getKey());
            if (mapSensor.get(vriName + nameDetector) != null)
            {
                /*- TODO
                HashMap<DoubleScalar.Abs<TimeUnit>, Integer> map = mapSensor.get(vriName + nameDetector).getStatusByTime();
                // compare value of latest change and this status
                Entry<DoubleScalar.Abs<TimeUnit>, Integer> maxEntry = null;
                for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entry1 : map.entrySet())
                {
                    if (maxEntry == null || entry1.getKey().getSI() > maxEntry.getValue())
                    {
                        maxEntry = entry1;
                    }
                }
                if (entry.getValue() != maxEntry.getValue())
                {
                    System.out.println("Status detector verkeerd ingelezen!!!!!");
                }
                 */
            }

        }
    }

    public static void CheckStatusSignalGroup(HashMap<String, AbstractSensor> mapSensor,
        HashMap<Integer, Integer> mapStatus, HashMap<String, ConfigVri> vriList, String vriName)
    {
        for (Entry<Integer, Integer> entry : mapStatus.entrySet())
        {
            ConfigVri vri = vriList.get(vriName);
            String nameSignalGroup = vri.getSignalGroups().get(entry.getKey());
            if (mapSensor.get(vriName + nameSignalGroup) != null)
            {
                /*- TODO
                HashMap<DoubleScalar.Abs<TimeUnit>, Integer> map =
                    mapSensor.get(vriName + nameSignalGroup).getStatusByTime();
                // compare value of latest change and this status
                Entry<DoubleScalar.Abs<TimeUnit>, Integer> maxEntry = null;
                for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entry1 : map.entrySet())
                {
                    if (maxEntry == null || entry1.getKey().getSI() > maxEntry.getValue())
                    {
                        maxEntry = entry1;
                    }
                }
                if (entry.getValue() != maxEntry.getValue())
                {
                    System.out.println("Status signaalgroep verkeerd ingelezen!!!!!");
                }
                 */
            }
        }
    }

    /*******************************************************************************************************************/
    /***************************** PARSE ONDERDELEN VAN DE VLOG BESTANDEN (BYTE, NIBBLE, ...) **************************/
    /*******************************************************************************************************************/

    // het type bericht (letter 1 en 2)
    public static int parseTypebericht(final StringBuffer s, int aantal)
    {
        return parseByte(s);
    }

    /**
     * Lees de tijd uit string s
     * @param s de string met informatie.
     * @return de tijd als een Instant.
     */
    public static Instant parseTijd(final StringBuffer s)
    {
        // System.out.println(s);
        int year = parseNibble(s) * 1000 + parseNibble(s) * 100 + parseNibble(s) * 10 + parseNibble(s);
        // XXX: some files have a year coded as '0015' instead of '2015'...
        if (year < 100)
        {
            year += 2000;
        }
        int month = parseNibble(s) * 10 + parseNibble(s);
        int day = parseNibble(s) * 10 + parseNibble(s);
        int hour = parseNibble(s) * 10 + parseNibble(s);
        // XXX: VLOG files contain a 2-hour difference between the hour in the
        // internal data and the hour in the filename
        // XXX: but we assume the value IN the file to be correct
        int minute = parseNibble(s) * 10 + parseNibble(s);
        int second = parseNibble(s) * 10 + parseNibble(s);
        int tenth = parseNibble(s);
        Instant timeStamp =
            Instant.parse(String
                .format("%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day, hour, minute, second, tenth));
        return timeStamp;
    }

    // lees bij een statusbericht de waarden van de detectoren/signaalgroepen
    public static HashMap<Integer, Integer> parseStatus(final StringBuffer s, Long[] deltaTimeFromVLog)
    {
        deltaTimeFromVLog[0] = parseLong(s, 3);
        // lees reserve 4Bits
        parse4Bits(s);
        int aantal = parseByte(s);
        HashMap<Integer, Integer> mapDetectieStatus = new HashMap<Integer, Integer>();
        for (int i = 0; i < aantal; i++)
        {
            Integer detectorStatus = (int) parseLong(s, 1);
            mapDetectieStatus.put(i, detectorStatus);
        }
        return mapDetectieStatus;
    }

    // lees bij een wijzigigingsbericht de gewijzigde waarden van de
    // detectoren/signaalgroepen
    public static HashMap<Integer, Integer> parseWijziging(final StringBuffer s, Long[] deltaTimeFromVLog)
    {
        deltaTimeFromVLog[0] = parseLong(s, 3);
        int aantal = parse4Bits(s);
        HashMap<Integer, Integer> mapDetectieWijziging = new HashMap<Integer, Integer>();
        for (int i = 0; i < aantal; i++)
        {
            Integer detectorIndex = parseByte(s);
            Integer detectorStatus = parseByte(s);
            mapDetectieWijziging.put(detectorIndex, detectorStatus);
        }
        return mapDetectieWijziging;
    }

    public static int parseNibble(final StringBuffer s)
    {
        if (s.length() == 0)
        {
            System.out.println("empty parseNibble");
        }
        byte b0 = parseChar(s.charAt(0));
        s.delete(0, 1);
        return b0;
    }

    public static long parseLong(final StringBuffer s, int count)
    {
        long result = 0;
        for (int i = 0; i < count; i++)
        {
            int b = parseChar(s.charAt(i));
            result += b * Math.pow(16, (count - i - 1));
        }
        s.delete(0, count);
        return result;
    }

    public static int parseByte(final StringBuffer s)
    {
        byte b0 = parseChar(s.charAt(0));
        byte b1 = parseChar(s.charAt(1));
        s.delete(0, 2);
        return (byte) ((b0 * 16) + b1);
    }

    public static int parse4Bits(final StringBuffer s)
    {
        byte b0 = parseChar(s.charAt(0));
        s.delete(0, 1);
        return b0;
    }

    public static byte parseChar(final char c)
    {
        if (c >= '0' && c <= '9')
        {
            return (byte) (c - '0');
        }
        if (c >= 'A' && c <= 'F')
        {
            return (byte) ((byte) (c - 'A') + 10);
        }
        throw new RuntimeException("parseChar: character not hex: " + c);
    }

    class DetectorType
    {
        public static final String DETECTIELUS = "0x0001";

        public static final String DRUKKNOP = "0x0002";

        public static final String KOPLUS = "0x0100";

        public static final String LANGELUS = "0x0200";

        public static final String VERWEGLUS = "0x0400";

        public static final String DLKOP = "0x0101";

        public static final String DLLNG = "0x0201";

        public static final String DLVER = "0x0401";
    }

    enum DetectorTypes
    {
        DETECTIELUS, DRUKKNOP, KOPLUS, LANGELUS, VERWEGLUS, DLKOPLUS, DLLNG, DLVER
    };

}
