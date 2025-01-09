package org.opentrafficsim.road.gtu.generator.characteristics;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Distribution;

/**
 * Distribution of LaneBasedTemplateGTUType.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class LaneBasedGtuTemplateDistribution implements LaneBasedGtuCharacteristicsGenerator, Serializable
{

    /** */
    private static final long serialVersionUID = 20160000L;

    /** The Distribution of lane based template GTU types. */
    private final Distribution<LaneBasedGtuTemplate> distribution;

    /**
     * Construct a new LaneBasedTemplateGTUTypeDistribution.
     * @param distributionOfLanebasedTemplateGTUType the distribution of LaneBasedTemplateGTUTypes
     */
    public LaneBasedGtuTemplateDistribution(final Distribution<LaneBasedGtuTemplate> distributionOfLanebasedTemplateGTUType)
    {
        this.distribution = distributionOfLanebasedTemplateGTUType;
    }

    @Override
    public final LaneBasedGtuCharacteristics draw() throws ParameterException
    {
        return this.distribution.draw().draw();
    }

    @Override
    public final String toString()
    {
        return "LaneBasedTemplateGTUTypeDistribution [distribution=" + this.distribution + "]";
    }

}
