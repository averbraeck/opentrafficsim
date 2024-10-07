package org.opentrafficsim.base.parameters;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Parameter type for a list of classes, each of which may need to be present in a constraint set.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> class, e.g. TacticalPlanner
 */
public class ParameterTypeClassList<T> extends ParameterType<List<Class<? extends T>>>
{

    /** */
    private static final long serialVersionUID = 20170702L;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterTypeClassList(final String id, final String description, final Class<List<Class<? extends T>>> valueClass)
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
    public ParameterTypeClassList(final String id, final String description, final Class<List<Class<? extends T>>> valueClass,
            final List<Class<? extends T>> defaultValue)
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
    public ParameterTypeClassList(final String id, final String description, final Class<List<Class<? extends T>>> valueClass,
            final Constraint<? super List<Class<? extends T>>> constraint)
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
    public ParameterTypeClassList(final String id, final String description, final Class<List<Class<? extends T>>> valueClass,
            final List<Class<? extends T>> defaultValue, final Constraint<? super List<Class<? extends T>>> constraint)
    {
        super(id, description, valueClass, defaultValue, constraint);
    }

    /**
     * Returns a typed class, where the type is {@code List<Class<? extends T>>}, such that {@code ParameterTypeClass} instances
     * can easily be created.
     * @param clazz class instance
     * @param <T> constraining class in parameter type, e.g. TacticalPlanner
     * @return typed class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<List<Class<? extends T>>> getValueClass(final Class<T> clazz)
    {
        List<Class<? extends T>> list = new ArrayList<>();
        return (Class<List<Class<? extends T>>>) list.getClass();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String printValue(final Parameters parameters) throws ParameterException
    {
        String delimiter = "";
        StringBuilder str = new StringBuilder("[");
        for (Class<? extends T> clazz : parameters.getParameter(this))
        {
            str.append(clazz.getSimpleName());
            str.append(delimiter);
            delimiter = ", ";
        }
        str.append("]");
        return str.toString();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ParameterTypeClassList []";
    }

}
