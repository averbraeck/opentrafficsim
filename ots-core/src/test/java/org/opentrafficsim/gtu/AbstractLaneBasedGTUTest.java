package org.opentrafficsim.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.factory.Node;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
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
 * Test the various methods of an AbstractLaneBasedGTU.<br/>
 * As abstract classes cannot be directly
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AbstractLaneBasedGTUTest
{

    /**
     * Test that the constructor puts the supplied values in the correct fields.
     * @throws SimRuntimeException
     * @throws RemoteException
     * @throws NamingException
     * @throws NetworkException
     */
    @Test
    public void constructorTest() throws RemoteException, SimRuntimeException, NamingException, NetworkException
    {
        // This initialization code should probably be moved to a helper method that will be used in several tests.
        // First we need a set of Lanes
        // To create Lanes we need Nodes and a LaneType
        Node nodeAFrom = new Node("AFrom", new Coordinate(0, 0, 0));
        Node nodeATo = new Node("ATo", new Coordinate(1000, 0, 0));
        LaneType<String> laneType = new LaneType<String>("CarLane");
        // And a simulator, but for that we first need something that implements OTSModelInterface
        OTSModelInterface model = new DummyModel();
        final SimpleSimulator simulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));

        Lane[] lanesGroupA =
                LaneFactory.makeMultiLane("A", nodeAFrom, nodeATo, null, 3, laneType,
                        (OTSDEVSSimulatorInterface) simulator.getSimulator());
        // A GTU can exist on several lanes at once; create another lane group to test that
        Node nodeBFrom = new Node("BFrom", new Coordinate(10, 0, 0));
        Node nodeBTo = new Node("BTo", new Coordinate(1000, 100, 0));
        Lane[] lanesGroupB =
                LaneFactory.makeMultiLane("B", nodeBFrom, nodeBTo, null, 3, laneType,
                        (OTSDEVSSimulatorInterface) simulator.getSimulator());
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions =
                new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();

        DoubleScalar.Rel<LengthUnit> positionA = new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER);
        initialLongitudinalPositions.put(lanesGroupA[1], positionA);
        DoubleScalar.Rel<LengthUnit> positionB = new DoubleScalar.Rel<LengthUnit>(90, LengthUnit.METER);
        initialLongitudinalPositions.put(lanesGroupB[1], positionB);
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
        // Now we can verify the various fields in the newly created Car
        assertEquals("ID of the car should be identical to the provided one", carID, car.getId());
        assertEquals("GTU following model should be identical to the provided one", cfm, car.getGTUFollowingModel());
        assertEquals("GTU type should be identical to the provided one", gtuType, car.getGTUType());
        assertEquals("front in lanesGroupA[1] is positionA", positionA.getSI(),
                car.position(lanesGroupA[1], car.getFront()).getSI(), 0.0001);
        assertEquals("front in lanesGroupB[1] is positionB", positionB.getSI(),
                car.position(lanesGroupB[1], car.getFront()).getSI(), 0.0001);
        // Test the position(Lane, RelativePosition) method
        try
        {
            car.position(null, car.getFront());
            fail("position on null lane should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore
        }
        for (Lane[] laneGroup : new Lane[][]{lanesGroupA, lanesGroupB})
        {
            for (int laneIndex = 0; laneIndex < laneGroup.length; laneIndex++)
            {
                Lane lane = laneGroup[laneIndex];
                boolean expectException = 1 != laneIndex;
                for (RelativePosition relativePosition : new RelativePosition[]{car.getFront(), car.getRear()})
                {
                    // System.out.println("lane:" + lane + ", expectedException: " + expectException
                    // + ", relativePostion: " + relativePosition);
                    try
                    {
                        DoubleScalar.Rel<LengthUnit> position = car.position(lane, relativePosition);
                        if (expectException)
                        {
                            // System.out.println("position: " + position);
                            fail("Calling position on lane that the car is NOT on should have thrown a NetworkException");
                        }
                        else
                        {
                            DoubleScalar.Rel<LengthUnit> expectedPosition =
                                    laneGroup == lanesGroupA ? positionA : positionB;
                            // FIXME There should be a better way to check equality of RelativePosition
                            if (relativePosition.getDx().getSI() != 0)
                            {
                                expectedPosition = DoubleScalar.minus(expectedPosition, carLength).immutable();
                            }
                            // System.out.println("reported position: " + position);
                            // System.out.println("expected position: " + expectedPosition);
                            assertEquals("Position should match initial position", expectedPosition.getSI(),
                                    position.getSI(), 0.0001);
                        }
                    }
                    catch (NetworkException ne)
                    {
                        if (!expectException)
                        {
                            System.out.println(ne);
                            fail("Calling position on lane that the car is on should NOT have thrown a NetworkException");
                        }
                    }
                }
            }
        }
        // Assign a movement to the car
        DoubleScalar.Abs<AccelerationUnit> acceleration =
                new DoubleScalar.Abs<AccelerationUnit>(2, AccelerationUnit.METER_PER_SECOND_2);
        DoubleScalar.Abs<TimeUnit> validUntil = new DoubleScalar.Abs<TimeUnit>(10, TimeUnit.SECOND);
        car.setState(new GTUFollowingModelResult(acceleration, validUntil));
        // Increase the simulator clock in small steps and verify the positions of the Car at each step
        final double step = 0.01d;
        for (int i = 0;; i++)
        {
            DoubleScalar.Abs<TimeUnit> stopTime = new DoubleScalar.Abs<TimeUnit>(i * step, TimeUnit.SECOND);
            if (stopTime.getSI() > validUntil.getSI())
            {
                break;
            }
            //System.out.println("Simulating until " + stopTime.getSI());
            simulateUntil((OTSDEVSSimulatorInterface) simulator.getSimulator(), stopTime);
            //System.out.println("Clock is now " + simulator.getSimulator().getSimulatorTime().get().getSI());
            for (Lane[] laneGroup : new Lane[][]{lanesGroupA, lanesGroupB})
            {
                for (int laneIndex = 0; laneIndex < laneGroup.length; laneIndex++)
                {
                    Lane lane = laneGroup[laneIndex];
                    boolean expectException = 1 != laneIndex;
                    for (RelativePosition relativePosition : new RelativePosition[]{car.getFront(), car.getRear()})
                    {
                        // System.out.println("lane:" + lane + ", expectedException: " + expectException
                        // + ", relativePostion: " + relativePosition);
                        try
                        {
                            DoubleScalar.Rel<LengthUnit> position = car.position(lane, relativePosition);
                            if (expectException)
                            {
                                // System.out.println("position: " + position);
                                fail("Calling position on lane that the car is NOT on should have thrown a NetworkException");
                            }
                            else
                            {
                                DoubleScalar.Rel<LengthUnit> expectedPosition =
                                        laneGroup == lanesGroupA ? positionA : positionB;
                                expectedPosition =
                                        DoubleScalar.plus(
                                                expectedPosition,
                                                new DoubleScalar.Rel<LengthUnit>(stopTime.getSI()
                                                        * initialSpeed.getSI(), LengthUnit.SI)).immutable();
                                expectedPosition =
                                        DoubleScalar.plus(
                                                expectedPosition,
                                                new DoubleScalar.Rel<LengthUnit>(0.5 * acceleration.getSI()
                                                        * stopTime.getSI() * stopTime.getSI(), LengthUnit.SI))
                                                .immutable();
                                // FIXME There should be a better way to check equality of RelativePosition
                                if (relativePosition.getDx().getSI() != 0)
                                {
                                    expectedPosition = DoubleScalar.minus(expectedPosition, carLength).immutable();
                                }
                                // System.out.println("reported position: " + position);
                                // System.out.println("expected position: " + expectedPosition);
                                assertEquals("Position should match initial position", expectedPosition.getSI(),
                                        position.getSI(), 0.0001);
                            }
                        }
                        catch (NetworkException ne)
                        {
                            if (!expectException)
                            {
                                System.out.println(ne);
                                fail("Calling position on lane that the car is on should NOT have thrown a NetworkException");
                            }
                        }
                    }
                }
            }

        }
    }

    /** Flag to indicate that the autoPauseSimulator event was executed. */
    private volatile boolean stopTimeReached;

    /**
     * Increase the simulator clock to the indicated value.
     * @param simulator
     * @param stopTime
     * @throws RemoteException
     * @throws SimRuntimeException
     */
    private void simulateUntil(OTSDEVSSimulatorInterface simulator, DoubleScalar.Abs<TimeUnit> stopTime)
            throws RemoteException, SimRuntimeException
    {
        if (simulator.getSimulatorTime().get().getSI() > stopTime.getSI())
        {
            System.out.println("stopTime: " + stopTime.getSI());
            System.out.println("simuTime: " + simulator.getSimulatorTime().get().getSI());
            throw new Error("Cannot step back in time");
        }
        this.stopTimeReached = false;
        SimEvent<OTSSimTimeDouble> stopAtEvent =
                new SimEvent<OTSSimTimeDouble>(new OTSSimTimeDouble(stopTime), SimEventInterface.MAX_PRIORITY, this,
                        this, "autoPauseSimulator", null);
        simulator.scheduleEvent(stopAtEvent);
        // Insert a dummy event to ensure that the clock will stay at this value
        SimEvent<OTSSimTimeDouble> dummyEvent =
                new SimEvent<OTSSimTimeDouble>(new OTSSimTimeDouble(stopTime), SimEventInterface.MIN_PRIORITY, this,
                        this, "dummyEvent", null);
        simulator.scheduleEvent(dummyEvent);
        while (!this.stopTimeReached)
        {
            simulator.step();
        }
    }

    /** Called by the simulator. */
    public final void autoPauseSimulator()
    {
        this.stopTimeReached = true;
    }

    /** Called by the simulator. */
    public final void dummyEvent()
    {
        // Do nothing
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
class DummyModel implements OTSModelInterface
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
