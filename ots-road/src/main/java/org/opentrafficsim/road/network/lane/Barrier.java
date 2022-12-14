package org.opentrafficsim.road.network.lane;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Typically, a barrier will have no lateral permeability. Sometimes, pedestrians can be given lateral permeability for the
 * barrier.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class Barrier extends RoadMarkerAlong
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param parentLink CrossSectionLink; the cross section link to which the element belongs.
     * @param beginCenterPosition Length; the lateral start position compared to the linear geometry of the cross section link.
     * @param endCenterPosition Length; the lateral end position compared to the linear geometry of the Cross Section Link.
     * @param beginWidth Length; start width, positioned <i>symmetrically around</i> the lateral start position.
     * @param endWidth Length; end width, positioned <i>symmetrically around</i> the lateral end position.
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Barrier(final CrossSectionLink parentLink, final Length beginCenterPosition, final Length endCenterPosition,
            final Length beginWidth, final Length endWidth) throws OtsGeometryException, NetworkException
    {
        super(parentLink, beginCenterPosition, endCenterPosition, beginWidth, endWidth);
    }

}
