package org.opentrafficsim.base.parameters;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.collections.IndexedMap;

/**
 * Implementation of {@link Parameters} with methods to initialize the set of parameters.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class ParameterSet implements Parameters
{

    /** Whether to copy internal data on write. */
    private boolean copyOnWrite = false;

    /** List of parameters. */
    private Map<ParameterType<?>, Object> parameters;

    /** Keys that claim setting parameters. */
    private Map<ParameterType<?>, Object> keys = new IndexedMap<>(ParameterType.maxIndex());

    /** List of parameters with overridden values. */
    private Map<ParameterType<?>, Object> overrides;

    /**
     * Construct a new, empty Parameters set.
     */
    public ParameterSet()
    {
        this.parameters = new IndexedMap<>(ParameterType.maxIndex());
        this.overrides = new IndexedMap<>(ParameterType.maxIndex());
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
            this.overrides = parameterSet.overrides;
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

    /**
     * Sets a parameter value while checking conditions.
     * @param parameterType the parameter type
     * @param value new value for the parameter
     * @param override whether the parameter set should be overridden
     * @param <T> Class of the value
     * @throws ParameterException If the value does not comply with constraints.
     */
    private <T> void saveSetParameter(final ParameterType<T> parameterType, final T value, final boolean override)
            throws ParameterException
    {
        parameterType.check(value, this);
        parameterType.checkConstraint(value);
        checkCopyOnWrite();
        this.overrides.remove(parameterType);
        this.parameters.put(parameterType, value);
    }

    @Override
    public <T> void setParameterOverride(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        parameterType.check(value, this);
        parameterType.checkConstraint(value);
        this.overrides.put(parameterType, value);
    }

    @Override
    public void removeParameterOverride(final ParameterType<?> parameterType) throws ParameterException
    {
        this.overrides.remove(parameterType);
    }

    /**
     * Copy the internal data if needed.
     */
    private void checkCopyOnWrite()
    {
        if (this.copyOnWrite)
        {
            this.parameters = new IndexedMap<>(this.parameters, ParameterType.maxIndex());
            this.overrides = new IndexedMap<>(this.overrides, ParameterType.maxIndex());
            this.copyOnWrite = false;
        }
    }

    // set methods guarantee matching of parameter type and value
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getParameter(final ParameterType<T> parameterType) throws ParameterException
    {
        T result = (T) this.overrides.get(parameterType);
        if (result == null)
        {
            result = (T) this.parameters.get(parameterType);
            if (result == null)
            {
                throw new ParameterException(
                        "Could not get parameter of type '" + parameterType.getId() + "' as it was not set.");
            }
        }
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
        return new IndexedMap<>(this.parameters, ParameterType.maxIndex());
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
                    // should not happen, we set it accessible
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
        for (Entry<ParameterType<?>, Object> entry : this.parameters.entrySet())
        {
            try
            {
                params.setParameter((ParameterType<T>) entry.getKey(), (T) entry.getValue());
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

}
