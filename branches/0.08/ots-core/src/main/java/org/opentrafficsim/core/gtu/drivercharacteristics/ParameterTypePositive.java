package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * Extends ParameterType with a default check for a positive and non-zero value.
 * @author Wouter Schakel
 * @param <T> The parameter type
 */
public class ParameterTypePositive<T extends DoubleScalar.Rel<?>> extends ParameterType<T>
{

    /**
     * Constructor with default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public ParameterTypePositive(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        super(id, description, valueClass, checkPositive(id, defaultValue));
        try
        {
            check(defaultValue);
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Default value does not comply with constraints.", exception);
        }
    }

    /**
     * Checks whether the default value is positive.
     * @param id Short name of parameter.
     * @param value Default value.
     * @param <T> The parameter type
     * @return Original value.
     */
    private static <T extends DoubleScalar.Rel<?>> T checkPositive(final String id, final T value)
    {
        if (value.si <= 0)
        {
            throw new RuntimeException("Default value of parameter of type " + id + " is not positive.");
        }
        return value;
    }

    /**
     * Constructor without default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterTypePositive(final String id, final String description, final Class<T> valueClass)
    {
        super(id, description, valueClass);
    }

    /**
     * Check if value is positive and non-zero.
     * @param value Value to check with constraints.
     * @throws ParameterException If the value does not comply with constraints.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void check(final T value) throws ParameterException
    {
        ParameterException.failIf(value.si <= 0, "Value of positive parameter type '" + this.getId()
            + "' is negative or zero.");
    }
    
    /**
     * Check if value is positive and non-zero.
     * @param value Value to check with constraints.
     * @param bc Set of behavioral characteristics.
     * @throws ParameterException If the value does not comply with constraints.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void check(final T value, final BehavioralCharacteristics bc) throws ParameterException
    {
        //
    }

}
