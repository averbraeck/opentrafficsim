package org.opentrafficsim.road.network.factory;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
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
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
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
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.VelocityGTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingLaneChangeTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.opendrive.GeneratorAnimation;
import org.opentrafficsim.road.network.factory.opendrive.OpenDriveNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.Sensor;
import org.opentrafficsim.road.network.lane.SinkSensor;
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
public class TestOpenDriveParserNoRTI extends AbstractWrappableAnimation
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
                    TestOpenDriveParserNoRTI xmlModel = new TestOpenDriveParserNoRTI();
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

        /** The simulator. */
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

            // put some generators and sinks on the outer edges of the network
            for (Link link : network.getLinkMap().values())
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
                                Time.Abs startTime = Time.Abs.ZERO;
                                Time.Abs endTime = new Time.Abs(Double.MAX_VALUE, TimeUnit.SI);
                                Length.Rel position = lane.getLength().lt(M25) ? M0 : M25;
                                String id = lane.getParentLink().getId() + "." + lane.getId();
                                BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
                                //LaneBasedBehavioralCharacteristics drivingCharacteristics =
                                //    new LaneBasedBehavioralCharacteristics(new IDMPlusOld(), new Altruistic());
                                LaneBasedStrategicalPlanner strategicalPlanner =
                                    new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                                        new LaneBasedGTUFollowingLaneChangeTacticalPlanner(new IDMPlusOld()));
                                LanePerceptionFull perception = new LanePerceptionFull();
                                // new GTUGeneratorIndividual(id, this.simulator, carType, LaneBasedIndividualCar.class,
                                // initialSpeedDist, iatDist, lengthDist, widthDist, maxSpeedDist, Integer.MAX_VALUE,
                                // startTime, endTime, lane, position, GTUDirectionality.DIR_PLUS,
                                // makeSwitchableGTUColorer(), strategicalPlanner, perception);
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
                                Length.Rel position = lane.getLength().lt(M25) ? M0 : M25;
                                Sensor sensor = new SinkSensor(lane, position, this.simulator);
                                try
                                {
                                    lane.addSensor(sensor, GTUType.ALL);
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
                                Time.Abs startTime = Time.Abs.ZERO;
                                Time.Abs endTime = new Time.Abs(Double.MAX_VALUE, TimeUnit.SI);
                                Length.Rel position =
                                    lane.getLength().lt(M25) ? lane.getLength() : lane.getLength().minus(M25);
                                String id = lane.getParentLink().getId() + "." + lane.getId();
                                BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
                                //LaneBasedBehavioralCharacteristics drivingCharacteristics =
                                //    new LaneBasedBehavioralCharacteristics(new IDMPlusOld(), new Altruistic());
                                LaneBasedStrategicalPlanner strategicalPlanner =
                                    new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                                        new LaneBasedGTUFollowingLaneChangeTacticalPlanner(new IDMPlusOld()));
                                LanePerceptionFull perception = new LanePerceptionFull();
                                // new GTUGeneratorIndividual(id, this.simulator, carType, LaneBasedIndividualCar.class,
                                // initialSpeedDist, iatDist, lengthDist, widthDist, maxSpeedDist, Integer.MAX_VALUE,
                                // startTime, endTime, lane, position, GTUDirectionality.DIR_MINUS,
                                // makeSwitchableGTUColorer(), strategicalPlanner, perception);
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
                                Length.Rel position =
                                    lane.getLength().lt(M25) ? lane.getLength() : lane.getLength().minus(M25);
                                Sensor sensor = new SinkSensor(lane, position, this.simulator);
                                try
                                {
                                    lane.addSensor(sensor, GTUType.ALL);
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

            CrossSectionLink link1 = (CrossSectionLink) network.getLink("3766054.5");
            CrossSectionLink link2 = (CrossSectionLink) network.getLink("3766059.7");
            CrossSectionLink link3 = (CrossSectionLink) network.getLink("3766068.3");
            CrossSectionLink link4 = (CrossSectionLink) network.getLink("3766038.5");
            CrossSectionLink link5 = (CrossSectionLink) network.getLink("3766043.3");
            CrossSectionLink link6 = (CrossSectionLink) network.getLink("3766064.2");
            CrossSectionLink link7 = (CrossSectionLink) network.getLink("3766046.3");
            CrossSectionLink link8 = (CrossSectionLink) network.getLink("3766050.3");

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

            for (int i = 0; i < 1; i++) // 52; i++)
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

                BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
                //LaneBasedBehavioralCharacteristics drivingCharacteristics =
                //    new LaneBasedBehavioralCharacteristics(new IDMPlusOld(), new Altruistic());
                LaneBasedStrategicalPlanner sPlanner =
                    new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                        new LaneBasedGTUFollowingTacticalPlanner(new IDMPlusOld())); // , cr);

                System.out.println("Car " + i + " - generated on lane " + lane + " with sn="
                    + lane.getParentLink().getStartNode() + " and en=" + lane.getParentLink().getEndNode()
                    + ", route = " + cr);

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
