package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.xml.bindings.types.TailgatingType;

/**
 * Adapter for Tailgating expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TailgatingAdapter extends StaticFieldAdapter<Tailgating, TailgatingType>
{

    /**
     * Constructor.
     */
    public TailgatingAdapter()
    {
        super(Tailgating.class, TailgatingType.class);
    }

}
