package org.opentrafficsim.road.network.lane;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Stripe extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** Lateral permeability per GTU type and direction. */
    private final Map<GtuType, Set<LateralDirectionality>> permeabilityMap = new LinkedHashMap<>();

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param startCenterPosition Length; the lateral start position compared to the linear geometry of the Cross Section Link
     *            at the start of the road marker.
     * @param endCenterPosition Length; the lateral end position compared to the linear geometry of the Cross Section Link at
     *            the end of the road marker.
     * @param beginWidth Length; start width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral start position.
     * @param endWidth Length; end width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral end position.
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length startCenterPosition, final Length endCenterPosition,
            final Length beginWidth, final Length endWidth, final boolean fixGradualLateralOffset)
            throws OtsGeometryException, NetworkException
    {
        super(parentLink, UUID.randomUUID().toString(), startCenterPosition, endCenterPosition, beginWidth, endWidth,
                fixGradualLateralOffset);
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param startCenterPosition Length; the lateral start position compared to the linear geometry of the Cross Section Link
     *            at the start of the road marker.
     * @param endCenterPosition Length; the lateral end position compared to the linear geometry of the Cross Section Link at
     *            the end of the road marker.
     * @param beginWidth Length; start width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral start position.
     * @param endWidth Length; end width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral end position.
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length startCenterPosition, final Length endCenterPosition,
            final Length beginWidth, final Length endWidth) throws OtsGeometryException, NetworkException
    {
        this(parentLink, startCenterPosition, endCenterPosition, beginWidth, endWidth, false);
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param lateralCenterPosition Length; the lateral start position compared to the linear geometry of the Cross Section
     *            Link.
     * @param width Length; start width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral start position.
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final Length lateralCenterPosition, final Length width)
            throws OtsGeometryException, NetworkException
    {
        super(parentLink, UUID.randomUUID().toString(), lateralCenterPosition, width);
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; The offsets and widths at positions along the line, relative to
     *            the design line of the parent link. If there is just one with and offset, there should just be one element in
     *            the list with Length = 0. If there are more slices, the last one should be at the length of the design line.
     *            If not, a NetworkException is thrown.
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Stripe(final CrossSectionLink parentLink, final List<CrossSectionSlice> crossSectionSlices)
            throws OtsGeometryException, NetworkException
    {
        super(parentLink, UUID.randomUUID().toString(), crossSectionSlices);
    }

    /** {@inheritDoc} */
    @Override
    public final double getZ()
    {
        return 0.0001;
    }

    /**
     * Add lateral permeability for a GTU type in the direction of the design line of the overarching CrossSectionLink.
     * Therefore, the lateral directionality of one-sided permeability has to be switched for left lanes. This is done because
     * the CrossSectionLink has no idea in which direction vehicles will be moving. On a 1+1 lane road with overtaking
     * possibilities, the longitudinal directionality of both lanes will be BOTH. Example:
     * 
     * <pre>
     * Suppose the design line runs from left to right.
     * 
     * =========================
     * 
     * LANE 1L (BACKWARD)         GTUs are allowed to move to lane 2L 
     *                            Permeability RIGHT is true, although vehicles will go to the LEFT...
     * -------------------------  
     * =========================
     * 
     * LANE 2L (BACKWARD)         GTUs are NOT allowed to move to lane 1L nor to lane 2R
     *                            No permeability defined (empty set)
     * =========================
     * =========================
     * 
     * LANE 2R (FORWARD)          GTUs are NOT allowed to move to lane 1R nor to lane 2L
     *                            No permeability defined (empty set)
     * =========================
     * -------------------------
     * 
     * LANE 1R (FORWARD)          GTUs are allowed to move to lane 2R
     *                            Permeability LEFT is true
     * =========================
     * </pre>
     * 
     * <b>Note:</b> GtuType.ALL can be used to set permeability for all types of GTU at once.
     * <p>
     * @param gtuType GtuType; GTU type to add permeability for.
     * @param lateralDirection LateralDirectionality; direction to add (LEFT or RIGHT) compared to the direction of the design
     *            line.
     */
    private void addPermeability(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        if (!this.permeabilityMap.containsKey(gtuType))
        {
            this.permeabilityMap.put(gtuType, new LinkedHashSet<LateralDirectionality>(2));
        }
        this.permeabilityMap.get(gtuType).add(lateralDirection);
    }

    /**
     * Returns whether the given GTU type is allowed to cross the line in the given lateral direction.
     * @param gtuType GtuType; GTU type to look for.
     * @param lateralDirection LateralDirectionality; direction to look for (LEFT or RIGHT) compared to the direction of the
     *            design line.
     * @return whether the road marker is permeable for the GTU type.
     */
    public final boolean isPermeable(final GtuType gtuType, final LateralDirectionality lateralDirection)
    {
        for (GtuType testGtuType = gtuType; null != testGtuType; testGtuType = testGtuType.getParent())
        {
            Set<LateralDirectionality> directions = this.permeabilityMap.get(testGtuType);
            if (null != directions)
            {
                return directions.contains(lateralDirection);
            }
        }
        return false;
    }

    /**
     * @return permeabilityMap for internal use in (sub)classes.
     */
    protected final Map<GtuType, Set<LateralDirectionality>> getPermeabilityMap()
    {
        return this.permeabilityMap;
    }

    /**
     * Sets the permeability.
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

}
