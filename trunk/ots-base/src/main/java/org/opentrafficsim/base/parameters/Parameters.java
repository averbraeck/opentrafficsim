package org.opentrafficsim.base.parameters;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;

/**
 * Storage for a set of parameters with one level undo (set/reset).
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Parameters implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /** Object to recognize that no value was set previously. */
    private static final Empty EMPTY = new Empty();

    /** Whether to copy internal data on write. */
    private boolean copyOnWrite = false;

    /** List of parameters. */
    private Map<ParameterType<?>, Object> parameters;

    /** List of parameters with values before last set. */
    private Map<ParameterType<?>, Object> previous;

    /**
     * Construct a new, empty Parameters set.
     */
    public Parameters()
    {
        this.parameters = new HashMap<>();
        this.previous = new HashMap<>();
    }

    /**
     * Constructor which creates a copy of the input set.
     * @param parameters Parameters; input set to copy into the new Parameters object
     */
    public Parameters(final Parameters parameters)
    {
        this.parameters = parameters.parameters;
        this.previous = parameters.previous;
        this.copyOnWrite = true;
        parameters.copyOnWrite = true;
    }

    /**
     * Set parameter value of given parameter type.
     * @param parameterType AbstractParameterType&lt;T&gt;; the parameter type.
     * @param value T; new value for the parameter of type <code>parameterType</code>.
     * @param <T> Class of value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    public final <T> void setParameter(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        Throw.when(value == null, ParameterException.class,
                "Parameter of type '%s' was assigned a null value, this is not allowed.", parameterType.getId());
        parameterType.check(value, this);
        saveSetParameter(parameterType, value);
    }

    /**
     * Remember the current value for recovery on reset.
     * @param parameterType AbstractParameterType&lt;T&gt;; the parameter type
     * @param value T; new value for the parameter
     * @param <T> Class of the value
     * @throws ParameterException If the value does not comply with constraints.
     */
    private <T> void saveSetParameter(final ParameterType<T> parameterType, final T value) throws ParameterException
    {
        parameterType.checkConstraint(value);
        checkCopyOnWrite();
        if (this.parameters.containsKey(parameterType))
        {
            this.previous.put(parameterType, this.parameters.get(parameterType));
        }
        else
        {
            // remember that there was no value before this set
            this.previous.put(parameterType, EMPTY);
        }
        this.parameters.put(parameterType, value);
    }

    /**
     * Resets the parameter value to the value from before the last set. This goes only a single value back.
     * @param parameterType AbstractParameterType&lt;T&gt;; the parameter type.
     * @throws ParameterException If the parameter was never set.
     */
    public final void resetParameter(final ParameterType<?> parameterType) throws ParameterException
    {
        Throw.when(!this.previous.containsKey(parameterType), ParameterException.class,
                "Reset on parameter of type '%s' could not be performed, it was not set.", parameterType.getId());
        checkCopyOnWrite();
        if (this.previous.get(parameterType) instanceof Empty)
        {
            // no value was set before last set, so make parameter type not set
            this.parameters.remove(parameterType);
        }
        else
        {
            this.parameters.put(parameterType, this.previous.get(parameterType));
        }
        this.previous.remove(parameterType); // prevent consecutive resets
    }

    /**
     * Copy the internal data if needed.
     */
    private void checkCopyOnWrite()
    {
        if (this.copyOnWrite)
        {
            this.parameters = new HashMap<>(this.parameters);
            this.previous = new HashMap<>(this.previous);
            this.copyOnWrite = false;
        }
    }

    /**
     * Get parameter of given type.
     * @param parameterType AbstractParameterType&lt;T&gt;; the parameter type.
     * @param <T> Class of value.
     * @return T; parameter of the requested type if it exists
     * @throws ParameterException If the parameter was never set.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public <T> T getParameter(final ParameterType<T> parameterType) throws ParameterException
    {
        checkContains(parameterType);
        @SuppressWarnings("unchecked")
        // set methods guarantee matching of parameter type and value
        T result = (T) this.parameters.get(parameterType);
        return result;
    }

    /**
     * Check whether parameter has been set.
     * @param parameterType Parameter type.
     * @throws ParameterException If parameter is not present.
     */
    private void checkContains(final ParameterType<?> parameterType) throws ParameterException
    {
        Throw.when(!contains(parameterType), ParameterException.class,
                "Could not get parameter of type '%s' as it was not set.", parameterType.getId());
    }

    /**
     * Indicate whether the given parameter type has been set.
     * @param parameterType AbstractParameterType&lt;T&gt;; the parameter type to check
     * @return boolean; true if <code>parameterType</code> has been set; false if <code>parameterType</code> has not been set
     */
    public final boolean contains(final ParameterType<?> parameterType)
    {
        return this.parameters.containsKey(parameterType);
    }

    /**
     * Returns a safe copy of the parameters.
     * @return Map&lt;AbstractParameterType&lt;?&gt;&gt;; a safe copy of the parameters, e.g., for printing
     */
    public final Map<ParameterType<?>, Object> getParameters()
    {
        return new HashMap<>(this.parameters);
    }

    /**
     * Sets the default value of a parameter.
     * @param parameter AbstractParameterType&lt;T&gt;; the parameter to set the default value of
     * @param <T> Class of the value
     * @return Parameters; this set of parameters (for method chaining)
     * @throws ParameterException if the parameter type has no default value
     */
    public final <T> Parameters setDefaultParameter(final ParameterType<T> parameter) throws ParameterException
    {
        T defaultValue = parameter.getDefaultValue();
        try
        {
            saveSetParameter(parameter, defaultValue);
        }
        catch (ParameterException pe)
        {
            // should not happen, default value and parameter type are connected
            throw new RuntimeException(pe);
        }
        return this;
    }

    /**
     * Sets the default values of all accessible parameters defined in the given class.<br>
     * TODO Determin if this method should call checkCopyOnWrite and/or backup the current values in this.previous
     * @param clazz Class&lt;?&gt;; class with parameters
     * @return Parameters; this set of parameters (for method chaining)
     */
    public final Parameters setDefaultParameters(final Class<?> clazz)
    {
        return setDefaultParametersLocal(clazz);
    }

    /**
     * Sets the default values of all accessible parameters defined in the given class.
     * @param clazz Class&lt;?&gt;; class with parameters
     * @param <T> Class of the value
     * @return this set of parameters (for method chaining)
     */
    @SuppressWarnings("unchecked")
    private <T> Parameters setDefaultParametersLocal(final Class<?> clazz)
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
                    T defaultValue = p.getDefaultValue();
                    saveSetParameter(p, defaultValue);
                }
                catch (IllegalArgumentException iare)
                {
                    // should not happen, field and clazz are related
                    throw new RuntimeException(iare);
                }
                catch (IllegalAccessException iace)
                {
                    // parameter type not public
                    throw new RuntimeException(iace);
                }
                catch (ParameterException pe)
                {
                    // do not set parameter without default value
                    throw new RuntimeException(pe);
                }
            }
        }
        return this;
    }

    /**
     * Sets all parameters from the given set in this set. <br>
     * TODO Determine whether this method should call checkCopyOnWrite and/or make backups in this.previous
     * @param referenceSet Parameters; set of parameters to set in this set
     */
    public final void setAll(final Parameters referenceSet)
    {
        for (ParameterType<?> key : referenceSet.parameters.keySet())
        {
            this.parameters.put(key, referenceSet.parameters.get(key));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
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
                throw new RuntimeException(pe);
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
    }

}
