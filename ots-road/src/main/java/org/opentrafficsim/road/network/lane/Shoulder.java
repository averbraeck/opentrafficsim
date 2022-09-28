package org.opentrafficsim.road.network.lane;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.network.NetworkException;

/**
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
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralPositionStart Length; the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param lateralPositionEnd Length; the lateral end position compared to the linear geometry of the Cross Section Link
     * @param beginWidth Length; start width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral start position.
     * @param endWidth Length; end width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral end position.
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Shoulder(final CrossSectionLink parentLink, final String id, final Length lateralPositionStart,
            final Length lateralPositionEnd, final Length beginWidth, final Length endWidth,
            final boolean fixGradualLateralOffset) throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralPositionStart, lateralPositionEnd, beginWidth, endWidth, fixGradualLateralOffset);
    }

    /**
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralPositionStart Length; the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param lateralPositionEnd Length; the lateral end position compared to the linear geometry of the Cross Section Link
     * @param beginWidth Length; start width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral start position.
     * @param endWidth Length; end width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral end position.
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Shoulder(final CrossSectionLink parentLink, final String id, final Length lateralPositionStart,
            final Length lateralPositionEnd, final Length beginWidth, final Length endWidth)
            throws OTSGeometryException, NetworkException
    {
        this(parentLink, id, lateralPositionStart, lateralPositionEnd, beginWidth, endWidth, false);
    }

    /**
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralPosition Length; the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param width Length; the shoulder width, positioned &lt;i&gt;symmetrically around&lt;/i&gt; the lateral start position.
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Shoulder(final CrossSectionLink parentLink, final String id, final Length lateralPosition, final Length width)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralPosition, width);
    }

    /**
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; The offsets and widths at positions along the line, relative to
     *            the design line of the parent link. If there is just one with and offset, there should just be one element in
     *            the list with Length = 0. If there are more slices, the last one should be at the length of the design line.
     *            If not, a NetworkException is thrown.
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Shoulder(final CrossSectionLink parentLink, final String id, final List<CrossSectionSlice> crossSectionSlices)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, crossSectionSlices);
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
