package org.opentrafficsim.base.parameters;

import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Parameter type for {@code String} parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class ParameterTypeString extends ParameterType<String>
{

    /** */
    private static final long serialVersionUID = 20170911L;

    /**
     * @param id String; short name of the new ParameterTypeString
     * @param description String; description or full name of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description)
    {
        super(id, description, String.class);
    }

    /**
     * @param id String; short name of the new ParameterTypeString
     * @param description String; description or full name of the new ParameterTypeString
     * @param defaultValue String; default value of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description, final String defaultValue)
    {
        super(id, description, String.class, defaultValue);
    }

    /**
     * @param id String; short name of the new ParameterTypeString
     * @param description String; description or full name of the new ParameterTypeString
     * @param constraint Constraint&lt;? super String&gt;; constraint that applies to the value of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description, final Constraint<? super String> constraint)
    {
        super(id, description, String.class, constraint);
    }

    /**
     * @param id String; short name of the new ParameterTypeString
     * @param description String; description or full name of the new ParameterTypeString
     * @param defaultValue String; default value of the new ParameterTypeString
     * @param constraint Constraint&lt;? super String&gt;; constraint that applies to the value of the new ParameterTypeString
     */
    public ParameterTypeString(final String id, final String description, final String defaultValue,
            final Constraint<? super String> constraint)
    {
        super(id, description, String.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterTypeString [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
