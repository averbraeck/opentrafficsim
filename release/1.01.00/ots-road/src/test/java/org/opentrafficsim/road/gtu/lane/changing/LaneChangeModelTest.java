package org.opentrafficsim.road.gtu.lane.changing;

import static org.junit.Assert.assertEquals;
import static org.opentrafficsim.core.gtu.GTUType.CAR;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Altruistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneMovementStep;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test some very basic properties of lane change models.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-16 19:20:07 +0200 (Wed, 16 Sep 2015) $, @version $Revision: 1405 $, by $Author: averbraeck $,
 * initial version 14 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneChangeModelTest extends AbstractOTSModel implements UNITS
{
    /** */
    private static final long serialVersionUID = 20150313;

    /** The network. */
    private OTSNetwork network = new OTSNetwork("lane change model test network");

    /**
     */
    public LaneChangeModelTest()
    {
        super(new OTSSimulator());
    }

    /**
     * Create a Link.
     * @param network Network; the network
     * @param name String; name of the new Link
     * @param from Node; start node of the new Link
     * @param to Node; end node of the new Link
     * @param width Length; the width of the new Link
     * @param simulator OTSSimulatorInterface; the simulator
     * @return Link
     * @throws OTSGeometryException when coordinates cannot be calculated
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network
     */
    private static CrossSectionLink makeLink(final Network network, final String name, final OTSNode from, final OTSNode to,
            final Length width, final OTSSimulatorInterface simulator) throws OTSGeometryException, NetworkException
    {
        // TODO create a LinkAnimation if the simulator is compatible with that.
        // FIXME The current LinkAnimation is too bad to use...
        OTSPoint3D[] coordinates = new OTSPoint3D[] { new OTSPoint3D(from.getPoint().x, from.getPoint().y, 0),
                new OTSPoint3D(to.getPoint().x, to.getPoint().y, 0) };
        OTSLine3D line = new OTSLine3D(coordinates);
        CrossSectionLink link =
                new CrossSectionLink(network, name, from, to, LinkType.ROAD, line, simulator, LaneKeepingPolicy.KEEP_RIGHT);
        return link;
    }

    /**
     * Create one Lane.
     * @param link Link; the link that owns the new Lane
     * @param id String; the id of the lane, has to be unique within the link
     * @param laneType LaneType&lt;String&gt;; the type of the new Lane
     * @param latPos Length; the lateral position of the new Lane with respect to the design line of the link
     * @param width Length; the width of the new Lane
     * @return Lane
     * @throws NamingException on ???
     * @throws NetworkException on ??
     * @throws OTSGeometryException when center line or contour of a link or lane cannot be generated
     */
    private static Lane makeLane(final CrossSectionLink link, final String id, final LaneType laneType, final Length latPos,
            final Length width) throws NamingException, NetworkException, OTSGeometryException
    {
        Map<GTUType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(GTUType.VEHICLE, new Speed(100, KM_PER_HOUR));
        // XXX Decide what type of overtaking conditions we want in this test
        Lane result =
                new Lane(link, id, latPos, latPos, width, width, laneType, speedMap, new OvertakingConditions.LeftAndRight());
        return result;
    }

    /**
     * Create a simple straight road with the specified number of Lanes.
     * @param network Network; the network
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param laneType LaneType&lt;String&gt;; the type of GTU that can use the lanes
     * @param laneCount int; number of lanes in the road
     * @param simulator OTSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws Exception when something goes wrong (should not happen)
     */
    public static Lane[] makeMultiLane(final Network network, final String name, final OTSNode from, final OTSNode to,
            final LaneType laneType, final int laneCount, final OTSSimulatorInterface simulator) throws Exception
    {
        Length width = new Length(laneCount * 4.0, METER);
        final CrossSectionLink link = makeLink(network, name, from, to, width, simulator);
        Lane[] result = new Lane[laneCount];
        width = new Length(4.0, METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // successive lanes have a more negative offset => more to the RIGHT
            Length latPos = new Length((-0.5 - laneIndex) * width.getSI(), METER);
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
        GTUType gtuType = CAR;
        LaneType laneType = LaneType.TWO_WAY_LANE;
        int laneCount = 2;
        this.simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), this);
        Lane[] lanes =
                makeMultiLane(this.network, "Road with two lanes", new OTSNode(this.network, "From", new OTSPoint3D(0, 0, 0)),
                        new OTSNode(this.network, "To", new OTSPoint3D(200, 0, 0)), laneType, laneCount, this.simulator);

        // Let's see if adjacent lanes are accessible
        // lanes: | 0 : 1 : 2 | in case of three lanes
        assertEquals("Leftmost lane should not have accessible adjacent lanes on the LEFT side", 0,
                lanes[0].accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtuType, GTUDirectionality.DIR_PLUS).size());
        assertEquals("Leftmost lane should have one accessible adjacent lane on the RIGHT side", 1,
                lanes[0].accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtuType, GTUDirectionality.DIR_PLUS).size());
        assertEquals("Rightmost lane should have one accessible adjacent lane on the LEFT side", 1,
                lanes[1].accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtuType, GTUDirectionality.DIR_PLUS).size());
        assertEquals("Rightmost lane should not have accessible adjacent lanes on the RIGHT side", 0,
                lanes[1].accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtuType, GTUDirectionality.DIR_PLUS).size());

        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions
                .add(new DirectedLanePosition(lanes[1], new Length(100, METER), GTUDirectionality.DIR_PLUS));
        AbstractLaneChangeModel laneChangeModel = new Egoistic();
        ParameterSet parameters = DefaultTestParameters.create();
        // LaneBasedBehavioralCharacteristics drivingCharacteristics =
        // new LaneBasedBehavioralCharacteristics(new IDMPlusOld(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(
        // 1.5, METER_PER_SECOND_2), new Length(2, METER), new Duration(1, SECOND), 1d), laneChangeModel);
        LaneBasedIndividualGTU car = new LaneBasedIndividualGTU("ReferenceCar", gtuType, new Length(4, METER),
                new Length(2, METER), new Speed(150, KM_PER_HOUR), Length.createSI(2.0), this.simulator, this.network);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedCFLCTacticalPlanner(new IDMPlusOld(), laneChangeModel, car), car);
        car.setParameters(parameters);
        car.init(strategicalPlanner, initialLongitudinalPositions, new Speed(100, KM_PER_HOUR));
        car.getTacticalPlanner().getPerception().perceive();
        Collection<Headway> sameLaneGTUs = new LinkedHashSet<>();
        sameLaneGTUs.add(new HeadwayGTUSimple(car.getId(), car.getGTUType(), Length.ZERO, Length.ZERO, car.getLength(),
                car.getSpeed(), car.getAcceleration(), null));
        Collection<Headway> preferredLaneGTUs = new LinkedHashSet<>();
        Collection<Headway> nonPreferredLaneGTUs = new LinkedHashSet<>();
        LaneMovementStep laneChangeModelResult = laneChangeModel.computeLaneChangeAndAcceleration(car, sameLaneGTUs,
                preferredLaneGTUs, nonPreferredLaneGTUs, new Speed(100, KM_PER_HOUR), new Acceleration(0.3, METER_PER_SECOND_2),
                new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
        // System.out.println(laneChangeModelResult.toString());
        assertEquals("Vehicle want to change to the right lane", LateralDirectionality.RIGHT,
                laneChangeModelResult.getLaneChangeDirection());
        Length rear = car.position(lanes[0], car.getRear());
        Length front = car.position(lanes[0], car.getFront());
        Length reference = car.position(lanes[0], RelativePosition.REFERENCE_POSITION);
        // System.out.println("rear: " + rear);
        // System.out.println("front: " + front);
        // System.out.println("reference: " + reference);
        Length vehicleLength = front.minus(rear);
        Length collisionStart = reference.minus(vehicleLength);
        Length collisionEnd = reference.plus(vehicleLength);
        for (double pos = collisionStart.getSI() + 0.01; pos < collisionEnd.getSI() - 0.01; pos += 0.1)
        {
            Set<DirectedLanePosition> otherLongitudinalPositions = new LinkedHashSet<>(1);
            otherLongitudinalPositions
                    .add(new DirectedLanePosition(lanes[1], new Length(pos, METER), GTUDirectionality.DIR_PLUS));

            parameters = DefaultTestParameters.create();
            // parameters = new BehavioralCharacteristics();
            // parameters.setParameter(ParameterTypes.A, new Acceleration(1, METER_PER_SECOND_2));
            // parameters.setParameter(ParameterTypes.B, new Acceleration(1.5, METER_PER_SECOND_2));
            // parameters.setParameter(ParameterTypes.S0, new Length(2, METER));
            // parameters.setParameter(ParameterTypes.T, new Duration(1, SECOND));
            // parameters.setParameter(ParameterTypes.A, new Acceleration(1, METER_PER_SECOND_2));
            // parameters.setParameter(AbstractIDM.DELTA, 1d);
            // drivingCharacteristics =
            // new LaneBasedBehavioralCharacteristics(new IDMPlusOld(new Acceleration(1, METER_PER_SECOND_2),
            // new Acceleration(1.5, METER_PER_SECOND_2), new Length(2, METER), new Duration(1, SECOND), 1d),
            // laneChangeModel);
            LaneBasedIndividualGTU collisionCar =
                    new LaneBasedIndividualGTU("LaneChangeBlockingCarAt" + pos, gtuType, vehicleLength, new Length(2, METER),
                            new Speed(150, KM_PER_HOUR), vehicleLength.multiplyBy(0.5), this.simulator, this.network);
            strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                    new LaneBasedCFLCTacticalPlanner(new IDMPlusOld(), laneChangeModel, collisionCar), collisionCar);
            collisionCar.setParameters(parameters);
            collisionCar.init(strategicalPlanner, otherLongitudinalPositions, new Speed(100, KM_PER_HOUR));
            preferredLaneGTUs.clear();
            HeadwayGTUSimple collisionHWGTU = new HeadwayGTUSimple(collisionCar.getId(), collisionCar.getGTUType(),
                    new Length(pos - reference.getSI(), LengthUnit.SI), collisionCar.getLength(), collisionCar.getWidth(),
                    collisionCar.getSpeed(), collisionCar.getAcceleration(), null);
            preferredLaneGTUs.add(collisionHWGTU);
            laneChangeModelResult = new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                    nonPreferredLaneGTUs, new Speed(100, KM_PER_HOUR), new Acceleration(0.3, METER_PER_SECOND_2),
                    new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
            // System.out.println(laneChangeModelResult.toString());
            assertEquals("Vehicle cannot to change to the right lane because that would result in an immediate collision", null,
                    laneChangeModelResult.getLaneChangeDirection());
        }
        for (double pos = 0; pos < 180; pos += 5) // beyond 180m, a GTU gets a plan beyond the 200m long network
        {
            Set<DirectedLanePosition> otherLongitudinalPositions = new LinkedHashSet<>(1);
            otherLongitudinalPositions
                    .add(new DirectedLanePosition(lanes[1], new Length(pos, METER), GTUDirectionality.DIR_PLUS));

            parameters = new ParameterSet();
            parameters.setParameter(ParameterTypes.A, new Acceleration(1, METER_PER_SECOND_2));
            parameters.setParameter(ParameterTypes.B, new Acceleration(1.5, METER_PER_SECOND_2));
            parameters.setParameter(ParameterTypes.S0, new Length(2, METER));
            parameters.setParameter(ParameterTypes.T, new Duration(1, SECOND));
            parameters.setParameter(ParameterTypes.A, new Acceleration(1, METER_PER_SECOND_2));
            parameters.setDefaultParameter(ParameterTypes.LOOKAHEAD);
            parameters.setDefaultParameter(ParameterTypes.LOOKBACKOLD);
            parameters.setParameter(AbstractIDM.DELTA, 1d);
            // drivingCharacteristics =
            // new LaneBasedBehavioralCharacteristics(new IDMPlusOld(new Acceleration(1, METER_PER_SECOND_2),
            // new Acceleration(1.5, METER_PER_SECOND_2), new Length(2, METER), new Duration(1, SECOND), 1d),
            // laneChangeModel);
            LaneBasedIndividualGTU otherCar =
                    new LaneBasedIndividualGTU("OtherCarAt" + pos, gtuType, vehicleLength, new Length(2, METER),
                            new Speed(150, KM_PER_HOUR), vehicleLength.multiplyBy(0.5), this.simulator, this.network);
            strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                    new LaneBasedCFLCTacticalPlanner(new IDMPlusOld(), laneChangeModel, otherCar), otherCar);
            otherCar.setParameters(parameters);
            otherCar.init(strategicalPlanner, otherLongitudinalPositions, new Speed(100, KM_PER_HOUR));
            preferredLaneGTUs.clear();
            HeadwayGTUSimple collisionHWGTU = new HeadwayGTUSimple(otherCar.getId(), otherCar.getGTUType(),
                    new Length(pos - car.position(lanes[0], car.getReference()).getSI(), LengthUnit.SI), otherCar.getLength(),
                    otherCar.getWidth(), otherCar.getSpeed(), otherCar.getAcceleration(), null);
            preferredLaneGTUs.add(collisionHWGTU);
            laneChangeModelResult = new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                    nonPreferredLaneGTUs, new Speed(100, KM_PER_HOUR), new Acceleration(0.3, METER_PER_SECOND_2),
                    new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
            // System.out.println(String.format("pos=%5fm Egoistic: %s", pos, laneChangeModelResult.toString()));
            laneChangeModelResult = new Altruistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
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
    public void constructModel() throws SimRuntimeException
    {
        // DO NOTHING
    }

    /** {@inheritDoc} */
    @Override
    public final OTSNetwork getNetwork()
    {
        return this.network;
    }

}
