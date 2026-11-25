package org.opentrafficsim.editor.extensions.map;

/**
 * Remembers minimum and maximum start and end offset, to return the mean of both.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class MiddleOffset
{

    /** Minimum start offset. */
    private double startOffsetMin = Double.POSITIVE_INFINITY;

    /** Maximum start offset. */
    private double startOffsetMax = Double.NEGATIVE_INFINITY;

    /** Minimum end offset. */
    private double endOffsetMin = Double.POSITIVE_INFINITY;

    /** Maximum end offset. */
    private double endOffsetMax = Double.NEGATIVE_INFINITY;

    /**
     * Constructor.
     */
    public MiddleOffset()
    {
        //
    }

    /**
     * Get start offset.
     * @return start offset
     */
    public double getStartOffset()
    {
        return .5 * (this.startOffsetMin + this.startOffsetMax);
    }

    /**
     * Add start offset.
     * @param startOffset start offset.
     */
    public void addStartOffset(final double startOffset)
    {
        this.startOffsetMin = Math.min(this.startOffsetMin, startOffset);
        this.startOffsetMax = Math.max(this.startOffsetMax, startOffset);
    }

    /**
     * Get end offset.
     * @return end offset
     */
    public double getEndOffset()
    {
        return .5 * (this.endOffsetMin + this.endOffsetMax);
    }

    /**
     * Add end offset.
     * @param endOffset end offset.
     */
    public void addEndOffset(final double endOffset)
    {
        this.endOffsetMin = Math.min(this.endOffsetMin, endOffset);
        this.endOffsetMax = Math.max(this.endOffsetMax, endOffset);
    }

}
