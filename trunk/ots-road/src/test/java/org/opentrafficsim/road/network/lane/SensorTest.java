package org.opentrafficsim.road.network.lane;

import static org.opentrafficsim.core.gtu.GTUType.CAR;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Test sensors and scheduling of trigger.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 16 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SensorTest implements UNITS
{
    /**
     * Test the constructors of SensorLaneEnd and SensorLaneStart.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void sensorTest() throws Exception
    {
        Network network = new OTSNetwork("sensor test network");
        // First we need a set of Lanes
        // To create Lanes we need Nodes and a LaneType
        OTSNode nodeAFrom = new OTSNode(network, "AFrom", new OTSPoint3D(0, 0, 0));
        OTSNode nodeATo = new OTSNode(network, "ATo", new OTSPoint3D(1000, 0, 0));
        OTSNode nodeBTo = new OTSNode(network, "BTo", new OTSPoint3D(20000, 0, 0)); // so car won't run off lane B in 100 s.
        GTUType gtuType = CAR;
        Set<GTUType> compatibility = new HashSet<GTUType>();
        compatibility.add(gtuType);
        LaneType laneType = new LaneType("CarLane", compatibility);
        // And a simulator, but for that we first need something that implements OTSModelInterface
        OTSModelInterface model = new DummyModelForSensorTest();
        final SimpleSimulator simulator = new SimpleSimulator(Time.ZERO, Duration.ZERO, new Duration(3600.0, SECOND), model);
        Lane[] lanesA = LaneFactory.makeMultiLane(network, "A", nodeAFrom, nodeATo, null, 3, laneType,
                new Speed(100, KM_PER_HOUR), simulator, LongitudinalDirectionality.DIR_PLUS);
        Lane[] lanesB = LaneFactory.makeMultiLane(network, "B", nodeATo, nodeBTo, null, 3, laneType,
                new Speed(100, KM_PER_HOUR), simulator, LongitudinalDirectionality.DIR_PLUS);

        // put a sensor on each of the lanes at the end of LaneA
        for (Lane lane : lanesA)
        {
            Length longitudinalPosition = new Length(999.9999, METER);
            TriggerSensor sensor = new TriggerSensor(lane, longitudinalPosition, RelativePosition.REFERENCE,
                    "Trigger@" + lane.toString(), simulator);
        }

        Length positionA = new Length(100, METER);
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lanesA[1], positionA, GTUDirectionality.DIR_PLUS));

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
        BehavioralCharacteristics behavioralCharacteristics = DefaultTestParameters.create();

        // LaneBasedBehavioralCharacteristics drivingCharacteristics =
        // new LaneBasedBehavioralCharacteristics(fas, null);
        LaneBasedIndividualGTU car =
                new LaneBasedIndividualGTU(carID, gtuType, carLength, carWidth, maximumSpeed, simulator, (OTSNetwork) network);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                new LaneBasedGTUFollowingTacticalPlanner(fas, car), car);
        car.init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);
        simulator.runUpTo(new Time(1, TimeUnit.BASE_SECOND));
        if (!simulator.isRunning())
        {
            simulator.start();
        }
        while (simulator.isRunning())
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
        Set<SimEventInterface<OTSSimTimeDouble>> eventList = simulator.getEventList();
        SimEventInterface<OTSSimTimeDouble> triggerEvent = null;
        for (SimEventInterface<OTSSimTimeDouble> event : eventList)
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
class TriggerSensor extends AbstractSensor
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
    public TriggerSensor(final Lane lane, final Length longitudinalPosition, final RelativePosition.TYPE positionType,
            final String name, OTSDEVSSimulatorInterface simulator) throws NetworkException
    {
        super(name, lane, longitudinalPosition, positionType, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void triggerResponse(final LaneBasedGTU gtu)
    {
        // TODO check that the sensor is triggered at the right time.
    }

    /** {@inheritDoc} */
    @Override
    public AbstractSensor clone(CrossSectionElement newCSE, OTSSimulatorInterface newSimulator, boolean animation)
            throws NetworkException
    {
        return null;
    }

}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 4 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class DummyModelForSensorTest implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150114L;

    /** The simulator. */
    private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

    /**
     * Register the simulator.
     * @param simulator SimulatorInterface&lt;Time, Duration, OTSSimTimeDouble&gt;; the simulator
     */
    public final void setSimulator(SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(SimulatorInterface<Time, Duration, OTSSimTimeDouble> arg0) throws SimRuntimeException
    {
        // Nothing happens here
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()

    {
        if (null == this.simulator)
        {
            throw new Error("getSimulator called, but simulator field is null");
        }
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return null;
    }

}
