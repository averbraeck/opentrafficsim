package org.opentrafficsim.core.network;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Lane extends CrossSectionElement
{
    /** type of lane to deduce compatibility with GTU types. */
    private final LaneType<?> laneType;

    /** in direction of geometry, reverse, or both. */
    private final LongitudinalDirectionality directionality;

    /** Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private DoubleScalar.Abs<FrequencyUnit> capacity;

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralStartPosition the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param beginWidth start width, positioned <i>symmetrically around</i> the lateral start position.
     * @param endWidth end width, positioned <i>symmetrically around</i> the lateral end position.
     * @param laneType type of lane to deduce compatibility with GTU types.
     * @param directionality in direction of geometry, reverse, or both.
     * @param capacity Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage).
     */
    public Lane(final CrossSectionLink<?, ?> parentLink, final DoubleScalar.Rel<LengthUnit> lateralStartPosition,
            final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth,
            final LaneType<?> laneType, final LongitudinalDirectionality directionality,
            final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        super(parentLink, lateralStartPosition, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionality = directionality;
        this.capacity = capacity;
    }

    /**
     * @return capacity.
     */
    public final DoubleScalar.Abs<FrequencyUnit> getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set capacity.
     */
    public final void setCapacity(final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @return laneType.
     */
    public final LaneType<?> getLaneType()
    {
        return this.laneType;
    }

    /**
     * @return directionality.
     */
    public final LongitudinalDirectionality getDirectionality()
    {
        return this.directionality;
    }

    /**
     * Determine whether there is a lane to the left or to the right of this lane, which is accessible from this lane, or null
     * if no lane could be found. The method takes the LongitidinalDirectionality of the lane into account. In other words, if
     * we drive FORWARD and look for a lane on the LEFT, and there is a lane but the Directionality of that lane is not FORWARD
     * or BOTH, null will be returned.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This means that
     * a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * The algorithm also looks for RoadMarkerAcross elements between the lanes to determine the lateral permeability for a GTU.
     * A RoadMarkerAcross is seen as being between two lanes if its center line is not more than delta distance from the
     * relevant lateral edges of the two adjacent lanes. <br>
     * When there are multiple lanes that are adjacent, which could e.g. be the case if an overlapping tram lane and a car lane
     * are adjacent to the current lane, the widest lane that best matches the GTU accessibility of the provided GTUType is
     * returned. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param lateralDirection LEFT or RIGHT.
     * @param gtuType the type of GTU for which this an adjacent lane.
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction does
     *         not match.
     */
    public final Lane accessibleAdjacentLane(final LateralDirectionality lateralDirection, final GTUType<?> gtuType)
    {
        final double delta = 0.1; // in meters
        Set<Lane> candidates = new HashSet<>();
        for (CrossSectionElement cse : getParentLink().getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (cseLane.getLaneType().isCompatible(gtuType))
                {
                    if (cseLane.getDirectionality().equals(LongitudinalDirectionality.BOTH)
                            || cseLane.getDirectionality().equals(this.getDirectionality()))
                    {
                        if (lateralDirection.equals(LateralDirectionality.LEFT))
                        {
                            if (Math.abs(DoubleScalar.minus(this.getLateralBeginEndPosition(),
                                    cse.getLateralBeginStartPosition()).getSI()) < delta)
                            {
                                // adjacent. Can we cross?
                                if (canCrossTo(gtuType, this.getLateralBeginEndPosition().getSI(), delta, lateralDirection))
                                {
                                    candidates.add(cseLane);
                                }
                            }
                        }
                        else
                        {
                            if (Math.abs(DoubleScalar.minus(this.getLateralBeginStartPosition(),
                                    cse.getLateralBeginEndPosition()).getSI()) < delta)
                            {
                                // adjacent. Can we cross?
                                if (canCrossTo(gtuType, this.getLateralBeginStartPosition().getSI(), delta, lateralDirection))
                                {
                                    candidates.add(cseLane);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (candidates.isEmpty())
        {
            return null;
        }

        if (candidates.size() == 1)
        {
            return candidates.iterator().next();
        }

        Lane bestLane = null;
        double widthM = -1.0;
        for (Lane lane : candidates)
        {
            if (lane.getBeginWidth().getSI() > widthM)
            {
                widthM = lane.getBeginWidth().getSI();
                bestLane = lane;
            }
        }
        return bestLane;
    }

    /**
     * Find all longitudinal road markers along between the lanes and test if we can cross.
     * @param gtuType the GTU type for which we have to check permeability.
     * @param edgeOffset the offset
     * @param delta the margin to use to find the road markers.
     * @param lateralDirection direction to cross to.
     * @return whether we can cross or not.
     */
    private boolean canCrossTo(final GTUType<?> gtuType, final double edgeOffset, final double delta,
            final LateralDirectionality lateralDirection)
    {
        boolean canCross = true;
        for (CrossSectionElement cse : getParentLink().getCrossSectionElementList())
        {
            if (cse instanceof RoadMarkerAlong)
            {
                RoadMarkerAlong marker = (RoadMarkerAlong) cse;
                if (Math.abs(marker.getLateralCenterPosition().getSI() - edgeOffset) < delta)
                {
                    if (!marker.isPermeable(gtuType, lateralDirection))
                    {
                        canCross = false;
                    }
                }
            }
        }
        return canCross;
    }
}
