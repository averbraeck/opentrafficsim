package org.opentrafficsim.road.network.factory.rti.test;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gis.CoordinateTransformLonLatToXY;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.opendrive.old.LaneAnimationOD;
import org.opentrafficsim.road.network.factory.opendrive.old.OpenDriveNetworkLaneParserOld;
import org.opentrafficsim.road.network.factory.rti.communication.ReceiverThread;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestOpenDriveParserNoRTINew extends OTSSimulationApplication<OTSModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestOpenDriveParserNoRTINew(final OTSModelInterface model, final OTSAnimationPanel animationPanel)
            throws OTSDrawingException
    {
        super(model, animationPanel);
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
                    OTSAnimator simulator = new OTSAnimator();
                    TestOpenDriveModel openDriveModel = new TestOpenDriveModel(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), openDriveModel);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(openDriveModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, openDriveModel, DEFAULT_COLORER, openDriveModel.getNetwork());
                    new TestOpenDriveParserNoRTINew(openDriveModel, animationPanel);
                }
                catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TestOpenDriveParserNoRTINew []";
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class TestOpenDriveModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20150811L;

        /** the network. */
        private OTSRoadNetwork network;

        private List<LaneBasedIndividualGTU> rtiCars;

        /** */
        private ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist;

        /** */
        private ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> iatDist;

        /** */
        private ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist;

        /** */
        private ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist;

        /** */
        private ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maxSpeedDist;

        /** */
        private ContinuousDistDoubleScalar.Rel<Length, LengthUnit> initialPosDist;

        /** */
        private GTUType carType;

        /** Last id */
        private int lastId = 0;

        /** */
        private StreamInterface stream;

        /**
         * @param simulator the simulator
         */
        TestOpenDriveModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
            this.stream = new MersenneTwister(1);
            this.initialSpeedDist = new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 0.0), SpeedUnit.SI);
            this.iatDist = new ContinuousDistDoubleScalar.Rel<>(new DistExponential(this.stream, 30.0), DurationUnit.SECOND);
            this.lengthDist = new ContinuousDistDoubleScalar.Rel<>(new DistUniform(this.stream, 4.0, 5.0), LengthUnit.METER);
            this.widthDist = new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 2.0), LengthUnit.METER);
            this.maxSpeedDist = new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(this.stream, 30.0, 35.0, 40.0),
                    SpeedUnit.MILE_PER_HOUR);
            this.initialPosDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(this.stream, 0.0, 1.0), LengthUnit.METER);
            this.carType = this.network.getGtuType(GTUType.DEFAULTS.CAR);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            this.rtiCars = new ArrayList<LaneBasedIndividualGTU>();

            // URL url = URLResource.getResource("/NASAames.xodr");
            // URL url = URLResource.getResource("/testod.xodr");
            URL url = URLResource.getResource("/OpenDrive.xodr");
            this.simulator.setPauseOnError(false);
            OpenDriveNetworkLaneParserOld nlp = new OpenDriveNetworkLaneParserOld(this.simulator);
            this.network = null;
            try
            {
                this.network = nlp.build(url);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

            URL gisURL = URLResource.getResource("/gis/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());

            double latCenter = nlp.getHeaderTag().getOriginLat().si, lonCenter = nlp.getHeaderTag().getOriginLong().si;

            CoordinateTransform latLonToXY = new CoordinateTransformLonLatToXY(lonCenter, latCenter);
            new GisRenderable2D(this.simulator, gisURL, latLonToXY);

            // new ReceiverThread(this.simulator).run();

            /** Repair the network... */
            try
            {
                destroyLink(nlp, this.network, "3766109");
                destroyLink(nlp, this.network, "3766110");
                destroyLink(nlp, this.network, "3766111");

                Lane lane109 = makeLane(this.network, "3766068.1", "3766068.0", "3", "3766059.7", "3766059.150", "2", "3766109",
                        "-1", network.getLinkType(LinkType.DEFAULTS.ROAD), LaneKeepingPolicy.KEEPLANE);
                Renderable2D animation109 = new LaneAnimationOD(lane109, this.simulator, Color.gray);
                nlp.animationMap.put(lane109, animation109);

                Lane lane110 = makeLane(this.network, "3766068.1", "3766068.0", "4", "3766059.7", "3766059.150", "3", "3766110",
                        "-1", network.getLinkType(LinkType.DEFAULTS.ROAD), LaneKeepingPolicy.KEEPLANE);
                Renderable2D animation110 = new LaneAnimationOD(lane110, this.simulator, Color.gray);
                nlp.animationMap.put(lane110, animation110);

                Lane lane111 = makeLane(this.network, "3766068.1", "3766068.0", "5", "3766059.7", "3766059.150", "4", "3766111",
                        "-1", network.getLinkType(LinkType.DEFAULTS.ROAD), LaneKeepingPolicy.KEEPLANE);
                Renderable2D animation111 = new LaneAnimationOD(lane111, this.simulator, Color.gray);
                nlp.animationMap.put(lane111, animation111);

                destroyLink(nlp, this.network, "3766175");
                destroyLink(nlp, this.network, "3766176");
                destroyLink(nlp, this.network, "3766177");

                Lane lane175 = makeLane(this.network, "3766059.1", "3766059.0", "3", "3766054.5", "3766054.191", "2", "3766175",
                        "-1", network.getLinkType(LinkType.DEFAULTS.ROAD), LaneKeepingPolicy.KEEPLANE);
                Renderable2D animation175 = new LaneAnimationOD(lane175, this.simulator, Color.gray);
                nlp.animationMap.put(lane175, animation175);

                Lane lane176 = makeLane(this.network, "3766059.1", "3766059.0", "4", "3766054.5", "3766054.191", "3", "3766176",
                        "-1", network.getLinkType(LinkType.DEFAULTS.ROAD), LaneKeepingPolicy.KEEPLANE);
                Renderable2D animation176 = new LaneAnimationOD(lane176, this.simulator, Color.gray);
                nlp.animationMap.put(lane176, animation176);

                Lane lane177 = makeLane(this.network, "3766059.1", "3766059.0", "5", "3766054.5", "3766054.191", "4", "3766177",
                        "-1", network.getLinkType(LinkType.DEFAULTS.ROAD), LaneKeepingPolicy.KEEPLANE);
                Renderable2D animation177 = new LaneAnimationOD(lane177, this.simulator, Color.gray);
                nlp.animationMap.put(lane177, animation177);

                Lane lane191x = makeLane(this.network, "3766054.5", "3766054.191", "-6", "3766059.1", "3766059.0", "-4",
                        "3766191x", "-1", network.getLinkType(LinkType.DEFAULTS.ROAD), LaneKeepingPolicy.KEEPLANE);
                Renderable2D animation191x = new LaneAnimationOD(lane191x, this.simulator, Color.gray);
                nlp.animationMap.put(lane191x, animation191x);

            }
            catch (OTSGeometryException | NetworkException | NamingException | RemoteException e)
            {
                System.out.println("Repair network: " + e.getMessage());
            }

            /*-
            CompleteRoute cr1 = null, cr2 = null, cr3 = null, cr4 = null, cr5 = null, cr6 = null;
            
            List<Node> nodesVia1 = new ArrayList<Node>();
            nodesVia1.add(link2.getStartNode());
            nodesVia1.add(link3.getEndNode());
            nodesVia1.add(link4.getStartNode());
            nodesVia1.add(link5.getEndNode());
            nodesVia1.add(link7.getEndNode());
            nodesVia1.add(link8.getStartNode());
            try
            {
                cr1 =
                    network.getShortestRouteBetween(GTUType.ALL, link1.getStartNode(), link1.getStartNode(), nodesVia1);
                Collections.reverse(nodesVia1);
                cr2 =
                    network.getShortestRouteBetween(GTUType.ALL, link1.getStartNode(), link1.getStartNode(), nodesVia1);
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }
            
            List<Node> nodesVia2 = new ArrayList<Node>();
            nodesVia2.add(link3.getEndNode());
            nodesVia2.add(link5.getEndNode());
            try
            {
                cr3 =
                    network.getShortestRouteBetween(GTUType.ALL, link3.getStartNode(), link3.getStartNode(), nodesVia2);
                Collections.reverse(nodesVia2);
                cr4 =
                    network.getShortestRouteBetween(GTUType.ALL, link3.getStartNode(), link3.getStartNode(), nodesVia2);
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }
            
            List<Node> nodesVia3 = new ArrayList<Node>();
            nodesVia3.add(link7.getEndNode());
            nodesVia3.add(link8.getEndNode());
            try
            {
                cr5 =
                    network.getShortestRouteBetween(GTUType.ALL, link6.getStartNode(), link6.getStartNode(), nodesVia3);
                Collections.reverse(nodesVia3);
                cr6 =
                    network.getShortestRouteBetween(GTUType.ALL, link6.getStartNode(), link6.getStartNode(), nodesVia3);
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }
             */

            // build the graph
            this.network.buildGraph(this.carType);

            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld()));

            // generate 2 GTUs on 70.1 (parking lot NRC SV)
            try
            {
                CrossSectionLink link70_1 = (CrossSectionLink) this.network.getLink("3766070.1");
                Lane lane70_1m1 = getLane(link70_1, "-1"); // get the rightmost lane (-1) to drive north
                Lane lane70_1p1 = getLane(link70_1, "1"); // get the rightmost lane (1) to drive south

                // create a route around NRC SV
                Node n70_0 = this.network.getNode("3766070.0");
                Node n70_23 = this.network.getNode("3766070.23");
                Node n45_0 = this.network.getNode("3766045.0");
                Node n45_22 = this.network.getNode("3766045.22");
                Node n65_0 = this.network.getNode("3766065.0");
                Node n65_8 = this.network.getNode("3766065.8");
                Route route7070r = new CompleteRoute("Right around NRC-SV", this.carType,
                        this.network
                                .getShortestRouteBetween(this.carType, n70_0, n70_0, Arrays.asList(new Node[] {n45_0, n65_0}))
                                .getNodes());
                putCar(lane70_1m1, route7070r, this.network, GTUDirectionality.DIR_PLUS, strategicalPlannerFactory);
                Route route7070l = new CompleteRoute("Left around NRC-SV", this.carType, this.network
                        .getShortestRouteBetween(this.carType, n70_0, n70_0, Arrays.asList(new Node[] {n65_8, n45_22, n70_23}))
                        .getNodes());
                putCar(lane70_1p1, route7070l, this.network, GTUDirectionality.DIR_MINUS, strategicalPlannerFactory);
            }
            catch (NetworkException | GTUException | NamingException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

            // generate 10 GTUs on 68 driving the upper circle.
            try
            {
                List<CrossSectionLink> links68 = new ArrayList<>();
                links68.add((CrossSectionLink) this.network.getLink("3766068.1"));
                links68.add((CrossSectionLink) this.network.getLink("3766068.2"));
                links68.add((CrossSectionLink) this.network.getLink("3766068.3"));
                links68.add((CrossSectionLink) this.network.getLink("3766068.4"));
                links68.add((CrossSectionLink) this.network.getLink("3766068.5"));

                // create a right route for top circle
                Node n68_0 = this.network.getNode("3766068.0");
                Node n38_88 = this.network.getNode("3766038.88");
                Node n43_0 = this.network.getNode("3766043.0");
                Node n45_0 = this.network.getNode("3766045.0");
                Node n65_0 = this.network.getNode("3766065.0");
                Node n64_17 = this.network.getNode("3766064.17");
                Route route6868r =
                        new CompleteRoute("Right top", this.carType, this.network.getShortestRouteBetween(this.carType, n68_0,
                                n68_0, Arrays.asList(new Node[] {n38_88, n43_0, n45_0, n65_0, n64_17})).getNodes());
                System.out.println(route6868r);
                for (int i = 0; i < 5; i++)
                {
                    putCar(randLane(links68, LongitudinalDirectionality.DIR_PLUS), route6868r, this.network,
                            GTUDirectionality.DIR_PLUS, strategicalPlannerFactory);
                }

                // create a left route for top circle
                Node n38_0 = this.network.getNode("3766038.0");
                Node n43_53 = this.network.getNode("3766043.53");
                Node n45_22 = this.network.getNode("3766045.22");
                Node n65_8 = this.network.getNode("3766065.8");
                Node n64_0 = this.network.getNode("3766064.0");
                Node n68_158 = this.network.getNode("3766068.158");
                Route route6868l =
                        new CompleteRoute("Right top", this.carType, this.network.getShortestRouteBetween(this.carType, n68_158,
                                n68_158, Arrays.asList(new Node[] {n64_0, n65_8, n45_22, n43_53, n38_0})).getNodes());
                System.out.println(route6868l);
                for (int i = 0; i < 5; i++)
                {
                    putCar(randLane(links68, LongitudinalDirectionality.DIR_MINUS), route6868l, this.network,
                            GTUDirectionality.DIR_MINUS, strategicalPlannerFactory);
                }

            }
            catch (NetworkException | GTUException | NamingException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

            // XXX dirty hack...
            while (this.rtiCars.size() < 52)
            {
                this.rtiCars.add(this.rtiCars.get(this.rtiCars.size() - 1));

            }
            /*-
            for (int i = 0; i < 10; i++)
            {
                Lane lane = null;
                GTUDirectionality dir = GTUDirectionality.DIR_PLUS;
                for (CrossSectionElement cse : link1.getCrossSectionElementList())
                {
                    if (cse instanceof Lane && !(cse instanceof NoTrafficLane))
                    {
                        lane = (Lane) cse;
                        dir =
                            lane.getDirectionality(carType).isForwardOrBoth() ? GTUDirectionality.DIR_PLUS
                                : GTUDirectionality.DIR_MINUS;
                        break;
                    }
                }
                // int i = 1;
                LaneBasedDrivingCharacteristics drivingCharacteristics =
                    new LaneBasedDrivingCharacteristics(new IDMPlus(), new Altruistic());
                LaneBasedStrategicalPlanner sPlanner =
                    new LaneBasedStrategicalRoutePlanner(drivingCharacteristics,
                        new LaneBasedGTUFollowingLaneChangeTacticalPlanner(), cr2);
            
                System.out.println("Car " + i + " - generated on lane " + lane + " with sn="
                    + lane.getParentLink().getStartNode() + " and en=" + lane.getParentLink().getEndNode()
                    + ", route = " + cr2);
            
                LanePerceptionFull perception = new LanePerceptionFull();
                DirectedLanePosition directedLanePosition =
                    new DirectedLanePosition(lane,
                        initialPosDist.draw().times(lane.getCenterLine().getLengthSI()), dir);
                Set<DirectedLanePosition> lanepositionSet = new LinkedHashSet<DirectedLanePosition>();
                lanepositionSet.add(directedLanePosition);
                Length carLength = lengthDist.draw();
            
                try
                {
                    LaneBasedIndividualCar car =
                        new LaneBasedIndividualCar(String.valueOf(i), carType, lanepositionSet, new Speed(0.0,
                            SpeedUnit.METER_PER_SECOND), carLength, widthDist.draw(), maxSpeedDist.draw(),
                            this.simulator, sPlanner, perception, network);
                    this.rtiCars.add(car);
            
                }
                catch (NamingException | NetworkException | GTUException | OTSGeometryException exception)
                {
                    exception.printStackTrace();
                }
            }
             */

            /*-
            List<CompleteRoute> cRoutes = new ArrayList<>();
            cRoutes.add(cr1);
            cRoutes.add(cr2);
            cRoutes.add(cr3);
            cRoutes.add(cr4);
            cRoutes.add(cr5);
            cRoutes.add(cr6);
            Random routeRandom = new Random();
            
            List<CrossSectionLink> links = new ArrayList<>();
            links.add(link1);
            links.add(link2);
            links.add(link3);
            links.add(link4);
            links.add(link5);
            links.add(link6);
            links.add(link7);
            links.add(link8);
            
            for (int i = 0; i < 52; i++)
            {
                CompleteRoute cr = cRoutes.get(routeRandom.nextInt(6));
            
                CrossSectionLink link;
                while (true)
                {
                    link = links.get(routeRandom.nextInt(8));
                    if (cr.getNodes().contains(link.getStartNode()))
                        break;
                }
            
                GTUDirectionality dir = GTUDirectionality.DIR_PLUS;
                Lane lane = null;
            
                while (true)
                {
                    CrossSectionElement cse =
                        link.getCrossSectionElementList().get(
                            routeRandom.nextInt(link.getCrossSectionElementList().size()));
                    if (cse instanceof Lane && !(cse instanceof NoTrafficLane))
                    {
                        lane = (Lane) cse;
                        break;
            
                    }
                }
            
                if (lane.getDirectionality(carType).equals(LongitudinalDirectionality.DIR_MINUS))
                {
                    dir = GTUDirectionality.DIR_MINUS;
                }
            
                LaneBasedBehavioralCharacteristics drivingCharacteristics =
                    new LaneBasedBehavioralCharacteristics(new IDMPlusOld(), new Altruistic());
                LaneBasedStrategicalPlanner sPlanner =
                    new LaneBasedStrategicalRoutePlanner(drivingCharacteristics,
                        new LaneBasedGTUFollowingLaneChangeTacticalPlanner(), cRoutes.get(routeRandom.nextInt(6)));
                LanePerceptionFull perception = new LanePerceptionFull();
            
                DirectedLanePosition directedLanePosition = null;
                try
                {
                    directedLanePosition =
                        new DirectedLanePosition(lane, initialPosDist.draw().times(
                            lane.getCenterLine().getLengthSI()), dir);
                }
                catch (GTUException exception1)
                {
                    exception1.printStackTrace();
                }
                Set<DirectedLanePosition> lanepositionSet = new LinkedHashSet<DirectedLanePosition>();
                lanepositionSet.add(directedLanePosition);
            
                Length carLength = lengthDist.draw();
                double genPosSI = directedLanePosition.getPosition().getSI();
                double lengthSI = lane.getLength().getSI();
                double frontNew = (genPosSI + carLength.getSI()) / lengthSI;
                double rearNew = genPosSI / lengthSI;
            
                boolean isEnoughSpace = true;
            
                for (LaneBasedGTU gtu : lane.getGtuList())
                {
                    double frontGTU = 0;
                    try
                    {
                        frontGTU = gtu.fractionalPosition(lane, gtu.getFront());
                    }
                    catch (GTUException exception)
                    {
                        exception.printStackTrace();
                    }
                    double rearGTU = 0;
                    try
                    {
                        rearGTU = gtu.fractionalPosition(lane, gtu.getRear());
                    }
                    catch (GTUException exception)
                    {
                        exception.printStackTrace();
                    }
                    if ((frontNew >= rearGTU && frontNew <= frontGTU) || (rearNew >= rearGTU && rearNew <= frontGTU)
                        || (frontGTU >= rearNew && frontGTU <= frontNew) || (rearGTU >= rearNew && rearGTU <= frontNew))
                        isEnoughSpace = false;
                }
            
                if (isEnoughSpace)
                {
                    try
                    {
                        LaneBasedIndividualGTU car =
                            new LaneBasedIndividualGTU(String.valueOf(i), carType, lanepositionSet, new Speed(0.0,
                                SpeedUnit.METER_PER_SECOND), carLength, widthDist.draw(), maxSpeedDist.draw(),
                                this.simulator, sPlanner, perception, network);
                        this.rtiCars.add(car);
            
                    }
                    catch (NamingException | NetworkException | GTUException | OTSGeometryException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else
                {
                    i = i - 1;
                }
            
            }
             */

            try
            {
                new Thread(new ReceiverThread(this.simulator, this.carType, this.rtiCars, this.network)).start();
            }
            catch (SocketException exception1)
            {
                exception1.printStackTrace();
            }

        }

        private final Lane randLane(final List<CrossSectionLink> links, final LongitudinalDirectionality dir)
        {
            // choose a random link with a random lane on that link in the right direction
            CrossSectionLink link = links.get(this.stream.nextInt(0, links.size() - 1));
            List<Lane> lanes = new ArrayList<>();
            for (CrossSectionElement cse : link.getCrossSectionElementList())
            {
                if (cse instanceof Lane)
                {
                    Lane lane = (Lane) cse;
                    if (lane.getLaneType().getDirectionality(this.carType).equals(dir)
                            || lane.getLaneType().getDirectionality(this.carType).isBoth())
                    {
                        lanes.add(lane);
                    }
                }
            }
            for (int i = 0; i < 10; i++)
            {
                Lane lane = lanes.get(this.stream.nextInt(0, lanes.size() - 1));
                if (lane.getGtuList().isEmpty())
                {
                    return lane;
                }
            }
            return lanes.get(this.stream.nextInt(0, lanes.size() - 1));
        }

        private final void putCar(final Lane lane, final Route route, final OTSRoadNetwork network, final GTUDirectionality dir,
                final LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> laneBasedStrategicalPlannerFactory)
                throws GTUException, NamingException, NetworkException, SimRuntimeException, OTSGeometryException
        {
            DirectedLanePosition directedLanePosition = new DirectedLanePosition(lane,
                    this.initialPosDist.draw().times(lane.getCenterLine().getLengthSI()), dir);
            Set<DirectedLanePosition> lanepositionSet = new LinkedHashSet<DirectedLanePosition>();
            lanepositionSet.add(directedLanePosition);
            Length carLength = this.lengthDist.draw();
            LaneBasedIndividualGTU car = new LaneBasedIndividualGTU("" + (++this.lastId), this.carType, carLength,
                    this.widthDist.draw(), this.maxSpeedDist.draw(), carLength.times(0.5), this.simulator, network);
            car.init(laneBasedStrategicalPlannerFactory.create(car, route, null, null), lanepositionSet, Speed.ZERO);
            this.rtiCars.add(car);
        }

        private final Lane getLane(final CrossSectionLink link, final String id) throws NetworkException
        {
            for (CrossSectionElement cse : link.getCrossSectionElementList())
            {
                if (cse instanceof Lane && !(cse instanceof NoTrafficLane) && cse.getId().equals(id))
                {
                    return (Lane) cse;
                }
            }
            throw new NetworkException("Could not find Lane " + id + " in link " + link);
        }

        /**
         * Destroy the animation of the link and underlying cross section elements.
         * @param nlp OpenDriveNetworkLaneParser; the parser with the animation map
         * @param network OTSRoadNetwork; the network in which the link is registered
         * @param linkId String; the link to destroy
         * @throws NamingException in case destroying fails
         * @throws NetworkException in case link cannot be found in the network
         */
        private void destroyLink(final OpenDriveNetworkLaneParserOld nlp, final OTSRoadNetwork network, final String linkId)
                throws NamingException, NetworkException
        {
            try
            {
                Link link = network.getLink(linkId);
                link.getStartNode().removeLink(link);
                link.getEndNode().removeLink(link);
                network.removeLink(link);
                if (link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement cse : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (nlp.animationMap.containsKey(cse))
                        {
                            nlp.animationMap.get(cse).destroy();
                        }
                    }
                }
                if (nlp.animationMap.containsKey(link))
                {
                    nlp.animationMap.get(link).destroy();
                }
            }
            catch (Exception e)
            {
                System.err.println("destroyLink: " + e.getMessage());
            }
        }

        /**
         * Create an extra link to "repair" the network.
         * @param network OTSRoadNetwork; network
         * @param sLinkStr String; start link id
         * @param sNodeStr String; start node id
         * @param sLaneStr String; start lane id
         * @param eLinkStr String; end link id
         * @param eNodeStr String; end node id
         * @param eLaneStr String; end lane id
         * @param linkId String; the id of the new link
         * @param laneId String; the id of the new lane
         * @param linkType LinkType; the type of the new link
         * @param laneKeepingPolicy LaneKeepingPolicy; the lane keeping policy of the new link
         * @return the created lane
         * @throws OTSGeometryException when points cannot be found or line cannot be constructed
         * @throws NetworkException when lane cannot be constructed
         */
        private Lane makeLane(final OTSRoadNetwork network, final String sLinkStr, final String sNodeStr, final String sLaneStr,
                final String eLinkStr, final String eNodeStr, final String eLaneStr, final String linkId, final String laneId,
                final LinkType linkType, final LaneKeepingPolicy laneKeepingPolicy)
                throws OTSGeometryException, NetworkException
        {
            try
            {
                CrossSectionLink sLink = (CrossSectionLink) network.getLink(sLinkStr);
                OTSRoadNode sNode = (OTSRoadNode) network.getNode(sNodeStr);
                Lane sLane = (Lane) sLink.getCrossSectionElement(sLaneStr);
                CrossSectionLink eLink = (CrossSectionLink) network.getLink(eLinkStr);
                OTSRoadNode eNode = (OTSRoadNode) network.getNode(eNodeStr);
                Lane eLane = (Lane) eLink.getCrossSectionElement(eLaneStr);
                DirectedPoint sp, ep;
                Length beginWidth, endWidth;
                if (sLink.getStartNode().equals(sNode))
                {
                    OTSPoint3D p1 = sLane.getCenterLine().get(1);
                    OTSPoint3D p2 = sLane.getCenterLine().get(0);
                    sp = new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
                    beginWidth = sLane.getBeginWidth();
                }
                else
                {
                    OTSPoint3D p1 = sLane.getCenterLine().get(sLane.getCenterLine().size() - 2);
                    OTSPoint3D p2 = sLane.getCenterLine().get(sLane.getCenterLine().size() - 1);
                    sp = new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
                    beginWidth = sLane.getEndWidth();
                }
                if (eLink.getStartNode().equals(eNode))
                {
                    OTSPoint3D p1 = eLane.getCenterLine().get(1);
                    OTSPoint3D p2 = eLane.getCenterLine().get(0);
                    ep = new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p1.y - p2.y, p1.x - p2.x));
                    endWidth = eLane.getBeginWidth();
                }
                else
                {
                    OTSPoint3D p1 = eLane.getCenterLine().get(eLane.getCenterLine().size() - 2);
                    OTSPoint3D p2 = eLane.getCenterLine().get(eLane.getCenterLine().size() - 1);
                    ep = new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p1.y - p2.y, p1.x - p2.x));
                    endWidth = eLane.getEndWidth();
                }
                OTSLine3D designLine = Bezier.cubic(64, sp, ep);
                CrossSectionLink newLink = new CrossSectionLink(network, linkId, sNode, eNode, linkType, designLine,
                        this.simulator, laneKeepingPolicy);
                Lane newLane = new Lane(newLink, laneId, Length.ZERO, Length.ZERO, beginWidth, endWidth, sLane.getLaneType(),
                        sLane.getSpeedLimit(network.getGtuType(GTUType.DEFAULTS.VEHICLE)));
                return newLane;
            }
            catch (Exception e)
            {
                System.err.println("makeLane: " + e.getMessage());
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public final OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "TestOpenDriveModel [rtiCar.sizes=" + this.rtiCars.size() + ", carType=" + this.carType + ", lastId="
                    + this.lastId + "]";
        }
    }

}
