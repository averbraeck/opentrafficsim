package org.opentrafficsim.road.network.lane;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;

/**
 * Lane without traffic, e.g. emergency lane next to highway.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class NoTrafficLane extends Lane
{
    /** */
    private static final long serialVersionUID = 20150228L;

    /**
     * Map that tells that speed is 0.0 for all vehicles.
     * @param network RoadNetwork; the network for which to define the speeds
     * @return Map&lt;GtuType, Speed&gt;; Map that tells that speed is 0.0 for all vehicles
     */
    private static Map<GtuType, Speed> speedNull(final RoadNetwork network)
    {
        Map<GtuType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(network.getGtuType(GtuType.DEFAULTS.VEHICLE), Speed.ZERO);
        return speedMap;
    }

    /**
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralOffsetAtStart Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; end width, positioned <i>symmetrically around</i> the design line
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OtsGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public NoTrafficLane(final CrossSectionLink parentLink, final String id, final Length lateralOffsetAtStart,
            final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth,
            final boolean fixGradualLateralOffset) throws OtsGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth,
                parentLink.getNetwork().getLaneType(LaneType.DEFAULTS.NONE), speedNull(parentLink.getNetwork()),
                fixGradualLateralOffset);
    }

    /**
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralOffsetAtStart Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; end width, positioned <i>symmetrically around</i> the design line
     * @throws OtsGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public NoTrafficLane(final CrossSectionLink parentLink, final String id, final Length lateralOffsetAtStart,
            final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth)
            throws OtsGeometryException, NetworkException
    {
        this(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth, false);
    }

    /**
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralOffset Length; the lateral offset of the design line of the new CrossSectionLink with respect to the design
     *            line of the parent Link
     * @param width Length; width, positioned <i>symmetrically around</i> the design line
     * @throws OtsGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public NoTrafficLane(final CrossSectionLink parentLink, final String id, final Length lateralOffset, final Length width)
            throws OtsGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffset, width, parentLink.getNetwork().getLaneType(LaneType.DEFAULTS.NONE),
                speedNull(parentLink.getNetwork()));
    }

    /**
     * @param parentLink CrossSectionLink; Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; The offsets and widths at positions along the line, relative to
     *            the design line of the parent link. If there is just one with and offset, there should just be one element in
     *            the list with Length = 0. If there are more slices, the last one should be at the length of the design line.
     *            If not, a NetworkException is thrown.
     * @throws OtsGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public NoTrafficLane(final CrossSectionLink parentLink, final String id, final List<CrossSectionSlice> crossSectionSlices)
            throws OtsGeometryException, NetworkException
    {
        super(parentLink, id, crossSectionSlices, parentLink.getNetwork().getLaneType(LaneType.DEFAULTS.NONE),
                speedNull(parentLink.getNetwork()));
    }

    /** {@inheritDoc} */
    @Override
    public final double getZ()
    {
        return -0.00005;
    }
}
