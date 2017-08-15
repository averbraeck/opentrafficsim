package org.opentrafficsim.base.parameters;

import java.util.Set;

import nl.tudelft.simulation.language.Throw;

import org.opentrafficsim.base.OTSClassUtil;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jun. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> class, e.g. TacticalPlanner
 */
public class ParameterTypeClass<T> extends AbstractParameterType<Class<? extends T>>
{

    /** */
    private static final long serialVersionUID = 20170630L;

    /**
     * Constraint that checks whether the value is any of a given set.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jun. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> class
     */
    public static class ClassConstraint<T> implements Constraint<Class<? extends T>>
    {

        /** Acceptable classes. */
        private final Set<Class<? extends T>> classes;

        /**
         * @param classes acceptable classes
         */
        public ClassConstraint(final Set<Class<? extends T>> classes)
        {
            Throw.whenNull(classes, "Set of classes may not be null.");
            this.classes = classes;
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public boolean fails(final Class<? extends T> value)
        {
            return !this.classes.contains(value);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public String failMessage()
        {
            return "Value of parameter '%s' is not in the set of acceptable values.";
        }

        /**
         * Creates a new instance with given set.
         * @param type type class
         * @param objs acceptable classes
         * @param <T> type class
         * @return new instance with given set
         */
        @SafeVarargs
        public static <T> ClassConstraint<T> newInstance(final Class<T> type, final Class<? extends T>... objs)
        {
            return new ClassConstraint<>(OTSClassUtil.toTypedSet(type, objs));
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ClassConstraint [classes=" + this.classes + "]";
        }

    }

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
            final Constraint<Class<? extends T>> constraint)
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
            final Class<? extends T> defaultValue, final Constraint<Class<? extends T>> constraint)
    {
        super(id, description, valueClass, defaultValue, constraint);
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
        return "ParameterTypeClass []";
    }

}
