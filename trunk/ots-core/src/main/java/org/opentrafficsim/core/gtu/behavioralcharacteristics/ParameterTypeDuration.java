package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Wrapper class for Time parameters.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeDuration extends ParameterTypeNumeric<Duration> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150400L;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeDuration(final String id, final String description)
    {
        super(id, description, Duration.class);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeDuration(final String id, final String description, final Duration defaultValue)
    {
        super(id, description, Duration.class, defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeDuration(final String id, final String description, final NumericConstraint constraint)
    {
        super(id, description, Duration.class, constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeDuration(final String id, final String description, final Duration defaultValue,
            final NumericConstraint constraint)
    {
        super(id, description, Duration.class, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ParameterTypeDuration [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
