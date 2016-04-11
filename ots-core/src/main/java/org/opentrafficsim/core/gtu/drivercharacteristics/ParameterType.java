package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * Defines meta-information of a parameter, defining the parameter uniquely.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Wouter Schakel
 * @param <U> Unit of the value.
 * @param <T> Class of the value.
 */
public class ParameterType<U extends Unit<U>, T extends DoubleScalar.Rel<U>> extends AbstractParameterType<U, T>
{

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass)
    {
        super(id, description, valueClass, null, null);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        super(id, description, valueClass, defaultValue, null);
        try
        {
            // Forward empty set of parameters. At creation time of parameter types, values cannot be checked with values of
            // other parameter types.
            check(defaultValue, new BehavioralCharacteristics());
        }
        catch (ParameterException pe)
        {
            throw new RuntimeException("Default value of parameter '" + getId()
                + "' does not comply with custom constraints.", pe);
        }

    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param check Check for parameter values.
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass, final Check check)
    {
        super(id, description, valueClass, null, check);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param check Check for parameter values.
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
        final Check check)
    {
        super(id, description, valueClass, defaultValue, check);
    }

    /**
     * Returns the class of the value.
     * @return valueClass Class of the value.
     */
    public final Class<T> getValueClass()
    {
        return super.valueClass;
    }

    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @param bc Set of behavioral characteristics.
     * @throws ParameterException If the value does not comply with constraints.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void check(final T value, final BehavioralCharacteristics bc) throws ParameterException
    {
        //
    }

    /**
     * Returns the default value.
     * @return defaultValue Default value.
     * @throws ParameterException If no default value was set.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public T getDefaultValue() throws ParameterException
    {
        ParameterException.throwIf(null == this.defaultValue, "No default value was set for " + getId());
        return this.defaultValue;
    }

    /** {@inheritDoc} */
    public final String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        return behavioralCharacteristics.getParameter(this).toString();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ParameterType [id=" + getId() + ", description=" + getDescription() + ", valueClass=" + this.valueClass
            + "]";
    }

}
