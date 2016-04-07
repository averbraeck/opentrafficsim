package org.opentrafficsim.demo.carFollowing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedBehavioralCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LanePathInfo;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Sensor;
import org.opentrafficsim.road.network.lane.SinkSensor;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.BooleanProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;
import org.opentrafficsim.simulationengine.properties.IDMPropertySet;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

/**
 * Simplest contour plots demonstration.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-01-05 06:14:49 +0100 (Tue, 05 Jan 2016) $, @version $Revision: 1685 $, by $Author: averbraeck $,
 * initial version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StraightPerception extends AbstractWrappableAnimation implements UNITS
{
    /** The model. */
    private StraightPerceptionModel model;

    /** Create a ContourPlots simulation. */
    public StraightPerception()
    {
        ArrayList<AbstractProperty<?>> outputProperties = new ArrayList<AbstractProperty<?>>();
        outputProperties.add(new BooleanProperty("Density", "Density contour plot", true, false, 0));
        outputProperties.add(new BooleanProperty("Flow", "Flow contour plot", true, false, 1));
        outputProperties.add(new BooleanProperty("Speed", "Speed contour plot", true, false, 2));
        outputProperties.add(new BooleanProperty("Acceleration", "Acceleration contour plot", true, false, 3));
        outputProperties.add(new BooleanProperty("Trajectories", "Trajectory (time/distance) diagram", true, false, 4));
        this.properties.add(new CompoundProperty("Output graphs", "Select the graphical output", outputProperties,
            true, 1000));
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
     * @throws SimRuntimeException when simulation cannot be created with given parameters
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    StraightPerception straight = new StraightPerception();
                    ArrayList<AbstractProperty<?>> localProperties = straight.getProperties();
                    try
                    {
                        localProperties.add(new ProbabilityDistributionProperty("Traffic composition",
                            "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"},
                            new Double[]{0.8, 0.2}, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    localProperties.add(new SelectionProperty("Car following model",
                        "<html>The car following model determines "
                            + "the acceleration that a vehicle will make taking into account "
                            + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                            + "curvature of the road) capabilities of the vehicle and personality "
                            + "of the driver.</html>", new String[]{"IDM", "IDM+"}, 1, false, 1));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("Car", new Acceleration(1.0,
                        METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2), new Length.Rel(2.0, METER),
                        new Time.Rel(1.0, SECOND), 2));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("Truck", new Acceleration(0.5,
                        METER_PER_SECOND_2), new Acceleration(1.25, METER_PER_SECOND_2), new Length.Rel(2.0, METER),
                        new Time.Rel(1.0, SECOND), 3));
                    straight.buildAnimator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(3600.0,
                        SECOND), localProperties, null, true);
                    straight.panel.getTabbedPane().addTab("info", straight.makeInfoPane());
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(0, -100, 5000, 200);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.model = new StraightPerceptionModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /**
     * @return an info pane to be added to the tabbed pane.
     */
    protected final JComponent makeInfoPane()
    {
        // Make the info tab
        String helpSource =
            "/" + StraightPerceptionModel.class.getPackage().getName().replace('.', '/') + "/IDMPlus.html";
        URL page = StraightPerceptionModel.class.getResource(helpSource);
        if (page != null)
        {
            try
            {
                HTMLPanel htmlPanel = new HTMLPanel(page);
                return new JScrollPane(htmlPanel);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
        return new JPanel();
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts() throws OTSSimulationException
    {

        // Make the tab with the plots
        AbstractProperty<?> output =
            new CompoundProperty("", "", this.properties, false, 0).findByShortName("Output graphs");
        if (null == output)
        {
            throw new Error("Cannot find output properties");
        }
        ArrayList<BooleanProperty> graphs = new ArrayList<BooleanProperty>();
        if (output instanceof CompoundProperty)
        {
            CompoundProperty outputProperties = (CompoundProperty) output;
            for (AbstractProperty<?> ap : outputProperties.getValue())
            {
                if (ap instanceof BooleanProperty)
                {
                    BooleanProperty bp = (BooleanProperty) ap;
                    if (bp.getValue())
                    {
                        graphs.add(bp);
                    }
                }
            }
        }
        else
        {
            throw new Error("output properties should be compound");
        }
        int graphCount = graphs.size();
        int columns = (int) Math.ceil(Math.sqrt(graphCount));
        int rows = 0 == columns ? 0 : (int) Math.ceil(graphCount * 1.0 / columns);
        TablePanel charts = new TablePanel(columns, rows);

        for (int i = 0; i < graphCount; i++)
        {
            String graphName = graphs.get(i).getShortName();
            Container container = null;
            LaneBasedGTUSampler graph;
            if (graphName.contains("Trajectories"))
            {
                List<Lane> path = new ArrayList<Lane>();
                path.add(this.model.getLane());
                TrajectoryPlot tp = new TrajectoryPlot("TrajectoryPlot", new Time.Rel(0.5, SECOND), path);
                tp.setTitle("Trajectory Graph");
                tp.setExtendedState(Frame.MAXIMIZED_BOTH);
                graph = tp;
                container = tp.getContentPane();
            }
            else
            {
                ContourPlot cp;
                if (graphName.contains("Density"))
                {
                    cp = new DensityContourPlot("DensityPlot", this.model.getPath());
                    cp.setTitle("Density Contour Graph");
                }
                else if (graphName.contains("Speed"))
                {
                    cp = new SpeedContourPlot("SpeedPlot", this.model.getPath());
                    cp.setTitle("Speed Contour Graph");
                }
                else if (graphName.contains("Flow"))
                {
                    cp = new FlowContourPlot("FlowPlot", this.model.getPath());
                    cp.setTitle("Flow Contour Graph");
                }
                else if (graphName.contains("Acceleration"))
                {
                    cp = new AccelerationContourPlot("AccelerationPlot", this.model.getPath());
                    cp.setTitle("Acceleration Contour Graph");
                }
                else
                {
                    throw new Error("Unhandled type of contourplot: " + graphName);
                }
                graph = cp;
                container = cp.getContentPane();
            }
            // Add the container to the matrix
            charts.setCell(container, i % columns, i / columns);
            this.model.getPlots().add(graph);
        }
        return charts;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Straight lane";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Simulation of a straight one-lane road with opening bridge</H1>"
            + "Simulation of a single lane road of 5 km length. Vehicles are generated at a constant rate of "
            + "1500 veh/hour. At time 300s a blockade is inserted at position 4km; this blockade is removed at "
            + "time 420s. This blockade simulates a bridge opening.<br>"
            + "The blockade causes a traffic jam that slowly dissolves after the blockade is removed.<br>"
            + "Selected trajectory and contour plots are generated during the simulation.</html>";
    }

}

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s a
 * blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is IDM+ <a
 * href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
 * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
 * Output is a set of block charts:
 * <ul>
 * <li>Traffic density</li>
 * <li>Speed</li>
 * <li>Flow</li>
 * <li>Acceleration</li>
 * </ul>
 * All these graphs display simulation time along the horizontal axis and distance along the road along the vertical axis.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-01-05 06:14:49 +0100 (Tue, 05 Jan 2016) $, @version $Revision: 1685 $, by $Author: averbraeck $,
 * initial version ug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class StraightPerceptionModel implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** The network. */
    private OTSNetwork network = new OTSNetwork("network");

    /** The headway (inter-vehicle time). */
    private Time.Rel headway;

    /** Number of cars created. */
    private int carsCreated = 0;

    /** Type of all GTUs. */
    private GTUType gtuType = GTUType.makeGTUType("Car");

    /** The car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModelOld carFollowingModelCars;

    /** The car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModelOld carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The lane change model. */
    private AbstractLaneChangeModel laneChangeModel = new Egoistic();

    /** The blocking car. */
    private LaneBasedIndividualGTU block = null;

    /** Minimum distance. */
    private Length.Rel minimumDistance = new Length.Rel(0, METER);

    /** Maximum distance. */
    private Length.Rel maximumDistance = new Length.Rel(5000, METER);

    /** The Lane that contains the simulated Cars. */
    private Lane lane;

    /** The contour plots. */
    private ArrayList<LaneBasedGTUSampler> plots = new ArrayList<LaneBasedGTUSampler>();

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12345);

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /**
     * @param properties the user settable properties
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    StraightPerceptionModel(final ArrayList<AbstractProperty<?>> properties, final GTUColorer gtuColorer)
    {
        this.properties = properties;
        this.gtuColorer = gtuColorer;
    }

    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<Lane>();

    /** The speed limit on all Lanes. */
    private Speed speedLimit = new Speed(100, KM_PER_HOUR);

    /** The perception interval distribution. */
    @SuppressWarnings("visibilitymodifier")
    DistContinuous perceptionIntervalDist = new DistTriangular(new MersenneTwister(2), 0.25, 1, 2);

    /** The forward headway distribution. */
    @SuppressWarnings("visibilitymodifier")
    DistContinuous forwardHeadwayDist = new DistTriangular(new MersenneTwister(20), 20, 50, 100);

    /**
     * @return List&lt;Lane&gt;; the set of lanes for the specified index
     */
    public List<Lane> getPath()
    {
        return new ArrayList<Lane>(this.path);
    }

    /** {@inheritDoc} */
    @Override
    public final
        void
        constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        OTSNode from = new OTSNode("From", new OTSPoint3D(getMinimumDistance().getSI(), 0, 0));
        OTSNode to = new OTSNode("To", new OTSPoint3D(getMaximumDistance().getSI(), 0, 0));
        OTSNode end = new OTSNode("End", new OTSPoint3D(getMaximumDistance().getSI() + 50.0, 0, 0));
        try
        {
            LaneType laneType = new LaneType("CarLane");
            laneType.addCompatibility(this.gtuType);
            this.lane =
                LaneFactory.makeLane("Lane", from, to, null, laneType, this.speedLimit, this.simulator,
                    LongitudinalDirectionality.DIR_PLUS);
            this.path.add(this.lane);
            CrossSectionLink endLink =
                LaneFactory.makeLink("endLink", to, end, null, LongitudinalDirectionality.DIR_PLUS);
            // No overtaking, single lane
            Lane sinkLane =
                new Lane(endLink, "sinkLane", this.lane.getLateralCenterPosition(1.0),
                    this.lane.getLateralCenterPosition(1.0), this.lane.getWidth(1.0), this.lane.getWidth(1.0),
                    laneType, LongitudinalDirectionality.DIR_PLUS, this.speedLimit, new OvertakingConditions.None());
            Sensor sensor = new SinkSensor(sinkLane, new Length.Rel(10.0, METER), this.simulator);
            sinkLane.addSensor(sensor, GTUType.ALL);
            String carFollowingModelName = null;
            CompoundProperty propertyContainer = new CompoundProperty("", "", this.properties, false, 0);
            AbstractProperty<?> cfmp = propertyContainer.findByShortName("Car following model");
            if (null == cfmp)
            {
                throw new Error("Cannot find \"Car following model\" property");
            }
            if (cfmp instanceof SelectionProperty)
            {
                carFollowingModelName = ((SelectionProperty) cfmp).getValue();
            }
            else
            {
                throw new Error("\"Car following model\" property has wrong type");
            }
            Iterator<AbstractProperty<ArrayList<AbstractProperty<?>>>> iterator =
                new CompoundProperty("", "", this.properties, false, 0).iterator();
            while (iterator.hasNext())
            {
                AbstractProperty<?> ap = iterator.next();
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("Car following model".equals(sp.getShortName()))
                    {
                        carFollowingModelName = sp.getValue();
                    }
                }
                else if (ap instanceof ProbabilityDistributionProperty)
                {
                    ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) ap;
                    String modelName = ap.getShortName();
                    if (modelName.equals("Traffic composition"))
                    {
                        this.carProbability = pdp.getValue()[0];
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty cp = (CompoundProperty) ap;
                    if (ap.getShortName().equals("Output graphs"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getShortName().contains("IDM"))
                    {
                        Acceleration a = IDMPropertySet.getA(cp);
                        Acceleration b = IDMPropertySet.getB(cp);
                        Length.Rel s0 = IDMPropertySet.getS0(cp);
                        Time.Rel tSafe = IDMPropertySet.getTSafe(cp);
                        GTUFollowingModelOld gtuFollowingModel = null;
                        if (carFollowingModelName.equals("IDM"))
                        {
                            gtuFollowingModel = new IDMOld(a, b, s0, tSafe, 1.0);
                        }
                        else if (carFollowingModelName.equals("IDM+"))
                        {
                            gtuFollowingModel = new IDMPlusOld(a, b, s0, tSafe, 1.0);
                        }
                        else
                        {
                            throw new Error("Unknown gtu following model: " + carFollowingModelName);
                        }
                        if (ap.getShortName().contains(" Car "))
                        {
                            this.carFollowingModelCars = gtuFollowingModel;
                        }
                        else if (ap.getShortName().contains(" Truck "))
                        {
                            this.carFollowingModelTrucks = gtuFollowingModel;
                        }
                        else
                        {
                            throw new Error("Cannot determine gtu type for " + ap.getShortName());
                        }
                        /*
                         * System.out.println("Created " + carFollowingModelName + " for " + p.getShortName());
                         * System.out.println("a: " + a); System.out.println("b: " + b); System.out.println("s0: " + s0);
                         * System.out.println("tSafe: " + tSafe);
                         */
                    }
                }
            }

            // 1500 [veh / hour] == 2.4s headway
            this.headway = new Time.Rel(3600.0 / 1500.0, SECOND);
            // Schedule creation of the first car (it will re-schedule itself one headway later, etc.).
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, SECOND), this, this, "generateCar",
                null);
            // Create a block at t = 5 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(300, SECOND), this, this, "createBlock",
                null);
            // Remove the block at t = 7 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(420, SECOND), this, this, "removeBlock",
                null);
            // Schedule regular updates of the graphs
            for (int t = 1; t <= 1800; t++)
            {
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(t - 0.001, SECOND), this, this,
                    "drawGraphs", null);
            }
        }
        catch (SimRuntimeException | NamingException | NetworkException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraphs()
    {
        for (LaneBasedGTUSampler plot : this.plots)
        {
            plot.reGraph();
        }
    }

    /**
     * Set up the block.
     */
    protected final void createBlock()
    {
        Length.Rel initialPosition = new Length.Rel(4000, METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        try
        {
            initialPositions.add(new DirectedLanePosition(this.lane, initialPosition, GTUDirectionality.DIR_PLUS));
            LaneBasedBehavioralCharacteristics drivingCharacteristics =
                new LaneBasedBehavioralCharacteristics(this.carFollowingModelCars, this.laneChangeModel);
            LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(drivingCharacteristics,
                    new GTUFollowingTacticalPlannerNoPerceive());
            this.block =
                new LaneBasedIndividualGTU("999999", this.gtuType, initialPositions, new Speed(0.0, KM_PER_HOUR),
                    new Length.Rel(4, METER), new Length.Rel(1.8, METER), new Speed(0.0, KM_PER_HOUR), this.simulator,
                    strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer,
                    this.network);
        }
        catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
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
        Speed initialSpeed = new Speed(100, KM_PER_HOUR);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        try
        {
            initialPositions.add(new DirectedLanePosition(this.lane, initialPosition, GTUDirectionality.DIR_PLUS));
            Length.Rel vehicleLength = new Length.Rel(generateTruck ? 15 : 4, METER);
            GTUFollowingModelOld gtuFollowingModel;
            if (generateTruck)
            {
                Acceleration a = new Acceleration(0.5, AccelerationUnit.METER_PER_SECOND_2); // max acceleration
                Acceleration b = new Acceleration(1.25, AccelerationUnit.METER_PER_SECOND_2); // max xomfortable deceleration
                Length.Rel s0 = new Length.Rel(4, LengthUnit.METER); // headway distance
                Time.Rel tSafe = new Time.Rel(2.0, TimeUnit.SECOND); // time headway
                gtuFollowingModel = new IDMPlusOld(a, b, s0, tSafe, 1.0);
            }
            else
            {
                Acceleration a = new Acceleration(2.0, AccelerationUnit.METER_PER_SECOND_2); // max acceleration
                Acceleration b = new Acceleration(3, AccelerationUnit.METER_PER_SECOND_2); // max xomfortable deceleration
                Length.Rel s0 = new Length.Rel(2.0, LengthUnit.METER); // headway distance
                Time.Rel tSafe = new Time.Rel(1.0, TimeUnit.SECOND); // time headway
                gtuFollowingModel = new IDMPlusOld(a, b, s0, tSafe, 1.0);
            }
            LaneBasedBehavioralCharacteristics drivingCharacteristics =
                new LaneBasedBehavioralCharacteristics(gtuFollowingModel, this.laneChangeModel);
            LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(drivingCharacteristics,
                    new GTUFollowingTacticalPlannerNoPerceive());
            LaneBasedPerceivingCar car =
                new LaneBasedPerceivingCar("" + (++this.carsCreated), this.gtuType, initialPositions, initialSpeed,
                    vehicleLength, new Length.Rel(1.8, METER), new Speed(200, KM_PER_HOUR), this.simulator,
                    strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer,
                    this.network);
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
            car.setPerceptionInterval(new Time.Rel(this.perceptionIntervalDist.draw(), TimeUnit.SECOND));
            car.getStrategicalPlanner().getDrivingCharacteristics()
                .setForwardHeadwayDistance(new Length.Rel(this.forwardHeadwayDist.draw(), LengthUnit.METER));
        }
        catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
        getSimulator() throws RemoteException
    {
        return this.simulator;
    }

    /**
     * @return contourPlots
     */
    public final ArrayList<LaneBasedGTUSampler> getPlots()
    {
        return this.plots;
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

    /**
     * Perceiving car.
     */
    class LaneBasedPerceivingCar extends LaneBasedIndividualGTU
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** */
        private Time.Rel perceptionInterval = new Time.Rel(0.5, TimeUnit.SECOND);

        /**
         * @param id ID; the id of the GTU
         * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
         * @param initialLongitudinalPositions Map&lt;Lane, Length.Rel&gt;; the initial positions of the car on one or more
         *            lanes
         * @param initialSpeed Speed; the initial speed of the car on the lane
         * @param length Length.Rel; the maximum length of the GTU (parallel with driving direction)
         * @param width Length.Rel; the maximum width of the GTU (perpendicular to driving direction)
         * @param maximumVelocity Speed;the maximum speed of the GTU (in the driving direction)
         * @param simulator OTSDEVSSimulatorInterface; the simulator
         * @param strategicalPlanner the strategical planner (e.g., route determination) to use
         * @param perception the lane-based perception model of the GTU
         * @param network the network that the GTU is initially registered in
         * @throws NamingException if an error occurs when adding the animation handler
         * @throws NetworkException when the GTU cannot be placed on the given lane
         * @throws SimRuntimeException when the move method cannot be scheduled
         * @throws GTUException when a parameter is invalid
         * @throws OTSGeometryException when the initial path is wrong
         */
        @SuppressWarnings("checkstyle:parameternumber")
        LaneBasedPerceivingCar(final String id, final GTUType gtuType,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
            final Length.Rel length, final Length.Rel width, final Speed maximumVelocity,
            final OTSDEVSSimulatorInterface simulator, final LaneBasedStrategicalPlanner strategicalPlanner,
            final LanePerceptionFull perception, final OTSNetwork network) throws NamingException, NetworkException,
            SimRuntimeException, GTUException, OTSGeometryException
        {
            super(id, gtuType, initialLongitudinalPositions, initialSpeed, length, width, maximumVelocity, simulator,
                strategicalPlanner, perception, network);
            perceive();
        }

        /**
         * Construct a new LaneBasedIndividualCar.
         * @param id ID; the id of the GTU
         * @param gtuType GTUTYpe; the type of GTU, e.g. TruckType, CarType, BusType
         * @param initialLongitudinalPositions Map&lt;Lane, Length.Rel&gt;; the initial positions of the car on one or more
         *            lanes
         * @param initialSpeed Speed; the initial speed of the car on the lane
         * @param length Length.Rel; the maximum length of the GTU (parallel with driving direction)
         * @param width Length.Rel; the maximum width of the GTU (perpendicular to driving direction)
         * @param maximumVelocity Speed;the maximum speed of the GTU (in the driving direction)
         * @param simulator OTSDEVSSimulatorInterface; the simulator
         * @param strategicalPlanner the strategical planner (e.g., route determination) to use
         * @param perception the lane-based perception model of the GTU
         * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation
         * @param gtuColorer GTUColorer; the GTUColorer that will be linked from the animation to determine the color (may be
         *            null in which case a default will be used)
         * @param network the network that the GTU is initially registered in
         * @throws NamingException if an error occurs when adding the animation handler
         * @throws NetworkException when the GTU cannot be placed on the given lane
         * @throws SimRuntimeException when the move method cannot be scheduled
         * @throws GTUException when a parameter is invalid
         * @throws OTSGeometryException when the initial path is wrong
         */
        @SuppressWarnings("checkstyle:parameternumber")
        LaneBasedPerceivingCar(final String id, final GTUType gtuType,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
            final Length.Rel length, final Length.Rel width, final Speed maximumVelocity,
            final OTSDEVSSimulatorInterface simulator, final LaneBasedStrategicalPlanner strategicalPlanner,
            final LanePerceptionFull perception, final Class<? extends Renderable2D> animationClass,
            final GTUColorer gtuColorer, final OTSNetwork network) throws NamingException, NetworkException,
            SimRuntimeException, GTUException, OTSGeometryException
        {
            super(id, gtuType, initialLongitudinalPositions, initialSpeed, length, width, maximumVelocity, simulator,
                strategicalPlanner, perception, animationClass, gtuColorer, network);
            perceive();
        }

        /**
         * @param perceptionInterval the interval for perceiving.
         */
        public void setPerceptionInterval(final Time.Rel perceptionInterval)
        {
            this.perceptionInterval = perceptionInterval;
        }

        /**
         * Perceive and reschedule.
         * @throws SimRuntimeException RTE
         * @throws GTUException GTUE
         * @throws NetworkException NE
         */
        public void perceive() throws SimRuntimeException, GTUException, NetworkException
        {
            getPerception().perceive();
            getSimulator().scheduleEventRel(this.perceptionInterval, this, this, "perceive", null);
        }
    }

    /**
     * Tactical planner without perception update.
     */
    class GTUFollowingTacticalPlannerNoPerceive extends AbstractLaneBasedTacticalPlanner
    {
        /** */
        private static final long serialVersionUID = 20151125L;

        /**
         * Instantiated a tactical planner with just GTU following behavior and no lane changes.
         */
        GTUFollowingTacticalPlannerNoPerceive()
        {
            super();
        }

        /** {@inheritDoc} */
        @Override
        public OperationalPlan generateOperationalPlan(final GTU gtu, final Time.Abs startTime,
            final DirectedPoint locationAtStartTime) throws OperationalPlanException, NetworkException, GTUException
        {
            // ask Perception for the local situation
            LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
            LanePerceptionFull perception = laneBasedGTU.getPerception();

            // if the GTU's maximum speed is zero (block), generate a stand still plan
            if (laneBasedGTU.getMaximumVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                // time equal to fastest reaction time of GTU
                return new OperationalPlan(laneBasedGTU, locationAtStartTime, startTime, new Time.Rel(
                    perceptionIntervalDist.draw(), TimeUnit.SECOND));
            }

            // get some models to help us make a plan
            GTUFollowingModelOld gtuFollowingModel =
                laneBasedGTU.getStrategicalPlanner().getDrivingCharacteristics().getGTUFollowingModel();

            // get the lane plan
            LanePathInfo lanePathInfo =
                buildLanePathInfo(laneBasedGTU, laneBasedGTU.getBehavioralCharacteristics().getForwardHeadwayDistance());
            Length.Rel maxDistance = lanePathInfo.getPath().getLength();

            // look at the conditions for headway
            HeadwayGTU headwayGTU = perception.getForwardHeadwayGTU();
            AccelerationStep accelerationStep = null;
            if (headwayGTU.getGtuId() == null)
            {
                accelerationStep =
                    gtuFollowingModel.computeAccelerationStepWithNoLeader(laneBasedGTU, maxDistance,
                        perception.getSpeedLimit());
            }
            else
            {
                // TODO do not use the velocity of the other GTU, but the PERCEIVED velocity
                accelerationStep =
                    gtuFollowingModel.computeAccelerationStep(laneBasedGTU, headwayGTU.getGtuSpeed(),
                        headwayGTU.getDistance(), maxDistance, perception.getSpeedLimit());
            }

            // see if we have to continue standing still. In that case, generate a stand still plan
            if (accelerationStep.getAcceleration().si < 1E-6
                && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                return new OperationalPlan(laneBasedGTU, locationAtStartTime, startTime, accelerationStep.getDuration());
            }

            List<Segment> operationalPlanSegmentList = new ArrayList<>();
            if (accelerationStep.getAcceleration().si == 0.0)
            {
                Segment segment = new OperationalPlan.SpeedSegment(accelerationStep.getDuration());
                operationalPlanSegmentList.add(segment);
            }
            else
            {
                Segment segment =
                    new OperationalPlan.AccelerationSegment(accelerationStep.getDuration(),
                        accelerationStep.getAcceleration());
                operationalPlanSegmentList.add(segment);
            }
            OperationalPlan op =
                new OperationalPlan(laneBasedGTU, lanePathInfo.getPath(), startTime, gtu.getVelocity(),
                    operationalPlanSegmentList);
            return op;
        }
    }
}
