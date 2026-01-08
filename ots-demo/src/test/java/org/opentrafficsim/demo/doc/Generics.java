package org.opentrafficsim.demo.doc;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.constraint.Constraint;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("all")
public class Generics
{

    // @docs/02-model-structure/java.md#java-generics
    public abstract class HierarchicalType<T extends HierarchicalType<T>> implements Type<T>
    {
        private T parent;

        public final T getParent()
        {
            return this.parent;
        }
    }

    public final class GtuType extends HierarchicalType<GtuType>
    {
    }

    // @docs/02-model-structure/java.md#java-generics
    public class ParameterSet
    {
        private Map<ParameterType<?>, Object> parameters;

        public final <T> void setParameter(final ParameterType<T> parameterType, final T value)
        {
            this.parameters.put(parameterType, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T getParameter(final ParameterType<T> parameterType)
        {
            return (T) this.parameters.get(parameterType);
        }
    }

    // @docs/02-model-structure/java.md#java-generics (with uncommented extends)
    public class ParameterTypeLength // extends ParameterTypeNumeric<Length>
    {
        private Constraint<? super Length> constraint;
    }

    // @docs/02-model-structure/java.md#java-generics
    public interface PerceptionCollectable<P extends PerceivedObject, U> extends PerceptionIterable<P>
    {
        <C, I> C collect(Supplier<I> identity, PerceptionAccumulator<? super U, I> accumulator, Function<I, C> finalizer);
    }

}
