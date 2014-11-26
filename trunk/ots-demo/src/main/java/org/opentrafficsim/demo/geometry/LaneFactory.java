package org.opentrafficsim.demo.geometry;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.LinearGeometry;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
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
 * @version 30 okt. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class LaneFactory
{
    /** Do not instantiate this class. */
    private LaneFactory()
    {
        // Cannot be instantiated.
    }

    /**
     * Add the LinearGeometry to a Link.
     * @param link Link; the Link that must be fitted with a Geometry
     * @param lineString Coordinate[]; the coordinates of the Geometry
     * @return Link; the updated link
     */
    private static Link makeLink(final String name, final Node from, final Node to,
            final Coordinate[] intermediateCoordinates)
    {
        int coordinateCount = 2 + (null == intermediateCoordinates ? 0 : intermediateCoordinates.length);
        Coordinate[] coordinates = new Coordinate[coordinateCount];
        coordinates[0] = new Coordinate(from.getPoint().x, from.getPoint().y, 0);
        coordinates[coordinates.length - 1] = new Coordinate(to.getPoint().x, to.getPoint().y, 0);
        if (null != intermediateCoordinates)
        {
            for (int i = 0; i < intermediateCoordinates.length; i++)
            {
                coordinates[i + 1] = new Coordinate(intermediateCoordinates[i]);
            }
        }
        GeometryFactory factory = new GeometryFactory();
        LineString lineString = factory.createLineString(coordinates);
        Link link =
                new Link(name, from, to, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(), LengthUnit.METER));
        try
        {
            new LinearGeometry(link, lineString, null);
        }
        catch (NetworkException exception)
        {
            throw new Error("Network exception in LinearGeometry");
        }
        return link;
    }

    /**
     * Create one Lane.
     * @param link Link; the link that owns the new Lane
     * @param laneType LaneType&lt;String&gt;; the type of the new Lane
     * @param latPos DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral position of the new Lane with respect to the design
     *            line of the link
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the width of the new Lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane
     * @throws RemoteException on communications failure
     * @throws NamingException on ???
     */
    private static Lane makeLane(Link link, LaneType<String> laneType, DoubleScalar.Rel<LengthUnit> latPos,
            DoubleScalar.Rel<LengthUnit> width, OTSDEVSSimulatorInterface simulator) throws RemoteException,
            NamingException
    {
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        Lane result = new Lane(link, latPos, width, width, laneType, LongitudinalDirectionality.FORWARD, f2000);
        if (simulator instanceof OTSAnimatorInterface)
        {
            new LaneAnimation(result, simulator, Color.LIGHT_GRAY);
        }
        return result;
    }

    /**
     * Create a simple, straight Lane.
     * @param name String; name of the Lane (and also of the Link that owns it)
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediateCoordinates Coordinate[]; intermediate coordinates or null to create a straight road
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane; the new Lane
     * @throws NamingException
     * @throws RemoteException
     */
    public static Lane makeLane(final String name, final Node from, final Node to,
            Coordinate[] intermediateCoordinates, OTSDEVSSimulatorInterface simulator) throws RemoteException,
            NamingException
    {
        LaneType<String> carLaneType = new LaneType<String>("car");
        DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        final Link link = makeLink(name, from, to, intermediateCoordinates);
        return makeLane(link, carLaneType, latPos, width, simulator);
    }

    /**
     * Create a simple straight road with the specified number of Lanes.
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediateCoordinates Coordinate[]; intermediate coordinates or null to create a straight road
     * @param laneCount int; number of lanes in the road
     * @param laneType LaneType; type of the new Lanes
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane[]; array containing the new Lanes
     * @throws NamingException
     * @throws RemoteException
     */
    public static Lane[] makeMultiLane(final String name, final Node from, final Node to, Coordinate[] intermediateCoordinates,
            int laneCount, LaneType<String> laneType, OTSDEVSSimulatorInterface simulator) throws RemoteException, NamingException
    {
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(laneCount * 4.0, LengthUnit.METER);
        final Link link = makeLink(name, from, to, null);
        Lane[] result = new Lane[laneCount];
        width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            DoubleScalar.Rel<LengthUnit> latPos =
                    new DoubleScalar.Rel<LengthUnit>((-0.5 - laneIndex) * width.getSI(), LengthUnit.METER);
            result[laneIndex] = makeLane(link, laneType, latPos, width, simulator);
        }
        return result;
    }

}
