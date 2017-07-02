package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.util.List;
import java.util.Set;

import nl.tudelft.simulation.language.Throw;

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
public class ParameterTypeClassList<T> extends AbstractParameterType<List<Class<? extends T>>>
{

    /** */
    private static final long serialVersionUID = 20170702L;

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
    public static class ClassListConstraint<T> implements Constraint<List<Class<? extends T>>>
    {

        /** Acceptable classes. */
        private final Set<Class<? extends T>> classes;

        /**
         * @param classes acceptable classes
         */
        public ClassListConstraint(final Set<Class<? extends T>> classes)
        {
            Throw.whenNull(classes, "Set of classes may not be null.");
            this.classes = classes;
        }

        /** {@inheritDoc} */
        @Override
        public boolean fails(final List<Class<? extends T>> value)
        {
            for (Class<? extends T> clazz : value)
            {
                if (!this.classes.contains(clazz))
                {
                    return true;
                }
            }
            return false;
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
            final ClassListConstraint<T> constraint)
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
            final List<Class<? extends T>> defaultValue, final ClassListConstraint<T> constraint)
    {
        super(id, description, valueClass, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        String delimiter = "";
        StringBuilder str = new StringBuilder("[");
        for (Class<? extends T> clazz : behavioralCharacteristics.getParameter(this))
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
    public String toString()
    {
        return "ParameterTypeClass []";
    }

}
