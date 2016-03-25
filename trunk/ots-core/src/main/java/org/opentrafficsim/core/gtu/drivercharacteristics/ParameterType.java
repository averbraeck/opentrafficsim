package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.value.vdouble.scalar.DoubleScalar;

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
 * @param <T> Class of the value.
 */
public class ParameterType<T extends DoubleScalar.Rel<?>> extends AbstractParameterType<T>
{
	/**
     * Constructor with default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public ParameterType(String id, String description, Class<T> valueClass, T defaultValue)
    {
        super(id, description, valueClass, defaultValue);
    }

    /**
     * Constructor without default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterType(String id, String description, Class<T> valueClass)
    {
        super(id, description, valueClass);
    }

    /**
     * Returns the class of the value.
     * @return valueClass Class of the value.
     */
    public final Class<T> getValueClass()
    {
        return super.valueClass;
    }

    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @throws ParameterException If the value does not comply with constraints.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void check(T value) throws ParameterException
    {
        //
    }

}