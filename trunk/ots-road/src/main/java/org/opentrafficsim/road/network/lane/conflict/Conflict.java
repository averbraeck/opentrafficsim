package org.opentrafficsim.road.network.lane.conflict;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Conflicts deal with traffic on different links/roads that need to consider each other as their paths may be in conflict
 * spatially. A single {@code Conflict} represents the one-sided consideration of a conflicting situation. I.e., what is
 * considered <i>a single conflict in traffic theory, is represented by two {@code Conflict}s</i>, one on each of the
 * conflicting {@code Lane}s.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Conflict extends AbstractLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20160915L;

    /** Conflict type, i.e. crossing, merge or split. */
    private final ConflictType conflictType;

    /** Conflict rule, i.e. priority, give way, stop or all-stop. */
    private final ConflictRule conflictRule;

    /** Accompanying other conflict. */
    private Conflict otherConflict;

    /**
     * @param lane lane where this conflict starts
     * @param longitudinalPosition position of start of conflict on lane
     * @param length length of the conflict along the lane centerline
     * @param geometry geometry of conflict
     * @param conflictRule conflict rule, i.e. priority, give way, stop or all-stop
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @throws NetworkException when the position on the lane is out of bounds
     */
    private Conflict(final Lane lane, final Length longitudinalPosition, final Length length, final OTSLine3D geometry,
        final ConflictType conflictType, final ConflictRule conflictRule) throws NetworkException
    {
        super(lane, longitudinalPosition, length, geometry);
        this.conflictType = conflictType;
        this.conflictRule = conflictRule;
    }

    /**
     * @return conflictType.
     */
    public ConflictType getConflictType()
    {
        return this.conflictType;
    }

    /**
     * @return conflictRule.
     */
    public ConflictRule getConflictRule()
    {
        return this.conflictRule;
    }

    /**
     * Creates a pair of conflicts.
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param lane1 lane of conflict 1
     * @param longitudinalPosition1 longitudinal position of conflict 1
     * @param length1 {@code Length} of conflict 1
     * @param geometry1 geometry of conflict 1
     * @param conflictRule1 conflict rule of conflict 1
     * @param lane2 lane of conflict 2
     * @param longitudinalPosition2 longitudinal position of conflict 2
     * @param length2 {@code Length} of conflict 2
     * @param geometry2 geometry of conflict 1
     * @param conflictRule2 conflict rule of conflict 2
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void generateConflictPair(final ConflictType conflictType, final Lane lane1,
        final Length longitudinalPosition1, final Length length1, final OTSLine3D geometry1,
        final ConflictRule conflictRule1, final Lane lane2, final Length longitudinalPosition2, final Length length2,
        final OTSLine3D geometry2, final ConflictRule conflictRule2) throws NetworkException
    {
        // lane, longitudinalPosition, length and geometry are checked in AbstractLaneBasedObject
        Throw.whenNull(conflictType, "Conflict type may not be null.");
        Throw.whenNull(conflictRule1, "Conflict rule may not be null.");
        Throw.whenNull(conflictRule2, "Conflict rule may not be null.");
        if (conflictType.equals(ConflictType.SPLIT))
        {
            // Split with split (on split)
            Throw.when(!conflictRule1.equals(ConflictRule.SPLIT) || !conflictRule2.equals(ConflictRule.SPLIT),
                NetworkException.class, "Both conflict rules should be split for conflict type split.");
        }
        else
        {
            // Priority with give-way/stop
            boolean check1 =
                conflictRule1.equals(ConflictRule.PRIORITY) && !conflictRule2.equals(ConflictRule.GIVE_WAY)
                    && !conflictRule2.equals(ConflictRule.STOP);
            boolean check2 =
                conflictRule2.equals(ConflictRule.PRIORITY) && !conflictRule1.equals(ConflictRule.GIVE_WAY)
                    && !conflictRule1.equals(ConflictRule.STOP);
            boolean check3 = conflictRule1.equals(ConflictRule.GIVE_WAY) && !conflictRule2.equals(ConflictRule.PRIORITY);
            boolean check4 = conflictRule2.equals(ConflictRule.GIVE_WAY) && !conflictRule1.equals(ConflictRule.PRIORITY);
            boolean check5 = conflictRule1.equals(ConflictRule.STOP) && !conflictRule2.equals(ConflictRule.PRIORITY);
            boolean check6 = conflictRule2.equals(ConflictRule.STOP) && !conflictRule1.equals(ConflictRule.PRIORITY);
            Throw.when(check1 || check2 || check3 || check4 || check5 || check6, NetworkException.class,
                "Conflict rules need to be a combination of 'PRIORITY' and 'GIVE_WAY' or 'STOP', "
                    + "if any of these types is used.");
            // All-stop with all-stop
            boolean check7 = conflictRule1.equals(ConflictRule.ALL_STOP) && !conflictRule2.equals(ConflictRule.ALL_STOP);
            boolean check8 = conflictRule2.equals(ConflictRule.ALL_STOP) && !conflictRule1.equals(ConflictRule.ALL_STOP);
            Throw.when(check7 || check8, NetworkException.class,
                "Conflict rule 'ALL_STOP' can only be combined with a conflict rule 'ALL_STOP'.");
            // No split
            Throw.when(conflictRule1.equals(ConflictRule.SPLIT) || conflictRule2.equals(ConflictRule.SPLIT),
                NetworkException.class, "Conflict rule 'SPLIT' may only be used on conflicts of type SPLIT.");
        }
        Conflict conf1 = new Conflict(lane1, longitudinalPosition1, length1, geometry1, conflictType, conflictRule1);
        Conflict conf2 = new Conflict(lane2, longitudinalPosition2, length2, geometry2, conflictType, conflictRule2);
        conf1.otherConflict = conf2;
        conf2.otherConflict = conf1;
    }
}
