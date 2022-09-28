package org.opentrafficsim.road.gtu.generator.characteristics;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * Distribution of LaneBasedTemplateGtuType.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedTemplateGtuTypeDistribution implements LaneBasedGTUCharacteristicsGenerator, Serializable
{

    /** */
    private static final long serialVersionUID = 20160000L;

    /** The Distribution of lane based template GTU types. */
    private final Distribution<LaneBasedTemplateGtuType> distribution;

    /**
     * Construct a new LaneBasedTemplateGtuTypeDistribution.
     * @param distributionOfLanebasedTemplateGtuType Distribution&lt;LaneBasedTemplateGtuType&gt;; the distribution of
     *            LaneBasedTemplateGtuTypes
     */
    public LaneBasedTemplateGtuTypeDistribution(
            final Distribution<LaneBasedTemplateGtuType> distributionOfLanebasedTemplateGtuType)
    {
        this.distribution = distributionOfLanebasedTemplateGtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GtuException
    {
        return this.distribution.draw().draw();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedTemplateGtuTypeDistribution [distribution=" + this.distribution + "]";
    }

}
