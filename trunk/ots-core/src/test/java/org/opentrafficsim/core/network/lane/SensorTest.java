package org.opentrafficsim.core.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.factory.Node;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test SensorLaneEnd and SensorLaneStart.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 16 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SensorTest
{
    /**
     * Test the constructors of SensorLaneEnd and SensorLaneStart
     * @throws SimRuntimeException
     * @throws RemoteException
     * @throws NamingException
     * @throws NetworkException
     */
    @Test
    public void sensorLaneStartEndTest() throws RemoteException, SimRuntimeException, NamingException, NetworkException
    {
        // First we need a set of Lanes
        // To create Lanes we need Nodes and a LaneType
        Node nodeAFrom = new Node("AFrom", new Coordinate(0, 0, 0));
        Node nodeATo = new Node("ATo", new Coordinate(1000, 0, 0));
        LaneType<String> laneType = new LaneType<String>("CarLane");
        // And a simulator, but for that we first need something that implements OTSModelInterface
        OTSModelInterface model = new DummyModelForSensorTest();
        final SimpleSimulator simulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        Lane[] lanes =
                LaneFactory.makeMultiLane("A", nodeAFrom, nodeATo, null, 3, laneType,
                        (OTSDEVSSimulatorInterface) simulator.getSimulator());
        // Check that there is a SensorLaneStart and a SensorLaneEnd on each Lane
        for (Lane l : lanes)
        {
            int sensorsFound = 0;
            for (Sensor sensor : l.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                    new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER)))
            {
                sensorsFound++;
                if (sensor instanceof SensorLaneStart)
                {
                    assertEquals("SensorLaneStart should be at beginning of the Lane", 0, sensor
                            .getLongitudinalPosition().getSI(), 0.00001);
                }
                else if (sensor instanceof SensorLaneEnd)
                {
                    assertEquals("SensorLaneEnd should be (almost) at end of the Lane", l.getLength().getSI(), sensor
                            .getLongitudinalPosition().getSI(), 2 * Math.ulp(l.getLength().getSI()));
                }
                else
                {
                    fail("Unexpected sensor: " + sensor.toString());
                }
            }
            assertEquals("There should be two sensor on each Lane", 2, sensorsFound);
        }
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions =
                new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();

        DoubleScalar.Rel<LengthUnit> positionA = new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER);
        initialLongitudinalPositions.put(lanes[1], positionA);
        // A Car needs a CarFollowingModel
        GTUFollowingModel cfm = new IDMPlus();
        // A Car needs a type
        GTUType<String> gtuType = new GTUType<String>("Car");
        // A Car needs an initial speed
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        // Length of the Car
        DoubleScalar.Rel<LengthUnit> carLength = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
        // Width of the Car
        DoubleScalar.Rel<LengthUnit> carWidth = new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER);
        // Maximum velocity of the Car
        DoubleScalar.Abs<SpeedUnit> maximumVelocity = new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR);
        // ID of the Car
        String carID = "theCar";
        // Now we can make a GTU
        Car<String> car =
                new Car<String>(carID, gtuType, cfm, initialLongitudinalPositions, initialSpeed, carLength, carWidth,
                        maximumVelocity, (OTSDEVSSimulatorInterface) simulator.getSimulator());
        GTUFollowingModelResult gtuFollowingModelResult =
                new GTUFollowingModelResult(new DoubleScalar.Abs<AccelerationUnit>(0.5,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<TimeUnit>(100, TimeUnit.SECOND));
        car.setState(gtuFollowingModelResult);
        Set<SimEventInterface<OTSSimTimeDouble>> eventList = simulator.getSimulator().getEventList();
        SimEventInterface<OTSSimTimeDouble> triggerEvent = null;
        int index = 0;
        for (SimEventInterface<OTSSimTimeDouble> event : eventList)
        {
            //System.out.println("Scheduled Event " + event);
            if (1 == index)
            {
                triggerEvent = event;
            }
            index++;
        }
        assertEquals("There should be three scheduled events (warmup, trigger, terminate)", 3, eventList.size());
        // The sensor should be triggered around t=38.3403 (exact value: 10 / 9 * (sqrt(3541) - 25))
        //System.out.println("trigger event is " + triggerEvent);
        assertEquals("Trigger event should be around 38.3403", 38.3403, triggerEvent.getAbsoluteExecutionTime().get()
                .getSI(), 0.0001);

    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class DummyModelForSensorTest implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150114L;

    /** The simulator. */
    private SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator;

    /**
     * Register the simulator.
     * @param simulator SimulatorInterface&lt;DoubleScalar.Abs&lt;TimeUnit&gt;, DoubleScalar.Rel&lt;TimeUnit&gt;,
     *            OTSSimTimeDouble&gt;; the simulator
     */
    public void setSimulator(
            SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> arg0)
            throws SimRuntimeException, RemoteException
    {
        // Nothing happens here
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        if (null == this.simulator)
        {
            throw new Error("getSimulator called, but simulator field is null");
        }
        return this.simulator;
    }

}
