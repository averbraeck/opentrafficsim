package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.value.vdouble.scalar.Speed;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ParameterTypes
{
    /** */
    private ParameterTypes()
    {
        //
    }

    /** */
    public static final ParameterType<Speed> VMAX = new ParameterType<Speed>("vmax", "maximum speed", Speed.class)
    {
        /** {@inheritDoc} */
        @Override
        public void check(Speed value) throws ParameterException
        {
            ParameterException.failIf(value.si <= 0.0, "VMAX should contain a positive speed");
        }
    };
    
}

