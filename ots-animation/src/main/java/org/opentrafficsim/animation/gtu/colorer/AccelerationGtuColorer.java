package org.opentrafficsim.animation.gtu.colorer;

import java.util.Optional;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.colorer.AccelerationColorer;

/**
 * Color GTUs based on their current acceleration.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AccelerationGtuColorer extends AccelerationColorer<Gtu>
{

    /** Value function. */
    private static final Function<Gtu, Optional<Acceleration>> VALUE = (gtu) -> Optional.ofNullable(gtu.getAcceleration());

    /**
     * Constructor.
     * @param boundPaintScale bound paint scale
     */
    public AccelerationGtuColorer(final BoundsPaintScale boundPaintScale)
    {
        super(VALUE, boundPaintScale);
    }

    /**
     * Constructor.
     * @param minimumAcceleration minimum acceleration
     * @param maximumAcceleration maximum acceleration
     */
    public AccelerationGtuColorer(final Acceleration minimumAcceleration, final Acceleration maximumAcceleration)
    {
        super(VALUE, minimumAcceleration, maximumAcceleration);
    }

    /**
     * Constructor constructing a scale from -6.0m/s/s to 2m/s/s.
     */
    public AccelerationGtuColorer()
    {
        super(VALUE);
    }

}
