package org.opentrafficsim.core.value.vfloat;

import cern.colt.function.tfloat.FloatFunction;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jun 18, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FloatMathFunctionsImpl
{
    /**
     * Function that returns <tt>Math.cbrt(a)</tt>.
     */
    public static final FloatFunction cbrt = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.cbrt(a);
        }
    };

    /**
     * Function that returns <tt>Math.cosh(x)</tt>.
     */
    public static final FloatFunction cosh = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.cosh(a);
        }
    };

    /**
     * Function that returns <tt>Math.expm1(x)</tt>.
     */
    public static final FloatFunction expm1 = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.expm1(a);
        }
    };

    /**
     * Function that returns <tt>Math.log10(x)</tt>.
     */
    public static final FloatFunction log10 = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.log10(a);
        }
    };

    /**
     * Function that returns <tt>Math.log1p(x)</tt>.
     */
    public static final FloatFunction log1p = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.log1p(a);
        }
    };

    /**
     * Function that returns <tt>Math.round(x)</tt>.
     */
    public static final FloatFunction round = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return Math.round(a);
        }
    };

    /**
     * Function that returns <tt>Math.signum(x)</tt>.
     */
    public static final FloatFunction signum = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return Math.signum(a);
        }
    };

    /**
     * Function that returns <tt>Math.sinh(x)</tt>.
     */
    public static final FloatFunction sinh = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.sinh(a);
        }
    };

    /**
     * Function that returns <tt>Math.tanh(x)</tt>.
     */
    public static final FloatFunction tanh = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.tanh(a);
        }
    };

    /**
     * Function that returns <tt>Math.toDegrees(x)</tt>.
     */
    public static final FloatFunction toDegrees = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.toDegrees(a);
        }
    };

    /**
     * Function that returns <tt>Math.toRadians(x)</tt>.
     */
    public static final FloatFunction toRadians = new FloatFunction()
    {
        public final float apply(final float a)
        {
            return (float) Math.toRadians(a);
        }
    };

}
