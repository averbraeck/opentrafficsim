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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.FractionOfTripDemandByTimeSegment;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;

/**
 * A Cell extends a Zone and is used for the NetworkTransmissionModel The Cells cover a preferably homogeneous area and have
 * their specific characteristics such as their free speed, a capacity and an NFD diagram A trip matrix quantifies the amount of
 * trips between Cells in a network. The connection of neighbouring Cells are expressed by Links (connectors) The cost to go
 * from one to another Cell is quantified through the weights on the Connectors
 * </pre>
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 4 Sep 2014 <br>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */

public class CsvFileReader
{
    /**
     * Class reads the demand and others from .csv type of files.
     * @param csvFileName String; name of file
     * @param csvSplitBy String; : token that defines how to split a line
     * @param csvSplitByTwo String; : two tokens that defines how to split a line
     * @param centroids Map&lt;String,NTMNode&gt;; sources of traffic
     * @param links Map&lt;String,NTMLink&gt;; roads
     * @param connectors Map&lt;String,NTMLink&gt;; artificial roads connecting the links and centroids
     * @param settingsNTM NTMSettings; the parameters of the NTM
     * @param profiles ArrayList&lt;DepartureTimeProfile&gt;; departure profile of Trips
     * @param areas Map&lt;String,Area&gt;; the NTM model areas
     * @return the TripDemand (nested LinkedHashMap: <origin, map<destination, tripinformation>>
     * @throws IOException
     * @throws Throwable
     */
    public static TripDemand<TripInfoTimeDynamic> readOmnitransExportDemand(final NTMModel model, final String csvFileName,
            final String csvSplitBy, final String csvSplitByTwo, final Map<String, NTMNode> centroids,
            final Map<String, NTMLink> links, final Map<String, NTMLink> connectors, final NTMSettings settingsNTM,
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
        Map<String, Map<String, TripInfoTimeDynamic>> demand = new LinkedHashMap<>();
        Map<String, NTMNode> centroidsAndCordonConnectors = new LinkedHashMap<>();
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
                                Time startTime = new Time(timeInstance.getTimeInMillis() / 1000, TimeUnit.BASE_SECOND);
                                tripDemand.setStartTime(startTime);
                                settingsNTM.setDurationSinceMidnight(new Duration(startTime.getSI(), DurationUnit.SI));
                            }
                            else if (counter == 1)
                            {
                                Time endTime = new Time(timeInstance.getTimeInMillis() / 1000, TimeUnit.BASE_SECOND);
                                Duration timeSpan = endTime.minus(tripDemand.getStartTime());

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
            LinkedHashMap<Integer, String> orderedZones = new LinkedHashMap<Integer, String>();
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
                    NTMNode cordonPoint = null;

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
                        NTMLink cordonConnector = null;
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
                            // link 516821 is gekoppeld aan zone C203, maar in de matrix als cordonzone benoemd:
                            // TODO corrigeren
                            System.out.println("Connector already defined: probably a zoneconnector cut by the cordon");
                            cordonConnector = connectors.get(name);
                            createArea = true;
                        }
                        else
                        {
                            System.out.println("Strange: no connector found ??????????!!!!");
                        }

                        NTMNode nodeA = (NTMNode) cordonConnector.getStartNode();
                        NTMNode nodeB = (NTMNode) cordonConnector.getEndNode();
                        int countedNodesA = 0;
                        int countedNodesB = 0;
                        for (NTMLink link : links.values())
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
                            NTMNode node = null;
                            // there could be more connectors attached to this node. If so, create a new Node
                            if (centroidsAndCordonConnectors.get(nodeB.getId()) != null)
                            {
                                double x = nodeB.getPoint().x + 3;
                                double y = nodeB.getPoint().y + 3;
                                Coordinate point = NTMNode.createPoint(x, y);
                                String nr = nodeB.getId() + "_" + nodeA.getId();
                                node = new NTMNode(model.getNetwork(), nr, point, TrafficBehaviourType.CORDON);
                                centroids.put(nr, node);
                                centroidsAndCordonConnectors.put(node.getId(), node);
                                orderedZones.put(index, node.getId());
                                cordonPoint = node;
                                connectors.remove(cordonConnector);
                                cordonConnector = new NTMLink(model.getNetwork(), model.getSimulator(),
                                        cordonConnector.getDesignLine(), cordonConnector.getId(), cordonConnector.getLength(),
                                        (NTMNode) cordonConnector.getStartNode(), node, cordonConnector.getFreeSpeed(),
                                        cordonConnector.getDuration(), cordonConnector.getCapacity(),
                                        cordonConnector.getBehaviourType(), cordonConnector.getLinkData());
                                connectors.put(name, cordonConnector);
                            }
                            else
                            {
                                nodeB.setBehaviourType(TrafficBehaviourType.CORDON);
                                centroidsAndCordonConnectors.put(nodeB.getId(), nodeB);
                                orderedZones.put(index, nodeB.getId());
                                cordonPoint = nodeB;
                                centroids.put(cordonPoint.getId(), cordonPoint);
                            }
                        }
                        else
                        {
                            NTMNode node = null;
                            // there could be more connectors attached to this node. If so, create a new Node
                            if (centroidsAndCordonConnectors.get(nodeA.getId()) != null)
                            {
                                double x = nodeA.getPoint().x + 3;
                                double y = nodeA.getPoint().y + 3;
                                Coordinate point = NTMNode.createPoint(x, y);
                                String nr = nodeA.getId() + "_" + nodeB.getId();
                                node = new NTMNode(model.getNetwork(), nr, point, TrafficBehaviourType.CORDON);
                                centroids.put(nr, node);
                                centroidsAndCordonConnectors.put(node.getId(), node);
                                orderedZones.put(index, node.getId());
                                cordonPoint = node;
                                connectors.remove(cordonConnector);
                                cordonConnector = new NTMLink(model.getNetwork(), model.getSimulator(),
                                        cordonConnector.getDesignLine(), cordonConnector.getId(), cordonConnector.getLength(),
                                        node, (NTMNode) cordonConnector.getEndNode(), cordonConnector.getFreeSpeed(),
                                        cordonConnector.getDuration(), cordonConnector.getCapacity(),
                                        cordonConnector.getBehaviourType(), cordonConnector.getLinkData());

                                connectors.put(name, cordonConnector);
                            }
                            else
                            {
                                nodeA.setBehaviourType(TrafficBehaviourType.CORDON);
                                centroidsAndCordonConnectors.put(nodeA.getId(), nodeA);
                                orderedZones.put(index, nodeA.getId());
                                cordonPoint = nodeA;
                                centroids.put(cordonPoint.getId(), cordonPoint);
                            }
                        }
                        if (createArea)
                        {
                            // after determining the new cordon centroid, a new area is created around this feeding
                            // link. This becomes a feeder type of area
                            // Geometry buffer = cordonConnector.getStartNode().getPoint().getCoordinate().buffer(40);
                            GeometryFactory factory = new GeometryFactory();
                            Geometry point =
                                    factory.createPoint(new Coordinate(cordonPoint.getPoint().x, cordonPoint.getPoint().y));
                            Geometry buffer = point.buffer(40);
                            Coordinate centroid = cordonPoint.getPoint().getCoordinate();
                            String nr = cordonPoint.getId();
                            String newName = cordonConnector.getLinkData().getName();
                            String gemeente = cordonConnector.getLinkData().getName();
                            String gebied = cordonConnector.getLinkData().getName();
                            String regio = "cordonPoint " + nr;
                            double dhb = 0.0;
                            Double increaseDemandByFactor = settingsNTM.getScalingFactorDemand();
                            double accCritMaxCapStart = 25;
                            double accCritMaxCapEnd = 50;
                            double accCritJam = 100;
                            ArrayList<java.lang.Double> accCritical = new ArrayList<java.lang.Double>();
                            accCritical.add(accCritMaxCapStart);
                            accCritical.add(accCritMaxCapEnd);
                            accCritical.add(accCritJam);
                            ParametersNTM parametersNTM = new ParametersNTM(accCritical);
                            Area area = new Area(buffer, nr, newName, gemeente, gebied, regio, dhb, centroid,
                                    TrafficBehaviourType.CORDON, new Length(0, LengthUnit.METER),
                                    new Speed(0, SpeedUnit.KM_PER_HOUR), increaseDemandByFactor, parametersNTM);
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
            while ((line = bufferedReader.readLine()) != null && !line.isEmpty())
            {
                Map<String, TripInfoTimeDynamic> tripDemandRow = new LinkedHashMap<String, TripInfoTimeDynamic>();
                String[] tripData = line.split(csvSplitBy);
                boolean firstElement = true;
                NTMNode origin = null;
                String originLinknr = null;
                int indexColumn = 0;
                for (String numberOfTrips : tripData)
                {
                    numberOfTrips = removeQuotes(numberOfTrips);
                    if (firstElement)
                    {
                        // System.out.println("I " + indexRow + " name: "
                        // + centroidsAndCordonConnectors.get(orderedZones.get(indexRow)).getId());
                        origin = centroidsAndCordonConnectors.get(orderedZones.get(indexRow));
                        originLinknr = numberOfTrips;
                        firstElement = false;
                    }
                    else
                    {
                        numberOfTrips = removeQuotes(numberOfTrips);
                        // only the non-zero cells
                        if (Double.parseDouble(numberOfTrips) > 0.0)
                        {
                            // TODO now we simply take the first time profile.
                            // This should be input in file: which profile is connected to which OD pair
                            DepartureTimeProfile profile = profiles.get(0);
                            Time startSimulationTimeSinceMidnight =
                                    new Time(settingsNTM.getDurationSinceMidnight().getSI(), TimeUnit.BASE);
                            NavigableMap<Time, FractionOfTripDemandByTimeSegment> profileList =
                                    profile.checkAndNormalizeCurve(startSimulationTimeSinceMidnight,
                                            settingsNTM.getDurationOfSimulation(), profile.getDepartureTimeCurve());
                            profile.setDepartureTimeCurve(profileList);
                            TripInfoTimeDynamic tripInfo = new TripInfoTimeDynamic(Double.parseDouble(numberOfTrips), profile);
                            if (centroidsAndCordonConnectors.get(orderedZones.get(indexColumn)) == null)
                            {
                                System.out.println("Strange: no destination????");
                            }
                            else
                            {
                                NTMNode destination = centroidsAndCordonConnectors.get(orderedZones.get(indexColumn));
                                /*
                                 * if (destination.equals("2430")) { System.out.println("Strange: 2430"); }
                                 */
                                tripDemandRow.put(destination.getId(), tripInfo);
                            }
                        }
                        indexColumn++;

                    }
                }
                if (demand.get(origin) != null)
                {
                    System.out.println("duplicate origin!!" + originLinknr + " knoop " + origin.getId());
                    // throw new Error("duplicate origin");
                }
                demand.put(origin.getId(), tripDemandRow);
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
     * @param csvFileName String;
     * @param csvSplitBy String;
     * @param csvSplitInternalBy String;
     * @return an ArrayList<DepartureTimeProfile<?>>
     * @throws IOException
     * @throws ParseException
     */
    public static ArrayList<DepartureTimeProfile> readDepartureTimeProfiles(final String csvFileName, final String csvSplitBy,
            final String csvSplitInternalBy) throws IOException, ParseException
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
        NavigableMap<Time, FractionOfTripDemandByTimeSegment> fractions =
                new TreeMap<Time, FractionOfTripDemandByTimeSegment>();
        DepartureTimeProfile profile = null;
        Duration duration = null;
        Time segmentStartTime = null;
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
                            fractions = new TreeMap<Time, FractionOfTripDemandByTimeSegment>();
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
                            duration = new Duration((segmentEnd.getTimeInMillis() - segmentStart.getTimeInMillis()) / 1000,
                                    DurationUnit.SECOND);
                        }
                        // detects the specific Time that relates to the next fractions
                        else if (dataItem[0].equals(TIME))
                        {
                            String segmentTime = dataItem[1];
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                            Calendar startTime = Calendar.getInstance();
                            startTime.setTime(sdf.parse(segmentTime));
                            Time prevStartTime = segmentStartTime;
                            segmentStartTime = new Time(startTime.getTimeInMillis() / 1000, TimeUnit.BASE_SECOND);
                            if (prevStartTime != null)
                            {
                                while (segmentStartTime.minus(prevStartTime).doubleValue() > 0)
                                {
                                    FractionOfTripDemandByTimeSegment newFraction =
                                            new FractionOfTripDemandByTimeSegment(segmentStartTime, duration, fraction);
                                    fractions.put(segmentStartTime, newFraction);
                                    prevStartTime = prevStartTime.plus(duration);
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
                                segmentStartTime = segmentStartTime.plus(duration);
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
     * @param csvFileName String;
     * @param csvSplitBy String;
     * @param csvSplitInternalBy String;
     * @return an ArrayList<DepartureTimeProfile<?>>
     * @throws IOException
     * @throws ParseException
     */
    public static LinkedHashMap<String, ArrayList<Double>> readParametersNTM(final String csvFileName, final String csvSplitBy,
            final String csvSplitInternalBy) throws IOException, ParseException
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

        LinkedHashMap<String, ArrayList<Double>> parametersNTM = new LinkedHashMap<String, ArrayList<Double>>();

        if (url != null)
        {
            try
            {
                String path = url.getPath();
                bufferedReader = new BufferedReader(new FileReader(path));
                // read all lines: first column contains the name of the origin
                // this can be either a link or a centroid (starts with "C")
                while ((line = bufferedReader.readLine()) != null)
                {
                    String centroidName;
                    ArrayList<Double> parameters = new ArrayList<Double>();
                    String[] completeLine = line.split(csvSplitBy);
                    for (String lineSegment : completeLine)
                    {
                        lineSegment = lineSegment.trim();
                        String[] dataItem = lineSegment.split(csvSplitInternalBy);
                        if (dataItem.length > 0)
                        {
                            centroidName = dataItem[0];
                            parameters.add(Double.parseDouble(dataItem[1]));
                            parameters.add(Double.parseDouble(dataItem[2]));
                            parameters.add(Double.parseDouble(dataItem[3]));
                            parameters.add(Double.parseDouble(dataItem[4]));
                            parametersNTM.put(centroidName, parameters);
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
        }
        return parametersNTM;
    }

    public static LinkedHashMap<String, LinkedHashMap<String, Frequency>> readCapResNTM(final String csvFileName, final String csvSplitBy,
            final String csvSplitInternalBy) throws IOException, ParseException
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
        LinkedHashMap<String, LinkedHashMap<String, Frequency>> capResMap = new LinkedHashMap<String, LinkedHashMap<String, Frequency>>();
        // double OD = capResMap.get("O").get("D");
        if (url != null)
        {
            try
            {
                String path = url.getPath();
                bufferedReader = new BufferedReader(new FileReader(path));
                boolean header = true;
                // read all lines: first column contains the name of the origin
                // this can be either a link or a centroid (starts with "C")
                LinkedHashMap<Integer, String> name = new LinkedHashMap<Integer, String>();
                while ((line = bufferedReader.readLine()) != null)
                {
                    String centroidName = null;
                    LinkedHashMap<String, Frequency> capRes = new LinkedHashMap<String, Frequency>();
                    String[] completeLine = line.split(csvSplitBy);
                    for (String lineSegment : completeLine)
                    {
                        lineSegment = lineSegment.trim();
                        String[] dataItem = lineSegment.split(csvSplitInternalBy);
                        if (dataItem.length > 0)
                        {
                            if (header)
                            {
                                for (int i = 1; i < dataItem.length; i++)
                                {
                                    name.put(i, dataItem[i].trim());
                                }
                            }
                            else
                            {
                                centroidName = dataItem[0];
                                for (int i = 1; i < dataItem.length; i++)
                                {
                                    Frequency capacity = new Frequency(Double.parseDouble(dataItem[i]), FrequencyUnit.PER_HOUR);
                                    capRes.put(name.get(i), capacity);
                                }
                            }
                        }
                    }
                    if (header)
                    {
                        header = false;
                    }
                    else
                    {
                        capResMap.put(centroidName, capRes);
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
        }
        return capResMap;
    }

    public static LinkedHashMap<String, LinkedHashMap<String, Double>> readCapResFactorNTM(final String csvFileName,
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
        LinkedHashMap<String, LinkedHashMap<String, Double>> capResMap = new LinkedHashMap<String, LinkedHashMap<String, Double>>();
        // double OD = capResMap.get("O").get("D");
        if (url != null)
        {
            try
            {
                String path = url.getPath();
                bufferedReader = new BufferedReader(new FileReader(path));
                boolean header = true;
                // read all lines: first column contains the name of the origin
                // this can be either a link or a centroid (starts with "C")
                LinkedHashMap<Integer, String> name = new LinkedHashMap<Integer, String>();
                while ((line = bufferedReader.readLine()) != null)
                {
                    String centroidName = null;
                    LinkedHashMap<String, Double> capRes = new LinkedHashMap<String, Double>();
                    String[] completeLine = line.split(csvSplitBy);
                    for (String lineSegment : completeLine)
                    {
                        lineSegment = lineSegment.trim();
                        String[] dataItem = lineSegment.split(csvSplitInternalBy);
                        if (dataItem.length > 0)
                        {
                            if (header)
                            {
                                for (int i = 1; i < dataItem.length; i++)
                                {
                                    name.put(i, dataItem[i].trim());
                                }
                            }
                            else
                            {
                                centroidName = dataItem[0];
                                for (int i = 1; i < dataItem.length; i++)
                                {
                                    Double capacityReductionFactor = Double.parseDouble(dataItem[i]);
                                    capRes.put(name.get(i), capacityReductionFactor);
                                }
                            }
                        }
                    }
                    if (header)
                    {
                        header = false;
                    }
                    else
                    {
                        capResMap.put(centroidName, capRes);
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
        }
        return capResMap;
    }

    /**
     * @param name String;
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
     * public static String returnNumber(final String name) { // replace double quotes at start and end of string String nr =
     * removeQuotes(name); if (name.startsWith("Links")) { nr = null; } else { nr = ShapeFileReader.NodeCentroidNumber(nr); }
     * return nr; }
     */

}
