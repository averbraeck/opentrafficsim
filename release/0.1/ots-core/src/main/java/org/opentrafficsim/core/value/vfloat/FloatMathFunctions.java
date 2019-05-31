package org.opentrafficsim.core.value.vfloat;

import org.opentrafficsim.core.value.MathFunctions;

/**
 * Force implementation of multiply and divide.
 * <p>
 * This file was generated by the OpenTrafficSim value classes generator, 09 mrt, 2015
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 09 mrt, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> the type that these MathFunctions manipulate
 */
public interface FloatMathFunctions<T> extends MathFunctions<T>
{
    /**
     * Scale the value(s) by a factor.
     * @param factor float; the multiplier
     * @return T; the modified T
     */
    T multiply(float factor);

    /**
     * Scale the value(s) by the inverse of a factor; i.e. a divisor.
     * @param divisor float; the divisor
     * @return T; the modified T
     */
    T divide(float divisor);

}