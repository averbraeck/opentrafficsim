package org.opentrafficsim.editor.extensions;

import java.util.LinkedHashMap;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.xml.bindings.AngleAdapter;
import org.opentrafficsim.xml.bindings.ArcDirectionAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.DirectionAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;
import org.opentrafficsim.xml.bindings.IntegerAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.Point2dAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.StripeTypeAdapter;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;
import org.opentrafficsim.xml.bindings.types.ExpressionType;

/**
 * Class that houses static instances of adapters, for common usage within the editor.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class Adapters
{

    /** Map of adapters per output type. */
    private final static java.util.Map<Class<?>, ExpressionAdapter<?, ?>> ADAPTERS = new LinkedHashMap<>();

    static
    {
        ADAPTERS.put(Angle.class, new AngleAdapter());
        ADAPTERS.put(ArcDirection.class, new ArcDirectionAdapter());
        ADAPTERS.put(Boolean.class, new BooleanAdapter());
        ADAPTERS.put(Direction.class, new DirectionAdapter());
        ADAPTERS.put(Double.class, new DoubleAdapter());
        ADAPTERS.put(Integer.class, new IntegerAdapter());
        ADAPTERS.put(Length.class, new LengthAdapter());
        ADAPTERS.put(LinearDensity.class, new LinearDensityAdapter());
        ADAPTERS.put(Point2d.class, new Point2dAdapter());
        ADAPTERS.put(String.class, new StringAdapter());
        ADAPTERS.put(Stripe.Type.class, new StripeTypeAdapter());
    }

    /**
     * Returns an adapter for the given class. Adapters are only provided for known classes. This is to limit the number of
     * adapters in memory, as these are stateless. Adapters are available for: Angle, ArcDirection, Boolean, Direction, Double,
     * Integer, Length, LinearDensity, Point2d, String, and Stripe.Type.
     * @param <T> output type of the adapter.
     * @param <E> expression type of the adapter.
     * @param clazz Class&lt;T&gt;; class of the output type of the adapter.
     * @return ExpressionAdapter&lt;T, ?&gt;; adapter.
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends ExpressionType<T>> ExpressionAdapter<T, E> get(final Class<T> clazz)
    {
        Throw.when(!ADAPTERS.containsKey(clazz), RuntimeException.class,
                "No adapter for class %s available. Add it in the static code block of MapData or create one directly.");
        return (ExpressionAdapter<T, E>) ADAPTERS.get(clazz);
    }

    /**
     * Add an adapter for the given class.
     * @param <T> output type of the adapter.
     * @param <E> expression type of the adapter.
     * @param clazz Class&lt;T&gt;; class of the output type of the adapter.
     * @param adapter ExpressionAdapter&lt;T, ?&gt;; adapter.
     */
    public static <T, E extends ExpressionType<T>> void set(final Class<T> clazz, final ExpressionAdapter<T, E> adapter)
    {
        Throw.whenNull(clazz, "Class may not be null.");
        Throw.whenNull(adapter, "Adapter may not be null.");
        ADAPTERS.put(clazz, adapter);
    }

}
