package org.opentrafficsim.editor.extensions.map;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.ShoulderData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.SliceInfo;

/**
 * Shoulder data for in the editor. Implements {@code ShoulderData} additionally to extending {@code EditorCrossSectionData}.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MapShoulderData extends MapCrossSectionData implements ShoulderData
{

    /** Start offset. */
    private final Length startOffset;

    /**
     * Constructor.
     * @param startOffset Length; start offset.
     * @param linkNode XsdTreeNode; node representing the element.
     * @param centerLine PolyLine2d; center line.
     * @param contour PolyLine2d; contour.
     * @param sliceInfo SliceInfo; slice info.
     */
    public MapShoulderData(final Length startOffset, final XsdTreeNode linkNode, final PolyLine2d centerLine,
            final Polygon2d contour, final SliceInfo sliceInfo)
    {
        super(linkNode, centerLine, contour, sliceInfo);
        this.startOffset = startOffset;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Shoulder " + getLinkId() + " " + this.startOffset;
    }

}
