package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.xml.bindings.types.EstimationType;

/**
 * Adapter for Estimation expression type.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class EstimationAdapter extends StaticFieldAdapter<Estimation, EstimationType>
{

    /**
     * Constructor.
     */
    public EstimationAdapter()
    {
        super(Estimation.class, EstimationType.class);
    }

}
