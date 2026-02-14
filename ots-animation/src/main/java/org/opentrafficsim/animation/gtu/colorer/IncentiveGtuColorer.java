package org.opentrafficsim.animation.gtu.colorer;

import java.util.Optional;

import org.opentrafficsim.road.gtu.tactical.lmrs.DesireBased;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Incentive;

/**
 * Colorer for desire from a specific incentive.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IncentiveGtuColorer extends DesireGtuColorer
{

    /** Incentive class. */
    private final String incentiveName;

    /**
     * Constructor.
     * @param incentiveClass incentive class
     */
    public IncentiveGtuColorer(final Class<? extends Incentive> incentiveClass)
    {
        this(incentiveClass, incentiveClass.getSimpleName());
    }

    /**
     * Constructor.
     * @param incentiveClass incentive class
     * @param incentiveName name of incentive
     */
    public IncentiveGtuColorer(final Class<? extends Incentive> incentiveClass, final String incentiveName)
    {
        super((gtu) -> gtu.getTacticalPlanner() instanceof DesireBased desireBased ? desireBased.getLatestDesire(incentiveClass)
                : Optional.empty());
        this.incentiveName = incentiveName;
    }

    @Override
    public final String getName()
    {
        return this.incentiveName;
    }

}
