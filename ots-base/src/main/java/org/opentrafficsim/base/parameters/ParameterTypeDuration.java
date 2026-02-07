package org.opentrafficsim.base.parameters;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for Time parameters.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypeDuration extends ParameterTypeNumeric<Duration>
{

    /**
     * Construct a new ParameterTypeDuration without default value and check.
     * @param id short name of the new ParameterTypeDuration
     * @param description parameter description or full name of the new ParameterTypeDuration
     */
    public ParameterTypeDuration(final String id, final String description)
    {
        super(id, description, Duration.class);
    }

    /**
     * Construct a new ParameterTypeDuration with default value, without check.
     * @param id short name of the new ParameterTypeDuration
     * @param description parameter description or full name of the new ParameterTypeDuration
     * @param defaultValue the default value for the new ParameterTypeDuration
     */
    public ParameterTypeDuration(final String id, final String description, final Duration defaultValue)
    {
        super(id, description, Duration.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeDuration without default value, with check.
     * @param id short name of the new ParameterTypeDuration
     * @param description parameter description or full name of the new ParameterTypeDuration
     * @param constraint Constraint for parameter values
     */
    public ParameterTypeDuration(final String id, final String description, final Constraint<? super Duration> constraint)
    {
        super(id, description, Duration.class, constraint);
    }

    /**
     * Construct a new ParameterTypeDuration with default value and check.
     * @param id short name of the new ParameterTypeDuration
     * @param description parameter description or full name of the new ParameterTypeDuration
     * @param defaultValue Default value of the new ParameterTypeDuration
     * @param constraint Constraint for parameter values
     */
    public ParameterTypeDuration(final String id, final String description, final Duration defaultValue,
            final Constraint<? super Duration> constraint)
    {
        super(id, description, Duration.class, defaultValue, constraint);
    }

    @Override
    public final String toString()
    {
        return "ParameterTypeDuration [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
