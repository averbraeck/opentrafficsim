package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.conflict.ConflictPriority;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayConflict extends AbstractHeadwayCopy
{

    /** */
    private static final long serialVersionUID = 20160602L;

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
    private final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> upstreamConflictingGTUs;

    /**
     * Set of conflicting GTU's (partially) downstream of the <i>start</i> of the conflict ordered close to far from the start
     * of conflict. Distance and overlap info concerns the conflict.
     */
    private final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> downstreamConflictingGTUs;

    /** Visibility on the conflicting lane within which conflicting vehicles are visible. */
    private final Length conflictingVisibility;

    /** Speed limit on the conflicting lane. */
    private final Speed conflictingSpeedLimit;

    /** Link of conflicting conflict. */
    private final CrossSectionLink conflictingLink;

    /** Stop line on the own lane. */
    private final HeadwayStopLine stopLine;

    /** Stop line on the conflicting lane. */
    private final HeadwayStopLine conflictingStopLine;

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
     * @param conflictType ConflictType; conflict type
     * @param conflictPriority ConflictPriority; conflict priority
     * @param conflictRuleType Class&lt;? extends ConflictRule&gt;; conflict rule type
     * @param id String; id
     * @param distance Length; distance
     * @param length Length; length of the conflict
     * @param conflictingLength Length; length of the conflict on the conflicting lane
     * @param upstreamConflictingGTUs PerceptionCollectable&lt;HeadwayGTU,LaneBasedGTU&gt;; conflicting GTU's upstream of the
     *            &lt;i&gt;start&lt;/i&gt; of the conflict
     * @param downstreamConflictingGTUs PerceptionCollectable&lt;HeadwayGTU,LaneBasedGTU&gt;; conflicting GTU's downstream of
     *            the &lt;i&gt;start&lt;/i&gt; of the conflict
     * @param conflictingVisibility Length; visibility on the conflicting lane within which conflicting vehicles are visible
     * @param conflictingSpeedLimit Speed; speed limit on the conflicting lane
     * @param conflictingLink CrossSectionLink; conflicting link
     * @param width Width; width progression of conflict
     * @param stopLine HeadwayStopLine; stop line on the own lane
     * @param conflictingStopLine HeadwayStopLine; stop line on the conflicting lane
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public HeadwayConflict(final ConflictType conflictType, final ConflictPriority conflictPriority,
            final Class<? extends ConflictRule> conflictRuleType, final String id, final Length distance, final Length length,
            final Length conflictingLength, final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> upstreamConflictingGTUs,
            final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> downstreamConflictingGTUs, final Length conflictingVisibility,
            final Speed conflictingSpeedLimit, final CrossSectionLink conflictingLink, final Width width,
            final HeadwayStopLine stopLine, final HeadwayStopLine conflictingStopLine) throws GTUException
    {
        super(ObjectType.CONFLICT, id, distance, length);
        Throw.whenNull(conflictType, "Conflict type may not be null.");
        Throw.whenNull(conflictPriority, "Conflict priority may not be null.");
        Throw.whenNull(conflictRuleType, "Conflict rule type may not be null.");
        Throw.whenNull(id, "Conflict id may not be null.");
        Throw.whenNull(distance, "Conflict distance may not be null.");
        Throw.whenNull(conflictingLength, "Conflict length may not be null.");
        Throw.whenNull(upstreamConflictingGTUs, "Upstreaem conflicting GTU's may not be null.");
        Throw.whenNull(downstreamConflictingGTUs, "Downstream conflicting GTU's may not be null.");
        Throw.whenNull(width, "Width may not be null.");
        Throw.whenNull(conflictingVisibility, "Conflict visibility may not be null.");
        Throw.whenNull(conflictingSpeedLimit, "Conflict speed limit may not be null.");
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
    }

    /**
     * Constructor without stop lines.
     * @param conflictType ConflictType; conflict type
     * @param conflictPriority ConflictPriority; conflict priority
     * @param conflictRuleType Class&lt;? extends ConflictRule&gt;; conflict rule type
     * @param id String; id
     * @param distance Length; distance
     * @param length Length; length of the conflict
     * @param conflictingLength Length; length of the conflict on the conflicting lane
     * @param upstreamConflictingGTUs PerceptionCollectable&lt;HeadwayGTU,LaneBasedGTU&gt;; conflicting GTU's upstream of the
     *            &lt;i&gt;start&lt;/i&gt; of the conflict
     * @param downstreamConflictingGTUs PerceptionCollectable&lt;HeadwayGTU,LaneBasedGTU&gt;; conflicting GTU's downstream of
     *            the &lt;i&gt;start&lt;/i&gt; of the conflict
     * @param conflictingVisibility Length; visibility on the conflicting lane within which conflicting vehicles are visible
     * @param conflictingSpeedLimit Speed; speed limit on the conflicting lane
     * @param conflictingLink CrossSectionLink; conflicting link
     * @param width Width; width progression of conflict
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public HeadwayConflict(final ConflictType conflictType, final ConflictPriority conflictPriority,
            final Class<? extends ConflictRule> conflictRuleType, final String id, final Length distance, final Length length,
            final Length conflictingLength, final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> upstreamConflictingGTUs,
            final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> downstreamConflictingGTUs, final Length conflictingVisibility,
            final Speed conflictingSpeedLimit, final CrossSectionLink conflictingLink, final Width width) throws GTUException
    {
        this(conflictType, conflictPriority, conflictRuleType, id, distance, length, conflictingLength, upstreamConflictingGTUs,
                downstreamConflictingGTUs, conflictingVisibility, conflictingSpeedLimit, conflictingLink, width, null, null);
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
        return this.conflictType.equals(ConflictType.CROSSING);
    }

    /**
     * Returns whether this is a merge conflict.
     * @return whether this is a merge conflict
     */
    public final boolean isMerge()
    {
        return this.conflictType.equals(ConflictType.MERGE);
    }

    /**
     * Returns whether this is a split conflict.
     * @return whether this is a split conflict
     */
    public final boolean isSplit()
    {
        return this.conflictType.equals(ConflictType.SPLIT);
    }

    /**
     * Returns the conflict priority.
     * @return conflict priority
     */
    public final ConflictPriority getConflictPriority()
    {
        return this.conflictPriority;
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
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getUpstreamConflictingGTUs()
    {
        return this.upstreamConflictingGTUs;
    }

    /**
     * Returns a set of conflicting GTU's downstream of the <i>start</i> of the conflict ordered close to far from the conflict.
     * Distance is given relative to the <i>end</i> of the conflict, or null for conflicting vehicles on the conflict. In the
     * latter case the overlap is used.
     * @return set of conflicting GTU's downstream of the <i>start</i> of the conflict ordered close to far from the conflict
     */
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getDownstreamConflictingGTUs()
    {
        return this.downstreamConflictingGTUs;
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
     * Returns the conflicting link.
     * @return the conflicting link
     */
    public final CrossSectionLink getConflictingLink()
    {
        return this.conflictingLink;
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

    /**
     * Returns the conflict rule type.
     * @return conflict rule type
     */
    public final Class<? extends ConflictRule> getConflictRuleType()
    {
        return this.conflictRuleType;
    }

    /**
     * Returns the distance of a traffic light upstream on the conflicting lane.
     * @return distance of a traffic light upstream on the conflicting lane.
     */
    public final Length getConflictingTrafficLightDistance()
    {
        return this.conflictingTrafficLightDistance;
    }

    /**
     * Whether the conflict is permitted by the traffic light.
     * @return whether the conflict is permitted by the traffic light
     */
    public final boolean isPermitted()
    {
        return this.permitted;
    }

    /**
     * Set the distance of a traffic light upstream on the conflicting lane.
     * @param trafficLightDistance Length; distance of a traffic light upstream on the conflicting lane.
     * @param permittedConflict boolean; whether the conflict is permitted by the traffic light
     */
    public final void setConflictingTrafficLight(final Length trafficLightDistance, final boolean permittedConflict)
    {
        this.conflictingTrafficLightDistance = trafficLightDistance;
        this.permitted = permittedConflict;
    }

    /**
     * Returns the width at the given fraction.
     * @param fraction double; fraction from 0 to 1
     * @return Length; width at the given fraction
     */
    public final Length getWidthAtFraction(final double fraction)
    {
        try
        {
            return this.width.getWidth(fraction);
        }
        catch (ValueException exception)
        {
            throw new RuntimeException("Unexpected exception: fraction could not be interpolated.", exception);
        }
    }

    /**
     * Width progression of conflict.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 aug. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class Width
    {

        /** Fractions, from 0 to 1. */
        private final double[] fractions;

        /** Vector with widths. */
        private final LengthVector width;

        /**
         * @param fractions double[]; fractions, from 0 to 1
         * @param width LengthVector; vector of equal length with widths
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
         * @param fraction double; fraction from 0 to 1
         * @return Length; width at the given fraction
         * @throws ValueException when index is out of bounds
         */
        public Length getWidth(final double fraction) throws ValueException
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
            throw new RuntimeException("Unexpected exception: fraction could not be interpolated.");
        }

        /**
         * Returns a linear width progression.
         * @param startWidth Length; start width
         * @param endWidth Length; end width
         * @return Width; linear width progression
         */
        public static Width linear(final Length startWidth, final Length endWidth)
        {
            Throw.whenNull(startWidth, "Start width may not be null.");
            Throw.whenNull(endWidth, "End width may not be null.");
            try
            {
                return new Width(new double[] {0.0, 1.0},
                        new LengthVector(new Length[] {startWidth, endWidth}, StorageType.DENSE));
            }
            catch (ValueException exception)
            {
                throw new RuntimeException("Unexpected exception: widths could not be put in a vector.", exception);
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return String.format("Headway %s to object %s of type %s", getDistance(), getId(), getObjectType());
    }

}