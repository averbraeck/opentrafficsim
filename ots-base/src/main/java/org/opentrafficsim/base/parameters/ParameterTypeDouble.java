package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.formatter.EngineeringFormatter;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Wrapper class for double parameters.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
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
     * Construct a new ParameterTypeDouble without default value and check.
     * @param id String; short name of the new ParameterTypeDouble
     * @param description String; parameter description or full name of the new ParameterTypeDouble
     */
    public ParameterTypeDouble(final String id, final String description)
    {
        super(id, description, Double.class);
    }

    /**
     * Construct a new ParameterTypeDouble with default value, without check.
     * @param id String; short name of the new ParameterTypeDouble
     * @param description String; parameter description or full name of the new ParameterTypeDouble
     * @param defaultValue double; the default value of the new ParametertypeDouble
     */
    public ParameterTypeDouble(final String id, final String description, final double defaultValue)
    {
        super(id, description, Double.class, defaultValue);
    }

    /**
     * Construct a new ParameterTypeDouble without default value, with check.
     * @param id String; short name of the new ParameterTypeDouble
     * @param description String; parameter description or full name of the new ParameterTypeDouble
     * @param constraint Constrain&lt;? super Double&gt;; constraint for parameter values
     */
    public ParameterTypeDouble(final String id, final String description, final Constraint<? super Double> constraint)
    {
        super(id, description, Double.class, constraint);
    }

    /**
     * Construct a new ParameterTypeDouble with default value and check.
     * @param id String; short name of the new ParameterTypeDouble
     * @param description String; parameter description or full name of the new ParameterTypeDouble
     * @param defaultValue double; the default value of the new ParameterTypeDouble
     * @param constraint Constraint&lt;? super Double&gt;; constraint for parameter values
     */
    public ParameterTypeDouble(final String id, final String description, final double defaultValue,
            final Constraint<? super Double> constraint)
    {
        super(id, description, Double.class, defaultValue, constraint);
    }

    /**
     * Construct a new ParameterTypeDouble with default value and check.
     * @param id String; short name of the new ParameterTypeDouble
     * @param description String; parameter description or full name of the new ParameterTypeDouble
     * @param defaultValue Double; default value for the new ParameterTypeDouble
     * @param constraint Constraint&lt;Number&gt;; constraint for parameter values
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
