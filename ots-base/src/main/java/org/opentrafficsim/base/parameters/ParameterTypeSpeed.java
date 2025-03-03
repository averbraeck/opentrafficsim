package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for Speed parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypeSpeed extends ParameterTypeNumeric<Speed> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Construct a new ParameterTypeSpeed without default value and check.
     * @param id short name of the new ParameterTypeSpeed
     * @param description parameter description or full name of the new ParameterTypeSpeed
     */
    public ParameterTypeSpeed(final String id, final String description)
    {
        super(id, description, Speed.class);
    }

    /**
     * Construct a new ParameterTypeSpeed with default value, without check.
     * @param id short name of the new ParameterTypeSpeed
     * @param description parameter description or full name of the new ParameterTypeSpeed
     * @param defaultValue default value of the new ParameterTypeSpeed
     */
    public ParameterTypeSpeed(final String id, final String description, final Speed defaultValue)
    {
        super(id, description, Speed.class, defaultValue);
    }

    /**
     * Constructor a new ParameterTypeSpeed without default value, with check.
     * @param id short name of the new ParameterTypeSpeed
     * @param description parameter description or full name of the new ParameterTypeSpeed
     * @param constraint constraint for parameter values
     */
    public ParameterTypeSpeed(final String id, final String description, final Constraint<? super Speed> constraint)
    {
        super(id, description, Speed.class, constraint);
    }

    /**
     * Constructor a new ParameterTypeSpeed with default value and check.
     * @param id short name of the new ParameterTypeSpeed
     * @param description parameter description or full name of the new ParameterTypeSpeed
     * @param defaultValue default value of the new ParameterTypeSpeed
     * @param constraint constraint for parameter values
     */
    public ParameterTypeSpeed(final String id, final String description, final Speed defaultValue,
            final Constraint<? super Speed> constraint)
    {
        super(id, description, Speed.class, defaultValue, constraint);
    }

    @Override
    public final String toString()
    {
        return "ParameterTypeSpeed [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
