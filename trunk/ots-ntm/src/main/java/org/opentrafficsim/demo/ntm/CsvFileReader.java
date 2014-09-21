package org.opentrafficsim.demo.ntm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;

/**
 * A Cell extends a Zone and is used for the NetworkTransmissionModel The Cells cover a preferably homogeneous area and
 * have their specific characteristics such as their free speed, a capacity and an NFD diagram A trip matrix quantifies
 * the amount of trips between Cells in a network. The connection of neighbouring Cells are expressed by Links
 * (connectors) The cost to go from one to another Cell is quantified through the weights on the Connectors </pre>
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 4 Sep 2014 <br>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */

public class CsvFileReader
{

    /**
     * @param csvFileName
     * @param csvSplitBy : token that defines how to split a line
     * @return the TripDemand (nested HashMap: <origin, map<destination, tripinformation>>
     * @throws IOException
     */
    public static TripDemand CsvReader(final String csvFileName, String csvSplitBy) throws IOException
    {
        TripDemand tripDemand = null;
        Map<Long, Map<Long, TripInfo>> demand = new HashMap<Long, Map<Long, TripInfo>>();
        ArrayList<Long> zoneNames = new ArrayList<Long>();
        BufferedReader bufferedReader = null;
        String line = "";
        URL url;
        if (new File(csvFileName).canRead())
            url = new File(csvFileName).toURI().toURL();
        else
            url = ShapeFileReader.class.getResource(csvFileName);
        String path = url.getPath();
        try
        {
            bufferedReader = new BufferedReader(new FileReader(path));
            // read the first line of the demand file from Omnitrans
            // the first line contains the destinations: put them in the array
            if ((line = bufferedReader.readLine()) != null)
            {
                String[] namesZone = line.split(csvSplitBy);
                for (String name : namesZone)
                {
                    Long checkedName = CheckName(name);
                    if (checkedName != null)
                    {
                        zoneNames.add(checkedName);
                    }
                }
            }
            // then, read all other lines: first column contains the name of the origin
            // this can be either a link or a centroid (starts with "C")
            while ((line = bufferedReader.readLine()) != null)
            {
                Map<Long, TripInfo> tripDemandRow = new HashMap<Long, TripInfo>();
                String[] tripData = line.split(csvSplitBy);
                boolean firstElement = true;
                long origin = 0;
                int index = 0;
                for (String dataItem : tripData)
                {
                    if (firstElement)
                    {
                        Long checkedName = CheckName(dataItem);
                        origin = checkedName;
                        firstElement = false;
                    }
                    else
                    {
                        dataItem = RemoveQuotes(dataItem);
                        TripInfo tripInfo = new TripInfo(Double.parseDouble(dataItem));
                        long destination = zoneNames.get(index);
                        tripDemandRow.put(destination, tripInfo);
                        index++;
                    }
                }
                if (demand.get(origin) != null)
                    throw new Error("duplicate origin");
                demand.put(origin, tripDemandRow);

                /*
                 * System.out.println(demand.get(3569L).get(1L).getNumberOfTrips());
                 * System.out.println(demand.get(3569L).get(2L).getNumberOfTrips());
                 */
            }

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bufferedReader != null)
            {
                try
                {
                    bufferedReader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        tripDemand = new TripDemand(demand);
        return tripDemand;

    }

    /**
     * @param name
     * @return name without quotes
     */
    public static String RemoveQuotes(String name)
    {
        if (name.length() >= 2 && name.charAt(0) == '"' && name.charAt(name.length() - 1) == '"')
        {
            name = name.substring(1, name.length() - 1);
        }
        return name;
    }

    /**
     * @param name
     * @return name as a long
     */
    public static Long CheckName(String name)
    {
        Long nr = null;
        // replace double quotes at start and end of string

        name = RemoveQuotes(name);

        if (name.startsWith("Links"))
        {
            nr = null;
        }
        else
        {
            nr = ShapeFileReader.InspectNodeCentroid(name);
        }
        return nr;
    }

}
