package org.opentrafficsim.road.network.factory;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

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

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.VelocityGTUColorer;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingLaneChangeTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Altruistic;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.opendrive.LaneAnimationOD;
import org.opentrafficsim.road.network.factory.opendrive.OpenDriveNetworkLaneParser;
import org.opentrafficsim.road.network.factory.opendrive.communicationRTI.RTICars;
import org.opentrafficsim.road.network.factory.opendrive.communicationRTI.ReceiverThread;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestOpenDriveParserNoRTINew extends AbstractWrappableAnimation
{
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
                    xmlModel.buildAnimator(new Time.Abs(0.0, TimeUnit.SECOND), new Time.Rel(0.0, TimeUnit.SECOND),
                        new Time.Rel(60.0, TimeUnit.MINUTE), new ArrayList<AbstractProperty<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException exception)
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
    protected final JPanel makeCharts()
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

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
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

        /** the simulator. */
        private OTSDEVSSimulatorInterface simulator;

        private List<LaneBasedIndividualGTU> rtiCars;

        /** {@inheritDoc} */
        @Override
        public final
            void
            constructModel(
                final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
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

            // Make a GTU Type
            GTUType carType = GTUType.makeGTUType("Car");

            // new ReceiverThread(this.simulator).run();

            // stream
            StreamInterface stream = new MersenneTwister(1);
            Length.Rel M25 = new Length.Rel(25.0, LengthUnit.METER);
            Length.Rel M0 = new Length.Rel(0.0, LengthUnit.METER);

            // distributions
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 0.0), SpeedUnit.SI);
            ContinuousDistDoubleScalar.Rel<Time.Rel, TimeUnit> iatDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistExponential(stream, 30.0), TimeUnit.SECOND);
            ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> lengthDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 4.0, 5.0), LengthUnit.METER);
            ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> widthDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 2.0), LengthUnit.METER);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maxSpeedDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(stream, 30.0, 35.0, 40.0),
                    SpeedUnit.MILE_PER_HOUR);

            ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> initialPosDist =
                new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 0.0, 1.0), LengthUnit.METER);

            // default colorer

            CrossSectionLink link1 = (CrossSectionLink) network.getLink("3766054.5");
            CrossSectionLink link2 = (CrossSectionLink) network.getLink("3766059.7");
            CrossSectionLink link3 = (CrossSectionLink) network.getLink("3766068.3");
            CrossSectionLink link4 = (CrossSectionLink) network.getLink("3766038.5");
            CrossSectionLink link5 = (CrossSectionLink) network.getLink("3766043.3");
            CrossSectionLink link6 = (CrossSectionLink) network.getLink("3766064.2");
            CrossSectionLink link7 = (CrossSectionLink) network.getLink("3766046.3");
            CrossSectionLink link8 = (CrossSectionLink) network.getLink("3766050.3");

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
                    makeLane(network, "3766054.5", "3766054.191", "-6", "3766059.1", "3766059.0", "-4", "3766191x",
                        "-1", LinkType.ALL, LaneKeepingPolicy.KEEP_LANE);
                Renderable2D animation191x = new LaneAnimationOD(lane191x, this.simulator, Color.gray);
                nlp.animationMap.put(lane191x, animation191x);

            }
            catch (OTSGeometryException | NetworkException | NamingException | RemoteException e)
            {
                e.printStackTrace();
            }

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

            /*-
            // generate 1 GTU on cr2
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
                Length.Rel carLength = lengthDist.draw();

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

                LaneBasedDrivingCharacteristics drivingCharacteristics =
                    new LaneBasedDrivingCharacteristics(new IDMPlus(), new Altruistic());
                LaneBasedStrategicalPlanner sPlanner =
                    new LaneBasedStrategicalRoutePlanner(drivingCharacteristics,
                        new LaneBasedGTUFollowingLaneChangeTacticalPlanner(), cRoutes.get(routeRandom.nextInt(6)));
                LanePerceptionFull perception = new LanePerceptionFull();

                DirectedLanePosition directedLanePosition =
                    new DirectedLanePosition(lane,
                        initialPosDist.draw().multiplyBy(lane.getCenterLine().getLengthSI()), dir);
                Set<DirectedLanePosition> lanepositionSet = new HashSet<DirectedLanePosition>();
                lanepositionSet.add(directedLanePosition);

                Length.Rel carLength = lengthDist.draw();
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

            try
            {
                new Thread(new ReceiverThread(this.simulator, carType, this.rtiCars)).start();
            }
            catch (SocketException exception1)
            {
                exception1.printStackTrace();
            }

        }

        /**
         * Destroy the animation of the link and underlying cross section elements
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
         * Create an extra link to "repair" the network
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
        private Lane
            makeLane(final OTSNetwork network, final String sLinkStr, final String sNodeStr, final String sLaneStr,
                final String eLinkStr, final String eNodeStr, final String eLaneStr, final String linkId,
                final String laneId, final LinkType linkType, final LaneKeepingPolicy laneKeepingPolicy)
                throws OTSGeometryException, NetworkException
        {
            CrossSectionLink sLink = (CrossSectionLink) network.getLink(sLinkStr);
            OTSNode sNode = (OTSNode) network.getNode(sNodeStr);
            Lane sLane = (Lane) sLink.getCrossSectionElement(sLaneStr);
            CrossSectionLink eLink = (CrossSectionLink) network.getLink(eLinkStr);
            OTSNode eNode = (OTSNode) network.getNode(eNodeStr);
            Lane eLane = (Lane) eLink.getCrossSectionElement(eLaneStr);
            DirectedPoint sp, ep;
            Length.Rel beginWidth, endWidth;
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
            CrossSectionLink newLink =
                new CrossSectionLink(linkId, sNode, eNode, linkType, designLine, laneKeepingPolicy);
            newLink.addDirectionality(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
            Lane newLane =
                new Lane(newLink, laneId, Length.Rel.ZERO, Length.Rel.ZERO, beginWidth, endWidth, sLane.getLaneType(),
                    LongitudinalDirectionality.DIR_PLUS, sLane.getSpeedLimit(GTUType.ALL),
                    sLane.getOvertakingConditions());
            network.addLink(newLink);
            return newLane;
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
            getSimulator()

        {
            return this.simulator;
        }

        /**
         * @return a GTUColorer
         */
        private GTUColorer makeSwitchableGTUColorer()
        {
            GTUColorer[] gtuColorers =
                new GTUColorer[]{
                    new IDGTUColorer(),
                    new VelocityGTUColorer(new Speed(100.0, SpeedUnit.KM_PER_HOUR)),
                    new AccelerationGTUColorer(new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                        new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2))};
            return new SwitchableGTUColorer(0, gtuColorers);
        }
    }

}
