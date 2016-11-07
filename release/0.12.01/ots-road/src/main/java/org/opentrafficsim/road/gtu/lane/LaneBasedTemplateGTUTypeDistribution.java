package org.opentrafficsim.road.gtu.lane;

import java.io.Serializable;

import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

/**
 * Distribution of LaneBasedTemplateGTUType.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param distributionOfLanebasedTemplateGTUType the distribution of LaneBasedTemplateGTUTypes
     */
    public LaneBasedTemplateGTUTypeDistribution(
        final Distribution<LaneBasedTemplateGTUType> distributionOfLanebasedTemplateGTUType)
    {
        this.distribution = distributionOfLanebasedTemplateGTUType;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException,
        GTUException
    {
        return this.distribution.draw().draw();
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator() throws ProbabilityException
    {
        return this.distribution.get(0).getObject().getSimulator();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedTemplateGTUTypeDistribution [distribution=" + this.distribution + "]";
    }

}
