package org.opentrafficsim.road.network.lane.object.sensor;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
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
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the TrafficLightSensor class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightSensorTest implements EventListenerInterface
{

    /**
     * Make a simulator.
     * @return OTSDEVSSimulator; the new simulator
     * @throws SimRuntimeException ...
     * @throws NamingException ...
     */
    private static OTSDEVSSimulator makeSimulator() throws SimRuntimeException, NamingException
    {
        return new SimpleSimulator(Time.ZERO, Duration.ZERO, new Duration(1, TimeUnit.HOUR), new OTSModelInterface()
        {

            /** */
            private static final long serialVersionUID = 1L;

            /** Store the simulator. */
            private SimulatorInterface<Time, Duration, OTSSimTimeDouble> sim;

            @Override
            public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                    throws SimRuntimeException, RemoteException
            {
                this.sim = theSimulator;
            }

            @Override
            public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
            {
                return this.sim;
            }
        });
    }

    /**
     * Build the test network.
     * @param numberOfLinks int; number of consecutive links.
     * @param directionalities String; directionalities for the consecutive lanes
     * @param simulator OTSDEVSSimulator; the simulator
     * @return Lane[]; an array of linearly connected (single) lanes
     * @throws NetworkException ...
     * @throws OTSGeometryException ...
     * @throws NamingException ...
     * @throws SimRuntimeException ...
     */
    private static Lane[] buildNetwork(final int numberOfLinks, final String directionalities, final OTSDEVSSimulator simulator)
            throws NetworkException, NamingException, OTSGeometryException, SimRuntimeException
    {
        OTSNetwork network = new OTSNetwork("network");
        OTSNode prevNode = null;
        Lane[] result = new Lane[numberOfLinks];
        LaneType laneType = LaneType.ALL;
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        for (int nodeNumber = 0; nodeNumber <= numberOfLinks; nodeNumber++)
        {
            double x = 0 == nodeNumber ? 0 : numberOfLinks == nodeNumber ? 1000 : (500 + nodeNumber);
            OTSNode node = new OTSNode(network, "node" + nodeNumber, new OTSPoint3D(x, 0, 0));
            LongitudinalDirectionality direction = LongitudinalDirectionality.DIR_PLUS;
            if (directionalities.length() > nodeNumber && directionalities.charAt(nodeNumber) == 'R')
            {
                direction = LongitudinalDirectionality.DIR_MINUS;
            }
            if (null != prevNode)
            {
                OTSNode fromNode = LongitudinalDirectionality.DIR_PLUS == direction ? prevNode : node;
                OTSNode toNode = LongitudinalDirectionality.DIR_PLUS == direction ? node : prevNode;
                result[nodeNumber - 1] =
                        LaneFactory.makeMultiLane(network, "Link" + nodeNumber, fromNode, toNode, null, 1, laneType,
                                speedLimit, simulator, direction)[0];
            }
            prevNode = node;
        }
        // put a sink at halfway point of last lane
        Lane lastLane = result[numberOfLinks - 1];
        lastLane.addSensor(new SinkSensor(lastLane, new Length(lastLane.getLength().divideBy(2)), simulator), GTUType.ALL);
        return result;
    }

    /**
     * Test the constructor.
     * @throws SimRuntimeException ...
     * @throws OTSGeometryException ...
     * @throws NamingException ...
     * @throws NetworkException ...
     * @throws GTUException ...
     */
    @Test
    public final void constructorTest() throws NetworkException, NamingException, OTSGeometryException, SimRuntimeException,
            GTUException
    {
        OTSDEVSSimulator simulator = makeSimulator();
        Lane lane = buildNetwork(1, "", simulator)[0];
        OTSNetwork network = (OTSNetwork) lane.getParentLink().getNetwork();
        Length a = new Length(100, LengthUnit.METER);
        Length b = new Length(120, LengthUnit.METER);
        String sensorId = "D123";
        TYPE entryPosition = RelativePosition.FRONT;
        TYPE exitPosition = RelativePosition.REAR;
        TrafficLightSensor tls =
                new TrafficLightSensor(sensorId, lane, a, lane, b, null, entryPosition, exitPosition, simulator);
        assertEquals("Id should match the provided id", sensorId, tls.getId());
        assertEquals("Simulator should match", simulator, tls.getSimulator());
        assertEquals("Entry position", entryPosition, tls.getPositionTypeEntry());
        assertEquals("Exit position", exitPosition, tls.getPositionTypeExit());
        assertEquals("Position a", a.si, tls.getLanePositionA().si, 0.00001);
        assertEquals("Position b", b.si, tls.getLanePositionB().si, 0.00001);
        this.loggedEvents.clear();
        assertEquals("event list is empty", 0, this.loggedEvents.size());
        tls.addListener(this, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT);
        tls.addListener(this, NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT);
        assertEquals("event list is empty", 0, this.loggedEvents.size());

        GTUType gtuType = new GTUType("Truck");
        Length gtuLength = new Length(18, LengthUnit.METER);
        Length gtuWidth = new Length(2, LengthUnit.METER);
        Speed maximumSpeed = new Speed(90, SpeedUnit.KM_PER_HOUR);
        LaneBasedGTU gtu = new LaneBasedIndividualGTU("GTU1", gtuType, gtuLength, gtuWidth, maximumSpeed, simulator, network);
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        Length initialPosition = new Length(50, LengthUnit.METER);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        BehavioralCharacteristics behavioralCharacteristics = DefaultTestParameters.create();
        LaneChangeModel laneChangeModel = new Egoistic();
        GTUFollowingModelOld gtuFollowingModel =
                new FixedAccelerationModel(new Acceleration(0, AccelerationUnit.METER_PER_SECOND_2), new Duration(10,
                        TimeUnit.SECOND));
        LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, new LaneBasedCFLCTacticalPlanner(
                        gtuFollowingModel, laneChangeModel, gtu), gtu);
        Speed initialSpeed = new Speed(10, SpeedUnit.METER_PER_SECOND);
        ((AbstractLaneBasedGTU) gtu).init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);
        System.out.println("GTU is initially at " + gtu.positions(RelativePosition.REFERENCE_POSITION));
        assertEquals("event list is empty", 0, this.loggedEvents.size());
        Time stopTime = new Time(100, TimeUnit.SECOND);
        simulator.runUpTo(stopTime);
        while (simulator.getSimulatorTime().get().lt(stopTime))
        {
            System.out.println("simulation time is now " + simulator);
            simulator.step();
        }
        System.out.println("simulation time is now " + simulator);
        System.out.println("GTU is now at " + gtu.positions(RelativePosition.REFERENCE_POSITION));
        assertEquals("event list contains 2 events", 2, this.loggedEvents.size());
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

}
