package org.opentrafficsim.road.network.lane.conflict;

import java.util.HashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Default determination of priority based on link priority, or right-hand traffic. Note that this class is stateful as the
 * priorities are cached. So each conflict pair should receive a separate {@code DefaultConflictRule}. This rule is only for use
 * on merge and crossing conflicts. For split conflicts there is a separate rule {@code SplitConflictRule}.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 26 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultConflictRule implements ConflictRule
{

    /** Priority per conflict. */
    private Map<Conflict, ConflictPriority> map = null;

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

    /** {@inheritDoc} */
    @Override
    public ConflictPriority determinePriority(final Conflict conflict)
    {
        if (this.map == null)
        {
            ConflictPriority[] conflictPriorities = getConflictRules(conflict.getLane(), conflict.getLongitudinalPosition(),
                    conflict.getOtherConflict().getLane(), conflict.getOtherConflict().getLongitudinalPosition(),
                    conflict.getConflictType());
            this.map = new HashMap<>();
            this.map.put(conflict, conflictPriorities[0]);
            this.map.put(conflict.getOtherConflict(), conflictPriorities[1]);
        }
        Throw.when(!this.map.containsKey(conflict), IllegalArgumentException.class,
                "Conflict %s is not related to a conflict that was used before in the same conflict rule.", conflict);
        return this.map.get(conflict);
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
        Priority priority1 = lane1.getParentLink().getPriority();
        Priority priority2 = lane2.getParentLink().getPriority();
        if (priority1.isAllStop() && priority2.isAllStop())
        {
            conflictRules[0] = ConflictPriority.ALL_STOP;
            conflictRules[1] = ConflictPriority.ALL_STOP;
        }
        else if (priority1.equals(priority2))
        {
            // Based on right- or left-hand traffic
            DirectedPoint p1;
            DirectedPoint p2;
            try
            {
                p1 = lane1.getCenterLine().getLocation(longitudinalPosition1);
                p2 = lane2.getCenterLine().getLocation(longitudinalPosition2);
            }
            catch (OTSGeometryException exception)
            {
                throw new RuntimeException("Conflict position is not on its lane.", exception);
            }
            double diff = p2.getRotZ() - p1.getRotZ();
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
                conflictRules[0] = priority1.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
                conflictRules[1] = ConflictPriority.PRIORITY;
            }
            else
            {
                // 1 comes from the right
                conflictRules[0] = ConflictPriority.PRIORITY;
                conflictRules[1] = priority2.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
            }
        }
        else if (priority1.isPriority() && (priority2.isNone() || priority2.isStop()))
        {
            conflictRules[0] = ConflictPriority.PRIORITY;
            conflictRules[1] = priority2.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
        }
        else if (priority2.isPriority() && (priority1.isNone() || priority1.isStop()))
        {
            conflictRules[0] = priority1.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
            conflictRules[1] = ConflictPriority.PRIORITY;
        }
        else if (priority1.isNone() && priority2.isStop())
        {
            conflictRules[0] = ConflictPriority.PRIORITY;
            conflictRules[1] = ConflictPriority.STOP;
        }
        else if (priority2.isNone() && priority1.isStop())
        {
            conflictRules[0] = ConflictPriority.STOP;
            conflictRules[1] = ConflictPriority.PRIORITY;
        }
        else
        {
            throw new RuntimeException(
                    "Could not sort out conflict priority from link priorities " + priority1 + " and " + priority2);
        }
        return conflictRules;
    }

    /** {@inheritDoc} */
    @Override
    public ConflictRule clone(final OTSSimulatorInterface newSimulator)
    {
        return new DefaultConflictRule();
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DefaultConflictRule";
    }

}
