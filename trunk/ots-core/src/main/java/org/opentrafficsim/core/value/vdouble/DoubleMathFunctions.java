package org.opentrafficsim.core.value.vdouble;

import org.opentrafficsim.core.value.MathFunctions;

/**
 * Force implementation of multiplyBy and divideBy.
 * <p>
 * This file was generated by the OpenTrafficSim value classes generator, 26 jun, 2015
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version26 jun, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> the type that these MathFunctions manipulate
 */
public interface DoubleMathFunctions<T> extends MathFunctions<T>
{
    /**
     * Scale the value(s) by a factor.
     * @param factor double; the multiplier
     * @return T; the modified T
     */
    T multiplyBy(double factor);

    /**
     * Scale the value(s) by the inverse of a factor; i.e. a divisor.
     * @param divisor double; the divisor
     * @return T; the modified T
     */
    T divideBy(double divisor);

}
