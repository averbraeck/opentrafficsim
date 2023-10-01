package org.opentrafficsim.animation;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.animation.data.AnimationBusStopData;
import org.opentrafficsim.animation.data.AnimationConflictData;
import org.opentrafficsim.animation.data.AnimationCrossSectionElementData;
import org.opentrafficsim.animation.data.AnimationGtuData;
import org.opentrafficsim.animation.data.AnimationGtuGeneratorPositionData;
import org.opentrafficsim.animation.data.AnimationLaneData;
import org.opentrafficsim.animation.data.AnimationLaneDetectorData;
import org.opentrafficsim.animation.data.AnimationLinkData;
import org.opentrafficsim.animation.data.AnimationNodeData;
import org.opentrafficsim.animation.data.AnimationShoulderData;
import org.opentrafficsim.animation.data.AnimationSpeedSignData;
import org.opentrafficsim.animation.data.AnimationStripeData;
import org.opentrafficsim.animation.data.AnimationTrafficLightData;
import org.opentrafficsim.animation.data.AnimationTrafficLightDetectorData;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuGenerator.GtuGeneratorPosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.object.LocatedObject;
import org.opentrafficsim.core.object.NonLocatedObject;
import org.opentrafficsim.draw.OtsDrawingException;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.draw.road.ConflictAnimation;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.LaneDetectorAnimation;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.LaneDetectorData;
import org.opentrafficsim.draw.road.SpeedSignAnimation;
import org.opentrafficsim.draw.road.SpeedSignAnimation.SpeedSignData;
import org.opentrafficsim.draw.road.StripeAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.draw.road.TrafficLightDetectorAnimation;
import org.opentrafficsim.draw.road.TrafficLightDetectorAnimation.TrafficLightDetectorData;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * DefaultAnimationFactory.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultAnimationFactory implements EventListener
{
    /** */
    private static final long serialVersionUID = 20230129L;

    /** The network. */
    private final Network network;

    /** The simulator. */
    private final OtsSimulatorInterface simulator;

    /** GTU colorer. */
    private final GtuColorer gtuColorer;

    /** Rendered gtus. */
    private Map<LaneBasedGtu, Renderable2d<GtuData>> animatedGTUs = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Rendered located objects. */
    private Map<Locatable, Renderable2d<?>> animatedLocatedObjects = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Rendered non-located objects. */
    private Map<NonLocatedObject, Renderable2d<?>> animatedNonLocatedObjects =
            Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Creates animations for nodes, links and lanes. The class will subscribe to the network and listen to changes, so the
     * adding and removing of GTUs and Objects is animated correctly.
     * @param network Network; the network
     * @param gtuColorer GtuColorer; GTU colorer
     * @param animateNetwork boolean; whether to animate the current network objects
     * @throws OtsDrawingException on drawing error
     */
    protected DefaultAnimationFactory(final Network network, final GtuColorer gtuColorer, final boolean animateNetwork)
            throws OtsDrawingException
    {
        this.network = network;
        this.simulator = network.getSimulator();
        this.gtuColorer = gtuColorer;

        // subscribe to adding and removing events
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
        network.addListener(this, Network.OBJECT_ADD_EVENT);
        network.addListener(this, Network.OBJECT_REMOVE_EVENT);
        network.addListener(this, Network.NONLOCATED_OBJECT_ADD_EVENT);
        network.addListener(this, Network.NONLOCATED_OBJECT_REMOVE_EVENT);

        // model the current infrastructure
        try
        {
            if (animateNetwork)
            {
                for (Node node : network.getNodeMap().values())
                {
                    new NodeAnimation(new AnimationNodeData(node), this.simulator);
                }
                for (Link link : network.getLinkMap().values())
                {
                    new LinkAnimation(new AnimationLinkData(link), this.simulator, 0.5f);
                    if (link instanceof CrossSectionLink)
                    {
                        for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                        {
                            if (element instanceof Lane)
                            {
                                Lane lane = (Lane) element;
                                new LaneAnimation(new AnimationLaneData(lane), this.simulator, Color.GRAY.brighter());
                            }
                            else if (element instanceof Stripe)
                            {
                                Stripe stripe = (Stripe) element;
                                new StripeAnimation(new AnimationStripeData(stripe), this.simulator);
                            }
                            else if (element instanceof Shoulder)
                            {
                                Shoulder shoulder = (Shoulder) element;
                                new CrossSectionElementAnimation(new AnimationShoulderData(shoulder), this.simulator,
                                        Color.DARK_GRAY);
                            }
                            else
                            {
                                new CrossSectionElementAnimation(new AnimationCrossSectionElementData<>(element),
                                        this.simulator, Color.DARK_GRAY);
                            }
                        }
                    }
                }
            }

            for (Gtu gtu : network.getGTUs())
            {
                GtuData gtuData = new AnimationGtuData(this.gtuColorer, (LaneBasedGtu) gtu);
                Renderable2d<GtuData> gtuAnimation = new DefaultCarAnimation(gtuData, this.simulator);
                this.animatedGTUs.put((LaneBasedGtu) gtu, gtuAnimation);
            }

            for (LocatedObject object : network.getObjectMap().values())
            {
                animateLocatedObject(object);
            }

            for (NonLocatedObject object : network.getNonLocatedObjectMap().values())
            {
                animateNonLocatedObject(object);
            }
        }
        catch (RemoteException | NamingException exception)
        {
            throw new OtsDrawingException("Exception while creating network animation.", exception);
        }

    }

    /**
     * Creates animations for nodes, links, lanes and GTUs. This can be used if the network is not read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network Network; the network
     * @param contextualized Contextualized; context provider
     * @param gtuColorer GtuColorer; GTU colorer
     * @return the DefaultAnimationFactory
     * @throws OtsDrawingException on drawing error
     */
    public static DefaultAnimationFactory animateNetwork(final Network network, final Contextualized contextualized,
            final GtuColorer gtuColorer) throws OtsDrawingException
    {
        return new DefaultAnimationFactory(network, gtuColorer, true);
    }

    /**
     * Creates animations for nodes, links, lanes and GTUs. This can be used if the network is read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network Network; the network
     * @param gtuColorer GtuColorer; GTU colorer
     * @return the DefaultAnimationFactory
     * @throws OtsDrawingException on drawing error
     */
    public static DefaultAnimationFactory animateXmlNetwork(final Network network, final GtuColorer gtuColorer)
            throws OtsDrawingException
    {
        return new DefaultAnimationFactory(network, gtuColorer, false);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        try
        {
            if (event.getType().equals(Network.GTU_ADD_EVENT))
            {
                // schedule the addition of the GTU to prevent it from not having an operational plan
                LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU((String) event.getContent());
                this.simulator.scheduleEventNow(this, "animateGTU", new Object[] {gtu});
            }
            else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
            {
                LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU((String) event.getContent());
                if (this.animatedGTUs.containsKey(gtu))
                {
                    this.animatedGTUs.get(gtu).destroy(gtu.getSimulator());
                    this.animatedGTUs.remove(gtu);
                }
            }
            else if (event.getType().equals(Network.OBJECT_ADD_EVENT))
            {
                LocatedObject object = this.network.getObjectMap().get((String) event.getContent());
                animateLocatedObject(object);
            }
            else if (event.getType().equals(Network.OBJECT_REMOVE_EVENT))
            {
                LocatedObject object = this.network.getObjectMap().get((String) event.getContent());
                // TODO: this.animatedObjects.get(object).destroy(object.getSimulator());
                // XXX: this is now a memory leak; we don't expect static animation objects to be removed during the run
                this.animatedLocatedObjects.remove(object);
            }
            else if (event.getType().equals(Network.NONLOCATED_OBJECT_ADD_EVENT))
            {
                NonLocatedObject object = this.network.getNonLocatedObjectMap().get((String) event.getContent());
                animateNonLocatedObject(object);
            }
            else if (event.getType().equals(Network.NONLOCATED_OBJECT_REMOVE_EVENT))
            {
                NonLocatedObject object = this.network.getNonLocatedObjectMap().get((String) event.getContent());
                // TODO: this.animatedObjects.get(object).destroy(object.getSimulator());
                // XXX: this is now a memory leak; we don't expect static animation objects to be removed during the run
                this.animatedNonLocatedObjects.remove(object);
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
            GtuData gtuData = new AnimationGtuData(this.gtuColorer, gtu);
            Renderable2d<GtuData> gtuAnimation = new DefaultCarAnimation(gtuData, this.simulator);
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
    protected void animateLocatedObject(final LocatedObject object)
    {
        try
        {
            if (object instanceof SinkDetector)
            {
                SinkDetector detector = (SinkDetector) object;
                // Renderable2d<SinkSensor> objectAnimation = new SinkAnimation(detector, this.simulator);
                Renderable2d<LaneDetectorData> objectAnimation =
                        new LaneDetectorAnimation(new AnimationLaneDetectorData(detector), this.simulator, Color.ORANGE);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof TrafficLightDetector)
            {
                TrafficLightDetector trafficLigthDetector = (TrafficLightDetector) object;
                Renderable2d<TrafficLightDetectorData> objectAnimation = new TrafficLightDetectorAnimation(
                        new AnimationTrafficLightDetectorData(trafficLigthDetector), this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof TrafficLightDetector.StartEndDetector)
            {
                // we do not draw these, as we draw the TrafficLightDetector
                return;
            }
            else if (object instanceof LaneDetector)
            {
                LaneDetector detector = (LaneDetector) object;
                Renderable2d<LaneDetectorData> objectAnimation =
                        new LaneDetectorAnimation(new AnimationLaneDetectorData(detector), this.simulator, Color.BLACK);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof Conflict)
            {
                Conflict conflict = (Conflict) object;
                Renderable2d<ConflictData> objectAnimation =
                        new ConflictAnimation(new AnimationConflictData(conflict), this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof TrafficLight)
            {
                TrafficLight trafficLight = (TrafficLight) object;
                Renderable2d<TrafficLightData> objectAnimation =
                        new TrafficLightAnimation(new AnimationTrafficLightData(trafficLight), this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof SpeedSign)
            {
                SpeedSign speedSign = (SpeedSign) object;
                Renderable2d<SpeedSignData> objectAnimation =
                        new SpeedSignAnimation(new AnimationSpeedSignData(speedSign), this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof BusStop)
            {
                BusStop busStop = (BusStop) object;
                Renderable2d<BusStopData> objectAnimation =
                        new BusStopAnimation(new AnimationBusStopData(busStop), this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
        }
        catch (RemoteException | NamingException exception)
        {
            CategoryLogger.always().error(exception, "Exception while drawing Object of class LocatedObject.");
        }
    }

    /**
     * Draw non-located objects.
     * @param object NonLocatedObject; the object to draw.
     */
    protected void animateNonLocatedObject(final NonLocatedObject object)
    {
        try
        {
            if (object instanceof LaneBasedGtuGenerator)
            {
                LaneBasedGtuGenerator generator = (LaneBasedGtuGenerator) object;
                for (GtuGeneratorPosition position : generator.getPositions())
                {
                    Renderable2d<GtuGeneratorPositionData> objectAnimation =
                            new GtuGeneratorPositionAnimation(new AnimationGtuGeneratorPositionData(position), this.simulator);
                    this.animatedLocatedObjects.put(position, objectAnimation);
                }
            }
        }
        catch (RemoteException | NamingException exception)
        {
            CategoryLogger.always().error(exception, "Exception while drawing Object of class NonLocatedObject.");
        }
    }

}
