package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.xml.bindings.types.HeadwayDistributionType;

/**
 * Adapter for HeadwayDistribution expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class HeadwayDistributionAdapter extends StaticFieldAdapter<HeadwayDistribution, HeadwayDistributionType>
{

    /**
     * Constructor.
     */
    public HeadwayDistributionAdapter()
    {
        super(HeadwayDistribution.class, HeadwayDistributionType.class);
    }

}
