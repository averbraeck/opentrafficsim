package org.opentrafficsim.demo.conflict;

import java.awt.Dimension;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.demo.conflict.BusStreetDemo.BusStreetModel;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TTCRoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectBusStopPerception;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRS;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class BusStreetDemo extends OTSSimulationApplication<BusStreetModel>
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a CrossingTrafficLights Swing application.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model BusStreetModel; the model
     * @throws OTSDrawingException on animation error
     */
    public BusStreetDemo(final String title, final OTSAnimationPanel panel, final BusStreetModel model)
            throws OTSDrawingException
    {
        super(model, panel);
        OTSRoadNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
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
            BusStreetModel busModel = new BusStreetModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), busModel);
            OTSAnimationPanel animationPanel = new OTSAnimationPanel(busModel.getNetwork().getExtent(), new Dimension(800, 600),
                    simulator, busModel, DEFAULT_COLORER, busModel.getNetwork());
            BusStreetDemo app = new BusStreetDemo("Bus street demo", animationPanel, busModel);
            app.setExitOnClose(exitOnClose);
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class BusStreetModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OTSRoadNetwork network;

        /**
         * @param simulator OTSSimulatorInterface; the simulator for this model
         */
        public BusStreetModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            Map<String, StreamInterface> streams = new HashMap<>();
            streams.put("generation", new MersenneTwister(100L));
            this.simulator.getReplication().setStreams(streams);
            try
            {
                URL xmlURL = URLResource.getResource("/conflict/BusStreet.xml");
                this.network = new OTSRoadNetwork("BusStreet", true);
                XmlNetworkLaneParser.build(xmlURL, this.network, getSimulator());

                ConflictBuilder.buildConflicts(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));

                // Add bus stops
                Lane lane = ((CrossSectionLink) this.network.getLink("B1B2")).getLanes().get(0);
                BusStop stop = new BusStop("Cafe Boszicht.1", lane, lane.getLength(), "Cafe Boszicht", this.simulator);
                Set<String> lines1 = new HashSet<>();
                lines1.add("1");
                stop.setLines(lines1);

                lane = ((CrossSectionLink) this.network.getLink("C1C2")).getLanes().get(0);
                stop = new BusStop("Cafe Boszicht.2", lane, lane.getLength(), "Cafe Boszicht", this.simulator);
                Set<String> lines2 = new HashSet<>();
                lines2.add("2");
                stop.setLines(lines2);

                lane = ((CrossSectionLink) this.network.getLink("EF")).getLanes().get(0);
                stop = new BusStop("Herberg De Deugd", lane, new Length(75.0, LengthUnit.SI), "Herberg De Deugd",
                        this.simulator);
                stop.setLines(lines1);

                lane = ((CrossSectionLink) this.network.getLink("FG")).getLanes().get(1);
                stop = new BusStop("De Vleeshoeve", lane, new Length(75.0, LengthUnit.SI), "De Vleeshoeve", this.simulator);
                Set<String> lines12 = new HashSet<>();
                lines12.add("1");
                lines12.add("2");
                stop.setLines(lines12);

                lane = ((CrossSectionLink) this.network.getLink("GH")).getLanes().get(2);
                stop = new BusStop("Kippenboerderij De Scharrelaar", lane, new Length(50.0, LengthUnit.SI),
                        "Kippenboerderij De Scharrelaar", this.simulator);
                stop.setLines(lines2);

                lane = ((CrossSectionLink) this.network.getLink("I1I2")).getLanes().get(0);
                stop = new BusStop("Dorpshuys", lane, lane.getLength(), "Dorpshuys", this.simulator);
                stop.setLines(lines1);

                lane = ((CrossSectionLink) this.network.getLink("K1K2")).getLanes().get(0);
                stop = new BusStop("De verkeerde afslag", lane, lane.getLength(), "De verkeerde afslag", this.simulator);
                stop.setLines(lines12);

                makeGenerator(streams.get("generation"));

            }
            catch (Exception exception)
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
         * Make the generator.
         * @param stream StreamInterface; random number stream
         * @throws GTUException on exception
         * @throws ParameterException on exception
         * @throws ProbabilityException on exception
         * @throws SimRuntimeException on exception
         */
        private void makeGenerator(final StreamInterface stream)
                throws GTUException, SimRuntimeException, ProbabilityException, ParameterException
        {
            Lane lane = ((CrossSectionLink) this.network.getLink("AB")).getLanes().get(0);
            String id = lane.getId();
            Set<DirectedLanePosition> initialLongitudinalPositions = new HashSet<>();
            initialLongitudinalPositions
                    .add(new DirectedLanePosition(lane, new Length(10.0, LengthUnit.SI), GTUDirectionality.DIR_PLUS));
            Generator<Duration> headwayGenerator =
                    new HeadwayGenerator(new Frequency(800, FrequencyUnit.PER_HOUR), this.simulator);
            LaneBasedGTUCharacteristicsGenerator characteristicsGenerator =
                    new CharacteristicsGenerator(this.simulator, new double[] {0.9, 0.06, 0.04}, this.network);
            RoomChecker roomChecker = new TTCRoomChecker(new Duration(10.0, DurationUnit.SI));
            new LaneBasedGTUGenerator(id, headwayGenerator, characteristicsGenerator,
                    GeneratorPositions.create(initialLongitudinalPositions, stream), this.network, this.simulator, roomChecker,
                    new IdGenerator(""));
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 jan. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class HeadwayGenerator implements Generator<Duration>
    {

        /** Demand level. */
        private final Frequency demand;

        /** Simulator. */
        private final SimulatorInterface.TimeDoubleUnit simulator;

        /**
         * @param demand Frequency; demand
         * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
         */
        HeadwayGenerator(final Frequency demand, final SimulatorInterface.TimeDoubleUnit simulator)
        {
            this.demand = demand;
            this.simulator = simulator;
        }

        /** {@inheritDoc} */
        @Override
        public Duration draw() throws ProbabilityException, ParameterException
        {
            return new Duration(
                    -Math.log(this.simulator.getReplication().getStream("generation").nextDouble()) / this.demand.si,
                    DurationUnit.SI);
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 jan. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class CharacteristicsGenerator implements LaneBasedGTUCharacteristicsGenerator
    {

        /** Simulator. */
        private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

        /** Probabilities. */
        private final double[] probabilities;

        /** Strategical planner factory. */
        private final LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> plannerFactory;

        /** Route for car. */
        private final Route carRouteN;

        /** Route for car. */
        private final Route carRouteO;

        /** Nodes for bus, line 1. */
        private final List<Node> busNodes1;

        /** Nodes for bus, line 2. */
        private final List<Node> busNodes2;

        /** Short dwell time. */
        private final Duration shortDwellTime = new Duration(15.0, DurationUnit.SI);

        /** Long dwell time. */
        private final Duration longDwellTime = new Duration(60.0, DurationUnit.SI);

        /** The network. */
        private OTSRoadNetwork network;

        /**
         * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
         * @param probabilities double[]; probabilities
         * @param network OTSRoadNetwork; network
         */
        public CharacteristicsGenerator(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final double[] probabilities,
                final OTSRoadNetwork network)
        {
            this.simulator = simulator;
            this.probabilities = probabilities;
            this.network = network;
            List<Node> carNodesN = new ArrayList<>();
            carNodesN.add(network.getNode("A"));
            carNodesN.add(network.getNode("B"));
            carNodesN.add(network.getNode("C"));
            carNodesN.add(network.getNode("D"));
            carNodesN.add(network.getNode("E"));
            carNodesN.add(network.getNode("F"));
            carNodesN.add(network.getNode("G"));
            carNodesN.add(network.getNode("H"));
            carNodesN.add(network.getNode("I"));
            carNodesN.add(network.getNode("J"));
            carNodesN.add(network.getNode("K"));
            carNodesN.add(network.getNode("L"));
            carNodesN.add(network.getNode("M"));
            List<Node> carNodesO = new ArrayList<>(carNodesN);
            carNodesN.add(network.getNode("N"));
            carNodesO.add(network.getNode("O"));
            this.carRouteN = new Route("carN", carNodesN);
            this.carRouteO = new Route("carO", carNodesO);
            this.busNodes1 = new ArrayList<>();
            this.busNodes1.add(network.getNode("A"));
            this.busNodes1.add(network.getNode("B"));
            this.busNodes1.add(network.getNode("B1"));
            this.busNodes1.add(network.getNode("B2"));
            this.busNodes1.add(network.getNode("D"));
            this.busNodes1.add(network.getNode("E"));
            this.busNodes1.add(network.getNode("F"));
            this.busNodes1.add(network.getNode("G"));
            this.busNodes1.add(network.getNode("H"));
            this.busNodes1.add(network.getNode("I"));
            this.busNodes1.add(network.getNode("I1"));
            this.busNodes1.add(network.getNode("I2"));
            this.busNodes1.add(network.getNode("J"));
            this.busNodes1.add(network.getNode("K"));
            this.busNodes1.add(network.getNode("K1"));
            this.busNodes1.add(network.getNode("K2"));
            this.busNodes1.add(network.getNode("L"));
            this.busNodes1.add(network.getNode("M"));
            this.busNodes1.add(network.getNode("N"));

            this.busNodes2 = new ArrayList<>();
            this.busNodes2.add(network.getNode("A"));
            this.busNodes2.add(network.getNode("B"));
            this.busNodes2.add(network.getNode("C"));
            this.busNodes2.add(network.getNode("C1"));
            this.busNodes2.add(network.getNode("C2"));
            this.busNodes2.add(network.getNode("E"));
            this.busNodes2.add(network.getNode("F"));
            this.busNodes2.add(network.getNode("G"));
            this.busNodes2.add(network.getNode("H"));
            this.busNodes2.add(network.getNode("I"));
            this.busNodes2.add(network.getNode("J"));
            this.busNodes2.add(network.getNode("K"));
            this.busNodes2.add(network.getNode("K1"));
            this.busNodes2.add(network.getNode("K2"));
            this.busNodes2.add(network.getNode("K3"));
            this.busNodes2.add(network.getNode("L"));
            this.busNodes2.add(network.getNode("M"));
            this.busNodes2.add(network.getNode("O"));

            this.plannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactoryCarBus(), new ParameterFactoryCarBus());
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GTUException
        {
            double r = this.simulator.getReplication().getStream("generation").nextDouble();
            int classNum = r < this.probabilities[0] ? 0 : r < this.probabilities[0] + this.probabilities[1] ? 1 : 2;
            r = this.simulator.getReplication().getStream("generation").nextDouble();
            GTUType gtuType;
            Length length;
            Length width;
            Speed maximumSpeed;
            Route route;
            switch (classNum)
            {
                case 0:
                {
                    gtuType = new GTUType("CAR", this.network.getGtuType(GTUType.DEFAULTS.CAR));
                    length = new Length(4.0, LengthUnit.SI);
                    width = new Length(1.8, LengthUnit.SI);
                    maximumSpeed = new Speed(200.0, SpeedUnit.KM_PER_HOUR);
                    route = r < 0.5 ? this.carRouteN : this.carRouteO;
                    break;
                }
                case 1:
                {
                    gtuType = new GTUType("BUS1", this.network.getGtuType(GTUType.DEFAULTS.SCHEDULED_BUS));
                    length = new Length(8.0, LengthUnit.SI);
                    width = new Length(2.0, LengthUnit.SI);
                    maximumSpeed = new Speed(100.0, SpeedUnit.KM_PER_HOUR);
                    BusSchedule schedule = new BusSchedule("bus1." + this.simulator.getSimulatorTime(), this.busNodes1, "1");
                    Time now = this.simulator.getSimulatorTime();
                    schedule.addBusStop("Cafe Boszicht.1", now.plus(new Duration(70.0, DurationUnit.SI)), this.longDwellTime,
                            true);
                    schedule.addBusStop("Herberg De Deugd", now.plus(new Duration(100.0, DurationUnit.SI)), this.shortDwellTime,
                            false);
                    schedule.addBusStop("De Vleeshoeve", now.plus(new Duration(120.0, DurationUnit.SI)), this.shortDwellTime,
                            false);
                    schedule.addBusStop("Dorpshuys", now.plus(new Duration(200.0, DurationUnit.SI)), this.longDwellTime, true);
                    schedule.addBusStop("De verkeerde afslag", now.plus(new Duration(270.0, DurationUnit.SI)),
                            this.longDwellTime, true);
                    route = schedule;
                    break;
                }
                case 2:
                {
                    gtuType = new GTUType("BUS2", this.network.getGtuType(GTUType.DEFAULTS.SCHEDULED_BUS));
                    length = new Length(12.0, LengthUnit.SI);
                    width = new Length(2.0, LengthUnit.SI);
                    maximumSpeed = new Speed(100.0, SpeedUnit.KM_PER_HOUR);
                    BusSchedule schedule = new BusSchedule("bus2." + this.simulator.getSimulatorTime(), this.busNodes2, "2");
                    Time now = this.simulator.getSimulatorTime();
                    schedule.addBusStop("Cafe Boszicht.2", now.plus(new Duration(80.0, DurationUnit.SI)), this.longDwellTime,
                            true);
                    schedule.addBusStop("De Vleeshoeve", now.plus(new Duration(110.0, DurationUnit.SI)), this.shortDwellTime,
                            false);
                    schedule.addBusStop("Kippenboerderij De Scharrelaar", now.plus(new Duration(180.0, DurationUnit.SI)),
                            this.longDwellTime, false);
                    schedule.addBusStop("De verkeerde afslag", now.plus(new Duration(260.0, DurationUnit.SI)),
                            this.longDwellTime, true);
                    route = schedule;
                    break;
                }
                default:
                    throw new RuntimeException("Reaching default of switch case.");
            }

            GTUCharacteristics gtuCharacteristics = new GTUCharacteristics(gtuType, length, width, maximumSpeed,
                    Acceleration.createSI(3.0), Acceleration.createSI(-8.0), length.multiplyBy(0.5));

            return new LaneBasedGTUCharacteristics(gtuCharacteristics, this.plannerFactory, route, null, null,
                    VehicleModel.MINMAX);
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 jan. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class LMRSFactoryCarBus implements LaneBasedTacticalPlannerFactory<LMRS>
    {

        /** */
        LMRSFactoryCarBus()
        {
        }

        /** {@inheritDoc} */
        @Override
        public final Parameters getParameters()
        {
            ParameterSet parameters = new ParameterSet();
            parameters.setDefaultParameters(ParameterTypes.class);
            parameters.setDefaultParameters(LmrsParameters.class);
            parameters.setDefaultParameters(ConflictUtil.class);
            parameters.setDefaultParameters(AbstractIDM.class);
            return parameters;
        }

        /** {@inheritDoc} */
        @Override
        public final LMRS create(final LaneBasedGTU gtu) throws GTUException
        {
            DefaultLMRSPerceptionFactory pFac = new DefaultLMRSPerceptionFactory();
            LMRS lmrs = new LMRS(new IDMPlus(), gtu, pFac.generatePerception(gtu), Synchronization.PASSIVE, Cooperation.PASSIVE,
                    GapAcceptance.INFORMED, Tailgating.NONE);
            lmrs.setDefaultIncentives();
            if (gtu.getGTUType().isOfType(GTUType.DEFAULTS.SCHEDULED_BUS))
            {
                lmrs.addMandatoryIncentive(new IncentiveBusStop());
                lmrs.addAccelerationIncentive(new AccelerationBusStop());
                lmrs.getPerception().addPerceptionCategory(new DirectBusStopPerception(lmrs.getPerception()));
            }
            return lmrs;
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 jan. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class ParameterFactoryCarBus implements ParameterFactory
    {

        /** */
        ParameterFactoryCarBus()
        {
        }

        /** {@inheritDoc} */
        @Override
        public void setValues(final Parameters parameters, final GTUType gtuType) throws ParameterException
        {

            parameters.setParameter(ParameterTypes.LOOKAHEAD, new Length(100.0, LengthUnit.METER));
            if (gtuType.isOfType(GTUType.DEFAULTS.CAR))
            {
                parameters.setParameter(LmrsParameters.VGAIN, new Speed(3.0, SpeedUnit.METER_PER_SECOND));
            }
            else if (gtuType.isOfType(GTUType.DEFAULTS.SCHEDULED_BUS))
            {
                parameters.setParameter(ParameterTypes.A, new Acceleration(0.8, AccelerationUnit.METER_PER_SECOND_2));
            }
            else
            {
                throw new RuntimeException("Unable to determine characteristics for GTU of type " + gtuType);
            }

        }

    }

}
