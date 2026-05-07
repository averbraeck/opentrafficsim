package org.opentrafficsim.animation;

import java.awt.Color;

import org.jfree.chart.renderer.PaintScale;

/**
 * Paint scale that forces the output to be a color. This is used inside XYInterpolatedBlockRenderer.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public interface ColorPaintScale extends PaintScale
{

    @Override
    Color getPaint(double value);

}
