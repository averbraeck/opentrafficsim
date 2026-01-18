package org.opentrafficsim.animation.gtu.colorer;

import java.util.Optional;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Colorer for total desire.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TotalDesireGtuColorer extends DesireGtuColorer
{

    /**
     * Constructor.
     */
    public TotalDesireGtuColorer()
    {
        super((gtu) -> gtu.getParameters().contains(LmrsParameters.DLEFT) && gtu.getParameters().contains(LmrsParameters.DRIGHT)
                ? Optional.of(new Desire(gtu.getParameters().getOptionalParameter(LmrsParameters.DLEFT)
                        .get(),
                        gtu.getParameters().getOptionalParameter(LmrsParameters.DRIGHT).get()))
                : Optional.empty());
    }

    @Override
    public final String getName()
    {
        return "Total desire";
    }

}
