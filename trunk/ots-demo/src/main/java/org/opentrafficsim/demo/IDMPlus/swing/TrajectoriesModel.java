package org.opentrafficsim.demo.IDMPlus.swing;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.car.following.IDMPlus;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.graphs.TrajectoryPlot;

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
    protected CarFollowingModel<Car> carFollowingModel;

    /** cars in the model. */
    protected ArrayList<Car> cars = new ArrayList<Car>();

    /** minimum distance. */
    private DoubleScalar.Abs<LengthUnit> minimumDistance = new DoubleScalar.Abs<LengthUnit>(0, LengthUnit.METER);

    /** maximum distance. */
    private DoubleScalar.Abs<LengthUnit> maximumDistance = new DoubleScalar.Abs<LengthUnit>(5000, LengthUnit.METER);

    /** the speed limit. */
    private DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the trajectory plot. */
    private TrajectoryPlot trajectoryPlot;

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulator) simulator;

        this.carFollowingModel = new IDMPlus<Line<String>, Car>();

        // 1500 [veh / hour] == 2.4s headway
        this.headway = new DoubleScalar.Rel<TimeUnit>(3600.0 / 1500.0, TimeUnit.SECOND);

        try
        {
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "generateCar", null);
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(1799.99, TimeUnit.SECOND), this, this,
                    "drawGraph", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        DoubleScalar.Abs<LengthUnit> initialPosition = new DoubleScalar.Abs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Rel<SpeedUnit> initialSpeed = new DoubleScalar.Rel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        IDMCar car =
                new IDMCar(++this.carsCreated, this.simulator, this.carFollowingModel, this.simulator
                        .getSimulatorTime().get(), initialPosition, initialSpeed);
        this.cars.add(0, car);
        try
        {
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
    protected class IDMCar extends Car
    {
        /**
         * Create a new IDMCar.
         * @param id integer; the id of the new IDMCar
         * @param simulator OTSDEVSSimulator; the simulator that runs the new IDMCar
         * @param carFollowingModel CarFollowingModel; the car following model of the new IDMCar
         * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time of first evaluation of the new IDMCar
         * @param initialPosition DoubleScalar.Abs&lt;LengthUnit&gt;; the initial position of the new IDMCar
         * @param initialSpeed DoubleScalar.Rel&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         */
        public IDMCar(final int id, final OTSDEVSSimulator simulator, final CarFollowingModel carFollowingModel,
                final DoubleScalar.Abs<TimeUnit> initialTime, final DoubleScalar.Abs<LengthUnit> initialPosition,
                final DoubleScalar.Rel<SpeedUnit> initialSpeed)
        {
            super(id, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
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
         * @throws RemoteException
         */
        protected final void move() throws RemoteException
        {
            System.out.println("move " + this.getID());
            DoubleScalar.Abs<TimeUnit> now = getSimulator().getSimulatorTime().get();
            if (getPosition(now).getValueSI() > TrajectoriesModel.this.maximumDistance.getValueSI())
            {
                TrajectoriesModel.this.cars.remove(this);
                return;
            }
            Collection<Car> leaders = new ArrayList<Car>();
            int carIndex = TrajectoriesModel.this.cars.indexOf(this);
            if (carIndex < TrajectoriesModel.this.cars.size() - 1)
            {
                leaders.add(TrajectoriesModel.this.cars.get(carIndex + 1));
            }
            // Add a stationary car at 4000m to simulate an opening bridge
            if (now.getValueSI() >= 300 && now.getValueSI() < 500)
            {
                Car block =
                        new Car(99999, null, TrajectoriesModel.this.carFollowingModel, now,
                                new DoubleScalar.Abs<LengthUnit>(4000, LengthUnit.METER),
                                new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
                leaders.add(block);
            }
            CarFollowingModelResult cfmr =
                    TrajectoriesModel.this.carFollowingModel.computeAcceleration(this, leaders,
                            TrajectoriesModel.this.speedLimit);
            setState(cfmr);

            // Add the movement of this Car to the contour plots
            addToTrajectoryPlot(this);

            try
            {
                getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this, this,
                        "move", null);
            }
            catch (RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }

    }

    /**
     * @param idmCar IDMCar
     */
    private void addToTrajectoryPlot(final IDMCar idmCar)
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
    public final DoubleScalar.Abs<LengthUnit> getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximum distance of the simulation
     */
    public final DoubleScalar.Abs<LengthUnit> getMaximumDistance()
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
