package org.opentrafficsim.demo.ntm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.FractionOfTripDemandByTimeSegment;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfo;

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
     * @param csvSplitByTwo : two tokens that defines how to split a line
     * @param centroids
     * @param links
     * @param connectors
     * @return the TripDemand (nested HashMap: <origin, map<destination, tripinformation>>
     * @throws IOException
     * @throws Throwable
     */
    public static TripDemand ReadOmnitransExportDemand(final String csvFileName, String csvSplitBy,
            final String csvSplitByTwo, Map<String, ShpNode> centroids, Map<String, ShpLink> links,
            Map<String, ShpLink> connectors, NTMSettings settingsNTM) throws Throwable
    {
        BufferedReader bufferedReader = null;
        String line = "";
        URL url;
        if (new File(csvFileName).canRead())
        {
            url = new File(csvFileName).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(csvFileName);
        }

        String path = url.getPath();
        TripDemand tripDemand = new TripDemand();
        Map<String, Map<String, TripInfo>> demand = new HashMap<String, Map<String, TripInfo>>();
        Map<String, ShpNode> centroidsAndCordonConnectors = new HashMap<String, ShpNode>();
        try
        {

            bufferedReader = new BufferedReader(new FileReader(path));

            // read the first line of the demand file from Omnitrans
            // this line contains the time period of the demand file: as an example....
            // TimePeriod: 07:00:00 - 09:00:00
            if ((line = bufferedReader.readLine()) != null)
            {
                String[] timePeriod = line.split(csvSplitByTwo);
                int counter = 0;
                for (String name : timePeriod)
                {
                    if (name.length() > 0)
                    {
                        if (name.charAt(2) == ':')
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                            Calendar timeInstance = Calendar.getInstance();
                            timeInstance.setTime(sdf.parse(name));
                            if (counter == 0)
                            {
                                DoubleScalar.Abs<TimeUnit> startTime =
                                        new DoubleScalar.Abs<TimeUnit>(timeInstance.getTimeInMillis() / 1000,
                                                TimeUnit.SECOND);
                                tripDemand.setStartTime(startTime);
                                settingsNTM.setStartTimeSinceMidnight(startTime);
                            }
                            else if (counter == 1)
                            {
                                DoubleScalar.Abs<TimeUnit> endTime =
                                        new DoubleScalar.Abs<TimeUnit>(timeInstance.getTimeInMillis() / 1000,
                                                TimeUnit.SECOND);
                                DoubleScalar.Rel<TimeUnit> timeSpan =
                                        MutableDoubleScalar.Abs.minus(endTime, tripDemand.getStartTime()).immutable();

                                tripDemand.setTimeSpan(timeSpan);
                                settingsNTM.setDurationOfSimulation(timeSpan);
                            }
                            counter++;
                        }
                    }
                }

            }

            // read the second line of the demand file from Omnitrans
            // this line contains the destinations: put them in the array
            // The internal centroids start wit a capital "C",
            // in case of a subarea model, other centroids are the nodes that are at the cordon of the subarea. They
            // have the link number plus (sometimes) a name of the road
            // "Links + Centroids";"3569";"11212";"95014";"95608";"116117";"116738";...................";
            // ..... "563089";"563430";"C1";"C2";"C3";"C4";"C5";"C6";".........."
            HashMap<Integer, String> orderedZones = new HashMap<Integer, String>();
            if ((line = bufferedReader.readLine()) != null)
            {
                /*
                 * // temporarily create a map that finds the node indices of centroids Map<String, ShpNode>
                 * invertCentroids = new HashMap<>(); for (ShpNode centroid : centroids.values()) {
                 * invertCentroids.put(centroid.getName(), centroid); }
                 */

                String[] namesZone = line.split(csvSplitBy);
                int index = 0;
                for (String name : namesZone)
                {
                    // first we inspect if it is a centroid
                    name = CsvFileReader.RemoveQuotes(name);
                    boolean isCentroid = ShapeFileReader.InspectNodeCentroid(name);
                    if (isCentroid)
                    {
                        centroidsAndCordonConnectors.put(name, centroids.get(name));
                        orderedZones.put(index, name);
                    }
                    // otherwise it is a cordon link: detect the "zoneConnector" node (at the cordon)
                    // this is by definition a dangling link
                    // we add the Node of the cordon Link to the "centroidsAndCordonConnectors"
                    else if (links.get(name) != null || connectors.get(name) != null)
                    {
                        ShpLink cordonConnector = null;
                        if (links.get(name) != null)
                        {
                            cordonConnector = links.get(name);
                            links.remove(links.get(name));
                            connectors.put(name, cordonConnector);
                        }
                        else if (connectors.get(name) != null)
                        {
                            cordonConnector = connectors.get(name);
                        }

                        if (cordonConnector == null)
                        {
                            System.out.println("Strange: no connector found!!!!");
                        }
                        ShpNode nodeA = cordonConnector.getNodeA();
                        ShpNode nodeB = cordonConnector.getNodeB();
                        int countedNodesA = 0;
                        int countedNodesB = 0;
                        for (ShpLink link : links.values())
                        {
                            if (link.getNodeA().equals(nodeA))
                            {
                                countedNodesA++;
                            }
                            if (link.getNodeA().equals(nodeB))
                            {
                                countedNodesB++;
                            }
                            if (link.getNodeB().equals(nodeA))
                            {
                                countedNodesA++;
                            }
                            if (link.getNodeB().equals(nodeB))
                            {
                                countedNodesB++;
                            }

                        }
                        if (countedNodesA > countedNodesB)
                        {
                            centroidsAndCordonConnectors.put(nodeA.getName(), nodeA);
                            orderedZones.put(index, nodeA.getName());
                        }
                        else
                        {
                            centroidsAndCordonConnectors.put(nodeB.getName(), nodeB);
                            orderedZones.put(index, nodeB.getName());
                        }

                    }
                    else
                    {
                        System.out.println("Strange: no connector found!!!!");
                        continue;
                    }

                    index++;
                }
            }

            // then, read all other lines: first column contains the name of the origin
            // this can be either a link or a centroid (starts with "C")
            while ((line = bufferedReader.readLine()) != null)
            {
                Map<String, TripInfo> tripDemandRow = new HashMap<String, TripInfo>();
                String[] tripData = line.split(csvSplitBy);
                boolean firstElement = true;
                String origin = null;
                int index = 0;
                for (String dataItem : tripData)
                {
                    dataItem = RemoveQuotes(dataItem);
                    if (firstElement)
                    {
                        String checkedName = returnNumber(dataItem);
                        origin = checkedName;
                        firstElement = false;
                    }
                    else
                    {
                        dataItem = RemoveQuotes(dataItem);
                        TripInfo tripInfo = new TripInfo(Double.parseDouble(dataItem));
                        if (centroidsAndCordonConnectors.get(orderedZones.get(index)) == null)
                        {
                            System.out.println("Strange: no destination????");
                        }
                        else
                        {
                            String destination = centroidsAndCordonConnectors.get(orderedZones.get(index)).getName();
                            tripDemandRow.put(destination, tripInfo);
                        }
                        index++;
                    }
                }
                if (demand.get(origin) != null)
                {
                    throw new Error("duplicate origin");
                }
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
        tripDemand.setTripInfo(demand);
        return tripDemand;

    }

    /**
     * @param csvFileName
     * @param csvSplitBy
     * @param csvSplitInternalBy
     * @return an ArrayList<DepartureTimeProfile<?>>
     * @throws IOException
     * @throws ParseException
     */
    public static ArrayList<DepartureTimeProfile> ReadDepartureTimeProfiles(final String csvFileName,
            String csvSplitBy, String csvSplitInternalBy) throws IOException, ParseException
    {
        BufferedReader bufferedReader = null;
        String line = "";
        URL url;
        if (new File(csvFileName).canRead())
        {
            url = new File(csvFileName).toURI().toURL();
        }
        else
        {
            url = ShapeFileReader.class.getResource(csvFileName);
        }

        String path = url.getPath();

        final String NAME = "ProfileName";
        final String FRACTION = "Fraction";
        final String TIME = "Time";
        final String DURATION = "SegmentDuration";

        ArrayList<DepartureTimeProfile> profiles = new ArrayList<DepartureTimeProfile>();
        ArrayList<FractionOfTripDemandByTimeSegment> fractions = null;
        DepartureTimeProfile profile = null;
        DoubleScalar.Rel<TimeUnit> duration = null;
        DoubleScalar.Abs<TimeUnit> segmentStartTime = null;
        Double fraction = null;
        try
        {
            bufferedReader = new BufferedReader(new FileReader(path));
            // read all lines: first column contains the name of the origin
            // this can be either a link or a centroid (starts with "C")
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] completeLine = line.split(csvSplitBy);
                for (String lineSegment : completeLine)
                {
                    lineSegment = lineSegment.trim();
                    String[] dataItem = lineSegment.split(csvSplitInternalBy);
                    if (dataItem.length > 0)
                    {
                        // if we encounter a ProfileName, create a new profile
                        if (dataItem[0].equals(NAME))
                        {
                            fractions = new ArrayList<FractionOfTripDemandByTimeSegment>();
                            profile = new DepartureTimeProfile();
                            profile.setName(dataItem[1]);
                            profiles.add(profile);
                        }
                        // detects the duration of the succeeding fractions
                        else if (dataItem[0].equals(DURATION))
                        {
                            final String TIMEZERO = "00:00:00";
                            String timeSpan = dataItem[1];
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                            Calendar segmentStart = Calendar.getInstance();
                            Calendar segmentEnd = Calendar.getInstance();
                            segmentEnd.setTime(sdf.parse(timeSpan));
                            segmentStart.setTime(sdf.parse(TIMEZERO));
                            duration =
                                    new DoubleScalar.Rel<TimeUnit>(
                                            (segmentEnd.getTimeInMillis() - segmentStart.getTimeInMillis()) / 1000,
                                            TimeUnit.SECOND);
                        }
                        // detects the specific Time that relates to the next fractions
                        else if (dataItem[0].equals(TIME))
                        {
                            String segmentTime = dataItem[1];
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                            Calendar startTime = Calendar.getInstance();
                            startTime.setTime(sdf.parse(segmentTime));
                            DoubleScalar.Abs<TimeUnit> prevStartTime = segmentStartTime;
                            segmentStartTime =
                                    new DoubleScalar.Abs<TimeUnit>(startTime.getTimeInMillis() / 1000, TimeUnit.SECOND);
                            if (prevStartTime != null)
                            {
                                while (MutableDoubleScalar.Abs.minus(segmentStartTime, prevStartTime).doubleValue() > 0)
                                {
                                    FractionOfTripDemandByTimeSegment newFraction =
                                            new FractionOfTripDemandByTimeSegment(segmentStartTime, duration, fraction);
                                    fractions.add(newFraction);
                                    prevStartTime = MutableDoubleScalar.Abs.plus(prevStartTime, duration).immutable();
                                }
                            }
                            profile.setDepartureTimeCurve(fractions);
                        }
                        // the fractions of traffic demand
                        else if (dataItem[0].equals(FRACTION))
                        {
                            for (int i = 1; i < dataItem.length; i++)
                            {
                                fraction = Double.parseDouble(dataItem[i]);
                                FractionOfTripDemandByTimeSegment newFraction =
                                        new FractionOfTripDemandByTimeSegment(segmentStartTime, duration, fraction);
                                fractions.add(newFraction);
                                segmentStartTime = MutableDoubleScalar.Abs.plus(segmentStartTime, duration).immutable();
                            }
                        }
                    }
                }

            }
            // test if the fractions are read correctly
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
        return profiles;
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
    public static String returnNumber(String name)
    {
        String nr = null;
        // replace double quotes at start and end of string

        name = RemoveQuotes(name);

        if (name.startsWith("Links"))
        {
            nr = null;
        }
        else
        {
            nr = ShapeFileReader.NodeCentroidNumber(name);
        }
        return nr;
    }

}
