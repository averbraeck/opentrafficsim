package org.opentrafficsim.base;

/**
 * Superclass of all sorts of types, for example GTUType, ParameterType, etc. The only method in this class is <code>isType()</code>
 * which should be used to check whether any type object of unknown type, is a specific type. For example:
 * <code>speedLimitType.isType(SpeedLimitType.CURVATURE)</code>.<br>
 * <br>
 * Note that it is <b>not</b> safe to use <code>speedLimitType == SpeedLimitType.CURVATURE</code>.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Class of type.
 */
public abstract class Type<T extends Type<T>>
{

    /**
     * Returns whether this type is the same type as the given type. Use this method to check for any kind of type (e.g.
     * {@code GTUType} , {@code ParameterType}, etc.) whether an instance is a specific instance of the type. For example:
     * <code>speedLimitType.isType(SpeedLimitType.CURVATURE)</code>.<br>
     * <br>
     * Note that it is <b>not</b> safe to use <code>speedLimitType == SpeedLimitType.CURVATURE</code>.
     * @param type T; type instance to compare to
     * @return whether this type is the same type as the given type
     */
    public final boolean isType(final T type)
    {
        return this.equals(type);
    }

}
