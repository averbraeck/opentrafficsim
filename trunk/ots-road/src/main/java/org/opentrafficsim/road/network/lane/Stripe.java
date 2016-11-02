package org.opentrafficsim.road.network.lane;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.animation.StripeAnimation;

/**
 * Longitudinal road stripes; simple constructors.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Oct 25, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Stripe extends RoadMarkerAlong
{
    /** */
    private static final long serialVersionUID = 20151025L;

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink Cross Section Link to which the element belongs
     * @param lateralCenterPosition the lateral start position compared to the linear geometry of the Cross Section Link
     * @param width positioned <i>symmetrically around</i> the center line given by the lateralCenterPosition.
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length lateralCenterPosition, final Length width)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, lateralCenterPosition, width);
    }

    /**
     * Helper constructor that immediately provides permeability for a number of GTU classes.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink Cross Section Link to which the element belongs
     * @param lateralCenterPosition the lateral start position compared to the linear geometry of the Cross Section Link
     * @param width positioned <i>symmetrically around</i> the center line given by the lateralCenterPosition
     * @param gtuTypes the GTU types for which the permeability is defined
     * @param permeable one of the enums of Stripe.Permeable to define the permeability
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length lateralCenterPosition, final Length width,
            final Set<GTUType> gtuTypes, final Permeable permeable) throws OTSGeometryException, NetworkException
    {
        super(parentLink, lateralCenterPosition, width);
        for (GTUType gtuType : gtuTypes)
        {
            addPermeability(gtuType, permeable);
        }
    }

    /**
     * Helper constructor that immediately provides permeability for all GTU classes.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink Cross Section Link to which the element belongs
     * @param crossSectionSlices The offsets and widths at positions along the line, relative to the design line of the parent
     *            link. If there is just one with and offset, there should just be one element in the list with Length = 0. If
     *            there are more slices, the last one should be at the length of the design line. If not, a NetworkException is
     *            thrown.
     * @param permeable one of the enums of Stripe.Permeable to define the permeability
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final List<CrossSectionSlice> crossSectionSlices,
            final Permeable permeable) throws OTSGeometryException, NetworkException
    {
        super(parentLink, crossSectionSlices);
        addPermeability(GTUType.ALL, permeable);
    }

    /**
     * Clone a Stripe for a new network.
     * @param newParentLink the new link to which the clone belongs
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not
     * @param cse the element to clone from
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    protected Stripe(final CrossSectionLink newParentLink, final OTSSimulatorInterface newSimulator,
            final boolean animation, final Stripe cse) throws NetworkException
    {
        super(newParentLink, newSimulator, animation, cse);
    }

    /**
     * @param gtuType GTU type to add permeability for.
     * @param permeable direction(s) to add compared to the direction of the design line.
     */
    public final void addPermeability(final GTUType gtuType, final Permeable permeable)
    {
        if (permeable.equals(Permeable.LEFT) || permeable.equals(Permeable.BOTH))
        {
            addPermeability(gtuType, LateralDirectionality.LEFT);
        }
        if (permeable.equals(Permeable.RIGHT) || permeable.equals(Permeable.BOTH))
        {
            addPermeability(gtuType, LateralDirectionality.RIGHT);
        }
    }

    /** The types of permeability of a stripe. */
    public enum Permeable
    {
        /** Permeable in the positive lateral direction compared to the design line direction. */
        LEFT,
        /** Permeable in the negative lateral direction compared to the design line direction. */
        RIGHT,
        /** Permeable in both directions. */
        BOTH;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("Stripe offset %.2fm..%.2fm, width %.2fm..%.2fm", getDesignLineOffsetAtBegin().getSI(),
                getDesignLineOffsetAtEnd().getSI(), getBeginWidth().getSI(), getEndWidth().getSI());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Stripe clone(final CrossSectionLink newParentLink, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException
    {
        try
        {
            Stripe newStripe = new Stripe(newParentLink, newSimulator, animation, this);

            if (animation)
            {
                // estimate the animation...
                Permeable permeable = null;
                for (GTUType gtuType : newStripe.getPermeabilityMap().keySet())
                {
                    for (LateralDirectionality dir : newStripe.getPermeabilityMap().get(gtuType))
                    {
                        if (dir.isLeft())
                        {
                            if (permeable == null)
                            {
                                permeable = Permeable.LEFT;
                            }
                            if (permeable.equals(Permeable.RIGHT))
                            {
                                permeable = Permeable.BOTH;
                            }
                        }
                        if (dir.isRight())
                        {
                            if (permeable == null)
                            {
                                permeable = Permeable.RIGHT;
                            }
                            if (permeable.equals(Permeable.LEFT))
                            {
                                permeable = Permeable.BOTH;
                            }
                        }
                    }
                }
                if (permeable == null)
                {
                    new StripeAnimation(newStripe, newSimulator, StripeAnimation.TYPE.SOLID);
                }
                else
                {
                    switch (permeable)
                    {
                        case BOTH:
                            new StripeAnimation(newStripe, newSimulator, StripeAnimation.TYPE.DASHED);
                            break;

                        case LEFT:
                            new StripeAnimation(newStripe, newSimulator, StripeAnimation.TYPE.LEFTONLY);
                            break;

                        case RIGHT:
                            new StripeAnimation(newStripe, newSimulator, StripeAnimation.TYPE.RIGHTONLY);
                            break;

                        default:
                            break;
                    }
                }
            }

            return newStripe;
        }
        catch (NamingException | RemoteException | OTSGeometryException exception)
        {
            throw new NetworkException(exception);
        }
    }

}
