package org.opentrafficsim.demo.carFollowing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
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
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Sensor;
import org.opentrafficsim.road.network.lane.SinkSensor;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.BooleanProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;
import org.opentrafficsim.simulationengine.properties.IDMPropertySet;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

/**
 * Single lane road consisting of three consecutive links.<br>
 * Tests that GTUs correctly transfer themselves onto the next lane and that the graph samplers handle this situation.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 30 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SequentialLanes extends AbstractWrappableAnimation implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private SequentialModel model;

    /**
     * Create a SequentialLanes simulation.
     * @throws PropertyException
     */
    public SequentialLanes() throws PropertyException
    {
        ArrayList<AbstractProperty<?>> outputProperties = new ArrayList<>();
        outputProperties.add(new BooleanProperty("DensityPlot", "Density", "Density contour plot", true, false, 0));
        outputProperties.add(new BooleanProperty("FlowPlot", "Flow", "Flow contour plot", true, false, 1));
        outputProperties.add(new BooleanProperty("SpeedPlot", "Speed", "Speed contour plot", true, false, 2));
        outputProperties.add(new BooleanProperty("AccelerationPlot", "Acceleration", "Acceleration contour plot", true,
            false, 3));
        outputProperties.add(new BooleanProperty("TrajectoryPlot", "Trajectories", "Trajectory (time/distance) diagram",
            true, false, 4));
        this.properties.add(new CompoundProperty("OutputGraphs", "Output graphs", "Select the graphical output",
            outputProperties, true, 1000));
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
                    SequentialLanes sequential = new SequentialLanes();
                    ArrayList<AbstractProperty<?>> localProperties = sequential.getProperties();
                    try
                    {
                        localProperties.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                            "<html>Mix of passenger cars and trucks</html>", new String[] {"passenger car", "truck"},
                            new Double[] {0.8, 0.2}, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    localProperties.add(new SelectionProperty("CarFollowingModel", "Car following model",
                        "<html>The car following model determines "
                            + "the acceleration that a vehicle will make taking into account "
                            + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                            + "curvature of the road) capabilities of the vehicle and personality "
                            + "of the driver.</html>", new String[] {"IDM", "IDM+"}, 1, false, 1));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car", new Acceleration(1.0,
                        METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2), new Length(2.0, METER),
                        new Duration(1.0, SECOND), 2));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck", new Acceleration(0.5,
                        METER_PER_SECOND_2), new Acceleration(1.25, METER_PER_SECOND_2), new Length(2.0, METER),
                        new Duration(1.0, SECOND), 3));
                    sequential.buildAnimator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(3600.0, SECOND),
                        localProperties, null, true);
                    sequential.panel.getTabbedPane().addTab("info", sequential.makeInfoPane());
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
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
        return new Rectangle2D.Double(0, -100, 2010, 200);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.model = new SequentialModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /**
     * @return an info pane to be added to the tabbed pane.
     */
    protected final JComponent makeInfoPane()
    {
        // Make the info tab
        String helpSource = "/" + StraightModel.class.getPackage().getName().replace('.', '/') + "/IDMPlus.html";
        URL page = StraightModel.class.getResource(helpSource);
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
    protected final JPanel makeCharts(SimpleSimulatorInterface simulator) throws OTSSimulationException, PropertyException
    {
        // Make the tab with the plots
        AbstractProperty<?> output = new CompoundProperty("", "", "", this.properties, false, 0).findByKey("OutputGraphs");
        if (null == output)
        {
            throw new Error("Cannot find output properties");
        }
        ArrayList<BooleanProperty> graphs = new ArrayList<>();
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
            String graphName = graphs.get(i).getKey();
            Container container = null;
            LaneBasedGTUSampler graph;
            if (graphName.contains("Trajectories"))
            {
                TrajectoryPlot tp = new TrajectoryPlot("TrajectoryPlot", new Duration(0.5, SECOND), this.model.getPath(), simulator);
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
                    continue;
                    // throw new Error("Unhandled type of contourplot: " + graphName);
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
        return "Sequential Lanes";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Simulation of a straight one-lane road consisting of three consecutive Links</H1>"
            + "Simulation of a single lane road consisting of two 1 km stretches with a 1m stretch in between. "
            + "This will test transition of a GTU from one lane section onto the next.<br>"
            + "Vehicles are generated at a constant rate of 1500 veh/hour.<br>"
            + "Selected trajectory and contour plots are generated during the simulation.</html>";
    }

}

/**
 * Build the sequential model.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 0 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class SequentialModel implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** The network. */
    private OTSNetwork network = new OTSNetwork("network");

    /** The nodes of our network in the order that all GTUs will visit them. */
    private ArrayList<OTSNode> nodes = new ArrayList<>();

    /** The car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModelOld carFollowingModelCars;

    /** The car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModelOld carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The headway (inter-vehicle time). */
    private Duration headway;

    /** Number of cars created. */
    private int carsCreated = 0;

    /** Type of all GTUs. */
    private GTUType gtuType = new GTUType("Car");

    /** Minimum distance. */
    private Length minimumDistance = new Length(0, METER);

    /** The Lane where newly created Cars initially placed on. */
    private Lane initialLane;

    /** Maximum distance. */
    private Length maximumDistance = new Length(2001, METER);

    /** The contour plots. */
    private ArrayList<LaneBasedGTUSampler> plots = new ArrayList<>();

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12345);

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;

    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<>();

    /** The speedLimit on all Lanes. */
    private Speed speedLimit;

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /**
     * @param properties the user settable properties
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    public SequentialModel(final ArrayList<AbstractProperty<?>> properties, final GTUColorer gtuColorer)
    {
        this.properties = properties;
        this.gtuColorer = gtuColorer;
    }

    /**
     * @return a newly created path (which all GTUs in this simulation will follow).
     */
    public List<Lane> getPath()
    {
        return new ArrayList<>(this.path);
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
        throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        this.speedLimit = new Speed(100, KM_PER_HOUR);

        // TODO Bezier curves make 180 degree mistake when minus is true
        boolean minus = false;

        this.nodes = new ArrayList<>();
        OTSNode n0 = new OTSNode("Node(0,0)", new OTSPoint3D(0, 0));
        OTSNode n1 = new OTSNode("Node(1000,0)", new OTSPoint3D(1000, 0));
        OTSNode n2 = new OTSNode("Node(1020,3)", new OTSPoint3D(1020, 3));
        OTSNode n3 = new OTSNode("Node(2000,197)", new OTSPoint3D(2000, 197));
        OTSNode n4 = new OTSNode("Node(2020,200)", new OTSPoint3D(2020, 200));
        OTSNode n5 = new OTSNode("Node(2200,200)", new OTSPoint3D(2200, 200));
        this.nodes.addAll(Arrays.asList(new OTSNode[] {n0, n1, n2, n3, n4, n5}));

        Set<GTUType> compatibility = new HashSet<>();
        compatibility.add(this.gtuType);
        LaneType laneType = new LaneType("CarLane", compatibility);

        try
        {
            // Now we can build a series of Links with one Lane on them
            ArrayList<CrossSectionLink> links = new ArrayList<>();
            OTSLine3D l01 = new OTSLine3D(n0.getPoint(), n1.getPoint());
            OTSLine3D l12 = LaneFactory.makeBezier(n0, n1, n2, n3);
            OTSLine3D l23 =
                minus ? new OTSLine3D(n3.getPoint(), n2.getPoint()) : new OTSLine3D(n2.getPoint(), n3.getPoint());
            OTSLine3D l34 = LaneFactory.makeBezier(n2, n3, n4, n5);
            OTSLine3D l45 = new OTSLine3D(n4.getPoint(), n5.getPoint());
            OTSLine3D[] lines = new OTSLine3D[] {l01, l12, l23, l34, l45};

            for (int i = 1; i < this.nodes.size(); i++)
            {
                OTSNode fromNode = this.nodes.get(i - 1);
                OTSNode toNode = this.nodes.get(i);
                OTSLine3D line = lines[i - 1];
                String linkName = fromNode.getId() + "-" + toNode.getId();
                LongitudinalDirectionality direction =
                    line.equals(l23) && minus ? LongitudinalDirectionality.DIR_MINUS : LongitudinalDirectionality.DIR_PLUS;
                Lane[] lanes =
                    LaneFactory.makeMultiLane(linkName, fromNode, toNode, line.getPoints(), 1, laneType, this.speedLimit,
                        this.simulator, direction);
                if (i == this.nodes.size() - 1)
                {
                    Sensor sensor = new SinkSensor(lanes[0], new Length(100.0, METER), this.simulator);
                    lanes[0].addSensor(sensor, GTUType.ALL);
                }
                this.path.add(lanes[0]);
                links.add(lanes[0].getParentLink());
                if (1 == i)
                {
                    this.initialLane = lanes[0];
                }
            }
        }
        catch (NamingException | NetworkException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }

        // 1500 [veh / hour] == 2.4s headway
        this.headway = new Duration(3600.0 / 1500.0, SECOND);
        // Schedule creation of the first car (it will re-schedule itself one headway later, etc.).
        this.simulator.scheduleEventAbs(new DoubleScalar.Abs<>(0.0, SECOND), this, this, "generateCar", null);
        // Schedule regular updates of the graphs
        for (int t = 1; t <= 1800; t++)
        {
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<>(t - 0.001, SECOND), this, this, "drawGraphs",
                null);
        }
        try
        {
            String carFollowingModelName = null;
            CompoundProperty propertyContainer = new CompoundProperty("", "", "", this.properties, false, 0);
            AbstractProperty<?> cfmp = propertyContainer.findByKey("CarFollowingModel");
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
            Iterator<AbstractProperty<List<AbstractProperty<?>>>> iterator =
                new CompoundProperty("", "", "", this.properties, false, 0).iterator();
            while (iterator.hasNext())
            {
                AbstractProperty<?> ap = iterator.next();
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("CarFollowingModel".equals(sp.getKey()))
                    {
                        carFollowingModelName = sp.getValue();
                    }
                }
                else if (ap instanceof ProbabilityDistributionProperty)
                {
                    ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) ap;
                    if (ap.getKey().equals("TrafficComposition"))
                    {
                        this.carProbability = pdp.getValue()[0];
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty cp = (CompoundProperty) ap;
                    if (ap.getKey().equals("OutputGraphs"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getKey().contains("IDM"))
                    {
                        // System.out.println("Car following model name appears to be " + ap.getKey());
                        Acceleration a = IDMPropertySet.getA(cp);
                        Acceleration b = IDMPropertySet.getB(cp);
                        Length s0 = IDMPropertySet.getS0(cp);
                        Duration tSafe = IDMPropertySet.getTSafe(cp);
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
                        if (ap.getKey().contains("Car"))
                        {
                            this.carFollowingModelCars = gtuFollowingModel;
                        }
                        else if (ap.getKey().contains("Truck"))
                        {
                            this.carFollowingModelTrucks = gtuFollowingModel;
                        }
                        else
                        {
                            throw new Error("Cannot determine gtu type for " + ap.getKey());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Caught exception " + e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
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
    public final Length getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximumDistance
     */
    public final Length getMaximumDistance()
    {
        return this.maximumDistance;
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
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        Length initialPosition = new Length(0, METER);
        Speed initialSpeed = new Speed(100, KM_PER_HOUR);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        try
        {
            initialPositions.add(new DirectedLanePosition(this.initialLane, initialPosition, GTUDirectionality.DIR_PLUS));
            Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
            GTUFollowingModelOld gtuFollowingModel =
                generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
            if (null == gtuFollowingModel)
            {
                throw new Error("gtuFollowingModel is null");
            }
            BehavioralCharacteristics behavioralCharacteristics = DefaultsFactory.getDefaultBehavioralCharacteristics();
            LaneBasedIndividualGTU gtu =
                new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, vehicleLength, new Length(1.8, METER),
                    new Speed(200, KM_PER_HOUR), this.simulator, DefaultCarAnimation.class, this.gtuColorer, this.network);
            LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, new LaneBasedGTUFollowingTacticalPlanner(
                    gtuFollowingModel, gtu), gtu);
            gtu.init(strategicalPlanner, initialPositions, initialSpeed);
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
        }
        catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

}
