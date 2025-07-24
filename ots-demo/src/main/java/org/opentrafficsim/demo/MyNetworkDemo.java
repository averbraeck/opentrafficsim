package org.opentrafficsim.demo;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DsolException;
import org.djunits.value.vdouble.scalar.*;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.demo.MyNetworkDemo.MyNetworkDemoModel;
import org.opentrafficsim.draw.OtsDrawingException;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.*;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.djutils.event.EventListener;

import javax.naming.NamingException;
import java.awt.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.Instant;

import static org.djunits.unit.LengthUnit.METER;
import static org.djunits.unit.SpeedUnit.KM_PER_HOUR;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MyNetworkDemo extends OtsSimulationApplication<MyNetworkDemoModel>
{
    /** */
    private static final long serialVersionUID = 20170407L;

    /** Simulation time. */
    public static final Time SIMTIME = Time.instantiateSI(3600);

    /**
     * Create a MyNetworkDemo Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public MyNetworkDemo(final String title, final OtsAnimationPanel panel, final MyNetworkDemoModel model)
            throws OtsDrawingException
    {
        super(model, panel);
    }

    @Override
    protected void setAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesFull(getAnimationPanel());
        getAnimationPanel().getAnimationPanel().toggleClass(Link.class);
        getAnimationPanel().getAnimationPanel().toggleClass(Node.class);
        getAnimationPanel().getAnimationPanel().showClass(SpeedSign.class);
    }

    @Override
    protected void addTabs()
    {
        GraphPath<LaneDataRoad> path;
//        try
//        {
//            Lane start = ((CrossSectionLink) getModel().getNetwork().getLink("AB")).getLanes().get(1);
//            path = GraphLaneUtil.createPath("Right lane", start);
//        }
//        catch (NetworkException exception)
//        {
//            throw new RuntimeException("Could not create a path as a lane has no set speed limit.", exception);
//        }
//        RoadSampler sampler = new RoadSampler(getModel().getNetwork());
//        GraphPath.initRecording(sampler, path);
//        PlotScheduler scheduler = new OtsPlotScheduler(getModel().getSimulator());
//        Duration updateInterval = Duration.instantiateSI(10.0);
//        SwingPlot plot = new SwingTrajectoryPlot(
//                new TrajectoryPlot("Trajectory right lane", updateInterval, scheduler, sampler.getSamplerData(), path));
//        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "Trajectories",
//                plot.getContentPane());
    }

    /**
     * Main program.
     * @param args the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("MyNetworkDemo");
            final MyNetworkDemoModel otsModel = new MyNetworkDemoModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(otsModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, otsModel, DEFAULT_COLORER, otsModel.getNetwork());
            MyNetworkDemo app = new MyNetworkDemo("MyNetworkDemo", animationPanel, otsModel);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException
               | IndexOutOfBoundsException | DsolException | OtsDrawingException exception)
        {
            exception.printStackTrace();
        }
    }


    public static class MyNetworkDemoModel extends AbstractOtsModel implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20170407L;
        /** Number of cars created. */
        private int carsCreated = 0;
        /** The network. */
        private RoadNetwork network;
        /** The random number generator used to decide what kind of GTU to generate etc. */
        private StreamInterface stream = new MersenneTwister(12345);
        /** Strategical planner generator for cars. */
        private LaneBasedStrategicalPlannerFactory<?> strategicalPlannerGeneratorCars = null;

        /**
         * Constructor.
         * @param simulator the simulator
         */
        public MyNetworkDemoModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /**
         * Set network.
         * @param network set network.
         */
        public void setNetwork(final RoadNetwork network)
        {
            this.network = network;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                        new LmrsFactory(new IdmPlusFactory(this.stream), new DefaultLmrsPerceptionFactory()));
                URL xmlURL = URLResource.getResource("/resources/MotorwayExit.xml");
                this.network = new RoadNetwork("MyNetworkDemo", getSimulator());
                new XmlParser(this.network).setUrl(xmlURL).build();
                System.out.println("Network created");

                LaneBasedGtu gtu = generateGTU(new Length(5, METER), "l191", 200);

                gtu.addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
//                generateGTU(new Length(1, METER), "cp1-lane1", 200);
//                generateGTU(new Length(1, METER), "cp4-lane1", 0);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        public void notify(final org.djutils.event.Event event) throws RemoteException
        {
            if (event.getType().equals(LaneBasedGtu.LANEBASED_MOVE_EVENT))
            {
                // Payload: [String gtuId, PositionVector currentPosition, Direction currentDirection, Speed speed, Acceleration
                // acceleration, TurnIndicatorStatus turnIndicatorStatus, Length odometer, Link id of referenceLane, Lane id of
                // referenceLane, Length positionOnReferenceLane]
                Object[] payload = (Object[]) event.getContent();
                CrossSectionLink link = (CrossSectionLink) this.network.getLink(payload[7].toString());
                Lane lane = (Lane) link.getCrossSectionElement(payload[8].toString());
                LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU(payload[0].toString());
                LaneDataRoad laneData = new LaneDataRoad(lane);
                gtu.isBrakingLightsOn();
                Instant timestamp = Instant.now();
                System.out.println("Current timestamp: " + timestamp);
                System.out.println("Direction: " + payload[2]);
            }
        }

        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }

        private Lane getLane(final CrossSectionLink link, final String id)
        {
            return (Lane) link.getCrossSectionElement(id);
        }

        protected final LaneBasedGtu generateGTU(Length initialPosition, String lane_id, int maxSpeed)
                throws GtuException, NetworkException, SimRuntimeException, InputParameterException
        {
            CrossSectionLink link = (CrossSectionLink) this.network.getLink(lane_id);
            Lane lane = link.getLanes().get(0);
            // GTU itself
            Length vehicleLength = new Length(4, METER);
            LaneBasedGtu gtu = new LaneBasedGtu("" + (++this.carsCreated), DefaultsNl.CAR, vehicleLength, new Length(1.8, METER),
                    new Speed(maxSpeed, KM_PER_HOUR), vehicleLength.times(0.5), this.network);
            gtu.setNoLaneChangeDistance(Length.ZERO);
            gtu.setInstantaneousLaneChange(false);
            gtu.setMaximumAcceleration(Acceleration.instantiateSI(3.0));
            gtu.setMaximumDeceleration(Acceleration.instantiateSI(-8.0));

            // strategical planner
            LaneBasedStrategicalPlanner strategicalPlanner;
            Route route = null;
            Node start = this.network.getNode("l109-0");
            Node end = this.network.getNode("l52-1");
            strategicalPlanner = this.strategicalPlannerGeneratorCars.create(gtu, route, start, end);

            // init
            Speed initialSpeed = new Speed(0, KM_PER_HOUR);
            try {
                gtu.init(strategicalPlanner, new LanePosition(lane, initialPosition), initialSpeed);
            } catch (OtsGeometryException e) {
                throw new RuntimeException(e);
            }
            return gtu;
        }
    }

}
