package org.opentrafficsim.car.lanechanging;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.lanechanging.LaneChangeModel.LaneChangeModelResult;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.AbstractNode;
import org.opentrafficsim.core.network.CrossSectionLink;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinearGeometry;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.simulationengine.FakeSimulator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Test some very basic properties of lane change models.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneChangeModelTest
{
    /**
     * Create a Link.
     * @param name String; name of the new Link
     * @param from Node; start node of the new Link
     * @param to Node; end node of the new Link
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the width of the new Link
     * @return Link
     */
    private static Link makeLink(final String name, final Node from, final Node to, DoubleScalar.Rel<LengthUnit> width)
    {
        // TODO create a LinkAnimation if the simulator is compatible with that.
        // FIXME The current LinkAnimation is too bad to use...
        Link link =
                new Link(name, from, to, new DoubleScalar.Rel<LengthUnit>(from.getPoint().distance(to.getPoint()),
                        LengthUnit.METER));
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates =
                new Coordinate[]{new Coordinate(from.getPoint().x, from.getPoint().y, 0),
                        new Coordinate(to.getPoint().x, to.getPoint().y, 0)};
        LineString line = factory.createLineString(coordinates);
        try
        {
            new LinearGeometry(link, line, null);
        }
        catch (NetworkException exception)
        {
            throw new Error("This network is probably too simple for this to happen...");
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
     * @return Lane
     * @throws RemoteException on communications failure
     * @throws NamingException on ???
     */
    private static Lane makeLane(Link link, LaneType<String> laneType, DoubleScalar.Rel<LengthUnit> latPos,
            DoubleScalar.Rel<LengthUnit> width) throws RemoteException, NamingException
    {
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        Lane result = new Lane(link, latPos, width, width, laneType, LongitudinalDirectionality.FORWARD, f2000);
        return result;
    }

    /**
     * Create a simple straight road with the specified number of Lanes.
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param laneType LaneType&lt;String&gt;; the type of GTU that can use the lanes
     * @param laneCount int; number of lanes in the road
     * @return Lane[]; array containing the new Lanes
     * @throws NamingException
     * @throws RemoteException
     */
    public static Lane[] makeMultiLane(final String name, final Node from, final Node to, LaneType<String> laneType,
            int laneCount) throws RemoteException, NamingException
    {
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(laneCount * 4.0, LengthUnit.METER);
        final Link link = makeLink(name, from, to, width);
        Lane[] result = new Lane[laneCount];
        width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            DoubleScalar.Rel<LengthUnit> latPos =
                    new DoubleScalar.Rel<LengthUnit>((-0.5 - laneIndex) * width.getSI(), LengthUnit.METER);
            result[laneIndex] = makeLane(link, laneType, latPos, width);
        }
        return result;
    }

    /**
     * Test that a vehicle in the left lane changes to the right lane if that is empty, or there is enough room.
     * @throws RemoteException on communications failure
     * @throws NamingException on ???
     * @throws SimRuntimeException on ???
     * @throws NetworkException on Network inconsistency
     */
    @Test
    public final void changeRight() throws RemoteException, NamingException, SimRuntimeException, NetworkException
    {
        GTUType<String> gtuType = new GTUType<String>("car");
        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addPermeability(gtuType);
        Lane[] lanes =
                makeMultiLane("Road with two lanes", new Node("From", new Coordinate(0, 0, 0)), new Node("To",
                        new Coordinate(200, 0, 0)), laneType, 2);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions =
                new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialLongitudinalPositions.put(lanes[0], new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER));
        OTSDEVSSimulatorInterface fakeSimulator = new FakeSimulator();
        Car<String> car =
                new Car<String>("ReferenceCar", gtuType, new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(150,
                                SpeedUnit.KM_PER_HOUR), new IDMPlus(null), initialLongitudinalPositions,
                        new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), fakeSimulator);
        Collection<AbstractLaneBasedGTU<?>> sameLaneGTUs = new HashSet<AbstractLaneBasedGTU<?>>();
        sameLaneGTUs.add(car);
        Collection<AbstractLaneBasedGTU<?>> preferredLaneGTUs = new HashSet<AbstractLaneBasedGTU<?>>();
        Collection<AbstractLaneBasedGTU<?>> nonPreferredLaneGTUs = new HashSet<AbstractLaneBasedGTU<?>>();
        LaneChangeModelResult laneChangeModelResult =
                new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                        nonPreferredLaneGTUs, new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR),
                        new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(laneChangeModelResult.toString());
        assertEquals("Vehicle want to change to the right lane", LateralDirectionality.RIGHT,
                laneChangeModelResult.getLaneChange());
        DoubleScalar.Rel<LengthUnit> rear = car.positionOfRear(lanes[0]);
        DoubleScalar.Rel<LengthUnit> front = car.positionOfFront(lanes[0]);
        DoubleScalar.Rel<LengthUnit> reference = car.getLongitudinalPositions().get(lanes[0]);
        System.out.println("rear:      " + rear);
        System.out.println("front:     " + front);
        System.out.println("reference: " + reference);
        DoubleScalar.Rel<LengthUnit> vehicleLength = DoubleScalar.minus(front, rear).immutable();
        DoubleScalar.Rel<LengthUnit> collisionStart = DoubleScalar.minus(reference, vehicleLength).immutable();
        DoubleScalar.Rel<LengthUnit> collisionEnd = DoubleScalar.plus(reference, vehicleLength).immutable();
        for (double pos = collisionStart.getSI() + 0.01; pos < collisionEnd.getSI() - 0.01; pos += 0.1)
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> otherLongitudinalPositions =
                    new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            otherLongitudinalPositions.put(lanes[1], new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER));
            Car<String> collisionCar =
                    new Car<String>("LaneChangeBlockingCar", gtuType, vehicleLength,
                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(150,
                                    SpeedUnit.KM_PER_HOUR), new IDMPlus(null), otherLongitudinalPositions,
                            new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), fakeSimulator);
            preferredLaneGTUs.clear();
            preferredLaneGTUs.add(collisionCar);
            laneChangeModelResult =
                    new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                            nonPreferredLaneGTUs, new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR),
                            new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2));
            System.out.println(laneChangeModelResult.toString());
            assertEquals(
                    "Vehicle cannot to change to the right lane because that would result in an immediate collision",
                    null, laneChangeModelResult.getLaneChange());
        }
        for (double pos = 0; pos < 200; pos += 5)
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> otherLongitudinalPositions =
                    new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            otherLongitudinalPositions.put(lanes[1], new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER));
            Car<String> otherCar =
                    new Car<String>("OtherCar", gtuType, vehicleLength,
                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(150,
                                    SpeedUnit.KM_PER_HOUR), new IDMPlus(null), otherLongitudinalPositions,
                            new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), fakeSimulator);
            preferredLaneGTUs.clear();
            preferredLaneGTUs.add(otherCar);
            laneChangeModelResult =
                    new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                            nonPreferredLaneGTUs, new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR),
                            new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2));
            System.out.println(String.format("pos=%5fm Egoistic:   %s", pos, laneChangeModelResult.toString()));
            laneChangeModelResult =
                    new Altruistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                            nonPreferredLaneGTUs, new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR),
                            new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2));
            System.out.println(String.format("pos=%5fm Altruistic: %s", pos, laneChangeModelResult.toString()));
//            assertEquals(
//                    "Vehicle cannot to change to the right lane because that would result in an immediate collision",
//                    null, laneChangeModelResult.getLaneChange());
        }
    }
    
    // TODO: test/prove the expected differences between Egoistic and Altruistic
    // TODO: prove that the most restrictive car in the other lane determines what happens
    // TODO: test merge into overtaking lane
    
}

/**
 * Local Node class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class Node extends AbstractNode<String, Coordinate>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param id id
     * @param coordinate coordinate
     */
    public Node(final String id, final Coordinate coordinate)
    {
        super(id, coordinate);
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(getPoint().x, getPoint().y, 0.0);
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(0.1, 0.1, 0.0);
    }
}

/**
 * Local Link class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class Link extends CrossSectionLink<String, Node> implements LocatableInterface
{
    /** */
    private static final long serialVersionUID = 20140921L;

    /** speed. */
    private double speed;

    /**
     * @param id the id of the link (String).
     * @param nodeA the start node of the link.
     * @param nodeB the end node of the link.
     * @param length the length of the link with a unit.
     */
    public Link(final String id, final Node nodeA, final Node nodeB, final DoubleScalar.Rel<LengthUnit> length)
    {
        super(id, nodeA, nodeB, length);
    }

    /**
     * @return speed
     */
    public final double getSpeed()
    {
        return this.speed;
    }

    /**
     * @param speed set speed
     */
    public final void setSpeed(final double speed)
    {
        this.speed = speed;
    }

}
