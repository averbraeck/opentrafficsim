package org.opentrafficsim.road.network;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Cross section elements are used to compose a CrossSectionLink.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class CrossSectionElement extends LocalEventProducer implements OtsShape, Identifiable
{
    /** The id. Should be unique within the parentLink. */
    private final String id;

    /** Cross Section Link to which the element belongs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CrossSectionLink link;

    /** The center line of the element. Calculated once at the creation. */
    private final OtsLine2d centerLine;

    /** The contour of the element. Calculated once at the creation. */
    private final Polygon2d absoluteContour;

    /** Relative contour.. */
    private final Polygon2d relativeContour;

    /** Offset. */
    private final ContinuousPiecewiseLinearFunction offset;

    /** Width. */
    private final ContinuousPiecewiseLinearFunction width;

    /** Location, center of contour. */
    private final DirectedPoint2d location;

    /**
     * Constructor.
     * @param link link
     * @param id id
     * @param geometry geometry
     */
    public CrossSectionElement(final CrossSectionLink link, final String id, final CrossSectionGeometry geometry)
    {
        Throw.whenNull(link, "link");
        Throw.whenNull(id, "id");
        Throw.whenNull(geometry, "geometry");
        this.link = link;
        this.id = id;
        this.centerLine = geometry.centerLine();
        this.location = geometry.centerLine().getLocationPointFractionExtended(0.5);
        this.absoluteContour = geometry.absoluteContour();
        this.relativeContour =
                new Polygon2d(0.0, OtsShape.toRelativeTransform(this.location).transform(this.absoluteContour.iterator()));
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
     * Returns network.
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
        return Length.ofSI(this.offset.get(fractionalPosition));
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
        return Length.ofSI(this.width.get(fractionalPosition));
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
        return Length.ofSI(this.offset.get(0.0));
    }

    /**
     * Retrieve the offset from the design line at the end of the parent link.
     * @return the offset of this CrossSectionElement at the end of the parent link
     */
    public final Length getOffsetAtEnd()
    {
        return Length.ofSI(this.offset.get(1.0));
    }

    /**
     * Retrieve the width at the begin of the parent link.
     * @return the width of this CrossSectionElement at the begin of the parent link
     */
    public final Length getBeginWidth()
    {
        return Length.ofSI(this.width.get(0.0));
    }

    /**
     * Retrieve the width at the end of the parent link.
     * @return the width of this CrossSectionElement at the end of the parent link
     */
    public final Length getEndWidth()
    {
        return Length.ofSI(this.width.get(1.0));
    }

    /**
     * Retrieve the Z offset (used to determine what covers what when drawing).
     * @return the Z-offset for drawing (what's on top, what's underneath).
     */
    @Override
    public double getZ()
    {
        // default implementation returns 0.0 in case of a null location or a 2D location
        return Try.assign(() -> OtsShape.super.getZ(), "Remote exception on calling getZ()");
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
    public final Polygon2d getAbsoluteContour()
    {
        return this.absoluteContour;
    }

    @Override
    public final Polygon2d getRelativeContour()
    {
        return this.relativeContour;
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
    public DirectedPoint2d getLocation()
    {
        return this.location;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds2d getRelativeBounds()
    {
        return this.relativeContour.getAbsoluteBounds();
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
