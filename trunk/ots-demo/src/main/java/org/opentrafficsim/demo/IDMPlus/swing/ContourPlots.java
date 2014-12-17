package org.opentrafficsim.demo.IDMPlus.swing;

import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JScrollPane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.IDMPlus.swing.animation.AnimatedCar;
import org.opentrafficsim.demo.IDMPlus.swing.animation.CarAnimation;
import org.opentrafficsim.demo.geometry.LaneFactory;
import org.opentrafficsim.demo.geometry.Node;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.SimulatorFrame;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Simplest contour plots demonstration.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlots
{
    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    public static void main(final String[] args) throws RemoteException, SimRuntimeException
    {
        // Create the simulation and wrap its panel in a JFrame. It does not get much easier/shorter than this...
        new SimulatorFrame("Contour Plots animation", buildSimulator().getPanel());
    }

    /**
     * Create the simulation.
     * @return SimpleSimulator; the simulation
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     */
    public static SimpleSimulator buildSimulator() throws SimRuntimeException, RemoteException
    {
        ContourPlotsModel model = new ContourPlotsModel();
        SimpleSimulator result = new SimpleSimulator(new OTSSimTimeDouble(
                new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)), new DoubleScalar.Rel<TimeUnit>(0.0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0, TimeUnit.SECOND), model,
                new Rectangle2D.Double(0, -100, 5000, 200));
        new ControlPanel(result);

        // Make the info tab
        String helpSource = "/" + ContourPlotsModel.class.getPackage().getName().replace('.', '/') + "/IDMPlus.html";
        URL page = ContourPlotsModel.class.getResource(helpSource);
        if (page != null)
        {
            HTMLPanel htmlPanel;
            try
            {
                htmlPanel = new HTMLPanel(page);
                result.getPanel().getTabbedPane().addTab("info", new JScrollPane(htmlPanel));
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        // Make the tab with the contour plots
        TablePanel charts = new TablePanel(2, 2);
        result.getPanel().getTabbedPane().addTab("statistics", charts);

        // Make the four contour plots
        ContourPlot cp;

        cp = new DensityContourPlot("DensityPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Density Contour Graph");
        cp.setExtendedState(Frame.MAXIMIZED_BOTH);
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 0);

        cp = new SpeedContourPlot("SpeedPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Speed Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 0);

        cp = new FlowContourPlot("FlowPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("FLow Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 1);

        cp = new AccelerationContourPlot("AccelerationPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Acceleration Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 1);
        
        return result;
    }

}

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s
 * a blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is
 * IDM+ <a href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with
 * Relaxation and Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class ContourPlotsModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** the headway (inter-vehicle time). */
    private DoubleScalar.Rel<TimeUnit> headway;

    /** number of cars created. */
    private int carsCreated = 0;

    /** the car following model, e.g. IDM Plus. */
    protected GTUFollowingModel carFollowingModel;

    /** cars in the model. */
    ArrayList<AnimatedCar> cars = new ArrayList<AnimatedCar>();

    /** The blocking car. */
    protected AnimatedCar block = null;

    /** minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** maximum distance. */
    private DoubleScalar.Rel<LengthUnit> maximumDistance = new DoubleScalar.Rel<LengthUnit>(5000, LengthUnit.METER);

    /** The Lane that contains the simulated Cars. */
    Lane lane;

    /** the speed limit. */
    DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the contour plots. */
    private ArrayList<ContourPlot> contourPlots = new ArrayList<ContourPlot>();

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        Node from = new Node("From", new Coordinate(getMinimumDistance().getSI(), 0, 0));
        Node to = new Node("To", new Coordinate(getMaximumDistance().getSI(), 0, 0));
        try
        {
            LaneType<String> laneType = new LaneType<String>("CarLane");
            this.lane = LaneFactory.makeLane("Lane", from, to, null, laneType, this.simulator);
            this.carFollowingModel =
                    new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
            this.carFollowingModel =
                    new IDM(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
            // this.carFollowingModel = new IDM();
            // 1500 [veh / hour] == 2.4s headway
            this.headway = new DoubleScalar.Rel<TimeUnit>(3600.0 / 1500.0, TimeUnit.SECOND);
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
                        "drawGraphs", null);
            }
        }
        catch (RemoteException | SimRuntimeException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add one movement step of one Car to all contour plots.
     * @param car Car
     * @throws RemoteException on communications failure
     */
    protected final void addToContourPlots(final Car<?> car) throws RemoteException
    {
        for (ContourPlot contourPlot : this.contourPlots)
        {
            contourPlot.addData(car);
        }
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraphs()
    {
        for (ContourPlot contourPlot : this.contourPlots)
        {
            contourPlot.reGraph();
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
        try
        {
            this.block =
                    new IDMCar(999999, null, this.simulator, this.carFollowingModel, this.simulator.getSimulatorTime()
                            .get(), initialPositions, new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
        }
        catch (NamingException exception)
        {
            exception.printStackTrace();
        }
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
     * @throws NamingException on ???
     */
    protected final void generateCar()
    {
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lane, initialPosition);
        try
        {
            IDMCar car =
                    new IDMCar(++this.carsCreated, null, this.simulator, this.carFollowingModel, this.simulator
                            .getSimulatorTime().get(), initialPositions, initialSpeed);
            this.cars.add(0, car);
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
            new CarAnimation(car, this.simulator);
        }
        catch (RemoteException | SimRuntimeException | NamingException exception)
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

    /**
     * @return contourPlots
     */
    public final ArrayList<ContourPlot> getContourPlots()
    {
        return this.contourPlots;
    }

    /**
     * @return minimumDistance
     */
    public final DoubleScalar.Rel<LengthUnit> getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximumDistance
     */
    public final DoubleScalar.Rel<LengthUnit> getMaximumDistance()
    {
        return this.maximumDistance;
    }

    /** Inner class IDMCar. */
    protected class IDMCar extends AnimatedCar
    {
        /** */
        private static final long serialVersionUID = 20141030L;

        /**
         * Create a new IDMCar.
         * @param id integer; the id of the new IDMCar
         * @param gtuType GTUType&lt;String&gt;; the type of the GTU
         * @param simulator OTSDEVSSimulator; the simulator that runs the new IDMCar
         * @param carFollowingModel CarFollowingModel; the car following model of the new IDMCar
         * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time of first evaluation of the new IDMCar
         * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial lane
         *            positions of the new IDMCar
         * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         * @throws NamingException on ???
         * @throws RemoteException on communication failure
         */
        public IDMCar(final int id, GTUType<String> gtuType, final OTSDEVSSimulatorInterface simulator,
                final GTUFollowingModel carFollowingModel, final DoubleScalar.Abs<TimeUnit> initialTime,
                final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
                final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException, NamingException
        {
            super(id, gtuType, simulator, carFollowingModel, initialTime, initialLongitudinalPositions, initialSpeed);
            try
            {
                simulator.scheduleEventAbs(simulator.getSimulatorTime(), this, this, "move", null);
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * @throws RemoteException RemoteException
         * @throws NamingException on ???
         * @throws NetworkException on network inconsistency
         * @throws SimRuntimeException on ??
         */
        protected final void move() throws RemoteException, NamingException, NetworkException, SimRuntimeException
        {
            // System.out.println("move " + getId());
            if (this == ContourPlotsModel.this.block)
            {
                return;
            }
            if (positionOfFront().getLongitudinalPosition().getSI() > getMaximumDistance().getSI())
            {
                ContourPlotsModel.this.cars.remove(this);
                return;
            }
            Collection<AnimatedCar> leaders = new ArrayList<AnimatedCar>();
            // FIXME: there should be a much easier way to obtain the leader; we should not have to maintain our own
            // list
            int carIndex = ContourPlotsModel.this.cars.indexOf(this);
            if (carIndex < ContourPlotsModel.this.cars.size() - 1)
            {
                leaders.add(ContourPlotsModel.this.cars.get(carIndex + 1));
            }
            GTUFollowingModelResult cfmr =
                    ContourPlotsModel.this.carFollowingModel.computeAcceleration(this, leaders,
                            ContourPlotsModel.this.speedLimit);
            if (null != ContourPlotsModel.this.block)
            {
                leaders.clear();
                leaders.add(ContourPlotsModel.this.block);
                GTUFollowingModelResult blockCFMR =
                        ContourPlotsModel.this.carFollowingModel.computeAcceleration(this, leaders,
                                ContourPlotsModel.this.speedLimit);
                if (blockCFMR.getAcceleration().getSI() < cfmr.getAcceleration().getSI()
                        && blockCFMR.getAcceleration().getSI() >= -5)
                {
                    cfmr = blockCFMR;
                }
            }
            setState(cfmr);
            // Add the movement of this Car to the contour plots
            addToContourPlots(this);
            // Schedule the next evaluation of this car
            getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this, this, "move",
                    null);
        }
    }
}
