package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.DoubleScalarInterface;
import org.opentrafficsim.core.Throw;

import nl.tudelft.simulation.language.reflection.ClassUtil;

/**
 * In this class a set of behavioral characteristics in the form of parameters can be stored for use in behavioral models.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class BehavioralCharacteristics implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Object to recognize that no value was set previously.
     */
    private static final Empty EMPTY = new Empty();

    /** List of parameters. */
    private final Map<AbstractParameterType<?>, DoubleScalarInterface> parameters = new HashMap<>();

    /** List of parameters with values before last set. */
    private final Map<AbstractParameterType<?>, DoubleScalarInterface> previous = new HashMap<>();

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     * @param <T> Class of value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    public final <T extends DoubleScalarInterface> void setParameter(final ParameterType<T> parameterType,
            final T value) throws ParameterException
    {
        Throw.when(value == null, ParameterException.class,
                "Parameter of type '%s' was assigned a null value, this is not allowed.", parameterType.getId());
        parameterType.check(value, this);
        saveSetParameter(parameterType, value);
    }

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     */
    public final void setParameter(final ParameterTypeBoolean parameterType, final boolean value)
    {
        try
        {
            saveSetParameter(parameterType, new Dimensionless(value ? 1.0 : 0.0, DimensionlessUnit.SI));
        }
        catch (ParameterException pe)
        {
            // This cannot occur as the ParameterTypeBoolean constructor does not allow a default check.
            throw new RuntimeException("ParameterTypeBoolean default check throws a ParameterException.", pe);
        }
    }

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    public final void setParameter(final ParameterTypeDouble parameterType, final double value) throws ParameterException
    {
        parameterType.check(value, this);
        saveSetParameter(parameterType, new Dimensionless(value, DimensionlessUnit.SI));
    }

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    public final void setParameter(final ParameterTypeInteger parameterType, final int value) throws ParameterException
    {
        parameterType.check(value, this);
        saveSetParameter(parameterType, new Dimensionless(value, DimensionlessUnit.SI));
    }

    /**
     * Remembers the current value, or if it is not given, for possible reset.
     * @param parameterType Parameter type.
     * @param value Value.
     * @param <T> Class of the value.
     * @throws ParameterException If the value does not comply with constraints.
     */
    private <T extends DoubleScalarInterface> void saveSetParameter(
            final AbstractParameterType<T> parameterType, final T value) throws ParameterException
    {
        parameterType.checkCheck(value);
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
     * @param parameterType Parameter type.
     * @throws ParameterException If the parameter was never set.
     */
    public final void resetParameter(final AbstractParameterType<?> parameterType) throws ParameterException
    {
        Throw.when(!this.previous.containsKey(parameterType), ParameterException.class,
                "Reset on parameter of type '%s' could not be performed, it was not set.", parameterType.getId());
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
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @param <T> Class of value.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public <T extends DoubleScalarInterface> T getParameter(final ParameterType<T> parameterType)
            throws ParameterException
    {
        checkContains(parameterType);
        @SuppressWarnings("unchecked")
        // set methods guarantee matching of parameter type and value
        T result = (T) this.parameters.get(parameterType);
        return result;
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final boolean getParameter(final ParameterTypeBoolean parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return this.parameters.get(parameterType).getSI() != 0.0;
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final int getParameter(final ParameterTypeInteger parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return (int) this.parameters.get(parameterType).getSI();
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final double getParameter(final ParameterTypeDouble parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return this.parameters.get(parameterType).getSI();
    }

    /**
     * Check whether parameter has been set.
     * @param parameterType Parameter type.
     * @throws ParameterException If parameter is not present.
     */
    private void checkContains(final AbstractParameterType<?> parameterType) throws ParameterException
    {
        Throw.when(!contains(parameterType), ParameterException.class,
                "Could not get parameter of type '%s' as it was not set.", parameterType.getId());
    }

    /**
     * Whether the given parameter type has been set.
     * @param parameterType Parameter type.
     * @return Whether the given parameter type has been set.
     */
    public final boolean contains(final AbstractParameterType<?> parameterType)
    {
        return this.parameters.containsKey(parameterType);
    }

    /**
     * Returns a safe copy of the parameters.
     * @return Safe copy of the parameters, e.g., for printing.
     */
    public final Map<AbstractParameterType<?>, DoubleScalarInterface> getParameters()
    {
        return new HashMap<>(this.parameters);
    }

    /**
     * Sets the default value of a parameter.
     * @param parameter parameter to set the default value of
     * @param <T> Class of the value.
     * @return this set of behavioral characteristics (for method chaining)
     * @throws ParameterException if the parameter type has no default value
     */
    @SuppressWarnings("unchecked")
    public final <T extends DoubleScalarInterface> BehavioralCharacteristics setDefaultParameter(
            final AbstractParameterType<T> parameter) throws ParameterException
    {
        T defaultValue;
        if (parameter.getDefaultValue() instanceof DoubleScalarInterface)
        {
            // all types based on DJUNITS
            defaultValue = (T) parameter.getDefaultValue();
        }
        else if (parameter.getDefaultValue() instanceof Boolean)
        {
            // boolean
            defaultValue = (T) new Dimensionless((boolean) parameter.getDefaultValue() ? 1.0 : 0.0, DimensionlessUnit.SI);
        }
        else
        {
            // double or integer
            defaultValue = (T) new Dimensionless(((Number) parameter.getDefaultValue()).doubleValue(), DimensionlessUnit.SI);
        }
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
     * Sets the default values of all accessible parameters defined in the given class.
     * @param clazz class with parameters
     * @return this set of behavioral characteristics (for method chaining)
     */
    public final BehavioralCharacteristics setDefaultParameters(final Class<?> clazz)
    {
        return setDefaultParametersLocal(clazz);
    }

    /**
     * Sets the default values of all accessible parameters defined in the given class.
     * @param clazz class with parameters
     * @param <T> Class of the value.
     * @return this set of behavioral characteristics (for method chaining)
     */
    @SuppressWarnings("unchecked")
    private <T extends DoubleScalarInterface> BehavioralCharacteristics setDefaultParametersLocal(
            final Class<?> clazz)
    {
        // set all default values using reflection
        Set<Field> fields = ClassUtil.getAllFields(clazz);

        for (Field field : fields)
        {
            if (AbstractParameterType.class.isAssignableFrom(field.getType()))
            {
                try
                {
                    field.setAccessible(true);
                    AbstractParameterType<T> p = (AbstractParameterType<T>) field.get(clazz);
                    T defaultValue;
                    if (p.getDefaultValue() instanceof DoubleScalarInterface)
                    {
                        // all types based on DJUNITS
                        defaultValue = (T) p.getDefaultValue();
                    }
                    else if (p.getDefaultValue() instanceof Boolean)
                    {
                        // boolean
                        defaultValue = (T) new Dimensionless((boolean) p.getDefaultValue() ? 1.0 : 0.0, DimensionlessUnit.SI);
                    }
                    else
                    {
                        // double or integer
                        defaultValue =
                                (T) new Dimensionless(((Number) p.getDefaultValue()).doubleValue(), DimensionlessUnit.SI);
                    }
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
                }
                catch (ParameterException pe)
                {
                    // do not set parameter without default value
                }
            }
        }

        return this;
    }

    /**
     * Sets all behavioral characteristics from the given set in this set.
     * @param behavioralCharacteristics set of behavioral characteristics to include in this set
     */
    public final void setAll(final BehavioralCharacteristics behavioralCharacteristics)
    {
        for (AbstractParameterType<?> key : behavioralCharacteristics.parameters.keySet())
        {
            this.parameters.put(key, behavioralCharacteristics.parameters.get(key));
        }
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        StringBuilder out = new StringBuilder("BehavioralCharacteristics [");
        String sep = "";
        for (AbstractParameterType<?> apt : this.parameters.keySet())
        {
            try
            {
                out.append(sep).append(apt.getId()).append("=").append(apt.printValue(this));
                sep = ", ";
            }
            catch (ParameterException pe)
            {
                // We know the parameter has been set as we get the keySet from parameters
            }
        }
        out.append("]");
        return out.toString();
    }

    /**
     * Class to put in a HashMap to recognize that no value was set at some point.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 14, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class Empty extends Dimensionless
    {
        /** */
        private static final long serialVersionUID = 20160414L;

        /**
         * Empty constructor.
         */
        Empty()
        {
            super(Double.NaN, DimensionlessUnit.SI);
        }
    }

}
