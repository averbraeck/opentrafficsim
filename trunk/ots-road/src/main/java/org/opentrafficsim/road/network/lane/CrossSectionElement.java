package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class CrossSectionElement implements LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150826L;

    /** the id. Should be unique within the parentLink. */
    private final String id;

    /** Cross Section Link to which the element belongs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CrossSectionLink parentLink;

    /** The offsets and widths at positions along the line, relative to the design line of the parent link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final List<CrossSectionSlice> crossSectionSlices;

    /** The length of the line. Calculated once at the creation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Length.Rel length;

    /** The center line of the element. Calculated once at the creation. */
    private final OTSLine3D centerLine;

    /** The contour of the element. Calculated once at the creation. */
    private final OTSLine3D contour;

    /** constant for relative length 0. */
    private static final Length.Rel LENGTH_0 = new Length.Rel(0.0, LengthUnit.SI);

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param id String; The id of the CrosssSectionElement. Should be unique within the parentLink.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param crossSectionSlices The offsets and widths at positions along the line, relative to the design line of the parent
     *            link
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public CrossSectionElement(final CrossSectionLink parentLink, final String id,
        final List<CrossSectionSlice> crossSectionSlices) throws OTSGeometryException, NetworkException
    {
        if (id == null)
        {
            throw new NetworkException("Constructor of CrossSectionElement -- id cannot be null");
        }
        for (CrossSectionElement cse : parentLink.getCrossSectionElementList())
        {
            if (cse.getId().equals(id))
            {
                throw new NetworkException("Constructor of CrossSectionElement -- id " + id
                    + " not unique within the Link");
            }
        }
        this.id = id;
        this.parentLink = parentLink;
        this.crossSectionSlices = new ArrayList<>(crossSectionSlices); // copy of list with immutable slices

        // TODO take the cross section slices into account...
        this.centerLine =
            this.getParentLink().getDesignLine().offsetLine(getDesignLineOffsetAtBegin().getSI(),
                getDesignLineOffsetAtEnd().getSI());
        this.length = this.centerLine.getLength();
        this.contour = constructContour(this);

        this.parentLink.addCrossSectionElement(this);
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param id String; The id of the CrosssSectionElement. Should be unique within the parentLink.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffsetAtBegin Length.Rel; the lateral offset of the design line of the new CrossSectionLink with respect to
     *            the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length.Rel; the lateral offset of the design line of the new CrossSectionLink with respect to
     *            the design line of the parent Link at the end of the parent Link
     * @param beginWidth Length.Rel; width at start, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length.Rel; width at end, positioned <i>symmetrically around</i> the design line
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public CrossSectionElement(final CrossSectionLink parentLink, final String id,
        final Length.Rel lateralOffsetAtBegin, final Length.Rel lateralOffsetAtEnd, final Length.Rel beginWidth,
        final Length.Rel endWidth) throws OTSGeometryException, NetworkException
    {
        this(parentLink, id, Arrays.asList(new CrossSectionSlice[]{
            new CrossSectionSlice(LENGTH_0, lateralOffsetAtBegin, beginWidth),
            new CrossSectionSlice(parentLink.getLength(), lateralOffsetAtEnd, endWidth)}));
    }

    /**
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction, with the direction from
     * the StartNode towards the EndNode as the longitudinal direction.
     * @param id String; The id of the CrosssSectionElement. Should be unique within the parentLink.
     * @param parentLink CrossSectionLink; Link to which the element belongs.
     * @param lateralOffset Length.Rel; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link
     * @param width Length.Rel; width, positioned <i>symmetrically around</i> the design line
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    public CrossSectionElement(final CrossSectionLink parentLink, final String id, final Length.Rel lateralOffset,
        final Length.Rel width) throws OTSGeometryException, NetworkException
    {
        this(parentLink, id, Arrays
            .asList(new CrossSectionSlice[]{new CrossSectionSlice(LENGTH_0, lateralOffset, width)}));
    }

    /**
     * @return parentLink.
     */
    public final CrossSectionLink getParentLink()
    {
        return this.parentLink;
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param fractionalPosition double; fractional longitudinal position on this Lane
     * @return Length.Rel the lateralCenterPosition at the specified longitudinal position
     */
    public final Length.Rel getLateralCenterPosition(final double fractionalPosition)
    {
        // TODO take the cross section slices into account...
        return Length.Rel.interpolate(this.getDesignLineOffsetAtBegin(), this.getDesignLineOffsetAtEnd(),
            fractionalPosition);
    }

    /**
     * Retrieve the lateral offset from the Link design line at the specified longitudinal position.
     * @param longitudinalPosition Length.Rel; the longitudinal position on this Lane
     * @return Length.Rel the lateralCenterPosition at the specified longitudinal position
     */
    public final Length.Rel getLateralCenterPosition(final Length.Rel longitudinalPosition)
    {
        return getLateralCenterPosition(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified longitudinal position.
     * @param longitudinalPosition DoubleScalar&lt;LengthUnit&gt;; the longitudinal position
     * @return Length.Rel; the width of this CrossSectionElement at the specified longitudinal position.
     */
    public final Length.Rel getWidth(final Length.Rel longitudinalPosition)
    {
        return getWidth(longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Return the width of this CrossSectionElement at a specified fractional longitudinal position.
     * @param fractionalPosition double; the fractional longitudinal position
     * @return Length.Rel; the width of this CrossSectionElement at the specified fractional longitudinal position.
     */
    public final Length.Rel getWidth(final double fractionalPosition)
    {
        // TODO take the cross section slices into account...
        return Length.Rel.interpolate(getBeginWidth(), getEndWidth(), fractionalPosition);
    }

    /**
     * Return the length of this CrossSectionElement as measured along the design line (which equals the center line).
     * @return Length.Rel; the length of this CrossSectionElement
     */
    public final Length.Rel getLength()
    {
        return this.length;
    }

    /**
     * @return designLineOffsetAtBegin.
     */
    public final Length.Rel getDesignLineOffsetAtBegin()
    {
        return this.crossSectionSlices.get(0).getDesignLineOffset();
    }

    /**
     * @return designLineOffsetAtEnd.
     */
    public final Length.Rel getDesignLineOffsetAtEnd()
    {
        return this.crossSectionSlices.get(this.crossSectionSlices.size() - 1).getDesignLineOffset();
    }

    /**
     * @return beginWidth.
     */
    public final Length.Rel getBeginWidth()
    {
        return this.crossSectionSlices.get(0).getWidth();
    }

    /**
     * @return endWidth.
     */
    public final Length.Rel getEndWidth()
    {
        return this.crossSectionSlices.get(this.crossSectionSlices.size() - 1).getWidth();
    }

    /**
     * @return the z-offset for drawing (what's on top, what's underneath).
     */
    protected abstract double getZ();

    /**
     * @return centerLine.
     */
    public final OTSLine3D getCenterLine()
    {
        return this.centerLine;
    }

    /**
     * @return contour.
     */
    public final OTSLine3D getContour()
    {
        return this.contour;
    }

    /**
     * @return id
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Return the lateral offset from the design line of the parent Link of the Left or Right boundary of this
     * CrossSectionElement at the specified fractional longitudinal position.
     * @param lateralDirection LateralDirectionality; LEFT, or RIGHT
     * @param fractionalLongitudinalPosition double; ranges from 0.0 (begin of parentLink) to 1.0 (end of parentLink)
     * @return Length.Rel
     */
    public final Length.Rel getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
        final double fractionalLongitudinalPosition)
    {
        // TODO take the cross section slices into account...
        Length.Rel designLineOffset =
            Length.Rel.interpolate(getDesignLineOffsetAtBegin(), getDesignLineOffsetAtEnd(),
                fractionalLongitudinalPosition);
        // TODO take the cross section slices into account...
        Length.Rel halfWidth =
            Length.Rel.interpolate(getBeginWidth(), getEndWidth(), fractionalLongitudinalPosition).multiplyBy(0.5);
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
     * @param longitudinalPosition Length.Rel; the position along the length of this CrossSectionElement
     * @return Length.Rel
     */
    public final Length.Rel getLateralBoundaryPosition(final LateralDirectionality lateralDirection,
        final Length.Rel longitudinalPosition)
    {
        return getLateralBoundaryPosition(lateralDirection, longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Construct a buffer geometry by offsetting the linear geometry line with a distance and constructing a so-called "buffer"
     * around it.
     * @param cse the CrossSectionElement to construct the contour for
     * @return the geometry belonging to this CrossSectionElement.
     * @throws OTSGeometryException when construction of the geometry fails
     * @throws NetworkException when the resulting contour is degenerate (cannot happen; we hope)
     */
    public static OTSLine3D constructContour(final CrossSectionElement cse) throws OTSGeometryException,
        NetworkException
    {
        OTSLine3D crossSectionDesignLine =
            cse.getParentLink().getDesignLine().offsetLine(cse.getDesignLineOffsetAtBegin().getSI(),
                cse.getDesignLineOffsetAtEnd().getSI());
        OTSLine3D rightBoundary =
            crossSectionDesignLine.offsetLine(-cse.getBeginWidth().getSI() / 2, -cse.getEndWidth().getSI() / 2);
        OTSLine3D leftBoundary =
            crossSectionDesignLine.offsetLine(cse.getBeginWidth().getSI() / 2, cse.getEndWidth().getSI() / 2);
        OTSPoint3D[] result = new OTSPoint3D[rightBoundary.size() + leftBoundary.size() + 1];
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
        return OTSLine3D.createAndCleanOTSLine3D(result);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        DirectedPoint centroid = this.contour.getLocation();
        return new DirectedPoint(centroid.x, centroid.y, getZ());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds()
    {
        return this.contour.getBounds();
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
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.parentLink == null) ? 0 : this.parentLink.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
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
        if (this.parentLink == null)
        {
            if (other.parentLink != null)
                return false;
        }
        else if (!this.parentLink.equals(other.parentLink))
            return false;
        return true;
    }

}
