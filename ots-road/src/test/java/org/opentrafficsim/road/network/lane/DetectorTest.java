package org.opentrafficsim.road.network.lane;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGtuFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.eventlists.EventListInterface;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Test detectors and scheduling of trigger.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class DetectorTest implements UNITS
{
    /**
     * Test the constructors of SensorLaneEnd and SensorLaneStart.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void sensorTest() throws Exception
    {
        // We need a simulator, but for that we first need something that implements OtsModelInterface
        OtsSimulatorInterface simulator = new OtsSimulator("SensorTest");
        OtsModelInterface model = new DummyModelForSensorTest(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
        RoadNetwork network = new RoadNetwork("sensor test network", simulator);
        // Now we need a set of Lanes
        // To create Lanes we need Nodes and a LaneType
        Node nodeAFrom = new Node(network, "AFrom", new Point2d(0, 0), Direction.ZERO);
        Node nodeATo = new Node(network, "ATo", new Point2d(1000, 0), Direction.ZERO);
        Node nodeBTo = new Node(network, "BTo", new Point2d(20000, 0), Direction.ZERO);
        // so car won't run off lane B in 100 s.
        GtuType gtuType = DefaultsNl.CAR;
        LaneType laneType = DefaultsRoadNl.TWO_WAY_LANE;
        Lane[] lanesA = LaneFactory.makeMultiLane(network, "A", nodeAFrom, nodeATo, null, 3, laneType,
                new Speed(100, KM_PER_HOUR), simulator, DefaultsNl.VEHICLE);
        Lane[] lanesB = LaneFactory.makeMultiLane(network, "B", nodeATo, nodeBTo, null, 3, laneType,
                new Speed(100, KM_PER_HOUR), simulator, DefaultsNl.VEHICLE);

        // put a sensor on each of the lanes at the end of LaneA
        for (Lane lane : lanesA)
        {
            Length longitudinalPosition = new Length(999.9999, METER);
            TriggerDetector sensor = new TriggerDetector(lane, longitudinalPosition, RelativePosition.REFERENCE,
                    "Trigger@" + lane.toString(), simulator);
        }

        Length positionA = new Length(100, METER);
        LanePosition initialLongitudinalPositions = new LanePosition(lanesA[1], positionA);

        // A Car needs an initial speed
        Speed initialSpeed = new Speed(50, KM_PER_HOUR);
        // Length of the Car
        Length carLength = new Length(4, METER);
        // Width of the Car
        Length carWidth = new Length(1.8, METER);
        // Maximum speed of the Car
        Speed maximumSpeed = new Speed(100, KM_PER_HOUR);
        // ID of the Car
        String carID = "theCar";
        // Create an acceleration profile for the car
        FixedAccelerationModel fas =
                new FixedAccelerationModel(new Acceleration(0.5, METER_PER_SECOND_2), new Duration(100, SECOND));
        // Now we can make a car (GTU) (and we don't even have to hold a pointer to it)
        Parameters parameters = DefaultTestParameters.create();

        // LaneBasedBehavioralCharacteristics drivingCharacteristics =
        // new LaneBasedBehavioralCharacteristics(fas, null);
        LaneBasedGtu car = new LaneBasedGtu(carID, gtuType, carLength, carWidth, maximumSpeed, carLength.times(0.5),
                (RoadNetwork) network);
        LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(new LaneBasedGtuFollowingTacticalPlanner(fas, car), car);
        car.setParameters(parameters);
        car.init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);
        simulator.runUpTo(new Time(1, TimeUnit.BASE_SECOND));
        if (!simulator.isStartingOrRunning())
        {
            simulator.start();
        }
        while (simulator.isStartingOrRunning())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ie)
            {
                ie = null; // ignore
            }
        }
        // Construction of the car scheduled a car move event at t=0
        EventListInterface<Duration> eventList = simulator.getEventList();
        SimEventInterface<Duration> triggerEvent = null;
        for (SimEventInterface<Duration> event : eventList)
        {
            System.out.println("Scheduled Event " + event);
            if (event.toString().contains("trigger"))
            {
                triggerEvent = event;
            }
        }
        // XXX this is not true anymore with OperationalPlans, Perception, etc =>
        // XXX the number of events that should be scheduled can vary per models chosen
        // XXX assertEquals("There should be three scheduled events (trigger, leaveLane,
        // XXX car.move, terminate)", 4, eventList.size());
        // The sensor should be triggered around t=38.3403 (exact value: 10 / 9 * (sqrt(3541) - 25))
        // System.out.println("trigger event is " + triggerEvent);
        // / TODO not triggered in next half second.
        // XXX assertEquals("Trigger event should be around 38.3403", 38.3403,
        // XXX triggerEvent.getAbsoluteExecutionTime().get().getSI(), 0.0001);
    }
}

/** */
class TriggerDetector extends LaneDetector
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param lane lane of the sensor
     * @param longitudinalPosition position of the sensor on the lane
     * @param positionType trigger position of the GTU
     * @param name name of the sensor
     * @param simulator the simulator
     * @throws NetworkException in case position is out of bounds
     */
    TriggerDetector(final Lane lane, final Length longitudinalPosition, final RelativePosition.Type positionType,
            final String name, final OtsSimulatorInterface simulator) throws NetworkException
    {
        super(name, lane, longitudinalPosition, positionType, simulator, DefaultsRoadNl.ROAD_USERS);
    }

    /** {@inheritDoc} */
    @Override
    public void triggerResponse(final LaneBasedGtu gtu)
    {
        // TODO check that the sensor is triggered at the right time.
    }

}

/**
 * Dummy OtsModelInterface.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
class DummyModelForSensorTest extends AbstractOtsModel
{
    /** */
    private static final long serialVersionUID = 20150114L;

    /**
     * @param simulator the simulator to use
     */
    DummyModelForSensorTest(final OtsSimulatorInterface simulator)
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
    public final RoadNetwork getNetwork()
    {
        return null;
    }
}
