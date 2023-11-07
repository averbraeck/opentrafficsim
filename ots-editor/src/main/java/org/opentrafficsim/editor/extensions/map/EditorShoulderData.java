package org.opentrafficsim.editor.extensions.map;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.ShoulderData;

/**
 * Shoulder data for in the editor. Implements {@code ShoulderData} additionally to extending {@code EditorCrossSectionData}.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class EditorShoulderData extends EditorCrossSectionData implements ShoulderData
{

    /**
     * Constructor.
     * @param centerLine PolyLine2d; center line.
     * @param contour PolyLine2d; contour.
     */
    public EditorShoulderData(final PolyLine2d centerLine, final Polygon2d contour)
    {
        super(centerLine, contour);
    }

}
