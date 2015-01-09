package org.opentrafficsim.demo.carFollowing;

import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.naming.NamingException;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

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
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.geometry.LaneFactory;
import org.opentrafficsim.demo.geometry.Node;
import org.opentrafficsim.graphs.FundamentalDiagram;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.IncompatiblePropertyException;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.SimulatorFrame;
import org.opentrafficsim.simulationengine.WrappableSimulation;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Demonstrate the FundamentalDiagram plot.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagrams implements WrappableSimulation
{
    /** The properties exhibited by this simulation. */
    private ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** Create a FundamentalDiagrams simulation. */
    public FundamentalDiagrams()
    {
        try
        {
            this.properties
                    .add(new SelectionProperty(
                            "Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account nearby vehicles, infrastructural "
                                    + "restrictions (e.g. speed limit, curvature of the road) capabilities of the vehicle and "
                                    + "personality of the driver.</html>", new String[]{"IDM", "IDM+"}, 1, false, 500));
            this.properties.add(new ProbabilityDistributionProperty("Traffic composition",
                    "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"},
                    new Double[]{0.8, 0.2}, false, 10));
        }
        catch (IncompatiblePropertyException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    public static void main(final String[] args) throws RemoteException, SimRuntimeException
    {
        // Create the simulation and wrap its panel in a JFrame. It does not get much easier/shorter than this...
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FundamentalDiagrams fundamentalDiagrams = new FundamentalDiagrams();
                    new SimulatorFrame("Fundamental Diagrams animation", fundamentalDiagrams.buildSimulator(
                            fundamentalDiagrams.getProperties()).getPanel());
                }
                catch (RemoteException | SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * @return SimpleSimulator
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    public SimpleSimulator buildSimulator(ArrayList<AbstractProperty<?>> userModifiedProperties)
            throws RemoteException, SimRuntimeException
    {
        FundamentalDiagramPlotsModel model = new FundamentalDiagramPlotsModel(userModifiedProperties);
        SimpleSimulator result =
            new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND),
                new DoubleScalar.Rel<TimeUnit>(1800.0, TimeUnit.SECOND), model, new Rectangle2D.Double(0, -100, 5000, 200));
        new ControlPanel(result);
        makePlots(model, result.getPanel());
        addInfoTab(result.getPanel());
        return result;
    }

    /**
     * make the stand-alone plots for the model and put them in the statistics panel.
     * @param model FundamentalDiagramPlotsModel; the model.
     * @param panel DSOLPanel
     */
    private static void makePlots(final FundamentalDiagramPlotsModel model,
        final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        final int panelsPerRow = 3;
        TablePanel charts = new TablePanel(4, panelsPerRow);
        panel.getTabbedPane().addTab("statistics", charts);
        for (int plotNumber = 0; plotNumber < 10; plotNumber++)
        {
            DoubleScalar.Abs<LengthUnit> detectorLocation =
                new DoubleScalar.Abs<LengthUnit>(400 + 500 * plotNumber, LengthUnit.METER);
            FundamentalDiagram fd =
                new FundamentalDiagram("Fundamental Diagram at " + detectorLocation.getSI() + "m", 1,
                    new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.MINUTE), detectorLocation);
            fd.setTitle("Density Contour Graph");
            fd.setExtendedState(Frame.MAXIMIZED_BOTH);
            model.getFundamentalDiagrams().add(fd);
            charts.setCell(fd.getContentPane(), plotNumber / panelsPerRow, plotNumber % panelsPerRow);
        }
    }

    /**
     * @param panel DSOLPanel
     */
    private static void addInfoTab(
        final DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> panel)
    {
        // Let's find some content for our infoscreen and add it to our tabbedPane
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

    /** {@inheritDoc} */
    @Override
    public String shortName()
    {
        return "Fundamental Diagrams";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "<html><h1>Fundamental Diagram Plots</H1>"
            + "Simulation of a single lane road of 5 km length. Vechicles are generated at a constant rate of "
            + "1500 veh/hour. At time 300s a blockade is inserted at position 4km; this blockade is removed at time "
            + "500s. This blockade simulates a bridge opening.<br/>"
            + "The blockade causes a traffic jam that slowly dissolves after the blockade is removed.<br />"
            + "Output is a set of Diagrams that plot observed density, flow and speed plots against each other.</html>";
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<AbstractProperty<?>> getProperties()
    {
        return this.properties;
    }
}

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s a
 * blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is IDM+ <a
 * href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
 * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
 * Output is a set of FundamentalDiagram plots for various point along the lane.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class FundamentalDiagramPlotsModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140820L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** the headway (inter-vehicle time). */
    private DoubleScalar.Rel<TimeUnit> headway;

    /** number of cars created. */
    private int carsCreated = 0;

    /** the car following model, e.g. IDM Plus for cars. */
    protected GTUFollowingModel carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    protected GTUFollowingModel carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    double carProbability;

    /** cars in the model. */
    ArrayList<Car<Integer>> cars = new ArrayList<Car<Integer>>();

    /** The blocking car. */
    Car<Integer> block = null;

    /** minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** maximum distance. */
    private DoubleScalar.Rel<LengthUnit> maximumDistance = new DoubleScalar.Rel<LengthUnit>(5000, LengthUnit.METER);

    /** The Lane containing the simulated Cars. */
    Lane lane;

    /** the speed limit. */
    DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the fundamental diagram plots. */
    private ArrayList<FundamentalDiagram> fundamentalDiagrams = new ArrayList<FundamentalDiagram>();

    /** User settable properties */
    ArrayList<AbstractProperty<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    Random randomGenerator = new Random(12345);

    /**
     * @param properties
     */
    public FundamentalDiagramPlotsModel(ArrayList<AbstractProperty<?>> properties)
    {
        this.properties = properties;
    }

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
        catch (NamingException exception)
        {
            exception.printStackTrace();
        }

        for (AbstractProperty<?> p : this.properties)
        {
            if (p instanceof SelectionProperty)
            {
                SelectionProperty sp = (SelectionProperty) p;
                if ("Car following model".equals(sp.getShortName()))
                {
                    String modelName = sp.getValue();
                    if (modelName.equals("IDM"))
                    {
                        this.carFollowingModelCars =
                            new IDM(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
                        this.carFollowingModelTrucks =
                            new IDM(new DoubleScalar.Abs<AccelerationUnit>(0.5, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
                    }
                    else if (modelName.equals("IDM+"))
                    {
                        this.carFollowingModelCars =
                            new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
                        this.carFollowingModelTrucks =
                            new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(0.5, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                                new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
                    }
                    else
                    {
                        throw new Error("Car following model " + modelName + " not implemented");
                    }
                }
                else
                {
                    throw new Error("Unhandled SelectionProperty " + p.getShortName());
                }
            }
            else if (p instanceof ProbabilityDistributionProperty)
            {
                ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) p;
                String modelName = p.getShortName();
                if (modelName.equals("Traffic composition"))
                {
                    this.carProbability = pdp.getValue()[0];
                }
                else
                {
                    throw new Error("Unhandled ProbabilityDistributionProperty " + p.getShortName());
                }
            }
            else
            {
                throw new Error("Unhandled property: " + p);
            }
        }
        // 1500 [veh / hour] == 2.4s headway
        this.headway = new DoubleScalar.Rel<TimeUnit>(3600.0 / 1500.0, TimeUnit.SECOND);

        try
        {
            // Schedule creation of the first car (this will re-schedule itself one headway later, etc.).
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this, "generateCar",
                null);
            // Create a block at t = 5 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(300, TimeUnit.SECOND), this, this, "createBlock",
                null);
            // Remove the block at t = 7 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(420, TimeUnit.SECOND), this, this, "removeBlock",
                null);
            // Schedule regular updates of the graph
            for (int t = 1; t <= 1800; t++)
            {
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(t - 0.001, TimeUnit.SECOND), this, this,
                    "drawGraphs", null);
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
        try
        {
            this.block =
                new IDMCar(999999, null, this.simulator, this.carFollowingModelCars, new DoubleScalar.Rel<LengthUnit>(4,
                    LengthUnit.METER), this.simulator.getSimulatorTime().get(), initialPositions,
                    new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
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
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lane, initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength =
                new DoubleScalar.Rel<LengthUnit>(generateTruck ? 15 : 4, LengthUnit.METER);
            IDMCar car =
                new IDMCar(++this.carsCreated, null, this.simulator, generateTruck ? this.carFollowingModelTrucks
                    : this.carFollowingModelCars, vehicleLength, this.simulator.getSimulatorTime().get(), initialPositions,
                    initialSpeed);
            this.cars.add(0, car);
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
        }
        catch (RemoteException | SimRuntimeException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * 
     */
    protected final void drawGraphs()
    {
        // Notify the Fundamental Diagram plots that the underlying data has changed
        for (FundamentalDiagram fd : this.fundamentalDiagrams)
        {
            fd.reGraph();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
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
    protected class IDMCar extends Car<Integer>
    {
        /** */
        private static final long serialVersionUID = 20141031L;

        /**
         * Create a new IDMCar.
         * @param id integer; the id of the new IDMCar
         * @param gtuType GTUType&lt;String&gt;; the type of the GTU
         * @param simulator OTSDEVSSimulator; the simulator that runs the new IDMCar
         * @param carFollowingModel CarFollowingModel; the car following model of the new IDMCar
         * @param vehicleLength DoubleScalar.Rel&lt;LengthUnit&gt;; the length of the new IDMCar
         * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time of first evaluation of the new IDMCar
         * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial lane positions
         *            of the new IDMCar
         * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         * @throws NamingException on ???
         * @throws RemoteException on communication failure
         */
        public IDMCar(final int id, GTUType<String> gtuType, final OTSDEVSSimulatorInterface simulator,
            final GTUFollowingModel carFollowingModel, DoubleScalar.Rel<LengthUnit> vehicleLength,
            final DoubleScalar.Abs<TimeUnit> initialTime,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException, NamingException
        {
            super(id, gtuType, carFollowingModel, initialLongitudinalPositions, initialSpeed, vehicleLength,
                new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                    SpeedUnit.KM_PER_HOUR), simulator);
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
         * @throws RemoteException on communication failure
         * @throws NetworkException on network inconsistency
         * @throws SimRuntimeException on ???
         */
        protected final void move() throws RemoteException, NetworkException, SimRuntimeException
        {
            // System.out.println("move " + this.getId());
            if (this == FundamentalDiagramPlotsModel.this.block)
            {
                return;
            }
            Lane lane = positions(getFront()).keySet().iterator().next();
            if (position(lane, getFront()).getSI() > getMaximumDistance().getSI())
            {
                FundamentalDiagramPlotsModel.this.cars.remove(this);
                return;
            }
            Collection<Car<Integer>> leaders = new ArrayList<Car<Integer>>();
            // FIXME: there should be a much easier way to obtain the leader; we should not have to maintain our own
            // list
            int carIndex = FundamentalDiagramPlotsModel.this.cars.indexOf(this);
            if (carIndex < FundamentalDiagramPlotsModel.this.cars.size() - 1)
            {
                leaders.add(FundamentalDiagramPlotsModel.this.cars.get(carIndex + 1));
            }
            GTUFollowingModelResult cfmr =
                getGTUFollowingModel().computeAcceleration(this, leaders, FundamentalDiagramPlotsModel.this.speedLimit);
            if (null != FundamentalDiagramPlotsModel.this.block)
            {
                leaders.clear();
                leaders.add(FundamentalDiagramPlotsModel.this.block);
                GTUFollowingModelResult blockCFMR =
                    getGTUFollowingModel().computeAcceleration(this, leaders, FundamentalDiagramPlotsModel.this.speedLimit);
                if (blockCFMR.getAcceleration().getSI() < cfmr.getAcceleration().getSI()
                    && blockCFMR.getAcceleration().getSI() >= -5)
                {
                    cfmr = blockCFMR;
                }
            }
            setState(cfmr);

            // Add the movement of this Car to the Fundamental Diagram plots
            addToFundamentalDiagramPlots(this);
            getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this, this, "move", null);
        }

        /**
         * @param idmCar IDMCar
         * @throws NetworkException on network inconsistency
         */
        private void addToFundamentalDiagramPlots(final IDMCar idmCar) throws RemoteException, NetworkException
        {
            DoubleScalar.Abs<TimeUnit> lowerBound = idmCar.getLastEvaluationTime();
            DoubleScalar.Abs<TimeUnit> upperBound = idmCar.getNextEvaluationTime();
            DoubleScalar.Rel<LengthUnit> beginPosition =
                idmCar.position(FundamentalDiagramPlotsModel.this.lane, idmCar.getFront(), lowerBound);
            DoubleScalar.Rel<LengthUnit> endPosition =
                idmCar.position(FundamentalDiagramPlotsModel.this.lane, idmCar.getFront(), upperBound);
            for (FundamentalDiagram fd : getFundamentalDiagrams())
            {
                DoubleScalar.Abs<LengthUnit> detectorPosition = fd.getPosition();
                if (beginPosition.getSI() <= detectorPosition.getSI() && endPosition.getSI() > detectorPosition.getSI())
                {
                    // This car passes the detector; add the movement of this Car to the fundamental diagram plot
                    // Figure out at what time the car passes the detector.
                    // For this demo we use bisection to converge to the correct time.
                    final double maximumTimeError = 0.01; // [s]
                    DoubleScalar.Abs<TimeUnit> passingTime = lowerBound;
                    while (upperBound.getSI() - lowerBound.getSI() > maximumTimeError)
                    {
                        passingTime =
                            new DoubleScalar.Abs<TimeUnit>((lowerBound.getSI() + upperBound.getSI()) / 2, TimeUnit.SECOND);
                        DoubleScalar.Rel<LengthUnit> position =
                            idmCar.position(FundamentalDiagramPlotsModel.this.lane, idmCar.getFront(), passingTime);
                        if (position.getSI() > detectorPosition.getSI())
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
