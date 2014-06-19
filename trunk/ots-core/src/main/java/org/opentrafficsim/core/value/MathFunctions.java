package org.opentrafficsim.core.value;

import java.io.Serializable;

/**
 * Interface to force all functions of Math to be implemented.
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
 * @version Jun 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface MathFunctions extends Serializable
{
    /**
     * Set the value(s) to their absolute value.
     */
    void abs();

    /**
     * Set the value(s) to the arc cosine of the value(s); the angle is in the range 0.0 through pi.
     */
    void acos();

    /**
     * Set the value(s) to the arc sine of the value(s); the angle is in the range -pi/2 through pi/2.
     */
    void asin();

    /**
     * Set the value(s) to the arc tangent of the value(s); the angle is in the range -pi/2 through pi/2.
     */
    void atan();

    /**
     * Set the value(s) to the cube root.
     */
    void cbrt();

    /**
     * Set the value(s) to the smallest (closest to negative infinity) value(s) that are greater than or equal to the
     * argument and equal to a mathematical integer.
     */
    void ceil();

    /**
     * Set the value(s) to the trigonometric cosine of the value(s).
     */
    void cos();

    /**
     * Set the value(s) to the hyperbolic cosine of the value(s).
     */
    void cosh();

    /**
     * Set the value(s) to Euler's number e raised to the power of the value(s).
     */
    void exp();

    /**
     * Set the value(s) to e to the power of the value(s) minus 1 (e^x - 1).
     */
    void expm1();

    /**
     * Set the value(s) to the largest (closest to positive infinity) value(s) that are less than or equal to the
     * argument and equal to a mathematical integer.
     */
    void floor();

    /**
     * Set the value(s) to the natural logarithm (base e) of the value(s).
     */
    void log();

    /**
     * Set the value(s) to the base 10 logarithm of the value(s).
     */
    void log10();

    /**
     * Set the value(s) to the natural logarithm of the sum of the argument and 1.
     */
    void log1p();

    /**
     * Set the value(s) to the value(s) raised to the power of the argument.
     * @param x the value to use as the power
     */
    void pow(double x);

    /**
     * Set the value(s) to the value(s) that are closest in value to the argument and equal to a mathematical integer.
     */
    void rint();

    /**
     * Set the value(s) to the closest long to the argument, with ties rounding up.
     */
    void round();

    /**
     * Set the value(s) to the signum function of the value(s); zero if the argument is zero, 1.0 if the argument is
     * greater than zero, -1.0 if the argument is less than zero.
     */
    void signum();

    /**
     * Set the value(s) to the trigonometric sine of the value(s).
     */
    void sin();

    /**
     * Set the value(s) to the hyperbolic sine of the value(s).
     */
    void sinh();

    /**
     * Set the value(s) to the correctly rounded positive square root of the value(s).
     */
    void sqrt();

    /**
     * Set the value(s) to the trigonometric tangent of the value(s).
     */
    void tan();

    /**
     * Set the value(s) to the hyperbolic tangent of the value(s).
     */
    void tanh();

    /**
     * Set the value(s) to approximately equivalent angle(s) measured in degrees.
     */
    void toDegrees();

    /**
     * Set the value(s) to approximately equivalent angle(s) measured in radians.
     */
    void toRadians();

    /**
     * Set the value(s) to the complement (1.0/x) of the value(s).
     */
    void inv();
}
