package org.opentrafficsim.road.network.lane;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Oct 25, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class RoadMarkerAlong extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** Lateral permeability per GTU type and direction. */
    private final Map<GTUType, Set<LateralDirectionality>> permeabilityMap = new LinkedHashMap<>();

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
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public RoadMarkerAlong(final CrossSectionLink parentLink, final Length startCenterPosition, final Length endCenterPosition,
            final Length beginWidth, final Length endWidth, final boolean fixGradualLateralOffset)
            throws OTSGeometryException, NetworkException
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
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public RoadMarkerAlong(final CrossSectionLink parentLink, final Length startCenterPosition, final Length endCenterPosition,
            final Length beginWidth, final Length endWidth) throws OTSGeometryException, NetworkException
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
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public RoadMarkerAlong(final CrossSectionLink parentLink, final Length lateralCenterPosition, final Length width)
            throws OTSGeometryException, NetworkException
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
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public RoadMarkerAlong(final CrossSectionLink parentLink, final List<CrossSectionSlice> crossSectionSlices)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, UUID.randomUUID().toString(), crossSectionSlices);
    }

    /**
     * Clone a RoadMarkerAlong for a new network.
     * @param newCrossSectionLink CrossSectionLink; the new link to which the clone belongs
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @param cse RoadMarkerAlong; the element to clone from
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    protected RoadMarkerAlong(final CrossSectionLink newCrossSectionLink, final OTSSimulatorInterface newSimulator,
            final RoadMarkerAlong cse) throws NetworkException
    {
        super(newCrossSectionLink, newSimulator, cse);
        this.permeabilityMap.putAll(cse.permeabilityMap);
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
     * <b>Note:</b> GTUType.ALL can be used to set permeability for all types of GTU at once.
     * <p>
     * @param gtuType GTUType; GTU type to add permeability for.
     * @param lateralDirection LateralDirectionality; direction to add (LEFT or RIGHT) compared to the direction of the design
     *            line.
     */
    public final void addPermeability(final GTUType gtuType, final LateralDirectionality lateralDirection)
    {
        if (!this.permeabilityMap.containsKey(gtuType))
        {
            this.permeabilityMap.put(gtuType, new LinkedHashSet<LateralDirectionality>(2));
        }
        this.permeabilityMap.get(gtuType).add(lateralDirection);
    }

    /**
     * @param gtuType GTUType; GTU type to look for.
     * @param lateralDirection LateralDirectionality; direction to look for (LEFT or RIGHT) compared to the direction of the
     *            design line.
     * @return whether the road marker is permeable for the GTU type.
     */
    public final boolean isPermeable(final GTUType gtuType, final LateralDirectionality lateralDirection)
    {
        for (GTUType testGTUType = gtuType; null != testGTUType; testGTUType = testGTUType.getParent())
        {
            Set<LateralDirectionality> directions = this.permeabilityMap.get(testGTUType);
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
    protected final Map<GTUType, Set<LateralDirectionality>> getPermeabilityMap()
    {
        return this.permeabilityMap;
    }

}
