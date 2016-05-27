package org.opentrafficsim.road.network.lane;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Shoulder extends CrossSectionElement
{
    /** */
    private static final long serialVersionUID = 20140819L;

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralPositionStart the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param lateralPositionEnd the lateral end position compared to the linear geometry of the Cross Section Link
     * @param beginWidth start width, positioned <i>symmetrically around</i> the lateral start position.
     * @param endWidth end width, positioned <i>symmetrically around</i> the lateral end position.
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Shoulder(final CrossSectionLink parentLink, final String id, final Length.Rel lateralPositionStart,
        final Length.Rel lateralPositionEnd, final Length.Rel beginWidth, final Length.Rel endWidth)
        throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralPositionStart, lateralPositionEnd, beginWidth, endWidth);
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralPosition the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param width the shoulder width, positioned <i>symmetrically around</i> the lateral start position.
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public Shoulder(final CrossSectionLink parentLink, final String id, final Length.Rel lateralPosition,
        final Length.Rel width) throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralPosition, width);
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param crossSectionSlices The offsets and widths at positions along the line, relative to the design line of the parent
     *            link. If there is just one with and offset, there should just be one element in the list with Length.Rel = 0.
     *            If there are more slices, the last one should be at the length of the design line. If not, a NetworkException
     *            is thrown.
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
    protected final double getZ()
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
