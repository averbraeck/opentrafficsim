package org.opentrafficsim.road.network.conflict;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.CrossSectionLink.Priority;

/**
 * Default determination of priority based on link priority, or right-hand traffic. Note that this class is stateful as the
 * priorities are cached. So each conflict pair should receive a separate {@code DefaultConflictRule}. This rule is only for use
 * on merge and crossing conflicts. For split conflicts there is a separate rule {@code SplitConflictRule}.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DefaultConflictRule implements ConflictRule
{

    /** Priority per conflict. */
    private Map<String, ConflictPriority> map = null;

    // Throw.whenNull(priority1, "Conflict rule may not be null.");
    // Throw.whenNull(priority2, "Conflict rule may not be null.");
    // if (priority1.equals(ConflictPriority.SPLIT) || priority2.equals(ConflictPriority.SPLIT))
    // {
    // // Split with split (on split)
    // Throw.when(!priority1.equals(ConflictPriority.SPLIT) || !priority2.equals(ConflictPriority.SPLIT),
    // NetworkException.class, "Both conflict rules should be split for conflict type split.");
    // }
    // else
    // {
    // // Priority with give-way/stop
    // boolean check1 = priority1.equals(ConflictPriority.PRIORITY) && !priority2.equals(ConflictPriority.GIVE_WAY)
    // && !priority2.equals(ConflictPriority.STOP);
    // boolean check2 = priority2.equals(ConflictPriority.PRIORITY) && !priority1.equals(ConflictPriority.GIVE_WAY)
    // && !priority1.equals(ConflictPriority.STOP);
    // boolean check3 =
    // priority1.equals(ConflictPriority.GIVE_WAY) && !priority2.equals(ConflictPriority.PRIORITY);
    // boolean check4 =
    // priority2.equals(ConflictPriority.GIVE_WAY) && !priority1.equals(ConflictPriority.PRIORITY);
    // boolean check5 = priority1.equals(ConflictPriority.STOP) && !priority2.equals(ConflictPriority.PRIORITY);
    // boolean check6 = priority2.equals(ConflictPriority.STOP) && !priority1.equals(ConflictPriority.PRIORITY);
    // Throw.when(check1 || check2 || check3 || check4 || check5 || check6, NetworkException.class,
    // "Conflict rules need to be a combination of 'PRIORITY' and 'GIVE_WAY' or 'STOP', "
    // + "if any of these types is used.");
    // // All-stop with all-stop
    // boolean check7 =
    // priority1.equals(ConflictPriority.ALL_STOP) && !priority2.equals(ConflictPriority.ALL_STOP);
    // boolean check8 =
    // priority2.equals(ConflictPriority.ALL_STOP) && !priority1.equals(ConflictPriority.ALL_STOP);
    // Throw.when(check7 || check8, NetworkException.class,
    // "Conflict rule 'ALL_STOP' can only be combined with a conflict rule 'ALL_STOP'.");
    // // No split
    // Throw.when(priority1.equals(ConflictPriority.SPLIT) || priority2.equals(ConflictPriority.SPLIT),
    // NetworkException.class, "Conflict rule 'SPLIT' may only be used on conflicts of type SPLIT.");
    // }

    /**
     * Constructor.
     */
    public DefaultConflictRule()
    {
        //
    }

    @Override
    public ConflictPriority determinePriority(final Conflict conflict)
    {
        if (this.map == null)
        {
            ConflictPriority[] conflictPriorities = getConflictRules(conflict.getLane(), conflict.getLongitudinalPosition(),
                    conflict.getOtherConflict().getLane(), conflict.getOtherConflict().getLongitudinalPosition(),
                    conflict.getConflictType());
            this.map = new LinkedHashMap<>();
            this.map.put(conflict.getId(), conflictPriorities[0]);
            this.map.put(conflict.getOtherConflict().getId(), conflictPriorities[1]);
        }
        ConflictPriority out = this.map.get(conflict.getId());
        Throw.when(out == null, IllegalArgumentException.class,
                "Conflict %s is not related to a conflict that was used before in the same conflict rule.", conflict);
        return out;
    }

    /**
     * Determine conflict rules.
     * @param lane1 lane 1
     * @param longitudinalPosition1 position 1
     * @param lane2 lane 2
     * @param longitudinalPosition2 position 2
     * @param conflictType conflict type
     * @return conflict rule 1 and 2
     */
    private static ConflictPriority[] getConflictRules(final Lane lane1, final Length longitudinalPosition1, final Lane lane2,
            final Length longitudinalPosition2, final ConflictType conflictType)
    {
        Throw.when(conflictType.equals(ConflictType.SPLIT), UnsupportedOperationException.class,
                "DefaultConflictRule is not for use on a split conflict. Use SplitConflictRule instead.");
        ConflictPriority[] conflictRules = new ConflictPriority[2];
        Priority priority1 = lane1.getLink().getPriority();
        Priority priority2 = lane2.getLink().getPriority();
        if (priority1.isAllStop() && priority2.isAllStop())
        {
            conflictRules[0] = ConflictPriority.ALL_STOP;
            conflictRules[1] = ConflictPriority.ALL_STOP;
        }
        else if (priority1.equals(priority2) || (priority1.isYield() && priority2.isStop())
                || (priority2.isYield() && priority1.isStop()))
        {
            Throw.when(priority1.isBusStop(), IllegalArgumentException.class,
                    "Both priorities are 'bus stop', which is not allowed. Use BusStopConflictRule for bus stops.");
            // Based on right- or left-hand traffic
            DirectedPoint2d p1 = lane1.getCenterLine().getLocation(longitudinalPosition1);
            DirectedPoint2d p2 = lane2.getCenterLine().getLocation(longitudinalPosition2);
            double diff = p2.getDirZ() - p1.getDirZ();
            while (diff > Math.PI)
            {
                diff -= 2 * Math.PI;
            }
            while (diff < -Math.PI)
            {
                diff += 2 * Math.PI;
            }
            if (diff > 0.0)
            {
                // 2 comes from the right
                conflictRules[0] = priority1.isStop() ? ConflictPriority.STOP : ConflictPriority.YIELD;
                conflictRules[1] = ConflictPriority.PRIORITY;
            }
            else
            {
                // 1 comes from the right
                conflictRules[0] = ConflictPriority.PRIORITY;
                conflictRules[1] = priority2.isStop() ? ConflictPriority.STOP : ConflictPriority.YIELD;
            }
        }
        else if ((priority1.isPriority() || priority1.isNone()) // note, both NONE already captured
                && (priority2.isNone() || priority2.isYield() || priority2.isStop()))
        {
            conflictRules[0] = ConflictPriority.PRIORITY;
            conflictRules[1] = priority2.isStop() ? ConflictPriority.STOP : ConflictPriority.YIELD;
        }
        else if ((priority2.isPriority() || priority2.isNone())
                && (priority1.isNone() || priority1.isYield() || priority1.isStop()))
        {
            conflictRules[0] = priority1.isStop() ? ConflictPriority.STOP : ConflictPriority.YIELD;
            conflictRules[1] = ConflictPriority.PRIORITY;
        }
        else
        {
            throw new IllegalArgumentException(
                    "Could not sort out conflict priority from link priorities " + priority1 + " and " + priority2);
        }
        return conflictRules;
    }

    @Override
    public final String toString()
    {
        return "DefaultConflictRule";
    }

}
