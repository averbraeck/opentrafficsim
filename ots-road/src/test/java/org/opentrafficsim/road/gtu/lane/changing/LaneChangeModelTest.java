package org.opentrafficsim.road.gtu.lane.changing;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.Altruistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechange.LaneMovementStep;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test some very basic properties of lane change models.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version 14 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneChangeModelTest implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20150313;

    /** network. */
    private OTSNetwork network = new OTSNetwork("network");
    
    /**
     * Create a Link.
     * @param name String; name of the new Link
     * @param from Node; start node of the new Link
     * @param to Node; end node of the new Link
     * @param width Length.Rel; the width of the new Link
     * @return Link
     * @throws NetworkException
     */
    private static CrossSectionLink makeLink(final String name, final OTSNode from, final OTSNode to,
        final Length.Rel width) throws NetworkException
    {
        // TODO create a LinkAnimation if the simulator is compatible with that.
        // FIXME The current LinkAnimation is too bad to use...
        OTSPoint3D[] coordinates =
            new OTSPoint3D[]{new OTSPoint3D(from.getPoint().x, from.getPoint().y, 0),
                new OTSPoint3D(to.getPoint().x, to.getPoint().y, 0)};
        OTSLine3D line = new OTSLine3D(coordinates);
        CrossSectionLink link =
            new CrossSectionLink(name, from, to, LinkType.ALL, line, LongitudinalDirectionality.DIR_PLUS,
                LaneKeepingPolicy.KEEP_RIGHT);
        return link;
    }

    /**
     * Create one Lane.
     * @param link Link; the link that owns the new Lane
     * @param id String; the id of the lane, has to be unique within the link
     * @param laneType LaneType&lt;String&gt;; the type of the new Lane
     * @param latPos Length.Rel; the lateral position of the new Lane with respect to the design line of the link
     * @param width Length.Rel; the width of the new Lane
     * @return Lane
     * @throws NamingException on ???
     * @throws NetworkException on ??
     * @throws OTSGeometryException when center line or contour of a link or lane cannot be generated
     */
    private static Lane makeLane(final CrossSectionLink link, final String id, final LaneType laneType,
        final Length.Rel latPos, final Length.Rel width) throws NamingException, NetworkException, OTSGeometryException
    {
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new LinkedHashMap<>();
        directionalityMap.put(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
        Map<GTUType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(GTUType.ALL, new Speed(100, KM_PER_HOUR));
        // XXX Decide what type of overtaking conditions we want in this test
        Lane result =
            new Lane(link, id, latPos, latPos, width, width, laneType, directionalityMap, speedMap,
                new OvertakingConditions.LeftAndRight());
        return result;
    }

    /**
     * Create a simple straight road with the specified number of Lanes.
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param laneType LaneType&lt;String&gt;; the type of GTU that can use the lanes
     * @param laneCount int; number of lanes in the road
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws Exception when something goes wrong (should not happen)
     */
    public static Lane[] makeMultiLane(final String name, final OTSNode from, final OTSNode to,
        final LaneType laneType, final int laneCount) throws Exception
    {
        Length.Rel width = new Length.Rel(laneCount * 4.0, METER);
        final CrossSectionLink link = makeLink(name, from, to, width);
        Lane[] result = new Lane[laneCount];
        width = new Length.Rel(4.0, METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // successive lanes have a more negative offset => more to the RIGHT
            Length.Rel latPos = new Length.Rel((-0.5 - laneIndex) * width.getSI(), METER);
            result[laneIndex] = makeLane(link, "lane." + laneIndex, laneType, latPos, width);
        }
        return result;
    }

    /**
     * Test that a vehicle in the left lane changes to the right lane if that is empty, or there is enough room.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void changeRight() throws Exception
    {
        GTUType gtuType = GTUType.makeGTUType("car");
        LaneType laneType = new LaneType("CarLane");
        laneType.addCompatibility(gtuType);
        int laneCount = 2;
        Lane[] lanes =
            makeMultiLane("Road with two lanes", new OTSNode("From", new OTSPoint3D(0, 0, 0)), new OTSNode("To",
                new OTSPoint3D(200, 0, 0)), laneType, laneCount);

        // Let's see if adjacent lanes are accessible
        // lanes: | 0 : 1 : 2 | in case of three lanes
        lanes[0].accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType);
        assertEquals("Leftmost lane should not have accessible adjacent lanes on the LEFT side", 0, lanes[0]
            .accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType).size());
        assertEquals("Leftmost lane should have one accessible adjacent lane on the RIGHT side", 1, lanes[0]
            .accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType).size());
        assertEquals("Rightmost lane should have one accessible adjacent lane on the LEFT side", 1, lanes[1]
            .accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType).size());
        assertEquals("Rightmost lane should not have accessible adjacent lanes on the RIGHT side", 0, lanes[1]
            .accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType).size());

        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lanes[0], new Length.Rel(100, METER),
            GTUDirectionality.DIR_PLUS));
        SimpleSimulator simpleSimulator =
            new SimpleSimulator(new Time.Abs(0, SECOND), new Time.Rel(0, SECOND), new Time.Rel(3600, SECOND), this
            /*
             * CRASH - FIXME - will have to wait for Network factory
             */);
        AbstractLaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedDrivingCharacteristics drivingCharacteristics =
            new LaneBasedDrivingCharacteristics(new IDMPlus(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(
                1.5, METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d), laneChangeModel);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(drivingCharacteristics);
        LaneBasedIndividualCar car =
            new LaneBasedIndividualCar("ReferenceCar", gtuType, initialLongitudinalPositions, new Speed(100,
                KM_PER_HOUR), new Length.Rel(4, METER), new Length.Rel(2, METER), new Speed(150, KM_PER_HOUR),
                simpleSimulator, strategicalPlanner, new LanePerception(), this.network);
        Collection<HeadwayGTU> sameLaneGTUs = new LinkedHashSet<HeadwayGTU>();
        sameLaneGTUs.add(new HeadwayGTU(car, 0));
        Collection<HeadwayGTU> preferredLaneGTUs = new LinkedHashSet<HeadwayGTU>();
        Collection<HeadwayGTU> nonPreferredLaneGTUs = new LinkedHashSet<HeadwayGTU>();
        LaneMovementStep laneChangeModelResult =
            laneChangeModel.computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                nonPreferredLaneGTUs, new Speed(100, KM_PER_HOUR), new Acceleration(0.3, METER_PER_SECOND_2),
                new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
        // System.out.println(laneChangeModelResult.toString());
        assertEquals("Vehicle want to change to the right lane", LateralDirectionality.RIGHT, laneChangeModelResult
            .getLaneChange());
        Length.Rel rear = car.position(lanes[0], car.getRear());
        Length.Rel front = car.position(lanes[0], car.getFront());
        Length.Rel reference = car.position(lanes[0], RelativePosition.REFERENCE_POSITION);
        // System.out.println("rear:      " + rear);
        // System.out.println("front:     " + front);
        // System.out.println("reference: " + reference);
        Length.Rel vehicleLength = front.minus(rear);
        Length.Rel collisionStart = reference.minus(vehicleLength);
        Length.Rel collisionEnd = reference.plus(vehicleLength);
        for (double pos = collisionStart.getSI() + 0.01; pos < collisionEnd.getSI() - 0.01; pos += 0.1)
        {
            Set<DirectedLanePosition> otherLongitudinalPositions = new LinkedHashSet<>(1);
            initialLongitudinalPositions.add(new DirectedLanePosition(lanes[1], new Length.Rel(pos, METER),
                GTUDirectionality.DIR_PLUS));

            drivingCharacteristics =
                new LaneBasedDrivingCharacteristics(new IDMPlus(new Acceleration(1, METER_PER_SECOND_2),
                    new Acceleration(1.5, METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d),
                    laneChangeModel);
            strategicalPlanner = new LaneBasedStrategicalRoutePlanner(drivingCharacteristics);
            LaneBasedIndividualCar collisionCar =
                new LaneBasedIndividualCar("LaneChangeBlockingCar", gtuType, otherLongitudinalPositions, new Speed(100,
                    KM_PER_HOUR), vehicleLength, new Length.Rel(2, METER), new Speed(150, KM_PER_HOUR),
                    simpleSimulator, strategicalPlanner, new LanePerception(), this.network);
            preferredLaneGTUs.clear();
            HeadwayGTU collisionHWGTU = new HeadwayGTU(collisionCar, pos - reference.getSI());
            preferredLaneGTUs.add(collisionHWGTU);
            laneChangeModelResult =
                new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                    nonPreferredLaneGTUs, new Speed(100, KM_PER_HOUR), new Acceleration(0.3, METER_PER_SECOND_2),
                    new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
            // System.out.println(laneChangeModelResult.toString());
            assertEquals(
                "Vehicle cannot to change to the right lane because that would result in an immediate collision", null,
                laneChangeModelResult.getLaneChange());
        }
        for (double pos = 0; pos < 200; pos += 5)
        {
            Set<DirectedLanePosition> otherLongitudinalPositions = new LinkedHashSet<>(1);
            initialLongitudinalPositions.add(new DirectedLanePosition(lanes[1], new Length.Rel(pos, METER),
                GTUDirectionality.DIR_PLUS));

            drivingCharacteristics =
                new LaneBasedDrivingCharacteristics(new IDMPlus(new Acceleration(1, METER_PER_SECOND_2),
                    new Acceleration(1.5, METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d),
                    laneChangeModel);
            strategicalPlanner = new LaneBasedStrategicalRoutePlanner(drivingCharacteristics);
            LaneBasedIndividualCar otherCar =
                new LaneBasedIndividualCar("OtherCar", gtuType, otherLongitudinalPositions,
                    new Speed(100, KM_PER_HOUR), vehicleLength, new Length.Rel(2, METER), new Speed(150, KM_PER_HOUR),
                    simpleSimulator, strategicalPlanner, new LanePerception(), this.network);
            preferredLaneGTUs.clear();
            HeadwayGTU collisionHWGTU =
                new HeadwayGTU(otherCar, pos - car.position(lanes[0], car.getReference()).getSI());
            preferredLaneGTUs.add(collisionHWGTU);
            laneChangeModelResult =
                new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                    nonPreferredLaneGTUs, new Speed(100, KM_PER_HOUR), new Acceleration(0.3, METER_PER_SECOND_2),
                    new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
            // System.out.println(String.format("pos=%5fm Egoistic:   %s", pos, laneChangeModelResult.toString()));
            laneChangeModelResult =
                new Altruistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                    nonPreferredLaneGTUs, new Speed(100, KM_PER_HOUR), new Acceleration(0.3, METER_PER_SECOND_2),
                    new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
            // System.out.println(String.format("pos=%5fm Altruistic: %s", pos, laneChangeModelResult.toString()));
            // assertEquals(
            // "Vehicle cannot to change to the right lane because that would result in an immediate collision",
            // null, laneChangeModelResult.getLaneChange());
        }
    }

    // TODO test/prove the expected differences between Egoistic and Altruistic
    // TODO prove that the most restrictive car in the other lane determines what happens
    // TODO test merge into overtaking lane

    /** {@inheritDoc} */
    @Override
    public void constructModel(
        SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
        throws SimRuntimeException
    {
        // DO NOTHING
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()

    {
        return null;
    }

}
