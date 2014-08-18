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
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;
import org.opentrafficsim.graphs.ContourPlot;

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s
 * a blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is
 * IDM+ <a href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with
 * Relaxation and Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br />
 * Output is a set of block charts:
 * <ul>
 * <li>Traffic density</li>
 * <li>Speed</li>
 * <li>Flow</li>
 * <li>Acceleration</li>
 * </ul>
 * All these graphs display simulation time along the horizontal axis and distance along the road along the vertical
 * axis.
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
 * @version Aug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlotsModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** the simulator */
    private OTSDEVSSimulator simulator;

    /** the headway (inter-vehicle time) */
    private DoubleScalarRel<TimeUnit> headway;

    /** number of cars created */
    private int carsCreated = 0;

    /** the car following model, e.g. IDM Plus */
    protected CarFollowingModel carFollowingModel;

    /** cars in the model */
    private ArrayList<Car> cars = new ArrayList<Car>();

    /** minimum distance */
    private DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);

    /** maximum distance */
    private DoubleScalarAbs<LengthUnit> maximumDistance = new DoubleScalarAbs<LengthUnit>(5000, LengthUnit.METER);

    /** the speed limit */
    private DoubleScalarAbs<SpeedUnit> speedLimit = new DoubleScalarAbs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the contour plots */
    private ArrayList<ContourPlot> contourPlots = new ArrayList<ContourPlot>();

    /**
     * @see nl.tudelft.simulation.dsol.ModelInterface#constructModel(nl.tudelft.simulation.dsol.simulators.SimulatorInterface)
     */
    @Override
    public void constructModel(
            SimulatorInterface<DoubleScalarAbs<TimeUnit>, DoubleScalarRel<TimeUnit>, OTSSimTimeDouble> _simulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulator) _simulator;

        this.carFollowingModel = new IDMPlus<Line<String>>();

        // 1500 [veh / hour] == 2.4s headway
        this.headway = new DoubleScalarRel<TimeUnit>(3600.0 / 1500.0, TimeUnit.SECOND);

        try
        {
            this.simulator.scheduleEventAbs(new DoubleScalarAbs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "generateCar", null);
            this.simulator.scheduleEventAbs(new DoubleScalarAbs<TimeUnit>(1799.99, TimeUnit.SECOND), this, this,
                    "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @param car
     */
    protected void addToContourPlots(final Car car)
    {
        for (ContourPlot contourPlot : this.contourPlots)
            contourPlot.addData(car);
    }

    /**
     * 
     */
    protected void drawGraphs()
    {
        // Notify the contour plots that the underlying data has changed
        for (ContourPlot contourPlot : this.contourPlots)
            contourPlot.reGraph();
    }

    /**
     * Method to generate cars that schedules itself till end of run.
     */
    protected void generateCar()
    {
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
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
    public SimulatorInterface<DoubleScalarAbs<TimeUnit>, DoubleScalarRel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

    /**
     * @return contourPlots
     */
    public ArrayList<ContourPlot> getContourPlots()
    {
        return this.contourPlots;
    }

    /**
     * @return minimumDistance
     */
    public DoubleScalarAbs<LengthUnit> getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximumDistance
     */
    public DoubleScalarAbs<LengthUnit> getMaximumDistance()
    {
        return this.maximumDistance;
    }

    /** Inner class IDMCar */
    protected class IDMCar extends Car
    {
        /** local access to simulator for scheduling */
        private final OTSDEVSSimulatorInterface simulator;

        /**
         * @param id
         * @param simulator
         * @param carFollowingModel
         * @param initialTime
         * @param initialPosition
         * @param initialSpeed
         */
        public IDMCar(int id, OTSDEVSSimulatorInterface simulator, CarFollowingModel carFollowingModel,
                DoubleScalarAbs<TimeUnit> initialTime, DoubleScalarAbs<LengthUnit> initialPosition,
                DoubleScalarRel<SpeedUnit> initialSpeed)
        {
            super(id, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
            this.simulator = simulator;
            if (id != 99999)
            {
                try
                {
                    simulator.scheduleEventAbs(simulator.getSimulatorTime(), this, this, "move", null);
                }
                catch (RemoteException | SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        /**
         * @throws RemoteException
         */
        protected void move() throws RemoteException
        {
            System.out.println("move " + this.getID());
            DoubleScalarAbs<TimeUnit> now = this.simulator.getSimulatorTime().get();
            if (getPosition(now).getValueSI() > ContourPlotsModel.this.maximumDistance.getValueSI())
            {
                ContourPlotsModel.this.cars.remove(this);
                return;
            }
            Collection<Car> leaders = new ArrayList<Car>();
            int carIndex = ContourPlotsModel.this.cars.indexOf(this);
            if (carIndex < ContourPlotsModel.this.cars.size() - 1)
                leaders.add(ContourPlotsModel.this.cars.get(carIndex + 1));

            // Add a stationary car at 4000m to simulate an opening bridge
            if (now.getValueSI() >= 300 && now.getValueSI() < 500)
            {
                IDMCar block =
                        new IDMCar(99999, this.simulator, ContourPlotsModel.this.carFollowingModel, now,
                                new DoubleScalarAbs<LengthUnit>(4000, LengthUnit.METER),
                                new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
                leaders.add(block);
            }
            CarFollowingModelResult cfmr =
                    ContourPlotsModel.this.carFollowingModel.computeAcceleration(this, leaders,
                            ContourPlotsModel.this.speedLimit);
            setState(cfmr);

            // Add the movement of this Car to the contour plots
            addToContourPlots(this);

            try
            {
                this.simulator.scheduleEventRel(new DoubleScalarRel<TimeUnit>(0.5, TimeUnit.SECOND), this, this,
                        "move", null);
            }
            catch (RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
