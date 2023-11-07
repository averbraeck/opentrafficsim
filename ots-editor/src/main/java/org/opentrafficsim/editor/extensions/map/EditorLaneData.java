package org.opentrafficsim.editor.extensions.map;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;

/**
 * Lane data for in the editor.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class EditorLaneData extends EditorCrossSectionData implements LaneData
{
    /** Id. */
    private final String id;

    /**
     * Constructor.
     * @param id String; id.
     * @param centerLine PolyLine2d; center line.
     * @param contour PolyLine2d; contour.
     */
    public EditorLaneData(final String id, final PolyLine2d centerLine, final Polygon2d contour)
    {
        super(centerLine, contour);
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getCenterLine()
    {
        return this.centerLine;
    }

}
