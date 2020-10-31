package org.opentrafficsim.draw.core;

import java.awt.Color;

import org.jfree.chart.renderer.PaintScale;

/**
 * Paint scale that forces the output to be a color. This is used inside XYInterpolatedBlockRenderer.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface ColorPaintScale extends PaintScale
{

    /** {@inheritDoc} */
    @Override
    Color getPaint(double value);

}
