package org.opentrafficsim.road.gtu.perception.categories;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.opentrafficsim.base.collections.IndexedMap;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.RelativeLane;

/**
 * Abstract perception category with dedicated caching for {@link RelativeLane} and {@link LateralDirectionality}
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <C> perception category type
 */
public abstract class AbstractLanePerceptionCategory<C extends PerceptionCategory<?, ?, C>>
        extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception, C>
{

    /** Cache dedicated to {@link RelativeLane} context. */
    private final Map<RelativeLane, Map<Enum<?>, Object>> relativeLaneCache = new IndexedMap<>();

    /** Cache dedicated to {@link RelativeLane} context. */
    @SuppressWarnings("unchecked")
    private final Map<RelativeLane, Map<Enum<?>, Object>>[] lateralDirectionRelativeLaneCache =
            (Map<RelativeLane, Map<Enum<?>, Object>>[]) new Map[LateralDirectionality.values().length];

    /**
     * Constructor setting the perception.
     * @param perception perception
     */
    public AbstractLanePerceptionCategory(final LanePerception perception)
    {
        super(perception);
        for (int i = 0; i < this.lateralDirectionRelativeLaneCache.length; i++)
        {
            this.lateralDirectionRelativeLaneCache[i] = new IndexedMap<>();
        }
    }

    @Override
    protected void clearCache()
    {
        super.clearCache();
        for (Map<?, ?> m : this.relativeLaneCache.values())
        {
            m.clear();
        }
        for (Map<RelativeLane, Map<Enum<?>, Object>> map : this.lateralDirectionRelativeLaneCache)
        {
            for (Map<?, ?> m : map.values())
            {
                m.clear();
            }
        }
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
    protected <T> T computeIfAbsent(final Enum<?> key, final Supplier<T> supplier, final RelativeLane context)
    {
        checkCache();
        Map<Enum<?>, Object> map = this.relativeLaneCache.get(context);
        if (map == null)
        {
            map = new LinkedHashMap<>();
            this.relativeLaneCache.put(context, map);
        }
        T t = (T) map.get(key);
        if (t == null)
        {
            t = supplier.get();
            map.put(key, t);
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
    @SuppressWarnings("unchecked")
    protected <T> T computeIfAbsent(final Enum<?> key, final Supplier<T> supplier, final RelativeLane context1,
            final LateralDirectionality context2)
    {
        checkCache();
        Map<RelativeLane, Map<Enum<?>, Object>> map1 = this.lateralDirectionRelativeLaneCache[context2.ordinal()];
        Map<Enum<?>, Object> map2 = map1.get(context1);
        if (map2 == null)
        {
            map2 = new LinkedHashMap<>();
            map1.put(context1, map2);
            T t = supplier.get();
            map2.put(key, t);
            return t;
        }
        T t = (T) map2.get(key);
        if (t == null)
        {
            t = supplier.get();
            map2.put(key, t);
        }
        return t;
    }

}
