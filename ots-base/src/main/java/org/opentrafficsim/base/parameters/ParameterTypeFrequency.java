package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for Frequency parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypeFrequency extends ParameterTypeNumeric<Frequency> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Construct a new ParameterTypeFrequency without default value and check.
     * @param id short name of the new ParameterTypeFrequency
     * @param description parameter description or full name of the new ParameterTypeFrequency
     */
    public ParameterTypeFrequency(final String id, final String description)
    {
        super(id, description, Frequency.class);
    }

    /**
     * Construct a new ParameterTypeFrequency with default value, without check.
     * @param id short name of the new ParameterTypeFrequency
     * @param description parameter description or full name of the new ParameterTypeFrequency
     * @param defaultValue the default value of the new ParameterTypeFrequency
     */
    public ParameterTypeFrequency(final String id, final String description, final Frequency defaultValue)
    {
        super(id, description, Frequency.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeFrequency without default value, with check.
     * @param id short name of the new ParameterTypeFrequency
     * @param description parameter description or full name of the new ParameterTypeFrequency
     * @param constraint constraint for parameter values
     */
    public ParameterTypeFrequency(final String id, final String description, final Constraint<? super Frequency> constraint)
    {
        super(id, description, Frequency.class, constraint);
    }

    /**
     * Construct a new ParameterTypeFrequency with default value and check.
     * @param id short name of the new ParameterTypeFrequency
     * @param description parameter description or full name of the new ParameterTypeFrequency
     * @param defaultValue the default value of the new ParameterTypeFrequency
     * @param constraint constraint for parameter values
     */
    public ParameterTypeFrequency(final String id, final String description, final Frequency defaultValue,
            final Constraint<? super Frequency> constraint)
    {
        super(id, description, Frequency.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ParameterTypeFrequency [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
