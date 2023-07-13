package org.opentrafficsim.road.network.lane;

import java.util.List;

import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Represents a shoulder. The purpose opf this class is to be able to toggle animation of shoulders, without also toggling lanes
 * and stripes.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Shoulder extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20140819L;

    /**
     * Constructor specifying geometry.
     * @param link CrossSectionLink; link.
     * @param id String; the id of this lane within the link; should be unique within the link.
     * @param centerLine OtsLine3d; center line.
     * @param contour OtsShape; contour shape.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; cross-section slices.
     * @throws NetworkException when no cross-section slice is defined.
     */
    public Shoulder(final CrossSectionLink link, final String id, final OtsLine3d centerLine, final OtsShape contour,
            final List<CrossSectionSlice> crossSectionSlices) throws NetworkException
    {
        super(link, id, centerLine, contour, crossSectionSlices);
    }

    /** {@inheritDoc} */
    @Override
    public final double getZ()
    {
        return -0.0004;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("Shoulder offset %.2fm..%.2fm, width %.2fm..%.2fm", getDesignLineOffsetAtBegin().getSI(),
                getDesignLineOffsetAtEnd().getSI(), getBeginWidth().getSI(), getEndWidth().getSI());
    }

}
