package org.opentrafficsim.road.gtu.generator.characteristics;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GTUException;

/**
 * Distribution of LaneBasedTemplateGTUType.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedTemplateGTUTypeDistribution implements LaneBasedGTUCharacteristicsGenerator, Serializable
{

    /** */
    private static final long serialVersionUID = 20160000L;

    /** The Distribution of lane based template GTU types. */
    private final Distribution<LaneBasedTemplateGTUType> distribution;

    /**
     * Construct a new LaneBasedTemplateGTUTypeDistribution.
     * @param distributionOfLanebasedTemplateGTUType Distribution&lt;LaneBasedTemplateGTUType&gt;; the distribution of
     *            LaneBasedTemplateGTUTypes
     */
    public LaneBasedTemplateGTUTypeDistribution(
            final Distribution<LaneBasedTemplateGTUType> distributionOfLanebasedTemplateGTUType)
    {
        this.distribution = distributionOfLanebasedTemplateGTUType;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GTUException
    {
        return this.distribution.draw().draw();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedTemplateGTUTypeDistribution [distribution=" + this.distribution + "]";
    }

}
