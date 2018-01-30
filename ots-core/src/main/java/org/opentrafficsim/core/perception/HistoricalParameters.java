package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.Parameters;

/**
 * Historical representation of {@code Parameters}.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HistoricalParameters extends
        AbstractHistorical<HistoricalParameters.ParameterValueSet, HistoricalParameters.ParameterEvent> implements Parameters
{

    /** Current parameter set. */
    private final Parameters parameters;

    /**
     * Constructor.
     * @param manager HistoryManager; history manager
     * @param parameters Parameters; initial parameter set
     */
    public HistoricalParameters(final HistoryManager manager, final Parameters parameters)
    {
        super(manager);
        this.parameters = parameters;
    }

    /**
     * Get the parameters at the current simulation time.
     * @return Parameters; parameters at the current simulation time
     */
    public Parameters getParameters()
    {
        return new ParameterSet(this.parameters);
    }

    /**
     * Get the parameters at the given simulation time.
     * @param time Time; simulation time
     * @return Parameters; parameters at the given simulation time
     */
    public Parameters getParameters(final Time time)
    {
        Parameters params = getParameters();
        for (ParameterEvent event : getEvents(time))
        {
            event.resetEvent(params);
        }
        return params;
    }

    /** {@inheritDoc} */
    @Override
    public <T> void setParameter(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.parameters));
        this.parameters.setParameter(parameterType, value);
    }

    /** {@inheritDoc} */
    @Override
    public <T> void setParameterResettable(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.parameters));
        this.parameters.setParameterResettable(parameterType, value);
    }

    /** {@inheritDoc} */
    @Override
    public void resetParameter(final ParameterType<?> parameterType) throws ParameterException
    {
        addEvent(new ParameterEvent(now().si, parameterType, this.parameters));
        this.parameters.resetParameter(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getParameter(final ParameterType<T> parameterType) throws ParameterException
    {
        return this.parameters.getParameter(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getParameterOrNull(final ParameterType<T> parameterType)
    {
        return this.parameters.getParameterOrNull(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final ParameterType<?> parameterType)
    {
        return this.parameters.contains(parameterType);
    }

    /** {@inheritDoc} */
    @Override
    public void setAllIn(final Parameters parameters)
    {
        this.parameters.setAllIn(parameters);
    }

    /**
     * Value for a parameter event, which contains a parameter type and (the previous) value.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class ParameterValueSet
    {

        /** Parameter type. */
        final ParameterType<?> parameter;

        /** Previous parameter value. */
        final Object value;

        /**
         * @param value
         * @param parameter
         */
        public <T> ParameterValueSet(ParameterType<T> parameter, final T value)
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

    }

    /**
     * Parameter event, which will restore the previous value.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class ParameterEvent extends AbstractHistorical.EventValue<ParameterValueSet> // import is removed
    {

        /**
         * Constructor. New value is not required, as it's not required to restore the state from before the change.
         * @param time double; time of event
         * @param parameterType ParameterType; parameter type
         * @param parameters Parameters; parameters
         */
        public <T> ParameterEvent(final double time, final ParameterType<T> parameterType, final Parameters parameters)
        {
            super(time, new ParameterValueSet(parameterType, parameters.getParameterOrNull(parameterType)));
        }

        /**
         * Resets the parameter type to it's value before the change.
         * @param parameters
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

    }

}
