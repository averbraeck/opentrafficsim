package org.opentrafficsim.base.parameters;

import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Parameter type for {@code String} parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class ParameterTypeString extends ParameterType<String>
{

    /** */
    private static final long serialVersionUID = 20170911L;

    /**
     * @param id short name of the new ParameterTypeString
     * @param description description or full name of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description)
    {
        super(id, description, String.class);
    }

    /**
     * @param id short name of the new ParameterTypeString
     * @param description description or full name of the new ParameterTypeString
     * @param defaultValue default value of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description, final String defaultValue)
    {
        super(id, description, String.class, defaultValue);
    }

    /**
     * @param id short name of the new ParameterTypeString
     * @param description description or full name of the new ParameterTypeString
     * @param constraint constraint that applies to the value of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description, final Constraint<? super String> constraint)
    {
        super(id, description, String.class, constraint);
    }

    /**
     * @param id short name of the new ParameterTypeString
     * @param description description or full name of the new ParameterTypeString
     * @param defaultValue default value of the new ParameterTypeString
     * @param constraint constraint that applies to the value of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description, final String defaultValue,
            final Constraint<? super String> constraint)
    {
        super(id, description, String.class, defaultValue, constraint);
    }

    @Override
    public final String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this);
    }

    @Override
    public String toString()
    {
        return "ParameterTypeString [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
