package org.opentrafficsim.demo.carFollowing;

import static org.opentrafficsim.core.gtu.GTUType.CAR;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;
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
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.graphs.AbstractPlot;
import org.opentrafficsim.core.graphs.ContourDataSource;
import org.opentrafficsim.core.graphs.ContourPlotAcceleration;
import org.opentrafficsim.core.graphs.ContourPlotDensity;
import org.opentrafficsim.core.graphs.ContourPlotFlow;
import org.opentrafficsim.core.graphs.ContourPlotSpeed;
import org.opentrafficsim.core.graphs.FundamentalDiagram;
import org.opentrafficsim.core.graphs.GraphCrossSection;
import org.opentrafficsim.core.graphs.TrajectoryPlot;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.DirectedLinkPosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.graphs.GraphLaneUtil;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Altruistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.toledo.ToledoFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.swing.gui.AnimationToggles;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterBoolean;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionList;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Circular road simulation demo.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularRoad extends AbstractWrappableAnimation implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private RoadSimulationModel model;

    /**
     * Create a CircularRoad simulation.
     * @throws InputParameterException when a property is not handled
     */
    public CircularRoad() throws InputParameterException
    {
        this.properties.add(new InputParameterSelectionList("LaneChanging", "Lane changing",
                "<html>The lane change strategies vary in politeness.<br>"
                        + "Two types are implemented:<ul><li>Egoistic (looks only at personal gain).</li>"
                        + "<li>Altruistic (assigns effect on new and current follower the same weight as "
                        + "the personal gain).</html>",
                new String[] { "Egoistic", "Altruistic" }, 0, 500));
        this.properties.add(new InputParameterSelectionList("TacticalPlanner", "Tactical planner",
                "<html>The tactical planner determines if a lane change is desired and possible.</html>",
                new String[] { "IDM", "MOBIL/IDM", "DIRECTED/IDM", "LMRS", "Toledo" }, 1, 600));
        this.properties.add(new InputParameterInteger("TrackLength", "Track length", "Circumference of the track", 2000, 500,
                6000, "Track length %dm", 10));
        this.properties.add(new InputParameterDouble("MeanDensity", "Mean density", "Number of vehicles per km", 40.0, 5.0,
                45.0, true, true, "Density %.1f veh/km", 11));
        this.properties.add(new InputParameterDouble("DensityVariability", "Density variability",
                "Variability of the number of vehicles per km", 0.0, 0.0, 1.0, true, true, "%.1f", 12));
        List<InputParameter<?>> outputProperties = new ArrayList<>();
        for (int lane = 1; lane <= 2; lane++)
        {
            int index = lane - 1;
            String laneId = String.format("Lane %d ", lane);
            outputProperties.add(index, new InputParameterBoolean(laneId + "Density", laneId + " Density",
                    laneId + "Density contour plot", true, 0));
            index += lane;
            outputProperties.add(index,
                    new InputParameterBoolean(laneId + "Flow", laneId + " Flow", laneId + "Flow contour plot", true, 1));
            index += lane;
            outputProperties.add(index,
                    new InputParameterBoolean(laneId + "Speed", laneId + " Speed", laneId + "Speed contour plot", true, 2));
            index += lane;
            outputProperties.add(index, new InputParameterBoolean(laneId + "Acceleration", laneId + " Acceleration",
                    laneId + "Acceleration contour plot", true, 3));
            index += lane;
            outputProperties.add(index, new InputParameterBoolean(laneId + "Trajectories", laneId + " Trajectories",
                    laneId + "Trajectory (time/distance) diagram", true, 4));
            // index += lane;
            // outputProperties.add(index, new InputParameterBoolean(laneId + "Variable Sample Rate Trajectories",
            // laneId + " VSR Trajectories", laneId + "Trajectory (time/distance) diagram", true, false, 5));
        }
        outputProperties.add(new InputParameterBoolean("Fundamental diagram aggregated", "Fundamental diagram aggregated",
                "Fundamental diagram aggregated", true, 5));
        outputProperties
                .add(new InputParameterBoolean("Fundamental diagram", "Fundamental diagram", "Fundamental diagram", true, 5));
        this.properties.add(
                new CompoundProperty("OutputGraphs", "Output graphs", "Select the graphical output", outputProperties, 1000));
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
     * @throws SimRuntimeException should never happen
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
                    CircularRoad circularRoad = new CircularRoad();
                    List<InputParameter<?>> propertyList = circularRoad.getProperties();
                    try
                    {
                        propertyList.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                                "<html>Mix of passenger cars and trucks</html>", new String[] { "passenger car", "truck" },
                                new Double[] { 0.8, 0.2 }, false, 10));
                    }
                    catch (InputParameterException exception)
                    {
                        exception.printStackTrace();
                    }
                    propertyList.add(new InputParameterSelectionList("CarFollowingModel", "Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account "
                                    + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                    + "curvature of the road) capabilities of the vehicle and personality "
                                    + "of the driver.</html>",
                            new String[] { "IDM", "IDM+" }, 1, false, 1));
                    propertyList.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car",
                            new Acceleration(1.0, METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2),
                            new Length(2.0, METER), new Duration(1.0, SECOND), 2));
                    propertyList.add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck",
                            new Acceleration(0.5, METER_PER_SECOND_2), new Acceleration(1.25, METER_PER_SECOND_2),
                            new Length(2.0, METER), new Duration(1.0, SECOND), 3));

                    circularRoad.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(3600.0, SECOND), propertyList, null,
                            true);
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
    protected final OTSModelInterface makeModel()
    {
        this.model = new RoadSimulationModel(getSavedUserModifiedProperties());
        return this.model;
    }

    /**
     * @return the saved user properties for a next run
     */
    private List<InputParameter<?>> getSavedUserModifiedProperties()
    {
        return this.savedUserModifiedProperties;
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected final void addTabs(final OTSSimulatorInterface simulator) throws OTSSimulationException, InputParameterException
    {
        // Make the tab with the plots
        InputParameter<?> output = new CompoundProperty("", "", "", this.properties, false, 0).findByKey("OutputGraphs");
        if (null == output)
        {
            throw new Error("Cannot find output properties");
        }
        ArrayList<InputParameterBoolean> graphs = new ArrayList<>();
        if (output instanceof CompoundProperty)
        {
            CompoundProperty outputProperties = (CompoundProperty) output;
            for (InputParameter<?> ap : outputProperties.getValue())
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

        GraphPath<KpiLaneDirection> path01;
        GraphPath<KpiLaneDirection> path0;
        GraphPath<KpiLaneDirection> path1;
        try
        {
            List<String> names = new ArrayList<>();
            names.add("Left lane");
            names.add("Right lane");
            List<LaneDirection> start = new ArrayList<>();
            start.add(new LaneDirection(this.model.getPath(0).get(0), GTUDirectionality.DIR_PLUS));
            start.add(new LaneDirection(this.model.getPath(1).get(0), GTUDirectionality.DIR_PLUS));
            path01 = GraphLaneUtil.createPath(names, start);
            path0 = GraphLaneUtil.createPath(names.get(0), start.get(0));
            path1 = GraphLaneUtil.createPath(names.get(1), start.get(1));
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }
        RoadSampler sampler = new RoadSampler(simulator);
        ContourDataSource<?> dataPool0 = new ContourDataSource<>(sampler, path0);
        ContourDataSource<?> dataPool1 = new ContourDataSource<>(sampler, path1);
        Duration updateInterval = Duration.createSI(10.0);

        for (int i = 0; i < graphCount; i++)
        {
            String graphName = graphs.get(i).getKey();
            AbstractPlot plot = null;
            GraphPath<KpiLaneDirection> path = null;
            ContourDataSource<?> dataPool = null;
            if (!graphName.contains("Fundamental diagram"))
            {
                int pos = graphName.indexOf(' ') + 1;
                String laneNumberText = graphName.substring(pos, pos + 1);
                int lane = Integer.parseInt(laneNumberText) - 1;
                path = lane == 0 ? path01 : path1;
                dataPool = lane == 0 ? dataPool0 : dataPool1;
            }

            if (graphName.contains("Trajectories"))
            {
                plot = new TrajectoryPlot(graphName, updateInterval, simulator, sampler, path);
            }
            else if (graphName.contains("Fundamental diagram"))
            {
                List<KpiLaneDirection> lanes = new ArrayList<>();
                List<Length> positions = new ArrayList<>();
                lanes.add(path01.get(0).getSource(0));
                lanes.add(path1.get(0).getSource(0));
                positions.add(Length.ZERO);
                positions.add(Length.ZERO);
                List<String> names = new ArrayList<>();
                names.add("Left lane");
                names.add("Right lane");
                DirectedLinkPosition linkPosition =
                        new DirectedLinkPosition(this.model.getPath(0).get(0).getParentLink(), 0.0, GTUDirectionality.DIR_PLUS);
                GraphCrossSection<KpiLaneDirection> crossSection;
                try
                {
                    crossSection = GraphLaneUtil.createCrossSection(names, linkPosition);
                }
                catch (NetworkException exception)
                {
                    throw new RuntimeException(exception);
                }
                if (graphName.contains("aggregated"))
                {
                    plot = new FundamentalDiagram(graphName, Quantity.DENSITY, Quantity.FLOW, simulator, sampler, crossSection,
                            true, Duration.createSI(60.0), false);
                }
                else
                {
                    plot = new FundamentalDiagram(graphName, Quantity.FLOW, Quantity.SPEED, simulator, sampler, crossSection,
                            false, Duration.createSI(60.0), false);
                }
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
        return "Circular Road simulation";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Circular Road simulation</h1>" + "Vehicles are unequally distributed over a two lane ring road.<br>"
                + "When simulation starts, all vehicles begin driving, some lane changes will occurr and some "
                + "shockwaves should develop.<br>"
                + "Trajectories and contourplots are generated during the simulation for both lanes.</html>";
    }

    /**
     * Simulate traffic on a circular, two-lane road.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version 1 nov. 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class RoadSimulationModel implements OTSModelInterface, UNITS
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private OTSSimulatorInterface simulator;

        /** Number of cars created. */
        private int carsCreated = 0;

        /** The car following model, e.g. IDM Plus for cars. */
        private GTUFollowingModelOld carFollowingModelCars;

        /** The car following model, e.g. IDM Plus for trucks. */
        private GTUFollowingModelOld carFollowingModelTrucks;

        /** The probability that the next generated GTU is a passenger car. */
        private double carProbability;

        /** The lane change model. */
        private AbstractLaneChangeModel laneChangeModel;

        /** Minimum distance. */
        private Length minimumDistance = new Length(0, METER);

        /** The speed limit. */
        private Speed speedLimit = new Speed(100, KM_PER_HOUR);

        /** User settable properties. */
        private List<InputParameter<?>> props = null;

        /** The sequence of Lanes that all vehicles will follow. */
        private List<List<Lane>> paths = new ArrayList<>();

        /** The random number generator used to decide what kind of GTU to generate etc. */
        private StreamInterface stream = new MersenneTwister(12345);

        /** Strategical planner generator for cars. */
        private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

        /** Strategical planner generator for cars. */
        private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

        /** The OTSNetwork. */
        private final OTSNetwork network = new OTSNetwork("network");

        /**
 * @param properties List&lt;InputParameter&lt;?&gt;&gt;; the properties
         */
        RoadSimulationModel(final List<InputParameter<?>> properties)
        {
            this.props = properties;
        }

        /**
         * @param index int; the rank number of the path
         * @return List&lt;Lane&gt;; the set of lanes for the specified index
         */
        public List<Lane> getPath(final int index)
        {
            return this.paths.get(index);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> theSimulator)
                throws SimRuntimeException
        {
            final int laneCount = 2;
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                this.paths.add(new ArrayList<Lane>());
            }
            this.simulator = (OTSSimulatorInterface) theSimulator;
            double radius = 6000 / 2 / Math.PI;
            double headway = 40;
            double headwayVariability = 0;
            try
            {

                // Get car-following model name
                String carFollowingModelName = null;
                CompoundProperty propertyContainer = new CompoundProperty("", "", "", this.props, false, 0);
                InputParameter<?> cfmp = propertyContainer.findByKey("CarFollowingModel");
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

                // Get car-following model parameter
                for (InputParameter<?> ap : new CompoundProperty("", "", "", this.props, false, 0))
                {
                    if (ap instanceof CompoundProperty)
                    {
                        CompoundProperty cp = (CompoundProperty) ap;
                        System.out.println("Checking compound property " + cp);
                        if (ap.getKey().contains("IDM"))
                        {
                            System.out.println("Car following model name appears to be " + ap.getKey());
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

                // Get lane change model
                cfmp = propertyContainer.findByKey("LaneChanging");
                if (null == cfmp)
                {
                    throw new Error("Cannot find \"Lane changing\" property");
                }
                if (cfmp instanceof InputParameterSelectionList)
                {
                    String laneChangeModelName = ((InputParameterSelectionList) cfmp).getValue();
                    if ("Egoistic".equals(laneChangeModelName))
                    {
                        this.laneChangeModel = new Egoistic();
                    }
                    else if ("Altruistic".equals(laneChangeModelName))
                    {
                        this.laneChangeModel = new Altruistic();
                    }
                    else
                    {
                        throw new Error("Lane changing " + laneChangeModelName + " not implemented");
                    }
                }
                else
                {
                    throw new Error("\"Lane changing\" property has wrong type");
                }

                // Get remaining properties
                for (InputParameter<?> ap : new CompoundProperty("", "", "", this.props, false, 0))
                {
                    if (ap instanceof InputParameterSelectionList)
                    {
                        InputParameterSelectionList sp = (InputParameterSelectionList) ap;
                        if ("TacticalPlanner".equals(sp.getKey()))
                        {
                            String tacticalPlannerName = sp.getValue();
                            if ("MOBIL/IDM".equals(tacticalPlannerName))
                            {
                                this.strategicalPlannerGeneratorCars =
                                        new LaneBasedStrategicalRoutePlannerFactory(new LaneBasedCFLCTacticalPlannerFactory(
                                                this.carFollowingModelCars, this.laneChangeModel));
                                this.strategicalPlannerGeneratorTrucks =
                                        new LaneBasedStrategicalRoutePlannerFactory(new LaneBasedCFLCTacticalPlannerFactory(
                                                this.carFollowingModelTrucks, this.laneChangeModel));
                            }
                            else if ("DIRECTED/IDM".equals(tacticalPlannerName))
                            {
                                this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                                        new LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(
                                                this.carFollowingModelCars));
                                this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                                        new LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(
                                                this.carFollowingModelTrucks));
                            }
                            else if ("LMRS".equals(tacticalPlannerName))
                            {
                                // provide default parameters with the car-following model
                                this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                                        new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
                                this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                                        new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
                            }
                            else if ("Toledo".equals(tacticalPlannerName))
                            {
                                this.strategicalPlannerGeneratorCars =
                                        new LaneBasedStrategicalRoutePlannerFactory(new ToledoFactory());
                                this.strategicalPlannerGeneratorTrucks =
                                        new LaneBasedStrategicalRoutePlannerFactory(new ToledoFactory());
                            }
                            else
                            {
                                throw new Error("Don't know how to create a " + tacticalPlannerName + " tactical planner");
                            }
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
                    else if (ap instanceof InputParameterInteger)
                    {
                        InputParameterInteger ip = (InputParameterInteger) ap;
                        if ("TrackLength".equals(ip.getKey()))
                        {
                            radius = ip.getValue() / 2 / Math.PI;
                        }
                    }
                    else if (ap instanceof InputParameterDouble)
                    {
                        InputParameterDouble cp = (InputParameterDouble) ap;
                        if (cp.getKey().equals("MeanDensity"))
                        {
                            headway = 1000 / cp.getValue();
                        }
                        if (cp.getKey().equals("DensityVariability"))
                        {
                            headwayVariability = cp.getValue();
                        }
                    }
                    else if (ap instanceof CompoundProperty)
                    {
                        if (ap.getKey().equals("OutputGraphs"))
                        {
                            continue; // Output settings are handled elsewhere
                        }
                    }
                }
                GTUType gtuType = CAR;
                LaneType laneType = LaneType.TWO_WAY_LANE;
                OTSNode start = new OTSNode(this.network, "Start", new OTSPoint3D(radius, 0, 0));
                OTSNode halfway = new OTSNode(this.network, "Halfway", new OTSPoint3D(-radius, 0, 0));

                OTSPoint3D[] coordsHalf1 = new OTSPoint3D[127];
                for (int i = 0; i < coordsHalf1.length; i++)
                {
                    double angle = Math.PI * (1 + i) / (1 + coordsHalf1.length);
                    coordsHalf1[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
                }
                Lane[] lanes1 = LaneFactory.makeMultiLane(this.network, "FirstHalf", start, halfway, coordsHalf1, laneCount,
                        laneType, this.speedLimit, this.simulator);
                OTSPoint3D[] coordsHalf2 = new OTSPoint3D[127];
                for (int i = 0; i < coordsHalf2.length; i++)
                {
                    double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                    coordsHalf2[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
                }
                Lane[] lanes2 = LaneFactory.makeMultiLane(this.network, "SecondHalf", halfway, start, coordsHalf2, laneCount,
                        laneType, this.speedLimit, this.simulator);
                for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
                {
                    this.paths.get(laneIndex).add(lanes1[laneIndex]);
                    this.paths.get(laneIndex).add(lanes2[laneIndex]);
                }
                // Put the (not very evenly spaced) cars on the track
                double variability = (headway - 20) * headwayVariability;
                System.out.println("headway is " + headway + " variability limit is " + variability);
                Random random = new Random(12345);
                for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
                {
                    double lane1Length = lanes1[laneIndex].getLength().getSI();
                    double trackLength = lane1Length + lanes2[laneIndex].getLength().getSI();
                    for (double pos = 0; pos <= trackLength - headway - variability;)
                    {
                        Lane lane = pos >= lane1Length ? lanes2[laneIndex] : lanes1[laneIndex];
                        // Actual headway is uniformly distributed around headway
                        double laneRelativePos = pos > lane1Length ? pos - lane1Length : pos;
                        double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                        // System.out.println(lane + ", len=" + lane.getLength() + ", pos=" + laneRelativePos);
                        generateCar(new Length(laneRelativePos, METER), lane, gtuType);
                        pos += actualHeadway;
                    }
                }
            }
            catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException
                    | InputParameterException exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Generate one car.
         * @param initialPosition Length; the initial position of the new cars
         * @param lane Lane; the lane on which the new cars are placed
         * @param gtuType GTUType; the type of the new cars
         * @throws NamingException on ???
         * @throws SimRuntimeException cannot happen
         * @throws NetworkException on network inconsistency
         * @throws GTUException when something goes wrong during construction of the car
         * @throws OTSGeometryException when the initial position is outside the center line of the lane
         */
        protected final void generateCar(final Length initialPosition, final Lane lane, final GTUType gtuType)
                throws NamingException, NetworkException, SimRuntimeException, GTUException, OTSGeometryException
        {

            // GTU itself
            boolean generateTruck = this.stream.nextDouble() > this.carProbability;
            Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
            LaneBasedIndividualGTU gtu =
                    new LaneBasedIndividualGTU("" + (++this.carsCreated), gtuType, vehicleLength, new Length(1.8, METER),
                            new Speed(200, KM_PER_HOUR), vehicleLength.multiplyBy(0.5), this.simulator, this.network);
            gtu.setNoLaneChangeDistance(Length.ZERO);
            gtu.setMaximumAcceleration(Acceleration.createSI(3.0));
            gtu.setMaximumDeceleration(Acceleration.createSI(-8.0));

            // strategical planner
            LaneBasedStrategicalPlanner strategicalPlanner;
            Route route = null;
            if (!generateTruck)
            {
                strategicalPlanner = this.strategicalPlannerGeneratorCars.create(gtu, route, null, null);
            }
            else
            {
                strategicalPlanner = this.strategicalPlannerGeneratorTrucks.create(gtu, route, null, null);
            }

            // init
            Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
            initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
            Speed initialSpeed = new Speed(0, KM_PER_HOUR);
            gtu.init(strategicalPlanner, initialPositions, initialSpeed, DefaultCarAnimation.class,
                    CircularRoad.this.getColorer());
            // Check that the gtu is indeed at the expected position
            /*-
            Lane actualLane = gtu.getLanes().keySet().iterator().next();
            if (actualLane != lane)
            {
            System.out.println("Newly created GTU already switched from lane" + lane + " to " + actualLane);
            }
            else
            {
            Length actualPosition = gtu.position(lane, gtu.getReference());
            if (Math.abs(actualPosition.si - initialPosition.si) > 0.5)
            {
                System.out.println("Newly created GTU jumped from " + initialPosition + " to " + actualPosition);
            }
            DirectedPoint actualLocation = gtu.getLocation();
            DirectedPoint expectedLocation =
                    lane.getCenterLine().extract(initialPosition, initialPosition.plus(new Length(0.0001, LengthUnit.METER)))
                            .getLocation();
            double distance = actualLocation.distance(expectedLocation);
            if (distance > 0.5)
            {
                System.out.println("Actual location of new GTU is " + actualLocation + ", expectedLocation is "
                        + expectedLocation + " difference is " + distance + "m");
            }
            }
             */
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
        {
            return this.simulator;
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
         * Stop simulation and throw an Error.
         * @param theSimulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
         * @param errorMessage String; the error message
         */
        public void stopSimulator(final DEVSSimulatorInterface.TimeDoubleUnit theSimulator, final String errorMessage)
        {
            System.out.println("Error: " + errorMessage);
            try
            {
                if (theSimulator.isRunning())
                {
                    theSimulator.stop();
                }
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
            throw new Error(errorMessage);
        }

    }
}
