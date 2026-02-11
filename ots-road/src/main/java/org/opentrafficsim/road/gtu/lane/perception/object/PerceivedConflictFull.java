package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.Optional;

import org.djunits.unit.LengthUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictPriority;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;

/**
 * Perceived intersection conflict with given input values.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedConflictFull extends PerceivedLaneBasedObjectBase implements PerceivedConflict
{

    /** Conflict type. */
    private final ConflictType conflictType;

    /** Conflict priority. */
    private final ConflictPriority conflictPriority;

    /** Length of the conflict in the conflicting directions. */
    private final Length conflictingLength;

    /**
     * Set of conflicting GTU's <i>completely</i> upstream of the <i>start</i> of the conflict ordered close to far from the
     * start of the conflict. Distance and overlap info concerns the conflict.
     */
    private final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> upstreamConflictingGTUs;

    /**
     * Set of conflicting GTU's (partially) downstream of the <i>start</i> of the conflict ordered close to far from the start
     * of conflict. Distance and overlap info concerns the conflict.
     */
    private final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> downstreamConflictingGTUs;

    /** Visibility on the conflicting lane within which conflicting vehicles are visible. */
    private final Length conflictingVisibility;

    /** Speed limit on the conflicting lane. */
    private final Speed conflictingSpeedLimit;

    /** Link of conflicting conflict. */
    private final CrossSectionLink conflictingLink;

    /** Stop line on the own lane. */
    private final PerceivedObject stopLine;

    /** Stop line on the conflicting lane. */
    private final PerceivedObject conflictingStopLine;

    /** Turn direction. */
    private final LateralDirectionality turn;

    /** Type of conflict rule. */
    private final Class<? extends ConflictRule> conflictRuleType;

    /** Distance of traffic light upstream on conflicting lane. */
    private Length conflictingTrafficLightDistance = null;

    /** Whether the conflict is permitted by the traffic light. */
    private boolean permitted = false;

    /** Width progression of conflict. */
    private final Width width;

    /**
     * Constructor.
     * @param id id
     * @param distance distance to conflict
     * @param length length of the conflict
     * @param conflictType conflict type
     * @param conflictPriority conflict priority
     * @param conflictRuleType conflict rule type
     * @param conflictingLength length of the conflict on the conflicting lane
     * @param upstreamConflictingGTUs conflicting GTU's upstream of the &lt;i&gt;start&lt;/i&gt; of the conflict
     * @param downstreamConflictingGTUs conflicting GTU's downstream of the &lt;i&gt;start&lt;/i&gt; of the conflict
     * @param conflictingVisibility visibility on the conflicting lane within which conflicting vehicles are visible
     * @param conflictingSpeedLimit speed limit on the conflicting lane
     * @param conflictingLink conflicting link
     * @param width width progression of conflict
     * @param stopLine stop line on the own lane
     * @param conflictingStopLine stop line on the conflicting lane
     * @param turn turn direction relative to other conflict
     * @param lane the lane
     */
    @SuppressWarnings("parameternumber")
    public PerceivedConflictFull(final String id, final Length distance, final Length length, final ConflictType conflictType,
            final ConflictPriority conflictPriority, final Class<? extends ConflictRule> conflictRuleType,
            final Length conflictingLength, final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> upstreamConflictingGTUs,
            final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> downstreamConflictingGTUs,
            final Length conflictingVisibility, final Speed conflictingSpeedLimit, final CrossSectionLink conflictingLink,
            final Width width, final PerceivedObject stopLine, final PerceivedObject conflictingStopLine,
            final LateralDirectionality turn, final Lane lane)
    {
        super(id, ObjectType.CONFLICT, length, Kinematics.staticAhead(distance), lane);
        Throw.whenNull(conflictType, "Conflict type may not be null.");
        Throw.whenNull(conflictPriority, "Conflict priority may not be null.");
        Throw.whenNull(conflictRuleType, "Conflict rule type may not be null.");
        Throw.whenNull(conflictingLength, "Conflict length may not be null.");
        Throw.whenNull(upstreamConflictingGTUs, "Upstreaem conflicting GTU's may not be null.");
        Throw.whenNull(downstreamConflictingGTUs, "Downstream conflicting GTU's may not be null.");
        Throw.whenNull(width, "Width may not be null.");
        Throw.whenNull(conflictingVisibility, "Conflict visibility may not be null.");
        Throw.whenNull(conflictingSpeedLimit, "Conflict speed limit may not be null.");
        Throw.whenNull(turn, "turn");
        this.conflictType = conflictType;
        this.conflictPriority = conflictPriority;
        this.conflictRuleType = conflictRuleType;
        this.conflictingLength = conflictingLength;
        this.upstreamConflictingGTUs = upstreamConflictingGTUs;
        this.downstreamConflictingGTUs = downstreamConflictingGTUs;
        this.conflictingVisibility = conflictingVisibility;
        this.conflictingSpeedLimit = conflictingSpeedLimit;
        this.conflictingLink = conflictingLink;
        this.width = width;
        this.stopLine = stopLine;
        this.conflictingStopLine = conflictingStopLine;
        this.turn = turn;
    }

    @Override
    public ConflictType getConflictType()
    {
        return this.conflictType;
    }

    @Override
    public boolean isCrossing()
    {
        return this.conflictType.equals(ConflictType.CROSSING);
    }

    @Override
    public boolean isMerge()
    {
        return this.conflictType.equals(ConflictType.MERGE);
    }

    @Override
    public boolean isSplit()
    {
        return this.conflictType.equals(ConflictType.SPLIT);
    }

    @Override
    public ConflictPriority getConflictPriority()
    {
        return this.conflictPriority;
    }

    @Override
    public Length getConflictingLength()
    {
        return this.conflictingLength;
    }

    @Override
    public PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getUpstreamConflictingGtus()
    {
        return this.upstreamConflictingGTUs;
    }

    @Override
    public PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getDownstreamConflictingGtus()
    {
        return this.downstreamConflictingGTUs;
    }

    @Override
    public Length getConflictingVisibility()
    {
        return this.conflictingVisibility;
    }

    @Override
    public Speed getConflictingSpeedLimit()
    {
        return this.conflictingSpeedLimit;
    }

    @Override
    public CrossSectionLink getConflictingLink()
    {
        return this.conflictingLink;
    }

    @Override
    public PerceivedObject getStopLine()
    {
        return this.stopLine;
    }

    @Override
    public PerceivedObject getConflictingStopLine()
    {
        return this.conflictingStopLine;
    }

    @Override
    public LateralDirectionality getTurn()
    {
        return this.turn;
    }

    @Override
    public Class<? extends ConflictRule> getConflictRuleType()
    {
        return this.conflictRuleType;
    }

    @Override
    public Optional<Length> getConflictingTrafficLightDistance()
    {
        return Optional.of(this.conflictingTrafficLightDistance);
    }

    @Override
    public boolean isPermitted()
    {
        return this.permitted;
    }

    /**
     * Set the distance of a traffic light upstream on the conflicting lane.
     * @param trafficLightDistance distance of a traffic light upstream on the conflicting lane.
     * @param permittedConflict whether the conflict is permitted by the traffic light
     */
    public void setConflictingTrafficLight(final Length trafficLightDistance, final boolean permittedConflict)
    {
        this.conflictingTrafficLightDistance = trafficLightDistance;
        this.permitted = permittedConflict;
    }

    @Override
    public Length getWidthAtFraction(final double fraction)
    {
        try
        {
            return this.width.getWidth(fraction);
        }
        catch (ValueRuntimeException exception)
        {
            throw new OtsRuntimeException("Unexpected exception: fraction could not be interpolated.", exception);
        }
    }

    /**
     * Width progression of conflict.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class Width
    {

        /** Fractions, from 0 to 1. */
        private final double[] fractions;

        /** Vector with widths. */
        private final LengthVector width;

        /**
         * Constructor.
         * @param fractions fractions, from 0 to 1
         * @param width vector of equal length with widths
         */
        public Width(final double[] fractions, final LengthVector width)
        {
            Throw.whenNull(fractions, "Fractions may not be null.");
            Throw.whenNull(width, "Width may not be null.");
            Throw.when(fractions.length != width.size(), IllegalArgumentException.class,
                    "Array and vector are not of equal length.");
            Throw.when(fractions.length < 2, IllegalArgumentException.class, "Input should at least contain 2 values.");
            Throw.when(fractions[0] != 0.0 || fractions[fractions.length - 1] != 1.0, IllegalArgumentException.class,
                    "Fractions should range from 0 to 1.");
            for (int i = 1; i < fractions.length; i++)
            {
                Throw.when(fractions[i] <= fractions[i - 1], IllegalArgumentException.class, "Fractions are not increasing.");
            }
            this.fractions = fractions;
            this.width = width;
        }

        /**
         * Returns the width at the given fraction.
         * @param fraction fraction from 0 to 1
         * @return width at the given fraction
         * @throws ValueRuntimeException when index is out of bounds
         */
        public Length getWidth(final double fraction) throws ValueRuntimeException
        {
            Throw.when(fraction < 0.0 || fraction > 1.0, IllegalArgumentException.class, "Fraction should be between 0 and 1.");
            if (fraction == 1.0)
            {
                return this.width.get(this.width.size() - 1);
            }
            for (int i = 0; i < this.fractions.length - 1; i++)
            {
                if (this.fractions[i] <= fraction && this.fractions[i + 1] > fraction)
                {
                    double r = (fraction - this.fractions[i]) / (this.fractions[i + 1] - this.fractions[i]);
                    return Length.interpolate(this.width.get(i), this.width.get(i + 1), r);
                }
            }
            throw new OtsRuntimeException("Unexpected exception: fraction could not be interpolated.");
        }

        /**
         * Returns a linear width progression.
         * @param startWidth start width
         * @param endWidth end width
         * @return linear width progression
         */
        public static Width linear(final Length startWidth, final Length endWidth)
        {
            Throw.whenNull(startWidth, "Start width may not be null.");
            Throw.whenNull(endWidth, "End width may not be null.");
            try
            {
                return new Width(new double[] {0.0, 1.0}, new LengthVector(new Length[] {startWidth, endWidth}, LengthUnit.SI));
            }
            catch (ValueRuntimeException exception)
            {
                throw new OtsRuntimeException("Unexpected exception: widths could not be put in a vector.", exception);
            }
        }

    }

    @Override
    public final String toString()
    {
        return String.format("Headway %s to object %s of type %s", getKinematics().getDistance(), getId(), getObjectType());
    }

}
