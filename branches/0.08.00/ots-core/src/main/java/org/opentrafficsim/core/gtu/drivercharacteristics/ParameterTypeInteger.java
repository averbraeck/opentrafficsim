package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;

/**
 * Defines meta-information of a parameter, defining the parameter uniquely.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Wouter Schakel
 */
public class ParameterTypeInteger extends AbstractParameterType<Dimensionless>
{
    /**
     * Constructor with default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeInteger(final String id, final String description, final int defaultValue)
    {
        super(id, description, Dimensionless.class, new Dimensionless(defaultValue, DimensionlessUnit.SI));
        try
        {
            check(defaultValue);
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Default value does not comply with constraints.", exception);
        }
    }

    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(final double value) throws ParameterException
    {
        //
    }
    
    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @param bc Set of behavioral characteristics. 
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
    {
        //
    }

}
