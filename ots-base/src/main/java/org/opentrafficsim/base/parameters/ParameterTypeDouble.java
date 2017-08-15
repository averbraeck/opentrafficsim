package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.formatter.EngineeringFormatter;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for double parameters.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeDouble extends ParameterTypeNumeric<Double> implements Serializable
{

    /** */
    private static final long serialVersionUID = 120160400;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeDouble(final String id, final String description)
    {
        super(id, description, Double.class);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name..
     * @param defaultValue Default value.
     */
    public ParameterTypeDouble(final String id, final String description, final double defaultValue)
    {
        super(id, description, Double.class, defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeDouble(final String id, final String description, final Constraint<Number> constraint)
    {
        super(id, description, Double.class, constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeDouble(final String id, final String description, final double defaultValue,
            final Constraint<Number> constraint)
    {
        super(id, description, Double.class, defaultValue, constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeDouble(final String id, final String description, final Double defaultValue,
            final Constraint<Number> constraint)
    {
        super(id, description, Double.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String printValue(final Parameters parameters) throws ParameterException
    {
        return EngineeringFormatter.format(parameters.getParameter(this));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterTypeDouble [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
