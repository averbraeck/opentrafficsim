package code.generators;

/**
 * Information about the math functions.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 0 sep. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class MathFunction
{
    /** Name of the function. */
    public final String name;

    /** Additional argument with description. */
    public final String argument;

    /** If set, the result of the function is always double (regardless of the argument). */
    public final boolean castToFloatRequired;

    /** Description of the function. */
    public final String description;

    /** If set this function also appears in *MathFunctionsImpl. */
    public final boolean appearsInMathFunctionsImpl;

    /** Generate this text in a to do if not null. */
    public final String toDoText;

    /**
     * Create a new mathFunctionEntry.
     * @param name String; name of the function
     * @param argument String; additional argument of the function (set to null if the function has only one argument)
     * @param castToFloatRequired boolean; if true; the result of the function is double (regardless of the argument)
     * @param appearsInMathFunctionsImpl boolean; if true; this function must also appear in the *MathFunctionImpl class
     * @param comment String; description of the function
     * @param toDoText String; if non-null a to do comment containing this text is generated with the implementation
     */
    public MathFunction(final String name, final String argument, final boolean castToFloatRequired,
            final boolean appearsInMathFunctionsImpl, final String comment, final String toDoText)
    {
        this.name = name;
        this.argument = argument;
        this.castToFloatRequired = castToFloatRequired;
        this.appearsInMathFunctionsImpl = appearsInMathFunctionsImpl;
        this.description = comment;
        this.toDoText = toDoText;
    }

    /** The math functions. */
    public static final MathFunction[] mathFunctions =
            {
                    new MathFunction("abs", null, false, false, "Set the value(s) to their absolute value.", null),
                    new MathFunction("acos", null, true, false,
                            "Set the value(s) to the arc cosine of the value(s); the resulting angle is in the range "
                                    + "0.0 through pi.", "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("asin", null, true, false,
                            "Set the value(s) to the arc sine of the value(s); the resulting angle is in the range "
                                    + "-pi/2 through pi/2.", "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("atan", null, true, false,
                            "Set the value(s) to the arc tangent of the value(s); the resulting angle is in the "
                                    + "range -pi/2 through pi/2.", "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("cbrt", null, true, true, "Set the value(s) to the(ir) cube root.",
                            "dimension for all SI coefficients / 3."),
                    new MathFunction("ceil", null, true, false,
                            "Set the value(s) to the smallest (closest to negative infinity) value(s) that are greater "
                                    + "than or equal to the\r\n"
                                    + "     * argument and equal to a mathematical integer.", null),
                    new MathFunction("cos", null, true, false,
                            "Set the value(s) to the trigonometric cosine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("cosh", null, true, true,
                            "Set the value(s) to the hyperbolic cosine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("exp", null, true, false,
                            "Set the value(s) to Euler's number e raised to the power of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("expm1", null, true, true,
                            "Set the value(s) to Euler's number e raised to the power of the value(s) minus 1 "
                                    + "(e^x - 1).", "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("floor", null, true, false,
                            "Set the value(s) to the largest (closest to positive infinity) value(s) that are less "
                                    + "than or equal to the\r\n"
                                    + "     * argument and equal to a mathematical integer.", null),
                    new MathFunction("log", null, true, false,
                            "Set the value(s) to the natural logarithm (base e) of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("log10", null, true, true,
                            "Set the value(s) to the base 10 logarithm of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("log1p", null, true, true,
                            "Set the value(s) to the natural logarithm of the sum of the value(s) and 1.",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("pow", "double|x|the value to use as the power", true, false,
                            "Set the value(s) to the value(s) raised to the power of the argument.",
                            "SI unit with coefficients * x."),
                    new MathFunction("rint", null, true, false,
                            "Set the value(s) to the value(s) that are closest in value to the argument and equal to "
                                    + "a mathematical integer.", null),
                    new MathFunction("round", null, false, true,
                            "Set the value(s) to the closest long to the argument with ties rounding up.", null),
                    new MathFunction("signum", null, false, true,
                            "Set the value(s) to the signum function of the value(s); zero if the argument is zero, "
                                    + "1.0 if the argument is\r\n"
                                    + "         * greater than zero, -1.0 if the argument is less than zero.",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("sin", null, true, false,
                            "Set the value(s) to the trigonometric sine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("sinh", null, true, true,
                            "Set the value(s) to the hyperbolic sine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("sqrt", null, true, false,
                            "Set the value(s) to the correctly rounded positive square root of the value(s).",
                            "dimension for all SI coefficients / 2."),
                    new MathFunction("tan", null, true, false,
                            "Set the value(s) to the trigonometric tangent of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("tanh", null, true, true,
                            "Set the value(s) to the hyperbolic tangent of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunction("toDegrees", null, true, true,
                            "Set the value(s) to approximately equivalent angle(s) measured in degrees.", null),
                    new MathFunction("toRadians", null, true, true,
                            "Set the value(s) to approximately equivalent angle(s) measured in radians.", null),
                    new MathFunction("inv", null, true, false,
                            "Set the value(s) to the complement (1.0/x) of the value(s).",
                            "negate all coefficients in the Unit.")};

}