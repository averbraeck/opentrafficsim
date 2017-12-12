package org.opentrafficsim.road.gtu.strategical.od;

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
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.animation.AnimationToggles;
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
            streams.put("generation", new MersenneTwister(100L));
            this.simulator.getReplication().setStreams(streams);

            this.network = new OTSNetwork("ODApplierExample");
            try
            {
                // Network
                OTSPoint3D pointA = new OTSPoint3D(0, 0, 0);
                OTSPoint3D pointB = new OTSPoint3D(1000, 0, 0);
                OTSNode nodeA = new OTSNode(this.network, "A", pointA);
                OTSNode nodeB = new OTSNode(this.network, "B", pointB);
                CrossSectionLink linkAB =
                        new CrossSectionLink(this.network, "AB", nodeA, nodeB, LinkType.ROAD, new OTSLine3D(pointA, pointB),
                                this.simulator, LongitudinalDirectionality.DIR_PLUS, LaneKeepingPolicy.KEEP_RIGHT);
                Lane lane1 = new Lane(linkAB, "lane1", Length.createSI(1.75), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Lane lane2 = new Lane(linkAB, "lane2", Length.createSI(-1.75), Length.createSI(3.5), LaneType.HIGHWAY,
                        new Speed(120, SpeedUnit.KM_PER_HOUR), new OvertakingConditions.LeftOnly());
                Set<GTUType> gtuTypes = new HashSet<>();
                gtuTypes.add(GTUType.VEHICLE);
                Stripe stripe12 = new Stripe(linkAB, Length.ZERO, Length.createSI(0.2), gtuTypes, Permeable.BOTH);
                // animation
                new NodeAnimation(nodeA, this.simulator);
                new NodeAnimation(nodeB, this.simulator);
                new LinkAnimation(linkAB, this.simulator, 0.5f);
                new LaneAnimation(lane1, this.simulator, Color.GRAY.brighter(), false);
                new LaneAnimation(lane2, this.simulator, Color.GRAY.brighter(), false);
                new StripeAnimation(stripe12, this.simulator, TYPE.DASHED);
                new SinkSensor(lane1, Length.createSI(900), this.simulator);
                new SinkSensor(lane2, Length.createSI(900), this.simulator);

                // OD
                Categorization categorization = new Categorization("ODExample", Lane.class, GTUType.class, Route.class);
                List<Node> origins = new ArrayList<>();
                origins.add(nodeA);
                List<Node> destinations = new ArrayList<>();
                destinations.add(nodeB);
                TimeVector timeVector =
                        new TimeVector(new double[] { 5, 600, 610, 1800, 3000 }, TimeUnit.BASE, StorageType.DENSE);
                ODMatrix od =
                        new ODMatrix("ODExample", origins, destinations, categorization, timeVector, Interpolation.LINEAR);
                FrequencyVector demand =
                        new FrequencyVector(new double[] { 0, 1000, 3000, 4000, 0 }, FrequencyUnit.PER_HOUR, StorageType.DENSE);
                Route route = new Route("AB").addNode(nodeA).addNode(nodeB);
                Category category = new Category(categorization, lane1, GTUType.CAR, route);
                od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .6);
                category = new Category(categorization, lane2, GTUType.CAR, route);
                od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .2);
                category = new Category(categorization, lane2, GTUType.TRUCK, route);
                od.putDemandVector(nodeA, nodeB, category, demand, timeVector, Interpolation.LINEAR, .2);
                // options
                ODOptions odOptions = new ODOptions().set(ODOptions.COLORER, this.colorer).setReadOnly();
                ODApplier.applyOD(this.network, od, this.simulator, odOptions);
                

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
