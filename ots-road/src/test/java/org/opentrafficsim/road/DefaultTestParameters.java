package org.opentrafficsim.road;

import java.lang.reflect.Field;
import java.util.Set;

import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypeBoolean;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeInteger;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.road.gtu.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;

/**
 * Creator of set of parameters with default values.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DefaultTestParameters
{

    /**
     * Do not create instance.
     */
    private DefaultTestParameters()
    {
        // Do not instantiate
    }

    /**
     * Returns a default set of parameters.
     * @return Default set of parameters.
     */
    @SuppressWarnings("unchecked")
    public static ParameterSet create()
    {
        ParameterSet params = new ParameterSet();

        // set all default values using reflection
        for (Class<?> clazz : new Class[] {ParameterTypes.class, LmrsParameters.class, AbstractIdm.class})
        {
            Set<Field> fields = ClassUtil.getAllFields(clazz);
            try
            {
                for (Field field : fields)
                {
                    try
                    {
                        if (ParameterTypeNumeric.class.isAssignableFrom(field.getType()))
                        {
                            field.setAccessible(true);
                            @SuppressWarnings("rawtypes")
                            ParameterTypeNumeric p = (ParameterTypeNumeric) field.get(ParameterTypes.class);
                            params.setParameter(p, p.getDefaultValue());
                        }
                        else if (ParameterTypeBoolean.class.equals(field.getType()))
                        {
                            field.setAccessible(true);
                            ParameterTypeBoolean p = (ParameterTypeBoolean) field.get(ParameterTypes.class);
                            params.setParameter(p, p.getDefaultValue());
                        }
                        else if (ParameterTypeDouble.class.equals(field.getType()))
                        {
                            field.setAccessible(true);
                            ParameterTypeDouble p = (ParameterTypeDouble) field.get(ParameterTypes.class);
                            params.setParameter(p, p.getDefaultValue());
                        }
                        else if (ParameterTypeInteger.class.equals(field.getType()))
                        {
                            field.setAccessible(true);
                            ParameterTypeInteger p = (ParameterTypeInteger) field.get(ParameterTypes.class);
                            params.setParameter(p, p.getDefaultValue());
                        }
                        // FIXME: add another else to catch any unanticipated cases?
                    }
                    catch (ParameterException pe)
                    {
                        // FIXME: Explain why this exception can/should be ignored.
                        // do not set parameter without default value
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
        }
        return params;
    }

}
