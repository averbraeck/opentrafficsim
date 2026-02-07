package org.opentrafficsim.base.parameters;

import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for int parameters.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypeInteger extends ParameterTypeNumeric<Integer>
{
    /**
     * Construct a new ParameterTypeInteger without default value and check.
     * @param id short name of the new ParameterTypeInteger
     * @param description parameter description or full name of the new ParameterTypeInteger
     */
    public ParameterTypeInteger(final String id, final String description)
    {
        super(id, description, Integer.class);
    }

    /**
     * Construct a new ParameterTypeInteger with default value, without check.
     * @param id short name of the new ParameterTypeInteger
     * @param description parameter description or full name of the new ParameterTypeInteger
     * @param defaultValue the default value of the new ParameterTypeInteger
     */
    public ParameterTypeInteger(final String id, final String description, final int defaultValue)
    {
        super(id, description, Integer.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeInteger with default value, without check.
     * @param id short name of the new ParameterTypeInteger
     * @param description parameter description or full name of the new ParameterTypeInteger
     * @param defaultValue the default value of the new ParameterTypeInteger
     */
    public ParameterTypeInteger(final String id, final String description, final Integer defaultValue)
    {
        super(id, description, Integer.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeInteger without default value, with check.
     * @param id short name of the new ParameterTypeInteger
     * @param description parameter description or full name of the new ParameterTypeInteger
     * @param constraint constraint for parameter values
     */
    public ParameterTypeInteger(final String id, final String description, final Constraint<? super Integer> constraint)
    {
        super(id, description, Integer.class, constraint);
    }

    /**
     * Construct a new ParameterTypeInteger with default value and check.
     * @param id short name of the new ParameterTypeInteger
     * @param description parameter description or full name of the new ParameterTypeInteger
     * @param defaultValue the default value of the new ParameterTypeInteger
     * @param constraint constraint for parameter values
     */
    public ParameterTypeInteger(final String id, final String description, final int defaultValue,
            final Constraint<? super Integer> constraint)
    {
        super(id, description, Integer.class, defaultValue, constraint);
    }

    /**
     * Construct a new ParameterTypeInteger with default value and check.
     * @param id short name of the new ParameterTypeInteger
     * @param description parameter description or full name of the new ParameterTypeInteger
     * @param defaultValue the default value of the new ParameterTypeInteger
     * @param constraint constraint for parameter values
     */
    public ParameterTypeInteger(final String id, final String description, final Integer defaultValue,
            final Constraint<Number> constraint)
    {
        super(id, description, Integer.class, defaultValue, constraint);
    }

    @Override
    public final String printValue(final Parameters parameters) throws ParameterException
    {
        return Integer.toString(parameters.getParameter(this));
    }

    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @param params Set of parameters.
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(final int value, final Parameters params) throws ParameterException
    {
        //
    }

    @Override
    public String toString()
    {
        return "ParameterTypeInteger [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
