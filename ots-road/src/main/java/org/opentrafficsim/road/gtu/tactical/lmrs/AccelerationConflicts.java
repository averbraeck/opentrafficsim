package org.opentrafficsim.road.gtu.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.tactical.Blockable;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.tactical.util.ConflictUtil.ConflictPlans;

/**
 * Conflicts acceleration incentive.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class AccelerationConflicts implements AccelerationIncentive, Blockable
{

    /** Set of yield plans at conflicts with priority. Remembering for static model. */
    // @docs/06-behavior/tactical-planner/#modular-utilities
    private final ConflictPlans conflictPlans = new ConflictPlans();

    /**
     * Constructor.
     */
    public AccelerationConflicts()
    {
        //
    }

    @Override
    public final Acceleration accelerate(final TacticalContextEgo context, final RelativeLane lane, final Length mergeDistance)
            throws ParameterException, GtuException
    {
        return ConflictUtil.approachConflicts(context, this.conflictPlans, lane, mergeDistance, true);
    }

    @Override
    public boolean isBlocking()
    {
        return this.conflictPlans.isBlocking();
    }

    @Override
    public final String toString()
    {
        return "AccelerationConflicts";
    }

}
