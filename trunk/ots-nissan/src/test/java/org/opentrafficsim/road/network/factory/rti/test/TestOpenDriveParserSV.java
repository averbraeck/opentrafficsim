package org.opentrafficsim.road.network.factory.rti.test;

import java.awt.Dimension;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.CoordinateTransformLonLatToXY;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorIndividualOld;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.opendrive.old.GeneratorAnimation;
import org.opentrafficsim.road.network.factory.opendrive.old.OpenDriveNetworkLaneParserOld;
import org.opentrafficsim.road.network.factory.rti.communication.RTICar;
import org.opentrafficsim.road.network.factory.rti.communication.ReceiverThread;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestOpenDriveParserSV extends OTSSimulationApplication<OTSModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestOpenDriveParserSV(final OTSModelInterface model, final OTSAnimationPanel animationPanel)
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
                    new TestOpenDriveParserSV(openDriveModel, animationPanel);
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
        return "TestOpenDriveParserSV []";
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

        /**
         * @param simulator the simulator
         */
        TestOpenDriveModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            this.rtiCars = new ArrayList<>();

            // URL url = URLResource.getResource("/NASAames.xodr");
            URL url = URLResource.getResource("/testod.xodr");
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

            // TODO parse these from the xodr-file.
            // double latCenter = 37.40897623275873, lonCenter = -122.0246091728831;//sunnyvale
            // double latCenter = 37.419933552777, lonCenter = -122.05752616111000;//nasa
            double latCenter = nlp.getHeaderTag().getOriginLat().si, lonCenter = nlp.getHeaderTag().getOriginLong().si;

            CoordinateTransform latLonToXY = new CoordinateTransformLonLatToXY(lonCenter, latCenter);
            new GisRenderable2D(this.simulator, gisURL, latLonToXY);

            // Make a GTU Type
            GTUType carType = this.network.getGtuType(GTUType.DEFAULTS.CAR);

            // new ReceiverThread(this.simulator).run();

            // stream
            StreamInterface stream = new MersenneTwister(1);
            Length m25 = new Length(25.0, LengthUnit.METER);
            Length m0 = new Length(0.0, LengthUnit.METER);

            // distributions
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 0.0), SpeedUnit.SI);
            ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> iatDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistExponential(stream, 30.0), DurationUnit.SECOND);
            ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 4.0, 5.0), LengthUnit.METER);
            ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 2.0), LengthUnit.METER);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maxSpeedDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(stream, 30.0, 35.0, 40.0), SpeedUnit.MILE_PER_HOUR);

            ContinuousDistDoubleScalar.Rel<Length, LengthUnit> initialPosDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 0.0, 1.0), LengthUnit.METER);

            // default colorer

            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld()));

            // put some generators and sinks on the outer edges of the network
            for (Link link : this.network.getLinkMap().values())
            {
                CrossSectionLink csLink = (CrossSectionLink) link;
                // look if start node is isolated
                if (link.getStartNode().getLinks().size() == 1) // only ourselves...
                {
                    // put generators and sinks 25 m from the edge of the link
                    for (CrossSectionElement cse : csLink.getCrossSectionElementList())
                    {
                        if (cse instanceof Lane && !(cse instanceof NoTrafficLane))
                        {
                            Lane lane = (Lane) cse;
                            if (Integer.parseInt(lane.getId()) < 0)
                            {
                                // make a generator
                                Time startTime = Time.ZERO;
                                Time endTime = new Time(Double.MAX_VALUE, TimeUnit.BASE_SECOND);
                                Length position = lane.getLength().lt(m25) ? m0 : m25;
                                String id = lane.getParentLink().getId() + "." + lane.getId();
                                new GTUGeneratorIndividualOld(id, this.simulator, carType, LaneBasedIndividualGTU.class,
                                        initialSpeedDist, iatDist, lengthDist, widthDist, maxSpeedDist, Integer.MAX_VALUE,
                                        startTime, endTime, lane, position, GTUDirectionality.DIR_PLUS,
                                        strategicalPlannerFactory, null, this.network);
                                try
                                {
                                    new GeneratorAnimation(lane, position, this.simulator);
                                }
                                catch (RemoteException | NamingException | OTSGeometryException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                            else
                            {
                                // make a sink
                                Length position = lane.getLength().lt(m25) ? m0 : m25;
                                try
                                {
                                    new SinkSensor(lane, position, Compatible.EVERYTHING, this.simulator);
                                }
                                catch (NetworkException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                        }
                    }
                }
                else if (link.getEndNode().getLinks().size() == 1) // only ourselves...
                {
                    // put generators and sinks 25 m from the edge of the link
                    for (CrossSectionElement cse : csLink.getCrossSectionElementList())
                    {
                        if (cse instanceof Lane && !(cse instanceof NoTrafficLane))
                        {
                            Lane lane = (Lane) cse;
                            if (Integer.parseInt(lane.getId()) > 0)
                            {
                                // make a generator
                                Time startTime = Time.ZERO;
                                Time endTime = new Time(Double.MAX_VALUE, TimeUnit.BASE_SECOND);
                                Length position = lane.getLength().lt(m25) ? lane.getLength() : lane.getLength().minus(m25);
                                String id = lane.getParentLink().getId() + "." + lane.getId();
                                new GTUGeneratorIndividualOld(id, this.simulator, carType, LaneBasedIndividualGTU.class,
                                        initialSpeedDist, iatDist, lengthDist, widthDist, maxSpeedDist, Integer.MAX_VALUE,
                                        startTime, endTime, lane, position, GTUDirectionality.DIR_MINUS,
                                        strategicalPlannerFactory, null, this.network);
                                try
                                {
                                    new GeneratorAnimation(lane, position, this.simulator);
                                }
                                catch (RemoteException | NamingException | OTSGeometryException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                            else
                            {
                                // make a sink
                                Length position = lane.getLength().lt(m25) ? lane.getLength() : lane.getLength().minus(m25);
                                try
                                {
                                    new SinkSensor(lane, position, Compatible.EVERYTHING, this.simulator);
                                }
                                catch (NetworkException exception)
                                {
                                    exception.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            CrossSectionLink link1 = (CrossSectionLink) this.network.getLink("3766054.5");
            CrossSectionLink link2 = (CrossSectionLink) this.network.getLink("3766059.7");
            CrossSectionLink link3 = (CrossSectionLink) this.network.getLink("3766068.3");
            CrossSectionLink link4 = (CrossSectionLink) this.network.getLink("3766038.5");
            CrossSectionLink link5 = (CrossSectionLink) this.network.getLink("3766043.3");
            CrossSectionLink link6 = (CrossSectionLink) this.network.getLink("3766064.2");
            CrossSectionLink link7 = (CrossSectionLink) this.network.getLink("3766046.3");
            CrossSectionLink link8 = (CrossSectionLink) this.network.getLink("3766050.3");

            CompleteRoute cr1 = null, cr2 = null, cr3 = null, cr4 = null, cr5 = null, cr6 = null;

            List<Node> nodesVia1 = new ArrayList<>();
            nodesVia1.add(link2.getStartNode());
            nodesVia1.add(link3.getEndNode());
            nodesVia1.add(link4.getStartNode());
            nodesVia1.add(link5.getEndNode());
            nodesVia1.add(link7.getEndNode());
            nodesVia1.add(link8.getStartNode());
            try
            {
                cr1 = this.network.getShortestRouteBetween(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link1.getStartNode(), link1.getStartNode(),
                        nodesVia1);
                Collections.reverse(nodesVia1);
                cr2 = this.network.getShortestRouteBetween(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link1.getStartNode(), link1.getStartNode(),
                        nodesVia1);
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }

            List<Node> nodesVia2 = new ArrayList<>();
            nodesVia2.add(link3.getEndNode());
            nodesVia2.add(link5.getEndNode());
            try
            {
                cr3 = this.network.getShortestRouteBetween(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link3.getStartNode(), link3.getStartNode(),
                        nodesVia2);
                Collections.reverse(nodesVia2);
                cr4 = this.network.getShortestRouteBetween(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link3.getStartNode(), link3.getStartNode(),
                        nodesVia2);
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }

            List<Node> nodesVia3 = new ArrayList<>();
            nodesVia3.add(link7.getEndNode());
            nodesVia3.add(link8.getEndNode());
            try
            {
                cr5 = this.network.getShortestRouteBetween(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link6.getStartNode(), link6.getStartNode(),
                        nodesVia3);
                Collections.reverse(nodesVia3);
                cr6 = this.network.getShortestRouteBetween(network.getGtuType(GTUType.DEFAULTS.VEHICLE), link6.getStartNode(), link6.getStartNode(),
                        nodesVia3);
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }

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
                    CrossSectionElement cse = link.getCrossSectionElementList()
                            .get(routeRandom.nextInt(link.getCrossSectionElementList().size()));
                    if (cse instanceof Lane && !(cse instanceof NoTrafficLane))
                    {
                        lane = (Lane) cse;
                        break;

                    }
                }

                if (lane.getLaneType().getDirectionality(carType).equals(LongitudinalDirectionality.DIR_MINUS))
                {
                    dir = GTUDirectionality.DIR_MINUS;
                }

                DirectedLanePosition directedLanePosition = null;
                try
                {
                    directedLanePosition = new DirectedLanePosition(lane,
                            initialPosDist.draw().times(lane.getCenterLine().getLengthSI()), dir);
                }
                catch (GTUException exception1)
                {
                    exception1.printStackTrace();
                }
                Set<DirectedLanePosition> lanepositionSet = new LinkedHashSet<>();
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
                    {
                        isEnoughSpace = false;
                    }
                }

                if (isEnoughSpace)
                {
                    try
                    {
                        RTICar car = new RTICar(String.valueOf(i), carType, carLength, widthDist.draw(), maxSpeedDist.draw(),
                                this.simulator, this.network);
                        this.rtiCars.add(car);

                    }
                    catch (NamingException | GTUException | OperationalPlanException exception)
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
                new Thread(new ReceiverThread(this.simulator, carType, this.rtiCars, this.network)).start();
            }
            catch (SocketException exception1)
            {
                exception1.printStackTrace();
            }

            /*
             * CrossSectionLink link = (CrossSectionLink) network.getLink("3766053"); for (CrossSectionElement cse :
             * link.getCrossSectionElementList()) { if (cse instanceof Lane) { Lane lane = (Lane) cse;
             * System.out.println("Lane " + lane + " - offset=" + lane.getDesignLineOffsetAtEnd() + " - nextlanes(ALL) = " +
             * lane.nextLanes(GTUType.ALL) + " - nextlanes(CarType) = " + lane.nextLanes(carType)); } }
             */
            // test the shortest path method
            /*
             * Node nodeFrom = network.getLink("3766052").getEndNode(); Node nodeTo =
             * network.getLink("3766035.1").getStartNode(); CompleteRoute cr; try { cr =
             * network.getShortestRouteBetween(GTUType.ALL, nodeFrom, nodeTo);
             * System.out.println(cr.toString().replaceAll("to OTSNode", "\ntoOTSNode").replaceAll(", OTSNode", ", \nOTSNode"));
             * } catch (NetworkException exception) { exception.printStackTrace(); }
             */
        }

        /** {@inheritDoc} */
        @Override
        public final OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestOpenDriveModel [simulator=" + this.simulator + ", rtiCars.size=" + this.rtiCars.size() + "]";
        }
    }

}
