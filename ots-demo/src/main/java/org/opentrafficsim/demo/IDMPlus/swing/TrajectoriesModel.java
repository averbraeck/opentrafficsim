package org.opentrafficsim.demo.IDMPlus.swing;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.IDMPlus.swing.animation.Link;
import org.opentrafficsim.demo.IDMPlus.swing.animation.LinkAnimation;
import org.opentrafficsim.demo.geometry.LaneFactory;
import org.opentrafficsim.demo.geometry.Node;
import org.opentrafficsim.graphs.TrajectoryPlot;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 20, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoriesModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** the simulator. */
    private OTSDEVSSimulator simulator;

    /** the headway (inter-vehicle time). */
    private DoubleScalar.Rel<TimeUnit> headway;

    /** number of cars created. */
    private int carsCreated = 0;

    /** the car following model, e.g. IDM Plus. */
    protected GTUFollowingModel carFollowingModel;

    /** cars in the model. */
    protected ArrayList<Car<Integer>> cars = new ArrayList<Car<Integer>>();

    /** The blocking car. */
    protected Car<Integer> block = null;

    /** minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** maximum distance. */
    DoubleScalar.Rel<LengthUnit> maximumDistance = new DoubleScalar.Rel<LengthUnit>(5000, LengthUnit.METER);

    /** The Lane containing the simulated Cars. */
    Lane lane;

    /** the speed limit. */
    DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the trajectory plot. */
    private TrajectoryPlot trajectoryPlot;

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        Node from = new Node("From", new Coordinate(getMinimumDistance().getSI(), 0, 0));
        Node to = new Node("To", new Coordinate(getMaximumDistance().getSI(), 0, 0));
        this.lane = LaneFactory.makeLane("Lane", from, to);
        try
        {
            new LinkAnimation((Link) this.lane.getParentLink(), this.simulator, 5.0f);
        }
        catch (NamingException exception1)
        {
            exception1.printStackTrace();
        }

        // new LinkAnimation(this.lane.getParentLink(), this.simulator, 5.0f);
        this.simulator = (OTSDEVSSimulator) theSimulator;

        this.carFollowingModel = new IDMPlus((OTSDEVSSimulatorInterface) theSimulator);

        // 1500 [vehicles / hour] == 2.4s headway
        this.headway = new DoubleScalar.Rel<TimeUnit>(3600.0 / 1500.0, TimeUnit.SECOND);

        try
        {
            // Schedule creation of the first car (this will re-schedule itself one headway later, etc.).
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "generateCar", null);
            // Create a block at t = 5 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(300, TimeUnit.SECOND), this, this,
                    "createBlock", null);
            // Remove the block at t = 8 minutes, 20 seconds
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(500, TimeUnit.SECOND), this, this,
                    "removeBlock", null);
            // Schedule regular updates of the graph
            for (int t = 1; t <= 1800; t++)
            {
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(t - 0.001, TimeUnit.SECOND), this, this,
                        "drawGraph", null);
            }
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Set up the block.
     * @throws RemoteException on communications failure
     */
    protected final void createBlock() throws RemoteException
    {
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(4000, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lane, initialPosition);
        this.block =
                new Car<Integer>(999999, null, new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(0,
                                SpeedUnit.KM_PER_HOUR), null, initialPositions, new DoubleScalar.Abs<SpeedUnit>(0,
                                SpeedUnit.KM_PER_HOUR), this.simulator);
    }

    /**
     * Remove the block.
     */
    protected final void removeBlock()
    {
        this.block = null;
    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lane, initialPosition);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        try
        {
            IDMCar car =
                    new IDMCar(++this.carsCreated, this.simulator, this.carFollowingModel, this.simulator
                            .getSimulatorTime().get(), initialPositions, initialSpeed);
            this.cars.add(0, car);
            // Re-schedule this method after headway seconds
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

    /** Inner class IDMCar. */
    protected class IDMCar extends Car<Integer>
    {
        /** */
        private static final long serialVersionUID = 20141030L;

        /**
         * Create a new IDMCar.
         * @param id integer; the id of the new IDMCar
         * @param simulator OTSDEVSSimulator; the simulator that runs the new IDMCar
         * @param carFollowingModel CarFollowingModel; the car following model of the new IDMCar
         * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time of first evaluation of the new IDMCar
         * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial
         *            positions of the new IDMCar
         * @param initialSpeed DoubleScalar.Rel&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         * @throws RemoteException on communication error
         */
        public IDMCar(final int id, final OTSDEVSSimulator simulator, final GTUFollowingModel carFollowingModel,
                final DoubleScalar.Abs<TimeUnit> initialTime, /* final DoubleScalar.Abs<LengthUnit> initialPosition */
                final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
                final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException
        {
            super(id, null /* GTUType */, new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER),
                    new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                            SpeedUnit.KM_PER_HOUR), carFollowingModel, initialLongitudinalPositions, initialSpeed,
                    simulator);
            try
            {
                simulator.scheduleEventAbs(simulator.getSimulatorTime(), this, this, "move", null);
            }
            catch (RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * @throws RemoteException on communication failure
         * @throws NetworkException when the network is inconsistent
         * @throws SimRuntimeException on ???
         */
        protected final void move() throws RemoteException, NetworkException, SimRuntimeException
        {
            // System.out.println("move " + getId());
            if (positionOfFront().getLongitudinalPosition().getSI() > getMaximumDistance().getSI())
            {
                TrajectoriesModel.this.cars.remove(this);
                return;
            }
            Collection<Car<Integer>> leaders = new ArrayList<Car<Integer>>();
            // FIXME: there should be a much easier way to obtain the leader; we should not have to maintain our own
            // list
            int carIndex = TrajectoriesModel.this.cars.indexOf(this);
            if (carIndex < TrajectoriesModel.this.cars.size() - 1)
            {
                leaders.add(TrajectoriesModel.this.cars.get(carIndex + 1));
            }
            GTUFollowingModelResult cfmr =
                    TrajectoriesModel.this.carFollowingModel.computeAcceleration(this, leaders,
                            TrajectoriesModel.this.speedLimit);
            if (null != TrajectoriesModel.this.block)
            {
                leaders.clear();
                leaders.add(TrajectoriesModel.this.block);
                GTUFollowingModelResult blockCFMR =
                        TrajectoriesModel.this.carFollowingModel.computeAcceleration(this, leaders,
                                TrajectoriesModel.this.speedLimit);
                if (blockCFMR.getAcceleration().getSI() < cfmr.getAcceleration().getSI()
                        && blockCFMR.getAcceleration().getSI() >= -5)
                {
                    cfmr = blockCFMR;
                }
            }
            if (cfmr.getAcceleration().getSI() < -0.1)
            {
                // System.out.println("Deceleration: " + cfmr.getAcceleration());
            }
            setState(cfmr);

            // Add the movement of this Car to the contour plots
            addToTrajectoryPlot(this);
            getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this, this, "move",
                    null);
        }

    }

    /**
     * @param idmCar IDMCar
     * @throws RemoteException when communication fails
     */
    final void addToTrajectoryPlot(final IDMCar idmCar) throws RemoteException
    {
        this.trajectoryPlot.addData(idmCar);
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraph()
    {
        this.trajectoryPlot.reGraph();
    }

    /**
     * @return minimum distance of the simulation
     */
    public final DoubleScalar.Rel<LengthUnit> getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximum distance of the simulation
     */
    public final DoubleScalar.Rel<LengthUnit> getMaximumDistance()
    {
        return this.maximumDistance;
    }

    /**
     * @param trajectoryPlot TrajectoryPlot
     */
    public final void setTrajectoryPlot(final TrajectoryPlot trajectoryPlot)
    {
        this.trajectoryPlot = trajectoryPlot;
    }

}
