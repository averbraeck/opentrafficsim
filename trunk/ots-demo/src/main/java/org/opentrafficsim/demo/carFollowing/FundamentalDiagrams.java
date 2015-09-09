package org.opentrafficsim.demo.carFollowing;

import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.graphs.FundamentalDiagram;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

/**
 * Demonstrate the FundamentalDiagram plot.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagrams extends AbstractWrappableAnimation
{
    /** the model. */
    private FundamentalDiagramPlotsModel model;

    /** Create a FundamentalDiagrams simulation. */
    public FundamentalDiagrams()
    {
        try
        {
            this.properties.add(new SelectionProperty("Car following model", "<html>The car following model determines "
                + "the acceleration that a vehicle will make taking into account nearby vehicles, "
                + "infrastructural restrictions (e.g. speed limit, curvature of the road) "
                + "capabilities of the vehicle and personality of the driver.</html>", new String[]{"IDM", "IDM+"}, 1,
                false, 500));
            this.properties.add(new ProbabilityDistributionProperty("Traffic composition",
                "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"}, new Double[]{0.8,
                    0.2}, false, 10));
        }
        catch (PropertyException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
        this.model = null;
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException on ???
     * @throws RemoteException on communications failure
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
                    fundamentalDiagrams.buildAnimator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(
                        3600.0, SECOND), fundamentalDiagrams.getProperties(), null, true);
                }
                catch (RemoteException | SimRuntimeException | NamingException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.model = new FundamentalDiagramPlotsModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(0, -100, 5000, 200);
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts()
    {
        final int panelsPerRow = 3;
        TablePanel charts = new TablePanel(4, panelsPerRow);
        for (int plotNumber = 0; plotNumber < 10; plotNumber++)
        {
            Length.Rel detectorLocation = new Length.Rel(400 + 500 * plotNumber, METER);
            FundamentalDiagram fd;
            try
            {
                fd =
                    new FundamentalDiagram("Fundamental Diagram at " + detectorLocation.getSI() + "m", new Time.Rel(1,
                        MINUTE), this.model.getLane(), detectorLocation);
                fd.setTitle("Density Contour Graph");
                fd.setExtendedState(Frame.MAXIMIZED_BOTH);
                this.model.getFundamentalDiagrams().add(fd);
                charts.setCell(fd.getContentPane(), plotNumber / panelsPerRow, plotNumber % panelsPerRow);
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }
        }
        return charts;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Fundamental Diagrams";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Fundamental Diagram Plots</H1>"
            + "Simulation of a single lane road of 5 km length. Vechicles are generated at a constant rate of "
            + "1500 veh/hour. At time 300s a blockade is inserted at position 4km; this blockade is removed at time "
            + "500s. This blockade simulates a bridge opening.<br>"
            + "The blockade causes a traffic jam that slowly dissolves after the blockade is removed.<br />"
            + "Output is a set of Diagrams that plot observed density, flow and speed plots against each other.</html>";
    }

}

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s a
 * blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is IDM+ <a
 * href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
 * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
 * Output is a set of FundamentalDiagram plots for various point along the lane.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version ug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class FundamentalDiagramPlotsModel implements OTSModelInterface, OTS_SCALAR
{
    /** */
    private static final long serialVersionUID = 20140820L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** the headway (inter-vehicle time). */
    private Time.Rel headway;

    /** number of cars created. */
    private int carsCreated = 0;

    /** Type of all GTUs. */
    private GTUType gtuType = GTUType.makeGTUType("Car");

    /** the car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModel carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModel carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The lane change model. */
    private AbstractLaneChangeModel laneChangeModel = new Egoistic();

    /** The blocking car. */
    private LaneBasedIndividualCar block = null;

    /** minimum distance. */
    private Length.Rel minimumDistance = new Length.Rel(0, METER);

    /** maximum distance. */
    private Length.Rel maximumDistance = new Length.Rel(5000, METER);

    /** The Lane containing the simulated Cars. */
    private Lane lane;

    /** the speed limit. */
    private Speed.Abs speedLimit = new Speed.Abs(100, KM_PER_HOUR);

    /** the fundamental diagram plots. */
    private ArrayList<FundamentalDiagram> fundamentalDiagrams = new ArrayList<FundamentalDiagram>();

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12345);

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /**
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the properties
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    public FundamentalDiagramPlotsModel(final ArrayList<AbstractProperty<?>> properties, final GTUColorer gtuColorer)
    {
        this.properties = properties;
        this.gtuColorer = gtuColorer;
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
        final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
        throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        OTSNode from = new OTSNode("From", new OTSPoint3D(getMinimumDistance().getSI(), 0, 0));
        OTSNode to = new OTSNode("To", new OTSPoint3D(getMaximumDistance().getSI(), 0, 0));
        LaneType laneType = new LaneType("CarLane");
        laneType.addCompatibility(this.gtuType);
        try
        {
            this.lane = LaneFactory.makeLane("Lane", from, to, null, laneType, this.speedLimit, this.simulator);
        }
        catch (NamingException | NetworkException | OTSGeometryException exception)
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
                            new IDM(new Acceleration.Abs(1, METER_PER_SECOND_2), new Acceleration.Abs(1.5,
                                METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
                        this.carFollowingModelTrucks =
                            new IDM(new Acceleration.Abs(0.5, METER_PER_SECOND_2), new Acceleration.Abs(1.5,
                                METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
                    }
                    else if (modelName.equals("IDM+"))
                    {
                        this.carFollowingModelCars =
                            new IDMPlus(new Acceleration.Abs(1, METER_PER_SECOND_2), new Acceleration.Abs(1.5,
                                METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
                        this.carFollowingModelTrucks =
                            new IDMPlus(new Acceleration.Abs(0.5, METER_PER_SECOND_2), new Acceleration.Abs(1.5,
                                METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
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
        this.headway = new Time.Rel(3600.0 / 1500.0, SECOND);

        try
        {
            // Schedule creation of the first car (this will re-schedule itself one headway later, etc.).
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, SECOND), this, this, "generateCar", null);
            // Create a block at t = 5 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(300, SECOND), this, this, "createBlock", null);
            // Remove the block at t = 7 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(420, SECOND), this, this, "removeBlock", null);
            // Schedule regular updates of the graph
            for (int t = 1; t <= 1800; t++)
            {
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(t - 0.001, SECOND), this, this, "drawGraphs",
                    null);
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
        Length.Rel initialPosition = new Length.Rel(4000, METER);
        Map<Lane, Length.Rel> initialPositions = new LinkedHashMap<Lane, Length.Rel>();
        initialPositions.put(this.getLane(), initialPosition);
        try
        {
            this.block =
                new LaneBasedIndividualCar("999999", this.gtuType, this.carFollowingModelCars, this.laneChangeModel,
                    initialPositions, new Speed.Abs(0, KM_PER_HOUR), new Length.Rel(4, METER), new Length.Rel(1.8, METER),
                    new Speed.Abs(0, KM_PER_HOUR), new CompleteLaneBasedRouteNavigator(new CompleteRoute("")),
                    this.simulator, DefaultCarAnimation.class, this.gtuColorer);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Remove the block.
     */
    protected final void removeBlock()
    {
        this.block.destroy();
        this.block = null;
    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        Length.Rel initialPosition = new Length.Rel(0, METER);
        Speed.Abs initialSpeed = new Speed.Abs(100, KM_PER_HOUR);
        Map<Lane, Length.Rel> initialPositions = new LinkedHashMap<Lane, Length.Rel>();
        initialPositions.put(this.getLane(), initialPosition);
        try
        {
            Length.Rel vehicleLength = new Length.Rel(generateTruck ? 15 : 4, METER);
            GTUFollowingModel gtuFollowingModel = generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
            if (null == gtuFollowingModel)
            {
                throw new Error("gtuFollowingModel is null");
            }
            new LaneBasedIndividualCar("" + (++this.carsCreated), this.gtuType, generateTruck ? this.carFollowingModelTrucks
                : this.carFollowingModelCars, this.laneChangeModel, initialPositions, initialSpeed, vehicleLength,
                new Length.Rel(1.8, METER), new Speed.Abs(200, KM_PER_HOUR), new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute("")), this.simulator, DefaultCarAnimation.class, this.gtuColorer);
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
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
    public final Length.Rel getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximumDistance
     */
    public final Length.Rel getMaximumDistance()
    {
        return this.maximumDistance;
    }

    /**
     * @return lane.
     */
    public Lane getLane()
    {
        return this.lane;
    }

}
