package org.opentrafficsim.ahfe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.PerceptionException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedAbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedPerceptionCategory;

/**
 * Utility superclass for perception categories with single delayed snapshots.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@Deprecated
public abstract class AbstractDelayedPerceptionCategory extends LaneBasedAbstractPerceptionCategory
        implements LaneBasedPerceptionCategory
{

    /** Margin of 1 millisecond. */
    private static final Duration MARGIN = Duration.instantiateSI(0.001);

    /** */
    private static final long serialVersionUID = 20170217L;

    /**
     * @param perception LanePerception; perception
     */
    public AbstractDelayedPerceptionCategory(final LanePerception perception)
    {
        super(perception);
    }

    /** Map of info type and list of time stamped data of that info type. */
    private final LinkedHashMap<DelayedInfoType<?>, LinkedHashMap<RelativeLane, List<TimeStampedObject<?>>>> map =
            new LinkedHashMap<>();

    /**
     * Set info of given delayed info type, not pertaining to any lane.
     * @param delayedInfoType DelayedInfoType&lt;T&gt;; info type
     * @param info TimeStampedObject&lt;T&gt;; info
     * @param <T> data type of delayed info
     */
    public final <T> void setInfo(final DelayedInfoType<T> delayedInfoType, final TimeStampedObject<T> info)
    {
        setInfo(delayedInfoType, null, info);
    }

    /**
     * Set info of given delayed info type, pertaining to a lane.
     * @param delayedInfoType DelayedInfoType&lt;T&gt;; info type
     * @param lane RelativeLane; lane, may be {@code null}
     * @param info TimeStampedObject&lt;T&gt;; info
     * @param <T> data type of delayed info
     */
    public final <T> void setInfo(final DelayedInfoType<T> delayedInfoType, final RelativeLane lane,
            final TimeStampedObject<T> info)
    {
        Throw.whenNull(delayedInfoType, "Delayed info type may not be null.");
        Throw.whenNull(info, "Info may not be null.");
        if (!this.map.containsKey(delayedInfoType))
        {
            this.map.put(delayedInfoType, new LinkedHashMap<>());
        }
        if (!this.map.get(delayedInfoType).containsKey(lane))
        {
            this.map.get(delayedInfoType).put(lane, new ArrayList<TimeStampedObject<?>>());
        }
        List<TimeStampedObject<?>> list = this.map.get(delayedInfoType).get(lane);
        if (!list.isEmpty())
        {
            Throw.when(!list.isEmpty() && info.getTimestamp().le(list.get(list.size() - 1).getTimestamp()),
                    RuntimeException.class,
                    "Setting delayed info for type %s with timestamp %s while info with timestamp %s is already present.",
                    delayedInfoType, info.getTimestamp(), list.get(list.size() - 1).getTimestamp());
        }

        // append data at end
        list.add(info);
    }

    /**
     * Returns the most recent info of the given type, that is older than the delay. If all data is more recent than the delay,
     * the oldest data is returned. If no data is present, an exception is thrown.
     * @param delayedInfoType DelayedInfoType&lt;T&gt;; info type
     * @param <T> data type of the info type
     * @return info of the given type
     * @throws PerceptionException if info was not perceived
     */
    public final <T> TimeStampedObject<T> getInfo(final DelayedInfoType<T> delayedInfoType) throws PerceptionException
    {
        return getInfo(delayedInfoType, null);
    }

    /**
     * Returns the most recent info of the given type, that is older than the delay. If all data is more recent than the delay,
     * the oldest data is returned. If no data is present, an exception is thrown.
     * @param delayedInfoType DelayedInfoType&lt;T&gt;; info type
     * @param lane RelativeLane; lane the data pertains to, may be {@code null}
     * @param <T> data type of the info type
     * @return info of the given type
     * @throws PerceptionException if info was not perceived
     */
    @SuppressWarnings("unchecked")
    public final <T> TimeStampedObject<T> getInfo(final DelayedInfoType<T> delayedInfoType, final RelativeLane lane)
            throws PerceptionException
    {
        Throw.whenNull(delayedInfoType, "Delayed info type may not be null.");
        Throw.when(!this.map.containsKey(delayedInfoType), PerceptionException.class,
                "Perception does not contain any data for info type %s.", delayedInfoType);
        Throw.when(!this.map.get(delayedInfoType).containsKey(lane), PerceptionException.class,
                "Perception does not contain any data for info type %s for lane %s.", delayedInfoType, lane);
        List<TimeStampedObject<?>> list = this.map.get(delayedInfoType).get(lane);
        Throw.when(list.isEmpty(), RuntimeException.class, "Perception does not contain any data for info type %s.",
                delayedInfoType);

        // remove old data if required
        Parameters params;
        Time now;
        try
        {
            params = getPerception().getGtu().getParameters();
            now = getPerception().getGtu().getSimulator().getSimulatorAbsTime();
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("GTU not yet initialized.", exception);
        }
        Time delayedTime;
        try
        {
            delayedTime = now.minus(params.getParameter(delayedInfoType.getDelayParameter())).plus(MARGIN);
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Delay parameter not found.", exception);
        }
        while (list.size() > 1 && list.get(1).getTimestamp().le(delayedTime))
        {
            list.remove(0);
        }

        return (TimeStampedObject<T>) list.get(0);
    }

    /**
     * Move data coupled to a lane to another lane to account for a lane change. The tactical planner needs to call this exactly
     * when it flips logic concerning the origin and target lane.
     * @param dir LateralDirectionality; direction of lane change
     */
    public final void changeLane(final LateralDirectionality dir)
    {
        for (DelayedInfoType<?> delayedInfoType : this.map.keySet())
        {
            LinkedHashMap<RelativeLane, List<TimeStampedObject<?>>> newMap = new LinkedHashMap<>();
            for (RelativeLane lane : this.map.get(delayedInfoType).keySet())
            {
                if (lane != null)
                {
                    newMap.put(dir.isLeft() ? lane.getRight() : lane.getLeft(), this.map.get(delayedInfoType).get(lane));
                }
                else
                {
                    newMap.put(lane, this.map.get(delayedInfoType).get(lane));
                }
            }
            this.map.put(delayedInfoType, newMap);
        }
    }

    /**
     * Superclass for delayed info.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> type of information.
     */
    public static class DelayedInfoType<T> extends Type<DelayedInfoType<T>> implements Identifiable
    {

        /** Id. */
        private final String id;

        /** Parameter for delay. */
        private final ParameterTypeDuration delayParameter;

        /**
         * Constructor.
         * @param id String; id
         * @param delayParameter ParameterTypeDuration; delay parameter type
         */
        public DelayedInfoType(final String id, final ParameterTypeDuration delayParameter)
        {
            this.id = id;
            this.delayParameter = delayParameter;
        }

        /**
         * Returns the id.
         * @return id
         */
        @Override
        public final String getId()
        {
            return this.getId();
        }

        /**
         * Returns the delay parameter type.
         * @return delayParameter
         */
        public final ParameterTypeDuration getDelayParameter()
        {
            return this.delayParameter;
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public final boolean equals(final Object obj)
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
            DelayedInfoType<?> other = (DelayedInfoType<?>) obj;
            if (this.id == null)
            {
                if (other.id != null)
                {
                    return false;
                }
            }
            else if (!this.id.equals(other.id))
            {
                return false;
            }
            return true;
        }

    }

}
