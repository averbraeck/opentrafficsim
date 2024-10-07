package org.opentrafficsim.core.gtu.perception;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * Implements {@code PerceptionCategory} and allows sub-classes to easily implement lazy evaluation through the
 * {@code computeIfAbsent()} methods.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public abstract class AbstractPerceptionCategory<G extends Gtu, P extends Perception<G>>
        implements Serializable, PerceptionCategory<G, P>, Type<AbstractPerceptionCategory<G, P>>
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Connected perception. */
    private final P perception;

    /** Nested maps to contain context specific keys for given information keys. */
    private final Map<Object, Map<Object, Object>> contextualKeyMap = new LinkedHashMap<>();

    /** Map from key, either non-contextual or contextual, and time-stamped object. */
    private Map<Object, TimeStampedObject<?>> cache = new LinkedHashMap<>();

    /**
     * Constructor setting the perception.
     * @param perception perception
     */
    public AbstractPerceptionCategory(final P perception)
    {
        this.perception = perception;
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
     * @throws GtuException if the GTU has not been initialized
     */
    public final Time getTimestamp() throws GtuException
    {
        if (getGtu() == null)
        {
            throw new GtuException("gtu value has not been initialized for LanePerception when perceiving.");
        }
        return getGtu().getSimulator().getSimulatorAbsTime();
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
     * return computeIfAbsent("speedLimit", () -&gt; computeEgoSpeed());
     * </pre>
     * 
     * @param key key defining which information is requested
     * @param supplier
     * @param <T> value type
     * @return cached or computed value
     */
    protected <T> T computeIfAbsent(final Object key, final Supplier<T> supplier)
    {
        @SuppressWarnings("unchecked")
        TimeStampedObject<T> stampedObject = (TimeStampedObject<T>) this.cache.get(key);
        if (stampedObject == null || stampedObject.timestamp().lt(getGtu().getSimulator().getSimulatorAbsTime()))
        {
            stampedObject = new TimeStampedObject<>(supplier.get(), getGtu().getSimulator().getSimulatorAbsTime());
            this.cache.put(key, stampedObject);
        }
        return stampedObject.object();
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information, e.g. 'leading GTUs'. This information is context specific, namely the lane for
     * which 'leading GTUs' are requested.
     * <p>
     * This method will compute the information if required. A simple manner to define a {@code Supplier<T>} is to create a
     * method and invoke it using a lambda expression. For example, suppose we have the method
     * {@code public List<HeadwayGtu> getLeaders(Lane)} for the tactical planner to use, and
     * {@code private List<HeadwayGtu> computeLeaders(Lane)} for internal use, then we could use the following line inside
     * {@code getLeaders(Lane)}:
     * 
     * <pre>
     * return computeIfAbsent("leaders", () -&gt; computeLeaders(lane))
     * </pre>
     * 
     * @param key key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier
     * @param context object defining the context, e.g. the lane for which the information is requested
     * @param <T> value type
     * @return cached or computed value
     */
    protected <T> T computeIfAbsent(final Object key, final Supplier<T> supplier, final Object context)
    {
        return computeIfAbsent(contextualKey(key, context), supplier);
    }

    /**
     * Returns a key that is unique for the given information key and singled-object context.
     * @param key information key
     * @param context context, for example the lane to which the information applies
     * @return key that is unique for the given information key and singled-object context
     */
    private Object contextualKey(final Object key, final Object context)
    {
        Map<Object, Object> map = this.contextualKeyMap.computeIfAbsent(key, (k) -> new LinkedHashMap<>());
        return map.computeIfAbsent(context, (k) -> new Object());
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information, e.g. 'neighboring GTUs'. This information is context specific, namely the lane for
     * which 'neighboring GTUs' are requested, as well as the longitudinal direction.
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
     * {@code public List<HeadwayGtu> getNeighbors(Lane, LongiudinalDirection)} for the tactical planner to use, and
     * {@code private List<HeadwayGtu> computeNeighbors(Lane, LongiudinalDirection)} for internal use, then we could use the
     * following line inside {@code getNeighbors(Lane, LongiudinalDirection)}:
     * 
     * <pre>
     * return computeIfAbsent("neighbors", () -&gt; computeNeighbors(lane, longDir))
     * </pre>
     * 
     * @param key key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier
     * @param context objects defining the context, e.g. the lane and direction for which the information is requested
     * @param <T> value type
     * @return cached or computed value
     */
    protected <T> T computeIfAbsent(final Object key, final Supplier<T> supplier, final Object... context)
    {
        return computeIfAbsent(contextualKey(key, context), supplier);
    }

    /**
     * Returns a key that is unique for the given information key and multi-object context.
     * @param key information key
     * @param context context, for example the lane and longitudinal direction to which the information applies
     * @return key that is unique for the given information key and multi-object context
     */
    @SuppressWarnings("unchecked")
    private Object contextualKey(final Object key, final Object... context)
    {
        // get layer of highest level, the key level
        Map<Object, Object> map = this.contextualKeyMap.computeIfAbsent(key, (k) -> new LinkedHashMap<>());
        for (int i = 0; i < context.length; i++)
        {
            if (i == context.length - 1)
            {
                // deepest layer, i.e. a leaf, we need to return an Object as key
                return map.computeIfAbsent(context[i], (k) -> new Object());
            }
            // intermediate layer, we need to obtain the next layer's map
            map = (Map<Object, Object>) map.computeIfAbsent(context[i], (k) -> new LinkedHashMap<>());
        }
        throw new RuntimeException("Unexpected exception while obtaining contextual key for specific perceived info.");
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
        for (Entry<Object, TimeStampedObject<?>> entry : this.cache.entrySet())
        {
            str.append(sep).append(entry.getKey()).append("=").append(entry.getValue());
            sep = ", ";
        }
        str.append("]");
        return str.toString();
    }

}
