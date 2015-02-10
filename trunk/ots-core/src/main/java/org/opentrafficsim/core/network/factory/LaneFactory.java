package org.opentrafficsim.core.network.factory;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.LateralDirectionality;
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
     * Create a Link along intermediate coordinates from one Node to another.
     * @param name String; name of the new Link
     * @param from Node; start Node of the new Link
     * @param to Node; end Node of the new Link
     * @param intermediateCoordinates Coordinate[]; array of intermediate coordinates (may be null)
     * @return Link; the newly constructed Link
     */
    public static CrossSectionLink makeLink(final String name, final NodeGeotools.STR from, final NodeGeotools.STR to,
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
        CrossSectionLink link =
                new CrossSectionLink(name, from, to, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                        LengthUnit.METER));
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
     * @throws NamingException when names cannot be registered for animation
     * @throws RemoteException on communications failure
     * @throws NetworkException
     */
    private static Lane makeLane(final CrossSectionLink link, final LaneType<String> laneType,
            final DoubleScalar.Rel<LengthUnit> latPos, final DoubleScalar.Rel<LengthUnit> width,
            final OTSDEVSSimulatorInterface simulator) throws RemoteException, NamingException, NetworkException
    {
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        Lane result = new Lane(link, latPos, latPos, width, width, laneType, LongitudinalDirectionality.FORWARD, f2000);
        if (simulator instanceof OTSAnimatorInterface)
        {
            new LaneAnimation(result, simulator, Color.LIGHT_GRAY);
        }
        return result;
    }

    /**
     * Create a simple Lane.
     * @param name String; name of the Lane (and also of the Link that owns it)
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediateCoordinates Coordinate[]; intermediate coordinates or null to create a straight road
     * @param laneType LaneType; type of the new Lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane; the new Lane
     * @throws NamingException when names cannot be registered for animation
     * @throws RemoteException on communications failure
     * @throws NetworkException
     */
    public static Lane makeLane(final String name, final NodeGeotools.STR from, final NodeGeotools.STR to,
            final Coordinate[] intermediateCoordinates, final LaneType<String> laneType,
            final OTSDEVSSimulatorInterface simulator) throws RemoteException, NamingException, NetworkException
    {
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        final CrossSectionLink link = makeLink(name, from, to, intermediateCoordinates);
        DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
        return makeLane(link, laneType, latPos, width, simulator);
    }

    /**
     * Create a simple road with the specified number of Lanes.<br/>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the
     * getParentLink method of the Lane.
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediateCoordinates Coordinate[]; intermediate coordinates or null to create a straight road
     * @param laneCount int; number of lanes in the road
     * @param laneType LaneType; type of the new Lanes
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @return Lane[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws RemoteException on communications failure
     * @throws NetworkException on topological problems
     */
    public static Lane[] makeMultiLane(final String name, final NodeGeotools.STR from, final NodeGeotools.STR to,
            final Coordinate[] intermediateCoordinates, final int laneCount, final LaneType<String> laneType,
            final OTSDEVSSimulatorInterface simulator) throws RemoteException, NamingException, NetworkException
    {
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(laneCount * 4.0, LengthUnit.METER);
        final CrossSectionLink link = makeLink(name, from, to, intermediateCoordinates);
        Lane[] result = new Lane[laneCount];
        width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            DoubleScalar.Rel<LengthUnit> latPos =
                    new DoubleScalar.Rel<LengthUnit>((-0.5 - laneIndex) * width.getSI(), LengthUnit.METER);
            result[laneIndex] = makeLane(link, laneType, latPos, width, simulator);
        }
        // Make lanes adjacent in their natural order
        for (int laneIndex = 1; laneIndex < laneCount; laneIndex++)
        {
            result[laneIndex - 1].addAccessibleAdjacentLane(result[laneIndex], LateralDirectionality.RIGHT);
            result[laneIndex].addAccessibleAdjacentLane(result[laneIndex - 1], LateralDirectionality.LEFT);
        }
        return result;
    }

}
