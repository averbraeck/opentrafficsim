package org.opentrafficsim.road.gtu.tactical.lmrs;

import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.MandatoryIncentive;

/**
 * Dummy desire disabling lane changes when used as the only incentive.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveDummy implements MandatoryIncentive, Stateless<IncentiveDummy>
{

    /** Singleton instance. */
    public static final IncentiveDummy SINGLETON = new IncentiveDummy();

    @Override
    public IncentiveDummy get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveDummy()
    {
        //
    }

    @Override
    public Desire determineDesire(final TacticalContextEgo context,
            final ImmutableLinkedHashMap<Class<? extends MandatoryIncentive>, Desire> mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {
        return new Desire(0, 0);
    }

    @Override
    public String toString()
    {
        return "IncentiveDummy";
    }

}
