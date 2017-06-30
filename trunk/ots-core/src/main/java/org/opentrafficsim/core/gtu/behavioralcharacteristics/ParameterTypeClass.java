package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.util.Set;

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
            this.classes = classes;
        }

        /** {@inheritDoc} */
        @Override
        public boolean fails(final Class<? extends T> value)
        {
            return !this.classes.contains(value);
        }

        /** {@inheritDoc} */
        @Override
        public String failMessage()
        {
            return "Value of parameter '%s' is not in the set of acceptable values.";
        }

    }

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeClass(final String id, final String description)
    {
        this(id, description, null, null, false);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeClass(final String id, final String description, final Class<? extends T> defaultValue)
    {
        this(id, description, defaultValue, null, true);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeClass(final String id, final String description, final ClassConstraint<? super T> constraint)
    {
        this(id, description, null, constraint, false);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeClass(final String id, final String description, final Class<? extends T> defaultValue,
            final ClassConstraint<? super T> constraint)
    {
        this(id, description, defaultValue, constraint, true);
    }

    /**
     * Private constructor with default value and check, which may check the default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     * @param hasDefaultValue Whether to check the default value for null.
     */
    @SuppressWarnings("unchecked")
    private ParameterTypeClass(final String id, final String description, final Class<? extends T> defaultValue,
            final ClassConstraint<? super T> constraint, final boolean hasDefaultValue)
    {
        super(id, description, (Class<Class<? extends T>>) defaultValue.getClass(), hasDefaultValue ? defaultValue : null,
                constraint, hasDefaultValue);
    }

    /** {@inheritDoc} */
    @Override
    public String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        return behavioralCharacteristics.getParameter(this).getSimpleName();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ParameterTypeClass []";
    }

}
