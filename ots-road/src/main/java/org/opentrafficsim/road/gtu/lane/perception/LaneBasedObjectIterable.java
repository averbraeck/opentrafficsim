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
 * Iterable that searches downstream for a certain type of lane based object.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <L> lane based object type
 */
public abstract class LaneBasedObjectIterable<H extends Headway, L extends LaneBasedObject>
        extends AbstractPerceptionIterable<H, L, Void>
{

    /** Class of lane based objects to return. */
    private final Class<L> clazz;

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param clazz Class&lt;L&gt;; class of lane based objects to return
     * @param root LaneRecord&lt;?&gt;; root record
     * @param initialPosition Length; initial position
     * @param maxDistance Length; max distance to search
     * @param relativePosition RelativePosition; relative position
     * @param route Route; route of the GTU, may be {@code null}
     */
    public LaneBasedObjectIterable(final LaneBasedGTU perceivingGtu, final Class<L> clazz, final LaneRecord<?> root,
            final Length initialPosition, final Length maxDistance, final RelativePosition relativePosition, final Route route)
    {
        super(perceivingGtu, root, initialPosition, true, maxDistance, relativePosition, route);
        this.clazz = clazz;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected Entry getNext(final LaneRecord<?> record, final Length position, final Void counter)
    {
        if (!record.isDownstreamBranch())
        {
            return null;
        }
        List<LaneBasedObject> list = record.getLane().getObjectAhead(position, record.getDirection());
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
                    return new Entry(set.iterator().next(), null, pos);
                }
                return new Entry(set, null, pos);
            }
            list = record.getLane().getObjectAhead(pos, record.getDirection());
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected final Length getDistance(final L object, final LaneRecord<?> record, final Length position)
    {
        return record.getDistanceToPosition(position).minus(getDx());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LaneBasedObjectIterable [class=" + this.clazz + "]";
    }

}
