package org.opentrafficsim.animation.gtu.colorer;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.colorer.SpeedColorer;

/**
 * Color GTU depending on their speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SpeedGtuColorer extends SpeedColorer<Gtu>
{

    /** */
    private static final long serialVersionUID = 20150000L;

    /** Value function. */
    private static final Function<Gtu, Speed> VALUE = (gtu) -> gtu.getSpeed();

    /**
     * Constructor.
     * @param boundPaintScale bounds paint scale, based on values in km/h
     */
    public SpeedGtuColorer(final BoundsPaintScale boundPaintScale)
    {
        super(VALUE, boundPaintScale);
    }

    /**
     * Constructor.
     * @param maximumSpeed maximum speed
     */
    public SpeedGtuColorer(final Speed maximumSpeed)
    {
        super(VALUE, maximumSpeed);
    }

    /**
     * Constructor constructing a range to 150km/h.
     */
    public SpeedGtuColorer()
    {
        super(VALUE);
    }

}
