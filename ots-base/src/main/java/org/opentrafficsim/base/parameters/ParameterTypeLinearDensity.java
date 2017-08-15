package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.base.parameters.constraint.Constraint;
import org.opentrafficsim.base.parameters.constraint.SingleBound.NumericConstraint;

/**
 * Wrapper class for LinearDensity parameters.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeLinearDensity extends ParameterTypeNumeric<LinearDensity> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150000L;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeLinearDensity(final String id, final String description)
    {
        super(id, description, LinearDensity.class);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeLinearDensity(final String id, final String description, final LinearDensity defaultValue)
    {
        super(id, description, LinearDensity.class, defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeLinearDensity(final String id, final String description, final Constraint<Number> constraint)
    {
        super(id, description, LinearDensity.class, constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeLinearDensity(final String id, final String description, final LinearDensity defaultValue,
            final Constraint<Number> constraint)
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
