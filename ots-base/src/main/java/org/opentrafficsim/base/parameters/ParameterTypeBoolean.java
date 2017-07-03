package org.opentrafficsim.base.parameters;

import java.io.Serializable;

/**
 * Wrapper class for boolean parameters.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeBoolean extends AbstractParameterType<Boolean> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor without default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeBoolean(final String id, final String description)
    {
        super(id, description, Boolean.class);
    }

    /**
     * Constructor with default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeBoolean(final String id, final String description, final boolean defaultValue)
    {
        super(id, description, Boolean.class, defaultValue);
    }

    /** {@inheritDoc} */
    public final String printValue(final Parameters parameters) throws ParameterException
    {
        return Boolean.toString(parameters.getParameter(this));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterTypeBoolean [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
