package org.opentrafficsim.demo.doc;

import java.util.Map;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.constraint.Constraint;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionFinalizer;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

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
    public interface PerceptionCollectable<H extends Headway, U> extends PerceptionIterable<H>
    {
        <C, I> C collect(Supplier<I> identity, PerceptionAccumulator<? super U, I> accumulator,
                PerceptionFinalizer<C, I> finalizer);
    }
    
}