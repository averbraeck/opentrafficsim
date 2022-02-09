package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.constraint.Constraint;
//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wrapper class for parameters of any quantity in JUnits, or double, integer, etc.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> Class of the value.
 */
public abstract class ParameterTypeNumeric<T extends Number> extends ParameterType<T> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor without default value and check.
     * @param id String; Short name of parameter.
     * @param description String; Parameter description or full name.
     * @param valueClass Class&lt;T&gt;; Class of the value.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass)
    {
        super(id, description, valueClass);
    }

    /**
     * Constructor with default value, without check.
     * @param id String; Short name of parameter.
     * @param description String; Parameter description or full name.
     * @param valueClass Class&lt;T&gt;; Class of the value.
     * @param defaultValue T; Default value.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        super(id, description, valueClass, defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id String; Short name of parameter.
     * @param description String; Parameter description or full name.
     * @param valueClass Class&lt;T&gt;; Class of the value.
     * @param constraint Constraint&lt;? super T&gt;; Constraint for parameter values.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass,
            final Constraint<? super T> constraint)
    {
        super(id, description, valueClass, constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id String; Short name of parameter.
     * @param description String; Parameter description or full name.
     * @param valueClass Class&lt;T&gt;; Class of the value.
     * @param defaultValue T; Default value.
     * @param constraint Constraint&lt;? super T&gt;; Constraint for parameter values.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Constraint<? super T> constraint)
    {
        super(id, description, valueClass, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this).toString();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ParameterTypeNumeric [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
