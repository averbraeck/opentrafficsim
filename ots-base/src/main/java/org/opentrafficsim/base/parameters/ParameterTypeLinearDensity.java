package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for LinearDensity parameters.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeLinearDensity extends ParameterTypeNumeric<LinearDensity> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150000L;

    /**
     * Construct a new ParameterTypeLinearDensity without default value and check.
     * @param id String; short name of the new ParameterTypeLinearDensity
     * @param description String; parameter description or full name of the new ParameterTypeLinearDensity
     */
    public ParameterTypeLinearDensity(final String id, final String description)
    {
        super(id, description, LinearDensity.class);
    }

    /**
     * Construct a new ParameterTypeLinearDensity with default value, without check.
     * @param id String; short name of the new ParameterTypeLinearDensity
     * @param description String; parameter description or full name of the new ParameterTypeLinearDensity
     * @param defaultValue LinearDensity; the default value of the new ParameterTypeLinearDensity
     */
    public ParameterTypeLinearDensity(final String id, final String description, final LinearDensity defaultValue)
    {
        super(id, description, LinearDensity.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeLinearDensity without default value, with check.
     * @param id String; short name of the new ParameterTypeLinearDensity
     * @param description String; parameter description or full name of the new ParameterTypeLinearDensity
     * @param constraint Constraint&lt;? super LinearDensity&gt;; constraint for parameter values
     */
    public ParameterTypeLinearDensity(final String id, final String description,
            final Constraint<? super LinearDensity> constraint)
    {
        super(id, description, LinearDensity.class, constraint);
    }

    /**
     * Construct a new ParameterTypeLinearDensity with default value and check.
     * @param id String; short name of the new ParameterTypeLinearDensity
     * @param description String; parameter description or full name of the new ParameterTypeLinearDensity
     * @param defaultValue LinearDensity; the default value of the new ParameterTypeLinearDensity
     * @param constraint Constraint&lt;? super LinearDensity&gt;; constraint for parameter values
     */
    public ParameterTypeLinearDensity(final String id, final String description, final LinearDensity defaultValue,
            final Constraint<? super LinearDensity> constraint)
    {
        super(id, description, LinearDensity.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ParameterTypeLinearDensity [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
