package org.opentrafficsim.road.network.lane;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Slice info. Can be used {@code Lane} but also the editor.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SliceInfo
{

    /** The offsets and widths at positions along the line, relative to the design line of the parent link. */
    private final List<CrossSectionSlice> crossSectionSlices;

    /** Length of element for which slices are defined. */
    private final Length length;
    
    /**
     * Constructor.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; slices.
     * @param length Length; length of element for which slices are defined.
     */
    public SliceInfo(final List<CrossSectionSlice> crossSectionSlices, final Length length)
    {
        this.crossSectionSlices = crossSectionSlices;
        this.length = length;
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
     * Calculate the slice the fractional position is in.
     * @param fractionalPosition double; the fractional position between 0 and 1 compared to the design line
     * @return int; the lower slice number between 0 and number of slices - 1.
     */
    private int calculateSliceNumber(final double fractionalPosition)
    {
        for (int i = 0; i < this.crossSectionSlices.size() - 1; i++)
        {
            if (fractionalPosition >= this.crossSectionSlices.get(i).getRelativeLength().si / this.length.si
                    && fractionalPosition <= this.crossSectionSlices.get(i + 1).getRelativeLength().si / this.length.si)
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
        double startPos = this.crossSectionSlices.get(sliceNumber).getRelativeLength().si / this.length.si;
        double endPos = this.crossSectionSlices.get(sliceNumber + 1).getRelativeLength().si / this.length.si;
        return (fractionalPosition - startPos) / (endPos - startPos);
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
    
}
