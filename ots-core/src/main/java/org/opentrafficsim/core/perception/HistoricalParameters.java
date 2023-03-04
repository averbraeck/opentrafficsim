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
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class HistoricalParameters extends AbstractHistorical<ParameterValueSet, ParameterEvent> implements Parameters
{

    /** Current parameter set. */
    private final Parameters params;

    /**
     * Constructor.
     * @param manager HistoryManager; history manager
     * @param parameters Parameters; initial parameter set
     */
    public HistoricalParameters(final HistoryManager manager, final Parameters parameters)
    {
        super(manager);
        this.params = parameters;
    }

    /**
     * Get the parameters at the current simulation time.
     * @return Parameters; parameters at the current simulation time
     */
    public Parameters getParameters()
    {
        return new ParameterSet(this.params);
    }

    /**
     * Get the parameters at the given simulation time.
     * @param time Time; simulation time
     * @return Parameters; parameters at the given simulation time
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

    /** {@inheritDoc} */
    @Override
    public <T> void setParameter(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.params));
        this.params.setParameter(parameterType, value);
    }

    /** {@inheritDoc} */
    @Override
    public <T> void setParameterResettable(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.params));
        this.params.setParameterResettable(parameterType, value);
    }

    /** {@inheritDoc} */
    @Override
    public void resetParameter(final ParameterType<?> parameterType) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.params));
        this.params.resetParameter(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getParameter(final ParameterType<T> parameterType) throws ParameterException
    {
        return this.params.getParameter(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getParameterOrNull(final ParameterType<T> parameterType)
    {
        return this.params.getParameterOrNull(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final ParameterType<?> parameterType)
    {
        return this.params.contains(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public void setAllIn(final Parameters parameters)
    {
        this.params.setAllIn(parameters);
    }

    /**
     * Value for a parameter event, which contains a parameter type and (the previous) value.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class ParameterValueSet
    {

        /** Parameter type. */
        private final ParameterType<?> parameter;

        /** Previous parameter value. */
        private final Object value;

        /**
         * @param parameter ParameterType&lt;T&gt;; parameter
         * @param value T; parameter value
         * @param <T> parameter value type
         */
        public <T> ParameterValueSet(final ParameterType<T> parameter, final T value)
        {
            this.value = value;
            this.parameter = parameter;
        }

        /**
         * @return value.
         */
        public Object getValue()
        {
            return this.value;
        }

        /**
         * @return parameter.
         */
        public ParameterType<?> getParameter()
        {
            return this.parameter;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ParameterValueSet [parameter=" + this.parameter + ", value=" + this.value + "]";
        }

    }

    /**
     * Parameter event, which will restore the previous value.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class ParameterEvent extends AbstractHistorical.EventValue<ParameterValueSet> // import is removed
    {

        /**
         * Constructor. New value is not required, as it's not required to restore the state from before the change.
         * @param time double; time of event
         * @param parameterType ParameterType&lt;T&gt;; parameter type
         * @param parameters Parameters; parameters
         * @param <T> parameter value type
         */
        public <T> ParameterEvent(final double time, final ParameterType<T> parameterType, final Parameters parameters)
        {
            super(time, new ParameterValueSet(parameterType, parameters.getParameterOrNull(parameterType)));
        }

        /**
         * Resets the parameter type to it's value before the change.
         * @param parameters Parameters; parameters
         * @param <T> parameter value type
         */
        @SuppressWarnings("unchecked")
        public final <T> void resetEvent(final Parameters parameters)
        {
            try
            {
                parameters.setParameter((ParameterType<T>) getValue().getParameter(), (T) getValue().getValue());
            }
            catch (ParameterException exception)
            {
                // should not happen
                throw new RuntimeException(exception);
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ParameterEvent []";
        }

    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalParameters [params=" + this.params + "]";
    }

}
