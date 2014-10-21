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
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.FractionOfTripDemandByTimeSegment;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

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
     * Class reads the demand and others from .csv type of files.
     * @param csvFileName name of file
     * @param csvSplitBy : token that defines how to split a line
     * @param csvSplitByTwo : two tokens that defines how to split a line
     * @param centroids sources of traffic
     * @param links roads
     * @param connectors artificial roads connecting the links and centroids
     * @param settingsNTM the parameters of the NTM
     * @param profiles departure profile of Trips
     * @param areas the NTM model areas
     * @return the TripDemand (nested HashMap: <origin, map<destination, tripinformation>>
     * @throws IOException
     * @throws Throwable
     */
    public static TripDemand<TripInfoTimeDynamic> readOmnitransExportDemand(final String csvFileName,
            final String csvSplitBy, final String csvSplitByTwo, final Map<String, Node> centroids,
            final Map<String, Link> links, final Map<String, Link> connectors, final NTMSettings settingsNTM,
            final ArrayList<DepartureTimeProfile> profiles, final Map<String, Area> areas) throws Throwable
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
        TripDemand<TripInfoTimeDynamic> tripDemand = new TripDemand<TripInfoTimeDynamic>();
        Map<String, Map<String, TripInfoTimeDynamic>> demand = new HashMap<String, Map<String, TripInfoTimeDynamic>>();
        Map<String, Node> centroidsAndCordonConnectors = new HashMap<String, Node>();
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
                String[] namesZone = line.split(csvSplitBy);
                int index = 0;
                for (String name : namesZone)
                {
                    // first we inspect if it is a centroid
                    name = CsvFileReader.removeQuotes(name);
                    String nameBA = name + "_BA";
                    boolean isCentroid = ShapeFileReader.inspectNodeCentroid(name);
                    Node cordonPoint = null;
                    if (isCentroid)
                    {
                        centroidsAndCordonConnectors.put(name, centroids.get(name));
                        orderedZones.put(index, name);
                    }
                    // otherwise it is a cordon link: detect the "zoneConnector" node (at the cordon)
                    // this is often a dangling link, but not always!
                    // we add the Node of the cordon Link to the "centroidsAndCordonConnectors"
                    else if (links.get(name) != null || connectors.get(name) != null || links.get(nameBA) != null
                            || connectors.get(nameBA) != null)
                    {
                        if (links.get(name) == null && connectors.get(name) == null)
                        {
                            name = nameBA;
                        }
                        Link cordonConnector = null;
                        boolean createArea = false;
                        if (links.get(name) != null)
                        {
                            cordonConnector = links.get(name);
                            createArea = true;
                            // remove cordonLink from normal links to connectors!!
                            links.remove(cordonConnector.getId());
                            cordonConnector.setBehaviourType(TrafficBehaviourType.CORDON);
                            connectors.put(name, cordonConnector);
                        }
                        else if (connectors.get(name) != null)
                        {
                            System.out.println("Strange: connector already defined???");
                            cordonConnector = connectors.get(name);
                            createArea = true;
                        }
                        if (cordonConnector == null)
                        {
                            System.out.println("Strange: no connector found ??????????!!!!");
                        }

                        Node nodeA = cordonConnector.getStartNode();
                        Node nodeB = cordonConnector.getEndNode();
                        int countedNodesA = 0;
                        int countedNodesB = 0;
                        for (Link link : links.values())
                        {
                            if (link.getStartNode().equals(nodeA))
                            {
                                countedNodesA++;
                            }
                            if (link.getStartNode().equals(nodeB))
                            {
                                countedNodesB++;
                            }
                            if (link.getEndNode().equals(nodeA))
                            {
                                countedNodesA++;
                            }
                            if (link.getEndNode().equals(nodeB))
                            {
                                countedNodesB++;
                            }

                        }
                        if (countedNodesA > countedNodesB)
                        {
                            Node node = null;
                            // there could be more connectors attached to this node. If so, create a new Node
                            if (centroidsAndCordonConnectors.get(nodeB.getId()) != null)
                            {

                                double x = nodeB.getPoint().getX() + 3;
                                double y = nodeB.getPoint().getY() + 3;
                                Point point = Node.createPoint(x, y);
                                String nr = nodeB.getId() + "_" + nodeA.getId();
                                node = new Node(nr, point, null);
                                centroids.put(nr, node);
                                centroidsAndCordonConnectors.put(node.getId(), node);
                                orderedZones.put(index, node.getId());
                                cordonPoint = node;
                                connectors.remove(cordonConnector);
                                cordonConnector =
                                        new Link(cordonConnector.getGeometry(), cordonConnector.getId(),
                                                cordonConnector.getLength(), cordonConnector.getStartNode(), node,
                                                cordonConnector.getSpeed(), cordonConnector.getCapacity(),
                                                cordonConnector.getBehaviourType(), cordonConnector.getLinkData());
                                connectors.put(name, cordonConnector);
                            }
                            else
                            {
                                centroidsAndCordonConnectors.put(nodeB.getId(), nodeB);
                                orderedZones.put(index, nodeB.getId());
                                cordonPoint = nodeB;
                            }
                        }
                        else
                        {
                            Node node = null;
                            // there could be more connectors attached to this node. If so, create a new Node
                            if (centroidsAndCordonConnectors.get(nodeA.getId()) != null)
                            {
                                double x = nodeA.getPoint().getX() + 3;
                                double y = nodeA.getPoint().getY() + 3;
                                Point point = Node.createPoint(x, y);
                                String nr = nodeA.getId() + "_" + nodeB.getId();
                                node = new Node(nr, point, TrafficBehaviourType.ROAD);
                                centroids.put(nr, node);
                                centroidsAndCordonConnectors.put(node.getId(), node);
                                orderedZones.put(index, node.getId());
                                cordonPoint = node;
                                connectors.remove(cordonConnector);
                                cordonConnector =
                                        new Link(cordonConnector.getGeometry(), cordonConnector.getId(),
                                                cordonConnector.getLength(), node, cordonConnector.getEndNode(), 
                                                cordonConnector.getSpeed(), cordonConnector.getCapacity(),
                                                cordonConnector.getBehaviourType(), cordonConnector.getLinkData());
                                connectors.put(name, cordonConnector);
                            }
                            else
                            {
                                centroidsAndCordonConnectors.put(nodeA.getId(), nodeA);
                                orderedZones.put(index, nodeA.getId());
                                cordonPoint = nodeA;
                            }
                        }
                        if (createArea)
                        {
                            // after determining the new cordon centroid, a new area is created around this feeding
                            // link. This becomes a feeder type of area
                            Geometry buffer = cordonConnector.getGeometry().getLineString().buffer(10);
                            Point centroid = cordonPoint.getPoint();
                            String nr = cordonPoint.getId();
                            String newName = cordonConnector.getLinkData().getName();
                            String gemeente = cordonConnector.getLinkData().getName();
                            String gebied = cordonConnector.getLinkData().getName();
                            String regio = "cordonPoint " + nr;
                            double dhb = 0.0;
                            Area area =
                                    new Area(buffer, nr, newName, gemeente, gebied, regio, dhb, centroid,
                                            TrafficBehaviourType.CORDON);
                            areas.put(nr, area);
                        }

                    }
                    else if (name.contentEquals("Links + Centroids"))
                    {
                        continue;
                    }
                    else
                    {
                        System.out.println("Strange: no connector found!!!!");
                        continue;
                    }

                    index++;
                }
            }
            // after these preparations, the tripdata are being read
            // only values > 0 are recorded!
            // then, read all other lines: first column contains the name of the origin
            // this can be either a link or a centroid (starts with "C")
            int indexRow = 0;
            while ((line = bufferedReader.readLine()) != null)
            {
                Map<String, TripInfoTimeDynamic> tripDemandRow = new HashMap<String, TripInfoTimeDynamic>();
                String[] tripData = line.split(csvSplitBy);
                boolean firstElement = true;
                String origin = null;
                String originLinknr = null;
                int indexColumn = 0;
                for (String numberOfTrips : tripData)
                {
                    numberOfTrips = removeQuotes(numberOfTrips);
                    if (firstElement)
                    {
                        /*
                         * String checkedName = returnNumber(dataItem); origin = checkedName;
                         */
                        System.out.println("I " + indexRow + "  name: "
                                + centroidsAndCordonConnectors.get(orderedZones.get(indexRow)).getId());
                        origin = centroidsAndCordonConnectors.get(orderedZones.get(indexRow)).getId();
                        originLinknr = numberOfTrips;
                        firstElement = false;
                    }
                    else
                    {
                        numberOfTrips = removeQuotes(numberOfTrips);
                        // only the non-zero cells
                        if (Double.parseDouble(numberOfTrips) > 0.0)
                        {
                            // TODO: now we simply take the first time profile.
                            // This should be input in file: which profile is connected to which OD pair
                            DepartureTimeProfile profile = profiles.get(0);
                            Abs<TimeUnit> startSimulationTimeSinceMidnight = settingsNTM.getStartTimeSinceMidnight();
                            NavigableMap<Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> profileList =
                                    profile.checkAndNormalizeCurve(startSimulationTimeSinceMidnight,
                                            settingsNTM.getDurationOfSimulation(), profile.getDepartureTimeCurve());
                            profile.setDepartureTimeCurve(profileList);
                            TripInfoTimeDynamic tripInfo =
                                    new TripInfoTimeDynamic(Double.parseDouble(numberOfTrips), profile);
                            if (centroidsAndCordonConnectors.get(orderedZones.get(indexColumn)) == null)
                            {
                                System.out.println("Strange: no destination????");
                            }
                            else
                            {
                                String destination =
                                        centroidsAndCordonConnectors.get(orderedZones.get(indexColumn)).getId();
                                tripDemandRow.put(destination, tripInfo);
                            }
                        }
                        indexColumn++;

                    }
                }
                if (demand.get(origin) != null)
                {
                    System.out.println("duplicate origin!!" + originLinknr + " knoop " + origin);
                    // throw new Error("duplicate origin");
                }
                demand.put(origin, tripDemandRow);
                indexRow++;

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
    public static ArrayList<DepartureTimeProfile> readDepartureTimeProfiles(final String csvFileName,
            final String csvSplitBy, final String csvSplitInternalBy) throws IOException, ParseException
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
        NavigableMap<DoubleScalar.Abs<TimeUnit>, FractionOfTripDemandByTimeSegment> fractions = new TreeMap<DoubleScalar.Abs<TimeUnit>, FractionOfTripDemandByTimeSegment>();
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
                            fractions = new TreeMap<DoubleScalar.Abs<TimeUnit>, FractionOfTripDemandByTimeSegment>();
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
                                    fractions.put(segmentStartTime, newFraction);
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
                                fractions.put(segmentStartTime, newFraction);
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
    public static String removeQuotes(final String name)
    {
        String newName = name;
        if (newName.length() >= 2 && newName.charAt(0) == '"' && newName.charAt(newName.length() - 1) == '"')
        {
            newName = newName.substring(1, newName.length() - 1);
        }
        return newName;
    }

    /*    *//**
     * @param name
     * @return name as a long
     */
    /*
     * public static String returnNumber(final String name) { // replace double quotes at start and end of string String
     * nr = removeQuotes(name); if (name.startsWith("Links")) { nr = null; } else { nr =
     * ShapeFileReader.NodeCentroidNumber(nr); } return nr; }
     */

}
