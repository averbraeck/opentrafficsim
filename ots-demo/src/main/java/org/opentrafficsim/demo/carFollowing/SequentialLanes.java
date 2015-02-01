package org.opentrafficsim.demo.carFollowing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.HTMLPanel;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.factory.Link;
import org.opentrafficsim.core.network.factory.Node;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.BooleanProperty;
import org.opentrafficsim.simulationengine.CompoundProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.IDMPropertySet;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.PropertyException;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.SimulatorFrame;
import org.opentrafficsim.simulationengine.WrappableSimulation;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Single lane road consisting of three consecutive links.<br>
 * Tests that GTUs correctly transfer themselves onto the next lane and that the graph samplers handle this situation.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SequentialLanes implements WrappableSimulation
{
    /** The properties exhibited by this simulation. */
    private ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** Create a SequentialLanes simulation. */
    public SequentialLanes()
    {
        ArrayList<AbstractProperty<?>> outputProperties = new ArrayList<AbstractProperty<?>>();
        outputProperties.add(new BooleanProperty("Density", "Density contour plot", true, false, 0));
        outputProperties.add(new BooleanProperty("Flow", "Flow contour plot", true, false, 1));
        outputProperties.add(new BooleanProperty("Speed", "Speed contour plot", true, false, 2));
        outputProperties.add(new BooleanProperty("Acceleration", "Acceleration contour plot", true, false, 3));
        outputProperties.add(new BooleanProperty("Trajectories", "Trajectory (time/distance) diagram", true, false, 4));
        this.properties
                .add(new CompoundProperty("Output", "Select the graphical output", outputProperties, true, 1000));
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException when simulation cannot be created with given parameters
     */
    public static void main(final String[] args) throws RemoteException, SimRuntimeException
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
                        localProperties.add(new ProbabilityDistributionProperty("Traffic composition",
                                "<html>Mix of passenger cars and trucks</html>",
                                new String[]{"passenger car", "truck"}, new Double[]{0.8, 0.2}, false, 10));
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
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("Car",
                            new DoubleScalar.Abs<AccelerationUnit>(1.0, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(
                                    1.0, TimeUnit.SECOND), 2));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("Truck",
                            new DoubleScalar.Abs<AccelerationUnit>(0.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.25, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(
                                    1.0, TimeUnit.SECOND), 3));
                    new SimulatorFrame("Sequential Lanes Plots animation", sequential.buildSimulator(localProperties)
                            .getPanel());
                }
                catch (RemoteException | SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public SimpleSimulator buildSimulator(ArrayList<AbstractProperty<?>> userModifiedProperties)
            throws SimRuntimeException, RemoteException
    {
        SequentialModel model = new SequentialModel(userModifiedProperties);
        SimpleSimulator result =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(0, -100, 2010, 200));
        new ControlPanel(result);

        // Make the info tab
        String helpSource = "/" + StraightModel.class.getPackage().getName().replace('.', '/') + "/IDMPlus.html";
        URL page = StraightModel.class.getResource(helpSource);
        if (page != null)
        {
            try
            {
                HTMLPanel htmlPanel = new HTMLPanel(page);
                result.getPanel().getTabbedPane().addTab("info", new JScrollPane(htmlPanel));
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        // Make the tab with the plots
        AbstractProperty<?> output = new CompoundProperty("", "", this.properties, false, 0).findByShortName("Output");
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
        result.getPanel().getTabbedPane().addTab("statistics", charts);

        for (int i = 0; i < graphCount; i++)
        {
            String graphName = graphs.get(i).getShortName();
            Container container = null;
            LaneBasedGTUSampler graph;
            if (graphName.contains("Trajectories"))
            {
                TrajectoryPlot tp =
                        new TrajectoryPlot("TrajectoryPlot", new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND),
                                model.getPath());
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
                    cp = new DensityContourPlot("DensityPlot", model.getMinimumDistance(), model.getMaximumDistance());
                    cp.setTitle("Density Contour Graph");
                }
                else if (graphName.contains("Speed"))
                {
                    cp = new SpeedContourPlot("SpeedPlot", model.getMinimumDistance(), model.getMaximumDistance());
                    cp.setTitle("Speed Contour Graph");
                }
                else if (graphName.contains("Flow"))
                {
                    cp = new FlowContourPlot("FlowPlot", model.getMinimumDistance(), model.getMaximumDistance());
                    cp.setTitle("Flow Contour Graph");
                }
                else if (graphName.contains("Acceleration"))
                {
                    cp =
                            new AccelerationContourPlot("AccelerationPlot", model.getMinimumDistance(),
                                    model.getMaximumDistance());
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
            model.getPlots().add(graph);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String shortName()
    {
        return "Sequential Lanes";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "<html><h1>Simulation of a straight one-lane road consisting of three consecutive Links</H1>"
                + "Simulation of a single lane road consisting of two 1 km stretches with a 1m stretch in between. "
                + "This will test transition of a GTU from one lane section onto the next.<br />"
                + "Vehicles are generated at a constant rate of 1500 veh/hour.<br />"
                + "Selected trajectory and contour plots are generated during the simulation.</html>";
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<AbstractProperty<?>> getProperties()
    {
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

}

/**
 * Build the sequential model.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class SequentialModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;
    
    /** The nodes of our network in the order that all GTUs will visit them. */
    private ArrayList<Node> nodes = new ArrayList<Node>();

    /** the headway (inter-vehicle time). */
    private DoubleScalar.Rel<TimeUnit> headway;

    /** number of cars created. */
    private int carsCreated = 0;

    /** the car following model, e.g. IDM Plus. */
    private GTUFollowingModel carFollowingModel;

    /** minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** The Lane where newly created Cars initially placed on. */
    private Lane initialLane;

    /** the speed limit. */
    private DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** maximum distance. */
    private DoubleScalar.Rel<LengthUnit> maximumDistance = new DoubleScalar.Rel<LengthUnit>(2001, LengthUnit.METER);

    /** the contour plots. */
    private ArrayList<LaneBasedGTUSampler> plots = new ArrayList<LaneBasedGTUSampler>();

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;
    
    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<Lane>();

    /**
     * @param properties the user settable properties
     */
    public SequentialModel(final ArrayList<AbstractProperty<?>> properties)
    {
        this.properties = properties;
    }

    /**
     * @return a newly created path (which all GTUs in this simulation will follow).
     */
    public List<Lane> getPath()
    {
        return new ArrayList<Lane>(this.path);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        this.nodes = new ArrayList<Node>();
        int[] linkBoundaries = {0, 1000, 1001, 2001, 2200};
        for (int xPos : linkBoundaries)
        {
            this.nodes.add(new Node("Node at " + xPos, new Coordinate(xPos, -10, 0)));
        }
        LaneType<String> laneType = new LaneType<String>("CarLane");
        // Now we can build a series of Links with one Lane on them
        ArrayList<CrossSectionLink<?, ?>> links = new ArrayList<CrossSectionLink<?, ?>>();
        for (int i = 1; i < this.nodes.size(); i++)
        {
            Node fromNode = this.nodes.get(i - 1);
            Node toNode = this.nodes.get(i);
            String linkName = fromNode.getId() + "-" + toNode.getId();
            try
            {
                Lane[] lanes = LaneFactory.makeMultiLane(linkName, fromNode, toNode, null, 1, laneType, this.simulator);
                if (i == this.nodes.size() - 1)
                {
                    Link link = (Link) lanes[0].getParentLink();
                    int index = link.getCrossSectionElementList().indexOf(lanes[0]);
                    lanes[0] =
                            new SinkLane(link, lanes[0].getLateralCenterPosition(0),
                                    lanes[0].getLateralCenterPosition(1), lanes[0].getLaneType(),
                                    lanes[0].getDirectionality());
                    link.getCrossSectionElementList().remove(index);
                    link.getCrossSectionElementList().add(index, lanes[0]); // FIXME - this is horrible
                }
                else
                {
                    this.path.add(lanes[0]);
                }
                links.add(lanes[0].getParentLink());
                if (1 == i)
                {
                    this.initialLane = lanes[0];
                }
            }
            catch (NamingException | NetworkException exception)
            {
                exception.printStackTrace();
            }
        }
        this.carFollowingModel = new IDMPlus();
        // 1500 [veh / hour] == 2.4s headway
        this.headway = new DoubleScalar.Rel<TimeUnit>(3600.0 / 1500.0, TimeUnit.SECOND);
        // Schedule creation of the first car (it will re-schedule itself one headway later, etc.).
        this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                "generateCar", null);
        // Schedule regular updates of the graphs
        for (int t = 1; t <= 1800; t++)
        {
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(t - 0.001, TimeUnit.SECOND), this, this,
                    "drawGraphs", null);
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

    /**
     * Add one movement step of one Car to all contour plots.
     * @param car Car
     * @throws RemoteException on communications failure
     * @throws NetworkException on network-related inconsistency
     */
    protected final void addToContourPlots(final LaneBasedIndividualCar<?> car) throws RemoteException, NetworkException
    {
        for (LaneBasedGTUSampler plot : this.plots)
        {
            plot.addData(car);
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
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.initialLane, initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
            IDMCar car =
                    new IDMCar(++this.carsCreated, null, this.simulator, this.carFollowingModel, vehicleLength,
                            this.simulator.getSimulatorTime().get(), initialPositions, initialSpeed);
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException exception)
        {
            exception.printStackTrace();
        }
    }

    /** Inner class IDMCar. */
    protected class IDMCar extends LaneBasedIndividualCar<Integer>
    {
        /** */
        private static final long serialVersionUID = 20141030L;

        /**
         * Create a new IDMCar.
         * @param id integer; the id of the new IDMCar
         * @param gtuType GTUType&lt;String&gt;; the type of the GTU
         * @param simulator OTSDEVSSimulator; the simulator that runs the new IDMCar
         * @param carFollowingModel CarFollowingModel; the car following model of the new IDMCar
         * @param vehicleLength DoubleScalar.Rel&lt;LengthUnit&gt;; the length of the new IDMCar
         * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time of first evaluation of the new IDMCar
         * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial lane
         *            positions of the new IDMCar
         * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         * @throws NamingException when the animation cannot be created.
         * @throws RemoteException when the simulator cannot be reached.
         * @throws NetworkException when the GTU cannot be placed on the given lane.
         * @throws SimRuntimeException when the move method cannot be scheduled.
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public IDMCar(final int id, final GTUType<String> gtuType, final OTSDEVSSimulatorInterface simulator,
                final GTUFollowingModel carFollowingModel, final DoubleScalar.Rel<LengthUnit> vehicleLength,
                final DoubleScalar.Abs<TimeUnit> initialTime,
                final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
                final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException, NamingException,
                NetworkException, SimRuntimeException
        {
            super(id, gtuType, carFollowingModel, initialLongitudinalPositions, initialSpeed, vehicleLength,
                    new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                            SpeedUnit.KM_PER_HOUR), simulator);
            this.setRoute(getRoute());
        }
    }
}
