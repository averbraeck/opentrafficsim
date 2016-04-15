package org.opentrafficsim.road;

import java.lang.reflect.Field;
import java.util.Set;

import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeBoolean;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeInteger;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;

/**
 * Creator of set of behavioral characteristics with default values.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 15, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class DefaultTestParameters
{

    /**
     * Do not create instance.
     */
    private DefaultTestParameters()
    {
        //
    }
    
    /**
     * Returns a default set of behavioral characteristics.
     * @return Default set of behavioral characteristics.
     */
    @SuppressWarnings("unchecked")
    public static BehavioralCharacteristics create()
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

        return bc;
        
    }

}
