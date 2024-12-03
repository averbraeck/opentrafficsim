package org.opentrafficsim.road.network.lane;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.geometry.DirectionalPolyLine;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.network.lane.StripeData.StripePhaseSync;

/**
 * Stripe road marking. This class only contains functional information. There is no information on how to draw the stripe, i.e.
 * no color and no information on dashes. The stripe types has information on this, but this only serves as a default towards
 * classes that do draw a stripe.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Stripe extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** Stripe data. */
    private final StripeData data;

    /** Longitudinal offset of dashes. */
    private Length dashOffset = Length.ZERO;

    /** Link reference line. */
    private PolyLine2d linkReferenceLine = null;

    /**
     * Constructor specifying geometry. Permeability is set according to the stripe type default.
     * @param id id
     * @param data stripe data, including permeability, stripe elements and dash synchronization
     * @param link link
     * @param geometry geometry
     */
    public Stripe(final String id, final StripeData data, final CrossSectionLink link, final CrossSectionGeometry geometry)
    {
        super(link, id, geometry);
        Throw.whenNull(data, "Type may not be null.");
        this.data = data;
    }

    /**
     * Add lateral permeability for a GTU type. This overrules overall stripe permeability. Add NONE to prevent lane changes.
     * Add both LEFT and RIGHT in two calls, to enable lane changes. Add LEFT or RIGHT to enable one direction while prohibiting
     * the other.
     * @param gtuType GTU type to add permeability for
     * @param lateralDirection direction to add compared to the direction of the design line
     */
    public void addPermeability(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        this.data.addPermeability(gtuType, lateralDirection);
    }

    /**
     * Returns whether the given GTU type is allowed to cross the line in the given lateral direction.
     * @param gtuType GTU type to look for.
     * @param lateralDirection direction to look for (LEFT or RIGHT) compared to the direction of the design line.
     * @return whether the road marker is permeable for the GTU type.
     */
    public final boolean isPermeable(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        return this.data.isPermeable(gtuType, lateralDirection);
    }

    /**
     * Returns the elements.
     * @return elements
     */
    public List<StripeElement> getElements()
    {
        return this.data.getElements();
    }

    /**
     * Sets the dash offset.
     * @param dashOffset dash offset
     */
    public void setDashOffset(final Length dashOffset)
    {
        this.dashOffset = dashOffset;
    }

    /**
     * Returns the dash offset.
     * @return dash offset
     */
    public Length getDashOffset()
    {
        return this.dashOffset;
    }

    /**
     * Sets the lateral synchronization.
     * @param lateralSync lateral synchronization
     */
    public void setLateralSync(final StripeLateralSync lateralSync)
    {
        this.data.setLateralSync(lateralSync);
    }

    /**
     * Returns the lateral synchronization.
     * @return lateral synchronization
     */
    public StripeLateralSync getLateralSync()
    {
        return this.data.getLateralSync();
    }

    /**
     * Sets the phase synchronization.
     * @param phaseSync phase synchronization
     */
    public void setPhaseSync(final StripePhaseSync phaseSync)
    {
        this.data.setPhaseSync(phaseSync);
    }

    /**
     * Returns the phase synchronization.
     * @return phase synchronization
     */
    public StripePhaseSync getPhaseSync()
    {
        return this.data.getPhaseSync();
    }

    /**
     * Returns the period of the common dash pattern.
     * @return period of the common dash pattern
     */
    public double getPeriod()
    {
        return this.data.getPeriod();
    }

    /**
     * Return link reference line, which is the line halfway between the left-most and right-most stripes.
     * @return link reference line
     */
    public PolyLine2d getLinkReferenceLine()
    {
        if (this.linkReferenceLine == null)
        {
            PolyLine2d linkLine = getLink().getDesignLine();
            double offsetMin0 = Double.POSITIVE_INFINITY;
            double offsetMax0 = Double.NEGATIVE_INFINITY;
            double offsetMin1 = Double.POSITIVE_INFINITY;
            double offsetMax1 = Double.NEGATIVE_INFINITY;
            for (CrossSectionElement element : getLink().getCrossSectionElementList())
            {
                if (element instanceof Stripe)
                {
                    offsetMin0 = Math.min(offsetMin0, element.getOffsetAtBegin().si);
                    offsetMax0 = Math.max(offsetMax0, element.getOffsetAtBegin().si);
                    offsetMin1 = Math.min(offsetMin1, element.getOffsetAtEnd().si);
                    offsetMax1 = Math.max(offsetMax1, element.getOffsetAtEnd().si);
                }
            }
            DirectionalPolyLine directionalLine = new DirectionalPolyLine(linkLine, getLink().getStartNode().getHeading(),
                    getLink().getEndNode().getHeading());
            PolyLine2d start = directionalLine.directionalOffsetLine(.5 * (offsetMin0 + offsetMax0));
            PolyLine2d end = directionalLine.directionalOffsetLine(.5 * (offsetMin1 + offsetMax1));
            this.linkReferenceLine = start.transitionLine(end, (f) -> f);
        }
        return this.linkReferenceLine;
    }

    @Override
    public String toString()
    {
        return "Stripe [id=" + this.getFullId() + "]";
    }

}
