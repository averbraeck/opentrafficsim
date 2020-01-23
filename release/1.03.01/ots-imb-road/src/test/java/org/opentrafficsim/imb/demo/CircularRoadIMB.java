package org.opentrafficsim.imb.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.DirectedLinkPosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.graphs.GraphCrossSection;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.draw.road.SensorAnimation;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.OTSIMBConnector;
import org.opentrafficsim.imb.transceiver.urbanstrategy.GTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LaneGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LinkGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NetworkTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NodeTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SensorGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SimulatorTransceiver;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.SwingContourPlot;
import org.opentrafficsim.swing.graphs.SwingFundamentalDiagram;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Circular road simulation demo.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-08-24 13:50:36 +0200 (Wed, 24 Aug 2016) $, @version $Revision: 2144 $, by $Author: pknoppers $,
 * initial version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularRoadIMB extends OTSSimulationApplication<CircularRoadModelIMB> implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a CircularRoadIMB Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     * @throws OTSDrawingException on animation error
     */
    public CircularRoadIMB(final String title, final OTSAnimationPanel panel, final CircularRoadModelIMB model)
            throws OTSDrawingException
    {
        super(model, panel);
        OTSNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
    }

    /** {@inheritDoc} */
    @Override
    protected void addTabs()
    {
        addStatisticsTabs(getModel().getSimulator());
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OTSAnimator simulator = new OTSAnimator();
            final CircularRoadModelIMB otsModel = new CircularRoadModelIMB(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, new DefaultSwitchableGTUColorer(), otsModel.getNetwork());
                CircularRoadIMB app = new CircularRoadIMB("Circular Road", animationPanel, otsModel);
                app.setExitOnClose(exitOnClose);
            }
            else
            {
                if (exitOnClose)
                {
                    System.exit(0);
                }
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add the statistics tabs.
     * @param simulator the simulator on which sampling can be scheduled
     */
    protected final void addStatisticsTabs(final OTSSimulatorInterface simulator)
    {
        GraphPath<KpiLaneDirection> path01;
        GraphPath<KpiLaneDirection> path0;
        GraphPath<KpiLaneDirection> path1;
        try
        {
            List<String> names = new ArrayList<>();
            names.add("Left lane");
            names.add("Right lane");
            List<LaneDirection> start = new ArrayList<>();
            start.add(new LaneDirection(getModel().getPath(0).get(0), GTUDirectionality.DIR_PLUS));
            start.add(new LaneDirection(getModel().getPath(1).get(0), GTUDirectionality.DIR_PLUS));
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
        Duration updateInterval = Duration.instantiateSI(10.0);

        SwingPlot plot = null;
        GraphPath<KpiLaneDirection> path = null;
        ContourDataSource<?> dataPool = null;

        TablePanel trajectoryChart = new TablePanel(2, 2);
        plot = new SwingTrajectoryPlot(new TrajectoryPlot("Trajectory all lanes", updateInterval, simulator, sampler, path01));
        trajectoryChart.setCell(plot.getContentPane(), 0, 0);

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
                new DirectedLinkPosition(getModel().getPath(0).get(0).getParentLink(), 0.0, GTUDirectionality.DIR_PLUS);
        GraphCrossSection<KpiLaneDirection> crossSection;
        try
        {
            crossSection = GraphLaneUtil.createCrossSection(names, linkPosition);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException(exception);
        }

        plot = new SwingFundamentalDiagram(new FundamentalDiagram("Fundamental diagram Density-Flow", Quantity.DENSITY,
                Quantity.FLOW, simulator, sampler, crossSection, true, Duration.instantiateSI(60.0), false));
        trajectoryChart.setCell(plot.getContentPane(), 1, 0);

        plot = new SwingFundamentalDiagram(new FundamentalDiagram("Fundamental diagram Flow-Speed", Quantity.FLOW,
                Quantity.SPEED, simulator, sampler, crossSection, false, Duration.instantiateSI(60.0), false));
        trajectoryChart.setCell(plot.getContentPane(), 1, 1);

        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "Trajectories",
                trajectoryChart);

        for (int lane : new int[] {0, 1})
        {
            TablePanel charts = new TablePanel(3, 2);
            path = lane == 0 ? path0 : path1;
            dataPool = lane == 0 ? dataPool0 : dataPool1;

            plot = new SwingTrajectoryPlot(
                    new TrajectoryPlot("Trajectory lane " + lane, updateInterval, simulator, sampler, path));
            charts.setCell(plot.getContentPane(), 0, 0);

            plot = new SwingContourPlot(new ContourPlotDensity("Density lane " + lane, simulator, dataPool));
            charts.setCell(plot.getContentPane(), 1, 0);

            plot = new SwingContourPlot(new ContourPlotSpeed("Speed lane " + lane, simulator, dataPool));
            charts.setCell(plot.getContentPane(), 1, 1);

            plot = new SwingContourPlot(new ContourPlotFlow("Flow lane " + lane, simulator, dataPool));
            charts.setCell(plot.getContentPane(), 2, 0);

            plot = new SwingContourPlot(new ContourPlotAcceleration("Accceleration lane " + lane, simulator, dataPool));
            charts.setCell(plot.getContentPane(), 2, 1);

            getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "stats lane " + lane,
                    charts);
        }
    }
}

/**
 * Simulate traffic on a circular, two-lane road.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-08-24 13:50:36 +0200 (Wed, 24 Aug 2016) $, @version $Revision: 2144 $, by $Author: pknoppers $,
 * initial version 1 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class CircularRoadModelIMB extends AbstractOTSModel implements UNITS
{
    /** */
    private static final long serialVersionUID = 20141121L;

    /** Number of cars created. */
    private int carsCreated = 0;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** Minimum distance. */
    private Length minimumDistance = new Length(0, METER);

    /** The speed limit. */
    private Speed speedLimit = new Speed(100, KM_PER_HOUR);

    /** The sequence of Lanes that all vehicles will follow. */
    private List<List<Lane>> paths = new ArrayList<>();

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

    /** the network as created by the AbstractWrappableIMBAnimation. */
    private final OTSRoadNetwork network = new OTSRoadNetwork("network", true);

    /** The random number generator used to decide what kind of GTU to generate etc. */
    private StreamInterface stream = new MersenneTwister(12345);

    /** Connector to the IMB hub. */
    OTSIMBConnector imbConnector;

    /**
     * @param simulator the simulator for this model
     */
    public CircularRoadModelIMB(final OTSSimulatorInterface simulator)
    {
        super(simulator);
        makeInputParameterMap();
    }

    /**
     * Make a map of input parameters for this demo.
     */
    public void makeInputParameterMap()
    {
        try
        {
            InputParameterHelper.makeInputParameterMapIMB(this.inputParameterMap);
            InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 4.0);
            InputParameterMap genericMap = (InputParameterMap) this.inputParameterMap.get("generic");

            genericMap.add(new InputParameterDoubleScalar<LengthUnit, Length>("trackLength", "Track length",
                    "Track length (circumfence of the track)", Length.instantiateSI(1000.0), Length.instantiateSI(500.0),
                    Length.instantiateSI(2000.0), true, true, "%.0f", 1.0));
            genericMap.add(new InputParameterDouble("densityMean", "Mean density (veh / km)",
                    "mean density of the vehicles (vehicles per kilometer)", 30.0, 5.0, 45.0, true, true, "%.0f", 2.0));
            genericMap.add(new InputParameterDouble("densityVariability", "Density variability",
                    "Variability of the denisty: variability * (headway - 20) meters", 0.0, 0.0, 1.0, true, true, "%.00f",
                    3.0));
        }
        catch (InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        System.out.println("CircularRoadIMB: constructModel called; Connecting to IMB");

        try
        {
            InputParameterMap imbSettings = (InputParameterMap) getInputParameterMap().get("imb");
            Throw.whenNull(imbSettings, "IMB Settings not found in properties");
            this.imbConnector = OTSIMBConnector.create(imbSettings, "CircularRoadIMB");
            new NetworkTransceiver(this.imbConnector, getSimulator(), this.network);
            new NodeTransceiver(this.imbConnector, getSimulator(), this.network);
            new LinkGTUTransceiver(this.imbConnector, getSimulator(), this.network);
            new LaneGTUTransceiver(this.imbConnector, getSimulator(), this.network);
            new GTUTransceiver(this.imbConnector, getSimulator(), this.network);
            new SensorGTUTransceiver(this.imbConnector, getSimulator(), this.network);
            new SimulatorTransceiver(this.imbConnector, getSimulator());
        }
        catch (IMBException | InputParameterException exception)
        {
            throw new SimRuntimeException(exception);
        }

        try
        {
            final int laneCount = 2;
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                this.paths.add(new ArrayList<Lane>());
            }

            this.carProbability = (double) getInputParameter("generic.carProbability");
            double radius = ((Length) getInputParameter("generic.trackLength")).si / 2 / Math.PI;
            double headway = 1000.0 / (double) getInputParameter("generic.densityMean");
            double headwayVariability = (double) getInputParameter("generic.densityVariability");

            Acceleration aCar = (Acceleration) getInputParameter("car.a");
            Acceleration bCar = (Acceleration) getInputParameter("car.b");
            Length s0Car = (Length) getInputParameter("car.s0");
            Duration tSafeCar = (Duration) getInputParameter("car.tSafe");
            CarFollowingModel carFollowingModel = new IDMPlus();

            Acceleration aTruck = (Acceleration) getInputParameter("truck.a");
            Acceleration bTruck = (Acceleration) getInputParameter("truck.b");
            Length s0Truck = (Length) getInputParameter("truck.s0");
            Duration tSafeTruck = (Duration) getInputParameter("truck.tSafe");
            CarFollowingModel truckFollowingModel = new IDMPlus();

            this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
            this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));

            GTUType gtuType = network.getGtuType(GTUType.DEFAULTS.CAR);
            LaneType laneType = network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
            OTSRoadNode start = new OTSRoadNode(this.network, "Start", new OTSPoint3D(radius, 0, 0),
                    new Direction(90, DirectionUnit.EAST_DEGREE));
            OTSRoadNode halfway = new OTSRoadNode(this.network, "Halfway", new OTSPoint3D(-radius, 0, 0),
                    new Direction(270, DirectionUnit.EAST_DEGREE));

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

            int sensorNr = 0;
            for (Lane lane : lanes1)
            {
                new SimpleSilentSensor("sensor " + ++sensorNr, lane, new Length(10.0, LengthUnit.METER), RelativePosition.FRONT,
                        getSimulator());
            }
            for (Lane lane : lanes2)
            {
                new SimpleSilentSensor("sensor" + ++sensorNr, lane, new Length(20.0, LengthUnit.METER), RelativePosition.REAR,
                        getSimulator());
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
                    generateGTU(new Length(laneRelativePos, METER), lane, gtuType);
                    pos += actualHeadway;
                }
            }

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Generate one gtu.
     * @param initialPosition Length; the initial position of the new cars
     * @param lane Lane; the lane on which the new cars are placed
     * @param gtuType GTUType; the type of the new cars
     * @throws SimRuntimeException cannot happen
     * @throws NetworkException on network inconsistency
     * @throws GTUException when something goes wrong during construction of the car
     * @throws OTSGeometryException when the initial position is outside the center line of the lane
     */
    protected final void generateGTU(final Length initialPosition, final Lane lane, final GTUType gtuType)
            throws GTUException, NetworkException, SimRuntimeException, OTSGeometryException
    {
        // GTU itself
        boolean generateTruck = this.stream.nextDouble() > this.carProbability;
        Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
        LaneBasedIndividualGTU gtu =
                new LaneBasedIndividualGTU("" + (++this.carsCreated), gtuType, vehicleLength, new Length(1.8, METER),
                        new Speed(200, KM_PER_HOUR), vehicleLength.times(0.5), this.simulator, this.network);
        gtu.setNoLaneChangeDistance(Length.ZERO);
        gtu.setMaximumAcceleration(Acceleration.instantiateSI(3.0));
        gtu.setMaximumDeceleration(Acceleration.instantiateSI(-8.0));

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
        gtu.init(strategicalPlanner, initialPositions, initialSpeed);
    }

    /** {@inheritDoc} */
    @Override
    public final OTSNetwork getNetwork()
    {
        return this.network;
    }

    /**
     * @param index int; the rank number of the path
     * @return List&lt;Lane&gt;; the set of lanes for the specified index
     */
    public List<Lane> getPath(final int index)
    {
        return this.paths.get(index);
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

    /**
     * Simple sensor that does not provide output, but is drawn on the Lanes.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 18, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class SimpleSilentSensor extends AbstractSensor
    {
        /** */
        private static final long serialVersionUID = 20150130L;

        /**
         * @param lane Lane; the lane of the sensor.
         * @param position Length; the position of the sensor
         * @param triggerPosition RelativePosition.TYPE; the relative position type (e.g., FRONT, REAR) of the vehicle that
         *            triggers the sensor.
         * @param id String; the id of the sensor.
         * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to enable animation.
         * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
         * @throws OTSGeometryException when the geometry of the sensor cannot be calculated, e.g. when the lane width is zero,
         *             or the position is beyond or before the lane length
         */
        public SimpleSilentSensor(final String id, final Lane lane, final Length position,
                final RelativePosition.TYPE triggerPosition, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
                throws NetworkException, OTSGeometryException
        {
            super(id, lane, position, triggerPosition, simulator, LaneBasedObject.makeGeometry(lane, position),
                    Compatible.EVERYTHING);
            try
            {
                new SensorAnimation(this, position, simulator, Color.RED);
            }
            catch (RemoteException | NamingException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public final void triggerResponse(final LaneBasedGTU gtu)
        {
            // do nothing.
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "SimpleSilentSensor [Lane=" + this.getLane() + "]";
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public SimpleSilentSensor clone(final CrossSectionElement newCSE, final SimulatorInterface.TimeDoubleUnit newSimulator)
                throws NetworkException
        {
            Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
            Throw.when(!(newSimulator instanceof DEVSSimulatorInterface.TimeDoubleUnit), NetworkException.class,
                    "simulator should be a DEVSSimulator");
            try
            {
                return new SimpleSilentSensor(getId(), (Lane) newCSE, getLongitudinalPosition(), getPositionType(),
                        (DEVSSimulatorInterface.TimeDoubleUnit) newSimulator);
            }
            catch (OTSGeometryException exception)
            {
                throw new NetworkException(exception);
            }
        }
    }
}
