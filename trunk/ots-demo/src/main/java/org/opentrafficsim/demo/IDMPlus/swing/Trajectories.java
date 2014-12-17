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
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;
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
import org.opentrafficsim.demo.geometry.LaneFactory;
import org.opentrafficsim.demo.geometry.Node;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.SimulatorFrame;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Demonstrate the Trajectories plot.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Trajectories
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
        new SimulatorFrame("Trajectory Plots animation", buildSimulator().getPanel());
    }

    /**
     * @return
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    private static SimpleSimulator buildSimulator() throws RemoteException, SimRuntimeException
    {
        InternalTrajectoriesModel model = new InternalTrajectoriesModel();
        SimpleSimulator result =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(0, -100, 5000, 200));
        new ControlPanel(result);
        makePlot(model, result.getPanel());
        addInfoTab(result.getPanel());
        return result;
    }

    /**
     * make the stand-alone plot for the model and put it in the statistics panel.
     * @param model the model.
     * @param panel DSOLPanel
     */
    private static void makePlot(final InternalTrajectoriesModel model,
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        TablePanel charts = new TablePanel(1, 1);
        panel.getTabbedPane().addTab("statistics", charts);
        DoubleScalar.Rel<TimeUnit> sampleInterval = new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND);
        TrajectoryPlot tp =
                new TrajectoryPlot("Trajectory Plot", sampleInterval, model.getMinimumDistance(),
                        model.getMaximumDistance());
        tp.setTitle("Density Contour Graph");
        tp.setExtendedState(Frame.MAXIMIZED_BOTH);
        model.setTrajectoryPlot(tp);
        charts.setCell(tp.getContentPane(), 0, 0);
    }

    /**
     * @param panel DSOLPanel
     */
    private static void addInfoTab(
            final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        // Let's find some content for our info screen and add it to our tabbedPane
        String helpSource = "/" + ContourPlotsModel.class.getPackage().getName().replace('.', '/') + "/package.html";
        URL page = ContourPlotsModel.class.getResource(helpSource);
        if (page != null)
        {
            HTMLPanel htmlPanel;
            try
            {
                htmlPanel = new HTMLPanel(page);
                panel.getTabbedPane().addTab("info", new JScrollPane(htmlPanel));
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

}

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s
 * a blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is
 * IDM+ <a href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with
 * Relaxation and Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
 * Output is a trajectory plot with simulation time along the horizontal axis and distance along the road along the
 * vertical axis.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class InternalTrajectoriesModel implements OTSModelInterface
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
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        Node from = new Node("From", new Coordinate(getMinimumDistance().getSI(), 0, 0));
        Node to = new Node("To", new Coordinate(getMaximumDistance().getSI(), 0, 0));
        LaneType<String> laneType = new LaneType<String>("CarLane");
        try
        {
            this.lane = LaneFactory.makeLane("Lane", from, to, null, laneType, this.simulator);
        }
        catch (NamingException exception1)
        {
            exception1.printStackTrace();
        }

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
                    new IDMCar(++this.carsCreated, null, this.simulator, this.carFollowingModel, this.simulator
                            .getSimulatorTime().get(), initialPositions, initialSpeed);
            this.cars.add(0, car);
            // Re-schedule this method after headway seconds
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
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
         * @throws NamingException ...
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
                InternalTrajectoriesModel.this.cars.remove(this);
                return;
            }
            Collection<Car<Integer>> leaders = new ArrayList<Car<Integer>>();
            // FIXME: there should be a much easier way to obtain the leader; we should not have to maintain our own
            // list
            int carIndex = InternalTrajectoriesModel.this.cars.indexOf(this);
            if (carIndex < InternalTrajectoriesModel.this.cars.size() - 1)
            {
                leaders.add(InternalTrajectoriesModel.this.cars.get(carIndex + 1));
            }
            GTUFollowingModelResult cfmr =
                    InternalTrajectoriesModel.this.carFollowingModel.computeAcceleration(this, leaders,
                            InternalTrajectoriesModel.this.speedLimit);
            if (null != InternalTrajectoriesModel.this.block)
            {
                leaders.clear();
                leaders.add(InternalTrajectoriesModel.this.block);
                if (positionOfFront().getLongitudinalPosition().getSI() > 3850
                        && positionOfFront().getLongitudinalPosition().getSI() < 4000 && getId() == 57
                        && getNextEvaluationTime().getSI() > 312)
                {
                    System.out.println("Pas op; vehicle " + this);
                }
                GTUFollowingModelResult blockCFMR =
                        InternalTrajectoriesModel.this.carFollowingModel.computeAcceleration(this, leaders,
                                InternalTrajectoriesModel.this.speedLimit);
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
