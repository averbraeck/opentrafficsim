package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Test;
import org.reflections.Reflections;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 12 jan. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class DefinesSITest
{
    /**
     * Check that all unit classes that extend Unit define the SI field.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public final void definesSI()
    {
        Reflections reflections = new Reflections("org.opentrafficsim");
        Set<Class<? extends Unit>> classes = reflections.getSubTypesOf(Unit.class);
        for (Class<? extends Unit> c : classes)
        {
            final String className = c.getCanonicalName();
            if (className.endsWith("SIUnit") || className.endsWith("OffsetUnit"))
            {
                continue;
            }
            Field[] fields = c.getDeclaredFields();
            boolean foundSI = false;
            for (Field f : fields)
            {
                if (f.getName().equals("SI"))
                {
                    foundSI = true;
                }
            }
            assertTrue("Class " + className + " does not declare field SI", foundSI);
        }
    }
}
