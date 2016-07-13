package org.opentrafficsim.road.gtu.lane.perception;

import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayConflict extends AbstractHeadway
{

    /** */
    private static final long serialVersionUID = 20160602L;

    /** Conflict type. */
    private final ConflictType conflictType;

    /** Conflict rule. */
    private final ConflictRule conflictRule;

    /** Length of the conflict in the conflicting directions. */
    private final Length conflictingLength;

    /**
     * Set of conflicting GTU's <i>completely</i> upstream of the <i>start</i> of the conflict ordered close to far from the
     * start of the conflict. Distance and overlap info concerns the conflict.
     */
    private final SortedSet<AbstractHeadwayGTU> upstreamConflictingGTUs;

    /**
     * Set of conflicting GTU's (partially) downstream of the <i>start</i> of the conflict ordered close to far from the start
     * of conflict. Distance and overlap info concerns the conflict.
     */
    private final SortedSet<AbstractHeadwayGTU> downstreamConflictingGTUs;

    /** Visibility on the conflicting lane within which conflicting vehicles are visible. */
    private final Length conflictingVisibility;

    /** Speed limit on the conflicting lane. */
    private final Speed conflictingSpeedLimit;

    /** Stop line on the own lane. */
    private final HeadwayStopLine stopLine;

    /** Stop line on the conflicting lane. */
    private final HeadwayStopLine conflictingStopLine;

    /**
     * Constructor.
     * @param conflictType conflict type
     * @param conflictRule conflict rule
     * @param id id
     * @param distance distance
     * @param length length of the conflict
     * @param conflictingLength length of the conflict on the conflicting lane
     * @param upstreamConflictingGTUs conflicting GTU's upstream of the <i>start</i> of the conflict
     * @param downstreamConflictingGTUs conflicting GTU's downstream of the <i>start</i> of the conflict
     * @param conflictingVisibility visibility on the conflicting lane within which conflicting vehicles are visible
     * @param conflictingSpeedLimit speed limit on the conflicting lane
     * @param stopLine stop line on the own lane
     * @param conflictingStopLine stop line on the conflicting lane
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public HeadwayConflict(final ConflictType conflictType, final ConflictRule conflictRule, final String id,
        final Length distance, final Length length, final Length conflictingLength,
        final SortedSet<AbstractHeadwayGTU> upstreamConflictingGTUs,
        final SortedSet<AbstractHeadwayGTU> downstreamConflictingGTUs, final Length conflictingVisibility,
        final Speed conflictingSpeedLimit, final HeadwayStopLine stopLine, final HeadwayStopLine conflictingStopLine)
        throws GTUException
    {
        super(ObjectType.CONFLICT, id, distance, length);
        Throw.whenNull(conflictType, "Conflict type may not be null.");
        Throw.whenNull(conflictRule, "Conflict rule may not be null.");
        Throw.whenNull(id, "Conflict id may not be null.");
        Throw.whenNull(distance, "Conflict distance may not be null.");
        Throw.whenNull(conflictingLength, "Conflict length may not be null.");
        Throw.whenNull(upstreamConflictingGTUs, "Upstreaem conflicting GTU's may not be null.");
        Throw.whenNull(downstreamConflictingGTUs, "Downstream conflicting GTU's may not be null.");
        Throw.whenNull(conflictingVisibility, "Conflict visibility may not be null.");
        Throw.whenNull(conflictingSpeedLimit, "Conflict speed limit may not be null.");
        this.conflictType = conflictType;
        this.conflictRule = conflictRule;
        this.conflictingLength = conflictingLength;
        this.upstreamConflictingGTUs = upstreamConflictingGTUs;
        this.downstreamConflictingGTUs = downstreamConflictingGTUs;
        this.conflictingVisibility = conflictingVisibility;
        this.conflictingSpeedLimit = conflictingSpeedLimit;
        this.stopLine = stopLine;
        this.conflictingStopLine = conflictingStopLine;
    }

    /**
     * Constructor without stop lines.
     * @param conflictType conflict type
     * @param conflictRule conflict rule
     * @param id id
     * @param distance distance
     * @param length length of the conflict
     * @param conflictingLength length of the conflict on the conflicting lane
     * @param upstreamConflictingGTUs conflicting GTU's upstream of the <i>start</i> of the conflict
     * @param downstreamConflictingGTUs conflicting GTU's downstream of the <i>start</i> of the conflict
     * @param conflictingVisibility visibility on the conflicting lane within which conflicting vehicles are visible
     * @param conflictingSpeedLimit speed limit on the conflicting lane
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public HeadwayConflict(final ConflictType conflictType, final ConflictRule conflictRule, final String id,
        final Length distance, final Length length, final Length conflictingLength,
        final SortedSet<AbstractHeadwayGTU> upstreamConflictingGTUs,
        final SortedSet<AbstractHeadwayGTU> downstreamConflictingGTUs, final Length conflictingVisibility,
        final Speed conflictingSpeedLimit) throws GTUException
    {
        this(conflictType, conflictRule, id, distance, length, conflictingLength, upstreamConflictingGTUs,
            downstreamConflictingGTUs, conflictingVisibility, conflictingSpeedLimit, null, null);
    }

    /**
     * Returns the conflict type.
     * @return conflict type
     */
    public final ConflictType getConflictType()
    {
        return this.conflictType;
    }

    /**
     * Returns whether this is a crossing conflict.
     * @return whether this is a crossing conflict
     */
    public final boolean isCrossing()
    {
        return this.conflictType == ConflictType.CROSSING;
    }

    /**
     * Returns whether this is a merge conflict.
     * @return whether this is a merge conflict
     */
    public final boolean isMerge()
    {
        return this.conflictType == ConflictType.MERGE;
    }

    /**
     * Returns the conflict rule.
     * @return conflict rule
     */
    public final ConflictRule getConflictRule()
    {
        return this.conflictRule;
    }

    /**
     * Returns whether this is a priority conflict.
     * @return whether this is a priority conflict
     */
    public final boolean isPriority()
    {
        return this.conflictRule == ConflictRule.PRIORITY;
    }

    /**
     * Returns whether this is a give-way conflict.
     * @return whether this is a give-way conflict
     */
    public final boolean isGiveWay()
    {
        return this.conflictRule == ConflictRule.GIVE_WAY;
    }

    /**
     * Returns whether this is a stop conflict.
     * @return whether this is a stop conflict
     */
    public final boolean isStop()
    {
        return this.conflictRule == ConflictRule.STOP;
    }

    /**
     * Returns whether this is a all-stop conflict.
     * @return whether this is a all-stop conflict
     */
    public final boolean isAllStop()
    {
        return this.conflictRule == ConflictRule.ALL_STOP;
    }

    /**
     * Returns the length of the conflict on the conflicting lane.
     * @return length of the conflict on the conflicting lane
     */
    public final Length getConflictingLength()
    {
        return this.conflictingLength;
    }

    /**
     * Returns a set of conflicting GTU's upstream of the <i>start</i> of the conflict ordered close to far from the conflict.
     * @return set of conflicting GTU's upstream of the <i>start</i> of the conflict ordered close to far from the conflict
     */
    public final SortedSet<AbstractHeadwayGTU> getUpstreamConflictingGTUs()
    {
        return new TreeSet<>(this.upstreamConflictingGTUs);
    }

    /**
     * Returns a set of conflicting GTU's downstream of the <i>start</i> of the conflict ordered close to far from the conflict.
     * Distance is given relative to the <i>end</i> of the conflict, or null for conflicting vehicles on the conflict. In the
     * latter case the overlap is used.
     * @return set of conflicting GTU's downstream of the <i>start</i> of the conflict ordered close to far from the conflict
     */
    public final SortedSet<AbstractHeadwayGTU> getDownstreamConflictingGTUs()
    {
        return new TreeSet<>(this.downstreamConflictingGTUs);
    }

    /**
     * Returns the visibility on the conflicting lane within which conflicting vehicles are visible. All upstream conflicting
     * GTUs have a distance smaller than the visibility. Depending on a limited visibility, a certain (lower) speed may be
     * required while approaching the conflict.
     * @return visibility on the conflicting lane within which conflicting vehicles are visible
     */
    public final Length getConflictingVisibility()
    {
        return this.conflictingVisibility;
    }

    /**
     * Returns the speed limit on the conflicting lane.
     * @return speed limit on the conflicting lane
     */
    public final Speed getConflictingSpeedLimit()
    {
        return this.conflictingSpeedLimit;
    }

    /**
     * Returns the stop line.
     * @return stop line
     */
    public final HeadwayStopLine getStopLine()
    {
        return this.stopLine;
    }

    /**
     * Returns the stop line on the conflicting lane.
     * @return stop line
     */
    public final HeadwayStopLine getConflictingStopLine()
    {
        return this.conflictingStopLine;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("Headway %s to object %s of type %s", getDistance(), getId(), getObjectType());
    }

    /**
     * Type of conflict.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 2, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum ConflictType
    {
        /** Crossing conflict. */
        CROSSING,

        /** Merge conflict. */
        MERGE;
    }

    /**
     * Rule of conflict.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 2, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum ConflictRule
    {
        /** Have priority. */
        PRIORITY,

        /** Give priority. */
        GIVE_WAY,

        /** Stop and give priority. */
        STOP,

        /** All-way stop. */
        ALL_STOP;
    }

}
