package org.opentrafficsim.base.parameters;

import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Parameter type for classes, of which the value may need to be present in a constraint set.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jun. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class, e.g. TacticalPlanner
 */
public class ParameterTypeClass<T> extends ParameterType<Class<? extends T>>
{

    /** */
    private static final long serialVersionUID = 20170630L;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterTypeClass(final String id, final String description, final Class<Class<? extends T>> valueClass)
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
    public ParameterTypeClass(final String id, final String description, final Class<Class<? extends T>> valueClass,
            final Class<? extends T> defaultValue)
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
    public ParameterTypeClass(final String id, final String description, final Class<Class<? extends T>> valueClass,
            final Constraint<? super Class<? extends T>> constraint)
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
    public ParameterTypeClass(final String id, final String description, final Class<Class<? extends T>> valueClass,
            final Class<? extends T> defaultValue, final Constraint<? super Class<? extends T>> constraint)
    {
        super(id, description, valueClass, defaultValue, constraint);
    }

    /**
     * Returns a typed class, where the type is {@code Class<? extends T>}, such that {@code ParameterTypeClass} instances can
     * easily be created.
     * @param clazz class instance
     * @param <T> constraining class in parameter type, e.g. TacticalPlanner
     * @return typed class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<Class<? extends T>> getValueClass(final Class<T> clazz)
    {
        return (Class<Class<? extends T>>) clazz.getClass();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this).getSimpleName();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ParameterTypeClass [" + getConstraint().toString() + "]";
    }

}
