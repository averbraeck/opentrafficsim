package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.Throw;

/**
 * Wrapper class for parameters of any quantity in JUnits.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <U> Unit of the value.
 * @param <T> Class of the value.
 */
public class ParameterType<U extends Unit<U>, T extends DoubleScalar.Rel<U>> extends AbstractParameterType<U, T> implements
        Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass)
    {
        this(id, description, valueClass, null, null, false);
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
        this(id, description, valueClass, defaultValue, null, true);
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
        this(id, description, valueClass, null, check, false);
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
        this(id, description, valueClass, defaultValue, check, true);
    }

    /**
     * Private constructor with default value and check, which may check the default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param check Check for parameter values.
     * @param hasDefaultValue Whether to check the default value for null.
     */
    private ParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Check check, final boolean hasDefaultValue)
    {
        super(id, description, valueClass, defaultValue, check, hasDefaultValue);
        try
        {
            // Forward empty set of parameters. At creation time of parameter types, values cannot be checked with values of
            // other parameter types.
            check(defaultValue, new BehavioralCharacteristics());
        }
        catch (ParameterException pe)
        {
            throw new RuntimeException("Default value of parameter '" + getId() + "' does not comply with custom constraints.",
                    pe);
        }
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
    public void check(final T value, final BehavioralCharacteristics bc) throws ParameterException
    {
        //
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    public T getDefaultValue() throws ParameterException
    {
        Throw.when(null == this.defaultValue, ParameterException.class, "No default value was set for '%s'.", getId());
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
        return "ParameterType [id=" + getId() + ", description=" + getDescription() + ", valueClass=" + this.valueClass + "]";
    }

}
