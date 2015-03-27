package org.opentrafficsim.core.car;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.experiment.Treatment;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.FixedAccelerationModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.AccelerationUnit;
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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CarTest
{
    /**
     * Test some basics of the Car class.
     * @throws RemoteException on network error
     * @throws NetworkException on ???
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     * @throws GTUException on ???
     */
    @SuppressWarnings("static-method")
    @Test
    public final void carTest() throws RemoteException, NetworkException, SimRuntimeException, NamingException,
            GTUException
    {
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND);
        Lane lane = makeLane();
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(12, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(34, SpeedUnit.KM_PER_HOUR);
        OTSDEVSSimulator simulator = makeSimulator();
        GTUFollowingModel gtuFollowingModel =
                new FixedAccelerationModel(new DoubleScalar.Abs<AccelerationUnit>(0,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<TimeUnit>(10, TimeUnit.SECOND));
        LaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualCar<Integer> referenceCar =
                makeReferenceCar(12345, lane, initialPosition, initialSpeed, simulator, gtuFollowingModel,
                        laneChangeModel);
        assertEquals("The car should store it's ID", 12345, (int) referenceCar.getId());
        assertEquals("At t=initialTime the car should be at it's initial position", initialPosition.getSI(),
                referenceCar.position(lane, referenceCar.getReference(), initialTime).getSI(), 0.0001);
        assertEquals("The car should store it's initial speed", initialSpeed.getSI(), referenceCar
                .getLongitudinalVelocity(initialTime).getSI(), 0.00001);
        assertEquals("The car should have an initial acceleration equal to 0", 0,
                referenceCar.getAcceleration(initialTime).getSI(), 0.0001);
        assertEquals("The gtu following model should be " + gtuFollowingModel, gtuFollowingModel,
                referenceCar.getGTUFollowingModel());
        // There is (currently) no way to retrieve the lane change model of a GTU.
    }

    /**
     * Create the simplest possible simulator.
     * @return OTSDEVSSimulator
     * @throws RemoteException on network error
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     */
    public static OTSDEVSSimulator makeSimulator() throws RemoteException, SimRuntimeException, NamingException
    {
        OTSDEVSSimulator simulator = new OTSDEVSSimulator();
        Model model = new Model();
        Context context = new InitialContext();
        Experiment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> exp =
                new Experiment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>(context);
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
     * Create a new Car.
     * @param nr int; the name (number) of the Car
     * @param lane Lane; the lane on which the new Car is positioned
     * @param initialPosition DoubleScalar.Abs&lt;LengthUnit&gt;; the initial longitudinal position of the new Car
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed
     * @param simulator OTSDEVVSimulator; the simulator that controls the new Car (and supplies the initial value for
     *            getLastEvalutionTime())
     * @param gtuFollowingModel GTUFollowingModel; the GTU following model
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @return Car; the new Car
     * @throws NamingException on network error when making the animation
     * @throws RemoteException when the simulator cannot be reached.
     * @throws NetworkException when the GTU cannot be placed on the given lane.
     * @throws SimRuntimeException when the move method cannot be scheduled.
     * @throws GTUException when construction of the GTU fails (probably due to an invalid parameter)
     */
    public static LaneBasedIndividualCar<Integer> makeReferenceCar(final int nr, final Lane lane,
            final DoubleScalar.Rel<LengthUnit> initialPosition, final DoubleScalar.Abs<SpeedUnit> initialSpeed,
            final OTSDEVSSimulator simulator, GTUFollowingModel gtuFollowingModel, LaneChangeModel laneChangeModel)
            throws RemoteException, NamingException, NetworkException, SimRuntimeException, GTUException
    {
        GTUType<String> carType = new GTUType<String>("Car");
        DoubleScalar.Rel<LengthUnit> length = new DoubleScalar.Rel<LengthUnit>(5.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions = new HashMap<>();
        initialLongitudinalPositions.put(lane, initialPosition);
        DoubleScalar.Abs<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(120, SpeedUnit.KM_PER_HOUR);
        return new LaneBasedIndividualCar<Integer>(nr, carType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions, initialSpeed, length, width, maxSpeed, new Route(
                        new ArrayList<Node<?, ?>>()), simulator);
    }

    /**
     * @return a lane of 1000 m long.
     * @throws NetworkException on network error
     */
    public static Lane makeLane() throws NetworkException
    {
        NodeGeotools.STR n1 = new NodeGeotools.STR("n1", new Coordinate(0, 0));
        NodeGeotools.STR n2 = new NodeGeotools.STR("n2", new Coordinate(10000.0, 0.0));
        CrossSectionLink<String, String> link12 =
                new CrossSectionLink<>("link12", n1, n2, new DoubleScalar.Rel<LengthUnit>(10000.0, LengthUnit.METER));
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(10000.0, 0.0)};
        LineString line = factory.createLineString(coordinates);
        new LinearGeometry(link12, line, null);
        LaneType<String> carLaneType = new LaneType<String>("CarLane");
        DoubleScalar.Rel<LengthUnit> latPos = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        DoubleScalar.Abs<FrequencyUnit> f200 = new DoubleScalar.Abs<FrequencyUnit>(200.0, FrequencyUnit.PER_HOUR);
        return new Lane(link12, latPos, latPos, width, width, carLaneType, LongitudinalDirectionality.FORWARD, f200, XXX);
    }

    /** the helper model. */
    protected static class Model implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20141027L;

        /** The simulator. */
        private OTSDEVSSimulator simulator;

        /** {@inheritDoc} */
        @Override
        public final void constructModel(
                final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            this.simulator = (OTSDEVSSimulator) theSimulator;
        }

        /** {@inheritDoc} */
        @Override
        public final OTSDEVSSimulator getSimulator() throws RemoteException
        {
            return this.simulator;
        }

    }
}
