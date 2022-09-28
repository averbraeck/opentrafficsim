package org.opentrafficsim.road.network.lane.object.sensor;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the TrafficLightSensor class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightSensorTest implements EventListenerInterface
{
    /**
     * Build the test network.
     * @param lengths double[]; The lengths of the subsequent lanes to construct; negative lengths indicate that the design
     *            direction must be reversed
     * @param simulator DEVSSimulator.TimeDoubleUnit; the simulator
     * @return Lane[]; an array of linearly connected (single) lanes
     * @throws NetworkException ...
     * @throws OTSGeometryException ...
     * @throws NamingException ...
     * @throws SimRuntimeException ...
     */
    private static Lane[] buildNetwork(final double[] lengths, final OTSSimulatorInterface simulator)
            throws NetworkException, NamingException, OTSGeometryException, SimRuntimeException
    {
        OTSRoadNetwork network = new OTSRoadNetwork("network", true, simulator);
        OTSRoadNode prevNode = null;
        Lane[] result = new Lane[lengths.length];
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        double cumulativeLength = 0;
        for (int nodeNumber = 0; nodeNumber <= lengths.length; nodeNumber++)
        {
            OTSRoadNode node =
                    new OTSRoadNode(network, "node" + nodeNumber, new OTSPoint3D(cumulativeLength, 0, 0), Direction.ZERO);
            if (null != prevNode)
            {
                LongitudinalDirectionality direction = lengths[nodeNumber - 1] > 0 ? LongitudinalDirectionality.DIR_PLUS
                        : LongitudinalDirectionality.DIR_MINUS;
                OTSRoadNode fromNode = LongitudinalDirectionality.DIR_PLUS == direction ? prevNode : node;
                OTSRoadNode toNode = LongitudinalDirectionality.DIR_PLUS == direction ? node : prevNode;
                int laneOffset = LongitudinalDirectionality.DIR_PLUS == direction ? 0 : -1;
                result[nodeNumber - 1] = LaneFactory.makeMultiLane(network, "Link" + nodeNumber, fromNode, toNode, null, 1,
                        laneOffset, laneOffset, laneType, speedLimit, simulator)[0];
                System.out.println("Created lane with center line " + result[nodeNumber - 1].getCenterLine()
                        + ", directionality " + direction);
            }
            if (nodeNumber < lengths.length)
            {
                cumulativeLength += Math.abs(lengths[nodeNumber]);
            }
            prevNode = node;
        }
        // put a sink at halfway point of last lane
        Lane lastLane = result[lengths.length - 1];
        Length sinkPosition = new Length(lengths[lengths.length - 1] > 0 ? lastLane.getLength().si - 10 : 10, LengthUnit.METER);
        new SinkSensor(lastLane, sinkPosition, GTUDirectionality.DIR_PLUS, simulator);
        return result;
    }

    /**
     * Figure out on which lane and at which position we are when we're a given distance from the origin.
     * @param lanes Lane[]; the sequence of lanes
     * @param position Length; the distance
     * @return DirectedLanePosition
     * @throws GTUException should not happen; if it does; the test has failed
     */
    private DirectedLanePosition findLaneAndPosition(final Lane[] lanes, final Length position) throws GTUException
    {
        Length remainingLength = position;
        for (Lane lane : lanes)
        {
            if (lane.getLength().ge(remainingLength))
            {
                boolean reverse =
                        lane.getParentLink().getEndNode().getPoint().x < lane.getParentLink().getStartNode().getPoint().x;
                if (reverse)
                {
                    remainingLength = lane.getLength().minus(remainingLength);
                }
                return new DirectedLanePosition(lane, remainingLength,
                        reverse ? GTUDirectionality.DIR_MINUS : GTUDirectionality.DIR_PLUS);
            }
            remainingLength = remainingLength.minus(lane.getLength());
        }
        return null;
    }

    /**
     * Test the TrafficLightSensor.
     * @throws SimRuntimeException if that happens (uncaught) this test has failed
     * @throws OTSGeometryException if that happens (uncaught) this test has failed
     * @throws NamingException if that happens (uncaught) this test has failed
     * @throws NetworkException if that happens (uncaught) this test has failed
     * @throws GTUException if that happens (uncaught) this test has failed
     */
    // XXX @Test
    public final void trafficLightSensorTest()
            throws NetworkException, NamingException, OTSGeometryException, SimRuntimeException, GTUException
    {
        double[][] lengthLists = {{101.1, -1, 1, -1, 1, -900}, {1000}, {-1000}, {101.1, 900}, {101.1, 1, 1, 1, 1, 900},};
        for (double[] lengthList : lengthLists)
        {
            for (int pos = 50; pos < 130; pos++)
            {
                System.out.println("Number of lanes is " + lengthList.length + " pos is " + pos);
                OTSSimulatorInterface simulator = new OTSSimulator("TrafficLightSensorTest");
                Model model = new Model(simulator);
                simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
                Lane[] lanes = buildNetwork(lengthList, simulator);
                OTSRoadNetwork network = (OTSRoadNetwork) lanes[0].getParentLink().getNetwork();
                Length a = new Length(100, LengthUnit.METER);
                Length b = new Length(120, LengthUnit.METER);
                DirectedLanePosition pA = findLaneAndPosition(lanes, a);
                DirectedLanePosition pB = findLaneAndPosition(lanes, b);
                String sensorId = "D123";
                TYPE entryPosition = RelativePosition.FRONT;
                TYPE exitPosition = RelativePosition.REAR;
                List<Lane> intermediateLanes = null;
                if (lanes.length > 2)
                {
                    intermediateLanes = new ArrayList<>();
                    for (Lane lane : lanes)
                    {
                        if (lane.equals(pA.getLane()))
                        {
                            continue;
                        }
                        if (lane.equals(pB.getLane()))
                        {
                            break;
                        }
                        intermediateLanes.add(lane);
                    }
                }
                TrafficLightSensor tls = new TrafficLightSensor(sensorId, pA.getLane(), pA.getPosition(), pB.getLane(),
                        pB.getPosition(), intermediateLanes, entryPosition, exitPosition, simulator, Compatible.EVERYTHING);
                assertEquals("Id should match the provided id", sensorId, tls.getId());
                assertEquals("Simulator should match", simulator, tls.getSimulator());
                assertEquals("Entry position", entryPosition, tls.getPositionTypeEntry());
                assertEquals("Exit position", exitPosition, tls.getPositionTypeExit());
                assertEquals("Position a", pA.getPosition().si, tls.getLanePositionA().si, 0.00001);
                assertEquals("Position b", pB.getPosition().si, tls.getLanePositionB().si, 0.00001);
                this.loggedEvents.clear();
                assertEquals("event list is empty", 0, this.loggedEvents.size());
                tls.addListener(this, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT);
                tls.addListener(this, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT);
                assertEquals("event list is empty", 0, this.loggedEvents.size());

                GTUType gtuType = network.getGtuType(GTUType.DEFAULTS.TRUCK);
                Length gtuLength = new Length(17, LengthUnit.METER);
                Length gtuWidth = new Length(2, LengthUnit.METER);
                Speed maximumSpeed = new Speed(90, SpeedUnit.KM_PER_HOUR);
                LaneBasedGTU gtu = new LaneBasedIndividualGTU("GTU1", gtuType, gtuLength, gtuWidth, maximumSpeed,
                        gtuLength.times(0.5), simulator, network);
                Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
                Length initialPosition = new Length(pos, LengthUnit.METER);
                DirectedLanePosition gtuPosition = findLaneAndPosition(lanes, initialPosition);
                initialLongitudinalPositions.add(new DirectedLanePosition(gtuPosition.getLane(), gtuPosition.getPosition(),
                        gtuPosition.getGtuDirection()));
                Parameters parameters = DefaultTestParameters.create();
                LaneChangeModel laneChangeModel = new Egoistic();
                GTUFollowingModelOld gtuFollowingModel = new FixedAccelerationModel(
                        new Acceleration(0, AccelerationUnit.METER_PER_SECOND_2), new Duration(10, DurationUnit.SECOND));
                LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                        new LaneBasedCFLCTacticalPlanner(gtuFollowingModel, laneChangeModel, gtu), gtu);
                gtu.setParameters(parameters);
                Speed initialSpeed = new Speed(10, SpeedUnit.METER_PER_SECOND);
                if (lanes.length == 6 && pos >= 103)
                {
                    System.out.println("let op. InitialLongitudinalPositions: " + initialLongitudinalPositions);
                }
                ((AbstractLaneBasedGTU) gtu).init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);
                if (initialPosition.plus(gtuLength.divide(2)).lt(a) || initialPosition.minus(gtuLength.divide(2)).gt(b))
                {
                    assertEquals("event list is empty", 0, this.loggedEvents.size());
                }
                else
                {
                    if (1 != this.loggedEvents.size())
                    {
                        // TODO THIS TEST FAILS!!
                        // assertEquals("event list should contain one event (due to creation of the GTU on the detector)", 1,
                        // this.loggedEvents.size());
                    }
                }
                Time stopTime = new Time(100, TimeUnit.BASE_SECOND);
                while (simulator.getSimulatorAbsTime().lt(stopTime))
                {
                    // System.out.println("simulation time is now " + simulator);
                    simulator.step();
                }
                System.out.println("simulation time is now " + simulator);
                if (initialPosition.minus(gtuLength.divide(2)).lt(b))
                {
                    assertEquals("event list contains 2 events", 2, this.loggedEvents.size());
                }
                else
                {
                    assertEquals("event list contains 0 events", 0, this.loggedEvents.size());
                }
            }
        }
    }

    /** Storage for logged events. */
    private List<EventInterface> loggedEvents = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
    {
        System.out.println("Received event " + event);
        this.loggedEvents.add(event);
    }

    /** The helper model. */
    protected static class Model extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141027L;

        /**
         * @param simulator the simulator to use
         */
        public Model(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public final OTSRoadNetwork getNetwork()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "TrafficLightSensorTest.Model";
        }
    }

}
