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
import org.opentrafficsim.graphs.FundamentalDiagram;

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
public class FundamentalDiagramPlotsModel implements OTSModelInterface
{

    /** */
    private static final long serialVersionUID = 20140820L;

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

    /** the fundamental diagram plots. */
    private ArrayList<FundamentalDiagram> fundamentalDiagrams = new ArrayList<FundamentalDiagram>();

    /** {@inheritDoc} */
    @Override
    public void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulator) simulator;

        this.carFollowingModel = new IDMPlus<Line<String>>();

        // 1500 [veh / hour] == 2.4s headway
        this.headway = new DoubleScalar.Rel<TimeUnit>(3600.0 / 1500.0, TimeUnit.SECOND);

        try
        {
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "generateCar", null);
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(1799.99, TimeUnit.SECOND), this, this,
                    "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Method to generate cars that schedules itself till end of run.
     */
    protected void generateCar()
    {
        DoubleScalar.Rel<SpeedUnit> initialSpeed = new DoubleScalar.Rel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        IDMCar car =
                new IDMCar(++this.carsCreated, this.simulator, this.carFollowingModel, this.simulator
                        .getSimulatorTime().get(), this.minimumDistance, initialSpeed);
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
     * 
     */
    protected void drawGraphs()
    {
        // Notify the Fundamental Diagram plots that the underlying data has changed
        for (FundamentalDiagram fd : this.fundamentalDiagrams)
        {
            fd.reGraph();
        }
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return null;
    }

    /**
     * @return fundamentalDiagramPlots
     */
    public final ArrayList<FundamentalDiagram> getFundamentalDiagrams()
    {
        return this.fundamentalDiagrams;
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
            if (getPosition(now).getValueSI() > FundamentalDiagramPlotsModel.this.maximumDistance.getValueSI())
            {
                FundamentalDiagramPlotsModel.this.cars.remove(this);
                return;
            }
            Collection<Car> leaders = new ArrayList<Car>();
            int carIndex = FundamentalDiagramPlotsModel.this.cars.indexOf(this);
            if (carIndex < FundamentalDiagramPlotsModel.this.cars.size() - 1)
            {
                leaders.add(FundamentalDiagramPlotsModel.this.cars.get(carIndex + 1));
            }
            // Add a stationary car at 4000m to simulate an opening bridge
            if (now.getValueSI() >= 300 && now.getValueSI() < 500)
            {
                Car block =
                        new Car(99999, null, FundamentalDiagramPlotsModel.this.carFollowingModel, now,
                                new DoubleScalar.Abs<LengthUnit>(4000, LengthUnit.METER),
                                new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
                leaders.add(block);
            }
            CarFollowingModelResult cfmr =
                    FundamentalDiagramPlotsModel.this.carFollowingModel.computeAcceleration(this, leaders,
                            FundamentalDiagramPlotsModel.this.speedLimit);
            setState(cfmr);

            // Add the movement of this Car to the Fundamental Diagram plots
            addToFundamentalDiagramPlots(this);

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

        /**
         * @param idmCar IDMCar
         */
        private void addToFundamentalDiagramPlots(final IDMCar idmCar)
        {
            DoubleScalar.Abs<TimeUnit> lowerBound = idmCar.getLastEvaluationTime();
            DoubleScalar.Abs<TimeUnit> upperBound = idmCar.getNextEvaluationTime();
            for (FundamentalDiagram fd : getFundamentalDiagrams())
            {
                DoubleScalar.Abs<LengthUnit> detectorPosition = fd.getPosition();
                if (idmCar.getPosition(lowerBound).getValueSI() <= detectorPosition.getValueSI()
                        && idmCar.getPosition(upperBound).getValueSI() > detectorPosition.getValueSI())
                {
                    // This car passes the detector; add the movement of this Car to the fundamental diagram plot
                    // Figure out at what time the car passes the detector.
                    // For this demo we use bisection to converge to the correct time.
                    final double maximumTimeError = 0.01; // [s]
                    DoubleScalar.Abs<TimeUnit> passingTime = lowerBound;
                    while (upperBound.getValueSI() - lowerBound.getValueSI() > maximumTimeError)
                    {
                        passingTime =
                                new DoubleScalar.Abs<TimeUnit>((lowerBound.getValueSI() + upperBound.getValueSI()) / 2,
                                        TimeUnit.SECOND);
                        DoubleScalar.Abs<LengthUnit> position = idmCar.getPosition(passingTime);
                        if (position.getValueSI() > detectorPosition.getValueSI())
                        {
                            lowerBound = passingTime;
                        }
                        else
                        {
                            upperBound = passingTime;
                        }
                    }
                    fd.addData(0, idmCar, passingTime);
                }
            }
        }
    }

}
