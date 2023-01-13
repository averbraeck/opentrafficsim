package org.opentrafficsim.road.network.lane;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Represents a shoulder. The purpose opf this class is to be able to toggle animation of shoulders, without also toggling lanes
 * and stripes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Shoulder extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20140819L;

    /**
     * Creates a shoulder element.
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralPositionStart Length; the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param lateralPositionEnd Length; the lateral end position compared to the linear geometry of the Cross Section Link
     * @param beginWidth Length; start width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral start position.
     * @param endWidth Length; end width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral end position.
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Shoulder(final CrossSectionLink parentLink, final String id, final Length lateralPositionStart,
            final Length lateralPositionEnd, final Length beginWidth, final Length endWidth,
            final boolean fixGradualLateralOffset) throws OtsGeometryException, NetworkException
    {
        super(parentLink, id, lateralPositionStart, lateralPositionEnd, beginWidth, endWidth, false);
    }

    /** {@inheritDoc} */
    @Override
    public final double getZ()
    {
        return -0.0002;
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
