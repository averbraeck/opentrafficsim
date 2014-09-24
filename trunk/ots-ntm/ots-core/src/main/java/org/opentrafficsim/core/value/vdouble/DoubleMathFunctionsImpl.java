package org.opentrafficsim.core.value.vdouble;

import cern.colt.function.tdouble.DoubleFunction;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 18, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class DoubleMathFunctionsImpl
{
    /**
     * This class should never be instantiated.
     */
    private DoubleMathFunctionsImpl()
    {
        // Prevent instantiation of this class
    }

    /**
     * Function that returns <tt>Math.cbrt(a)</tt>.
     */
    public static final DoubleFunction cbrt = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.cbrt(a);
        }
    };

    /**
     * Function that returns <tt>Math.cosh(x)</tt>.
     */
    public static final DoubleFunction cosh = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.cosh(a);
        }
    };

    /**
     * Function that returns <tt>Math.expm1(x)</tt>.
     */
    public static final DoubleFunction expm1 = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.expm1(a);
        }
    };

    /**
     * Function that returns <tt>Math.log10(x)</tt>.
     */
    public static final DoubleFunction log10 = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.log10(a);
        }
    };

    /**
     * Function that returns <tt>Math.log1p(x)</tt>.
     */
    public static final DoubleFunction log1p = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.log1p(a);
        }
    };

    /**
     * Function that returns <tt>Math.round(x)</tt>.
     */
    public static final DoubleFunction round = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.round(a);
        }
    };

    /**
     * Function that returns <tt>Math.signum(x)</tt>.
     */
    public static final DoubleFunction signum = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.signum(a);
        }
    };

    /**
     * Function that returns <tt>Math.sinh(x)</tt>.
     */
    public static final DoubleFunction sinh = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.sinh(a);
        }
    };

    /**
     * Function that returns <tt>Math.tanh(x)</tt>.
     */
    public static final DoubleFunction tanh = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.tanh(a);
        }
    };

    /**
     * Function that returns <tt>Math.toDegrees(x)</tt>.
     */
    public static final DoubleFunction toDegrees = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.toDegrees(a);
        }
    };

    /**
     * Function that returns <tt>Math.toRadians(x)</tt>.
     */
    public static final DoubleFunction toRadians = new DoubleFunction()
    {
        @Override
        public double apply(final double a)
        {
            return Math.toRadians(a);
        }
    };

}
