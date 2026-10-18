package org.opentrafficsim.core.gtu.perception;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Implements {@code PerceptionCategory} and allows sub-classes to easily implement lazy evaluation through the
 * {@code computeIfAbsent()} methods.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <G> GTU type
 * @param <P> perception type
 * @param <C> perception category type
 */
public abstract class AbstractPerceptionCategory<G extends Gtu, P extends Perception<G>, C extends PerceptionCategory<?, ?, C>>
        implements PerceptionCategory<G, P, C>
{

    /** Connected perception. */
    private final P perception;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Time of cache. */
    private double time;

    /** Map from key, either non-contextual or contextual, and time-stamped object. */
    private final Map<Object, Object> cache = new LinkedHashMap<>();

    /** Cache dedicated to {@link LateralDirectionality} context. */
    @SuppressWarnings("unchecked")
    private final Map<Enum<?>, Object>[] lateralDirectionCache =
            (Map<Enum<?>, Object>[]) new Map<?, ?>[LateralDirectionality.values().length];

    /**
     * Constructor setting the perception.
     * @param perception perception
     */
    public AbstractPerceptionCategory(final P perception)
    {
        this.perception = perception;
        this.simulator = this.perception.getGtu().getSimulator();
        for (int i = 0; i < this.lateralDirectionCache.length; i++)
        {
            this.lateralDirectionCache[i] = new LinkedHashMap<>();
        }
    }

    /**
     * Returns the connected perception.
     * @return connected perception
     */
    @SuppressWarnings("checkstyle:designforextension")
    public P getPerception()
    {
        return this.perception;
    }

    /**
     * Returns the connected GTU.
     * @return connected GTU
     */
    @SuppressWarnings("checkstyle:designforextension")
    public G getGtu()
    {
        return this.perception.getGtu();
    }

    /**
     * Returns the current time.
     * @return current time
     */
    public Duration getTimestamp()
    {
        return this.simulator.getSimulatorTime();
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information, e.g. 'ego speed'.
     * <p>
     * This method will compute the information if required. A simple manner to define a {@code Supplier<T>} is to create a
     * method and invoke it using a lambda expression. For example, suppose we have the method
     * {@code public Speed getEgoSpeed()} for the tactical planner to use, and {@code private Speed computeEgoSpeed()} for
     * internal use, then we could use the following line inside {@code getEgoSpeed()}:
     *
     * <pre>
     * return computeIfAbsent(MyEnum.KEY, () -&gt; computeEgoSpeed());
     * </pre>
     *
     * @param key key defining which information is requested
     * @param supplier supplier
     * @param <T> value type
     * @return cached or computed value
     */
    @SuppressWarnings("unchecked")
    protected <T> T computeIfAbsent(final Object key, final Supplier<T> supplier)
    {
        checkCache();
        T t = (T) this.cache.get(key);
        if (t == null)
        {
            t = supplier.get();
            this.cache.put(key, t);
        }
        return t;
    }

    /**
     * Checks whether the cache needs to be clear and calls {@link #clearCache} if so.
     */
    protected void checkCache()
    {
        double t = this.simulator.getSimulatorTime().si;
        if (this.time < t)
        {
            clearCache();
            this.time = t;
        }
    }

    /**
     * Clears the cache when data is requested at a different time.
     */
    protected void clearCache()
    {
        this.cache.clear();
        for (Map<Enum<?>, Object> map : this.lateralDirectionCache)
        {
            map.clear();
        }
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information. This information is context specific.
     * <p>
     * This method will compute the information if required. A simple manner to define a {@code Supplier<T>} is to create a
     * method and invoke it using a lambda expression. For example, suppose we have the method
     * {@code public List<HeadwayGtu> getLeaders(Lane)} for the tactical planner to use, and
     * {@code private List<HeadwayGtu> computeLeaders(Lane)} for internal use, then we could use the following line inside
     * {@code getLeaders(Lane)}:
     *
     * <pre>
     * return computeIfAbsent(MyEnum.LEADERS, () -&gt; computeLeaders(lane), lane)
     * </pre>
     *
     * @param key key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier supplier
     * @param context object defining the context, e.g. the lane for which the information is requested
     * @param <T> value type
     * @return cached or computed value
     */
    protected <T> T computeIfAbsent(final Enum<?> key, final Supplier<T> supplier, final Object context)
    {
        return computeIfAbsent(new SingleContextKey(key, context), supplier);
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information. This information is context specific.
     * <p>
     * This method will compute the information if required. A simple manner to define a {@code Supplier<T>} is to create a
     * method and invoke it using a lambda expression. For example, suppose we have the method
     * {@code public List<HeadwayGtu> getLeadersWithIndicator(LateralDirectionality)} for the tactical planner to use, and
     * {@code private List<HeadwayGtu> computeLeadersWithIndicator(LateralDirectionality)} for internal use, then we could use
     * the following line inside {@code getLeadersWithIndicator(LateralDirectionality)}:
     *
     * <pre>
     * return computeIfAbsent(MyEnum.LEADERS, () -&gt; computeLeadersWithIndicator(latDir), latDir)
     * </pre>
     *
     * @param key key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier supplier
     * @param context object defining the context, e.g. the lane for which the information is requested
     * @param <T> value type
     * @return cached or computed value
     */
    @SuppressWarnings("unchecked")
    protected <T> T computeIfAbsent(final Enum<?> key, final Supplier<T> supplier, final LateralDirectionality context)
    {
        checkCache();
        Map<Enum<?>, Object> c = this.lateralDirectionCache[context.ordinal()];
        T t = (T) c.get(key);
        if (t == null)
        {
            t = supplier.get();
            c.put(key, t);
        }
        return t;
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information. This information is context specific.
     * <p>
     * This method will compute the information if required. A simple manner to define a {@code Supplier<T>} is to create a
     * method and invoke it using a lambda expression. For example, suppose we have the method
     * {@code public List<HeadwayGtu> getLeadersWithIndicator(Lane, LateralDirectionality)} for the tactical planner to use, and
     * {@code private List<HeadwayGtu> computeLeadersWithIndicator(Lane, LateralDirectionality)} for internal use, then we could
     * use the following line inside {@code getLeadersWithIndicator(Lane, LateralDirectionality)}:
     *
     * <pre>
     * return computeIfAbsent(MyEnum.LEADERS, () -&gt; computeLeadersWithIndicator(lane, latDir), lane, latDir)
     * </pre>
     *
     * @param key key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier supplier
     * @param context1 first object defining the context, e.g. the lane for which the information is requested
     * @param context2 second object defining the context, e.g. the direction for which the information is requested
     * @param <T> value type
     * @return cached or computed value
     */
    protected <T> T computeIfAbsent(final Enum<?> key, final Supplier<T> supplier, final Object context1, final Object context2)
    {
        return computeIfAbsent(new DualContextKey(key, context1, context2), supplier);
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information. This information is context specific.
     * <p>
     * It is not advised to use this method at high frequency as it contains slight overhead. Instead, consider defining keys
     * directly for various contexts. For example:
     *
     * <pre>
     * private final Object leftLaneLeadersKey = new Object();
     *
     * private final Object leftLaneFollowersKey = new Object();
     *
     * private final Object righttLaneLeadersKey = new Object();
     *
     * private final Object rightLaneFollowersKey = new Object();
     * </pre>
     *
     * This method will compute the information if required. A simple manner to define a {@code Supplier<T>} is to create a
     * method and invoke it using a lambda expression. For example, suppose we have the method
     * {@code public List<HeadwayGtu> getNeighborsWithIndiator(Lane, LongitudinalDirection, LateralDirectionality)} for the
     * tactical planner to use, and
     * {@code private List<HeadwayGtu> computeNeighborsWithIndiator(Lane, LongitudinalDirection, LateralDirectionality)} for
     * internal use, then we could use the following line inside
     * {@code getNeighborsWithIndiator(Lane, LongitudinalDirection, LateralDirectionality)}:
     *
     * <pre>
     * return computeIfAbsent(MyEnum.NEIGHBORS, () -&gt; computeNeighborsWithIndiator(lane, longDir, latDir))
     * </pre>
     *
     * @param key key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier supplier
     * @param context1 first object defining the context, e.g. the lane for which the information is requested
     * @param context2 second object defining the context, e.g. the direction for which the information is requested
     * @param context3 further objects defining the context
     * @param <T> value type
     * @return cached or computed value
     */
    protected <T> T computeIfAbsent(final Enum<?> key, final Supplier<T> supplier, final Object context1, final Object context2,
            final Object... context3)
    {
        return computeIfAbsent(new MultiContextKey(key, context1, context2, context3), supplier);
    }

    /**
     * Returns a string representation of the cache. This is in the form: {@code [speed=12.3m/s, acceleration=2.1m/s2]} and can
     * be used in {code toString()} methods of sub-classes.
     * @return string representation of the cache.
     */
    protected String cacheAsString()
    {
        StringBuilder str = new StringBuilder("[");
        String sep = "";
        for (Entry<Object, Object> entry : this.cache.entrySet())
        {
            str.append(sep).append(entry.getKey()).append("=").append(entry.getValue());
            sep = ", ";
        }
        str.append("]");
        return str.toString();
    }

    /**
     * Key with single context.
     * @param dataKey data key
     * @param context context
     */
    private record SingleContextKey(Enum<?> dataKey, Object context)
    {
        @Override
        public int hashCode()
        {
            return 31 * this.dataKey.hashCode() + this.context.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            SingleContextKey other = (SingleContextKey) obj;
            return Objects.equals(this.context, other.context) && Objects.equals(this.dataKey, other.dataKey);
        }
    }

    /**
     * Key with dual context.
     * @param dataKey data key
     * @param context1 context 1
     * @param context2 context 2
     */
    private record DualContextKey(Enum<?> dataKey, Object context1, Object context2)
    {
        @Override
        public int hashCode()
        {
            int result = this.dataKey.hashCode();
            result = 31 * result + this.context1.hashCode();
            result = 31 * result + this.context2.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            DualContextKey other = (DualContextKey) obj;
            return Objects.equals(this.dataKey, other.dataKey) && Objects.equals(this.context1, other.context1)
                    && Objects.equals(this.context2, other.context2);
        }
    }

    /**
     * Key with variable context.
     * @param dataKey data key
     * @param context1 context 1
     * @param context2 context 2
     * @param context3 further context
     */
    private record MultiContextKey(Enum<?> dataKey, Object context1, Object context2, Object[] context3)
    {
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.deepHashCode(this.context3);
            result = prime * result + Objects.hash(this.dataKey, this.context1, this.context2);
            return result;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            MultiContextKey other = (MultiContextKey) obj;
            return Objects.equals(this.dataKey, other.dataKey) && Objects.equals(this.context1, other.context1)
                    && Objects.equals(this.context2, other.context2) && Arrays.deepEquals(this.context3, other.context3);
        }
    }

}
