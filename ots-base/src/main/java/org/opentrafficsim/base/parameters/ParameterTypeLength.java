package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for Length parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypeLength extends ParameterTypeNumeric<Length> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor without default value and check.
     * @param id String; short name of the new ParameterTypeLength
     * @param description String; parameter description or full name of the new ParameterTypeLength
     */
    public ParameterTypeLength(final String id, final String description)
    {
        super(id, description, Length.class);
    }

    /**
     * Construct a new ParameterTypeLength with default value, without check.
     * @param id String; short name of the new ParameterTypeLength
     * @param description String; parameter description or full name of the new ParameterTypeLength
     * @param defaultValue Length; the default value of the new ParameterTypeLength
     */
    public ParameterTypeLength(final String id, final String description, final Length defaultValue)
    {
        super(id, description, Length.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeLength without default value, with check.
     * @param id String; short name of the new ParameterTypeLength
     * @param description String; parameter description or full name of the new ParameterTypeLength
     * @param constraint Constraint&lt;? super Length&gt;; constraint for parameter values
     */
    public ParameterTypeLength(final String id, final String description, final Constraint<? super Length> constraint)
    {
        super(id, description, Length.class, constraint);
    }

    /**
     * Construct a new ParameterTypeLength with default value and check.
     * @param id String; short name of the new ParameterTypeLength
     * @param description String; parameter description or full name of the new ParameterTypeLength
     * @param defaultValue Length; the default value of the new ParameterTypeLength
     * @param constraint Constraint&lt;? super Length&gt;; constraint for parameter values
     */
    public ParameterTypeLength(final String id, final String description, final Length defaultValue,
            final Constraint<? super Length> constraint)
    {
        super(id, description, Length.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ParameterTypeLength [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
