package org.opentrafficsim.road.network.factory.rti.test;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gis.CoordinateTransformLonLatToXY;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SpeedGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.opendrive.LaneAnimationOD;
import org.opentrafficsim.road.network.factory.opendrive.OpenDriveNetworkLaneParser;
import org.opentrafficsim.road.network.factory.rti.communication.ReceiverThread;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestOpenDriveParserNoRTINew extends AbstractWrappableAnimation
{
    /** */
    private static final long serialVersionUID = 1L;

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
                    TestOpenDriveParserNoRTINew xmlModel = new TestOpenDriveParserNoRTINew();
                    // 1 hour simulation run for testing
                    xmlModel.buildAnimator(new Time(0.0, TimeUnit.SECOND), new Duration(0.0, TimeUnit.SECOND), new Duration(
                        60.0, TimeUnit.MINUTE), new ArrayList<Property<?>>(), null, true);
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
    public final String shortName()
    {
        return "TestOpenDriveModel";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TestOpenDriveModel";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts(final SimpleSimulatorInterface simulator)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        return new TestOpenDriveModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
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
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class TestOpenDriveModel implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20150811L;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        private List<LaneBasedIndividualGTU> rtiCars;

        /** */
        private ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist;

        /** */
        private ContinuousDistDoubleScalar.Rel<Duration, TimeUnit> iatDist;

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
             * 
             */
        public TestOpenDriveModel()
        {
            super();
            this.stream = new MersenneTwister(1);
            this.initialSpeedDist = new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 0.0), SpeedUnit.SI);
            this.iatDist = new ContinuousDistDoubleScalar.Rel<>(new DistExponential(this.stream, 30.0), TimeUnit.SECOND);
            this.lengthDist = new ContinuousDistDoubleScalar.Rel<>(new DistUniform(this.stream, 4.0, 5.0), LengthUnit.METER);
            this.widthDist = new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 2.0), LengthUnit.METER);
            this.maxSpeedDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(this.stream, 30.0, 35.0, 40.0),
                    SpeedUnit.MILE_PER_HOUR);
            this.initialPosDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistUniform(this.stream, 0.0, 1.0), LengthUnit.METER);
            this.carType = new GTUType("Car");
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel(
            final SimulatorInterface<Time, Duration, OTSSimTimeDouble> pSimulator)
            throws SimRuntimeException
        {
            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;

            this.rtiCars = new ArrayList<LaneBasedIndividualGTU>();

            // URL url = URLResource.getResource("/NASAames.xodr");
            URL url = URLResource.getResource("/testod.xodr");
            this.simulator.setPauseOnError(false);
            OpenDriveNetworkLaneParser nlp = new OpenDriveNetworkLaneParser(this.simulator);
            OTSNetwork network = null;
            try
            {
                network = nlp.build(url);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException
                | GTUException | OTSGeometryException exception)
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
                destroyLink(nlp, network, "3766109");
                destroyLink(nlp, network, "3766110");
                destroyLink(nlp, network, "3766111");

                Lane lane109 =
                    makeLane(network, "3766068.1", "3766068.0", "3", "3766059.7", "3766059.150", "2", "3766109", "-1",
                        LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation109 = new LaneAnimationOD(lane109, this.simulator, Color.gray);
                nlp.animationMap.put(lane109, animation109);

                Lane lane110 =
                    makeLane(network, "3766068.1", "3766068.0", "4", "3766059.7", "3766059.150", "3", "3766110", "-1",
                        LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation110 = new LaneAnimationOD(lane110, this.simulator, Color.gray);
                nlp.animationMap.put(lane110, animation110);

                Lane lane111 =
                    makeLane(network, "3766068.1", "3766068.0", "5", "3766059.7", "3766059.150", "4", "3766111", "-1",
                        LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation111 = new LaneAnimationOD(lane111, this.simulator, Color.gray);
                nlp.animationMap.put(lane111, animation111);

                destroyLink(nlp, network, "3766175");
                destroyLink(nlp, network, "3766176");
                destroyLink(nlp, network, "3766177");

                Lane lane175 =
                    makeLane(network, "3766059.1", "3766059.0", "3", "3766054.5", "3766054.191", "2", "3766175", "-1",
                        LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation175 = new LaneAnimationOD(lane175, this.simulator, Color.gray);
                nlp.animationMap.put(lane175, animation175);

                Lane lane176 =
                    makeLane(network, "3766059.1", "3766059.0", "4", "3766054.5", "3766054.191", "3", "3766176", "-1",
                        LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation176 = new LaneAnimationOD(lane176, this.simulator, Color.gray);
                nlp.animationMap.put(lane176, animation176);

                Lane lane177 =
                    makeLane(network, "3766059.1", "3766059.0", "5", "3766054.5", "3766054.191", "4", "3766177", "-1",
                        LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation177 = new LaneAnimationOD(lane177, this.simulator, Color.gray);
                nlp.animationMap.put(lane177, animation177);

                Lane lane191x =
                    makeLane(network, "3766054.5", "3766054.191", "-6", "3766059.1", "3766059.0", "-4", "3766191x", "-1",
                        LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation191x = new LaneAnimationOD(lane191x, this.simulator, Color.gray);
                nlp.animationMap.put(lane191x, animation191x);

            }
            catch (OTSGeometryException | NetworkException | NamingException | RemoteException e)
            {
                e.printStackTrace();
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
            network.buildGraph(this.carType);
            
            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                        new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld()));

            // generate 2 GTUs on 70.1 (parking lot NRC SV)
            try
            {
                CrossSectionLink link70_1 = (CrossSectionLink) network.getLink("3766070.1");
                Lane lane70_1m1 = getLane(link70_1, "-1"); // get the rightmost lane (-1) to drive north
                Lane lane70_1p1 = getLane(link70_1, "1"); // get the rightmost lane (1) to drive south

                // create a route around NRC SV
                Node n70_0 = network.getNode("3766070.0");
                Node n70_23 = network.getNode("3766070.23");
                Node n45_0 = network.getNode("3766045.0");
                Node n45_22 = network.getNode("3766045.22");
                Node n65_0 = network.getNode("3766065.0");
                Node n65_8 = network.getNode("3766065.8");
                Route route7070r =
                    new CompleteRoute("Right around NRC-SV", this.carType, network.getShortestRouteBetween(this.carType,
                        n70_0, n70_0, Arrays.asList(new Node[] {n45_0, n65_0})).getNodes());
                putCar(lane70_1m1, route7070r, network, GTUDirectionality.DIR_PLUS, strategicalPlannerFactory);
                Route route7070l =
                    new CompleteRoute("Left around NRC-SV", this.carType, network.getShortestRouteBetween(this.carType,
                        n70_0, n70_0, Arrays.asList(new Node[] {n65_8, n45_22, n70_23})).getNodes());
                putCar(lane70_1p1, route7070l, network, GTUDirectionality.DIR_MINUS, strategicalPlannerFactory);
            }
            catch (NetworkException | GTUException | NamingException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

            // generate 10 GTUs on 68 driving the upper circle.
            try
            {
                List<CrossSectionLink> links68 = new ArrayList<>();
                links68.add((CrossSectionLink) network.getLink("3766068.1"));
                links68.add((CrossSectionLink) network.getLink("3766068.2"));
                links68.add((CrossSectionLink) network.getLink("3766068.3"));
                links68.add((CrossSectionLink) network.getLink("3766068.4"));
                links68.add((CrossSectionLink) network.getLink("3766068.5"));

                // create a right route for top circle
                Node n68_0 = network.getNode("3766068.0");
                Node n38_88 = network.getNode("3766038.88");
                Node n43_0 = network.getNode("3766043.0");
                Node n45_0 = network.getNode("3766045.0");
                Node n65_0 = network.getNode("3766065.0");
                Node n64_17 = network.getNode("3766064.17");
                Route route6868r =
                    new CompleteRoute("Right top", this.carType, network.getShortestRouteBetween(this.carType, n68_0, n68_0,
                        Arrays.asList(new Node[] {n38_88, n43_0, n45_0, n65_0, n64_17})).getNodes());
                System.out.println(route6868r);
                for (int i = 0; i < 5; i++)
                {
                    putCar(randLane(links68, LongitudinalDirectionality.DIR_PLUS), route6868r, network,
                        GTUDirectionality.DIR_PLUS, strategicalPlannerFactory);
                }

                // create a left route for top circle
                Node n38_0 = network.getNode("3766038.0");
                Node n43_53 = network.getNode("3766043.53");
                Node n45_22 = network.getNode("3766045.22");
                Node n65_8 = network.getNode("3766065.8");
                Node n64_0 = network.getNode("3766064.0");
                Node n68_158 = network.getNode("3766068.158");
                Route route6868l =
                    new CompleteRoute("Right top", this.carType, network.getShortestRouteBetween(this.carType, n68_158,
                        n68_158, Arrays.asList(new Node[] {n64_0, n65_8, n45_22, n43_53, n38_0})).getNodes());
                System.out.println(route6868l);
                for (int i = 0; i < 5; i++)
                {
                    putCar(randLane(links68, LongitudinalDirectionality.DIR_MINUS), route6868l, network,
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
                        initialPosDist.draw().multiplyBy(lane.getCenterLine().getLengthSI()), dir);
                Set<DirectedLanePosition> lanepositionSet = new HashSet<DirectedLanePosition>();
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
                        new DirectedLanePosition(lane, initialPosDist.draw().multiplyBy(
                            lane.getCenterLine().getLengthSI()), dir);
                }
                catch (GTUException exception1)
                {
                    exception1.printStackTrace();
                }
                Set<DirectedLanePosition> lanepositionSet = new HashSet<DirectedLanePosition>();
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
                new Thread(new ReceiverThread(this.simulator, this.carType, this.rtiCars, network)).start();
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
                    if (lane.getDirectionality(this.carType).equals(dir) || lane.getDirectionality(this.carType).isBoth())
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

        private final void putCar(final Lane lane, final Route route, final OTSNetwork network, final GTUDirectionality dir,
            final LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> laneBasedStrategicalPlannerFactory)
            throws GTUException, NamingException, NetworkException, SimRuntimeException, OTSGeometryException
        {
            DirectedLanePosition directedLanePosition =
                new DirectedLanePosition(lane, this.initialPosDist.draw().multiplyBy(lane.getCenterLine().getLengthSI()),
                    dir);
            Set<DirectedLanePosition> lanepositionSet = new HashSet<DirectedLanePosition>();
            lanepositionSet.add(directedLanePosition);
            Length carLength = this.lengthDist.draw();
            LaneBasedIndividualGTU car =
                new LaneBasedIndividualGTU("" + (++this.lastId), this.carType, carLength, this.widthDist.draw(),
                    this.maxSpeedDist.draw(), this.simulator, network);
            car.init(laneBasedStrategicalPlannerFactory.create(car), lanepositionSet, new Speed(0.0,
                SpeedUnit.METER_PER_SECOND));
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
         * @param nlp the parser with the animation map
         * @param network the network in which the link is registered
         * @param linkId the link to destroy
         * @throws NamingException in case destroying fails
         * @throws NetworkException in case link cannot be found in the network
         */
        private void destroyLink(final OpenDriveNetworkLaneParser nlp, final OTSNetwork network, final String linkId)
            throws NamingException, NetworkException
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

        /**
         * Create an extra link to "repair" the network.
         * @param network network
         * @param sLinkStr start link id
         * @param sNodeStr start node id
         * @param sLaneStr start lane id
         * @param eLinkStr end link id
         * @param eNodeStr end node id
         * @param eLaneStr end lane id
         * @param linkId the id of the new link
         * @param laneId the id of the new lane
         * @param linkType the type of the new link
         * @param laneKeepingPolicy the lane keeping policy of the new link
         * @return the created lane
         * @throws OTSGeometryException when points cannot be found or line cannot be constructed
         * @throws NetworkException when lane cannot be constructed
         */
        private Lane makeLane(final OTSNetwork network, final String sLinkStr, final String sNodeStr, final String sLaneStr,
            final String eLinkStr, final String eNodeStr, final String eLaneStr, final String linkId, final String laneId,
            final LinkType linkType, final LaneKeepingPolicy laneKeepingPolicy) throws OTSGeometryException,
            NetworkException
        {
            CrossSectionLink sLink = (CrossSectionLink) network.getLink(sLinkStr);
            OTSNode sNode = (OTSNode) network.getNode(sNodeStr);
            Lane sLane = (Lane) sLink.getCrossSectionElement(sLaneStr);
            CrossSectionLink eLink = (CrossSectionLink) network.getLink(eLinkStr);
            OTSNode eNode = (OTSNode) network.getNode(eNodeStr);
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
            CrossSectionLink newLink = new CrossSectionLink(network, linkId, sNode, eNode, linkType, designLine, laneKeepingPolicy);
            newLink.addDirectionality(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
            Lane newLane =
                new Lane(newLink, laneId, Length.ZERO, Length.ZERO, beginWidth, endWidth, sLane.getLaneType(),
                    LongitudinalDirectionality.DIR_PLUS, sLane.getSpeedLimit(GTUType.ALL), sLane.getOvertakingConditions());
            return newLane;
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()

        {
            return this.simulator;
        }

        /**
         * @return a GTUColorer
         */
        private final GTUColorer makeSwitchableGTUColorer()
        {
            GTUColorer[] gtuColorers =
                new GTUColorer[] {
                    new IDGTUColorer(),
                    new SpeedGTUColorer(new Speed(100.0, SpeedUnit.KM_PER_HOUR)),
                    new AccelerationGTUColorer(new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2), new Acceleration(
                        1.0, AccelerationUnit.METER_PER_SECOND_2))};
            return new SwitchableGTUColorer(0, gtuColorers);
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
