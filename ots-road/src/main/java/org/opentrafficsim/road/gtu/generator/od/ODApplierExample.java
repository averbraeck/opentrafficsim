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
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LinkAnimation;
import org.opentrafficsim.core.network.animation.NodeAnimation;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.gtu.animation.LmrsSwitchableColorer;
import org.opentrafficsim.road.gtu.generator.GeneratorAnimation;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.Bias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.od.ODApplier.GeneratorObjects;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation.TYPE;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    static final boolean LANE_BASED = false;

    /** */
    private static final long serialVersionUID = 20171211L;

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
                    animation.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(60.0, DurationUnit.MINUTE),
                            new ArrayList<Property<?>>(), null, true);

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
        AnimationToggles.setTextAnimationTogglesFull(this);
        this.hideAnimationClass(OTSLink.class);
    }

    /** {@inheritDoc} */
    @Override
    protected OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        return new ODApplierExampleModel(colorer);
    }

    /** {@inheritDoc} */
    @Override
    protected GTUColorer getColorer()
    {
        return new LmrsSwitchableColorer();
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
        private OTSDEVSSimulatorInterface simulator;

        /** GTU colorer. */
        private final GTUColorer colorer;

        /**
         * Constructor.
         * @param colorer GTUColorer; GTU colorer
         */
        ODApplierExampleModel(final GTUColorer colorer)
        {
            this.colorer = colorer;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> sim)
                throws SimRuntimeException, RemoteException
        {
            this.simulator = (OTSDEVSSimulatorInterface) sim;
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
                        new OTSLine3D(pointA, pointA1), this.simulator, LongitudinalDirectionality.DIR_PLUS,
                        LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkAA2 = new CrossSectionLink(this.network, "AA2", nodeA, nodeA2, LinkType.CONNECTOR,
                        new OTSLine3D(pointA, pointA2), this.simulator, LongitudinalDirectionality.DIR_PLUS,
                        LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkAA3 = new CrossSectionLink(this.network, "AA3", nodeA, nodeA3, LinkType.CONNECTOR,
                        new OTSLine3D(pointA, pointA3), this.simulator, LongitudinalDirectionality.DIR_PLUS,
                        LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkA1B =
                        new CrossSectionLink(this.network, "A1B", nodeA1, nodeB, LinkType.ROAD, new OTSLine3D(pointA1, pointB),
                                this.simulator, LongitudinalDirectionality.DIR_PLUS, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkA2B =
                        new CrossSectionLink(this.network, "A2B", nodeA2, nodeB, LinkType.ROAD, new OTSLine3D(pointA2, pointB),
                                this.simulator, LongitudinalDirectionality.DIR_PLUS, LaneKeepingPolicy.KEEP_RIGHT);
                CrossSectionLink linkA3B =
                        new CrossSectionLink(this.network, "A3B", nodeA3, nodeB, LinkType.ROAD, new OTSLine3D(pointA3, pointB),
                                this.simulator, LongitudinalDirectionality.DIR_PLUS, LaneKeepingPolicy.KEEP_RIGHT);
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
                Stripe stripe12 = new Stripe(linkA2B, Length.createSI(1.75), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                Stripe stripe23 = new Stripe(linkA2B, Length.createSI(-1.75), Length.createSI(0.2), gtuTypes, Permeable.BOTH);
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
                origins.add(nodeA);
                List<Node> destinations = new ArrayList<>();
                destinations.add(nodeB);
                TimeVector timeVector =
                        new TimeVector(new double[] { 5, 600, 610, 1800, 3000 }, TimeUnit.BASE, StorageType.DENSE);
                ODMatrix od =
                        new ODMatrix("ODExample", origins, destinations, categorization, timeVector, Interpolation.LINEAR);
                double f = 2.0;
                FrequencyVector demand = new FrequencyVector(new double[] { 0 * f, 1000 * f, 3000 * f, 7000 * f, 0 * f },
                        FrequencyUnit.PER_HOUR, StorageType.DENSE);

                if (ODApplierExample.LANE_BASED)
                {
                    Category category = new Category(categorization, lane1, GTUType.CAR);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .4);
                    category = new Category(categorization, lane2, GTUType.CAR);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .25);
                    category = new Category(categorization, lane2, GTUType.TRUCK);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .05);
                    category = new Category(categorization, lane3, GTUType.CAR);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .1);
                    category = new Category(categorization, lane3, GTUType.TRUCK);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .2);
                }
                else
                {
                    Category category = new Category(categorization, GTUType.CAR);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .9);
                    category = new Category(categorization, GTUType.TRUCK);
                    od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .1);
                }
                // options
                MarkovCorrelation<GTUType, Frequency> markov = new MarkovCorrelation<>();
                markov.addState(GTUType.TRUCK, 0.95);
                LaneBiases biases =
                        new LaneBiases().addBias(GTUType.TRUCK, Bias.TRUCK_RIGHT).addBias(GTUType.VEHICLE, Bias.LEFT);
                ODOptions odOptions = new ODOptions().set(ODOptions.COLORER, this.colorer).set(ODOptions.MARKOV, markov)
                        .set(ODOptions.BIAS, biases).set(ODOptions.NO_LC, Length.createSI(300)).setReadOnly();
                Map<String, GeneratorObjects> generatedObjects = ODApplier.applyOD(this.network, od, this.simulator, odOptions);
                for (String str : generatedObjects.keySet())
                {
                    new GeneratorAnimation(generatedObjects.get(str).getGenerator(), this.simulator);
                }

            }
            catch (NetworkException | OTSGeometryException | NamingException | ValueException | ParameterException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
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
