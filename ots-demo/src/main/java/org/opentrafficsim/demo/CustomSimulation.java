package org.opentrafficsim.demo;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DSOLException;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.*;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplate;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplateDistribution;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.*;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import javax.naming.NamingException;
import java.awt.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.djunits.unit.LengthUnit.METER;

public class CustomSimulation extends OtsSimulationApplication<CustomSimulation.CustomModel>{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a T-Junction demo.
     * @param title String; the title of the Frame
     * @param panel OtsAnimationPanel; the tabbed panel to display
     * @param model TJunctionModel; the model
     * @throws OtsDrawingException on animation error
     */
    public CustomSimulation(final String title, final OtsAnimationPanel panel, final CustomModel model)
            throws OtsDrawingException
    {
        super(model, panel);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose, OtsAnimator simulator, CustomModel model)
    {
        try
        {
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(model.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, model, DEFAULT_COLORER, model.getNetwork());
            CustomSimulation app = new CustomSimulation("T-Junction demo", animationPanel, model);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | OtsDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class CustomModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private RoadNetwork network;

        /**
         * @param simulator OtsSimulatorInterface; the simulator for this model
         */
        public CustomModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /**
         * @param network the network
         */
        public void setNetwork(RoadNetwork network){
            this.network = network;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException {
            try {
                StreamInterface stream = new MersenneTwister(12345);

                Node from = this.network.getNode("14-Start");
                Node to = this.network.getNode("7-End");

                CrossSectionLink link_start = (CrossSectionLink) this.network.getLink("14");
                Lane lane_start = (Lane) link_start.getCrossSectionElement("10");
                CrossSectionLink link_end = (CrossSectionLink) this.network.getLink("7");
                Lane lane_end = (Lane) link_end.getCrossSectionElement("115");
                new SinkDetector(lane_end, lane_end.getLength().minus(Length.instantiateSI(2)), this.simulator, DefaultsRoadNl.ROAD_USERS);

                Route route = this.network.getShortestRouteBetween(DefaultsNl.CAR, from, to);
                FixedRouteGenerator routeGenerator = new FixedRouteGenerator(route);

                CarFollowingModelFactory<IdmPlus> idmPlusFactory =
                        new IdmPlusFactory(stream);
                LaneBasedTacticalPlannerFactory<Lmrs> tacticalFactory =
                        new LmrsFactory(idmPlusFactory, new DefaultLmrsPerceptionFactory());
                LaneBasedStrategicalPlannerFactory<?> strategicalFactory =
                        new LaneBasedStrategicalRoutePlannerFactory(tacticalFactory);

                Distribution<LaneBasedGtuTemplate> gtuTypeDistribution =
                        new Distribution<>(stream);

                Generator<Length> lengthGenerator = new ConstantGenerator(new Length(4, METER));
                Generator<Length> widthGenerator = new ConstantGenerator(new Length(2, METER));
                Generator<Speed> maximumSpeedGenerator = new ConstantGenerator(new Speed(50, SpeedUnit.KM_PER_HOUR));


                LaneBasedGtuTemplate templateGtuType = new LaneBasedGtuTemplate(DefaultsNl.CAR, lengthGenerator, widthGenerator,
                        maximumSpeedGenerator, strategicalFactory, routeGenerator);
                gtuTypeDistribution.add(new Distribution.FrequencyAndObject<>(1.0, templateGtuType));

                TtcRoomChecker roomChecker = new TtcRoomChecker(new Duration(10.0, DurationUnit.SI));

                Generator<Duration> headwayGenerator = new ConstantGenerator(Duration.instantiateSI(5));

                Length position = Length.instantiateSI(5);
                Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
                initialLongitudinalPositions.add(new LanePosition(lane_start, position));

                IdGenerator idGenerator = new IdGenerator("");

                LaneBasedGtuTemplateDistribution characteristicsGenerator =
                        new LaneBasedGtuTemplateDistribution(gtuTypeDistribution);
                new LaneBasedGtuGenerator(lane_start.getFullId(), headwayGenerator, characteristicsGenerator,
                        GeneratorPositions.create(initialLongitudinalPositions, stream), this.network, simulator, roomChecker, idGenerator);
            }catch (Exception e){
                System.err.println("Error while creating GTUs");
            }
        }

        /** {@inheritDoc} */
        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }

    }
}

