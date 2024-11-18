package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.base.geometry.PolygonShape;
import org.opentrafficsim.core.geometry.ContinuousLine.ContinuousDoubleFunction;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;

/**
 * Cross section elements are used to compose a CrossSectionLink.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class CrossSectionElement extends LocalEventProducer implements OtsLocatable, Serializable, Identifiable
{
    /** */
    private static final long serialVersionUID = 20150826L;

    /** The id. Should be unique within the parentLink. */
    private final String id;

    /** Cross Section Link to which the element belongs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CrossSectionLink link;

    /** The center line of the element. Calculated once at the creation. */
    private final OtsLine2d centerLine;

    /** The contour of the element. Calculated once at the creation. */
    private final Polygon2d contour;

    /** Offset. */
    private final ContinuousDoubleFunction offset;

    /** Width. */
    private final ContinuousDoubleFunction width;

    /** Location, center of contour. */
    private final OrientedPoint2d location;

    /** Bounding box. */
    private final Bounds2d bounds;

    /** Shape. */
    private final OtsShape shape;

    /**
     * Constructor.
     * @param link link
     * @param id id
     * @param geometry geometry
     * @throws NetworkException when no cross-section slice is defined.
     */
    public CrossSectionElement(final CrossSectionLink link, final String id, final CrossSectionGeometry geometry)
            throws NetworkException
    {
        Throw.whenNull(link, "Link may not be null.");
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(geometry, "Geometry may not be null.");
        this.link = link;
        this.id = id;
        this.centerLine = geometry.centerLine();
        this.location = geometry.centerLine().getLocationPointFractionExtended(0.5);
        this.contour = geometry.contour();
        Polygon2d relativeContour = OtsLocatable.relativeContour(this);
        this.shape = new PolygonShape(relativeContour);
        this.bounds = relativeContour.getBounds();
        this.offset = geometry.offset();
        this.width = geometry.width();

        link.addCrossSectionElement(this);

        // clear lane change info cache for each cross section element created
        link.getNetwork().clearLaneChangeInfoCache();
    }

    /**
     * Returns the link of this cross-section element.
     * @return link of this cross-section element.
     */
    public final CrossSectionLink getLink()
    {
        return this.link;
    }

    /**
     * @return the road network to which the lane belongs
     */
    public final RoadNetwork getNetwork()
    {
        return this.link.getNetwork();
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param fractionalPosition fractional longitudinal position on this Lane
     * @return the lateralCenterPosition at the specified longitudinal position
     */
    public final Length getLateralCenterPosition(final double fractionalPosition)
    {
        return Length.instantiateSI(this.offset.apply(fractionalPosition));
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param longitudinalPosition the longitudinal position on this Lane
     * @return the lateralCenterPosition at the specified longitudinal position
     */
    public final Length getLateralCenterPosition(final Length longitudinalPosition)
    {
        return getLateralCenterPosition(longitudinalPosition.si / getLength().si);
    }

    /**
     * Return the width of this CrossSectionElement at a specified longitudinal position.
     * @param longitudinalPosition the longitudinal position
     * @return the width of this CrossSectionElement at the specified longitudinal position.
     */
    public final Length getWidth(final Length longitudinalPosition)
    {
        return getWidth(longitudinalPosition.si / getLength().si);
    }

    /**
     * Return the width of this CrossSectionElement at a specified fractional longitudinal position.
     * @param fractionalPosition the fractional longitudinal position
     * @return the width of this CrossSectionElement at the specified fractional longitudinal position.
     */
    public final Length getWidth(final double fractionalPosition)
    {
        return Length.instantiateSI(this.width.apply(fractionalPosition));
    }

    /**
     * Return the length of this CrossSectionElement as measured along the design line (which equals the center line).
     * @return the length of this CrossSectionElement
     */
    public final Length getLength()
    {
        return this.centerLine.getTypedLength();
    }

    /**
     * Retrieve the offset from the design line at the begin of the parent link.
     * @return the offset of this CrossSectionElement at the begin of the parent link
     */
    public final Length getOffsetAtBegin()
    {
        return Length.instantiateSI(this.offset.apply(0.0));
    }

    /**
     * Retrieve the offset from the design line at the end of the parent link.
     * @return the offset of this CrossSectionElement at the end of the parent link
     */
    public final Length getOffsetAtEnd()
    {
        return Length.instantiateSI(this.offset.apply(1.0));
    }

    /**
     * Retrieve the width at the begin of the parent link.
     * @return the width of this CrossSectionElement at the begin of the parent link
     */
    public final Length getBeginWidth()
    {
        return Length.instantiateSI(this.width.apply(0.0));
    }

    /**
     * Retrieve the width at the end of the parent link.
     * @return the width of this CrossSectionElement at the end of the parent link
     */
    public final Length getEndWidth()
    {
        return Length.instantiateSI(this.width.apply(1.0));
    }

    /**
     * Retrieve the Z offset (used to determine what covers what when drawing).
     * @return the Z-offset for drawing (what's on top, what's underneath).
     */
    @Override
    public double getZ()
    {
        // default implementation returns 0.0 in case of a null location or a 2D location
        return Try.assign(() -> OtsLocatable.super.getZ(), "Remote exception on calling getZ()");
    }

    /**
     * Retrieve the center line of this CrossSectionElement.
     * @return the center line of this CrossSectionElement
     */
    public final OtsLine2d getCenterLine()
    {
        return this.centerLine;
    }

    @Override
    public final Polygon2d getContour()
    {
        return this.contour;
    }

    @Override
    public final OtsShape getShape()
    {
        return this.shape;
    }

    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Retrieve the id of this CrossSectionElement.
     * @return the id of this CrossSectionElement
     */
    public final String getFullId()
    {
        return getLink().getId() + "." + this.id;
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified fractional longitudinal position.
     * @param lateralDirection LEFT, or RIGHT
     * @param fractionalLongitudinalPosition ranges from 0.0 (begin of parentLink) to 1.0 (end of parentLink)
     * @return Length
     * @throws IllegalArgumentException when lateral direction is {@code null} or NONE.
     */
    public final Length getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final double fractionalLongitudinalPosition)
    {
        Length offsetAt = getLateralCenterPosition(fractionalLongitudinalPosition);
        Length halfWidth = getWidth(fractionalLongitudinalPosition).times(0.5);

        switch (lateralDirection)
        {
            case LEFT:
                return offsetAt.minus(halfWidth);
            case RIGHT:
                return offsetAt.plus(halfWidth);
            default:
                throw new IllegalArgumentException("Bad value for LateralDirectionality " + lateralDirection);
        }
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified longitudinal position.
     * @param lateralDirection LEFT, or RIGHT
     * @param longitudinalPosition the position along the length of this CrossSectionElement
     * @return Length
     */
    public final Length getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final Length longitudinalPosition)
    {
        return getLateralBoundaryPosition(lateralDirection, longitudinalPosition.getSI() / getLength().getSI());
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public OrientedPoint2d getLocation()
    {
        return this.location;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds2d getBounds()
    {
        return this.bounds;
    }

    /**
     * Returns the elevation at the given position.
     * @param position position.
     * @return elevation at the given position.
     */
    public Length getElevation(final Length position)
    {
        return getElevation(position.si / getLength().si);
    }

    /**
     * Returns the elevation at the given fractional position.
     * @param fractionalPosition fractional position.
     * @return elevation at the given fractional position.
     */
    public Length getElevation(final double fractionalPosition)
    {
        return getLink().getElevation(fractionalPosition);
    }

    /**
     * Returns the grade at the given position, given as delta_h / delta_f, where f is fractional position.
     * @param position position.
     * @return grade at the given position.
     */
    public double getGrade(final Length position)
    {
        return getGrade(position.si / getLength().si);
    }

    /**
     * Returns the grade at the given fractional position, given as delta_h / delta_f, where f is fractional position.
     * @param fractionalPosition fractional position.
     * @return grade at the given fractional position.
     */
    public double getGrade(final double fractionalPosition)
    {
        return getLink().getGrade(fractionalPosition);
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("CSE offset %.2fm..%.2fm, width %.2fm..%.2fm", getOffsetAtBegin().getSI(),
                getOffsetAtEnd().getSI(), getBeginWidth().getSI(), getEndWidth().getSI());
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.link == null) ? 0 : this.link.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CrossSectionElement other = (CrossSectionElement) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.link == null)
        {
            if (other.link != null)
                return false;
        }
        else if (!this.link.equals(other.link))
            return false;
        return true;
    }
}
