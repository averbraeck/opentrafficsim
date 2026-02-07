package org.opentrafficsim.base.parameters;

import org.opentrafficsim.base.parameters.constraint.Constraint;
//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wrapper class for parameters of any quantity in JUnits, or double, integer, etc.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @param <T> Class of the value.
 */
public abstract class ParameterTypeNumeric<T extends Number> extends ParameterType<T>
{

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass)
    {
        super(id, description, valueClass);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        super(id, description, valueClass, defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass,
            final Constraint<? super T> constraint)
    {
        super(id, description, valueClass, constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Constraint<? super T> constraint)
    {
        super(id, description, valueClass, defaultValue, constraint);
    }

    @Override
    public String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this).toString();
    }

    @Override
    public String toString()
    {
        return "ParameterTypeNumeric [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
