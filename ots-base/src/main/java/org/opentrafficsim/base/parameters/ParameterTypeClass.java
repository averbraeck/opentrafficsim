package org.opentrafficsim.base.parameters;

import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Parameter type for classes, of which the value may need to be present in a constraint set.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> class, e.g. TacticalPlanner
 */
public class ParameterTypeClass<T> extends ParameterType<Class<? extends T>>
{

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterTypeClass(final String id, final String description, final Class<T> valueClass)
    {
        super(id, description, getTypedClass(valueClass));
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public ParameterTypeClass(final String id, final String description, final Class<T> valueClass,
            final Class<? extends T> defaultValue)
    {
        super(id, description, getTypedClass(valueClass), defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeClass(final String id, final String description, final Class<T> valueClass,
            final Constraint<? super Class<? extends T>> constraint)
    {
        super(id, description, getTypedClass(valueClass), constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeClass(final String id, final String description, final Class<T> valueClass,
            final Class<? extends T> defaultValue, final Constraint<? super Class<? extends T>> constraint)
    {
        super(id, description, getTypedClass(valueClass), defaultValue, constraint);
    }

    /**
     * @param object the object to provide the class for
     * @param <T> the type
     * @return the class of the object
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getTypedClass(final T object)
    {
        return (Class<T>) object.getClass();
    }

    @Override
    public String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this).getSimpleName();
    }

    @Override
    public String toString()
    {
        return "ParameterTypeClass [" + getConstraint().toString() + "]";
    }

}
