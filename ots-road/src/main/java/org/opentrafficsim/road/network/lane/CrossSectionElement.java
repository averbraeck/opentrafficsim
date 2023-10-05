package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Cross section elements are used to compose a CrossSectionLink.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class CrossSectionElement extends LocalEventProducer implements Locatable, Serializable, Identifiable, Drawable
{
    /** */
    private static final long serialVersionUID = 20150826L;

    /** The id. Should be unique within the parentLink. */
    private final String id;

    /** Cross Section Link to which the element belongs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CrossSectionLink link;

    /** The offsets and widths at positions along the line, relative to the design line of the parent link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final List<CrossSectionSlice> crossSectionSlices;

    /** The center line of the element. Calculated once at the creation. */
    private final OtsLine2d centerLine;

    /** The contour of the element. Calculated once at the creation. */
    private final Polygon2d contour;

    /** Location, center of contour. */
    private final Point2d location;

    /** Bounding box. */
    private final Bounds2d bounds;

    /**
     * Constructor.
     * @param link CrossSectionLink; link.
     * @param id String; id.
     * @param centerLine PolyLine2d; center line.
     * @param contour OtsLine2d; contour shape.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; cross-section slices.
     * @throws NetworkException when no cross-section slice is defined.
     */
    public CrossSectionElement(final CrossSectionLink link, final String id, final OtsLine2d centerLine,
            final Polygon2d contour, final List<CrossSectionSlice> crossSectionSlices) throws NetworkException
    {
        Throw.whenNull(link, "Link may not be null.");
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(centerLine, "Center line may not be null.");
        Throw.whenNull(contour, "Contour may not be null.");
        Throw.whenNull(crossSectionSlices, "Cross section slices may not be null.");
        Throw.when(crossSectionSlices.isEmpty(), NetworkException.class, "Need at least 1 cross section slice.");
        this.link = link;
        this.id = id;
        this.centerLine = centerLine;
        this.contour = contour;
        this.location = contour.getBounds().midPoint();
        this.bounds = new Bounds2d(contour.getBounds().getDeltaX(), contour.getBounds().getDeltaY());
        this.crossSectionSlices = crossSectionSlices;

        link.addCrossSectionElement(this);

        // clear lane change info cache for each cross section element created
        link.getNetwork().clearLaneChangeInfoCache();
    }

    /**
     * Returns the link of this cross-section element.
     * @return CrossSectionLink; link of this cross-section element.
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
     * Calculate the slice the fractional position is in.
     * @param fractionalPosition double; the fractional position between 0 and 1 compared to the design line
     * @return int; the lower slice number between 0 and number of slices - 1.
     */
    private int calculateSliceNumber(final double fractionalPosition)
    {
        double linkLength = this.link.getLength().si;
        for (int i = 0; i < this.crossSectionSlices.size() - 1; i++)
        {
            if (fractionalPosition >= this.crossSectionSlices.get(i).getRelativeLength().si / linkLength
                    && fractionalPosition <= this.crossSectionSlices.get(i + 1).getRelativeLength().si / linkLength)
            {
                return i;
            }
        }
        return this.crossSectionSlices.size() - 2;
    }

    /**
     * Returns the fractional position along the segment between two cross-section slices.
     * @param fractionalPosition double; fractional position on the whole link.
     * @param sliceNumber int; slice number at the start of the segment.
     * @return double; fractional position along the segment between two cross-section slices.
     */
    private double fractionalPositionSegment(final double fractionalPosition, final int sliceNumber)
    {
        double startPos = this.crossSectionSlices.get(sliceNumber).getRelativeLength().si / getLength().si;
        double endPos = this.crossSectionSlices.get(sliceNumber + 1).getRelativeLength().si / getLength().si;
        return (fractionalPosition - startPos) / (endPos - startPos);
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param fractionalPosition double; fractional longitudinal position on this Lane
     * @return Length; the lateralCenterPosition at the specified longitudinal position
     */
    public final Length getLateralCenterPosition(final double fractionalPosition)
    {
        if (this.crossSectionSlices.size() == 1)
        {
            return this.getDesignLineOffsetAtBegin();
        }
        if (this.crossSectionSlices.size() == 2)
        {
            return Length.interpolate(this.getDesignLineOffsetAtBegin(), this.getDesignLineOffsetAtEnd(), fractionalPosition);
        }
        int sliceNr = calculateSliceNumber(fractionalPosition);
        double segmentPosition = fractionalPositionSegment(fractionalPosition, sliceNr);
        return Length.interpolate(this.crossSectionSlices.get(sliceNr).getDesignLineOffset(),
                this.crossSectionSlices.get(sliceNr + 1).getDesignLineOffset(), segmentPosition);
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param longitudinalPosition Length; the longitudinal position on this Lane
     * @return Length; the lateralCenterPosition at the specified longitudinal position
     */
    public final Length getLateralCenterPosition(final Length longitudinalPosition)
    {
        return getLateralCenterPosition(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified longitudinal position.
     * @param longitudinalPosition Length; the longitudinal position
     * @return Length; the width of this CrossSectionElement at the specified longitudinal position.
     */
    public final Length getWidth(final Length longitudinalPosition)
    {
        return getWidth(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified fractional longitudinal position.
     * @param fractionalPosition double; the fractional longitudinal position
     * @return Length; the width of this CrossSectionElement at the specified fractional longitudinal position.
     */
    public final Length getWidth(final double fractionalPosition)
    {
        if (this.crossSectionSlices.size() == 1)
        {
            return this.getBeginWidth();
        }
        if (this.crossSectionSlices.size() == 2)
        {
            return Length.interpolate(this.getBeginWidth(), this.getEndWidth(), fractionalPosition);
        }
        int sliceNr = calculateSliceNumber(fractionalPosition);
        double segmentPosition = fractionalPositionSegment(fractionalPosition, sliceNr);
        return Length.interpolate(this.crossSectionSlices.get(sliceNr).getWidth(),
                this.crossSectionSlices.get(sliceNr + 1).getWidth(), segmentPosition);
    }

    /**
     * Return the length of this CrossSectionElement as measured along the design line (which equals the center line).
     * @return Length; the length of this CrossSectionElement
     */
    public final Length getLength()
    {
        return this.centerLine.getLength();
    }

    /**
     * Retrieve the offset from the design line at the begin of the parent link.
     * @return Length; the offset of this CrossSectionElement at the begin of the parent link
     */
    public final Length getDesignLineOffsetAtBegin()
    {
        return this.crossSectionSlices.get(0).getDesignLineOffset();
    }

    /**
     * Retrieve the offset from the design line at the end of the parent link.
     * @return Length; the offset of this CrossSectionElement at the end of the parent link
     */
    public final Length getDesignLineOffsetAtEnd()
    {
        return this.crossSectionSlices.get(this.crossSectionSlices.size() - 1).getDesignLineOffset();
    }

    /**
     * Retrieve the width at the begin of the parent link.
     * @return Length; the width of this CrossSectionElement at the begin of the parent link
     */
    public final Length getBeginWidth()
    {
        return this.crossSectionSlices.get(0).getWidth();
    }

    /**
     * Retrieve the width at the end of the parent link.
     * @return Length; the width of this CrossSectionElement at the end of the parent link
     */
    public final Length getEndWidth()
    {
        return this.crossSectionSlices.get(this.crossSectionSlices.size() - 1).getWidth();
    }

    /**
     * Retrieve the Z offset (used to determine what covers what when drawing).
     * @return double; the Z-offset for drawing (what's on top, what's underneath).
     */
    @Override
    public double getZ()
    {
        // default implementation returns 0.0 in case of a null location or a 2D location
        return Try.assign(() -> Locatable.super.getZ(), "Remote exception on calling getZ()");
    }

    /**
     * Retrieve the center line of this CrossSectionElement.
     * @return OtsLine2d; the center line of this CrossSectionElement
     */
    public final OtsLine2d getCenterLine()
    {
        return this.centerLine;
    }

    /**
     * Retrieve the contour of this CrossSectionElement.
     * @return Polygon2d; the contour of this CrossSectionElement
     */
    public final Polygon2d getContour()
    {
        return this.contour;
    }

    /**
     * Retrieve the id of this CrossSectionElement.
     * @return String; the id of this CrossSectionElement
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Retrieve the id of this CrossSectionElement.
     * @return String; the id of this CrossSectionElement
     */
    public final String getFullId()
    {
        return getLink().getId() + "." + this.id;
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified fractional longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param fractionalLongitudinalPosition double; ranges from 0.0 (begin of parentLink) to 1.0 (end of parentLink)
     * @return Length
     */
    public final Length getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final double fractionalLongitudinalPosition)
    {
        Length designLineOffset;
        Length halfWidth;
        if (this.crossSectionSlices.size() <= 2)
        {
            designLineOffset = Length.interpolate(getDesignLineOffsetAtBegin(), getDesignLineOffsetAtEnd(),
                    fractionalLongitudinalPosition);
            halfWidth = Length.interpolate(getBeginWidth(), getEndWidth(), fractionalLongitudinalPosition).times(0.5);
        }
        else
        {
            int sliceNr = calculateSliceNumber(fractionalLongitudinalPosition);
            double segmentPosition = fractionalPositionSegment(fractionalLongitudinalPosition, sliceNr);
            designLineOffset = Length.interpolate(this.crossSectionSlices.get(sliceNr).getDesignLineOffset(),
                    this.crossSectionSlices.get(sliceNr + 1).getDesignLineOffset(), segmentPosition);
            halfWidth = Length.interpolate(this.crossSectionSlices.get(sliceNr).getWidth(),
                    this.crossSectionSlices.get(sliceNr + 1).getWidth(), segmentPosition).times(0.5);
        }

        switch (lateralDirection)
        {
            case LEFT:
                return designLineOffset.minus(halfWidth);
            case RIGHT:
                return designLineOffset.plus(halfWidth);
            default:
                throw new Error("Bad switch on LateralDirectionality " + lateralDirection);
        }
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param longitudinalPosition Length; the position along the length of this CrossSectionElement
     * @return Length
     */
    public final Length getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
            final Length longitudinalPosition)
    {
        return getLateralBoundaryPosition(lateralDirection, longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Construct a buffer geometry by offsetting the linear geometry line with a distance and constructing a so-called "buffer"
     * around it.
     * @param cse CrossSectionElement; the cross section element to construct the contour for
     * @return Polygon2d; the geometry belonging to this CrossSectionElement.
     * @throws OtsGeometryException when construction of the geometry fails
     * @throws NetworkException when the resulting contour is degenerate (cannot happen; we hope)
     */
    @Deprecated
    public static Polygon2d constructContour(final CrossSectionElement cse) throws OtsGeometryException, NetworkException
    {
        Point2d[] result = null;

        if (cse.crossSectionSlices.size() <= 2)
        {
            OtsLine2d crossSectionDesignLine = cse.centerLine;
            PolyLine2d rightBoundary = crossSectionDesignLine.getLine2d().offsetLine(-cse.getBeginWidth().getSI() / 2,
                    -cse.getEndWidth().getSI() / 2);
            PolyLine2d leftBoundary = crossSectionDesignLine.getLine2d().offsetLine(cse.getBeginWidth().getSI() / 2,
                    cse.getEndWidth().getSI() / 2);
            result = new Point2d[rightBoundary.size() + leftBoundary.size() + 1];
            int resultIndex = 0;
            for (int index = 0; index < rightBoundary.size(); index++)
            {
                result[resultIndex++] = rightBoundary.get(index);
            }
            for (int index = leftBoundary.size(); --index >= 0;)
            {
                result[resultIndex++] = leftBoundary.get(index);
            }
            result[resultIndex] = rightBoundary.get(0); // close the contour
        }
        else
        {
            List<Point2d> resultList = new ArrayList<>();
            List<Point2d> rightBoundary = new ArrayList<>();
            for (int i = 0; i < cse.crossSectionSlices.size() - 1; i++)
            {
                double plLength = cse.getLink().getLength().si;
                double so = cse.crossSectionSlices.get(i).getDesignLineOffset().si;
                double eo = cse.crossSectionSlices.get(i + 1).getDesignLineOffset().si;
                double sw2 = cse.crossSectionSlices.get(i).getWidth().si / 2.0;
                double ew2 = cse.crossSectionSlices.get(i + 1).getWidth().si / 2.0;
                double sf = cse.crossSectionSlices.get(i).getRelativeLength().si / plLength;
                double ef = cse.crossSectionSlices.get(i + 1).getRelativeLength().si / plLength;
                OtsLine2d crossSectionDesignLine = cse.getLink().getDesignLine().extractFractional(sf, ef).offsetLine(so, eo);
                resultList.addAll(Arrays.asList(crossSectionDesignLine.offsetLine(-sw2, -ew2).getPoints()));
                rightBoundary.addAll(Arrays.asList(crossSectionDesignLine.offsetLine(sw2, ew2).getPoints()));
            }
            for (int index = rightBoundary.size(); --index >= 0;)
            {
                resultList.add(rightBoundary.get(index));
            }
            // close the contour (might not be needed)
            resultList.add(resultList.get(0));
            result = resultList.toArray(new Point2d[] {});
        }
        return new Polygon2d(true, result);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Point2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds2d getBounds()
    {
        return this.bounds;
    }

    /**
     * Returns the elevation at the given position.
     * @param position Length; position.
     * @return Length; elevation at the given position.
     */
    public Length getElevation(final Length position)
    {
        return getElevation(position.si / getLength().si);
    }

    /**
     * Returns the elevation at the given fractional position.
     * @param fractionalPosition double; fractional position.
     * @return Length; elevation at the given fractional position.
     */
    public Length getElevation(final double fractionalPosition)
    {
        return getLink().getElevation(fractionalPosition);
    }

    /**
     * Returns the grade at the given position, given as delta_h / delta_f, where f is fractional position.
     * @param position Length; position.
     * @return double; grade at the given position.
     */
    public double getGrade(final Length position)
    {
        return getGrade(position.si / getLength().si);
    }

    /**
     * Returns the grade at the given fractional position, given as delta_h / delta_f, where f is fractional position.
     * @param fractionalPosition double; fractional position.
     * @return double; grade at the given fractional position.
     */
    public double getGrade(final double fractionalPosition)
    {
        return getLink().getGrade(fractionalPosition);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("CSE offset %.2fm..%.2fm, width %.2fm..%.2fm", getDesignLineOffsetAtBegin().getSI(),
                getDesignLineOffsetAtEnd().getSI(), getBeginWidth().getSI(), getEndWidth().getSI());
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
