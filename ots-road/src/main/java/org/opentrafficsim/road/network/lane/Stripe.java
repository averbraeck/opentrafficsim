package org.opentrafficsim.road.network.lane;

import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Longitudinal road stripes; simple constructors.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Stripe extends RoadMarkerAlong
{
    /** */
    private static final long serialVersionUID = 20151025L;

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs
     * @param lateralCenterPositionStart Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param lateralCenterPositionEnd Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param width Length; positioned &lt;i&gt;symmetrically around&lt;/i&gt; the center line given by the
     *            lateralCenterPosition.
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length lateralCenterPositionStart,
            final Length lateralCenterPositionEnd, final Length width, final boolean fixGradualLateralOffset)
            throws OtsGeometryException, NetworkException
    {
        super(parentLink, lateralCenterPositionStart, lateralCenterPositionEnd, width, width, fixGradualLateralOffset);
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs
     * @param lateralCenterPositionStart Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param lateralCenterPositionEnd Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param width Length; positioned &lt;i&gt;symmetrically around&lt;/i&gt; the center line given by the
     *            lateralCenterPosition.
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length lateralCenterPositionStart,
            final Length lateralCenterPositionEnd, final Length width) throws OtsGeometryException, NetworkException
    {
        this(parentLink, lateralCenterPositionStart, lateralCenterPositionEnd, width, false);
    }

    /**
     * Helper constructor that immediately provides permeability for a number of GTU classes.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs
     * @param lateralCenterPositionStart Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param lateralCenterPositionEnd Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param width Length; positioned &lt;i&gt;symmetrically around&lt;/i&gt; the center line given by the
     *            lateralCenterPosition
     * @param gtuTypes Set&lt;GtuType&gt;; the GTU types for which the permeability is defined
     * @param permeable Permeable; one of the enums of Stripe.Permeable to define the permeability
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length lateralCenterPositionStart,
            final Length lateralCenterPositionEnd, final Length width, final Set<GtuType> gtuTypes, final Permeable permeable,
            final boolean fixGradualLateralOffset) throws OtsGeometryException, NetworkException
    {
        super(parentLink, lateralCenterPositionStart, lateralCenterPositionEnd, width, width, fixGradualLateralOffset);
        for (GtuType gtuType : gtuTypes)
        {
            addPermeability(gtuType, permeable);
        }
    }

    /**
     * Helper constructor that immediately provides permeability for a number of GTU classes.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs
     * @param lateralCenterPositionStart Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param lateralCenterPositionEnd Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link
     * @param width Length; positioned &lt;i&gt;symmetrically around&lt;/i&gt; the center line given by the
     *            lateralCenterPosition
     * @param gtuTypes Set&lt;GtuType&gt;; the GTU types for which the permeability is defined
     * @param permeable Permeable; one of the enums of Stripe.Permeable to define the permeability
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length lateralCenterPositionStart,
            final Length lateralCenterPositionEnd, final Length width, final Set<GtuType> gtuTypes, final Permeable permeable)
            throws OtsGeometryException, NetworkException
    {
        this(parentLink, lateralCenterPositionStart, lateralCenterPositionEnd, width, gtuTypes, permeable, false);
    }

    /**
     * Helper constructor that immediately provides permeability for all GTU classes.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; The offsets and widths at positions along the line, relative to
     *            the design line of the parent link. If there is just one with and offset, there should just be one element in
     *            the list with Length = 0. If there are more slices, the last one should be at the length of the design line.
     *            If not, a NetworkException is thrown.
     * @param permeable Permeable; one of the enums of Stripe.Permeable to define the permeability
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final List<CrossSectionSlice> crossSectionSlices,
            final Permeable permeable) throws OtsGeometryException, NetworkException
    {
        super(parentLink, crossSectionSlices);
        addPermeability(parentLink.getNetwork().getGtuType(GtuType.DEFAULTS.VEHICLE), permeable);
        addPermeability(parentLink.getNetwork().getGtuType(GtuType.DEFAULTS.PEDESTRIAN), permeable);
    }

    /**
     * @param gtuType GtuType; GTU type to add permeability for.
     * @param permeable Permeable; direction(s) to add compared to the direction of the design line.
     */
    public final void addPermeability(final GtuType gtuType, final Permeable permeable)
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

}
