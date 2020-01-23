package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Super class for perception categories that use a {@code LaneBasedGTU} and that use lazy evaluation.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class LaneBasedAbstractPerceptionCategory extends AbstractPerceptionCategory<LaneBasedGTU, LanePerception>
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /**
     * Maximum deceleration that is used to determine if a vehicle will attempt to stop for a yellow light. <br>
     * Derived from the report <cite>Onderzoek geeltijden</cite> by Goudappel Coffeng.
     */
    public static final Acceleration MAX_YELLOW_DECELERATION = new Acceleration(-2.8, AccelerationUnit.METER_PER_SECOND_2);

    /**
     * Maximum deceleration that is used to determine if a vehicle will attempt to stop for a red light. <br>
     * Not based on any scientific source; sorry.
     */
    public static final Acceleration MAX_RED_DECELERATION = new Acceleration(-5, AccelerationUnit.METER_PER_SECOND_2);

    /** Nested maps to contain context specific keys for given information keys. */
    private final Map<Object, Map<Object, Object>> contextualKeyMap = new LinkedHashMap<>();

    /** Map from key, either non-contextual or contextual, and time-stamped object. */
    private Map<Object, TimeStampedObject<?>> cache = new LinkedHashMap<>();

    /**
     * @param perception LanePerception; perception
     */
    public LaneBasedAbstractPerceptionCategory(final LanePerception perception)
    {
        super(perception);
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
     * @param key Object; key defining which information is requested
     * @param supplier Supplier&lt;T&gt;;
     * @param <T> value type
     * @return T; cached or computed value
     */
    protected <T> T computeIfAbsent(final Object key, final Supplier<T> supplier)
    {
        @SuppressWarnings("unchecked")
        TimeStampedObject<T> stampedObject = (TimeStampedObject<T>) this.cache.get(key);
        try
        {
            if (stampedObject == null || stampedObject.getTimestamp().lt(getGtu().getSimulator().getSimulatorTime()))
            {
                stampedObject = new TimeStampedObject<>(supplier.get(), getGtu().getSimulator().getSimulatorTime());
            }
            return stampedObject.getObject();
        }
        catch (GTUException ex)
        {
            throw new RuntimeException("Could not obtain time from GTU.");
        }
    }

    /**
     * Returns the cached value for the given key, or computes it if it's absent or not from the current simulation time. The
     * key represents a type of information, e.g. 'leading GTUs'. This information is context specific, namely the lane for
     * which 'leading GTUs' are requested.
     * <p>
     * This method will compute the information if required. A simple manner to define a {@code Supplier<T>} is to create a
     * method and invoke it using a lambda expression. For example, suppose we have the method
     * {@code public List<HeadwayGTU> getLeaders(Lane)} for the tactical planner to use, and
     * {@code private List<HeadwayGTU> computeLeaders(Lane)} for internal use, then we could use the following line inside
     * {@code getLeaders(Lane)}:
     * 
     * <pre>
     * return computeIfAbsent("leaders", () -&gt; computeLeaders(lane))
     * </pre>
     * 
     * @param key Object; key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier Supplier&lt;T&gt;;
     * @param context Object; object defining the context, e.g. the lane for which the information is requested
     * @param <T> value type
     * @return T; cached or computed value
     */
    protected <T> T computeIfAbsent(final Object key, final Supplier<T> supplier, final Object context)
    {
        return computeIfAbsent(contextualKey(key, context), supplier);
    }

    /**
     * Returns a key that is unique for the given information key and singled-object context.
     * @param key Object; information key
     * @param context Object; context, for example the lane to which the information applies
     * @return Object; key that is unique for the given information key and singled-object context
     */
    private Object contextualKey(final Object key, final Object context)
    {
        Map<Object, Object> map = this.contextualKeyMap.computeIfAbsent(key, (k) -> new LinkedHashMap<>());
        return map.computeIfAbsent(key, (k) -> new Object());
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
     * {@code public List<HeadwayGTU> getNeighbors(Lane, LongiudinalDirection)} for the tactical planner to use, and
     * {@code private List<HeadwayGTU> computeNeighbors(Lane, LongiudinalDirection)} for internal use, then we could use the
     * following line inside {@code getNeighbors(Lane, LongiudinalDirection)}:
     * 
     * <pre>
     * return computeIfAbsent("neighbors", () -&gt; computeNeighbors(lane, longDir))
     * </pre>
     * 
     * @param key Object; key defining which information is requested, it may be contextual if it's context dependent
     * @param supplier Supplier&lt;T&gt;;
     * @param context Object...; objects defining the context, e.g. the lane and direction for which the information is
     *            requested
     * @param <T> value type
     * @return T; cached or computed value
     */
    protected <T> T computeIfAbsent(final Object key, final Supplier<T> supplier, final Object... context)
    {
        return computeIfAbsent(contextualKey(key, context), supplier);
    }

    /**
     * Returns a key that is unique for the given information key and multi-object context.
     * @param key Object; information key
     * @param context Object...; context, for example the lane and longitudinal direction to which the information applies
     * @return Object; key that is unique for the given information key and multi-object context
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
                return map.computeIfAbsent(key, (k) -> new Object());
            }
            // intermediate layer, we need to obtain the next layer's map
            map = (Map<Object, Object>) map.computeIfAbsent(context[i], (k) -> new LinkedHashMap<>());
        }
        throw new RuntimeException("Unexpected exception while obtaining contextual key for specific perceived info.");
    }

}
