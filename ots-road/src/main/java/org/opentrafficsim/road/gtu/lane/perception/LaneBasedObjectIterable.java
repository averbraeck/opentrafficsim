package org.opentrafficsim.road.gtu.lane.perception;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Iterable that searches downstream for a certain type of lane based object.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <L> lane based object type
 * @param <R> record type
 */
public abstract class LaneBasedObjectIterable<H extends Headway, L extends LaneBasedObject, R extends LaneRecord<R>>
        extends AbstractPerceptionIterable<H, R, Void>
{

    /** Class of lane based objects to return. */
    private final Class<L> clazz;

    /**
     * Constructor.
     * @param clazz Class&lt;H&gt;; class of lane based objects to return
     * @param root R; root record
     * @param initialPosition Length; initial position
     * @param maxDistance Length; max distance to search
     * @param relativePosition RelativePosition; relative position
     * @param route Route; route of the GTU, may be {@code null}
     */
    public LaneBasedObjectIterable(final Class<L> clazz, final R root, final Length initialPosition, final Length maxDistance,
            final RelativePosition relativePosition, final Route route)
    {
        super(root, initialPosition, true, maxDistance, relativePosition, route);
        this.clazz = clazz;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected Entry getNext(final R record, final Length position, final Void counter)
    {
        List<LaneBasedObject> list = record.getLane().getObjectAhead(position, record.getDirection());
        while (list != null)
        {
            Set<H> set = new LinkedHashSet<>();
            Length pos = list.get(0).getLongitudinalPosition();
            Length headway = record.getDistanceToPosition(pos).minus(getDx());
            for (LaneBasedObject object : list)
            {
                if (this.clazz.isAssignableFrom(object.getClass()))
                {
                    // is assignable, so safe cast
                    Try.execute(() -> set.add(createHeadway((L) object, headway)),
                            "Exception during creation of headway object.");
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

    /**
     * Creates a headway object from a lane based object.
     * @param laneBasedObject LaneBasedObject; lane based object
     * @param distance Length; distance to the object
     * @return headway object from a lane based object
     * @throws ParameterException on missing parameter
     * @throws GTUException on GTU failures
     */
    protected abstract H createHeadway(L laneBasedObject, Length distance) throws ParameterException, GTUException;

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LaneBasedObjectIterable [class=" + this.clazz + "]";
    }

}
