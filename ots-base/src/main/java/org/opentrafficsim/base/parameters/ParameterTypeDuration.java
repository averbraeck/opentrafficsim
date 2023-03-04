package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for Time parameters.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeDuration extends ParameterTypeNumeric<Duration> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150400L;

    /**
     * Construct a new ParameterTypeDuration without default value and check.
     * @param id String; short name of the new ParameterTypeDuration
     * @param description String; parameter description or full name of the new ParameterTypeDuration
     */
    public ParameterTypeDuration(final String id, final String description)
    {
        super(id, description, Duration.class);
    }

    /**
     * Construct a new ParameterTypeDuration with default value, without check.
     * @param id String; short name of the new ParameterTypeDuration
     * @param description String; parameter description or full name of the new ParameterTypeDuration
     * @param defaultValue Duration; the default value for the new ParameterTypeDuration
     */
    public ParameterTypeDuration(final String id, final String description, final Duration defaultValue)
    {
        super(id, description, Duration.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeDuration without default value, with check.
     * @param id String; short name of the new ParameterTypeDuration
     * @param description String; parameter description or full name of the new ParameterTypeDuration
     * @param constraint Constraint&lt;? super Duration&gt;; Constraint for parameter values
     */
    public ParameterTypeDuration(final String id, final String description, final Constraint<? super Duration> constraint)
    {
        super(id, description, Duration.class, constraint);
    }

    /**
     * Construct a new ParameterTypeDuration with default value and check.
     * @param id String; short name of the new ParameterTypeDuration
     * @param description String; parameter description or full name of the new ParameterTypeDuration
     * @param defaultValue Duration; Default value of the new ParameterTypeDuration
     * @param constraint Constraint&lt;? super Duration&gt;; Constraint for parameter values
     */
    public ParameterTypeDuration(final String id, final String description, final Duration defaultValue,
            final Constraint<? super Duration> constraint)
    {
        super(id, description, Duration.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ParameterTypeDuration [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
