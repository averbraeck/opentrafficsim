package org.opentrafficsim.road.gtu.lane.perception;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Iterable that searches downstream or upstream for a certain type of lane based object.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <L> lane based object type
 */
public abstract class LaneBasedObjectIterable<H extends Headway, L extends LaneBasedObject>
        extends AbstractPerceptionIterable<H, L, Boolean>
{

    /** Margin for start and end of lane. */
    private static final Length MARGIN = Length.instantiateSI(1e-9);

    /** Class of lane based objects to return. */
    private final Class<L> clazz;

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param clazz Class&lt;L&gt;; class of lane based objects to return
     * @param root LaneRecord&lt;?&gt;; root record
     * @param initialPosition Length; initial position
     * @param downstream boolean; downstream
     * @param maxDistance Length; max distance to search
     * @param relativePosition RelativePosition; relative position
     * @param route Route; route of the GTU, may be {@code null}
     */
    public LaneBasedObjectIterable(final LaneBasedGTU perceivingGtu, final Class<L> clazz, final LaneRecord<?> root,
            final Length initialPosition, final boolean downstream, final Length maxDistance,
            final RelativePosition relativePosition, final Route route)
    {
        super(perceivingGtu, root, initialPosition, downstream, maxDistance, relativePosition, route);
        this.clazz = clazz;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected Entry getNext(final LaneRecord<?> record, final Length position, final Boolean counter)
    {
        List<LaneBasedObject> list;
        if (isDownstream())
        {
            if (!record.isDownstreamBranch())
            {
                return null;
            }
            Length pos = position.eq0() && counter == null ? MARGIN.neg() : position;
            list = record.getLane().getObjectAhead(pos, record.getDirection());
        }
        else
        {
            Length pos = position.eq(record.getLane().getLength()) && counter == null
                    ? record.getLane().getLength().plus(MARGIN) : position;
            list = record.getLane().getObjectBehind(pos, record.getDirection());
        }
        while (list != null)
        {
            Set<L> set = new LinkedHashSet<>();
            Length pos = list.get(0).getLongitudinalPosition();
            for (LaneBasedObject object : list)
            {
                if (this.clazz.isAssignableFrom(object.getClass()))
                {
                    // is assignable, so safe cast
                    set.add((L) object);
                }
            }
            if (!set.isEmpty())
            {
                if (set.size() == 1)
                {
                    return new Entry(set.iterator().next(), true, pos);
                }
                return new Entry(set, true, pos);
            }
            if (isDownstream())
            {
                list = record.getLane().getObjectAhead(pos, record.getDirection());
            }
            else
            {
                list = record.getLane().getObjectBehind(pos, record.getDirection());
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected final Length getDistance(final L object, final LaneRecord<?> record, final Length position)
    {
        return isDownstream() ? record.getDistanceToPosition(position).minus(getDx())
                : record.getDistanceToPosition(position).neg().plus(getDx());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LaneBasedObjectIterable [class=" + this.clazz + ", downstream=" + isDownstream() + "]";
    }

}
