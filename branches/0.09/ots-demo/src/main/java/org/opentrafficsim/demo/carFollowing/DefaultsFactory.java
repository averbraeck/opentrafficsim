package org.opentrafficsim.demo.carFollowing;

import java.lang.reflect.Field;
import java.util.Set;

import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeBoolean;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeInteger;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;

/**
 * Factory for defaults in demos.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class DefaultsFactory
{

    /**
     * Do not create instance.
     */
    private DefaultsFactory()
    {
        //
    }
    
    /**
     * Returns a default set of behavioral characteristics.
     * @return Default set of behavioral characteristics.
     */
    @SuppressWarnings("unchecked")
    public static BehavioralCharacteristics getDefaultBehavioralCharacteristics()
    {
        
        BehavioralCharacteristics bc = new BehavioralCharacteristics();

        // set all default values using reflection
        Set<Field> fields = ClassUtil.getAllFields(ParameterTypes.class);
        try
        {
            for (Field field : fields)
            {
                if (ParameterType.class.isAssignableFrom(field.getType()))
                {
                    try 
                    {
                        field.setAccessible(true);
                        @SuppressWarnings("rawtypes")
                        ParameterType p = (ParameterType) field.get(ParameterTypes.class);
                        bc.setParameter(p, p.getDefaultValue());
                    }
                    catch (ParameterException pe)
                    {
                        // do not set parameter without default value
                    }
                }
                else if (ParameterTypeBoolean.class.equals(field.getType()))
                {
                    try 
                    {
                        field.setAccessible(true);
                        ParameterTypeBoolean p = (ParameterTypeBoolean) field.get(ParameterTypes.class);
                        bc.setParameter(p, p.getDefaultValue());
                    }
                    catch (ParameterException pe)
                    {
                        // do not sat parameter without default value
                    }
                }
                else if (ParameterTypeDouble.class.equals(field.getType()))
                {
                    try 
                    {
                        field.setAccessible(true);
                        ParameterTypeDouble p = (ParameterTypeDouble) field.get(ParameterTypes.class);
                        bc.setParameter(p, p.getDefaultValue());
                    }
                    catch (ParameterException pe)
                    {
                        // do not sat parameter without default value
                    }
                }
                else if (ParameterTypeInteger.class.equals(field.getType()))
                {
                    try 
                    {
                        field.setAccessible(true);
                        ParameterTypeInteger p = (ParameterTypeInteger) field.get(ParameterTypes.class);
                        bc.setParameter(p, p.getDefaultValue());
                    }
                    catch (ParameterException pe)
                    {
                        // do not sat parameter without default value
                    }
                }
            }
        }
        catch (IllegalArgumentException iare)
        {
            iare.printStackTrace();
        }
        catch (IllegalAccessException iace)
        {
            iace.printStackTrace();
        }

        // demos use different value from default LMRS value
        try
        {
            bc.setParameter(ParameterTypes.LOOKAHEAD, new Length(250, LengthUnit.SI));
        }
        catch (ParameterException pe)
        {
            throw new RuntimeException("Parameter type 'LOOKAHEAD' could not be set.", pe);
        }

        return bc;
        
    }

}
