package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayStopLine;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLightReal;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictPriority;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

/**
 * Perceives traffic lights and intersection conflicts.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectIntersectionPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements IntersectionPerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

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

    @Override
    public final PerceptionCollectable<HeadwayTrafficLight, TrafficLight> getTrafficLights(final RelativeLane lane)
    {
        return computeIfAbsent("trafficLights", () -> computeTrafficLights(lane), lane);
    }

    @Override
    public final PerceptionCollectable<HeadwayConflict, Conflict> getConflicts(final RelativeLane lane)
    {
        return computeIfAbsent("conflicts", () -> computeConflicts(lane), lane);
    }

    @Override
    public final boolean isAlongsideConflictLeft()
    {
        return computeIfAbsent("alongside", () -> computeConflictAlongside(LateralDirectionality.LEFT),
                LateralDirectionality.LEFT);
    }

    @Override
    public final boolean isAlongsideConflictRight()
    {
        return computeIfAbsent("alongside", () -> computeConflictAlongside(LateralDirectionality.RIGHT),
                LateralDirectionality.RIGHT);
    }

    /**
     * Compute traffic lights.
     * @param lane lane
     * @return PerceptionCollectable of traffic lights
     */
    private PerceptionCollectable<HeadwayTrafficLight, TrafficLight> computeTrafficLights(final RelativeLane lane)
    {
        Iterable<Entry<TrafficLight>> iterable = Try.assign(() -> getPerception().getLaneStructure().getDownstreamObjects(lane,
                TrafficLight.class, RelativePosition.FRONT, true), "");
        Route route = Try.assign(() -> getPerception().getGtu().getStrategicalPlanner().getRoute(), "");
        return new AbstractPerceptionReiterable<>(Try.assign(() -> getGtu(), "GtuException"))
        {
            @Override
            protected Iterator<PrimaryIteratorEntry> primaryIterator()
            {
                Iterator<Entry<TrafficLight>> iterator = iterable.iterator();
                return new Iterator<>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    @Override
                    public AbstractPerceptionReiterable<LaneBasedGtu, HeadwayTrafficLight,
                            TrafficLight>.PrimaryIteratorEntry next()
                    {
                        Entry<TrafficLight> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            @Override
            protected HeadwayTrafficLight perceive(final TrafficLight trafficLight, final Length distance)
                    throws GtuException, ParameterException
            {
                return new HeadwayTrafficLightReal(trafficLight, distance,
                        trafficLight.canTurnOnRed(route, getPerception().getGtu().getType()));
            }
        };
    }

    /**
     * Compute conflicts.
     * @param lane lane
     * @return PerceptionCollectable of conflicts
     */
    private PerceptionCollectable<HeadwayConflict, Conflict> computeConflicts(final RelativeLane lane)
    {
        Iterable<Entry<Conflict>> iterable = Try.assign(() -> getPerception().getLaneStructure().getDownstreamObjects(lane,
                Conflict.class, RelativePosition.FRONT, true), "");
        return new AbstractPerceptionReiterable<>(Try.assign(() -> getGtu(), "GtuException"))
        {
            @Override
            protected Iterator<PrimaryIteratorEntry> primaryIterator()
            {
                Iterator<Entry<Conflict>> iterator = iterable.iterator();
                return new Iterator<>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    @Override
                    public AbstractPerceptionReiterable<LaneBasedGtu, HeadwayConflict, Conflict>.PrimaryIteratorEntry next()
                    {
                        Entry<Conflict> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            @Override
            protected HeadwayConflict perceive(final Conflict conflict, final Length distance)
                    throws GtuException, ParameterException
            {
                Conflict otherConflict = conflict.getOtherConflict();
                ConflictType conflictType = conflict.getConflictType();
                ConflictPriority conflictPriority = conflict.conflictPriority();
                Class<? extends ConflictRule> conflictRuleType = conflict.getConflictRule().getClass();
                String id = conflict.getId();
                Length length = conflict.getLength();
                Length conflictingLength = otherConflict.getLength();
                CrossSectionLink conflictingLink = otherConflict.getLane().getLink();

                // TODO get from link combination (needs to be a map property on the links)
                Length lookAhead =
                        Try.assign(() -> getObject().getParameters().getParameter(LOOKAHEAD), "Parameter not present.");
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

                // TODO limit 'conflictingVisibility' to first upstream traffic light, so GTU's behind it are ignored

                HeadwayConflict headwayConflict;
                try
                {
                    PerceptionCollectable<HeadwayGtu, LaneBasedGtu> upstreamConflictingGTUs = otherConflict.getUpstreamGtus(
                            getObject(), DirectIntersectionPerception.this.headwayGtuType, conflictingVisibility);
                    PerceptionCollectable<HeadwayGtu, LaneBasedGtu> downstreamConflictingGTUs = otherConflict.getDownstreamGtus(
                            getObject(), DirectIntersectionPerception.this.headwayGtuType, conflictingVisibility);
                    // TODO stop lines (current models happen not to use this, but should be possible)
                    HeadwayStopLine stopLine = new HeadwayStopLine("stopLineId", Length.ZERO, conflict.getLane());
                    HeadwayStopLine conflictingStopLine =
                            new HeadwayStopLine("conflictingStopLineId", Length.ZERO, conflict.getLane());

                    Lane thisLane = conflict.getLane();
                    Lane otherLane = otherConflict.getLane();
                    Length pos1a = conflict.getLongitudinalPosition();
                    Length pos2a = otherConflict.getLongitudinalPosition();
                    Length pos1b = Length.min(pos1a.plus(conflict.getLength()), thisLane.getLength());
                    Length pos2b = Length.min(pos2a.plus(otherConflict.getLength()), otherLane.getLength());
                    OtsLine2d line1 = thisLane.getCenterLine();
                    OtsLine2d line2 = otherLane.getCenterLine();
                    double dStart = line1.getLocation(pos1a).distance(line2.getLocation(pos2a));
                    double dEnd = line1.getLocation(pos1b).distance(line2.getLocation(pos2b));
                    Length startWidth =
                            Length.instantiateSI(dStart + .5 * thisLane.getWidth(pos1a).si + .5 * otherLane.getWidth(pos2a).si);
                    Length endWidth =
                            Length.instantiateSI(dEnd + .5 * thisLane.getWidth(pos1b).si + .5 * otherLane.getWidth(pos2b).si);

                    headwayConflict = new HeadwayConflict(conflictType, conflictPriority, conflictRuleType, id, distance,
                            length, conflictingLength, upstreamConflictingGTUs, downstreamConflictingGTUs,
                            conflictingVisibility, conflictingSpeedLimit, conflictingLink,
                            HeadwayConflict.Width.linear(startWidth, endWidth), stopLine, conflictingStopLine, thisLane);

                    Length trafficLightDistance = conflict.getOtherConflict()
                            .getTrafficLightDistance(getObject().getParameters().getParameter(ParameterTypes.LOOKAHEAD));
                    if (trafficLightDistance != null && trafficLightDistance.le(lookAhead))
                    {
                        headwayConflict.setConflictingTrafficLight(trafficLightDistance, conflict.isPermitted());
                    }
                }
                catch (GtuException | ParameterException exception)
                {
                    throw new RuntimeException("Could not create headway objects.", exception);
                }
                return headwayConflict;
            }
        };
    }

    /**
     * Compute whether there is a conflict alongside.
     * @param lat lateral directionality
     * @return whether there is a conflict alongside
     */
    private boolean computeConflictAlongside(final LateralDirectionality lat)
    {
        try
        {
            RelativeLane lane = new RelativeLane(lat, 1);
            if (getPerception().getLaneStructure().exists(lane))
            {
                Iterator<Entry<Conflict>> conflicts = getPerception().getLaneStructure()
                        .getUpstreamObjects(lane, Conflict.class, RelativePosition.FRONT).iterator();
                if (conflicts.hasNext())
                {
                    Entry<Conflict> entry = conflicts.next();
                    return entry.distance().si < entry.object().getLength().si + getGtu().getLength().si;
                }
            }
            return false;
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Unexpected exception while computing conflict alongside.", exception);
        }
    }

    @Override
    public final String toString()
    {
        return "DirectIntersectionPerception " + cacheAsString();
    }

}
