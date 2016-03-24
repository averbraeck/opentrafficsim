package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> 
 */
public class ParameterType<T extends DoubleScalar.Rel<?>> extends AbstractParameterType<T>
{
    /**
     * @param id
     * @param description
     * @param valueClass
     * @param defaultValue
     */
    public ParameterType(String id, String description, Class<T> valueClass, T defaultValue)
    {
        super(id, description, valueClass, defaultValue);
    }

    /**
     * @param id
     * @param description
     * @param valueClass
     */
    public ParameterType(String id, String description, Class<T> valueClass)
    {
        super(id, description, valueClass);
    }

    /**
     * @return valueClass
     */
    public final Class<T> getValueClass()
    {
        return super.valueClass;
    }

    /**
     * 
     * @param value
     * @throws ParameterException when the value does not comply with the constraints
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void check(T value) throws ParameterException
    {
        //
    }


}

