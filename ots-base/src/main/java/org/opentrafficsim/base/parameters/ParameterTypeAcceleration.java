package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for Acceleration parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypeAcceleration extends ParameterTypeNumeric<Acceleration> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Construct a new ParameterTypeAcceleration without default value and without check.
     * @param id String; short name of the new ParameterTypeAcceleration
     * @param description String; parameter description or full name of the new ParameterTypeAcceleration
     */
    public ParameterTypeAcceleration(final String id, final String description)
    {
        super(id, description, Acceleration.class);
    }

    /**
     * Construct a new ParameterTypeAcceleration with default value, without check.
     * @param id String; short name of the new ParameterTypeAcceleration
     * @param description String; parameter description or full name of the new ParameterTypeAcceleration
     * @param defaultValue Acceleration; default value of the new ParameterTypeAcceleration
     */
    public ParameterTypeAcceleration(final String id, final String description, final Acceleration defaultValue)
    {
        super(id, description, Acceleration.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeAcceleration without default value, with check.
     * @param id String; short name of the new ParameterTypeAcceleration
     * @param description String; parameter description or full name of the new ParameterTypeAcceleration
     * @param constraint Constraint&lt;? super Acceleration&gt;; constraint for parameter values
     */
    public ParameterTypeAcceleration(final String id, final String description,
            final Constraint<? super Acceleration> constraint)
    {
        super(id, description, Acceleration.class, constraint);
    }

    /**
     * Construct a new ParameterTypeAcceleration with default value and check.
     * @param id String; short name of the new ParameterTypeAcceleration
     * @param description String; parameter description or full name of the new ParameterTypeAcceleration
     * @param defaultValue Acceleration; default value of the new ParameterTypeAcceleration
     * @param constraint Constraint&lt;? super Acceleration&gt;; Constraint&lt;? super Acceleration&gt; constraint for parameter
     *            values
     */
    public ParameterTypeAcceleration(final String id, final String description, final Acceleration defaultValue,
            final Constraint<? super Acceleration> constraint)
    {
        super(id, description, Acceleration.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ParameterTypeAcceleration [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
