package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.xml.bindings.types.StripeLateralSyncType;

/**
 * Adapter for StripeLateralSync expression type.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StripeLateralSyncAdapter extends StaticFieldAdapter<StripeLateralSync, StripeLateralSyncType>
{

    /**
     * Constructor.
     */
    public StripeLateralSyncAdapter()
    {
        super(StripeLateralSync.class, StripeLateralSyncType.class);
    }

}
