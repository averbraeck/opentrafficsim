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
     * @return the TripDemand (nested HashMap: <origin, map<destination, tripinformation>>
     * @throws IOException
     * @throws Throwable
     */
    public static TripDemand ReadOmnitransExportDemand(final String csvFileName, String csvSplitBy,
            final String csvSplitByTwo) throws Throwable
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
        Map<Long, Map<Long, TripInfo>> demand = new HashMap<Long, Map<Long, TripInfo>>();
        ArrayList<Long> zoneNames = new ArrayList<Long>();
        try
        {

            bufferedReader = new BufferedReader(new FileReader(path));

            // read the first line of the demand file from Omnitrans
            // this line contains the time period of the demand file
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
                            }
                            else if (counter == 1)
                            {
                                DoubleScalar.Abs<TimeUnit> endTime =
                                        new DoubleScalar.Abs<TimeUnit>(timeInstance.getTimeInMillis() / 1000,
                                                TimeUnit.SECOND);
                                DoubleScalar.Rel<TimeUnit> timeSpan =
                                        MutableDoubleScalar.Abs.minus(endTime, tripDemand.getStartTime()).immutable();

                                tripDemand.setTimeSpan(timeSpan);
                            }
                            counter++;
                        }
                    }
                }

            }

            // read the second line of the demand file from Omnitrans
            // this line contains the destinations: put them in the array
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
