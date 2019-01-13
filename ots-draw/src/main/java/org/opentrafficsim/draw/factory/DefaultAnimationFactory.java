package org.opentrafficsim.draw.factory;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.ShoulderAnimation;
import org.opentrafficsim.draw.road.StripeAnimation;
import org.opentrafficsim.draw.road.StripeAnimation.TYPE;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * DefaultAnimationFactory.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DefaultAnimationFactory implements EventListenerInterface
{
    /** the simulator. */
    private final OTSSimulatorInterface simulator;

    /** rendered gtus. */
    private Map<LaneBasedGTU, Renderable2D<LaneBasedGTU>> animatedGTUs = new HashMap<>();

    /**
     * Creates animations for nodes, links and lanes. This can be used if the network is not read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network OTSNetwork; the network
     * @param simulator the simulator
     * @throws OTSDrawingException on drawing error
     */
    protected DefaultAnimationFactory(final OTSNetwork network, final OTSSimulatorInterface simulator) throws OTSDrawingException
    {
        this.simulator = simulator;

        // subscribe to adding and removing events
        network.addListener(this, Network.ANIMATION_GTU_ADD_EVENT);
        network.addListener(this, Network.ANIMATION_GTU_REMOVE_EVENT);
        network.addListener(this, Network.ANIMATION_OBJECT_ADD_EVENT);
        network.addListener(this, Network.ANIMATION_OBJECT_REMOVE_EVENT);

        // model the current infrastructure
        try
        {
            for (Node node : network.getNodeMap().values())
            {
                new NodeAnimation(node, simulator);
            }
            for (Link link : network.getLinkMap().values())
            {
                new LinkAnimation(link, simulator, 0.5f);
                if (link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (element instanceof Lane)
                        {
                            new LaneAnimation((Lane) element, simulator, Color.GRAY.brighter(), false);
                        }
                        else if (element instanceof Shoulder)
                        {
                            new ShoulderAnimation((Shoulder) element, simulator, Color.DARK_GRAY);
                        }
                        else if (element instanceof Stripe)
                        {
                            Stripe stripe = (Stripe) element;
                            TYPE type;
                            if (stripe.isPermeable(GTUType.CAR, LateralDirectionality.LEFT))
                            {
                                type = stripe.isPermeable(GTUType.CAR, LateralDirectionality.RIGHT) ? TYPE.DASHED
                                        : TYPE.LEFTONLY;
                            }
                            else
                            {
                                type = stripe.isPermeable(GTUType.CAR, LateralDirectionality.RIGHT) ? TYPE.RIGHTONLY
                                        : TYPE.SOLID;
                            }
                            new StripeAnimation((Stripe) element, simulator, type);
                        }
                    }
                }
            }
            for (GTU gtu : network.getGTUs())
            {
                Renderable2D<LaneBasedGTU> gtuAnimation = new DefaultCarAnimation((LaneBasedGTU) gtu, simulator);
                this.animatedGTUs.put((LaneBasedGTU) gtu, gtuAnimation);
            }
        }
        catch (RemoteException | NamingException | OTSGeometryException exception)
        {
            throw new OTSDrawingException("Exception while creating network animation.", exception);
        }
    }
    
    /**
     * Creates animations for nodes, links and lanes. This can be used if the network is not read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network OTSNetwork; the network
     * @param simulator the simulator
     * @throws OTSDrawingException on drawing error
     */
    public static void animateNetwork(final OTSNetwork network, final OTSSimulatorInterface simulator) throws OTSDrawingException
    {
        new DefaultAnimationFactory(network, simulator);
    }
    

    /** {@inheritDoc} */
    @Override
    public void notify(EventInterface event) throws RemoteException
    {
        try
        {
            if (event.getType().equals(Network.ANIMATION_GTU_ADD_EVENT))
            {
                // schedule the addition of the GTU to prevent it from not having an operational plan
                LaneBasedGTU gtu = (LaneBasedGTU) event.getContent();
                this.simulator.scheduleEventNow(this, this, "animateGTU", new Object[] {gtu});
            }
            else if (event.getType().equals(Network.ANIMATION_GTU_REMOVE_EVENT))
            {
                LaneBasedGTU gtu = (LaneBasedGTU) event.getContent();
                if (this.animatedGTUs.containsKey(gtu))
                {
                    this.animatedGTUs.get(gtu).destroy(); 
                    this.animatedGTUs.remove(gtu);
                }
            }
            else if (event.getType().equals(Network.ANIMATION_OBJECT_ADD_EVENT))
            {
                // TODO ANIMATION_OBJECT_ADD_EVENT
            }
            else if (event.getType().equals(Network.ANIMATION_OBJECT_REMOVE_EVENT))
            {
                // TODO ANIMATION_OBJECT_REMOVE_EVENT
            }
        }
        catch (NamingException | SimRuntimeException exception)
        {
            SimLogger.always().error(exception, "Exception while updating network animation.");
        }
    }

    /**
     * Draw the GTU (scheduled method).
     * @param gtu the GTU to draw
     */
    protected void animateGTU(LaneBasedGTU gtu)
    {
        try
        {
            Renderable2D<LaneBasedGTU> gtuAnimation = new DefaultCarAnimation(gtu, this.simulator);
            this.animatedGTUs.put(gtu, gtuAnimation);
        }
        catch (RemoteException | NamingException exception)
        {
            SimLogger.always().error(exception, "Exception while drawing GTU.");
        }
    }
}
