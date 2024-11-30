package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.xml.bindings.types.GapAcceptanceType;

/**
 * Adapter for GapAcceptance expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GapAcceptanceAdapter extends StaticFieldAdapter<GapAcceptance, GapAcceptanceType>
{

    /**
     * Constructor.
     */
    public GapAcceptanceAdapter()
    {
        super(GapAcceptance.class, GapAcceptanceType.class);
    }

}
