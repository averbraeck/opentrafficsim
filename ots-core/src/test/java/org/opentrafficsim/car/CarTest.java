package org.opentrafficsim.car;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.experiment.Treatment;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.AbstractNode;
import org.opentrafficsim.core.network.CrossSectionLink;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.LinearGeometry;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CarTest
{
    /**
     * Test some basics of the Car class.
     */
    @SuppressWarnings("static-method")
    @Test
    public final void carTest() throws RemoteException, NetworkException, SimRuntimeException, NamingException
    {
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND);
        Lane lane = makeLane();
        DoubleScalar.Abs<LengthUnit> initialPosition = new DoubleScalar.Abs<LengthUnit>(12, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(34, SpeedUnit.KM_PER_HOUR);
        OTSDEVSSimulator simulator = makeSimulator();
        Car<Integer> referenceCar = makeReferenceCar(12345, lane, initialPosition, initialSpeed, simulator);

        assertEquals("The car should store it's ID", 12345, (int) referenceCar.getId());
        assertEquals("At t=initialTime the car should be at it's initial position", initialPosition.getSI(), referenceCar
                .positionOfFront(initialTime).getLongitudinalPosition().getSI(), 0.0001);
        assertEquals("The car should store it's initial speed", initialSpeed.getSI(), referenceCar.getLongitudinalVelocity(
                initialTime).getSI(), 0.00001);
        assertEquals("The car should have an initial acceleration equal to 0", 0, referenceCar.getAcceleration(initialTime)
                .getSI(), 0.0001);
    }

    public static OTSDEVSSimulator makeSimulator() throws RemoteException, SimRuntimeException, NamingException
    {
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        Model model = new Model();
        Context context = new InitialContext();
        Experiment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> exp = new Experiment<>(context);
        Treatment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> tr =
                new Treatment<>(exp, "tr1", new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND));
        exp.setTreatment(tr);
        exp.setModel(model);
        Replication<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> rep =
                new Replication<>(context, exp);
        simulator.initialize(rep, ReplicationMode.TERMINATING);
        return simulator;
    }

    /**
     * @param nr
     * @param lane
     * @param initialPosition
     * @param initialSpeed
     * @param simulator
     * @return a standard car
     * @throws RemoteException
     */
    public static Car<Integer> makeReferenceCar(int nr, Lane lane, DoubleScalar.Abs<LengthUnit> initialPosition,
            DoubleScalar.Abs<SpeedUnit> initialSpeed, OTSDEVSSimulator simulator) throws RemoteException
    {
        GTUType<String> carType = new GTUType<String>("Car");
        DoubleScalar.Rel<LengthUnit> length = new DoubleScalar.Rel<LengthUnit>(5.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER);
        Map<Lane, DoubleScalar.Abs<LengthUnit>> initialLongitudinalPositions = new HashMap<>();
        initialLongitudinalPositions.put(lane, initialPosition);
        DoubleScalar.Abs<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(120, SpeedUnit.KM_PER_HOUR);
        GTUFollowingModel cfm = new IDMPlus(simulator);
        return new Car<Integer>(nr, carType, length, width, maxSpeed, cfm, initialLongitudinalPositions, initialSpeed,
                simulator);
    }

    /**
     * @return a lane of 1000 m long.
     */
    public static Lane makeLane() throws NetworkException
    {
        Node n1 = new Node("n1", new Coordinate(0, 0));
        Node n2 = new Node("n2", new Coordinate(10000.0, 0.0));
        CrossSectionLink<String, Node> link12 =
                new CrossSectionLink<>("link12", n1, n2, new DoubleScalar.Abs<LengthUnit>(10000.0, LengthUnit.METER));
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[] { new Coordinate(0.0, 0.0), new Coordinate(10000.0, 0.0) };
        LineString line = factory.createLineString(coordinates);
        new LinearGeometry(link12, line, null);
        LaneType<String> carLaneType = new LaneType<String>("CarLane");
        DoubleScalar.Abs<LengthUnit> latPos = new DoubleScalar.Abs<LengthUnit>(0.0, LengthUnit.METER);
        DoubleScalar.Abs<LengthUnit> width = new DoubleScalar.Abs<LengthUnit>(4.0, LengthUnit.METER);
        DoubleScalar.Abs<FrequencyUnit> f200 = new DoubleScalar.Abs<FrequencyUnit>(200.0, FrequencyUnit.PER_HOUR);
        return new Lane(link12, latPos, width, width, carLaneType, LongitudinalDirectionality.FORWARD, f200);

    }

    /** node class. */
    protected static class Node extends AbstractNode<String, Coordinate>
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param id id
         * @param point point
         */
        public Node(final String id, final Coordinate point)
        {
            super(id, point);
        }

        /** {@inheritDoc} */
        @Override
        public final DirectedPoint getLocation() throws RemoteException
        {
            return new DirectedPoint(getPoint().x, getPoint().y, 0.0);
        }

        /** {@inheritDoc} */
        @Override
        public final Bounds getBounds() throws RemoteException
        {
            return new BoundingSphere();
        }
    }

    /** the helper model. */
    protected static class Model implements OTSModelInterface
    {
        private OTSDEVSSimulator simulator;

        /** {@inheritDoc} */
        @Override
        public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator)
                throws SimRuntimeException, RemoteException
        {
            this.simulator = (OTSDEVSSimulator) simulator;
        }

        /** {@inheritDoc} */
        @Override
        public OTSDEVSSimulator getSimulator() throws RemoteException
        {
            return this.simulator;
        }

    }
}
