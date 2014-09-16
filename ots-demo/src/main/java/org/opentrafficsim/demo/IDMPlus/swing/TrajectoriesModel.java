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
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
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
    protected CarFollowingModel carFollowingModel;

    /** cars in the model. */
    private ArrayList<Car> cars = new ArrayList<Car>();

    /** minimum distance. */
    private DoubleScalar.Abs<LengthUnit> minimumDistance = new DoubleScalar.Abs<LengthUnit>(0, LengthUnit.METER);

    /** maximum distance. */
    private DoubleScalar.Abs<LengthUnit> maximumDistance = new DoubleScalar.Abs<LengthUnit>(5000, LengthUnit.METER);

    /** the speed limit. */
    private DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the trajectory plot. */
    private TrajectoryPlot trajectoryPlot;

    /**
     * @see nl.tudelft.simulation.dsol.ModelInterface#constructModel(nl.tudelft.simulation.dsol.simulators.SimulatorInterface)
     */
    @Override
    public void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> _simulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulator) _simulator;

        this.carFollowingModel = new IDMPlus<Line<String>>();

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
    protected void generateCar()
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

    /**
     * @see nl.tudelft.simulation.dsol.ModelInterface#getSimulator()
     */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

    /** Inner class IDMCar. */
    protected class IDMCar extends Car
    {
        /**
         * @param id
         * @param simulator
         * @param carFollowingModel
         * @param initialTime
         * @param initialPosition
         * @param initialSpeed
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
        protected void move() throws RemoteException
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
    protected void drawGraph()
    {
        this.trajectoryPlot.reGraph();
    }

    /**
     * @return minimum distance of the simulation
     */
    public DoubleScalar.Abs<LengthUnit> getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximum distance of the simulation
     */
    public DoubleScalar.Abs<LengthUnit> getMaximumDistance()
    {
        return this.maximumDistance;
    }

    /**
     * @param trajectoryPlot TrajectoryPlot
     */
    public void setTrajectories(final TrajectoryPlot trajectoryPlot)
    {
        this.trajectoryPlot = trajectoryPlot;
    }

}
