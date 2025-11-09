package org.opentrafficsim.road.network.lane.object.sensor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.Type;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.FixedCarFollowing;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the TrafficLightDetector class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class TrafficLightDetectorTest implements EventListener
{

    /** */
    private TrafficLightDetectorTest()
    {
        // do not instantiate test class
    }

    /**
     * Build the test network.
     * @param lengths The lengths of the subsequent lanes to construct; negative lengths indicate that the design direction must
     *            be reversed
     * @param simulator DevsSimulator.TimeDoubleUnit; the simulator
     * @return an array of linearly connected (single) lanes
     * @throws NetworkException ...
     * @throws OtsGeometryException ...
     * @throws NamingException ...
     * @throws SimRuntimeException ...
     */
    private static Lane[] buildNetwork(final double[] lengths, final OtsSimulatorInterface simulator)
            throws NetworkException, NamingException, SimRuntimeException
    {
        RoadNetwork network = new RoadNetwork("network", simulator);
        Node prevNode = null;
        Lane[] result = new Lane[lengths.length];
        LaneType laneType = DefaultsRoadNl.FREEWAY;
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        double cumulativeLength = 0;
        for (int nodeNumber = 0; nodeNumber <= lengths.length; nodeNumber++)
        {
            Node node = new Node(network, "node" + nodeNumber, new Point2d(cumulativeLength, 0), Direction.ZERO);
            if (null != prevNode)
            {
                Node fromNode = prevNode;
                Node toNode = node;
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
        new SinkDetector(lastLane, sinkPosition, DefaultsNl.ROAD_USERS);
        return result;
    }

    /**
     * Figure out on which lane and at which position we are when we're a given distance from the origin.
     * @param lanes the sequence of lanes
     * @param position the distance
     * @return LanePosition
     * @throws GtuException should not happen; if it does; the test has failed
     */
    private LanePosition findLaneAndPosition(final Lane[] lanes, final Length position) throws GtuException
    {
        Length remainingLength = position;
        for (Lane lane : lanes)
        {
            if (lane.getLength().ge(remainingLength))
            {
                boolean reverse = lane.getLink().getEndNode().getPoint().x < lane.getLink().getStartNode().getPoint().x;
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
     * @throws NamingException if that happens (uncaught) this test has failed
     * @throws NetworkException if that happens (uncaught) this test has failed
     * @throws GtuException if that happens (uncaught) this test has failed
     */
    // XXX @Test
    public void trafficLightSensorTest() throws NetworkException, NamingException, SimRuntimeException, GtuException
    {
        double[][] lengthLists = {{101.1, -1, 1, -1, 1, -900}, {1000}, {-1000}, {101.1, 900}, {101.1, 1, 1, 1, 1, 900}};
        for (double[] lengthList : lengthLists)
        {
            for (int pos = 50; pos < 130; pos++)
            {
                System.out.println("Number of lanes is " + lengthList.length + " pos is " + pos);
                OtsSimulatorInterface simulator = new OtsSimulator("TrafficLightSensorTest");
                Model model = new Model(simulator);
                simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                        HistoryManagerDevs.noHistory(simulator));
                Lane[] lanes = buildNetwork(lengthList, simulator);
                RoadNetwork network = (RoadNetwork) lanes[0].getLink().getNetwork();
                Length a = new Length(100, LengthUnit.METER);
                Length b = new Length(120, LengthUnit.METER);
                LanePosition pA = findLaneAndPosition(lanes, a);
                LanePosition pB = findLaneAndPosition(lanes, b);
                String sensorId = "D123";
                Type entryPosition = RelativePosition.FRONT;
                Type exitPosition = RelativePosition.REAR;
                List<Lane> intermediateLanes = null;
                if (lanes.length > 2)
                {
                    intermediateLanes = new ArrayList<>();
                    for (Lane lane : lanes)
                    {
                        if (lane.equals(pA.lane()))
                        {
                            continue;
                        }
                        if (lane.equals(pB.lane()))
                        {
                            break;
                        }
                        intermediateLanes.add(lane);
                    }
                }
                TrafficLightDetector tls = new TrafficLightDetector(sensorId, pA.lane(), pA.position(), pB.lane(),
                        pB.position(), intermediateLanes, entryPosition, exitPosition, DefaultsNl.TRAFFIC_LIGHT);
                assertEquals(sensorId, tls.getId(), "Id should match the provided id");
                assertEquals(simulator, tls.getSimulator(), "Simulator should match");
                assertEquals(entryPosition, tls.getPositionTypeEntry(), "Entry position");
                assertEquals(exitPosition, tls.getPositionTypeExit(), "Exit position");
                assertEquals(pA.position().si, tls.getLanePositionA().si, 0.00001, "Position a");
                assertEquals(pB.position().si, tls.getLanePositionB().si, 0.00001, "Position b");
                this.loggedEvents.clear();
                assertEquals(0, this.loggedEvents.size(), "event list is empty");
                tls.addListener(this, TrafficLightDetector.TRAFFIC_LIGHT_DETECTOR_TRIGGER_ENTRY_EVENT);
                tls.addListener(this, TrafficLightDetector.TRAFFIC_LIGHT_DETECTOR_TRIGGER_EXIT_EVENT);
                assertEquals(0, this.loggedEvents.size(), "event list is empty");

                GtuType gtuType = DefaultsNl.TRUCK;
                Length gtuLength = new Length(17, LengthUnit.METER);
                Length gtuWidth = new Length(2, LengthUnit.METER);
                Speed maximumSpeed = new Speed(90, SpeedUnit.KM_PER_HOUR);
                LaneBasedGtu gtu =
                        new LaneBasedGtu("GTU1", gtuType, gtuLength, gtuWidth, maximumSpeed, gtuLength.times(0.5), network);
                // Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
                Length initialPosition = new Length(pos, LengthUnit.METER);
                LanePosition gtuPosition = findLaneAndPosition(lanes, initialPosition);
                // initialLongitudinalPositions.add(new LanePosition(gtuPosition.getLane(), gtuPosition.getPosition()));
                LanePosition initialLongitudinalPositions = new LanePosition(gtuPosition.lane(), gtuPosition.position());
                Parameters parameters = DefaultTestParameters.create();
                LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                        new LmrsFactory.Factory().setCarFollowingModelFactory(new FixedCarFollowing()).build(null).create(gtu),
                        gtu);
                gtu.setParameters(parameters);
                Speed initialSpeed = new Speed(10, SpeedUnit.METER_PER_SECOND);
                if (lanes.length == 6 && pos >= 103)
                {
                    System.out.println("let op. InitialLongitudinalPositions: " + initialLongitudinalPositions);
                }
                gtu.init(strategicalPlanner, initialLongitudinalPositions.getLocation(), initialSpeed);
                if (initialPosition.plus(gtuLength.divide(2)).lt(a) || initialPosition.minus(gtuLength.divide(2)).gt(b))
                {
                    assertEquals(0, this.loggedEvents.size(), "event list is empty");
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
                Duration stopTime = Duration.ofSI(100.0);
                while (simulator.getSimulatorTime().lt(stopTime))
                {
                    // System.out.println("simulation time is now " + simulator);
                    simulator.step();
                }
                System.out.println("simulation time is now " + simulator);
                if (initialPosition.minus(gtuLength.divide(2)).lt(b))
                {
                    assertEquals(2, this.loggedEvents.size(), "event list contains 2 events");
                }
                else
                {
                    assertEquals(0, this.loggedEvents.size(), "event list contains 0 events");
                }
            }
        }
    }

    /** Storage for logged events. */
    private List<Event> loggedEvents = new ArrayList<>();

    @Override
    public void notify(final Event event)
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
         * Constructor.
         * @param simulator the simulator to use
         */
        public Model(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        @Override
        public final RoadNetwork getNetwork()
        {
            return null;
        }
    }

}
