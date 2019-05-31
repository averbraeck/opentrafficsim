package org.opentrafficsim.core.value.vfloat;

import cern.colt.function.tfloat.FloatFunction;

/**
 * FloatFunction implementations of the standard Math functions.
 * <p>
 * This file was generated by the OpenTrafficSim value classes generator, 26 jun, 2015
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 26 jun, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class FloatMathFunctionsImpl
{
    /**
     * This class shall never be instantiated.
     */
    private FloatMathFunctionsImpl()
    {
        // Prevent instantiation of this class
    }

    /**
     * Function that returns <tt>Math.cbrt(a)</tt>.
     */
    public static final FloatFunction cbrt = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.cbrt(a);
        }
    };

    /**
     * Function that returns <tt>Math.cosh(a)</tt>.
     */
    public static final FloatFunction cosh = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.cosh(a);
        }
    };

    /**
     * Function that returns <tt>Math.expm1(a)</tt>.
     */
    public static final FloatFunction expm1 = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.expm1(a);
        }
    };

    /**
     * Function that returns <tt>Math.log10(a)</tt>.
     */
    public static final FloatFunction log10 = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.log10(a);
        }
    };

    /**
     * Function that returns <tt>Math.log1p(a)</tt>.
     */
    public static final FloatFunction log1p = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.log1p(a);
        }
    };

    /**
     * Function that returns <tt>Math.round(a)</tt>.
     */
    public static final FloatFunction round = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return Math.round(a);
        }
    };

    /**
     * Function that returns <tt>Math.signum(a)</tt>.
     */
    public static final FloatFunction signum = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return Math.signum(a);
        }
    };

    /**
     * Function that returns <tt>Math.sinh(a)</tt>.
     */
    public static final FloatFunction sinh = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.sinh(a);
        }
    };

    /**
     * Function that returns <tt>Math.tanh(a)</tt>.
     */
    public static final FloatFunction tanh = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.tanh(a);
        }
    };

    /**
     * Function that returns <tt>Math.toDegrees(a)</tt>.
     */
    public static final FloatFunction toDegrees = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.toDegrees(a);
        }
    };

    /**
     * Function that returns <tt>Math.toRadians(a)</tt>.
     */
    public static final FloatFunction toRadians = new FloatFunction()
    {
        @Override
        public float apply(final float a)
        {
            return (float) Math.toRadians(a);
        }
    };

}