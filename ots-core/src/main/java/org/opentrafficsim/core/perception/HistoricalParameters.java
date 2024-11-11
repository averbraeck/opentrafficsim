package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.perception.HistoricalParameters.ParameterEvent;
import org.opentrafficsim.core.perception.HistoricalParameters.ParameterValueSet;

/**
 * Historical representation of {@code Parameters}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class HistoricalParameters extends AbstractHistorical<ParameterValueSet<?>, ParameterEvent> implements Parameters
{

    /** Current parameter set. */
    private final Parameters params;

    /**
     * Constructor.
     * @param manager history manager
     * @param parameters initial parameter set
     */
    public HistoricalParameters(final HistoryManager manager, final Parameters parameters)
    {
        super(manager);
        this.params = parameters;
    }

    /**
     * Get the parameters at the current simulation time.
     * @return parameters at the current simulation time
     */
    public Parameters getParameters()
    {
        return new ParameterSet(this.params);
    }

    /**
     * Get the parameters at the given simulation time.
     * @param time simulation time
     * @return parameters at the given simulation time
     */
    public Parameters getParameters(final Time time)
    {
        Parameters parameters = getParameters();
        for (ParameterEvent event : getEvents(time))
        {
            event.resetEvent(parameters);
        }
        return parameters;
    }

    @Override
    public <T> void setParameter(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.params));
        this.params.setParameter(parameterType, value);
    }

    @Override
    public <T> void setParameterResettable(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.params));
        this.params.setParameterResettable(parameterType, value);
    }

    @Override
    public void resetParameter(final ParameterType<?> parameterType) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.params));
        this.params.resetParameter(parameterType);
    }

    @Override
    public <T> T getParameter(final ParameterType<T> parameterType) throws ParameterException
    {
        return this.params.getParameter(parameterType);
    }

    @Override
    public <T> T getParameterOrNull(final ParameterType<T> parameterType)
    {
        return this.params.getParameterOrNull(parameterType);
    }

    @Override
    public boolean contains(final ParameterType<?> parameterType)
    {
        return this.params.contains(parameterType);
    }

    @Override
    public void setAllIn(final Parameters parameters)
    {
        this.params.setAllIn(parameters);
    }

    @Override
    public String toString()
    {
        return "HistoricalParameters [params=" + this.params + "]";
    }

    /**
     * Value for a parameter event, which contains a parameter type and (the previous) value.
     * @param <T> type of parameter
     * @param parameter parameter
     * @param value parameter value
     */
    record ParameterValueSet<T>(ParameterType<T> parameter, T value)
    {
    }

    /**
     * Parameter event, which will restore the previous value.
     */
    public static class ParameterEvent extends AbstractHistorical.EventValue<ParameterValueSet<?>> // import is removed
    {

        /**
         * Constructor. New value is not required, as it's not required to restore the state from before the change.
         * @param time time of event
         * @param parameterType parameter type
         * @param parameters parameters
         * @param <T> parameter value type
         */
        public <T> ParameterEvent(final double time, final ParameterType<T> parameterType, final Parameters parameters)
        {
            super(time, new ParameterValueSet<T>(parameterType, parameters.getParameterOrNull(parameterType)));
        }

        /**
         * Resets the parameter type to it's value before the change.
         * @param parameters parameters
         * @param <T> parameter value type
         */
        @SuppressWarnings("unchecked")
        public final <T> void resetEvent(final Parameters parameters)
        {
            try
            {
                parameters.setParameter((ParameterType<T>) getValue().parameter(), (T) getValue().value());
            }
            catch (ParameterException exception)
            {
                // should not happen
                throw new RuntimeException(exception);
            }
        }

        @Override
        public String toString()
        {
            return "ParameterEvent [time=" + getTime() + ", value=" + getValue() + "]";
        }

    }

}
