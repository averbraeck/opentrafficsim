package org.opentrafficsim.importexport.osm.output;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneAnimation;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30.12.2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a>Moritz Bergmann</a>
 */
public final class Convert
{
    /** Do not instantiate this class. */
    private Convert()
    {
        // Cannot be instantiated.
    }

    /**
     * This method converts an OSM link to an OTS link.
     * @param link OSM Link to be converted
     * @return OTS Link
     */
    public static CrossSectionLink convertLink(final org.opentrafficsim.importexport.osm.Link link)
    {
        NodeGeotools.STR start = convertNode(link.getStart());
        NodeGeotools.STR end = convertNode(link.getEnd());
        CrossSectionLink l2;
        if (link.getSplineList().isEmpty())
        {
            l2 =
                    new CrossSectionLink(link.getID(), start, end, new DoubleScalar.Rel<LengthUnit>(link.getLength(),
                            LengthUnit.METER));
        }
        else
        {
            List<Coordinate> iC = new ArrayList<Coordinate>();
            for (org.opentrafficsim.importexport.osm.Node spline : link.getSplineList())
            {
                Coordinate coord = new Coordinate(spline.getLongitude(), spline.getLatitude());
                iC.add(coord);
            }
            Coordinate[] intermediateCoordinates = new Coordinate[iC.size()];
            iC.toArray(intermediateCoordinates);
            int coordinateCount = 2 + (null == intermediateCoordinates ? 0 : intermediateCoordinates.length);
            Coordinate[] coordinates = new Coordinate[coordinateCount];
            coordinates[0] = new Coordinate(start.getPoint().x, start.getPoint().y, 0);
            coordinates[coordinates.length - 1] = new Coordinate(end.getPoint().x, end.getPoint().y, 0);
            if (null != intermediateCoordinates)
            {
                for (int i = 0; i < intermediateCoordinates.length; i++)
                {
                    coordinates[i + 1] = new Coordinate(intermediateCoordinates[i]);
                }
            }
            GeometryFactory factory = new GeometryFactory();
            LineString lineString = factory.createLineString(coordinates);
            l2 =
                    new CrossSectionLink(link.getID(), start, end, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                            LengthUnit.METER));
            try
            {
                new LinearGeometry(l2, lineString, null);
            }
            catch (NetworkException exception)
            {
                throw new Error("Network exception in LinearGeometry");
            }
        }
        return l2;
    }

    /**
     * This method converts an OSM node to an OTS node.
     * @param node OSM Node to be converted
     * @return OTS Node
     */
    public static NodeGeotools.STR convertNode(final org.opentrafficsim.importexport.osm.Node node)
    {
        Coordinate coord = new Coordinate(node.getLongitude(), node.getLatitude());
        NodeGeotools.STR n2 = new NodeGeotools.STR(Objects.toString(node.getID()), coord);
        return n2;
    }

    /**
     * This method creates lanes out of an OSM link LaneTypes are not jet extensive and can be further increased through
     * Tags provided by OSM. The standard lane width of 3.05 is an estimation based on the Wuropean width limitation for
     * vehicles (2.55m) + 25cm each side.
     * @param osmlink Link; the OSM link to make lanes for
     * @return Lanes
     * @throws NetworkException 
     * @throws NamingException 
     * @throws RemoteException 
     */
    public static List<Lane> makeLanes(final org.opentrafficsim.importexport.osm.Link osmlink, final OTSDEVSSimulatorInterface simulator) throws NetworkException, RemoteException, NamingException
    {
        CrossSectionLink otslink = convertLink(osmlink);
        List<Lane> lanes = new ArrayList<Lane>();
        LaneType<String> lt = null;
        Lane result = null;
        boolean widthOverride = false; /* In case the OSM link provides a width the standard width will be overridden */

        List<Coordinate> iC = new ArrayList<Coordinate>();
        for (org.opentrafficsim.importexport.osm.Node spline : osmlink.getSplineList())
        {
            Coordinate coord = new Coordinate(spline.getLongitude(), spline.getLatitude());
            iC.add(coord);
        }
        Coordinate[] intermediateCoordinates = (Coordinate[]) iC.toArray();

        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(3.05, LengthUnit.METER); /* estimation */
        for (org.opentrafficsim.importexport.osm.Tag t : osmlink.getTags())
        {
            if (t.getKey().equals("highway")
                    && (t.getValue().equals("primary") || t.getValue().equals("secondary")
                            || t.getValue().equals("tertiary") || t.getValue().equals("residential")
                            || t.getValue().equals("trunk") || t.getValue().equals("motorway") || t.getValue().equals(
                            "service")))
            {
                lt = makeLaneType(new GTUType<String>(GTUTypes.CAR.toString()));
            }
            if (t.getKey().equals("highway") && t.getValue().equals("cycleway"))
            {
                lt = makeLaneType(new GTUType<String>(GTUTypes.BIKE.toString()));
                if (!widthOverride)
                {
                    width = new DoubleScalar.Rel<LengthUnit>(0.8, LengthUnit.METER); /* estimation */
                }
            }
            if (t.getKey().equals("highway") && t.getValue().equals("footway"))
            {
                lt = makeLaneType(new GTUType<String>(GTUTypes.PEDESTRIAN.toString()));
                if (!widthOverride)
                {
                    width = new DoubleScalar.Rel<LengthUnit>(0.95, LengthUnit.METER); /* estimation */
                }
            }
            if (t.getKey().equals("width"))
            {
                width =
                        new DoubleScalar.Rel<LengthUnit>(Double.parseDouble(t.getValue()) / osmlink.getLanes(),
                                LengthUnit.METER);
                widthOverride = true;
            }
        }

        if (osmlink.isOneway())
        {
            for (int i = 0; i < osmlink.getLanes(); i++)
            {
                DoubleScalar.Abs<FrequencyUnit> f2000 =
                        new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
                /** temporary */
                DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
                result = new Lane(otslink, latPos, latPos, width, width, lt, LongitudinalDirectionality.FORWARD, f2000);
                lanes.add(result);
            }
        }
        else
        {
            for (int i = 0; i < osmlink.getForwardLanes(); i++) /** Create forward lanes */
            {
                DoubleScalar.Abs<FrequencyUnit> f2000 =
                        new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
                /** temporary */
                DoubleScalar.Rel<LengthUnit> latPos =
                        new DoubleScalar.Rel<LengthUnit>((i) * width.getInUnit(), LengthUnit.METER);
                result = new Lane(otslink, latPos, latPos, width, width, lt, LongitudinalDirectionality.FORWARD, f2000);
                lanes.add(result);
            }
            for (int i = 0; i < (osmlink.getLanes() - osmlink.getForwardLanes()); i++) /** Create backward lanes */
            {
                DoubleScalar.Abs<FrequencyUnit> f2000 =
                        new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
                /** temporary */
                DoubleScalar.Rel<LengthUnit> latPos =
                        new DoubleScalar.Rel<LengthUnit>((i) * width.getInUnit() * (-1), LengthUnit.METER);
                result = new Lane(otslink, latPos, latPos, width, width, lt, LongitudinalDirectionality.BACKWARD, f2000);
                lanes.add(result);
            }
        }
        if (osmlink.getTags().contains(new org.opentrafficsim.importexport.osm.Tag("cycleway", "lane")))
        {
            lt = makeLaneType(new GTUType<String>(GTUTypes.BIKE.toString()));
            DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
            /** temporary */
            DoubleScalar.Rel<LengthUnit> latPos =
                    new DoubleScalar.Rel<LengthUnit>(osmlink.getLanes() * width.getInUnit(), LengthUnit.METER);
            result =
                    new Lane(otslink, latPos, latPos,
                            new DoubleScalar.Rel<LengthUnit>(0.8, LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(0.8, LengthUnit.METER),
                            lt, LongitudinalDirectionality.FORWARD, f2000);
            lanes.add(result);
        }
        if (simulator instanceof OTSAnimatorInterface)
        {
            new LaneAnimation(result, simulator, Color.LIGHT_GRAY);
        }
        return lanes;
    }

    /**
     * This method creates a LaneType which supports all GTUTypes that have been specified in the GTUType List "GTUs".
     * @param gtuTypes List&lt;GTUType&lt;String&gt;&gt;; list of GTUTypes
     * @return LaneType permeable for all of the specific GTUTypes
     */
    public static LaneType<String> makeLaneType(final List<GTUType<String>> gtuTypes)
    {
        String iD = "";
        for (GTUType<String> gtu : gtuTypes)
        {
            iD.concat(gtu.getId());
        }
        LaneType<String> lt = new LaneType<String>(iD);
        for (GTUType<String> gtu : gtuTypes)
        {
            lt.addPermeability(gtu);
        }
        return lt;
    }

    /**
     * This method creates a LaneType which supports the specified GTUType.
     * @param gtuType GTUType; the type of GTU that can travel on the new LaneType
     * @return LaneType
     */
    public static LaneType<String> makeLaneType(final GTUType<String> gtuType)
    {
        String iD = gtuType.getId();
        LaneType<String> lt = new LaneType<String>(iD);
        lt.addPermeability(gtuType);
        return lt;
    }
}
