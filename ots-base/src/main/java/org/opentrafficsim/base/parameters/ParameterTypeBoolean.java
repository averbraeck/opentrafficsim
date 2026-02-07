package org.opentrafficsim.base.parameters;

/**
 * Wrapper class for boolean parameters.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypeBoolean extends ParameterType<Boolean>
{
    /**
     * Construct a new ParameterTypeBoolean without default value.
     * @param id short name of the new ParameterTypeBoolean
     * @param description parameter description or full name of the new ParameterTypeBoolean
     */
    public ParameterTypeBoolean(final String id, final String description)
    {
        super(id, description, Boolean.class);
    }

    /**
     * Construct a new ParameterTypeBoolean with default value.
     * @param id short name of the new ParameterTypeBoolean
     * @param description parameter description or full name of the new ParameterTypeBoolean
     * @param defaultValue the default value of the new ParameterTypeBoolean
     */
    public ParameterTypeBoolean(final String id, final String description, final boolean defaultValue)
    {
        super(id, description, Boolean.class, defaultValue);
    }

    @Override
    public final String printValue(final Parameters parameters) throws ParameterException
    {
        return Boolean.toString(parameters.getParameter(this));
    }

    @Override
    public String toString()
    {
        return "ParameterTypeBoolean [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
