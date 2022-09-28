package org.opentrafficsim.demo;

import java.awt.Dimension;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.SequentialLanes.SequentialModel;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.SwingContourPlot;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DSOLException;

/**
 * Single lane road consisting of three consecutive links.<br>
 * Tests that GTUs correctly transfer themselves onto the next lane and that the graph samplers handle this situation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 30 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SequentialLanes extends OTSSimulationApplication<SequentialModel> implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a Straight Swing application.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model SequentialModel; the model
     * @throws OTSDrawingException on animation error
     */
    public SequentialLanes(final String title, final OTSAnimationPanel panel, final SequentialModel model)
            throws OTSDrawingException
    {
        super(model, panel);
        OTSRoadNetwork network = model.getNetwork();
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
            OTSAnimator simulator = new OTSAnimator("SequentialLanes");
            final SequentialModel otsModel = new SequentialModel(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, DEFAULT_COLORER, otsModel.getNetwork());
                SequentialLanes app = new SequentialLanes("SequentialLanes", animationPanel, otsModel);
                app.setExitOnClose(exitOnClose);
                animationPanel.enableSimulationControlButtons();
            }
            else
            {
                if (exitOnClose)
                {
                    System.exit(0);
                }
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add the statistics tabs.
     * @param simulator OTSSimulatorInterface; the simulator on which sampling can be scheduled
     */
    protected final void addStatisticsTabs(final OTSSimulatorInterface simulator)
    {
        GraphPath<KpiLaneDirection> path;
        try
        {
            path = GraphLaneUtil.createPath("Lane", new LaneDirection(getModel().getPath().get(0), GTUDirectionality.DIR_PLUS));
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }

        RoadSampler sampler = new RoadSampler(getModel().getNetwork());
        GraphPath.initRecording(sampler, path);
        ContourDataSource<?> dataPool = new ContourDataSource<>(sampler.getSamplerData(), path);
        TablePanel charts = new TablePanel(3, 2);
        SwingPlot plot = null;

        plot = new SwingTrajectoryPlot(
                new TrajectoryPlot("TrajectoryPlot", Duration.instantiateSI(10.0), simulator, sampler.getSamplerData(), path));
        charts.setCell(plot.getContentPane(), 0, 0);

        plot = new SwingContourPlot(new ContourPlotDensity("DensityPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 1, 0);

        plot = new SwingContourPlot(new ContourPlotSpeed("SpeedPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 2, 0);

        plot = new SwingContourPlot(new ContourPlotFlow("FlowPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 1, 1);

        plot = new SwingContourPlot(new ContourPlotAcceleration("AccelerationPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 2, 1);

        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "statistics ", charts);
    }

    /**
     * Build the sequential model.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$, initial version 0 jan. 2015 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class SequentialModel extends AbstractOTSModel implements UNITS
    {
        /** */
        private static final long serialVersionUID = 20150130L;

        /** The network. */
        private final OTSRoadNetwork network = new OTSRoadNetwork("network", true, getSimulator());

        /** The nodes of our network in the order that all GTUs will visit them. */
        private List<OTSRoadNode> nodes = new ArrayList<>();

        /** Strategical planner generator for cars. */
        private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

        /** Strategical planner generator for trucks. */
        private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

        /** Car parameters. */
        private Parameters parametersCar;

        /** Truck parameters. */
        private Parameters parametersTruck;

        /** The probability that the next generated GTU is a passenger car. */
        private double carProbability;

        /** The headway (inter-vehicle time). */
        private Duration headway;

        /** Number of cars created. */
        private int carsCreated = 0;

        /** Minimum distance. */
        private Length minimumDistance = new Length(0, METER);

        /** The Lane where newly created Cars initially placed on. */
        private Lane initialLane;

        /** Maximum distance. */
        private Length maximumDistance = new Length(2001, METER);

        /** The random number generator used to decide what kind of GTU to generate. */
        private StreamInterface stream = new MersenneTwister(12345);

        /** The sequence of Lanes that all vehicles will follow. */
        private List<Lane> path = new ArrayList<>();

        /** The speedLimit on all Lanes. */
        private Speed speedLimit;

        /**
         * @param simulator OTSSimulatorInterface; the simulator for this model
         */
        SequentialModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
            InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 1.0);
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
                OTSPoint3D p2 = new OTSPoint3D(1020, 3);
                OTSPoint3D p3 = new OTSPoint3D(2000, 197);
                Direction dir23 = p2.horizontalDirection(p3);
                OTSRoadNode n0 = new OTSRoadNode(this.network, "Node-0-(0,0)", new OTSPoint3D(0, 0), Direction.ZERO);
                OTSRoadNode n1 = new OTSRoadNode(this.network, "Node-1-(1000,0)", new OTSPoint3D(1000, 0), Direction.ZERO);
                OTSRoadNode n2 = new OTSRoadNode(this.network, "Node-2-(1020,3)", p2, dir23);
                OTSRoadNode n3 = new OTSRoadNode(this.network, "Node-3-(2000,197)", p3, dir23);
                OTSRoadNode n4 = new OTSRoadNode(this.network, "Node-4-(2020,200)", new OTSPoint3D(2020, 200), Direction.ZERO);
                OTSRoadNode n5 = new OTSRoadNode(this.network, "Node-5-(2200,200)", new OTSPoint3D(2200, 200), Direction.ZERO);
                this.nodes.addAll(Arrays.asList(new OTSRoadNode[] {n0, n1, n2, n3, n4, n5}));

                LaneType laneType = this.network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);

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
                    OTSRoadNode fromNode = this.nodes.get(i - 1);
                    OTSRoadNode toNode = this.nodes.get(i);
                    OTSLine3D line = lines[i - 1];
                    String linkName = fromNode.getId() + "-" + toNode.getId();
                    // LongitudinalDirectionality direction = line.equals(l23) && minus ? LongitudinalDirectionality.DIR_MINUS
                    // : LongitudinalDirectionality.DIR_PLUS;
                    Lane[] lanes = LaneFactory.makeMultiLane(this.network, linkName, fromNode, toNode, line.getPoints(), 1,
                            laneType, this.speedLimit, this.simulator);
                    if (i == this.nodes.size() - 1)
                    {
                        new SinkSensor(lanes[0], new Length(100.0, METER), Compatible.EVERYTHING, this.simulator);
                    }
                    this.path.add(lanes[0]);
                    links.add(lanes[0].getParentLink());
                    if (1 == i)
                    {
                        this.initialLane = lanes[0];
                    }
                }

                this.carProbability = (double) getInputParameter("generic.carProbability");
                this.parametersCar = InputParameterHelper.getParametersCar(getInputParameterMap());
                this.parametersTruck = InputParameterHelper.getParametersTruck(getInputParameterMap());

                this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                        new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
                this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                        new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));

                // 1500 [veh / hour] == 2.4s headway
                this.headway = new Duration(3600.0 / 1500.0, SECOND);

                // Schedule creation of the first car (it will re-schedule itself one headway later, etc.).
                this.simulator.scheduleEventAbsTime(Time.ZERO, this, this, "generateCar", null);
            }
            catch (NamingException | NetworkException | OTSGeometryException | ParameterException | InputParameterException
                    | GTUException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSRoadNetwork getNetwork()
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
            try
            {
                boolean generateTruck = this.stream.nextDouble() > this.carProbability;
                Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
                LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU("" + (++this.carsCreated),
                        this.network.getGtuType(GTUType.DEFAULTS.CAR), vehicleLength, new Length(1.8, METER),
                        new Speed(200, KM_PER_HOUR), vehicleLength.times(0.5), this.simulator, this.network);
                gtu.setParameters(generateTruck ? this.parametersTruck : this.parametersCar);
                gtu.setNoLaneChangeDistance(Length.ZERO);
                gtu.setMaximumAcceleration(Acceleration.instantiateSI(3.0));
                gtu.setMaximumDeceleration(Acceleration.instantiateSI(-8.0));

                // strategical planner
                LaneBasedStrategicalPlanner strategicalPlanner =
                        generateTruck ? this.strategicalPlannerGeneratorTrucks.create(gtu, null, null, null)
                                : this.strategicalPlannerGeneratorCars.create(gtu, null, null, null);

                Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
                Length initialPosition = new Length(20, METER);
                initialPositions.add(new DirectedLanePosition(this.initialLane, initialPosition, GTUDirectionality.DIR_PLUS));
                Speed initialSpeed = new Speed(100.0, KM_PER_HOUR);
                gtu.init(strategicalPlanner, initialPositions, initialSpeed);
                this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
            }
            catch (SimRuntimeException | NetworkException | GTUException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "SequentialModel";
        }

    }
}
