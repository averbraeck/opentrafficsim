package org.opentrafficsim.core.car;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.experiment.Treatment;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.FixedAccelerationModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CarTest implements DOUBLE_SCALAR
{
    /**
     * Test some basics of the Car class.
     * @throws RemoteException on network error
     * @throws NetworkException on ???
     * @throws SimRuntimeException on ???
     * @throws NamingException on ???
     * @throws GTUException on ???
     * @throws OTSGeometryException when center line or contour of a link or lane cannot be generated
     */
    @SuppressWarnings("static-method")
    @Test
    public final void carTest() throws RemoteException, NetworkException, SimRuntimeException, NamingException,
        GTUException, OTSGeometryException
    {
        Time.Abs initialTime = new Time.Abs(0, SECOND);
        GTUType gtuType = GTUType.makeGTUType("Car");
        LaneType laneType = new LaneType("CarLane");
        laneType.addCompatibility(gtuType);
        Lane lane = makeLane(laneType);
        Length.Rel initialPosition = new Length.Rel(12, METER);
        Speed.Abs initialSpeed = new Speed.Abs(34, KM_PER_HOUR);
        OTSDEVSSimulator simulator = makeSimulator();
        GTUFollowingModel gtuFollowingModel =
            new FixedAccelerationModel(new Acceleration.Abs(0, METER_PER_SECOND_2), new Time.Rel(10,
                SECOND));
        LaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualCar referenceCar =
            makeReferenceCar("12345", gtuType, lane, initialPosition, initialSpeed, simulator, gtuFollowingModel,
                laneChangeModel);
        assertEquals("The car should store it's ID", "12345", referenceCar.getId());
        assertEquals("At t=initialTime the car should be at it's initial position", initialPosition.getSI(), referenceCar
            .position(lane, referenceCar.getReference(), initialTime).getSI(), 0.0001);
        assertEquals("The car should store it's initial speed", initialSpeed.getSI(), referenceCar.getLongitudinalVelocity(
            initialTime).getSI(), 0.00001);
        assertEquals("The car should have an initial acceleration equal to 0", 0, referenceCar.getAcceleration(initialTime)
            .getSI(), 0.0001);
        assertEquals("The gtu following model should be " + gtuFollowingModel, gtuFollowingModel, referenceCar
            .getGTUFollowingModel());
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
        Experiment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> exp =
            new Experiment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>();
        Treatment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> tr =
            new Treatment<>(exp, "tr1", new OTSSimTimeDouble(new Time.Abs(0, SECOND)), new Time.Rel(0, SECOND),
                new Time.Rel(3600.0, SECOND));
        exp.setTreatment(tr);
        exp.setModel(model);
        Replication<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> rep = new Replication<>(exp);
        simulator.initialize(rep, ReplicationMode.TERMINATING);
        return simulator;
    }

    /**
     * Create a new Car.
     * @param id String; the name (number) of the Car
     * @param gtuType GTUType; the type of the new car
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
    public static LaneBasedIndividualCar makeReferenceCar(final String id, final GTUType gtuType, final Lane lane,
        final Length.Rel initialPosition, final Speed.Abs initialSpeed,
        final OTSDEVSSimulator simulator, final GTUFollowingModel gtuFollowingModel, final LaneChangeModel laneChangeModel)
        throws RemoteException, NamingException, NetworkException, SimRuntimeException, GTUException
    {
        Length.Rel length = new Length.Rel(5.0, METER);
        Length.Rel width = new Length.Rel(2.0, METER);
        Map<Lane, Length.Rel> initialLongitudinalPositions = new HashMap<>();
        initialLongitudinalPositions.put(lane, initialPosition);
        Speed.Abs maxSpeed = new Speed.Abs(120, KM_PER_HOUR);
        return new LaneBasedIndividualCar(id, gtuType, gtuFollowingModel, laneChangeModel, initialLongitudinalPositions,
            initialSpeed, length, width, maxSpeed, new CompleteLaneBasedRouteNavigator(new CompleteRoute("")), simulator);
    }

    /**
     * @param laneType LaneType&lt;String&gt;; the type of the lane
     * @return a lane of 1000 m long.
     * @throws NetworkException on network error
     * @throws OTSGeometryException when center line or contour of a link or lane cannot be generated
     */
    public static Lane makeLane(final LaneType laneType) throws NetworkException, OTSGeometryException
    {
        OTSNode n1 = new OTSNode("n1", new OTSPoint3D(0, 0));
        OTSNode n2 = new OTSNode("n2", new OTSPoint3D(100000.0, 0.0));
        OTSPoint3D[] coordinates = new OTSPoint3D[]{new OTSPoint3D(0.0, 0.0), new OTSPoint3D(100000.0, 0.0)};
        CrossSectionLink link12 = new CrossSectionLink("link12", n1, n2, new OTSLine3D(coordinates));
        Length.Rel latPos = new Length.Rel(0.0, METER);
        Length.Rel width = new Length.Rel(4.0, METER);
        return new Lane(link12, "lane.1", latPos, latPos, width, width, laneType, LongitudinalDirectionality.FORWARD,
            new Speed.Abs(100, KM_PER_HOUR));
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
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
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
