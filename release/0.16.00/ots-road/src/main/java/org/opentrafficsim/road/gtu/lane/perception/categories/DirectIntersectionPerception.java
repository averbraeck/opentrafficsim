package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LaneBasedObjectIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructure.Entry;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayStopLine;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.Conflict.ConflictEnd;
import org.opentrafficsim.road.network.lane.conflict.ConflictPriority;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.language.Throw;

/**
 * Perceives traffic lights and intersection conflicts.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DirectIntersectionPerception extends LaneBasedAbstractPerceptionCategory implements IntersectionPerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Margin to find upstream conflicts who's ends are downstream, needed as the algorithm searches beyond a location. */
    private static final Length MARGIN = Length.createSI(0.001);

    /** Set of traffic lights. */
    private Map<RelativeLane, TimeStampedObject<PerceptionCollectable<HeadwayTrafficLight, TrafficLight>>> trafficLights =
            new HashMap<>();

    /** Set of conflicts. */
    private Map<RelativeLane, TimeStampedObject<PerceptionCollectable<HeadwayConflict, Conflict>>> conflicts = new HashMap<>();

    /** Conflicts alongside left. */
    private TimeStampedObject<Boolean> alongsideConflictLeft;

    /** Conflicts alongside right. */
    private TimeStampedObject<Boolean> alongsideConflictRight;

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /**
     * @param perception perception
     * @param headwayGtuType type of headway gtu to generate
     */
    public DirectIntersectionPerception(final LanePerception perception, final HeadwayGtuType headwayGtuType)
    {
        super(perception);
        this.headwayGtuType = headwayGtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final void updateTrafficLights() throws GTUException, ParameterException
    {
        this.trafficLights.clear();
        Route route = getPerception().getGtu().getStrategicalPlanner().getRoute();
        for (RelativeLane lane : getPerception().getLaneStructure().getExtendedCrossSection())
        {
            LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
            Length pos = record.getStartDistance().neg();
            pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx())
                    : pos.minus(getGtu().getFront().getDx());
            PerceptionCollectable<HeadwayTrafficLight, TrafficLight> it =
                    new LaneBasedObjectIterable<HeadwayTrafficLight, TrafficLight>(getGtu(), TrafficLight.class, record,
                            Length.max(Length.ZERO, pos), getGtu().getParameters().getParameter(LOOKAHEAD), getGtu().getFront(),
                            route)
                    {
                        /** {@inheritDoc} */
                        @Override
                        public HeadwayTrafficLight perceive(final LaneBasedGTU perceivingGtu, final TrafficLight trafficLight,
                                final Length distance)
                        {
                            try
                            {
                                return new HeadwayTrafficLight(trafficLight, distance);
                            }
                            catch (GTUException exception)
                            {
                                throw new RuntimeException(exception);
                            }
                        }
                    };
            this.trafficLights.put(lane, new TimeStampedObject<>(it, getTimestamp()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateConflicts() throws GTUException, ParameterException
    {
        this.conflicts.clear();
        Route route = getPerception().getGtu().getStrategicalPlanner().getRoute();
        for (RelativeLane lane : getPerception().getLaneStructure().getExtendedCrossSection())
        {
            LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
            Length pos = record.getStartDistance().neg().plus(getGtu().getRear().getDx());
            while (pos.lt0() && !record.getPrev().isEmpty())
            {
                pos = pos.plus(record.getLength());
                record = record.getPrev().get(0);
            }
            // find all ConflictEnd, and the most upstream relating position
            List<LaneBasedObject> laneObjs;
            if (record.isDownstreamBranch())
            {
                if (record.getDirection().isPlus())
                {
                    laneObjs = record.getLane().getLaneBasedObjects(Length.max(Length.ZERO, pos), record.getLane().getLength());
                }
                else
                {
                    laneObjs = record.getLane().getLaneBasedObjects(Length.ZERO, pos);
                }
            }
            else
            {
                laneObjs = new ArrayList<>();
            }
            // TODO if conflicts span multiple lanes, this within-lane search fails
            for (LaneBasedObject object : laneObjs)
            {
                if (object instanceof ConflictEnd)
                {
                    Conflict c = ((ConflictEnd) object).getConflict();
                    Length cPos = record.getDirection().isPlus() ? c.getLongitudinalPosition().minus(MARGIN)
                            : c.getLongitudinalPosition().plus(MARGIN);
                    pos = record.getDirection().isPlus() ? Length.min(pos, cPos) : Length.max(pos, cPos);
                }
            }
            PerceptionCollectable<HeadwayConflict, Conflict> it = new LaneBasedObjectIterable<HeadwayConflict, Conflict>(
                    getGtu(), Conflict.class, record, Length.max(MARGIN.neg(), pos),
                    getGtu().getParameters().getParameter(LOOKAHEAD), getGtu().getFront(), route)
            {
                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public HeadwayConflict perceive(final LaneBasedGTU perceivingGtu, final Conflict conflict,
                        final Length distance)
                {
                    Conflict otherConflict = conflict.getOtherConflict();
                    ConflictType conflictType = conflict.getConflictType();
                    ConflictPriority conflictPriority = conflict.conflictPriority();
                    Class<? extends ConflictRule> conflictRuleType = conflict.getConflictRule().getClass();
                    String id = conflict.getId();
                    Length length = conflict.getLength();
                    Length conflictingLength = otherConflict.getLength();
                    CrossSectionLink conflictingLink = otherConflict.getLane().getParentLink();

                    // TODO get from link combination (needs to be a map property on the links)
                    Length lookAhead =
                            Try.assign(() -> getGtu().getParameters().getParameter(LOOKAHEAD), "Parameter not present.");
                    Length conflictingVisibility = lookAhead;
                    Speed conflictingSpeedLimit;
                    try
                    {
                        conflictingSpeedLimit = otherConflict.getLane().getHighestSpeedLimit();
                    }
                    catch (NetworkException exception)
                    {
                        throw new RuntimeException("GTU type not available on conflicting lane.", exception);
                    }

                    LongitudinalDirectionality otherDir = otherConflict.getDirection();
                    Throw.when(otherDir.isBoth(), UnsupportedOperationException.class,
                            "Conflicts on lanes with direction BOTH are not supported.");
                    // TODO limit 'conflictingVisibility' to first upstream traffic light, so GTU's behind it are
                    // ignored

                    HeadwayConflict headwayConflict;
                    try
                    {
                        PerceptionCollectable<HeadwayGTU, LaneBasedGTU> upstreamConflictingGTUs = otherConflict.getUpstreamGtus(
                                getGtu(), DirectIntersectionPerception.this.headwayGtuType, conflictingVisibility);
                        PerceptionCollectable<HeadwayGTU, LaneBasedGTU> downstreamConflictingGTUs =
                                otherConflict.getDownstreamGtus(getGtu(), DirectIntersectionPerception.this.headwayGtuType,
                                        conflictingVisibility);
                        // TODO stop lines (current models happen not to use this, but should be possible)
                        HeadwayStopLine stopLine = new HeadwayStopLine("stopLineId", Length.ZERO);
                        HeadwayStopLine conflictingStopLine = new HeadwayStopLine("conflictingStopLineId", Length.ZERO);

                        Lane thisLane = conflict.getLane();
                        Lane otherLane = otherConflict.getLane();
                        Length pos1a = conflict.getLongitudinalPosition();
                        Length pos2a = otherConflict.getLongitudinalPosition();
                        Length pos1b = Length.min(pos1a.plus(conflict.getLength()), thisLane.getLength());
                        Length pos2b = Length.min(pos2a.plus(otherConflict.getLength()), otherLane.getLength());
                        OTSLine3D line1 = thisLane.getCenterLine();
                        OTSLine3D line2 = otherLane.getCenterLine();
                        double dStart = line1.getLocation(pos1a).distance(line2.getLocation(pos2a));
                        double dEnd = line1.getLocation(pos1b).distance(line2.getLocation(pos2b));
                        Length startWidth =
                                Length.createSI(dStart + .5 * thisLane.getWidth(pos1a).si + .5 * otherLane.getWidth(pos2a).si);
                        Length endWidth =
                                Length.createSI(dEnd + .5 * thisLane.getWidth(pos1b).si + .5 * otherLane.getWidth(pos2b).si);

                        headwayConflict = new HeadwayConflict(conflictType, conflictPriority, conflictRuleType, id, distance,
                                length, conflictingLength, upstreamConflictingGTUs, downstreamConflictingGTUs,
                                conflictingVisibility, conflictingSpeedLimit, conflictingLink,
                                HeadwayConflict.Width.linear(startWidth, endWidth), stopLine, conflictingStopLine);
                    }
                    catch (GTUException | OTSGeometryException exception)
                    {
                        throw new RuntimeException("Could not create headway objects.", exception);
                    }
                    // TODO
                    // if (trafficLightDistance != null && trafficLightDistance.le(lookAhead))
                    // {
                    // headwayConflict.setConflictingTrafficLight(trafficLightDistance, conflict.isPermitted());
                    // }
                    return headwayConflict;
                }
            };
            this.conflicts.put(lane, new TimeStampedObject<>(it, getTimestamp()));
        }

        // alongside
        for (RelativeLane lane : new RelativeLane[] { RelativeLane.LEFT, RelativeLane.RIGHT })
        {
            if (getPerception().getLaneStructure().getExtendedCrossSection().contains(lane))
            {
                SortedSet<Entry<Conflict>> conflictEntries = getPerception().getLaneStructure().getUpstreamObjects(lane,
                        Conflict.class, getGtu(), RelativePosition.FRONT);
                boolean alongside = false;
                if (!conflictEntries.isEmpty())
                {
                    Entry<Conflict> entry = conflictEntries.first();
                    alongside = entry.getDistance().si < entry.getLaneBasedObject().getLength().si + getGtu().getLength().si;
                }
                if (lane.isLeft())
                {
                    this.alongsideConflictLeft = new TimeStampedObject<>(alongside, getTimestamp());
                }
                else
                {
                    this.alongsideConflictRight = new TimeStampedObject<>(alongside, getTimestamp());
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Iterable<HeadwayTrafficLight> getTrafficLights(final RelativeLane lane)
    {
        return getObjectOrNull(this.trafficLights.get(lane));
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayConflict, Conflict> getConflicts(final RelativeLane lane)
    {
        return getObjectOrNull(this.conflicts.get(lane));
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAlongsideConflictLeft()
    {
        return this.alongsideConflictLeft == null ? false : this.alongsideConflictLeft.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAlongsideConflictRight()
    {
        return this.alongsideConflictRight == null ? false : this.alongsideConflictRight.getObject();
    }

    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<PerceptionCollectable<HeadwayTrafficLight, TrafficLight>> getTimeStampedTrafficLights(
            final RelativeLane lane)
    {
        return this.trafficLights.get(lane);
    }

    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<PerceptionCollectable<HeadwayConflict, Conflict>> getTimeStampedConflicts(
            final RelativeLane lane)
    {
        return this.conflicts.get(lane);
    }

    /**
     * Returns whether there is a conflict alongside to the left, time stamped.
     * @return whether there is a conflict alongside to the left
     */
    public final TimeStampedObject<Boolean> isAlongsideConflictLeftTimeStamped()
    {
        return this.alongsideConflictLeft;
    }

    /**
     * Returns whether there is a conflict alongside to the right, time stamped.
     * @return whether there is a conflict alongside to the right
     */
    public final TimeStampedObject<Boolean> isAlongsideConflictRightTimeStamped()
    {
        return this.alongsideConflictRight;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectIntersectionPerception";
    }

}
