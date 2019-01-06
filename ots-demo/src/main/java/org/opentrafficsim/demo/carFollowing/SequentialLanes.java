package org.opentrafficsim.demo.carFollowing;

import static org.opentrafficsim.core.gtu.GTUType.CAR;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.jgrapht.GraphPath;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.ProbabilityDistributionProperty;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.demo.carFollowing.Straight.StraightModel;
import org.opentrafficsim.draw.graphs.AbstractPlot;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.gui.AbstractOTSSwingApplication;
import org.opentrafficsim.swing.gui.AnimationToggles;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterBoolean;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionList;
import nl.tudelft.simulation.dsol.swing.gui.HTMLPanel;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;

/**
 * Single lane road consisting of three consecutive links.<br>
 * Tests that GTUs correctly transfer themselves onto the next lane and that the graph samplers handle this situation.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 30 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SequentialLanes extends AbstractOTSSwingApplication implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private SequentialModel model;

    /**
     * Create a SequentialLanes simulation.
     * @throws InputParameterException when the provided properties could not be handled
     */
    public SequentialLanes() throws InputParameterException
    {
        List<InputParameter<?, ?>> outputProperties = new ArrayList<>();
        outputProperties.add(new InputParameterBoolean("DensityPlot", "Density", "Density contour plot", true, false, 0));
        outputProperties.add(new InputParameterBoolean("FlowPlot", "Flow", "Flow contour plot", true, false, 1));
        outputProperties.add(new InputParameterBoolean("SpeedPlot", "Speed", "Speed contour plot", true, false, 2));
        outputProperties.add(
                new InputParameterBoolean("AccelerationPlot", "Acceleration", "Acceleration contour plot", true, false, 3));
        outputProperties.add(new InputParameterBoolean("TrajectoryPlot", "Trajectories", "Trajectory (time/distance) diagram",
                true, false, 4));
        this.inputParameterMap.add(new CompoundProperty("OutputGraphs", "Output graphs", "Select the graphical output",
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
            @SuppressWarnings("synthetic-access")
            @Override
            public void run()
            {
                try
                {
                    SequentialLanes sequential = new SequentialLanes();
                    List<InputParameter<?, ?>> localProperties = sequential.getProperties();
                    try
                    {
                        localProperties.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                                "<html>Mix of passenger cars and trucks</html>", new String[] { "passenger car", "truck" },
                                new Double[] { 0.8, 0.2 }, false, 10));
                    }
                    catch (InputParameterException exception)
                    {
                        exception.printStackTrace();
                    }
                    localProperties.add(new InputParameterSelectionList("CarFollowingModel", "Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account "
                                    + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                    + "curvature of the road) capabilities of the vehicle and personality "
                                    + "of the driver.</html>",
                            new String[] { "IDM", "IDM+" }, 1, false, 1));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car",
                            new Acceleration(1.0, METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2),
                            new Length(2.0, METER), new Duration(1.0, SECOND), 2));
                    localProperties.add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck",
                            new Acceleration(0.5, METER_PER_SECOND_2), new Acceleration(1.25, METER_PER_SECOND_2),
                            new Length(2.0, METER), new Duration(1.0, SECOND), 3));
                    sequential.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(3600.0, SECOND), localProperties, null,
                            true);
                    sequential.panel.getTabbedPane().addTab("info", sequential.makeInfoPane());
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | InputParameterException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel()
    {
        this.model = new SequentialModel(this.savedUserModifiedProperties);
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
    protected final void addTabs(final OTSSimulatorInterface simulator) throws OTSSimulationException, InputParameterException
    {
        // Make the tab with the plots
        InputParameter<?, ?> output = new CompoundProperty("", "", "", this.properties, false, 0).findByKey("OutputGraphs");
        if (null == output)
        {
            throw new Error("Cannot find output properties");
        }
        ArrayList<InputParameterBoolean> graphs = new ArrayList<>();
        if (output instanceof CompoundProperty)
        {
            CompoundProperty outputProperties = (CompoundProperty) output;
            for (InputParameter<?, ?> ap : outputProperties.getValue())
            {
                if (ap instanceof InputParameterBoolean)
                {
                    InputParameterBoolean bp = (InputParameterBoolean) ap;
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
        GraphPath<KpiLaneDirection> graphPath;
        try
        {
            graphPath = GraphLaneUtil.createPath("Path",
                    new LaneDirection(this.model.getPath().get(0), GTUDirectionality.DIR_PLUS));
        }
        catch (NetworkException exception)
        {
            throw new OTSSimulationException(exception);
        }
        RoadSampler sampler = new RoadSampler(simulator);
        ContourDataSource dataPool = new ContourDataSource(sampler, graphPath);
        Duration updateInterval = Duration.createSI(10.0);

        for (int i = 0; i < graphCount; i++)
        {
            String graphName = graphs.get(i).getKey();
            AbstractPlot plot = null;
            if (graphName.contains("TrajectoryPlot"))
            {
                plot = new TrajectoryPlot("Trajectory Graph", updateInterval, simulator, sampler, graphPath);
            }
            else
            {
                if (graphName.contains("Density"))
                {
                    plot = new ContourPlotDensity(graphName, simulator, dataPool);
                }
                else if (graphName.contains("Speed"))
                {
                    plot = new ContourPlotSpeed(graphName, simulator, dataPool);
                }
                else if (graphName.contains("Flow"))
                {
                    plot = new ContourPlotFlow(graphName, simulator, dataPool);
                }
                else if (graphName.contains("Acceleration"))
                {
                    plot = new ContourPlotAcceleration(graphName, simulator, dataPool);
                }
                else
                {
                    throw new Error("Unhandled type of contourplot: " + graphName);
                }
            }
            // Add the container to the matrix
            charts.setCell(plot.getContentPane(), i % columns, i / columns);
        }
        addTab(getTabCount(), "statistics", charts);
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

    /**
     * Build the sequential model.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version 0 jan. 2015 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class SequentialModel extends AbstractOTSModel implements UNITS
    {
        /** */
        private static final long serialVersionUID = 20150130L;

        /** The network. */
        private final OTSNetwork network = new OTSNetwork("network");

        /** The nodes of our network in the order that all GTUs will visit them. */
        private List<OTSNode> nodes = new ArrayList<>();

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
        private GTUType gtuType = CAR;

        /** Minimum distance. */
        private Length minimumDistance = new Length(0, METER);

        /** The Lane where newly created Cars initially placed on. */
        private Lane initialLane;

        /** Maximum distance. */
        private Length maximumDistance = new Length(2001, METER);

        /** The random number generator used to decide what kind of GTU to generate. */
        private Random randomGenerator = new Random(12345);

        /** User settable properties. */
        @SuppressWarnings("hiding")
        private List<InputParameter<?, ?>> properties = null;

        /** The sequence of Lanes that all vehicles will follow. */
        private List<Lane> path = new ArrayList<>();

        /** The speedLimit on all Lanes. */
        private Speed speedLimit;

        /**
         * @param properties List&lt;InputParameter&lt;?&gt;&gt;; the user settable properties
         */
        SequentialModel(final List<InputParameter<?, ?>> properties)
        {
            this.properties = properties;
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
        public final void constructModel() throws SimRuntimeException
        {
            this.speedLimit = new Speed(100, KM_PER_HOUR);

            // TODO Bezier curves make 180 degree mistake when minus is true
            boolean minus = false;

            this.nodes = new ArrayList<>();
            try
            {
                OTSNode n0 = new OTSNode(this.network, "Node(0,0)", new OTSPoint3D(0, 0));
                OTSNode n1 = new OTSNode(this.network, "Node(1000,0)", new OTSPoint3D(1000, 0));
                OTSNode n2 = new OTSNode(this.network, "Node(1020,3)", new OTSPoint3D(1020, 3));
                OTSNode n3 = new OTSNode(this.network, "Node(2000,197)", new OTSPoint3D(2000, 197));
                OTSNode n4 = new OTSNode(this.network, "Node(2020,200)", new OTSPoint3D(2020, 200));
                OTSNode n5 = new OTSNode(this.network, "Node(2200,200)", new OTSPoint3D(2200, 200));
                this.nodes.addAll(Arrays.asList(new OTSNode[] { n0, n1, n2, n3, n4, n5 }));

                LaneType laneType = LaneType.TWO_WAY_LANE;

                // Now we can build a series of Links with one Lane on them
                ArrayList<CrossSectionLink> links = new ArrayList<>();
                OTSLine3D l01 = new OTSLine3D(n0.getPoint(), n1.getPoint());
                OTSLine3D l12 = LaneFactory.makeBezier(n0, n1, n2, n3);
                OTSLine3D l23 =
                        minus ? new OTSLine3D(n3.getPoint(), n2.getPoint()) : new OTSLine3D(n2.getPoint(), n3.getPoint());
                OTSLine3D l34 = LaneFactory.makeBezier(n2, n3, n4, n5);
                OTSLine3D l45 = new OTSLine3D(n4.getPoint(), n5.getPoint());
                OTSLine3D[] lines = new OTSLine3D[] { l01, l12, l23, l34, l45 };

                for (int i = 1; i < this.nodes.size(); i++)
                {
                    OTSNode fromNode = this.nodes.get(i - 1);
                    OTSNode toNode = this.nodes.get(i);
                    OTSLine3D line = lines[i - 1];
                    String linkName = fromNode.getId() + "-" + toNode.getId();
                    // LongitudinalDirectionality direction = line.equals(l23) && minus ? LongitudinalDirectionality.DIR_MINUS
                    // : LongitudinalDirectionality.DIR_PLUS;
                    Lane[] lanes = LaneFactory.makeMultiLane(this.network, linkName, fromNode, toNode, line.getPoints(), 1,
                            laneType, this.speedLimit, this.simulator);
                    if (i == this.nodes.size() - 1)
                    {
                        new SinkSensor(lanes[0], new Length(100.0, METER), this.simulator);
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
            this.simulator.scheduleEventAbs(Time.ZERO, this, this, "generateCar", null);
            try
            {
                String carFollowingModelName = null;
                CompoundProperty propertyContainer = new CompoundProperty("", "", "", this.properties, false, 0);
                InputParameter<?, ?> cfmp = propertyContainer.findByKey("CarFollowingModel");
                if (null == cfmp)
                {
                    throw new Error("Cannot find \"Car following model\" property");
                }
                if (cfmp instanceof InputParameterSelectionList)
                {
                    carFollowingModelName = ((InputParameterSelectionList) cfmp).getValue();
                }
                else
                {
                    throw new Error("\"Car following model\" property has wrong type");
                }
                for (InputParameter<?, ?> ap : new CompoundProperty("", "", "", this.properties, false, 0))
                {
                    if (ap instanceof InputParameterSelectionList)
                    {
                        InputParameterSelectionList sp = (InputParameterSelectionList) ap;
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
        public OTSNetwork getNetwork()
        {
            return this.network;
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
                Parameters parameters = DefaultsFactory.getDefaultParameters();
                LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, vehicleLength,
                        new Length(1.8, METER), new Speed(200, KM_PER_HOUR), vehicleLength.multiplyBy(0.5), this.simulator,
                        this.network);
                LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                        new LaneBasedGTUFollowingTacticalPlanner(gtuFollowingModel, gtu), gtu);
                gtu.setParameters(parameters);
                gtu.init(strategicalPlanner, initialPositions, initialSpeed, DefaultCarAnimation.class,
                        SequentialLanes.this.getColorer());
                this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
            }
            catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }
        }

    }
}
