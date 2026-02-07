package org.opentrafficsim.editor.extensions.map;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.ShoulderData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.CrossSectionGeometry;

/**
 * Shoulder data for in the editor. Implements {@code ShoulderData} additionally to extending {@code EditorCrossSectionData}.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapShoulderData extends MapCrossSectionData implements ShoulderData
{

    /** Start offset. */
    private final Length startOffset;

    /**
     * Constructor.
     * @param linkNode node representing the element.
     * @param geometry geometry
     */
    public MapShoulderData(final XsdTreeNode linkNode, final CrossSectionGeometry geometry)
    {
        super(linkNode, geometry);
        this.startOffset = Length.ofSI(geometry.offset().get(0.0));
    }

    @Override
    public String toString()
    {
        return "Shoulder " + getLinkId() + " " + this.startOffset;
    }

}
