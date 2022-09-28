package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Contains information by which drivers know when they need to leave a lane in order to be able to stay on the infrastructure
 * and follow their route.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class InfrastructureLaneChangeInfo implements Comparable<InfrastructureLaneChangeInfo>, Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Required number of lane changes. */
    private final int requiredNumberOfLaneChanges;

    /** Record who's end defines the remaining distances. */
    private final LaneStructureRecord record;

    /** Available length after the start (reference on start). */
    private final Length afterStartLength;

    /** Whether the need to change lane comes from a dead-end. */
    private boolean deadEnd;

    /** Lateral directionality of required lane changes. */
    private final LateralDirectionality lat;

    /**
     * Constructor for subclasses.
     * @param requiredNumberOfLaneChanges int; required number of lane changes
     * @param deadEnd boolean; whether the need to change lane comes from a dead-end
     */
    protected InfrastructureLaneChangeInfo(final int requiredNumberOfLaneChanges, final boolean deadEnd)
    {
        this.requiredNumberOfLaneChanges = requiredNumberOfLaneChanges;
        this.record = null;
        this.deadEnd = deadEnd;
        this.afterStartLength = null;
        this.lat = LateralDirectionality.NONE;
    }

    /**
     * Constructor.
     * @param requiredNumberOfLaneChanges int; required number of lane changes
     * @param record LaneStructureRecord; record who's end defines the remaining distance
     * @param relativePosition RelativePosition; critical relative position (i.e. nose when driving forward)
     * @param deadEnd boolean; whether the need to change lane comes from a dead-end
     * @param lat LateralDirectionality; lateral directionality of required lane changes
     * @throws IllegalArgumentException if required number of lane changes or remaining distance is negative
     * @throws NullPointerException if remaining distance is null
     */
    public InfrastructureLaneChangeInfo(final int requiredNumberOfLaneChanges, final LaneStructureRecord record,
            final RelativePosition relativePosition, final boolean deadEnd, final LateralDirectionality lat)
    {
        Throw.when(requiredNumberOfLaneChanges < 0, IllegalArgumentException.class,
                "Required number of lane changes may not be negative.");
        Throw.whenNull(lat, "Lateral directionality may not be null.");
        Throw.when(requiredNumberOfLaneChanges != 0 && lat.equals(LateralDirectionality.NONE), IllegalArgumentException.class,
                "Lateral directionality may not be NONE for non-zero lane changes.");
        Throw.whenNull(record, "Record may not be null.");
        this.requiredNumberOfLaneChanges = requiredNumberOfLaneChanges;
        this.record = record;
        this.afterStartLength = this.record.getLane().getLength().minus(relativePosition.getDx());
        this.deadEnd = deadEnd;
        this.lat = lat;
    }

    /**
     * @return requiredNumberOfLaneChanges required number of lane changes.
     */
    public final int getRequiredNumberOfLaneChanges()
    {
        return this.requiredNumberOfLaneChanges;
    }

    /**
     * @return remainingDistance remaining distance to perform required lane changes.
     */
    public Length getRemainingDistance()
    {
        return this.record.getStartDistance().plus(this.afterStartLength);
    }

    /**
     * @return whether this reason to change lane is due to a dead-end.
     */
    public final boolean isDeadEnd()
    {
        return this.deadEnd;
    }

    /**
     * Sets whether this reason to change lane is due to a dead-end.
     * @param deadEnd boolean; whether the need to change lane comes from a dead-end
     */
    public final void setDeadEnd(final boolean deadEnd)
    {
        this.deadEnd = deadEnd;
    }

    /**
     * Returns the lateral directionality of the required lane changes.
     * @return LateralDirectionality; lateral directionality of the required lane changes
     */
    public final LateralDirectionality getLateralDirectionality()
    {
        return this.lat;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "InfrastructureLaneChangeInfo [requiredNumberOfLaneChanges=" + this.requiredNumberOfLaneChanges
                + ", remainingDistance=" + getRemainingDistance() + "]";
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(final InfrastructureLaneChangeInfo infrastructureLaneChangeInfo)
    {
        return this.getRemainingDistance().compareTo(infrastructureLaneChangeInfo.getRemainingDistance());
    }

    /**
     * Returns lane change info for one lane towards the left.
     * @param rec LaneStructureRecord; record who's end defines the remaining distance
     * @param rel RelativePosition; critical relative position (i.e. nose when driving forward)
     * @param dead boolean; whether the need to change lane comes from a dead-end
     * @return InfrastructureLaneChangeInfo; lane change info for one lane towards the left
     */
    public final InfrastructureLaneChangeInfo left(final LaneStructureRecord rec, final RelativePosition rel,
            final boolean dead)
    {
        return new InfrastructureLaneChangeInfo(this.requiredNumberOfLaneChanges + 1, rec, rel, dead,
                LateralDirectionality.LEFT);
    }

    /**
     * Returns lane change info for one lane towards the right.
     * @param rec LaneStructureRecord; record who's end defines the remaining distance
     * @param rel RelativePosition; critical relative position (i.e. nose when driving forward)
     * @param dead boolean; whether the need to change lane comes from a dead-end
     * @return InfrastructureLaneChangeInfo; lane change info for one lane towards the right
     */
    public final InfrastructureLaneChangeInfo right(final LaneStructureRecord rec, final RelativePosition rel,
            final boolean dead)
    {
        return new InfrastructureLaneChangeInfo(this.requiredNumberOfLaneChanges + 1, rec, rel, dead,
                LateralDirectionality.RIGHT);
    }

    /**
     * Returns an instance for the case the entire lane is inaccessible.
     * @param deadEnd boolean; dead end
     * @return instance for the case the entire lane is inaccessible
     */
    public static InfrastructureLaneChangeInfo fromInaccessibleLane(final boolean deadEnd)
    {
        return new InfrastructureLaneChangeInfoInaccessibleLane(deadEnd);
    }

    /**
     * Extension which sets the distance to 0 always, used for fully inaccessible lanes regarding the route.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class InfrastructureLaneChangeInfoInaccessibleLane extends InfrastructureLaneChangeInfo
    {

        /** */
        private static final long serialVersionUID = 20180214L;

        /**
         * @param deadEnd boolean; whether the need to change lane comes from a dead-end
         */
        InfrastructureLaneChangeInfoInaccessibleLane(final boolean deadEnd)
        {
            super(1, deadEnd);
        }

        /** {@inheritDoc} */
        @Override
        public Length getRemainingDistance()
        {
            return Length.ZERO;
        }

    }

}
