package org.opentrafficsim.road.test;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;

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
public class TestBehavioralCharacteristics
{

    /**
     * 
     */
    public TestBehavioralCharacteristics()
    {
    }

    public static void main(final String[] args) throws ParameterException
    {
        BehavioralCharacteristics bc = new BehavioralCharacteristics();
        bc.setParameter(ParameterTypes.VCONG, new Speed(100, SpeedUnit.KM_PER_HOUR));
        bc.setParameter(ParameterTypes.VCONG, new Speed(50.0, SpeedUnit.KM_PER_HOUR));

        ParameterTypeDouble ptd = new ParameterTypeDouble("mijnParam", "mijn parameter")
        {
            /** {@inheritDoc} */
            @Override
            public void check(double value, BehavioralCharacteristics bc) throws ParameterException
            {
                ParameterException.throwIf(value>1.0, "Value is NaN...");
            }
            
        };
        bc.setParameter(ptd, 3.0);
        bc.setParameter(ptd, Double.NaN);
    }
}

