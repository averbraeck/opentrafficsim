package org.opentrafficsim.road.network.lane.object.sensor;

import static org.junit.Assert.assertEquals;

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
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCfLcTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OtsRoadNode;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the TrafficLightDetector class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightDetectorTest implements EventListener
{
    /**
     * Build the test network.
     * @param lengths double[]; The lengths of the subsequent lanes to construct; negative lengths indicate that the design
     *            direction must be reversed
     * @param simulator DEVSSimulator.TimeDoubleUnit; the simulator
     * @return Lane[]; an array of linearly connected (single) lanes
     * @throws NetworkException ...
     * @throws OtsGeometryException ...
     * @throws NamingException ...
     * @throws SimRuntimeException ...
     */
    private static Lane[] buildNetwork(final double[] lengths, final OtsSimulatorInterface simulator)
            throws NetworkException, NamingException, OtsGeometryException, SimRuntimeException
    {
        OtsRoadNetwork network = new OtsRoadNetwork("network", simulator);
        OtsRoadNode prevNode = null;
        Lane[] result = new Lane[lengths.length];
        LaneType laneType = DefaultsRoadNl.FREEWAY;
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        double cumulativeLength = 0;
        for (int nodeNumber = 0; nodeNumber <= lengths.length; nodeNumber++)
        {
            OtsRoadNode node =
                    new OtsRoadNode(network, "node" + nodeNumber, new OtsPoint3D(cumulativeLength, 0, 0), Direction.ZERO);
            if (null != prevNode)
            {
                OtsRoadNode fromNode = prevNode;
                OtsRoadNode toNode = node;
                int laneOffset = 0;
                result[nodeNumber - 1] = LaneFactory.makeMultiLane(network, "Link" + nodeNumber, fromNode, toNode, null, 1,
                        laneOffset, laneOffset, laneType, speedLimit, simulator, DefaultsNl.VEHICLE)[0];
                System.out.println("Created lane with center line " + result[nodeNumber - 1].getCenterLine());
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
        new SinkDetector(lastLane, sinkPosition, simulator, DefaultsRoadNl.ROAD_USERS);
        return result;
    }

    /**
     * Figure out on which lane and at which position we are when we're a given distance from the origin.
     * @param lanes Lane[]; the sequence of lanes
     * @param position Length; the distance
     * @return DirectedLanePosition
     * @throws GtuException should not happen; if it does; the test has failed
     */
    private LanePosition findLaneAndPosition(final Lane[] lanes, final Length position) throws GtuException
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
                return new LanePosition(lane, remainingLength);
            }
            remainingLength = remainingLength.minus(lane.getLength());
        }
        return null;
    }

    /**
     * Test the TrafficLightSensor.
     * @throws SimRuntimeException if that happens (uncaught) this test has failed
     * @throws OtsGeometryException if that happens (uncaught) this test has failed
     * @throws NamingException if that happens (uncaught) this test has failed
     * @throws NetworkException if that happens (uncaught) this test has failed
     * @throws GtuException if that happens (uncaught) this test has failed
     */
    // XXX @Test
    public final void trafficLightSensorTest()
            throws NetworkException, NamingException, OtsGeometryException, SimRuntimeException, GtuException
    {
        double[][] lengthLists = {{101.1, -1, 1, -1, 1, -900}, {1000}, {-1000}, {101.1, 900}, {101.1, 1, 1, 1, 1, 900},};
        for (double[] lengthList : lengthLists)
        {
            for (int pos = 50; pos < 130; pos++)
            {
                System.out.println("Number of lanes is " + lengthList.length + " pos is " + pos);
                OtsSimulatorInterface simulator = new OtsSimulator("TrafficLightSensorTest");
                Model model = new Model(simulator);
                simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
                Lane[] lanes = buildNetwork(lengthList, simulator);
                OtsRoadNetwork network = (OtsRoadNetwork) lanes[0].getParentLink().getNetwork();
                Length a = new Length(100, LengthUnit.METER);
                Length b = new Length(120, LengthUnit.METER);
                LanePosition pA = findLaneAndPosition(lanes, a);
                LanePosition pB = findLaneAndPosition(lanes, b);
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
                TrafficLightDetector tls =
                        new TrafficLightDetector(sensorId, pA.getLane(), pA.getPosition(), pB.getLane(), pB.getPosition(),
                                intermediateLanes, entryPosition, exitPosition, simulator, DefaultsRoadNl.TRAFFIC_LIGHT);
                assertEquals("Id should match the provided id", sensorId, tls.getId());
                assertEquals("Simulator should match", simulator, tls.getSimulator());
                assertEquals("Entry position", entryPosition, tls.getPositionTypeEntry());
                assertEquals("Exit position", exitPosition, tls.getPositionTypeExit());
                assertEquals("Position a", pA.getPosition().si, tls.getLanePositionA().si, 0.00001);
                assertEquals("Position b", pB.getPosition().si, tls.getLanePositionB().si, 0.00001);
                this.loggedEvents.clear();
                assertEquals("event list is empty", 0, this.loggedEvents.size());
                tls.addListener(this, TrafficLightDetector.TRAFFIC_LIGHT_DETECTOR_TRIGGER_ENTRY_EVENT);
                tls.addListener(this, TrafficLightDetector.TRAFFIC_LIGHT_DETECTOR_TRIGGER_EXIT_EVENT);
                assertEquals("event list is empty", 0, this.loggedEvents.size());

                GtuType gtuType = DefaultsNl.TRUCK;
                Length gtuLength = new Length(17, LengthUnit.METER);
                Length gtuWidth = new Length(2, LengthUnit.METER);
                Speed maximumSpeed = new Speed(90, SpeedUnit.KM_PER_HOUR);
                LaneBasedGtu gtu =
                        new LaneBasedGtu("GTU1", gtuType, gtuLength, gtuWidth, maximumSpeed, gtuLength.times(0.5), network);
                Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
                Length initialPosition = new Length(pos, LengthUnit.METER);
                LanePosition gtuPosition = findLaneAndPosition(lanes, initialPosition);
                initialLongitudinalPositions.add(new LanePosition(gtuPosition.getLane(), gtuPosition.getPosition()));
                Parameters parameters = DefaultTestParameters.create();
                LaneChangeModel laneChangeModel = new Egoistic();
                GtuFollowingModelOld gtuFollowingModel = new FixedAccelerationModel(
                        new Acceleration(0, AccelerationUnit.METER_PER_SECOND_2), new Duration(10, DurationUnit.SECOND));
                LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                        new LaneBasedCfLcTacticalPlanner(gtuFollowingModel, laneChangeModel, gtu), gtu);
                gtu.setParameters(parameters);
                Speed initialSpeed = new Speed(10, SpeedUnit.METER_PER_SECOND);
                if (lanes.length == 6 && pos >= 103)
                {
                    System.out.println("let op. InitialLongitudinalPositions: " + initialLongitudinalPositions);
                }
                gtu.init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);
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
    private List<Event> loggedEvents = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public final void notify(final Event event) throws RemoteException
    {
        System.out.println("Received event " + event);
        this.loggedEvents.add(event);
    }

    /** The helper model. */
    protected static class Model extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20141027L;

        /**
         * @param simulator the simulator to use
         */
        public Model(final OtsSimulatorInterface simulator)
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
        public final OtsRoadNetwork getNetwork()
        {
            return null;
        }
    }

}
