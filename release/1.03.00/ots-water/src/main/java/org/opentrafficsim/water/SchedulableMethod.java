/**
 * 
 */
package org.opentrafficsim.water;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.reflection.ClassUtil;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SchedulableMethod implements Serializable
{
    /** */
    private static final long serialVersionUID = 1L;

    /** target reflects the target on which a state change is scheduled. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Object target = null;

    /** method is the method which embodies the state change. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected String method = null;

    /** args are the arguments which are used to invoke the method with. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Object[] args = null;

    /** cache. */
    private static Map<String, Method> cacheMethods = new LinkedHashMap<String, Method>();

    /**
     * The constructor of the schedulable method stores the object and method to invoke with its arguments.
     * @param target Object; reflects the object on which the method must be invoked.
     * @param method String; reflects the method to invoke
     * @param args Object[]; reflects the argumenst the method to invoke with
     */
    public SchedulableMethod(final Object target, final String method, final Object[] args)
    {
        if (target == null || method == null)
        {
            throw new IllegalArgumentException("target or method==null");
        }
        this.target = target;
        this.method = method;
        this.args = args;
    }

    /**
     * Executes the method. Method &lt;init&gt; means the constructor.
     */
    public final synchronized void execute()
    {
        try
        {
            if (this.method.equals("<init>"))
            {
                if (!(this.target instanceof Class))
                {
                    throw new SimRuntimeException("Invoking a constructor implies that target should be instance of Class");
                }
                Constructor<?> constructor = ClassUtil.resolveConstructor((Class<?>) this.target, this.args);
                constructor.setAccessible(true);
                constructor.newInstance(this.args);
            }
            else
            {
                String key = this.target.getClass().getName() + "_" + this.method;
                Method tm = cacheMethods.get(key);
                if (tm == null)
                {
                    tm = ClassUtil.resolveMethod(this.target, this.method, this.args);
                    cacheMethods.put(key, tm);
                }
                tm.setAccessible(true);
                tm.invoke(this.target, this.args);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return Returns the args.
     */
    public final Object[] getArgs()
    {
        return this.args;
    }

    /**
     * @return Returns the method.
     */
    public final String getMethod()
    {
        return this.method;
    }

    /**
     * @return Returns the target.
     */
    public final Object getTarget()
    {
        return this.target;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SchedulableMethod[target=" + this.target + "; method=" + this.method + "; args=" + this.args + "]";
    }

}
