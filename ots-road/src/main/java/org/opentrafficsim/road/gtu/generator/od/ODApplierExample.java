package org.opentrafficsim.road.gtu.generator.od;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LinkAnimation;
import org.opentrafficsim.core.network.animation.NodeAnimation;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.gtu.animation.LmrsSwitchableColorer;
import org.opentrafficsim.road.gtu.generator.CFBARoomChecker;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorAnimation;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.Platoons;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation.TYPE;
import org.opentrafficsim.road.network.animation.TrafficLightAnimation;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ODApplierExample extends AbstractWrappableAnimation
{

    /** Lane based or not. */
    static final boolean LANE_BASED = true;

    /** Simulation period. */
    static final Duration PERIOD = new Duration(60.0, DurationUnit.MINUTE);

    /** Demand factor. */
    static final Double DEMAND = 2.0;

    /** */
    private static final long serialVersionUID = 20171211L;

    /** Colorer. */
    private GTUColorer colorer = new LmrsSwitchableColorer();

    /**
     * @param args arguments
     */
    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ODApplierExample animation = new ODApplierExample();
                    // 1 hour simulation run for testing
                    animation.buildAnimator(Time.ZERO, Duration.ZERO, PERIOD, new ArrayList<Property<?>>(), null, true);

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
    public String shortName()
    {
        return "ODApplierExample";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "Example use of the utility ODApplier.applyOD()";
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setIconAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected OTSModelInterface makeModel() throws OTSSimulationException
    {
        return new ODApplierExampleModel();
    }

    /** {@inheritDoc} */
    @Override
    public GTUColorer getColorer()
    {
        return this.colorer;
    }

    /**
     * The simulation model.
     */
    class ODApplierExampleModel implements OTSModelInterface
    {

        /** */
        private static final long serialVersionUID = 20171211L;

        /** The network. */
        private OTSNetwork network;

        /** Simulator. */
        private OTSSimulatorInterface simulator;

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> sim) throws SimRuntimeException
        {
            this.simulator = (OTSSimulatorInterface) sim;
            Map<String, StreamInterface> streams = new HashMap<>();
            streams.put("generation", new MersenneTwister(1L));
            this.simulator.getReplication().setStreams(streams);

            this.network = new OTSNetwork("ODApplierExample");
            try
            {
                // Network
                OTSPoint3D pointA = new OTSPoint3D(-100, 50, 0);
                OTSPoint3D pointA1 = new OTSPoint3D(50, 50, 0);
                OTSPoint3D pointA2 = new OTSPoint3D(0, 0, 0);
                OTSPoint3D pointA3 = new OTSPoint3D(0, -100, 0);
                OTSPoint3D pointB = new OTSPoint3D(1000, 0, 0);
                OTSNode nodeA = new OTSNode(this.network, "A", pointA);
                OTSNode nodeA1 = new OTSNode(this.network, "A1", pointA1);
                OTSNode nodeA2 = new OTSNode(this.network, "A2", pointA2);
                OTSNode nodeA3 = new OTSNode(this.network, "A3", pointA3);
                OTSNode nodeB = new OTSNode(this.network, "B", pointB);
                CrossSectionLink linkAA1 = new CrossSectionLink(this.network, "AA1", nodeA, nodeA1, LinkType.CONNECTOR,
                        new OTSLine3D(pointA, pointA1), this.simulator, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkAA2 = new CrossSectionLink(this.network, "AA2", nodeA, nodeA2, LinkType.CONNECTOR,
                        new OTSLine3D(pointA, pointA2), this.simulator, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkAA3 = new CrossSectionLink(this.network, "AA3", nodeA, nodeA3, LinkType.CONNECTOR,
                        new OTSLine3D(pointA, pointA3), this.simulator, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkA1B = new CrossSectionLink(this.network, "A1B", nodeA1, nodeB, LinkType.FREEWAY,
                        new OTSLine3D(pointA1, pointB), this.simulator, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkA2B = new CrossSectionLink(this.network, "A2B", nodeA2, nodeB, LinkType.FREEWAY,
                        new OTSLine3D(pointA2, pointB), this.simulator, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkA3B = new CrossSectionLink(this.network, "A3B", nodeA3, nodeB, LinkType.FREEWAY,
                        new OTSLine3D(pointA3, pointB), this.simulator, LaneKeepingPolicy.KEEP_RIGHT);
                Lane lane0 = new Lane(linkA1B, "lane0", Length.createSI(0.0), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane lane1 = new Lane(linkA2B, "lane1", Length.createSI(3.5), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane lane2 = new Lane(linkA2B, "lane2", Length.createSI(0.0), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane lane3 = new Lane(linkA2B, "lane3", Length.createSI(-3.5), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane lane4 = new Lane(linkA3B, "lane4", Length.createSI(0.0), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Set<GTUType> gtuTypes = new HashSet<>();
                gtuTypes.add(GTUType.VEHICLE);
                Stripe stripe12 = new Stripe(linkA2B, Length.createSI(1.75), Length.createSI(1.75), Length.createSI(0.2),
                        gtuTypes, Permeable.BOTH);
                Stripe stripe23 = new Stripe(linkA2B, Length.createSI(-1.75), Length.createSI(-1.75), Length.createSI(0.2),
                        gtuTypes, Permeable.BOTH);

                // animation
                new NodeAnimation(nodeA, this.simulator);
                new NodeAnimation(nodeA1, this.simulator);
                new NodeAnimation(nodeA2, this.simulator);
                new NodeAnimation(nodeA3, this.simulator);
                new NodeAnimation(nodeB, this.simulator);
                new LinkAnimation(linkAA1, this.simulator, 0.5f);
                new LinkAnimation(linkAA2, this.simulator, 0.5f);
                new LinkAnimation(linkAA3, this.simulator, 0.5f);
                new LinkAnimation(linkA1B, this.simulator, 0.5f);
                new LinkAnimation(linkA2B, this.simulator, 0.5f);
                new LinkAnimation(linkA3B, this.simulator, 0.5f);
                new LaneAnimation(lane0, this.simulator, Color.GRAY.brighter(), false);
                new LaneAnimation(lane1, this.simulator, Color.GRAY.brighter(), false);
                new LaneAnimation(lane2, this.simulator, Color.GRAY.brighter(), false);
                new LaneAnimation(lane3, this.simulator, Color.GRAY.brighter(), false);
                new LaneAnimation(lane4, this.simulator, Color.GRAY.brighter(), false);
                new StripeAnimation(stripe12, this.simulator, TYPE.DASHED);
                new StripeAnimation(stripe23, this.simulator, TYPE.DASHED);
                new SinkSensor(lane0, Length.createSI(904), this.simulator);
                new SinkSensor(lane1, Length.createSI(900), this.simulator);
                new SinkSensor(lane2, Length.createSI(900), this.simulator);
                new SinkSensor(lane3, Length.createSI(900), this.simulator);
                new SinkSensor(lane4, Length.createSI(904), this.simulator);
                // traffic light
                TrafficLight trafficLight = new SimpleTrafficLight("light1", lane1, Length.createSI(800.0), this.simulator);
                new TrafficLightAnimation(trafficLight, this.simulator);
                this.simulator.scheduleEventAbs(Time.createSI(30 * 60), this, trafficLight, "setTrafficLightColor",
                        new Object[] { TrafficLightColor.YELLOW });
                this.simulator.scheduleEventAbs(Time.createSI(30 * 60 + 6), this, trafficLight, "setTrafficLightColor",
                        new Object[] { TrafficLightColor.RED });
                this.simulator.scheduleEventAbs(Time.createSI(35 * 60), this, trafficLight, "setTrafficLightColor",
                        new Object[] { TrafficLightColor.GREEN });

                // OD
                Categorization categorization;
                if (ODApplierExample.LANE_BASED)
                {
                    categorization = new Categorization("ODExample", Lane.class, GTUType.class);
                }
                else
                {
                    categorization = new Categorization("ODExample", GTUType.class);
                }
                List<Node> origins = new ArrayList<>();
                if (ODApplierExample.LANE_BASED)
                {
                    origins.add(nodeA1);
                    origins.add(nodeA2);
                    origins.add(nodeA3);
                }
                else
                {
                    origins.add(nodeA);
                }
                List<Node> destinations = new ArrayList<>();
                destinations.add(nodeB);
                double fT = PERIOD.si / 3600;
                TimeVector timeVector = new TimeVector(new double[] { 5 * fT, 600 * fT, 610 * fT, 1800 * fT, 3000 * fT },
                        TimeUnit.BASE, StorageType.DENSE);
                ODMatrix od =
                        new ODMatrix("ODExample", origins, destinations, categorization, timeVector, Interpolation.STEPWISE);
                FrequencyVector demand = new FrequencyVector(
                        new double[] { 0 * DEMAND, 1000 * DEMAND, 3000 * DEMAND, 7000 * DEMAND, 0 * DEMAND },
                        FrequencyUnit.PER_HOUR, StorageType.DENSE);

                Category platoonCategory;
                if (ODApplierExample.LANE_BASED)
                {
                    Category category = new Category(categorization, lane1, GTUType.CAR);
                    platoonCategory = category;
                    od.putDemandVector(nodeA2, nodeB, category, demand, timeVector, Interpolation.LINEAR, .4);
                    category = new Category(categorization, lane2, GTUType.CAR);
                    od.putDemandVector(nodeA2, nodeB, category, demand, timeVector, Interpolation.LINEAR, .25);
                    category = new Category(categorization, lane2, GTUType.TRUCK);
                    od.putDemandVector(nodeA2, nodeB, category, demand, timeVector, Interpolation.LINEAR, .05);
                    category = new Category(categorization, lane3, GTUType.CAR);
                    od.putDemandVector(nodeA2, nodeB, category, demand, timeVector, Interpolation.LINEAR, .1);
                    category = new Category(categorization, lane3, GTUType.TRUCK);
                    od.putDemandVector(nodeA2, nodeB, category, demand, timeVector, Interpolation.LINEAR, .2);
                }
                else
                {
                    Category category = new Category(categorization, GTUType.CAR);
                    platoonCategory = category;
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .9);
                    category = new Category(categorization, GTUType.TRUCK);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .1);
                }
                // options
                MarkovCorrelation<GTUType, Frequency> markov = new MarkovCorrelation<>();
                markov.addState(GTUType.TRUCK, 0.4);
                LaneBiases biases = new LaneBiases().addBias(GTUType.VEHICLE, LaneBias.bySpeed(130, 80)).addBias(GTUType.TRUCK,
                        LaneBias.TRUCK_RIGHT);
                ODOptions odOptions = new ODOptions().set(ODOptions.GTU_COLORER, getColorer())
                        .set(ODOptions.ROOM_CHECKER, new CFBARoomChecker()).set(ODOptions.MARKOV, markov)
                        .set(ODOptions.LANE_BIAS, biases).set(ODOptions.NO_LC_DIST, Length.createSI(100.0));
                Map<String, GeneratorObjects> generatedObjects = ODApplier.applyOD(this.network, od, this.simulator, odOptions);
                for (String str : generatedObjects.keySet())
                {
                    new GTUGeneratorAnimation(generatedObjects.get(str).getGenerator(), this.simulator);
                }

                // platoons
                String id = LANE_BASED ? "A21" : "A";
                Lane platoonLane = LANE_BASED ? lane1 : lane0;
                Set<DirectedLanePosition> position = new HashSet<>();
                position.add(new DirectedLanePosition(platoonLane, Length.ZERO, GTUDirectionality.DIR_PLUS));
                Platoons platoons = new Platoons(generatedObjects.get(id).getGenerator(),
                        odOptions.get(ODOptions.GTU_TYPE, null, null, null), this.simulator, streams.get("generation"),
                        position);
                platoons.addPlatoon(Time.createSI(60), Time.createSI(90));
                platoons.fixInfo(nodeA, nodeB, platoonCategory, new Speed(90, SpeedUnit.KM_PER_HOUR));
                for (double t = 62; t < 90; t += 2)
                {
                    platoons.addGtu(Time.createSI(t));
                }
                platoons.addPlatoon(Time.createSI(300), Time.createSI(330));
                for (double t = 302; t < 330; t += 2)
                {
                    platoons.addGtu(Time.createSI(t));
                }
                platoons.start();

            }
            catch (NetworkException | OTSGeometryException | NamingException | ValueException | ParameterException
                    | GTUException | RemoteException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

}
