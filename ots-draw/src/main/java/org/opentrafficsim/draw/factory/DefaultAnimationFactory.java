package org.opentrafficsim.draw.factory;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuGenerator;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.core.object.ObjectInterface;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.ConflictAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.SensorAnimation;
import org.opentrafficsim.draw.road.ShoulderAnimation;
import org.opentrafficsim.draw.road.SpeedSignAnimation;
import org.opentrafficsim.draw.road.StripeAnimation;
import org.opentrafficsim.draw.road.StripeAnimation.TYPE;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.road.network.lane.object.sensor.DestinationSensor;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * DefaultAnimationFactory.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DefaultAnimationFactory implements EventListenerInterface
{
    /** the simulator. */
    private final OtsSimulatorInterface simulator;

    /** GTU colorer. */
    private final GtuColorer gtuColorer;

    /** rendered gtus. */
    private Map<LaneBasedGtu, Renderable2D<LaneBasedGtu>> animatedGTUs = Collections.synchronizedMap(new LinkedHashMap<>());

    /** rendered static objects. */
    public Map<ObjectInterface, Renderable2D<?>> animatedObjects = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Creates animations for nodes, links and lanes. The class will subscribe to the network and listen to changes, so the
     * adding and removing of GTUs and Objects is animated correctly.
     * @param network OTSNetwork; the network
     * @param gtuColorer GtuColorer; GTU colorer
     * @param animateNetwork boolean; whether to animate the current network objects
     * @throws OtsDrawingException on drawing error
     */
    protected DefaultAnimationFactory(final OtsNetwork network, final GtuColorer gtuColorer, final boolean animateNetwork)
            throws OtsDrawingException
    {
        this.simulator = network.getSimulator();
        this.gtuColorer = gtuColorer;

        // subscribe to adding and removing events
        network.addListener(this, Network.ANIMATION_GTU_ADD_EVENT);
        network.addListener(this, Network.ANIMATION_GTU_REMOVE_EVENT);
        network.addListener(this, Network.ANIMATION_OBJECT_ADD_EVENT);
        network.addListener(this, Network.ANIMATION_OBJECT_REMOVE_EVENT);
        network.addListener(this, Network.ANIMATION_GENERATOR_ADD_EVENT);
        network.addListener(this, Network.ANIMATION_GENERATOR_REMOVE_EVENT);

        // model the current infrastructure
        try
        {
            if (animateNetwork)
            {
                for (Node node : network.getNodeMap().values())
                {
                    new NodeAnimation(node, this.simulator);
                }
                for (Link link : network.getLinkMap().values())
                {
                    new LinkAnimation(link, this.simulator, 0.5f);
                    if (link instanceof CrossSectionLink)
                    {
                        for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                        {
                            if (element instanceof Lane)
                            {
                                new LaneAnimation((Lane) element, this.simulator, Color.GRAY.brighter());
                            }
                            else if (element instanceof Shoulder)
                            {
                                new ShoulderAnimation((Shoulder) element, this.simulator, Color.DARK_GRAY);
                            }
                            else if (element instanceof Stripe)
                            {
                                Stripe stripe = (Stripe) element;
                                TYPE type;
                                if (stripe.isPermeable(network.getGtuType(GtuType.DEFAULTS.CAR), LateralDirectionality.LEFT))
                                {
                                    type = stripe.isPermeable(network.getGtuType(GtuType.DEFAULTS.CAR),
                                            LateralDirectionality.RIGHT) ? TYPE.DASHED : TYPE.LEFTONLY;
                                }
                                else
                                {
                                    type = stripe.isPermeable(network.getGtuType(GtuType.DEFAULTS.CAR),
                                            LateralDirectionality.RIGHT) ? TYPE.RIGHTONLY : TYPE.SOLID;
                                }
                                new StripeAnimation((Stripe) element, this.simulator, type);
                            }
                        }
                    }
                }

                for (TrafficLight tl : network.getObjectMap(TrafficLight.class).values())
                {
                    new TrafficLightAnimation(tl, this.simulator);
                }

            }

            for (Gtu gtu : network.getGTUs())
            {
                Renderable2D<LaneBasedGtu> gtuAnimation =
                        new DefaultCarAnimation((LaneBasedGtu) gtu, this.simulator, this.gtuColorer);
                this.animatedGTUs.put((LaneBasedGtu) gtu, gtuAnimation);
            }

            for (ObjectInterface object : network.getObjectMap().values())
            {
                animateStaticObject(object);
            }
        }
        catch (RemoteException | NamingException | OtsGeometryException exception)
        {
            throw new OtsDrawingException("Exception while creating network animation.", exception);
        }

    }

    /**
     * Creates animations for nodes, links, lanes and GTUs. This can be used if the network is not read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network OTSNetwork; the network
     * @param simulator OTSSimulatorInterface; the simulator
     * @param gtuColorer GtuColorer; GTU colorer
     * @return the DefaultAnimationFactory
     * @throws OtsDrawingException on drawing error
     */
    public static DefaultAnimationFactory animateNetwork(final OtsNetwork network, final OtsSimulatorInterface simulator,
            final GtuColorer gtuColorer) throws OtsDrawingException
    {
        return new DefaultAnimationFactory(network, gtuColorer, true);
    }

    /**
     * Creates animations for nodes, links, lanes and GTUs. This can be used if the network is read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network OTSNetwork; the network
     * @param gtuColorer GtuColorer; GTU colorer
     * @return the DefaultAnimationFactory
     * @throws OtsDrawingException on drawing error
     */
    public static DefaultAnimationFactory animateXmlNetwork(final OtsNetwork network, final GtuColorer gtuColorer)
            throws OtsDrawingException
    {
        return new DefaultAnimationFactory(network, gtuColorer, false);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        try
        {
            if (event.getType().equals(Network.ANIMATION_GTU_ADD_EVENT))
            {
                // schedule the addition of the GTU to prevent it from not having an operational plan
                LaneBasedGtu gtu = (LaneBasedGtu) event.getContent();
                this.simulator.scheduleEventNow(this, this, "animateGTU", new Object[] {gtu});
            }
            else if (event.getType().equals(Network.ANIMATION_GTU_REMOVE_EVENT))
            {
                LaneBasedGtu gtu = (LaneBasedGtu) event.getContent();
                if (this.animatedGTUs.containsKey(gtu))
                {
                    this.animatedGTUs.get(gtu).destroy(gtu.getSimulator());
                    this.animatedGTUs.remove(gtu);
                }
            }
            else if (event.getType().equals(Network.ANIMATION_OBJECT_ADD_EVENT))
            {
                ObjectInterface object = (ObjectInterface) event.getContent();
                animateStaticObject(object);
            }
            else if (event.getType().equals(Network.ANIMATION_OBJECT_REMOVE_EVENT))
            {
                ObjectInterface object = (ObjectInterface) event.getContent();
                if (this.animatedObjects.containsKey(object))
                {
                    // TODO: this.animatedObjects.get(object).destroy(object.getSimulator());
                    // XXX: this is now a memory leak; we don't expect static animation objects to be removed during the run
                    this.animatedObjects.remove(object);
                }
            }
            else if (event.getType().equals(Network.ANIMATION_GENERATOR_ADD_EVENT))
            {
                GtuGenerator gtuGenerator = (GtuGenerator) event.getContent();
                animateGtuGenerator(gtuGenerator);
            }
            else if (event.getType().equals(Network.ANIMATION_GENERATOR_REMOVE_EVENT))
            {
                // TODO: change the way generators are animated
            }
        }
        catch (SimRuntimeException exception)
        {
            CategoryLogger.always().error(exception, "Exception while updating network animation.");
        }
    }

    /**
     * Draw the GTU (scheduled method).
     * @param gtu LaneBasedGtu; the GTU to draw
     */
    protected void animateGTU(final LaneBasedGtu gtu)
    {
        try
        {
            Renderable2D<LaneBasedGtu> gtuAnimation = new DefaultCarAnimation(gtu, this.simulator, this.gtuColorer);
            this.animatedGTUs.put(gtu, gtuAnimation);
        }
        catch (RemoteException | NamingException exception)
        {
            gtu.getSimulator().getLogger().always().error(exception, "Exception while drawing GTU.");
        }
    }

    /**
     * Draw the static object.
     * @param object ObjectInterface; the object to draw
     */
    protected void animateStaticObject(final ObjectInterface object)
    {
        try
        {
            if (object instanceof SinkSensor)
            {
                SinkSensor sensor = (SinkSensor) object;
                // Renderable2D<SinkSensor> objectAnimation = new SinkAnimation(sensor, this.simulator);
                Renderable2D<SingleSensor> objectAnimation =
                        new SensorAnimation(sensor, sensor.getLongitudinalPosition(), this.simulator, Color.YELLOW);
                this.animatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof DestinationSensor)
            {
                DestinationSensor sensor = (DestinationSensor) object;
                // Renderable2D<DestinationSensor> objectAnimation = new DestinationAnimation(sensor, this.simulator);
                Renderable2D<SingleSensor> objectAnimation =
                        new SensorAnimation(sensor, sensor.getLongitudinalPosition(), this.simulator, Color.ORANGE);
                this.animatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof SingleSensor)
            {
                SingleSensor sensor = (SingleSensor) object;
                Renderable2D<SingleSensor> objectAnimation =
                        new SensorAnimation(sensor, sensor.getLongitudinalPosition(), this.simulator, Color.GREEN);
                this.animatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof Conflict)
            {
                Conflict conflict = (Conflict) object;
                Renderable2D<Conflict> objectAnimation = new ConflictAnimation(conflict, this.simulator);
                this.animatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof SpeedSign)
            {
                SpeedSign speedSign = (SpeedSign) object;
                Renderable2D<SpeedSign> objectAnimation = new SpeedSignAnimation(speedSign, this.simulator);
                this.animatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof BusStop)
            {
                BusStop busStop = (BusStop) object;
                Renderable2D<BusStop> objectAnimation = new BusStopAnimation(busStop, this.simulator);
                this.animatedObjects.put(object, objectAnimation);
            }
        }
        catch (RemoteException | NamingException exception)
        {
            CategoryLogger.always().error(exception, "Exception while drawing Object of class ObjectInterface.");
        }
    }

    /**
     * Draw the GtuGenerator.
     * @param gtuGenerator GtuGenerator; the GtuGenerator to draw
     */
    protected void animateGtuGenerator(final GtuGenerator gtuGenerator)
    {
        // TODO: default animation of GTU generator
    }

}
