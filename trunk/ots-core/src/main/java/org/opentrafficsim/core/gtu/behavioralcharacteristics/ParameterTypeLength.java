package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

/**
 * Wrapper class for Length parameters.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, 
 *          initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeLength extends ParameterType<LengthUnit, Length.Rel> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeLength(final String id, final String description)
    {
        super(id, description, Length.Rel.class);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeLength(final String id, final String description, final Length.Rel defaultValue)
    {
        super(id, description, Length.Rel.class, defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param check Check for parameter values.
     */
    public ParameterTypeLength(final String id, final String description, final Check check)
    {
        super(id, description, Length.Rel.class, check);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param check Check for parameter values.
     */
    public ParameterTypeLength(final String id, final String description, final Length.Rel defaultValue,
        final Check check)
    {
        super(id, description, Length.Rel.class, defaultValue, check);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ParameterTypeLength [id=" + super.getId() + "]";
    }

}
