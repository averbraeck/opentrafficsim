package org.opentrafficsim.demo;

import static org.opentrafficsim.core.gtu.GTUType.CAR;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.draw.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.gui.AbstractOTSSwingApplication;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Demonstrate the FundamentalDiagram plot.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagramsLane extends AbstractOTSSwingApplication implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private FundamentalDiagramLanePlotsModel model;

    /** the panel. */
    private OTSAnimationPanel animationPanel;

    /**
     * Create a Straight Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     * @throws OTSDrawingException on animation error
     * @throws OTSSimulationException on graph error
     */
    public FundamentalDiagramsLane(final String title, final OTSAnimationPanel panel,
            final FundamentalDiagramLanePlotsModel model) throws OTSDrawingException, OTSSimulationException
    {
        super(model, panel);
        this.animationPanel = panel;
        this.model = model;
        OTSNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getSimulator());
        AnimationToggles.setTextAnimationTogglesStandard(this.animationPanel);
        addStatisticsTabs(model.getSimulator());
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
            final FundamentalDiagramLanePlotsModel otsModel = new FundamentalDiagramLanePlotsModel(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), otsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, new DefaultSwitchableGTUColorer(), otsModel.getNetwork());
                FundamentalDiagramsLane app = new FundamentalDiagramsLane("FundamentalDiagramsLane", animationPanel, otsModel);
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
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | OTSSimulationException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add the statistics tabs.
     * @param simulator the simulator on which sampling can be scheduled
     * @throws OTSSimulationException on error
     */
    protected final void addStatisticsTabs(final OTSSimulatorInterface simulator) throws OTSSimulationException
    {
        final int panelsPerRow = 3;
        TablePanel charts = new TablePanel(3, panelsPerRow);
        RoadSampler sampler = new RoadSampler(simulator);
        for (int plotNumber = 0; plotNumber < this.model.getNumberOfLanes(); plotNumber++)
        {
            Lane lane = this.model.getLane(plotNumber);
            int xs = (int) lane.getParentLink().getStartNode().getPoint().x;
            int xe = (int) lane.getParentLink().getEndNode().getPoint().x;
            String name = "Fundamental Diagram for [" + xs + ", " + xe + "] m";
            FundamentalDiagram graph;
            try
            {
                graph = new FundamentalDiagram(name, Quantity.DENSITY, Quantity.FLOW, simulator, sampler,
                        GraphLaneUtil.createSingleLanePath(name, new LaneDirection(lane, GTUDirectionality.DIR_PLUS)), false,
                        Duration.createSI(60.0));
            }
            catch (NetworkException exception)
            {
                throw new OTSSimulationException(exception);
            }
            charts.setCell(graph.getContentPane(), plotNumber / panelsPerRow, plotNumber % panelsPerRow);
        }
        this.animationPanel.getTabbedPane().addTab(this.animationPanel.getTabbedPane().getTabCount(), "statistics ", charts);
    }

    /**
     * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s a
     * blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is IDM+
     * <a href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
     * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
     * Output is a set of FundamentalDiagram plots for various point along the lane.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version ug 1, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class FundamentalDiagramLanePlotsModel extends AbstractOTSModel implements UNITS
    {
        /** */
        private static final long serialVersionUID = 20140820L;

        /** The network. */
        private OTSNetwork network = new OTSNetwork("network");

        /** The headway (inter-vehicle time). */
        private Duration headway;

        /** Number of cars created. */
        private int carsCreated = 0;

        /** Type of all GTUs. */
        private GTUType gtuType = CAR;

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

        /** The blocking, implemented as a traffic light. */
        private SimpleTrafficLight block = null;

        /** Starting x-position. */
        private Length startX = new Length(0, METER);

        /** Length per lane. */
        private Length laneLength = new Length(500, METER);

        /** The Lanes containing the simulated Cars. */
        private List<Lane> lanes = new ArrayList<>();

        /** The speed limit. */
        private Speed speedLimit = new Speed(100, KM_PER_HOUR);

        /** The random number generator used to decide what kind of GTU to generate. */
        private StreamInterface stream = new MersenneTwister(12345);

        /**
         * @param simulator the simulator for this model
         */
        FundamentalDiagramLanePlotsModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
            InputParameterHelper.makeInputParameterMapCarTruck(this.inputParameterMap, 1.0);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            try
            {
                LaneType laneType = LaneType.TWO_WAY_LANE;
                OTSNode node = new OTSNode(this.network, "Node 0", new OTSPoint3D(this.startX.getSI(), 0, 0));
                for (int laneNr = 0; laneNr < 10; laneNr++)
                {
                    OTSNode next = new OTSNode(this.network, "Node " + (laneNr + 1),
                            new OTSPoint3D(node.getPoint().x + this.laneLength.si, 0, 0));
                    Lane lane = LaneFactory.makeLane(this.network, "Lane" + laneNr, node, next, null, laneType, this.speedLimit,
                            this.simulator);
                    this.lanes.add(lane);
                    node = next;
                }
                // create SinkLane
                OTSNode end = new OTSNode(this.network, "End", new OTSPoint3D(node.getPoint().x + 50.0, 0, 0));
                CrossSectionLink endLink = LaneFactory.makeLink(this.network, "endLink", node, end, null, this.simulator);
                int last = this.lanes.size() - 1;
                Lane sinkLane = new Lane(endLink, "sinkLane", this.lanes.get(last).getLateralCenterPosition(1.0),
                        this.lanes.get(last).getLateralCenterPosition(1.0), this.lanes.get(last).getWidth(1.0),
                        this.lanes.get(last).getWidth(1.0), laneType, this.speedLimit, new OvertakingConditions.None());
                new SinkSensor(sinkLane, new Length(10.0, METER), this.simulator);

                this.carProbability = (double) getInputParameter("generic.carProbability");
                this.parametersCar = InputParameterHelper.getParametersCar(getInputParameterMap());
                this.parametersTruck = InputParameterHelper.getParametersTruck(getInputParameterMap());

                this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                        new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
                this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                        new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));

                // 1500 [veh / hour] == 2.4s headway
                this.headway = new Duration(3600.0 / 1500.0, SECOND);

                // Schedule creation of the first car (this will re-schedule itself one headway later, etc.).
                this.simulator.scheduleEventAbs(Time.ZERO, this, this, "generateCar", null);

                // Set up the block in the last lane of the list.
                this.block = new SimpleTrafficLight(this.lanes.get(this.lanes.size() - 1).getId() + "_TL",
                        this.lanes.get(this.lanes.size() - 1), new Length(200, METER), this.simulator);
                this.block.setTrafficLightColor(TrafficLightColor.GREEN);

                // Create a block at t = 5 minutes
                this.simulator.scheduleEventAbs(new Time(1000, TimeUnit.BASE_SECOND), this, this, "createBlock", null);
                // Remove the block at t = 7 minutes
                this.simulator.scheduleEventAbs(new Time(1200, TimeUnit.BASE_SECOND), this, this, "removeBlock", null);
            }
            catch (SimRuntimeException | NetworkException | GTUException | OTSGeometryException | ParameterException
                    | InputParameterException exception)
            {
                exception.printStackTrace();
            }
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
                LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, vehicleLength,
                        new Length(1.8, METER), new Speed(200, KM_PER_HOUR), vehicleLength.multiplyBy(0.5), this.simulator,
                        this.network);
                gtu.setParameters(generateTruck ? this.parametersTruck : this.parametersCar);
                gtu.setNoLaneChangeDistance(Length.ZERO);
                gtu.setMaximumAcceleration(Acceleration.createSI(3.0));
                gtu.setMaximumDeceleration(Acceleration.createSI(-8.0));

                // strategical planner
                LaneBasedStrategicalPlanner strategicalPlanner =
                        generateTruck ? this.strategicalPlannerGeneratorTrucks.create(gtu, null, null, null)
                                : this.strategicalPlannerGeneratorCars.create(gtu, null, null, null);

                Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
                Length initialPosition = new Length(20, METER);
                initialPositions.add(new DirectedLanePosition(this.lanes.get(0), initialPosition, GTUDirectionality.DIR_PLUS));
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
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

        /**
         * @param laneNr int; the lane in the list.
         * @return lane.
         */
        public Lane getLane(final int laneNr)
        {
            return this.lanes.get(laneNr);
        }

        /**
         * @return int; nubmer of lanes
         */
        public int getNumberOfLanes()
        {
            return this.lanes.size();
        }
    }
}
