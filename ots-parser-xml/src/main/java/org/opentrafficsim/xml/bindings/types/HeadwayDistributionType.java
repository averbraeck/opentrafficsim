package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;

/**
 * Expression type with HeadwayDistribution value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayDistributionType extends ExpressionType<HeadwayDistribution>
{

    /**
     * Constructor with value.
     * @param value HeadwayDistribution; value, may be {@code null}.
     */
    public HeadwayDistributionType(final HeadwayDistribution value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public HeadwayDistributionType(final String expression)
    {
        super(expression);
    }

}
