package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.xml.bindings.types.SynchronizationType;

/**
 * Adapter for Synchronization expression type.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SynchronizationAdapter extends StaticFieldAdapter<Synchronization, SynchronizationType>
{

    /**
     * Constructor.
     */
    public SynchronizationAdapter()
    {
        super(Synchronization.class, SynchronizationType.class);
    }

}
