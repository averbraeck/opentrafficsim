package nl.grontmij.smarttraffic.lane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 17, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ConfigFile
{
    /**
     * Functions to read (streaming) V-log files: concentrates on detector and traffic light information
     */
    private ConfigFile()
    {
        // cannot be instantiated.
    }

    /**
     * Read vlog config files for the given array of vri numbers in the given folder.
     * @param dirConfigVri directory
     * @param dirBase absolute location of VRI directory
     * @param wegNummer e.g., 201
     * @param vriNummers e.g., {"231", "232", "233"}
     * @return a map of Strings to VRIs
     */
    public static HashMap<String, ConfigVri> readVlogConfigFiles(String dirBase, String wegNummer,
        String[] vriNummers)
    {
        // lijst met de configuratie van de vri's: naam kruispunt, detector (index en naam) en signaalgroep (index en naam)
        HashMap<String, ConfigVri> configVriList = new HashMap<String, ConfigVri>();
        // read VRI config files
        try
        {
            for (String vri : vriNummers)
            {
                String vriName = vri;
                String vriLocation = "VRI" + wegNummer + vriName;
                if (URLResource.getResource(dirBase + vriLocation + ".cfg") != null)
                {
                    URL url = URLResource.getResource(dirBase + vriLocation + ".cfg");
                    BufferedReader bufferedReader = null;
                    String path = url.getPath();
                    bufferedReader = new BufferedReader(new FileReader(path));
                    configVriList.put(vriName, readVLogConfigFile(bufferedReader));
                }
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            System.exit(-1);
        }
        return configVriList;
    }

    /**
     * Read one cfg-file using the BufferedReader.
     * @param bufferedReader the reader to the file.
     * @return a VRI configuration object
     * @throws IOException on read error
     */
    public static ConfigVri readVLogConfigFile(BufferedReader bufferedReader) throws IOException
    {
        String line = "";
        String nameVRI = null;
        Map<Integer, String> detectors = new HashMap<Integer, String>();
        Map<Integer, String> signalGroups = new HashMap<Integer, String>();

        while ((line = bufferedReader.readLine()) != null)
        {
            StringBuffer buffer = new StringBuffer(line);
            // zoek //DP
            if (buffer.length() > 0)
            {
                // //SYS
                // SYS,"201225"
                if (buffer.length() >= 4)
                {
                    if (buffer.substring(0, 4).contentEquals("//SY"))
                    {
                        line = bufferedReader.readLine();
                        line = line.replaceAll("\"", "");
                        String[] info = line.split(",");
                        String weg = info[1].substring(0, 3);
                        String vri = info[1].substring(3, 6);
                        nameVRI = vri;
                    }

                    else if (buffer.substring(0, 4).contentEquals("//DP"))
                    {
                        while ((line = bufferedReader.readLine()) != null)
                        {
                            if (line.length() > 0)
                            {
                                if (line.substring(0, 2).contentEquals("DP"))
                                {
                                    readDetectorSettings(line, detectors);
                                }
                                else if (line.length() >= 2)
                                {
                                    if (line.substring(0, 2).contentEquals("//"))
                                    {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    else if (buffer.substring(0, 4).contentEquals("//IS"))
                    {
                        // TODO: add IS info als nodig

                    }
                    else if (buffer.substring(0, 4).contentEquals("//FC"))
                    {
                        while ((line = bufferedReader.readLine()) != null)
                        {
                            if (line.length() > 0)
                            {
                                if (line.substring(0, 2).contentEquals("FC"))
                                {
                                    readTrafficlightSettings(line, signalGroups);
                                }
                                else if (line.length() >= 2)
                                {
                                    if (line.substring(0, 2).contentEquals("//"))
                                    {
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

    // lees bij een statusbericht de waarden van de detectoren/signaalgroepen
    public static void readDetectorSettings(String line, Map<Integer, String> detectors)
    {
        String[] buffer = new String[4];
        buffer = line.split(",");
        int count = Integer.parseInt(buffer[1]);
        String name = buffer[2].replaceAll("\"", "");
        String part1 = name.startsWith("0") ? name.substring(1, 2) : name.substring(0, 2);
        String part2 = name.length() == 3 ? name.substring(2, 3) : name.substring(2, 4);
        name = part1 + "." + part2;
        long l = Long.parseLong(buffer[3]);
        String strHex = String.format("0x%04X", l);
        if (strHex.substring(3, 4).contentEquals("1")
            && (strHex.substring(5, 6).contentEquals("1") || strHex.substring(5, 6).contentEquals("4")))
        {
            name = "K" + name;
        }
        detectors.put(count, name);
        // if ()
    }

    // lees bij een wijzigigingsbericht de gewijzigde waarden van de
    // detectoren/signaalgroepen
    public static void readTrafficlightSettings(String line, Map<Integer, String> signalGroups)
    {
        String[] buffer = new String[4];
        buffer = line.split(",");
        int fc_index = Integer.parseInt(buffer[1]);
        String fc_code = buffer[2].replaceAll("\"", "");
        long fc_type = Long.parseLong(buffer[3]); // 1 = motorvoertuig, 2 = voetganger, 4 = fiets, 8 = OV
        // if (fc_type == 1)
        {
            signalGroups.put(fc_index, fc_code);
        }
    }

}

