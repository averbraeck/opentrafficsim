package org.opentrafficsim.draw.factory;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
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

/**
 * DefaultAnimationFactory.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DefaultAnimationFactory
{
    /**
     * Creates animations for nodes, links and lanes. This can be used if the network is not read from XML.
     * @param network OTSNetwork; the network
     * @param simulator the simulator
     * @throws OTSDrawingException on drawing error
     */
    public static void animateNetwork(final OTSNetwork network, final OTSSimulatorInterface simulator)
            throws OTSDrawingException
    {
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
                new DefaultCarAnimation((LaneBasedGTU) gtu, simulator);
            }
        }
        catch (RemoteException | NamingException | OTSGeometryException exception)
        {
            throw new OTSDrawingException("Exception while creating network animation.", exception);
        }
    }

}
