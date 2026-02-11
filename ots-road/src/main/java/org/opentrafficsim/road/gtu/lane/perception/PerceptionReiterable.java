package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.function.BiFunction;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneStructure;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Standard implementation of {@link AbstractPerceptionReiterable} useful for most cases using an iterator over {@link Entry}
 * from the {@link LaneStructure}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <O> perceiving object type (an {@code O} is perceiving a {@code U} as a {@code P})
 * @param <P> perceived object type (an {@code O} is perceiving a {@code U} as a {@code P})
 * @param <U> underlying object type (an {@code O} is perceiving a {@code U} as a {@code P})
 */
public class PerceptionReiterable<O extends LaneBasedObject, P extends PerceivedObject, U>
        extends AbstractPerceptionReiterable<O, P, U>
{

    /** Iterator over entries of underlying objects. */
    private final Iterator<Entry<U>> iterator;

    /** Function to translate object and the distance to it in to a perceived object. */
    private final BiFunction<U, Length, P> perception;

    /**
     * Constructor.
     * @param perceivingObject perceiving object
     * @param iterable iterable over entries of underlying objects
     * @param perception function to translate an object, and the distance to it, in to a perceived object
     */
    public PerceptionReiterable(final O perceivingObject, final Iterable<Entry<U>> iterable,
            final BiFunction<U, Length, P> perception)
    {
        super(perceivingObject);
        Throw.whenNull(iterable, "iterator");
        Throw.whenNull(perception, "perception");
        this.iterator = iterable.iterator();
        this.perception = perception;
    }

    @Override
    protected Iterator<DistancedObject<U>> primaryIterator()
    {
        return new Iterator<>()
        {
            @Override
            public boolean hasNext()
            {
                return PerceptionReiterable.this.iterator.hasNext();
            }

            @Override
            public DistancedObject<U> next()
            {
                Entry<U> entry = PerceptionReiterable.this.iterator.next();
                return new DistancedObject<>(entry.object(), entry.distance());
            }
        };
    }

    @Override
    protected P perceive(final U object, final Length distance) throws GtuException, ParameterException
    {
        return this.perception.apply(object, distance);
    }

}
