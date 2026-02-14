package org.opentrafficsim.road.gtu.tactical.lmrs;

import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Determines lane change desire in order to adhere to keeping right or left. Such desire only exists if the route and speed
 * (considered within an anticipation distance) are not affected on the adjacent lane. The level of lane change desire is only
 * sufficient to overcome the lowest threshold for free lane changes.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveKeep implements VoluntaryIncentive, Stateless<IncentiveKeep>
{

    /** Free lane change threshold parameter type. */
    protected static final ParameterTypeDouble DFREE = LmrsParameters.DFREE;

    /** Singleton instance. */
    public static final IncentiveKeep SINGLETON = new IncentiveKeep();

    @Override
    public IncentiveKeep get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveKeep()
    {
        //
    }

    @Override
    public Desire determineDesire(final TacticalContextEgo context, final Desire mandatoryDesire,
            final ImmutableLinkedHashMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        Desire voluntarySpeed = voluntaryDesire.get(IncentiveSpeedWithCourtesy.class);
        if ((voluntarySpeed != null && voluntarySpeed.right() < 0) || mandatoryDesire.right() < 0
                || !context.getPerception().getLaneStructure().exists(RelativeLane.RIGHT))
        {
            // no desire to go right if more dominant incentives provide a negative desire to go right
            return new Desire(0, 0);
        }
        // keep right with dFree
        return new Desire(0, context.getParameters().getParameter(DFREE));
    }

    @Override
    public String toString()
    {
        return "IncentiveKeep";
    }

}
