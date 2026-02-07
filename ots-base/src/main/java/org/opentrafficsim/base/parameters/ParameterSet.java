package org.opentrafficsim.base.parameters;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djutils.exceptions.Throw;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.OtsRuntimeException;

/**
 * Implementation of {@link Parameters} with methods to initialize the set of parameters.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterSet implements Parameters
{

    /** Object to recognize that no value was set previously. */
    private static final Empty EMPTY = new Empty();

    /** Whether to copy internal data on write. */
    private boolean copyOnWrite = false;

    /** List of parameters. */
    private Map<ParameterType<?>, Object> parameters;

    /** Keys that claim setting parameters. */
    private Map<ParameterType<?>, Object> keys = new LinkedHashMap<>();

    /** List of parameters with values before last set. */
    private Map<ParameterType<?>, Object> previous;

    /**
     * Construct a new, empty Parameters set.
     */
    public ParameterSet()
    {
        this.parameters = new LinkedHashMap<>();
        this.previous = new LinkedHashMap<>();
    }

    /**
     * Constructor which creates a copy of the input set.
     * @param parameters input set to copy into the new Parameters object
     */
    public ParameterSet(final Parameters parameters)
    {
        if (parameters instanceof ParameterSet)
        {
            ParameterSet parameterSet = (ParameterSet) parameters;
            this.parameters = parameterSet.parameters;
            this.previous = parameterSet.previous;
            this.copyOnWrite = true;
            parameterSet.copyOnWrite = true;
        }
        else
        {
            parameters.setAllIn(this);
        }
    }

    @Override
    public <T> void setParameter(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        Throw.whenNull(parameterType, "parameterType");
        Throw.when(value == null, ParameterException.class,
                "Parameter of type '%s' was assigned a null value, this is not allowed.", parameterType.getId());
        Object key = this.keys.get(parameterType);
        Throw.when(key != null, ParameterException.class, "Parameter of type '%s' is set, but it is claimed by '%s'.",
                parameterType.getId(), key);
        saveSetParameter(parameterType, value, false);
    }

    @Override
    public <T> void setClaimedParameter(final ParameterType<T> parameterType, final T value, final Object key)
            throws ParameterException
    {
        Throw.whenNull(parameterType, "parameterType");
        Throw.when(value == null, ParameterException.class,
                "Parameter of type '%s' was assigned a null value, this is not allowed.", parameterType.getId());
        Throw.whenNull(key, "key");
        Object previousKey = this.keys.putIfAbsent(parameterType, key);
        Throw.when(previousKey != null && !key.equals(previousKey), ParameterException.class,
                "Parameter of type '%s' was assigned trying to claim from '%s' but was claimed by '%s'.", parameterType.getId(),
                key, previousKey);
        saveSetParameter(parameterType, value, false);
    }

    @Override
    public <T> void setParameterResettable(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        saveSetParameter(parameterType, value, true);
    }

    /**
     * Sets a parameter value while checking conditions.
     * @param parameterType the parameter type
     * @param value new value for the parameter
     * @param resettable whether the parameter set should be resettable
     * @param <T> Class of the value
     * @throws ParameterException If the value does not comply with constraints.
     */
    private <T> void saveSetParameter(final ParameterType<T> parameterType, final T value, final boolean resettable)
            throws ParameterException
    {
        parameterType.check(value, this);
        parameterType.checkConstraint(value);
        checkCopyOnWrite();
        if (resettable)
        {
            Object prevValue = this.parameters.get(parameterType);
            if (prevValue == null)
            {
                // remember that there was no value before this set
                this.previous.put(parameterType, EMPTY);
            }
            else
            {
                this.previous.put(parameterType, prevValue);
            }
        }
        else
        {
            // no reset after non-resettale set
            this.previous.remove(parameterType);
        }
        this.parameters.put(parameterType, value);
    }

    @Override
    public void resetParameter(final ParameterType<?> parameterType) throws ParameterException
    {
        checkCopyOnWrite();
        Object prevValue = this.previous.remove(parameterType);
        Throw.when(prevValue == null, ParameterException.class,
                "Reset on parameter of type '%s' could not be performed, it was not set resettable.", parameterType.getId());
        if (prevValue instanceof Empty)
        {
            // no value was set before last set, so make parameter type not set
            this.parameters.remove(parameterType);
        }
        else
        {
            this.parameters.put(parameterType, prevValue);
        }
    }

    /**
     * Copy the internal data if needed.
     */
    private void checkCopyOnWrite()
    {
        if (this.copyOnWrite)
        {
            this.parameters = new LinkedHashMap<>(this.parameters);
            this.previous = new LinkedHashMap<>(this.previous);
            this.copyOnWrite = false;
        }
    }

    @Override
    public <T> T getParameter(final ParameterType<T> parameterType) throws ParameterException
    {
        @SuppressWarnings("unchecked")
        // set methods guarantee matching of parameter type and value
        T result = (T) this.parameters.get(parameterType);
        Throw.when(result == null, ParameterException.class, "Could not get parameter of type '%s' as it was not set.",
                parameterType.getId());
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptionalParameter(final ParameterType<T> parameterType)
    {
        // set methods guarantee matching of parameter type and value
        return Optional.ofNullable((T) this.parameters.get(parameterType));
    }

    @Override
    public boolean contains(final ParameterType<?> parameterType)
    {
        return this.parameters.containsKey(parameterType);
    }

    /**
     * Returns a safe copy of the parameters.
     * @return a safe copy of the parameters, e.g., for printing
     */
    public Map<ParameterType<?>, Object> getParameters()
    {
        return new LinkedHashMap<>(this.parameters);
    }

    /**
     * Sets the default value of a parameter. Default value sets are not resettable.
     * @param parameter the parameter to set the default value of
     * @param <T> Class of the value
     * @return this set of parameters (for method chaining)
     * @throws ParameterException if the parameter type has no default value
     */
    public <T> ParameterSet setDefaultParameter(final ParameterType<T> parameter) throws ParameterException
    {
        T defaultValue = parameter.getDefaultValue();
        try
        {
            saveSetParameter(parameter, defaultValue, false);
        }
        catch (ParameterException pe)
        {
            // should not happen, default value and parameter type are connected
            throw new OtsRuntimeException(pe);
        }
        return this;
    }

    /**
     * Sets the default values of all accessible parameters defined in the given class. Default value sets are not
     * resettable.<br>
     * @param clazz class with parameters
     * @return this set of parameters (for method chaining)
     */
    public ParameterSet setDefaultParameters(final Class<?> clazz)
    {
        return setDefaultParametersLocal(clazz);
    }

    /**
     * Sets the default values of all accessible parameters defined in the given class. Default value sets are not resettable.
     * @param clazz class with parameters
     * @param <T> Class of the value
     * @return this set of parameters (for method chaining)
     */
    @SuppressWarnings("unchecked")
    private <T> ParameterSet setDefaultParametersLocal(final Class<?> clazz)
    {
        // set all default values using reflection
        Set<Field> fields = ClassUtil.getAllFields(clazz);

        for (Field field : fields)
        {
            if (ParameterType.class.isAssignableFrom(field.getType()))
            {
                try
                {
                    field.setAccessible(true);
                    ParameterType<T> p = (ParameterType<T>) field.get(clazz);
                    saveSetParameter(p, p.getDefaultValue(), false);
                }
                catch (IllegalArgumentException iare)
                {
                    // should not happen, field and clazz are related
                    throw new OtsRuntimeException(iare);
                }
                catch (IllegalAccessException iace)
                {
                    // parameter type not public
                    throw new OtsRuntimeException(iace);
                }
                catch (ParameterException pe)
                {
                    // do not set parameter without default value
                    throw new OtsRuntimeException(pe);
                }
            }
        }
        return this;
    }

    @Override
    public void setAllIn(final Parameters params)
    {
        if (params instanceof ParameterSet)
        {
            ParameterSet parameterSet = (ParameterSet) params;
            parameterSet.checkCopyOnWrite();
            parameterSet.parameters.putAll(this.parameters);
        }
        else
        {
            setAllOneByOne(params);
        }
    }

    /**
     * Sets the parameters of this set in the given set.
     * @param params parameters to set the values in
     * @param <T> parameter value type
     */
    @SuppressWarnings("unchecked")
    private <T> void setAllOneByOne(final Parameters params)
    {
        for (ParameterType<?> parameterType : this.parameters.keySet())
        {
            try
            {
                params.setParameter((ParameterType<T>) parameterType, (T) this.parameters.get(parameterType));
            }
            catch (ParameterException exception)
            {
                throw new OtsRuntimeException(exception); // should not happen
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder("Parameters [");
        String sep = "";
        for (ParameterType<?> apt : this.parameters.keySet())
        {
            try
            {
                out.append(sep).append(apt.getId()).append("=").append(apt.printValue(this));
                sep = ", ";
            }
            catch (ParameterException pe)
            {
                // We know the parameter has been set as we get the keySet from parameters
                throw new OtsRuntimeException(pe);
            }
        }
        out.append("]");
        return out.toString();
    }

    /**
     * Class of object to put in the internal Map of Parameters to indicate that no value was set.
     */
    private static class Empty extends Dimensionless
    {
        /** */
        private static final long serialVersionUID = 20160414L;

        /**
         * Constructor for Empty.
         */
        Empty()
        {
            super(Double.NaN, DimensionlessUnit.SI);
        }

        @Override
        public String toString()
        {
            return "Empty []";
        }

    }

}
