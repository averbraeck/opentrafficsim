package valueclasses.generator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Generate the Java code for the value classes.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 24 sep. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Generator
{
    /**
     * Information about the math functions
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version 30 sep. 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class MathFunctionEntry
    {
        /** Name of the function */
        public final String name;

        /** Additional argument with description */
        public final String argument;

        /** If set, the result of the function is always double (regardless of the argument) */
        public final boolean castToFloatRequired;

        /** Description of the function */
        public final String description;

        /** If set this function also appears in *MathFunctionsImpl */
        public final boolean appearsInMathFunctionsImpl;

        /** Generate this text in a to do if not null */
        public final String toDoText;

        /**
         * Create a new mathFunctionEntry.
         * @param name String; name of the function
         * @param argument String; additional argument of the function (set to null if the function has only one
         *            argument)
         * @param castToFloatRequired boolean; if true; the result of the function is double (regardless of the
         *            argument)
         * @param appearsInMathFunctionsImpl boolean; if true; this function must also appear in the *MathFunctionImpl
         *            class
         * @param comment String; description of the function
         * @param toDoText String; if non-null a to do comment containing this text is generated with the implementation
         */
        public MathFunctionEntry(final String name, final String argument, final boolean castToFloatRequired,
                boolean appearsInMathFunctionsImpl, final String comment, final String toDoText)
        {
            this.name = name;
            this.argument = argument;
            this.castToFloatRequired = castToFloatRequired;
            this.appearsInMathFunctionsImpl = appearsInMathFunctionsImpl;
            this.description = comment;
            this.toDoText = toDoText;
        }
    }

    /** The math functions */
    public static MathFunctionEntry[] mathFunctions =
            {
                    new MathFunctionEntry("abs", null, false, false, "Set the value(s) to their absolute value.", null),
                    new MathFunctionEntry("acos", null, true, false,
                            "Set the value(s) to the arc cosine of the value(s); the resulting angle is in the range "
                                    + "0.0 through pi.", "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("asin", null, true, false,
                            "Set the value(s) to the arc sine of the value(s); the resulting angle is in the range "
                                    + "-pi/2 through pi/2.", "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("atan", null, true, false,
                            "Set the value(s) to the arc tangent of the value(s); the resulting angle is in the "
                                    + "range -pi/2 through pi/2.", "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("cbrt", null, true, true, "Set the value(s) to the(ir) cube root.",
                            "dimension for all SI coefficients / 3."),
                    new MathFunctionEntry("ceil", null, true, false,
                            "Set the value(s) to the smallest (closest to negative infinity) value(s) that are "
                                    + "greater than or equal to the\r\n"
                                    + "     * argument and equal to a mathematical integer.", null),
                    new MathFunctionEntry("cos", null, true, false,
                            "Set the value(s) to the trigonometric cosine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("cosh", null, true, true,
                            "Set the value(s) to the hyperbolic cosine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("exp", null, true, false,
                            "Set the value(s) to Euler's number e raised to the power of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("expm1", null, true, true,
                            "Set the value(s) to Euler's number e raised to the power of the value(s) minus 1 "
                                    + "(e^x - 1).", "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("floor", null, true, false,
                            "Set the value(s) to the largest (closest to positive infinity) value(s) that are less "
                                    + "than or equal to the\r\n"
                                    + "     * argument and equal to a mathematical integer.", null),
                    new MathFunctionEntry("log", null, true, false,
                            "Set the value(s) to the natural logarithm (base e) of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("log10", null, true, true,
                            "Set the value(s) to the base 10 logarithm of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("log1p", null, true, true,
                            "Set the value(s) to the natural logarithm of the sum of the value(s) and 1.",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("pow", "double|x|the value to use as the power", true, false,
                            "Set the value(s) to the value(s) raised to the power of the argument.",
                            "SI unit with coefficients * x."),
                    new MathFunctionEntry("rint", null, true, false,
                            "Set the value(s) to the value(s) that are closest in value to the argument and equal "
                                    + "to a mathematical integer.", null),
                    new MathFunctionEntry("round", null, false, true,
                            "Set the value(s) to the closest long to the argument with ties rounding up.", null),
                    new MathFunctionEntry("signum", null, false, true,
                            "Set the value(s) to the signum function of the value(s); zero if the argument is zero, "
                                    + "1.0 if the argument is\r\n"
                                    + "         * greater than zero, -1.0 if the argument is less than zero.",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("sin", null, true, false,
                            "Set the value(s) to the trigonometric sine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("sinh", null, true, true,
                            "Set the value(s) to the hyperbolic sine of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("sqrt", null, true, false,
                            "Set the value(s) to the correctly rounded positive square root of the value(s).",
                            "dimension for all SI coefficients / 2."),
                    new MathFunctionEntry("tan", null, true, false,
                            "Set the value(s) to the trigonometric tangent of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("tanh", null, true, true,
                            "Set the value(s) to the hyperbolic tangent of the value(s).",
                            "dimensionless result (SIUnit.ONE)."),
                    new MathFunctionEntry("toDegrees", null, true, true,
                            "Set the value(s) to approximately equivalent angle(s) measured in degrees.", null),
                    new MathFunctionEntry("toRadians", null, true, true,
                            "Set the value(s) to approximately equivalent angle(s) measured in radians.", null),
                    new MathFunctionEntry("inv", null, true, false,
                            "Set the value(s) to the complement (1.0/x) of the value(s).",
                            "negate all coefficients in the Unit."),};

    /**
     * Generate the code for the value classes.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(String args[])
    {
        Date now = new Date();
        CodeGenerator cg =
                new CodeGenerator("the OpenTrafficSim valueclasses generator", "d:\\valueTree",
                        "org.opentrafficsim.core", new SimpleDateFormat("dd MMM, yyyy").format(now), new Long(
                                new SimpleDateFormat("yyyyMMdd").format(now)));
        cg.generateInterface("value", "Absolute", null,
                "Absolute values are quantities that are measured from some agreed upon reference point.", null, "",
                null);
        cg.generateInterface("value", "Relative", null, "Relative values express differences.", null, "", null);
        cg.generateInterface("value", "DenseData", null, "Values are stored densely.", null, "", null);
        cg.generateInterface("value", "SparseData", null, "Values are stored sparsely (lots of zero values expected).",
                null, "", null);
        cg.generateAbstractClass(
                "value",
                "AbstractValue",
                new String[]{"java.io.Serializable", "", "org.opentrafficsim.core.unit.Unit"},
                "AbstractValue is a class to help construct Matrix, Complex, "
                        + "and Vector but it does not extend java.lang.Number. The Scalar\r\n"
                        + " * class <i>does</i> extend Number, and implements the same interfaces from Value.",
                new String[]{"<U> the Unit of the value(s) in this AbstractValue. Used for setting, getting "
                        + "and displaying the value(s)"},
                "<U extends Unit<U>> implements Value<U>, Serializable",
                cg.buildField(cg.indent(1), "private final U unit", "The unit of this AbstractValue.")
                        + cg.buildMethod(cg.indent(1),
                                "protected||AbstractValue|the value in the unit as specified for this AbstractValue",
                                "Construct a new AbstractValue.",
                                new String[]{"final U|unit|the unit of the new AbstractValue"}, null, null,
                                new String[]{"this.unit = unit;"}, true)
                        + cg.buildMethod(cg.indent(1), "public final|U|getUnit", null, null, null, null,
                                new String[]{"return this.unit;"}, false)
                        + cg.buildMethod(cg.indent(1), "public final|double|expressAsSIUnit", null,
                                new String[]{"final double|value|"}, null, null,
                                new String[]{"return ValueUtil.expressAsSIUnit(value, this.unit);"}, false)
                        + cg.buildMethod(cg.indent(1), "protected final|double|expressAsSpecifiedUnit|the value "
                                + "in the unit as specified for this AbstractValue",
                                "Convert a value in SI standard unit into the unit of this AbstractValue.",
                                new String[]{"final double|value|the value in the standard SI unit"}, null, null,
                                new String[]{"return ValueUtil.expressAsUnit(value, this.unit);"}, false)
                        + cg.buildMethod(cg.indent(1), "public final|boolean|isAbsolute", null, null, null, null,
                                new String[]{"return this instanceof Absolute;"}, false)
                        + cg.buildMethod(cg.indent(1), "public final|boolean|isRelative", null, null, null, null,
                                new String[]{"return this instanceof Relative;"}, false));
        cg.generateFinalClass(
                "value",
                "Format",
                null,
                "Format a floating point number in a reasonable way. <br>\r\n"
                        + " * I've experienced problems with the %g conversions that caused array bounds"
                        + " violations. Those versions of the JRE that do\r\n"
                        + " * <b>not</b> throw such Exceptions use one digit less than specified in the %g "
                        + "conversions. <br >\r\n"
                        + " * TODO: check how to always format numbers corresponding to the Locale used.",
                null,
                "",
                cg.buildField(cg.indent(1), "public static final int DEFAULTSIZE = 9",
                        "Default total width of formatted value.")
                        + cg.buildField(cg.indent(1), "public static final int DEFAULTPRECISION = 3",
                                "Default number of fraction digits.")
                        + cg.buildMethod(cg.indent(1),
                                "private static|String|formatString|suitable for formatting a float or double",
                                "Build a format string.", new String[]{
                                        "final int|width|the number of characters in the result",
                                        "final int|precision|the number of fractional digits in the result",
                                        "final String|converter|the format conversion specifier"}, null, null,
                                new String[]{"return String.format(\"%%%d.%d%s\", width, precision, converter);"},
                                false) + buildFormatMethods("float", cg) + buildFormatMethods("double", cg));
        cg.generateInterface("value", "MathFunctions", new String[]{"java.io.Serializable"},
                "Interface to force all functions of Math to be implemented.", null, "extends Serializable",
                buildAllMathFunctions(cg));
        cg.generateInterface(
                "value",
                "Value",
                new String[]{"org.opentrafficsim.core.unit.Unit"},
                "Value is a static interface that forces implementation of a few unit- and value-related methods.",
                new String[]{"<U> the unit type"},
                "<U extends Unit<U>>",
                cg.buildMethod(cg.indent(1), "|U|getUnit|the unit of this Value", "Retrieve the unit of this Value.",
                        null, null, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|double|expressAsSIUnit|the value in the standard SI unit",
                                "Convert a value to the standard SI unit.",
                                new String[]{"final double|value|the value to convert to the standard SI unit"}, null,
                                null, null, false)
                        + cg.buildMethod(cg.indent(1), "|boolean|isAbsolute",
                                "Indicate whether this is an Absolute Value.", null, null, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|boolean|isRelative",
                                "Indicate whether this is a Relative Value.", null, null, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|Value<U>|copy", "Create a deep copy of this Value.", null,
                                null, null, null, false));
        cg.generateAbstractClass(
                "value",
                "Scalar",
                new String[]{"java.io.Serializable", "", "org.opentrafficsim.core.unit.Unit"},
                "Basics of the Scalar type",
                new String[]{"<U> the unit of the values in the constructor and for display"},
                "<U extends Unit<U>> extends Number implements Value<U>, Serializable",
                cg.buildField(cg.indent(1), "private final U unit", "The unit of the Scalar.")
                        + cg.buildMethod(cg.indent(1), "public||Scalar", "Construct a new Scalar.",
                                new String[]{"final U|unit|the unit of the new Scalar"}, null, null,
                                new String[]{"this.unit = unit;"}, true)
                        + cg.buildMethod(cg.indent(1), "public final|U|getUnit", null, null, null, null,
                                new String[]{"return this.unit;"}, false)
                        + cg.buildMethod(cg.indent(1), "public final|double|expressAsSIUnit", null,
                                new String[]{"final double|value|"}, null, null,
                                new String[]{"return ValueUtil.expressAsSIUnit(value, this.unit);"}, false)
                        + cg.buildMethod(cg.indent(1),
                                "protected final|double|expressAsSpecifiedUnit|the value in the unit of this Scalar",
                                "Convert a value from the standard SI unit into the unit of this Scalar.",
                                new String[]{"final double|value|the value to convert"}, null, null,
                                new String[]{"return ValueUtil.expressAsUnit(value, this.unit);"}, false)
                        + cg.buildMethod(cg.indent(1), "public final|boolean|isAbsolute", null, null, null, null,
                                new String[]{"return this instanceof Absolute;"}, false)
                        + cg.buildMethod(cg.indent(1), "public final|boolean|isRelative", null, null, null, null,
                                new String[]{"return this instanceof Relative;"}, false));
        cg.generateClass(
                "value",
                "ValueException",
                null,
                "Exception that is thrown for bad indices, or non-rectangular arrays, incompatible arrays or "
                        + "matrices, or empty arrays",
                null,
                "public",
                "extends Exception",
                true,
                cg.buildMethod(cg.indent(1), "public||ValueException", "Construct a new ValueException.", null, null,
                        null, new String[]{"super();"}, true)
                        + cg.buildMethod(cg.indent(1), "public||ValueException", "Construct a new ValueException.",
                                new String[]{"final String|message|description of the problem"}, null, null,
                                new String[]{"super(message);"}, true)
                        + cg.buildMethod(cg.indent(1), "public||ValueException", "Construct a new ValueException.",
                                new String[]{"final Throwable|cause|the cause of this ValueException"}, null, null,
                                new String[]{"super(cause);"}, true)
                        + cg.buildMethod(cg.indent(1), "public||ValueException", "Construct a new ValueException.",
                                new String[]{"final String|message|description of the problem",
                                        "final Throwable|cause|the cause of this ValueException"}, null, null,
                                new String[]{"super(message, cause);"}, true)
                        + cg.buildMethod(cg.indent(1), "public||ValueException", "Construct a new ValueException.",
                                new String[]{
                                        "final String|message|description of the problem",
                                        "final Throwable|cause|the cause of this ValueException",
                                        "final boolean|enableSuppression|whether or not suppression is enabled or "
                                                + "disabled",
                                        "final boolean|writableStackTrace|whether or not the stack trace should be "
                                                + "writable"}, null, null,
                                new String[]{"super(message, cause, enableSuppression, writableStackTrace);"}, true));
        cg.generateFinalClass(
                "value",
                "ValueUtil",
                new String[]{"org.opentrafficsim.core.unit.OffsetUnit", "org.opentrafficsim.core.unit.Unit"},
                "ValueUtil implements a couple of unit-related static methods.",
                null,
                "",
                cg.buildMethod(cg.indent(1), "public static|double|expressAsSIUnit|the value in the standard SI unit",
                        "Convert a value in a given unit into the equivalent in the standard SI unit.", new String[]{
                                "final double|value|the value to convert into the standard SI unit",
                                "final Unit<?>|unit|the unit of the given value"}, null, null, new String[]{
                                "if (unit instanceof OffsetUnit<?>)", "{",
                                "    return (value - ((OffsetUnit<?>) unit).getOffsetToStandardUnit())",
                                "            * unit.getConversionFactorToStandardUnit();", "}",
                                "return value * unit.getConversionFactorToStandardUnit();"}, false)

                        + cg.buildMethod(
                                cg.indent(1),
                                "public static|double|expressAsUnit|the value in the targetUnit",
                                "Convert a value from the standard SI unit into a compatible unit.",
                                new String[]{"final double|siValue|the given value in the standard SI unit",
                                        "final Unit<?>|targetUnit|the unit to convert the value into"},
                                null,
                                null,
                                new String[]{
                                        "if (targetUnit instanceof OffsetUnit<?>)",
                                        "{",
                                        cg.indent(1)
                                                + "return siValue / targetUnit.getConversionFactorToStandardUnit()",
                                        cg.indent(3) + "+ ((OffsetUnit<?>) targetUnit).getOffsetToStandardUnit();",
                                        "}", "return siValue / targetUnit.getConversionFactorToStandardUnit();"}, false));

        cg.generatePackageInfo("value",
                "Base classes for unit-based 0-d (Scalar), 1-d (Vector) and 2-d (Matrix) values.");
        for (String type : new String[]{"Double", "Float"})
        {
            String packageName = "value.v" + type.toLowerCase();
            cg.generatePackageInfo(packageName, "General classes for " + type + " math, used in " + type
                    + " scalar, vector and matrix.");
            for (String subType : new String[]{"Scalar", "Vector", "Matrix"})
            {
                String subPackageName = packageName + "." + subType.toLowerCase();
                cg.generatePackageInfo(subPackageName, type + " " + subType
                        + " storage and calculations with units, absolute/relative"
                        + (subType.equals("Scalar") ? "" : ", sparse/dense") + ".");
            }
            generateMathFunctions(type, cg);
            generateScalarClass(type, false, cg);
            generateScalarClass(type, true, cg);

            for (int dimensions = 1; dimensions <= 2; dimensions++)
            {
                generateReadOnlyFunctions(type, dimensions, cg);
                generateWriteFunctions(type, dimensions, cg);
                generateVectorOrMatrixClass(type, false, dimensions, cg);
                generateVectorOrMatrixClass(type, true, dimensions, cg);
            }
        }
    }

    /**
     * Write the *MathFunctions and *MathFunctionsImpl classes.
     * @param type String; either <cite>Float</cite>, or <cite>Double</cite>.
     * @param cg CodeGenerator; the code generator
     */
    private static void generateMathFunctions(String type, CodeGenerator cg)
    {
        cg.generateInterface(
                "value.v" + type.toLowerCase(),
                type + "MathFunctions",
                new String[]{"org.opentrafficsim.core.value.MathFunctions"},
                "Force implementation of multiply and divide.",
                null,
                "extends MathFunctions",
                cg.buildMethod(cg.indent(1), "|void|multiply", "Scale the value(s) by a factor.",
                        new String[]{type.toLowerCase() + "|factor|the multiplier"}, null, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|void|divide",
                                "Scale the value(s) by the inverse of a factor; i.e. a divisor.",
                                new String[]{type.toLowerCase() + "|divisor|the divisor"}, null, null, null, false));
        cg.generateFinalClass("value.v" + type.toLowerCase(), type + "MathFunctionsImpl",
                new String[]{"cern.colt.function.t" + type.toLowerCase() + "." + type + "Function"}, type
                        + "Function implementations of the standard Math functions.", null, "",
                buildMathFunctionImpl(type));
    }

    /**
     * @param type String; type of the result of the generated functions
     * @param dimensions int; number of dimensions of the data
     * @param cg CodeGenerator;
     */
    private static void generateWriteFunctions(String type, int dimensions, CodeGenerator cg)
    {
        final String vectorOrMatrix = 1 == dimensions ? "Vector" : "Matrix";
        final String valueException =
                1 == dimensions ? "ValueException|when index out of range (index &lt; 0 or index &gt;= size())"
                        : "ValueException|when row or column out of range (row &lt; 0 or row &gt;= rows() or "
                                + "column &lt; 0 or column\r\n" + cg.indent(1) + " *             &gt;= columns())";
        cg.generateInterface(
                "value.v" + type.toLowerCase() + "." + vectorOrMatrix.toLowerCase(),
                "Write" + type + vectorOrMatrix + "Functions",
                new String[]{"org.opentrafficsim.core.unit.Unit", "org.opentrafficsim.core.value.ValueException",
                        "org.opentrafficsim.core.value.v" + type.toLowerCase() + ".scalar." + type + "Scalar"},
                "Methods that modify the data stored in a " + type + vectorOrMatrix + ".",
                new String[]{"<U> Unit of the " + vectorOrMatrix.toLowerCase()},
                "<U extends Unit<U>>",
                cg.buildMethod(cg.indent(1), "|void|setSI", "Replace the value at "
                        + (1 == dimensions ? "index" : "row, column")
                        + " by the supplied value which is expressed in the standard SI unit.", new String[]{
                        1 == dimensions ? "int|index|index of the value to replace"
                                : "int|row|row of the value to replace",
                        dimensions > 1 ? "int|column|column of the value to replace" : null,
                        type.toLowerCase() + "|valueSI|the value to store (expressed in the standard SI unit)"},
                        valueException, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|void|set", "Replace the value at "
                                + (1 == dimensions ? "index" : "row, column")
                                + " by the supplied value which is in a compatible unit.", new String[]{
                                1 == dimensions ? "int|index|index of the value to replace"
                                        : "int|row|row of the value to replace",
                                dimensions > 1 ? "int|column|column of the value to replace" : null,
                                type + "Scalar<U>|value|the strongly typed value to store"}, valueException, null,
                                null, false)
                        + cg.buildMethod(
                                cg.indent(1),
                                "|void|setInUnit",
                                "Replace the value at " + (1 == dimensions ? "index" : "row, column")
                                        + " by the supplied value which is expressed in a "
                                        + "supplied (compatible) unit.",
                                new String[]{
                                        1 == dimensions ? "int|index|index of the value to replace"
                                                : "int|row|row of the value to replace",
                                        dimensions > 1 ? "int|column|column of the value to replace" : null,
                                        type.toLowerCase()
                                                + "|value|the value to store (which is expressed in valueUnit)",
                                        "U|valueUnit|unit of the supplied value"}, valueException, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|void|normalize",
                                "Normalize the " + vectorOrMatrix.toLowerCase()
                                        + ", i.e. scale the values to make the sum equal to 1.", null,
                                "ValueException|when the sum of the values is zero and normalization is not possible",
                                null, null, false));
    }

    /**
     * @param type String; type of the result of the generated functions
     * @param dimensions int; number of dimensions of the data
     * @param cg CodeGenerator; the code generator
     */
    private static void generateReadOnlyFunctions(String type, int dimensions, CodeGenerator cg)
    {
        final String vectorOrMatrix = 1 == dimensions ? "Vector" : "Matrix";
        final String valueException =
                1 == dimensions ? "ValueException|when index out of range (index &lt; 0 or index &gt;= size())"
                        : "ValueException|when row or column out of range (row &lt; 0 or row &gt;= rows() or "
                                + "column &lt; 0 or column\r\n" + cg.indent(1) + " *             &gt;= columns())";
        cg.generateInterface(
                "value.v" + type.toLowerCase() + "." + vectorOrMatrix.toLowerCase(),
                "ReadOnly" + type + vectorOrMatrix + "Functions",
                new String[]{"org.opentrafficsim.core.unit.Unit", "org.opentrafficsim.core.value.ValueException",
                        "org.opentrafficsim.core.value.v" + type.toLowerCase() + ".scalar." + type + "Scalar"},
                "Methods that operate on " + type + vectorOrMatrix + " but do not modify the contents of the " + type
                        + vectorOrMatrix + ".",
                new String[]{"<U> Unit of the " + vectorOrMatrix.toLowerCase()},
                "<U extends Unit<U>>",
                (1 == dimensions ? cg.buildMethod(cg.indent(1), "|int|size|the size of the vector",
                        "Retrieve the size of the vector.", null, null, null, null, false) : cg.buildMethod(
                        cg.indent(1), "|int|rows|the number of rows of the matrix",
                        "Retrieve the number of rows of the matrix.", null, null, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|int|columns|the number of columns of the matrix",
                                "Retrieve the number of columns of the matrix.", null, null, null, null, false))
                        + cg.buildMethod(cg.indent(1), "|int|cardinality|the number of cells having non-zero value",
                                "Count the number of cells that have a non-zero value (ignores tolerance).", null,
                                null, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|" + type.toLowerCase() + "|getSI|value at position "
                                + (1 == dimensions ? "index" : "row, column") + " in the standard SI unit",
                                "Retrieve the value stored at a specified "
                                        + (1 == dimensions ? "position" : "row and column")
                                        + " in the standard SI unit.", 1 == dimensions
                                        ? new String[]{"int|index|index of the value to retrieve"} : new String[]{
                                                "int|row|row of the value to retrieve",
                                                "int|column|column of the value to retrieve"}, valueException, null,
                                null, false)
                        + cg.buildMethod(cg.indent(1), "|" + type.toLowerCase() + "|getInUnit|value at position "
                                + (1 == dimensions ? "index" : "row, column") + " in the original unit",
                                "Retrieve the value stored at a specified "
                                        + (1 == dimensions ? "position" : "row and column") + " in the original unit.",
                                new String[]{
                                        1 == dimensions ? "int|index|index of the value to retrieve"
                                                : "int|row|row of the value to retrieve",
                                        dimensions > 1 ? "int|column|column of the value to retrieve" : null},
                                valueException, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|" + type.toLowerCase() + "|getInUnit|value at position "
                                + (1 == dimensions ? "index" : "row, column") + " converted into the specified unit",
                                "Retrieve the value stored at a specified "
                                        + (1 == dimensions ? "position" : "row and column")
                                        + " converted into a specified unit.", new String[]{
                                        1 == dimensions ? "int|index|index of the value to retrieve"
                                                : "int|row|row of the value to retrieve",
                                        dimensions > 1 ? "int|column|column of the value to retrieve" : null,
                                        "U|targetUnit|the unit for the result"}, valueException, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|" + type
                                + "Scalar<U>|get|the strongly typed value of the selected cell",
                                "Retrieve the value stored at a specified "
                                        + (1 == dimensions ? "index" : "row and column") + " as a " + type + "Scalar.",
                                new String[]{
                                        1 == dimensions ? "int|index|index of the value to retrieve"
                                                : "int|row|row of the value to retrieve",
                                        dimensions > 1 ? "int|column|column of the value to retrieve" : null},
                                valueException, null, null, false)
                        + cg.buildMethod(cg.indent(1), "|" + type.toLowerCase()
                                + "|zSum|the sum of all values of this " + vectorOrMatrix.toLowerCase(),
                                "Compute the sum of all values of this " + vectorOrMatrix.toLowerCase() + ".", null,
                                null, null, null, false)
                        + (2 == dimensions ? cg.buildMethod(cg.indent(1), "|" + type.toLowerCase()
                                + "|det|the determinant of the matrix", "Compute the determinant of the matrix.", null,
                                "ValueException|when matrix is neither sparse, nor dense, or not square", null, null,
                                false) : ""));
    }

    /**
     * Generate the Java code for *MathFunctionsImpl.
     * @param type String; type of the result of the generated functions
     * @return String; Java code
     */
    private static String buildMathFunctionImpl(String type)
    {
        String useCast = (type.startsWith("D") ? "" : "(" + type.toLowerCase() + ") "); // append a space
        StringBuilder construction = new StringBuilder();
        for (MathFunctionEntry mfu : mathFunctions)
        {
            if (mfu.appearsInMathFunctionsImpl)
            {
                construction.append("    /**\r\n     * Function that returns <tt>Math." + mfu.name
                        + "(a)</tt>.\r\n     */\r\n" + "    public static final " + type + "Function " + mfu.name
                        + " = new " + type + "Function()\r\n    {\r\n" + "        @Override\r\n        public "
                        + type.toLowerCase() + " apply(final " + type.toLowerCase() + " a)\r\n"
                        + "        {\r\n            return " + useCast + "Math." + mfu.name + "(a);\r\n"
                        + "        }\r\n    };\r\n\r\n");
            }
        }
        return construction.toString();
    }

    /**
     * Generate the Java code that declares all the math functions
     * @param cg CodeGenerator; the code generator
     * @return String; Java code
     */
    private static String buildAllMathFunctions(CodeGenerator cg)
    {
        StringBuilder construction = new StringBuilder();
        for (MathFunctionEntry mfu : mathFunctions)
        {
            construction.append(cg.buildMethod(cg.indent(1), "|void|" + mfu.name, mfu.description, null == mfu.argument
                    ? new String[]{} : new String[]{mfu.argument}, null, null, null, false));
        }
        return construction.toString();
    }

    /**
     * Generate a class file for a vector or matrix type.
     * @param type String; must be <cite>Float</cite>, or <cite>Double</cite> (starting with a capital latter)
     * @param mutable boolean; if true the mutable class is generated; of false the immutable class is generated
     * @param dimensions int; number of dimensions of the data (1: vector; 2: matrix)
     * @param cg CodeGenerator; the code generator
     */
    private static void generateVectorOrMatrixClass(String type, boolean mutable, int dimensions, CodeGenerator cg)
    {
        final String outerIndent = cg.indent(1);
        final String mutableType = mutable ? "Mutable" : "Immutable ";
        final String vectorOrMatrix = dimensions == 1 ? "Vector" : "Matrix";
        final String pluralAggregateType = dimensions == 1 ? "vectors" : "matrices";
        final String emptyBrackets = buildEmptyBrackets(dimensions);
        final String ots = "org.opentrafficsim.core.";
        final String cc = "cern.colt.matrix.";
        ArrayList<String> imports = new ArrayList<String>();
        if (!mutable)
        {
            imports.add("java.io.Serializable");
            imports.add("");
        }
        if (2 == dimensions || mutable)
        {
            imports.add(ots + "unit.SICoefficients");
            imports.add("org.opentrafficsim.core.unit.SIUnit");
        }
        imports.add(ots + "unit.Unit");
        imports.add(ots + "value.Absolute");
        if (!mutable)
        {
            imports.add(ots + "value.AbstractValue");
        }
        imports.add(ots + "value.DenseData");
        if (!mutable)
        {
            imports.add(ots + "value.Format");
        }
        imports.add(ots + "value.Relative");
        imports.add(ots + "value.SparseData");
        imports.add(ots + "value.ValueException");
        if (mutable)
        {
            imports.add(ots + "value.v" + type.toLowerCase() + "." + type + "MathFunctions");
            imports.add(ots + "value.v" + type.toLowerCase() + "." + type + "MathFunctionsImpl");
        }
        if (!mutable)
        {
            imports.add(ots + "value.ValueUtil");
        }
        imports.add(ots + "value.v" + type.toLowerCase() + ".scalar." + type + "Scalar");
        if (2 == dimensions && !mutable)
        {
            imports.add(ots + "value.v" + type.toLowerCase() + ".vector." + type + "Vector");
        }
        imports.add("");
        if (1 == dimensions || !mutable)
        {
            imports.add(cc + "t" + type.toLowerCase() + "." + type + "Matrix1D");
        }
        if (2 == dimensions)
        {
            imports.add(cc + "t" + type.toLowerCase() + "." + type + "Matrix2D");
        }
        if (2 == dimensions && !mutable)
        {
            imports.add(cc + "t" + type.toLowerCase() + ".algo.Dense" + type + "Algebra");
            imports.add(cc + "t" + type.toLowerCase() + ".algo.Sparse" + type + "Algebra");
        }
        if (mutable)
        {
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Dense" + type + "Matrix" + dimensions + "D");
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Sparse" + type + "Matrix" + dimensions + "D");
        }
        if (1 == dimensions && !mutable)
        {
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Dense" + type + "Matrix1D");
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Sparse" + type + "Matrix1D");
        }
        if (2 == dimensions && !mutable)
        {
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Dense" + type + "Matrix1D");
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Dense" + type + "Matrix2D");
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Sparse" + type + "Matrix1D");
            imports.add(cc + "t" + type.toLowerCase() + ".impl.Sparse" + type + "Matrix2D");
        }
        if (mutable)
        {
            imports.add("cern.jet.math.t" + type.toLowerCase() + "." + type + "Functions");
        }

        cg.generateAbstractClass(
                "value.v" + type.toLowerCase() + "." + vectorOrMatrix.toLowerCase(),
                (mutable ? "Mutable" : "") + type + vectorOrMatrix,
                arrayListToArray(imports),
                mutableType + type + vectorOrMatrix + ".",
                new String[]{"<U> Unit; the unit of this " + (mutable ? "Mutable" : "") + type + vectorOrMatrix},
                "<U extends Unit<U>> extends "
                        + (mutable ? type + vectorOrMatrix : "AbstractValue")
                        + "<U> implements "
                        + (mutable ? "\r\n" + cg.indent(2) + "Write" + type + vectorOrMatrix + "Functions<U>, " + type
                                + "MathFunctions" : "Serializable,\r\n" + cg.indent(1) + "ReadOnly" + type
                                + vectorOrMatrix + "Functions<U>"),
                mutable
                        ? cg.buildMethod(outerIndent, "protected||Mutable" + type + vectorOrMatrix,
                                "Construct a new Mutable" + type + vectorOrMatrix + ".",
                                new String[]{"final U|unit|the unit of the new Mutable" + type + vectorOrMatrix}, null,
                                null, new String[]{"super(unit);",
                                        "// System.out.println(\"Created Mutable" + type + vectorOrMatrix + "\");"},
                                true)
                                + cg.buildField(outerIndent, "private boolean copyOnWrite = false",
                                        "If set, any modification of the data must be preceded by replacing the data "
                                                + "with a local copy.")
                                + cg.buildMethod(outerIndent, "private|boolean|isCopyOnWrite",
                                        "Retrieve the value of the copyOnWrite flag.", null, null, null,
                                        new String[]{"return this.copyOnWrite;"}, false)
                                + cg.buildMethod(outerIndent, "final|void|setCopyOnWrite",
                                        "Change the copyOnWrite flag.",
                                        new String[]{"final boolean|copyOnWrite|the new value for the copyOnWrite "
                                                + "flag"}, null, null, new String[]{"this.copyOnWrite = copyOnWrite;"},
                                        false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|void|normalize",
                                        null,
                                        null,
                                        "ValueException|when zSum is 0",
                                        null,
                                        new String[]{
                                                type.toLowerCase() + " sum = zSum();",
                                                "if (0 == sum)",
                                                "{",
                                                cg.indent(1)
                                                        + "throw new ValueException(\"zSum is 0; cannot normalize\");",
                                                "}",
                                                "checkCopyOnWrite();",
                                                1 == dimensions ? "for (int i = 0; i < size(); i++)"
                                                        : "for (int row = rows(); --row >= 0;)",
                                                "{",
                                                dimensions > 1 ? cg.indent(1)
                                                        + "for (int column = columns(); --column >= 0;)" : null,
                                                dimensions > 1 ? cg.indent(1) + "{" : null,
                                                (dimensions > 1 ? cg.indent(1) : "") + cg.indent(1) + "safeSet("
                                                        + (1 == dimensions ? "i" : "row, column") + ", safeGet("
                                                        + (1 == dimensions ? "i" : "row, column") + ") / sum);",
                                                dimensions > 1 ? cg.indent(1) + "}" : null, "}"}, false)
                                + buildSubClass(outerIndent, "Abs", "Absolute " + mutableType + type + vectorOrMatrix,
                                        "Mutable" + type + vectorOrMatrix + "<U>", "Absolute", "Mutable" + type
                                                + vectorOrMatrix, true, dimensions, cg)
                                + buildSubClass(outerIndent, "Rel", "Relative " + mutableType + type + vectorOrMatrix,
                                        "Mutable" + type + vectorOrMatrix + "<U>", "Relative", "Mutable" + type
                                                + vectorOrMatrix, true, dimensions, cg)
                                + cg.buildMethod(outerIndent, "public abstract|" + type + vectorOrMatrix
                                        + "<U>|immutable", "Make (immutable) " + type + vectorOrMatrix
                                        + " equivalent for any type of Mutable" + type + vectorOrMatrix + ".", null,
                                        null, null, null, false)
                                + cg.buildMethod(outerIndent, "public final|Mutable" + type + vectorOrMatrix
                                        + "<U>|copy", null, null, null, null,
                                        new String[]{
                                                "return immutable().mutable();",
                                                "// FIXME: This may cause both the original and the copy to be deep "
                                                        + "copied later",
                                                "// Maybe it is better to make one deep copy now..."}, false)
                                + cg.buildMethod(outerIndent, "protected final|void|checkCopyOnWrite",
                                        "Check the copyOnWrite flag and, if it is set, make a deep copy of the data "
                                                + "and clear the flag.", null, null, null, new String[]{
                                                "if (isCopyOnWrite())",
                                                "{",
                                                cg.indent(1) + "// System.out.println(\"copyOnWrite is set: Copying "
                                                        + "data\");", cg.indent(1) + "deepCopyData();",
                                                cg.indent(1) + "setCopyOnWrite(false);", "}"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|void|setSI",
                                        null,
                                        1 == dimensions ? new String[]{"final int|index|",
                                                "final " + type.toLowerCase() + "|valueSI"} : new String[]{
                                                "final int|row|", "final int|column",
                                                "final " + type.toLowerCase() + "|valueSI"},
                                        "ValueException|when index is / indices are invalid", null, new String[]{
                                                "checkIndex(" + (1 == dimensions ? "index" : "row, column") + ");",
                                                "checkCopyOnWrite();",
                                                "safeSet(" + (1 == dimensions ? "index" : "row, column")
                                                        + ", valueSI);"}, false)
                                + cg.buildMethod(outerIndent, "public final|void|set", null, new String[]{
                                        "final int|" + (1 == dimensions ? "index" : "row") + "|",
                                        dimensions > 1 ? "final int|column|" : null,
                                        "final " + type + "Scalar<U>|value"}, "ValueException|when index is invalid",
                                        null, new String[]{"setSI(" + (1 == dimensions ? "index" : "row, column")
                                                + ", value.getValueSI());"}, false)
                                + cg.buildMethod(outerIndent, "public final|void|setInUnit", null, new String[]{
                                        "final int|" + (1 == dimensions ? "index" : "row") + "|",
                                        dimensions > 1 ? "final int|column|" : null,
                                        "final " + type.toLowerCase() + "|value", "final U|valueUnit|"},
                                        "ValueException|when index is invalid", null, new String[]{
                                                "// TODO: creating a " + type
                                                        + "Scalar.Abs along the way may not be the most efficient "
                                                        + "way to do this...",
                                                "setSI(" + (1 == dimensions ? "index" : "row, column") + ", new "
                                                        + type + "Scalar.Abs<U>(value, valueUnit).getValueSI());"},
                                        false)
                                + cg.buildMethod(outerIndent, "public final|void|assign",
                                        "Execute a function on a cell by cell basis.",
                                        new String[]{"final cern.colt.function.t" + type.toLowerCase() + "." + type
                                                + "Function|" + type.substring(0, 1).toLowerCase()
                                                + "|the function to apply"}, null, null, new String[]{
                                                "checkCopyOnWrite();",
                                                "get" + vectorOrMatrix + "SI().assign("
                                                        + type.substring(0, 1).toLowerCase() + ");"}, false)
                                + buildVectorFunctions(outerIndent, type, cg)
                                + cg.buildMethod(outerIndent, "public final|void|multiply", null, new String[]{"final "
                                        + type.toLowerCase() + "|constant|"}, null, null, new String[]{"assign(" + type
                                        + "Functions.mult(constant));"}, false)
                                + cg.buildMethod(outerIndent, "public final|void|divide", null, new String[]{"final "
                                        + type.toLowerCase() + "|constant|"}, null, null, new String[]{"assign(" + type
                                        + "Functions.div(constant));"}, false)
                                + buildInOrDecrementValueByValue(outerIndent, type, vectorOrMatrix,
                                        pluralAggregateType, dimensions, true, cg)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|Mutable" + type + vectorOrMatrix
                                                + "<U>|incrementBy|this modified Mutable" + type + vectorOrMatrix,
                                        "Increment the values in this Mutable" + type + vectorOrMatrix
                                                + " by the corresponding values in a Relative " + type + vectorOrMatrix
                                                + ".",
                                        new String[]{"final " + type + vectorOrMatrix + ".Rel<U>|rel|the Relative "
                                                + type + vectorOrMatrix},
                                        "ValueException|when the " + pluralAggregateType + " do not have the same size",
                                        null, new String[]{"return incrementValueByValue(rel);"}, false)
                                + buildInOrDecrementValueByValue(outerIndent, type, vectorOrMatrix,
                                        pluralAggregateType, dimensions, false, cg)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|Mutable" + type + vectorOrMatrix
                                                + "<U>|decrementBy|this modified Mutable" + type + vectorOrMatrix,
                                        "Decrement the values in this Mutable" + type + vectorOrMatrix
                                                + " by the corresponding values in a Relative " + type + vectorOrMatrix
                                                + ".",
                                        new String[]{"final " + type + vectorOrMatrix + ".Rel<U>|rel|the Relative "
                                                + type + vectorOrMatrix},
                                        "ValueException|when the " + pluralAggregateType + " do not have the same size",
                                        null, new String[]{"return decrementValueByValue(rel);"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "protected final|Mutable" + type + vectorOrMatrix
                                                + ".Rel<U>|decrementBy|this modified Relative Mutable" + type
                                                + vectorOrMatrix,
                                        "Decrement the values in this Relative Mutable" + type + vectorOrMatrix
                                                + " by the corresponding values in an Absolute"
                                                + (type.startsWith("D") ? "\r\n" + outerIndent + " * " : " ") + type
                                                + vectorOrMatrix + ".",
                                        new String[]{"final " + type + vectorOrMatrix + ".Abs<U>|abs|the Absolute "
                                                + type + vectorOrMatrix},
                                        "ValueException|when the " + pluralAggregateType + " do not have the same size",
                                        null, new String[]{"return (Mutable" + type + vectorOrMatrix
                                                + ".Rel<U>) decrementValueByValue(abs);"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|void|scaleValueByValue",
                                        "Scale the values in this Mutable" + type + vectorOrMatrix
                                                + " by the corresponding values in a " + type + vectorOrMatrix + ".",
                                        new String[]{"final " + type + vectorOrMatrix
                                                + "<?>|factor|contains the values by which to scale the corresponding "
                                                + "values in this Mutable" + type + vectorOrMatrix},
                                        "ValueException|when the " + pluralAggregateType + " do not have the same size",
                                        null,
                                        new String[]{
                                                "checkSizeAndCopyOnWrite(factor);",
                                                1 == dimensions ? "for (int index = size(); --index >= 0;)"
                                                        : "for (int row = rows(); --row >= 0;)",
                                                "{",
                                                dimensions > 1 ? "for (int column = columns(); --column >= 0;)" : null,
                                                dimensions > 1 ? cg.indent(1) + "{" : null,
                                                (dimensions > 1 ? cg.indent(1) : "") + cg.indent(1) + "safeSet("
                                                        + (1 == dimensions ? "index" : "row, column") + ", safeGet("
                                                        + (1 == dimensions ? "index" : "row, column")
                                                        + ") * factor.safeGet("
                                                        + (1 == dimensions ? "index" : "row, column") + "));",
                                                dimensions > 1 ? cg.indent(1) + "}" : null, "}"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|Mutable" + type + vectorOrMatrix
                                                + "<U>|scaleValueByValue|this modified Mutable" + type + vectorOrMatrix,
                                        "Scale the values in this Mutable" + type + vectorOrMatrix
                                                + " by the corresponding values in a " + type.toLowerCase() + " array.",
                                        new String[]{"final " + type.toLowerCase() + emptyBrackets
                                                + "|factor|contains the values by which to scale the corresponding "
                                                + "values in this Mutable" + type + vectorOrMatrix},
                                        "ValueException|when the " + vectorOrMatrix.toLowerCase()
                                                + " and the array do not have the same size",
                                        null,
                                        new String[]{
                                                "checkSizeAndCopyOnWrite(factor);",
                                                1 == dimensions ? "for (int index = size(); --index >= 0;)"
                                                        : "for (int row = rows(); --row >= 0;)",
                                                "{",
                                                dimensions > 1 ? "for (int column = columns(); --column >= 0;)" : null,
                                                dimensions > 1 ? cg.indent(1) + "{" : null,
                                                (dimensions > 1 ? cg.indent(1) : "") + cg.indent(1) + "safeSet("
                                                        + (1 == dimensions ? "index" : "row, column") + ", safeGet("
                                                        + (1 == dimensions ? "index" : "row, column") + ") * factor["
                                                        + (1 == dimensions ? "index" : "row][column") + "]);",
                                                dimensions > 1 ? cg.indent(1) + "}" : null, "}", "return this;"}, false)
                                + cg.buildMethod(outerIndent, "private|void|checkSizeAndCopyOnWrite",
                                        "Check sizes and copy the data if the copyOnWrite flag is set.",
                                        new String[]{"final " + type + vectorOrMatrix
                                                + "<?>|other|partner for the size check"}, "ValueException|when the "
                                                + pluralAggregateType + " do not have the same size", null,
                                        new String[]{"checkSize(other);", "checkCopyOnWrite();"}, false)
                                + cg.buildMethod(outerIndent, "private|void|checkSizeAndCopyOnWrite",
                                        "Check sizes and copy the data if the copyOnWrite flag is set.",
                                        new String[]{"final " + type.toLowerCase() + emptyBrackets
                                                + "|other|partner for the size check"}, "ValueException|when the "
                                                + pluralAggregateType + " do not have the same size", null,
                                        new String[]{"checkSize(other);", "checkCopyOnWrite();"}, false)
                                // Generate 6 plus methods
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Dense", "Rel", dimensions,
                                        true, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Sparse", "Rel.Dense",
                                        dimensions, true, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Sparse", "Rel.Sparse",
                                        dimensions, true, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Rel.Dense", "Rel", dimensions,
                                        true, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Rel.Sparse", "Rel.Dense",
                                        dimensions, true, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Rel.Sparse", "Rel.Sparse",
                                        dimensions, true, cg)
                                // Generate 9 minus methods
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Dense", "Abs", dimensions,
                                        false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Sparse", "Abs.Sparse",
                                        dimensions, false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Sparse", "Abs.Dense",
                                        dimensions, false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Dense", "Rel", dimensions,
                                        false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Sparse", "Rel.Dense",
                                        dimensions, false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Abs.Sparse", "Rel.Sparse",
                                        dimensions, false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Rel.Dense", "Rel", dimensions,
                                        false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Rel.Sparse", "Rel.Dense",
                                        dimensions, false, cg)
                                + buildVectorOrMatrixPlusOrMinus(outerIndent, type, "Rel.Sparse", "Rel.Sparse",
                                        dimensions, false, cg)
                                // Generate 10 times methods
                                // TODO: Decide if you ever need multiply an Absolute with anything; I don't think so...
                                + buildVectorOrMatrixTimes(outerIndent, type, "Abs.Dense", "Abs.Dense", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Abs.Dense", "Abs.Sparse", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Abs.Sparse", "Abs", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Rel.Dense", "Rel.Dense", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Rel.Dense", "Rel.Sparse", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Rel.Sparse", "Rel", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Abs.Dense", "", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Abs.Sparse", "", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Rel.Dense", "", dimensions, cg)
                                + buildVectorOrMatrixTimes(outerIndent, type, "Rel.Sparse", "", dimensions, cg)
                                + buildPrivateDenseSparseConverter(outerIndent, type, dimensions, true, cg)
                                + buildDenseSparseConverter(outerIndent, type, "Abs", true, dimensions, cg)
                                + buildDenseSparseConverter(outerIndent, type, "Rel", true, dimensions, cg)
                                + buildPrivateDenseSparseConverter(outerIndent, type, dimensions, false, cg)
                                + buildDenseSparseConverter(outerIndent, type, "Abs", false, dimensions, cg)
                                + buildDenseSparseConverter(outerIndent, type, "Rel", false, dimensions, cg)
                        : cg.buildField(
                                outerIndent,
                                "private " + type + "Matrix" + dimensions + "D " + vectorOrMatrix.toLowerCase() + "SI",
                                "\r\n" + outerIndent + " * The internal storage for the "
                                        + vectorOrMatrix.toLowerCase()
                                        + "; internally the values are stored in standard SI unit; storage can be "
                                        + "dense\r\n" + outerIndent + " * or sparse.\r\n" + outerIndent)
                                + cg.buildMethod(outerIndent, "protected||" + (mutable ? "Mutable" : " ") + type
                                        + vectorOrMatrix, "Construct a new " + mutableType + type + vectorOrMatrix
                                        + ".", new String[]{"final U|unit|the unit of the new "
                                        + (mutable ? "Mutable" : "") + type + vectorOrMatrix}, null, null,
                                        new String[]{"super(unit);",
                                                "// System.out.println(\"Created " + type + vectorOrMatrix + "\");"},
                                        true)
                                + buildSubClass(outerIndent, "Abs", "Absolute " + mutableType + type + vectorOrMatrix,
                                        type + vectorOrMatrix + "<U>", "Absolute", type + vectorOrMatrix, false,
                                        dimensions, cg)
                                + buildSubClass(outerIndent, "Rel", "Relative " + mutableType + type + vectorOrMatrix,
                                        type + vectorOrMatrix + "<U>", "Relative", type + vectorOrMatrix, false,
                                        dimensions, cg)
                                + cg.buildMethod(outerIndent, "protected final|" + type + "Matrix" + dimensions
                                        + "D|get" + vectorOrMatrix + "SI|the data in the internal format",
                                        "Retrieve the internal data.", null, null, null, new String[]{"return this."
                                                + vectorOrMatrix.toLowerCase() + "SI;"}, false)
                                + cg.buildMethod(outerIndent, "protected final|void|deepCopyData",
                                        "Make a deep copy of the data (used ONLY in the Mutable" + type
                                                + vectorOrMatrix + " sub class).", null, null, null,
                                        new String[]{"this." + vectorOrMatrix.toLowerCase() + "SI = get"
                                                + vectorOrMatrix
                                                + "SI().copy(); // makes a deep copy, using multithreading"}, false)
                                + cg.buildMethod(outerIndent, "public abstract|Mutable" + type + vectorOrMatrix
                                        + "<U>|mutable|mutable version of this " + type + vectorOrMatrix,
                                        "Create a mutable version of this " + type + vectorOrMatrix + ". <br>\r\n"
                                                + outerIndent
                                                + " * The mutable version is created with a shallow copy of the data "
                                                + "and the internal copyOnWrite flag set. The first\r\n" + outerIndent
                                                + " * operation in the mutable version that modifies the data shall "
                                                + "trigger a deep copy of the data.", null, null, null, null, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "protected final|void|initialize",
                                        "Import the values and convert them into the SI standard unit.",
                                        new String[]{"final " + type.toLowerCase() + emptyBrackets
                                                + "|values|an array of values"},
                                        dimensions > 1 ? "ValueException|when values is not rectangular" : null,
                                        null,
                                        1 == dimensions ? new String[]{
                                                "this.vectorSI = createMatrix1D(values.length);",
                                                "if (getUnit().equals(getUnit().getStandardUnit()))",
                                                "{",
                                                cg.indent(1) + "this." + vectorOrMatrix.toLowerCase()
                                                        + "SI.assign(values);",
                                                "}",
                                                "else",
                                                "{",
                                                cg.indent(1) + "for (int index = values.length; --index >= 0;)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "safeSet(index, "
                                                        + (type.startsWith("F") ? "(float) " : "")
                                                        + "expressAsSIUnit(values[index]));", cg.indent(1) + "}", "}"}
                                                : new String[]{
                                                        "ensureRectangular(values);",
                                                        "this.matrixSI = createMatrix2D(values.length, "
                                                                + "0 == values.length ? 0 : values[0].length);",
                                                        "if (getUnit().equals(getUnit().getStandardUnit()))",
                                                        "{",
                                                        cg.indent(1) + "this.matrixSI.assign(values);",
                                                        "}",
                                                        "else",
                                                        "{",
                                                        cg.indent(1) + "for (int row = values.length; --row >= 0;)",
                                                        cg.indent(1) + "{",
                                                        cg.indent(2) + "for (int column = values[row].length; "
                                                                + "--column >= 0;)",
                                                        cg.indent(2) + "{",
                                                        cg.indent(3) + "safeSet(row, column, "
                                                                + (type.startsWith("F") ? "(float) " : "")
                                                                + "expressAsSIUnit(values[row][column]));",
                                                        cg.indent(2) + "}", cg.indent(1) + "}", "}"}, false)
                                + cg.buildMethod(outerIndent, "protected final|void|initialize",
                                        "Import the values from an existing " + type + "Matrix" + dimensions
                                                + "D. This makes a shallow copy.", new String[]{"final " + type
                                                + "Matrix" + dimensions + "D|values|the values",}, null, null,
                                        new String[]{"this." + vectorOrMatrix.toLowerCase() + "SI = values;"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "protected final|void|initialize",
                                        "Construct the " + vectorOrMatrix.toLowerCase()
                                                + " and store the values in the standard SI unit.",
                                        new String[]{"final " + type + "Scalar<U>" + emptyBrackets + "|values|"
                                                + (dimensions > 1 ? "a " + dimensions + "D " : "an ")
                                                + "array of values"},
                                        1 == dimensions ? "ValueException|when values is empty"
                                                : "ValueException|when values has zero entries, or is not rectangular",
                                        null,
                                        1 == dimensions ? new String[]{
                                                "this." + vectorOrMatrix.toLowerCase()
                                                        + "SI = createMatrix1D(values.length);",
                                                "for (int index = 0; index < values.length; index++)", "{",
                                                cg.indent(1) + "safeSet(index, values[index].getValueSI());",
                                                cg.indent(1) + "}"} : new String[]{
                                                "ensureRectangularAndNonEmpty(values);",
                                                "this.matrixSI = createMatrix2D(values.length, values[0].length);",
                                                "for (int row = values.length; --row >= 0;)",
                                                "{",
                                                cg.indent(1) + "for (int column = values[row].length; --column >= 0;)",
                                                cg.indent(1) + "{",
                                                cg.indent(2)
                                                        + "safeSet(row, column, values[row][column].getValueSI());",
                                                cg.indent(1) + "}", "}"}, false)
                                + cg.buildMethod(outerIndent, "protected abstract|" + type + "Matrix" + dimensions
                                        + "D|createMatrix" + dimensions + "D|an instance of the right type of " + type
                                        + "Matrix" + dimensions + "D (absolute/relative, dense/sparse, etc.)",
                                        "Create storage for the data. <br/>\r\n" + outerIndent
                                                + " * This method must be implemented by each leaf class.",
                                        1 == dimensions ? new String[]{"final int|size|the number of cells in the "
                                                + vectorOrMatrix.toLowerCase()} : new String[]{
                                                "final int|rows|the number of rows in the matrix",
                                                "final int|columns|the number of columns in the matrix"}, null, null,
                                        null, false)
                                + cg.buildMethod(outerIndent, "public final|" + type.toLowerCase() + emptyBrackets
                                        + "|getValuesSI|array of values in the standard SI unit",
                                        "Create a " + type.toLowerCase() + emptyBrackets
                                                + " array filled with the values in the standard SI unit.", null, null,
                                        null, new String[]{"return this." + vectorOrMatrix.toLowerCase()
                                                + "SI.toArray(); // this makes a deep copy"}, false)
                                + cg.buildMethod(outerIndent, "public final|" + type.toLowerCase() + emptyBrackets
                                        + "|getValuesInUnit|the values in the original unit",
                                        "Create a " + type.toLowerCase() + emptyBrackets
                                                + " array filled with the values in the original unit.", null, null,
                                        null, new String[]{"return getValuesInUnit(getUnit());"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|" + type.toLowerCase() + emptyBrackets
                                                + "|getValuesInUnit|the values converted into the specified unit",
                                        "Create a " + type.toLowerCase() + emptyBrackets
                                                + " array filled with the values converted into a specified unit.",
                                        new String[]{"final U|targetUnit|the unit into which the values are converted "
                                                + "for use"},
                                        null,
                                        null,
                                        1 == dimensions ? new String[]{
                                                type.toLowerCase() + "[] values = this.vectorSI.toArray();",
                                                "for (int i = values.length; --i >= 0;)",
                                                "{",
                                                cg.indent(1) + "values[i] = "
                                                        + (type.startsWith("F") ? "(float) " : "")
                                                        + "ValueUtil.expressAsUnit(values[i], targetUnit);", "}",
                                                "return values;"} : new String[]{
                                                type.toLowerCase() + emptyBrackets
                                                        + " values = this.matrixSI.toArray();",
                                                "for (int row = rows(); --row >= 0;)",
                                                "{",
                                                cg.indent(1) + "for (int column = columns(); --column >= 0;)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "values[row][column] = "
                                                        + (type.startsWith("F") ? "(float) " : "")
                                                        + "ValueUtil.expressAsUnit(values[row][column], targetUnit);",
                                                cg.indent(1) + "}", "}", "return values;"}, false)
                                + (1 == dimensions ? cg.buildMethod(outerIndent, "public final|int|size", null, null,
                                        null, null, new String[]{"return (int) this." + vectorOrMatrix.toLowerCase()
                                                + "SI.size();"}, false) : cg.buildMethod(outerIndent,
                                        "public final|int|rows", null, null, null, null, new String[]{"return this."
                                                + vectorOrMatrix.toLowerCase() + "SI.rows();"}, false)
                                        + cg.buildMethod(outerIndent, "public final|int|columns", null, null, null,
                                                null, new String[]{"return this." + vectorOrMatrix.toLowerCase()
                                                        + "SI.columns();"}, false))
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|" + type.toLowerCase() + "|getSI",
                                        null,
                                        1 == dimensions ? new String[]{"final int|index|"} : new String[]{
                                                "final int|row|", "final int|column|"},
                                        "ValueException|",
                                        null,
                                        new String[]{
                                                "checkIndex(" + (1 == dimensions ? "index" : "row, column") + ");",
                                                "return safeGet(" + (1 == dimensions ? "index" : "row, column") + ");"},
                                        false)
                                + cg.buildMethod(outerIndent, "public final|" + type.toLowerCase() + "|getInUnit",
                                        null, 1 == dimensions ? new String[]{"final int|index|"} : new String[]{
                                                "final int|row|", "final int|column|"}, "ValueException", null,
                                        new String[]{"return " + (type.startsWith("F") ? "(float) " : "")
                                                + "expressAsSpecifiedUnit(getSI("
                                                + (1 == dimensions ? "index" : "row, column") + "));"}, false)
                                + cg.buildMethod(outerIndent, "public final|" + type.toLowerCase() + "|getInUnit",
                                        null, 1 == dimensions ? new String[]{"final int|index|", "final U|targetUnit|"}
                                                : new String[]{"final int|row|", "final int|column|",
                                                        "final U|targetUnit|"}, "ValueException", null,
                                        new String[]{"return " + (type.startsWith("F") ? "(float) " : "")
                                                + "ValueUtil.expressAsUnit(getSI("
                                                + (1 == dimensions ? "index" : "row, column") + "), targetUnit);"},
                                        false)
                                + cg.buildMethod(outerIndent, "public final|" + type.toLowerCase() + "|zSum", null,
                                        null, null, null, new String[]{"return this." + vectorOrMatrix.toLowerCase()
                                                + "SI.zSum();"}, false)
                                + cg.buildMethod(outerIndent, "public final|int|cardinality", null, null, null, null,
                                        new String[]{"return this." + vectorOrMatrix.toLowerCase()
                                                + "SI.cardinality();"}, false)
                                + (2 == dimensions ? cg.buildMethod(
                                        outerIndent,
                                        "public final|" + type.toLowerCase() + "|det",
                                        null,
                                        null,
                                        "ValueException|",
                                        null,
                                        new String[]{
                                                "try",
                                                "{",
                                                cg.indent(1) + "if (this instanceof SparseData)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "return new Sparse" + type
                                                        + "Algebra().det(getMatrixSI());",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "if (this instanceof DenseData)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "return new Dense" + type
                                                        + "Algebra().det(getMatrixSI());",
                                                cg.indent(1) + "}",
                                                "throw new ValueException(\"" + type
                                                        + "Matrix.det -- matrix implements neither Sparse nor "
                                                        + "Dense\");",
                                                "}",
                                                "catch (IllegalArgumentException exception)",
                                                "{",
                                                cg.indent(1) + "if (!exception.getMessage().startsWith(\"Matrix "
                                                        + "must be square\"))",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "exception.printStackTrace();",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "throw new ValueException(exception.getMessage()); "
                                                        + "// probably Matrix must be square", cg.indent(1) + "}"},
                                        false) : "")
                                + cg.buildMethod(outerIndent, "public final|String|toString|printable string with the "
                                        + vectorOrMatrix.toLowerCase() + " contents", null, null, null, null,
                                        new String[]{"return toString(getUnit());"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|String|toString|printable string with the "
                                                + vectorOrMatrix.toLowerCase() + " contents",
                                        "Print this " + type + vectorOrMatrix
                                                + " with the values expressed in the specified unit.",
                                        new String[]{"final U|displayUnit|the unit into which the values are "
                                                + "converted for display"},
                                        null,
                                        null,
                                        new String[]{
                                                "StringBuffer buf = new StringBuffer();",
                                                "if (this instanceof Mutable" + type + vectorOrMatrix + ")",
                                                "{",
                                                cg.indent(1) + "buf.append(\"Mutable   \");",
                                                "if (this instanceof Mutable" + type + vectorOrMatrix + ".Abs.Dense)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Abs Dense  \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else if (this instanceof Mutable" + type
                                                        + vectorOrMatrix + ".Rel.Dense)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Rel Dense  \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else if (this instanceof Mutable" + type
                                                        + vectorOrMatrix + ".Abs.Sparse)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Abs Sparse \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else if (this instanceof Mutable" + type
                                                        + vectorOrMatrix + ".Rel.Sparse)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Rel Sparse \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"??? \");",
                                                cg.indent(1) + "}",
                                                "}",
                                                "else",
                                                "{",
                                                cg.indent(1) + "buf.append(\"Immutable \");",
                                                "if (this instanceof " + type + vectorOrMatrix + ".Abs.Dense)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Abs Dense  \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else if (this instanceof " + type + vectorOrMatrix
                                                        + ".Rel.Dense)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Rel Dense  \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else if (this instanceof " + type + vectorOrMatrix
                                                        + ".Abs.Sparse)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Abs Sparse \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else if (this instanceof " + type + vectorOrMatrix
                                                        + ".Rel.Sparse)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"Rel Sparse \");",
                                                cg.indent(1) + "}",
                                                cg.indent(1) + "else",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "buf.append(\"??? \");",
                                                cg.indent(1) + "}",
                                                "}",
                                                "buf.append(\"[\" + displayUnit.getAbbreviation() + \"]\");",
                                                (1 == dimensions ? "for (int i = 0; i < size(); i++)"
                                                        : "for (int row = 0; row < rows(); row++)"),
                                                "{",
                                                (dimensions > 1 ? "buf.append(\"\\r\\n\\t\");" : null),
                                                (dimensions > 1 ? "for (int column = 0; column < columns(); column++)"
                                                        : null),
                                                (dimensions > 1 ? "{" : null),
                                                cg.indent(1) + (dimensions > 1 ? cg.indent(1) : "")
                                                        + (type.startsWith("F") ? "float f = (float) " : "double d = ")
                                                        + "ValueUtil.expressAsUnit(safeGet("
                                                        + (1 == dimensions ? "i" : "row, column") + "), displayUnit);",
                                                cg.indent(1) + (dimensions > 1 ? cg.indent(1) : "")
                                                        + "buf.append(\" \" + Format.format("
                                                        + type.substring(0, 1).toLowerCase() + "));",
                                                (dimensions > 1 ? cg.indent(1) + "}" : null), "}",
                                                "return buf.toString();"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "protected final|void|checkSize",
                                        "Centralized size equality check.",
                                        new String[]{"final " + type + vectorOrMatrix + "<?>|other|other " + type
                                                + vectorOrMatrix},
                                        "ValueException|when " + pluralAggregateType + " have unequal size",
                                        null,
                                        new String[]{
                                                1 == dimensions ? "if (size() != other.size())"
                                                        : "if (rows() != other.rows() || columns() != other.columns())",
                                                "{",
                                                cg.indent(1)
                                                        + "throw new ValueException(\"The "
                                                        + pluralAggregateType
                                                        + " have different sizes: \" + "
                                                        + (1 == dimensions ? "size() + \" != \" + other.size());"
                                                                : "rows() + \"x\" + columns() + \" != \""),
                                                dimensions > 1 ? cg.indent(3)
                                                        + "+ other.rows() + \"x\" + other.columns());" : null, "}"},
                                        false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "protected final|void|checkSize",
                                        "Centralized size equality check.",
                                        new String[]{"final " + type.toLowerCase() + emptyBrackets + "|other|array of "
                                                + type.toLowerCase()},
                                        "ValueException|when " + pluralAggregateType + " have unequal size",
                                        null,
                                        new String[]{
                                                (dimensions > 1 ? "final int otherColumns = 0 == other.length ? 0 : "
                                                        + "other[0].length;" : null),
                                                dimensions == 1 ? "if (size() != other.length)"
                                                        : "if (rows() != other.length || columns() != otherColumns)",
                                                "{",
                                                "throw new ValueException(\"The "
                                                        + vectorOrMatrix.toLowerCase()
                                                        + " and the array have different sizes: \" + "
                                                        + (1 == dimensions ? "size() + \" != \" + other.length);"
                                                                : "rows() + \"x\" + columns()"),
                                                dimensions > 1 ? cg.indent(3)
                                                        + "+ \" != \" + other.length + \"x\" + otherColumns);" : null,
                                                "}", dimensions > 1 ? "ensureRectangular(other);" : null}, false)
                                + (dimensions > 1 ? cg.buildMethod(
                                        outerIndent,
                                        "private static|void|ensureRectangular",
                                        "Check that a 2D array of " + type.toLowerCase()
                                                + " is rectangular; i.e. all rows have the same length.",
                                        new String[]{"final " + type.toLowerCase() + emptyBrackets
                                                + "|values|the 2D array to check"},
                                        "ValueException|when not all rows have the same length",
                                        null,
                                        new String[]{
                                                "for (int row = values.length; --row >= 1;)",
                                                "{",
                                                cg.indent(1) + "if (values[0].length != values[row].length)",
                                                cg.indent(1) + "{",
                                                cg.indent(2) + "throw new ValueException(\"Lengths of rows are "
                                                        + "not all the same\");", cg.indent(1) + "}", "}"}, false)
                                        + cg.buildMethod(outerIndent,
                                                "private static|void|ensureRectangularAndNonEmpty",
                                                "Check that a 2D array of " + type
                                                        + "Scalar&lt;?&gt; is rectangular; i.e. all rows have "
                                                        + "the same length and is non\r\n" + outerIndent + " * empty.",
                                                new String[]{"final " + type + "Scalar<?>" + emptyBrackets
                                                        + "|values|the 2D array to check"},
                                                "ValueException|when values is not rectangular, or contains "
                                                        + "no data", null,
                                                new String[]{
                                                        "if (0 == values.length || 0 == values[0].length)",
                                                        "{",
                                                        cg.indent(1) + "throw new ValueException(\"Cannot "
                                                                + "determine unit for " + type
                                                                + "Matrix from an empty array of " + type + "Scalar"
                                                                + "\");",
                                                        "}",
                                                        "for (int row = values.length; --row >= 1;)",
                                                        "{",
                                                        cg.indent(1) + "if (values[0].length != values[row].length)",
                                                        cg.indent(1) + "{",
                                                        cg.indent(2) + "throw new ValueException(\"Lengths of rows "
                                                                + "are not all the same\");", cg.indent(1) + "}", "}"},
                                                false) : "")
                                + cg.buildMethod(outerIndent, "protected final|void|checkIndex", 1 == dimensions
                                        ? "Check that a provided index is valid."
                                        : "Check that provided row and column indices are valid.", 1 == dimensions
                                        ? new String[]{"final int|index|the value to check"} : new String[]{
                                                "final int|row|the row value to check",
                                                "final int|column|the column value to check"}, 1 == dimensions
                                        ? "ValueException|when index is invalid"
                                        : "ValueException|when row or column is invalid", null, new String[]{
                                        1 == dimensions ? "if (index < 0 || index >= size())"
                                                : "if (row < 0 || row >= rows() || column < 0 || "
                                                        + "column >= columns())",
                                        "{",
                                        cg.indent(1)
                                                + "throw new ValueException(\"index out of range (valid "
                                                + "range is 0..\" + "
                                                + (1 == dimensions ? "(size() - 1) + \", got \" + index + \")\");"
                                                        : "(rows() - 1) + \", 0..\""),

                                        dimensions > 1 ? cg.indent(3)
                                                + "+ (columns() - 1) + \", got \" + row + \", \" + column + \")\");"
                                                : null, "}"}, false)
                                + cg.buildMethod(outerIndent, "protected final|" + type.toLowerCase()
                                        + "|safeGet|the value stored at "
                                        + (1 == dimensions ? "that index" : "the indicated row and column"),
                                        "Retrieve a value in " + vectorOrMatrix.toLowerCase()
                                                + "SI without checking validity of the "
                                                + (1 == dimensions ? "index." : "indices."),
                                        1 == dimensions ? new String[]{"final int|index|the index"} : new String[]{
                                                "final int|row|the row where the value must be retrieved",
                                                "final int|column|the column where the value must be retrieved"}, null,
                                        null, new String[]{"return this." + vectorOrMatrix.toLowerCase()
                                                + "SI.getQuick(" + (1 == dimensions ? "index);" : "row, column);")},
                                        false)
                                + cg.buildMethod(outerIndent, "protected final|void|safeSet", "Modify a value in "
                                        + vectorOrMatrix.toLowerCase() + "SI without checking validity of the "
                                        + (1 == dimensions ? "index." : "indices."), new String[]{
                                        1 == dimensions ? "final int|index|the index"
                                                : "final int|row|the row where the value must be stored",
                                        dimensions > 1 ? "final int|column|the column where the value must be stored"
                                                : null,
                                        "final " + type.toLowerCase() + "|valueSI|the new value for the entry in "
                                                + vectorOrMatrix.toLowerCase() + "SI"}, null, null,
                                        new String[]{"this." + vectorOrMatrix.toLowerCase() + "SI.setQuick("
                                                + (1 == dimensions ? "index, " : "row, column, ") + "valueSI);"}, false)
                                + cg.buildMethod(outerIndent, "protected final|" + type + "Matrix" + dimensions
                                        + "D|deepCopyOfData|deep copy of the data", "Create a deep copy of the data.",
                                        null, null, null, new String[]{"return this." + vectorOrMatrix.toLowerCase()
                                                + "SI.copy();"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "protected static <U extends Unit<U>>|" + type + "Scalar<U>" + emptyBrackets
                                                + "|checkNonEmpty|the provided array",
                                        "Check that a provided array can be used to create some descendant of a "
                                                + type + vectorOrMatrix + ".",
                                        new String[]{
                                                "final " + type + "Scalar<U>" + emptyBrackets + "|"
                                                        + type.substring(0, 1).toLowerCase()
                                                        + "sArray|the provided array",
                                                "Unit|<U>|the unit of the " + type + "Scalar array"},
                                        "ValueException|when the array has "
                                                + (1 == dimensions ? "length equal to 0" : "zero entries"), null,
                                        new String[]{
                                                "if (0 == "
                                                        + type.substring(0, 1).toLowerCase()
                                                        + "sArray.length"
                                                        + (dimensions > 1 ? " || 0 == "
                                                                + type.substring(0, 1).toLowerCase()
                                                                + "sArray[0].length" : "") + ")",
                                                "{",
                                                cg.indent(1) + "throw new ValueException(",
                                                cg.indent(3) + "\"Cannot create a " + type + vectorOrMatrix
                                                        + " or Mutable" + type + vectorOrMatrix
                                                        + " from an empty array of " + type + "Scalar\");", "}",
                                                "return " + type.substring(0, 1).toLowerCase() + "sArray;"}, false)
                                + (2 == dimensions
                                        ? cg.buildMethod(outerIndent, "public static|" + type
                                                + "Vector<SIUnit>|solve|vector x in A*x = b",
                                                "Solve x for A*x = b. According to Colt: x; a new independent matrix; "
                                                        + "solution if A is square, least squares\r\n" + cg.indent(1)
                                                        + " * solution if A.rows() &gt; A.columns(), underdetermined "
                                                        + "system solution if A.rows() &lt; A.columns().",
                                                new String[]{"final " + type + "Matrix<?>|A|matrix A in A*x = b",
                                                        "final " + type + "Vector<?>|b|vector b in A*x = b"},
                                                "ValueException|when matrix A is neither Sparse nor Dense", null,
                                                new String[]{
                                                        "// TODO: is this correct? Should lookup matrix algebra "
                                                                + "to find out unit for x when solving A*x = b ?",
                                                        "SIUnit targetUnit =",
                                                        cg.indent(2) + "Unit.lookupOrCreateSIUnitWithSICoefficients("
                                                                + "SICoefficients.divide(b.getUnit()."
                                                                + "getSICoefficients(),",
                                                        cg.indent(4) + "A.getUnit().getSICoefficients()).toString());",
                                                        "",
                                                        "// TODO: should the algorithm throw an exception when rows/"
                                                                + "columns do not match when solving A*x = b ?",
                                                        type + "Matrix2D A2D = A.getMatrixSI();",
                                                        "if (A instanceof SparseData)",
                                                        "{",
                                                        cg.indent(1) + "Sparse" + type + "Matrix1D b1D = new Sparse"
                                                                + type + "Matrix1D(b.getValuesSI());",
                                                        cg.indent(1) + type + "Matrix1D x1D = new Sparse" + type
                                                                + "Algebra().solve(A2D, b1D);",
                                                        cg.indent(1) + type + "Vector.Abs.Sparse<SIUnit> x = new "
                                                                + type + "Vector.Abs.Sparse<SIUnit>(x1D.toArray(), "
                                                                + "targetUnit);",
                                                        "return x;",
                                                        "}",
                                                        "if (A instanceof DenseData)",
                                                        "{",
                                                        cg.indent(1) + "Dense" + type + "Matrix1D b1D = new Dense"
                                                                + type + "Matrix1D(b.getValuesSI());",
                                                        cg.indent(1) + type + "Matrix1D x1D = new Dense" + type
                                                                + "Algebra().solve(A2D, b1D);",
                                                        cg.indent(1) + type + "Vector.Abs.Dense<SIUnit> x = new "
                                                                + type + "Vector.Abs.Dense<SIUnit>(x1D.toArray(), "
                                                                + "targetUnit);",
                                                        cg.indent(1) + "return x;",
                                                        "}",
                                                        "throw new ValueException(\"" + type
                                                                + "Matrix.det -- matrix implements neither Sparse nor "
                                                                + "Dense\");"}, false) : "")
                                + cg.buildMethod(outerIndent, "public final|int|hashCode", null, null, null, null,
                                        new String[]{
                                                "final int prime = 31;",
                                                "int result = 1;",
                                                "result = prime * result + this." + vectorOrMatrix.toLowerCase()
                                                        + "SI.hashCode();", "return result;"}, false)
                                + cg.buildMethod(
                                        outerIndent,
                                        "public final|boolean|equals",
                                        null,
                                        new String[]{"final Object|obj|"},
                                        null,
                                        null,
                                        new String[]{
                                                "if (this == obj)",
                                                "{",
                                                cg.indent(1) + "return true;",
                                                "}",
                                                "if (obj == null)",
                                                "{",
                                                "return false;",
                                                "}",
                                                "if (!(obj instanceof " + type + vectorOrMatrix + "))",
                                                "{",
                                                cg.indent(1) + "return false;",
                                                "}",
                                                type + vectorOrMatrix + "<?> other = (" + type + vectorOrMatrix
                                                        + "<?>) obj;",
                                                "// unequal if not both absolute or both relative",
                                                "if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != "
                                                        + "other.isRelative())",
                                                "{",
                                                cg.indent(1) + "return false;",
                                                "}",
                                                "// unequal if the standard SI units differ",
                                                "if (!this.getUnit().getStandardUnit().equals(other.getUnit()."
                                                        + "getStandardUnit()))",
                                                "{",
                                                cg.indent(1) + "return false;",
                                                "}",
                                                "// Colt's equals also tests the size of the "
                                                        + vectorOrMatrix.toLowerCase(),
                                                "if (!get" + vectorOrMatrix + "SI().equals(other.get" + vectorOrMatrix
                                                        + "SI()))", "{", cg.indent(1) + "return false;", "}",
                                                "return true;"}, false));
    }

    /**
     * Generate the Jave code for the private makeDense or makeSparse method.
     * @param outerIndent String; prefix for all output lines
     * @param type String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param dimensions int; 1, or 2
     * @param toDense boolean; if true; generate makeDense; if false; generate makeSparse
     * @param cg CodeGenerator; the code generator
     * @return String; Java code
     */
    private static String buildPrivateDenseSparseConverter(String outerIndent, String type, int dimensions,
            boolean toDense, CodeGenerator cg)
    {
        final String resultType = toDense ? "Sparse" : "Dense";
        final String inputType = toDense ? "Dense" : "Sparse";
        return cg.buildMethod(outerIndent, "private static|" + resultType + type + "Matrix" + dimensions + "D|make"
                + resultType, "Make the " + resultType + " equivalent of a " + inputType + type + "Matrix" + dimensions
                + "D.", new String[]{"final " + type + "Matrix" + dimensions + "D|" + inputType.toLowerCase() + "|the "
                + inputType + " " + type + "Matrix" + dimensions + "D"}, null, null, new String[]{
                resultType
                        + type
                        + "Matrix"
                        + dimensions
                        + "D result = new "
                        + resultType
                        + type
                        + "Matrix"
                        + dimensions
                        + "D("
                        + (1 == dimensions ? "(int) " + inputType.toLowerCase() + ".size()" : inputType.toLowerCase()
                                + ".rows(), " + inputType.toLowerCase() + ".columns()") + ");",
                "result.assign(" + inputType.toLowerCase() + ");", "return result;"}, false);
    }

    /**
     * Build Java code for incrementValueByValue or decrementValueByValue
     * @param outerIndent String; prefix for all output lines
     * @param type String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param aggregateType String; either <cite>Vector</cite>, or <cite>Matrix</cite>
     * @param pluralAggregateType String; either <cite>vectors</cite>, or <cite>matrices</cite>
     * @param dimensions int; 1, or 2
     * @param increment boolean; if true; the increment method is built; if false; the decrement method is built
     * @param cg CodeGenerator; the code generator
     * @return String; Java code
     */
    private static String buildInOrDecrementValueByValue(String outerIndent, String type, String aggregateType,
            String pluralAggregateType, int dimensions, boolean increment, CodeGenerator cg)
    {
        final String inOrDecrement = increment ? "in" : "de";
        return cg.buildMethod(outerIndent, "private|Mutable" + type + aggregateType + "<U>|" + inOrDecrement
                + "crementValueByValue|this modified Mutable" + type + aggregateType, (increment ? "In" : "De")
                + "crement the values in this Mutable" + type + aggregateType + " by the corresponding values in a "
                + type + aggregateType + ".", new String[]{"final " + type + aggregateType + "<U>|" + inOrDecrement
                + "crement|the values by which to " + inOrDecrement
                + "crement the corresponding values in this Mutable" + type + aggregateType},
                "ValueException|when the " + pluralAggregateType + " do not have the same size", null, new String[]{
                        "checkSizeAndCopyOnWrite(" + inOrDecrement + "crement);",
                        1 == dimensions ? "for (int index = size(); --index >= 0;)"
                                : "for (int row = rows(); --row >= 0;)",
                        "{",
                        dimensions > 1 ? cg.indent(1) + "for (int column = columns(); --column >= 0;)" : null,
                        dimensions > 1 ? cg.indent(1) + "{" : null,
                        (dimensions > 1 ? cg.indent(1) : "") + cg.indent(1) + "safeSet("
                                + (1 == dimensions ? "index" : "row, column") + ", safeGet("
                                + (1 == dimensions ? "index" : "row, column") + ") " + (increment ? "+" : "-") + " "
                                + inOrDecrement + "crement.safeGet(" + (1 == dimensions ? "index" : "row, column")
                                + "));", dimensions > 1 ? cg.indent(1) + "}" : null, "}", "return this;"}, false);
    }

    /**
     * Generate java code for a denseToSparse vector method.
     * @param outerIndent String; prefix for all output lines
     * @param type String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param absRel String; either <cite>Abs</cite>, or <cite>Rel</cite>
     * @param toSparse boolean; if true; code for denseToSparse is generated; if false; code for sparseToDense is
     *            generated
     * @param dimensions int; number of dimensions of the storage
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildDenseSparseConverter(String outerIndent, String type, String absRel, boolean toSparse,
            int dimensions, CodeGenerator cg)
    {
        final String from = toSparse ? "Dense" : "Sparse";
        final String to = toSparse ? "Sparse" : "Dense";
        final String vectorOrMatrix = 1 == dimensions ? "Vector" : "Matrix";
        return cg.buildMethod(outerIndent, "public static <U extends Unit<U>>|Mutable" + type + vectorOrMatrix + "."
                + absRel + "." + to + "<U>|" + from.toLowerCase() + "To" + to, "Create a " + to + " version of a "
                + from + " " + type + vectorOrMatrix + ".", new String[]{
                "final " + type + vectorOrMatrix + "." + absRel + "." + from + "<U>|in|the " + from + " " + type
                        + vectorOrMatrix, "Unit|<U>|the unit of the parameter and the result"}, null, null,
                new String[]{"return new Mutable" + type + vectorOrMatrix + "." + absRel + "." + to + "<U>(make" + to
                        + "(in.get" + vectorOrMatrix + "SI()), in.getUnit());"}, false);
    }

    /**
     * Generate the code for the Vector times methods
     * @param outerIndent String; prefix of all output line
     * @param type String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param leftType String; type of the left operand
     * @param rightType String; type of the right operand
     * @param dimensions int; number of dimensions of the data
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildVectorOrMatrixTimes(String outerIndent, String type, String leftType, String rightType,
            int dimensions, CodeGenerator cg)
    {
        final String resultType =
                leftType.contains("Sparse") ? leftType : rightType.contains("Sparse") ? leftType.replace("Dense",
                        "Sparse") : leftType;
        final String vectorOrMatrix = 1 == dimensions ? "Vector" : "Matrix";
        final String pluralVectorOrMatrix = 1 == dimensions ? "Vectors" : "Matrices";
        final String paramUnit = rightType.length() == 0 ? "U" : "?";
        ArrayList<String> code = new ArrayList<String>();
        if (rightType.length() > 0)
        {
            code.add("SIUnit targetUnit =");
            code.add(cg.indent(2)
                    + "Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients.multiply(left.getUnit()."
                    + "getSICoefficients(),");
            code.add(cg.indent(4) + "right.getUnit().getSICoefficients()).toString());");
            code.add("Mutable" + type + vectorOrMatrix + "." + resultType + "<SIUnit> work =");
            code.add(cg.indent(2) + "new Mutable" + type + vectorOrMatrix + "." + resultType
                    + "<SIUnit>(left.deepCopyOfData(), targetUnit);");
            code.add("work.scaleValueByValue(right);");
            code.add("return work;");
        }
        else
        {
            code.add("return (Mutable" + type + vectorOrMatrix + "." + leftType
                    + "<U>) left.mutable().scaleValueByValue(right);");
        }
        ArrayList<String> params = new ArrayList<String>();
        params.add("final " + type + vectorOrMatrix + "." + leftType + "<" + paramUnit + ">|left|the "
                + (rightType.length() == 0 ? type + vectorOrMatrix : "left operand"));
        params.add("final "
                + (rightType.length() == 0 ? type.toLowerCase() + buildEmptyBrackets(dimensions) : type
                        + vectorOrMatrix + "." + rightType + "<" + paramUnit + ">") + "|right|the "
                + (rightType.length() == 0 ? type.toLowerCase() + " array" : "right operand"));
        if (rightType.length() == 0)
        {
            params.add("Unit|<U>|the unit of the left parameter and the result");
        }

        return cg.buildMethod(
                outerIndent,
                "public static" + (rightType.length() == 0 ? " <U extends Unit<U>>" : "") + "|Mutable" + type
                        + vectorOrMatrix + "." + resultType + "<" + (rightType.length() == 0 ? "U" : "SIUnit")
                        + ">|times",
                (rightType.length() == 0 ? "Multiply the values in a " + type + vectorOrMatrix + " and a "
                        + type.toLowerCase() + " array" : "Multiply two " + type + pluralVectorOrMatrix)
                        + " value by value and store the result in a new\r\n"
                        + outerIndent
                        + " * Mutable"
                        + type
                        + vectorOrMatrix
                        + "."
                        + resultType
                        + "&lt;"
                        + (rightType.length() == 0 ? "U" : "SIUnit")
                        + "&gt;.",
                arrayListToArray(params),
                "ValueException|when the "
                        + (rightType.length() == 0 ? type + vectorOrMatrix + " and the array" : pluralVectorOrMatrix
                                .toLowerCase()) + " do not have the same size", null, arrayListToArray(code), false);
    }

    /**
     * Build a vector plus or minus method.
     * @param outerIndent String; prefix for all output lines
     * @param type String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param leftType String; type of the left operand
     * @param rightType String; type of the right operand
     * @param dimensions int; number of dimensions of the data
     * @param makePlus boolean; if true; generate code for plus; if false; generate code for minus
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildVectorOrMatrixPlusOrMinus(String outerIndent, String type, String leftType,
            String rightType, int dimensions, boolean makePlus, CodeGenerator cg)
    {
        // If either type is Dense, the result is Dense
        final String resultDenseSparse = leftType.contains("Dense") || rightType.contains("Dense") ? "Dense" : "Sparse";
        final String resultAbsRel = leftType.contains("Abs") ? (rightType.contains("Abs") ? "Rel" : "Abs") : "Rel";
        final String vectorOrMatrix = 1 == dimensions ? "Vector" : "Matrix";
        final String castCode =
                "(Mutable" + type + vectorOrMatrix + "." + resultAbsRel + "." + resultDenseSparse + "<U>) ";
        final String operation = makePlus ? "incrementBy" : "decrementBy";
        final boolean absAbsToRel = leftType.contains("Abs") && rightType.contains("Abs");
        final String pluralVectorOrMatrix = 1 == dimensions ? "Vectors" : "Matrices";
        ArrayList<String> code = new ArrayList<String>();
        if (absAbsToRel)
        {
            code.add("return " + castCode + "new Mutable" + type + vectorOrMatrix + ".Rel." + resultDenseSparse
                    + "<U>(left.deepCopyOfData(),");
            code.add(outerIndent + cg.indent(2) + "left.getUnit())." + operation + "(right);");
        }
        else if (resultDenseSparse.equals("Dense") && leftType.contains("Dense") || resultDenseSparse.equals("Sparse")
                && leftType.contains("Sparse"))
        {
            code.add("return " + castCode + "left.mutable()." + operation + "(right);");
        }
        else
        {
            // Need a sparseToDense conversion; this makes a deep copy
            code.add("return " + castCode + "sparseToDense(left)." + operation + "(right);");
        }
        return cg.buildMethod(outerIndent, "public static <U extends Unit<U>>|Mutable" + type + vectorOrMatrix + "."
                + resultAbsRel + "." + resultDenseSparse + "<U>|" + (makePlus ? "plus" : "minus"), (makePlus ? "Add"
                : "Subtract")
                + " two "
                + type
                + pluralVectorOrMatrix
                + " value by value and store the result in a new Mutable"
                + type
                + vectorOrMatrix
                + "."
                + resultAbsRel
                + "." + resultDenseSparse + "&lt;U&gt;.", new String[]{
                "final " + type + vectorOrMatrix + "." + leftType + "<U>|left|the left operand",
                "final " + type + vectorOrMatrix + "." + rightType + "<U>|right|the right operand",
                "Unit|<U>|the unit of the parameters and the result"}, "ValueException|when the "
                + pluralVectorOrMatrix.toLowerCase() + " do not have the same size", null, arrayListToArray(code),
                false);
    }

    /**
     * @param code
     * @return
     */
    private static String[] arrayListToArray(ArrayList<String> code)
    {
        String[] codeLines = new String[code.size()];
        for (int line = 0; line < code.size(); line++)
        {
            codeLines[line] = code.get(line);
        }
        return codeLines;
    }

    /**
     * Generate the code for the *Functions in Mutable*Vector.
     * @param indent String; prefix for all output lines
     * @param type String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildVectorFunctions(final String indent, final String type, CodeGenerator cg)
    {
        StringBuilder construction = new StringBuilder();
        for (MathFunctionEntry mfu : mathFunctions)
        {
            construction.append(cg.buildMethod(indent, "public final|void|" + mfu.name, null, null != mfu.argument
                    ? new String[]{"final double" + "|x|"} : null, null, null, new String[]{"assign(" + type
                    + (mfu.appearsInMathFunctionsImpl ? "MathFunctionsImpl." : "Functions.") + mfu.name
                    + (null != mfu.argument ? "(" + (type.startsWith("F") ? "(float) " : "") : "")
                    + (null != mfu.argument ? "x)" : "") + ");"}, false));
        }
        return construction.toString();
    }

    /**
     * Generate the Java code for a sub class of vector or matrix class
     * @param indent String; prefix for each output line
     * @param name String; name of the sub class, e.g. <cite>Abs</cite> or <cite>Rel</cite>
     * @param longName String; full name of the sub class, e.g. <cite>Absolute Immutable FloatVector</cite> or
     *            <cite>Relative Mutable DoubleVector</cite>
     * @param extendsString String; something like <cite>DoubleScalar&lt;U&gt;</cite>
     * @param implementsString String; something like <cite>Absolute, Comparable&lt;Abs&lt;U&gt;&gt;</cite>
     * @param parentClassName String; name of the class that is being sub-classed
     * @param mutable boolean; if true; the class file for the mutable version is generated; if false; the class file
     *            for the immutable version is generated
     * @param dimensions int; number of dimensions of the storage
     * @param cg CodeGenerator; the code generator
     * @return String; Java code implementing the sub class
     */
    private static String buildSubClass(final String indent, final String name, final String longName,
            final String extendsString, final String implementsString, final String parentClassName, boolean mutable,
            int dimensions, CodeGenerator cg)
    {
        final String absRelType = longName.split(" ")[0];
        final String floatType = extendsString.contains("Float") ? "Float" : "Double";
        StringBuilder construction = new StringBuilder();
        construction.append(indent + "/**\r\n" + indent + " * @param <U> Unit\r\n" + indent + " */\r\n");
        construction.append(indent + "public abstract static class " + name + "<U extends Unit<U>> extends "
                + extendsString + " implements " + implementsString + "\r\n" + indent + "{\r\n");
        final String contentIndent = indent + cg.indent(1);
        construction.append(cg.buildSerialVersionUID(contentIndent));
        construction.append(cg.buildMethod(contentIndent, "protected||" + name, "Construct a new " + longName + ".",
                new String[]{"final U|unit|the unit of the new " + longName}, null, null, new String[]{"super(unit);",
                        "// System.out.println(\"Created " + name + "\");"}, true));
        construction.append(buildSubSubClass(contentIndent, absRelType, "Dense", absRelType + " Dense "
                + parentClassName, mutable, dimensions, cg));
        construction.append(buildSubSubClass(contentIndent, absRelType, "Sparse", absRelType + " Sparse "
                + parentClassName, mutable, dimensions, cg));
        construction.append(cg.buildMethod(contentIndent, "public final|" + floatType + "Scalar." + name + "<U>|get",
                null, 1 == dimensions ? new String[]{"final int|index|"} : new String[]{"final int|row|",
                        "final int|column|"}, "ValueException|when index < 0 or index >= size()", null,
                new String[]{"return new " + floatType + "Scalar." + name + "<U>(getInUnit("
                        + (1 == dimensions ? "index" : "row, column") + ", getUnit()), getUnit());"}, false));
        construction.append(indent + "}\r\n\r\n");
        return construction.toString();
    }

    /**
     * Build a string with the specified number of <cite>[]</cite> pairs.
     * @param dimensions int; the number of bracket pairs to concatenate
     * @return String
     */
    private static String buildEmptyBrackets(int dimensions)
    {
        return buildBrackets(dimensions, "");
    }

    /**
     * Build a string with the specified number of <cite>[<b>string</b>]</cite> pairs.
     * @param dimensions int; the number of bracket pairs with contents to concatenate
     * @param contents String; the text that goes between each pair of brackets
     * @return String
     */
    private static String buildBrackets(int dimensions, String contents)
    {
        String result = "";
        for (int i = 0; i < dimensions; i++)
        {
            result += "[" + contents + "]";
        }
        return result;
    }

    /**
     * Generate the Java code for a vector or matrix sub sub class.
     * @param indent String; prefix of all output lines
     * @param absRel String; either <cite>Absolute</cite>, or <cite>Relative</cite>
     * @param denseOrSparse String; either <cite>Dense</cite>, or <cite>Sparse</cite>
     * @param longName String; something like <cite>Absolute Dense Immutable FloatVector</cite>
     * @param mutable boolean; if true; the code for the mutable version is generated; if false; the code for the
     *            immutable version is generated
     * @param dimensions int; number of dimensions of the storage
     * @param cg CodeGenerator; the code generator
     * @return String; Java code
     */
    private static String buildSubSubClass(final String indent, final String absRel, final String denseOrSparse,
            final String longName, boolean mutable, int dimensions, CodeGenerator cg)
    {
        final String fixedLongName = mutable ? longName : longName.replaceFirst("( \\S*$)", " Immutable$1");
        final String type = longName.replaceFirst(".* (.*)(Vector|Matrix)", "$1").replace("Mutable", "");
        final String typeName = longName.replaceFirst(".* (.*)$", "$1");
        final String immutableTypeName = typeName.startsWith("Mutable") ? typeName.substring(7) : typeName;
        final String vectorOrMatrix = dimensions == 1 ? "Vector" : "Matrix";
        final String emptyBrackets = buildEmptyBrackets(dimensions);
        final String zeroBrackets = buildBrackets(dimensions, "0");
        StringBuilder construction = new StringBuilder();
        construction.append(indent + "/**\r\n" + indent + " * @param <U> Unit\r\n" + indent + " */\r\n");
        construction.append(indent + "public static class " + denseOrSparse + "<U extends Unit<U>> extends "
                + longName.split(" ")[0].substring(0, 3) + "<U>" + " implements " + denseOrSparse + "Data\r\n" + indent
                + "{\r\n");
        final String contentIndent = indent + cg.indent(1);
        construction.append(cg.buildSerialVersionUID(contentIndent));
        construction.append(cg.buildMethod(contentIndent, "public||" + denseOrSparse, "Construct a new "
                + fixedLongName + ".", new String[]{
                "final " + type.toLowerCase() + emptyBrackets + "|values|the " + (mutable ? "initial " : "")
                        + "values of the entries in the new " + fixedLongName,
                "final U|unit|the unit of the new " + fixedLongName}, dimensions > 1
                ? "ValueException|when values is not rectangular" : null, null, new String[]{"super(unit);",
                "// System.out.println(\"Created " + denseOrSparse + "\");", "initialize(values);"}, true));
        construction.append(cg.buildMethod(contentIndent, "public||" + denseOrSparse, "Construct a new "
                + fixedLongName + ".", new String[]{"final " + type + "Scalar." + absRel.substring(0, 3) + "<U>"
                + emptyBrackets + "|values|the " + (mutable ? "initial " : "") + "values of the entries in the new "
                + fixedLongName}, "ValueException|when values has zero entries"
                + (dimensions > 1 ? ", or is not rectangular" : ""), null, new String[]{
                "super(checkNonEmpty(values)" + zeroBrackets + ".getUnit());",
                "// System.out.println(\"Created " + denseOrSparse + "\");", "initialize(values);"}, true));
        construction.append(cg.buildMethod(contentIndent, "protected||" + denseOrSparse,
                "For package internal use only.", new String[]{
                        "final " + type + "Matrix" + dimensions + "D|values|the " + (mutable ? "initial " : "")
                                + "values of the entries in the new " + fixedLongName,
                        "final U|unit|the unit of the new " + fixedLongName}, null, null, mutable ? new String[]{
                        "super(unit);", "// System.out.println(\"Created " + denseOrSparse + "\");",
                        "setCopyOnWrite(true);", "initialize(values); // shallow copy"} : new String[]{"super(unit);",
                        "// System.out.println(\"Created " + denseOrSparse + "\");",
                        "initialize(values); // shallow copy"}, true));
        if (mutable)
        {
            construction.append(cg.buildMethod(contentIndent,
                    "public final|" + type + vectorOrMatrix + "." + absRel.substring(0, 3) + "." + denseOrSparse
                            + "<U>|immutable", null, null, null, null, new String[]{
                            "setCopyOnWrite(true);",
                            "return new " + type + vectorOrMatrix + "." + absRel.substring(0, 3) + "." + denseOrSparse
                                    + "<U>(get" + vectorOrMatrix + "SI(), getUnit());"}, false));
        }
        construction.append(cg.buildMethod(contentIndent,
                "public final|Mutable" + immutableTypeName + "." + absRel.substring(0, 3) + "." + denseOrSparse
                        + "<U>|mutable", null, null, null, null, mutable ? new String[]{
                        "setCopyOnWrite(true);",
                        "return new Mutable" + immutableTypeName + "." + absRel.substring(0, 3) + "." + denseOrSparse
                                + "<U>(get" + vectorOrMatrix + "SI(), getUnit());"} : new String[]{"return new Mutable"
                        + immutableTypeName + "." + absRel.substring(0, 3) + "." + denseOrSparse + "<U>(get"
                        + vectorOrMatrix + "SI(), getUnit());"}, false));
        construction.append(cg.buildMethod(contentIndent, "protected final|" + type + "Matrix" + dimensions
                + "D|createMatrix" + dimensions + "D", null, 1 == dimensions ? new String[]{"final int|size|"}
                : new String[]{"final int|rows|", "final int|columns|"}, null, null, new String[]{"return new "
                + denseOrSparse + type + "Matrix" + dimensions + "D(" + (1 == dimensions ? "size" : "rows, columns")
                + ");"}, false));
        if (!mutable)
        {
            construction.append(cg.buildMethod(contentIndent,
                    "public final|" + immutableTypeName + "." + absRel.substring(0, 3) + "." + denseOrSparse
                            + "<U>|copy", null, null, null, null, new String[]{"return this; // That was easy..."},
                    false));
        }
        construction.append(indent + "}\r\n\r\n");
        return construction.toString();
    }

    /**
     * Generate a class file for a scalar type.
     * @param type String; must be <cite>Float</cite>, or <cite>Double</cite> (starting with a capital latter)
     * @param mutable boolean; if true the mutable class is generated; of false the immutable class is generated
     * @param cg CodeGenerator; the code generator
     */
    private static void generateScalarClass(String type, boolean mutable, CodeGenerator cg)
    {
        final String lowerCaseType = type.toLowerCase();
        final String outerIndent = cg.indent(1);
        final String cast = type.equals("Double") ? "" : "(float) ";
        final String mutableType = mutable ? "Mutable" : "Immutable ";
        cg.generateAbstractClass(
                "value.v" + lowerCaseType + ".scalar",
                (mutable ? "Mutable" : "") + type + "Scalar",
                mutable ? new String[]{"org.opentrafficsim.core.unit.SICoefficients",
                        "org.opentrafficsim.core.unit.SIUnit", "org.opentrafficsim.core.unit.Unit",
                        "org.opentrafficsim.core.value.Absolute", "org.opentrafficsim.core.value.Relative",
                        "org.opentrafficsim.core.value.ValueUtil",
                        "org.opentrafficsim.core.value.v" + lowerCaseType + "." + type + "MathFunctions"}
                        : new String[]{"org.opentrafficsim.core.unit.Unit", "org.opentrafficsim.core.value.Absolute",
                                "org.opentrafficsim.core.value.Relative", "org.opentrafficsim.core.value.Scalar",
                                "org.opentrafficsim.core.value.ValueUtil"},
                (mutable ? "Mutable" : "Immutable ") + type + "Scalar.",
                new String[]{"<U> the unit of the values in the constructor and for display"},
                "<U extends Unit<U>> extends " + (mutable ? type : "") + "Scalar<U>"
                        + (mutable ? " implements " + type + "MathFunctions" : ""),
                (mutable ? "" : cg.buildField(outerIndent, "private " + lowerCaseType + " valueSI",
                        "The value, stored in the standard SI unit."))
                        + cg.buildMethod(outerIndent, "protected||" + (mutable ? "Mutable" : " ") + type + "Scalar",
                                "Construct a new " + mutableType + type + "Scalar.",
                                new String[]{"final U|unit|the unit of the new " + (mutable ? "Mutable" : "") + type
                                        + "Scalar"}, null, null, new String[]{"super(unit);"}, true)
                        + buildScalarSubClass(outerIndent, "Abs", "Absolute " + mutableType + type + "Scalar", type
                                + "Scalar<U>", "Absolute, Comparable<Abs<U>>", type + "Scalar", mutable, cg)
                        + buildScalarSubClass(outerIndent, "Rel", "Relative " + mutableType + type + "Scalar", type
                                + "Scalar<U>", "Relative, Comparable<Rel<U>>", type + "Scalar", mutable, cg)
                        + (mutable ? cg.buildMethod(outerIndent, "public abstract|" + type
                                + "Scalar<U>|immutable|immutable version of this " + type + "Scalar",
                                "Construct an immutable version of this Mutable" + type + "Scalar. <br>\r\n"
                                        + outerIndent + " * The immutable version is created as a deep copy of this. "
                                        + "Delayed copying is not worthwhile for a Scalar.", null, null, null, null,
                                false)
                                + cg.buildMethod(outerIndent, "final|void|setSI",
                                        "Replace the stored value by the supplied value which is expressed "
                                                + "in the standard SI unit.",
                                        new String[]{"final " + type.toLowerCase()
                                                + "|valueSI|the value to store (value must already be in the "
                                                + "standard SI unit)"}, null, null,
                                        new String[]{"setValueSI(valueSI);"}, false)
                                + cg.buildMethod(outerIndent, "final|void|set|",
                                        "Replace the stored value by the supplied value.", new String[]{"final " + type
                                                + "Scalar<U>|value|the strongly typed value to store"}, null, null,
                                        new String[]{"setValueSI(value.getValueSI());"}, false)
                                + cg.buildMethod(outerIndent, "final|void|setInUnit",
                                        "Replace the stored value by the supplied value which can be "
                                                + "expressed in any compatible unit.",
                                        new String[]{"final " + type.toLowerCase() + "|value|the value to store",
                                                "final U|valueUnit|the unit of the supplied value"}, null, null,
                                        new String[]{"setValueSI(" + (cast.equals("") ? "" : cast + " ")
                                                + "ValueUtil.expressAsSIUnit(value, valueUnit));"}, false)
                                + buildOtherMutatingScalarMethods(outerIndent, type, cg)
                                : cg.buildMethod(outerIndent, "public abstract|Mutable" + type + "Scalar<U>|mutable",
                                        "Create a mutable version of this " + type + "Scalar. <br>\r\n" + outerIndent
                                                + " * The mutable version is created as a deep copy of this. "
                                                + "Delayed copying is not worthwhile for a Scalar.", null, null, null,
                                        null, false)
                                        + cg.buildMethod(outerIndent, "protected final|void|initialize",
                                                "Initialize the valueSI field (performing conversion to the SI "
                                                        + "standard unit if needed).", new String[]{"final "
                                                        + lowerCaseType + "|value|the value in the unit of this "
                                                        + type + "Scalar"}, null, null, new String[]{
                                                        "if (this.getUnit().equals(this.getUnit().getStandardUnit()))",
                                                        "{",
                                                        cg.indent(1) + "this.valueSI = value;",
                                                        "}",
                                                        "else",
                                                        "{",
                                                        cg.indent(1) + "this.valueSI = " + cast
                                                                + "expressAsSIUnit(value);", "}"}, false)
                                        + cg.buildMethod(outerIndent, "protected final|void|initialize",
                                                "Initialize the valueSI field. As the provided value is already in "
                                                        + "the SI standard unit, conversion is never necessary.",
                                                new String[]{"final " + type
                                                        + "Scalar<U>|value|the value to use for initialization"}, null,
                                                null, new String[]{"setValueSI(value.getValueSI());"}, false)
                                        + cg.buildMethod(outerIndent, "public final|" + lowerCaseType + "|getValueSI",
                                                "Retrieve the value in the underlying SI unit.", null, null, null,
                                                new String[]{"return this.valueSI;"}, false)
                                        + cg.buildMethod(outerIndent, "protected final|void|setValueSI",
                                                "Set the value in the underlying SI unit.", new String[]{"final "
                                                        + type.toLowerCase()
                                                        + "|value|the new value in the underlying SI unit"}, null,
                                                null, new String[]{"this.valueSI = value;"}, false)
                                        + cg.buildMethod(outerIndent, "public final|" + type.toLowerCase()
                                                + "|getValueInUnit", "Retrieve the value in the original unit.", null,
                                                null, null, new String[]{"return " + cast
                                                        + "expressAsSpecifiedUnit(this.valueSI);"}, false)
                                        + cg.buildMethod(outerIndent, "public final|" + type.toLowerCase()
                                                + "|getValueInUnit",
                                                "Retrieve the value converted into some specified unit.",
                                                new String[]{"final U|targetUnit|the unit to convert the value into"},
                                                null, null, new String[]{"return " + cast
                                                        + "ValueUtil.expressAsUnit(this.valueSI, targetUnit);"}, false)
                                        + buildNumberMethods(type, cg)));
    }

    /**
     * Generate most of the java code that modifies MutableScalar values.
     * @param indent String; prepended to output lines on the outermost level of the generated code
     * @param type String; either <cite>Float</cite> or <cite>Double</cite>
     * @param cg CodeGenerator; the code generator
     * @return String
     */
    private static String buildOtherMutatingScalarMethods(String indent, String type, CodeGenerator cg)
    {
        final String cast = (type.startsWith("F") ? "(float)" : null);
        StringBuilder construction = new StringBuilder();
        construction.append(cg.buildBlockComment(indent, "NON-STATIC METHODS"));
        construction.append(cg.buildMethod(indent, "public final|void|add", "Add another value to this value. "
                + "Only Relative values are allowed; adding an absolute value to an absolute value\r\n" + indent
                + " * is not allowed. Adding an absolute value to an existing relative value would require the "
                + "result to become\r\n" + indent
                + " * absolute, which is a type change that is impossible. For that operation, use a static method.",
                new String[]{"final " + type + "Scalar.Rel<U>|value|the value to add"}, null, null,
                new String[]{"setValueSI(getValueSI() + value.getValueSI());"}, false));
        construction.append(cg.buildMethod(indent, "public final|void|subtract",
                "Subtract another value from this value. "
                        + "Only relative values are allowed; subtracting an absolute value from a\r\n" + indent
                        + " * relative value is not allowed. Subtracting an absolute value from an existing "
                        + "absolute value would require the\r\n" + indent
                        + " * result to become relative, which is a type change that is impossible. "
                        + "For that operation, use a static method.", new String[]{"final " + type
                        + "Scalar.Rel<U>|value|the value to subtract"}, null, null,
                new String[]{"setValueSI(getValueSI() - value.getValueSI());"}, false));
        construction.append(cg.buildBlockComment(indent, "STATIC METHODS"));
        construction.append(buildScalarIncrementDecrement(indent, type, true, cg));
        construction.append(buildScalarPlus(indent, type, true, cg));
        construction.append(buildScalarPlus(indent, type, false, cg));
        construction.append(buildScalarIncrementDecrement(indent, type, false, cg));
        construction.append(buildScalarMinus(indent, type, true, cg));
        construction.append(buildScalarMinus(indent, type, false, cg));
        // abs minus abs -> rel
        construction.append(cg.buildMethod(indent, "public static <U extends Unit<U>>|Mutable" + type
                + "Scalar.Rel<U>|minus|the difference of the two absolute values as a relative value",
                "Subtract two absolute values. Return a new instance of a relative value of the difference. The unit "
                        + "of the value\r\n" + indent + " * will be the unit of the first argument.", new String[]{
                        "final " + type + "Scalar.Abs<U>|valueAbs1|value 1",
                        "final " + type + "Scalar.Abs<U>|valueAbs2|value 2",
                        "Unit|<U>|the unit of the parameters and the result"}, null, null, new String[]{
                        "Mutable" + type + "Scalar.Rel<U> result = ",
                        indent + indent + "new Mutable" + type
                                + "Scalar.Rel<U>(valueAbs1.getValueInUnit(), valueAbs1.getUnit());",
                        "result.decrementBy(valueAbs2);", "return result;"}, false));
        construction.append(buildScalarMultiplyOrDivide(indent, type, true, true, cg));
        construction.append(buildScalarMultiplyOrDivide(indent, type, false, true, cg));
        construction.append(buildScalarMultiplyOrDivide(indent, type, true, false, cg));
        construction.append(buildScalarMultiplyOrDivide(indent, type, false, false, cg));
        construction.append(cg.buildBlockComment(indent, "MATH METHODS"));
        for (MathFunctionEntry mfe : mathFunctions)
        {
            String code;
            if ("inv".equals(mfe.name))
            {
                code = "setValueSI(1.0" + (type.startsWith("F") ? "f" : "") + " / getValueSI());";
            }
            else
            {
                code =
                        "setValueSI(" + (null != cast && mfe.castToFloatRequired ? cast + " " : "") + "Math."
                                + mfe.name + "(" + "getValueSI()" + (null != mfe.argument ? ", x" : "") + ")" + ");";
            }
            construction.append(cg.buildMethod(indent, "public final|void|" + mfe.name, null, null != mfe.argument
                    ? new String[]{"final double|x|"} : null, null, null, new String[]{
                    null != mfe.toDoText ? "// TODO: " + mfe.toDoText : null, code}, false));
        }
        final String typeCast = (cast == null ? "double" : cast.substring(1, cast.length() - 1));
        construction.append(cg.buildMethod(indent, "public final|void|multiply", null, new String[]{"final " + typeCast
                + "|constant|"}, null, null, new String[]{"setValueSI(getValueSI() * constant);"}, false));
        construction.append(cg.buildMethod(indent, "public final|void|divide", null, new String[]{"final " + typeCast
                + "|constant|"}, null, null, new String[]{"setValueSI(getValueSI() / constant);"}, false));
        return construction.toString();
    }

    /**
     * Generate the code for scalar multiply or divide.
     * @param indent String; prefix for all output lines
     * @param scalarType String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param absolute boolean; if true; the code for handling two absolutes is generated; if false; the code for
     *            handling two relatives is generated
     * @param multiply boolean; if true; the code for multiply is generated; if false; the code for divide is generated
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildScalarMultiplyOrDivide(final String indent, final String scalarType, boolean absolute,
            boolean multiply, CodeGenerator cg)
    {
        final String absRel = absolute ? "Abs" : "Rel";
        return cg.buildMethod(indent, "public static|Mutable" + scalarType + "Scalar." + absRel + "<SIUnit>|"
                + (multiply ? "multiply" : "divide") + "|the " + (multiply ? "product" : "ratio")
                + " of the two values", (multiply ? "Multiply" : "Divide")
                + " two values; the result is a new instance with a different (existing or generated) SI unit.",
                new String[]{"final " + scalarType + "Scalar." + absRel + "<?>|left|the left operand",
                        "final " + scalarType + "Scalar." + absRel + "<?>|right|the right operand"}, null, null,
                new String[]{
                        "SIUnit targetUnit =",
                        indent + indent + "Unit.lookupOrCreateSIUnitWithSICoefficients(SICoefficients."
                                + (multiply ? "multiply" : "divide") + "(left.getUnit().getSICoefficients(),",
                        indent + indent + indent + indent + "right.getUnit().getSICoefficients()).toString());",
                        "return new Mutable" + scalarType + "Scalar." + absRel + "<SIUnit>(left.getValueSI() "
                                + (multiply ? "*" : "/") + " right.getValueSI(), targetUnit);"}, false);
    }

    /**
     * Generate the code for scalar incrementBy and decrementBy.
     * @param indent String; prefix for all output lines
     * @param scalarType String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param increment boolean; if true; the code for incrementBy is generated; if false; the code for decrementBy is
     *            generated
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildScalarIncrementDecrement(String indent, String scalarType, boolean increment,
            CodeGenerator cg)
    {
        return cg.buildMethod(indent, "protected final|" + scalarType + "Scalar<?>|" + (increment ? "in" : "de")
                + "crementBy|the modified Mutable" + scalarType + "Scalar", (increment ? "In" : "De")
                + "crement the stored value by a specified amount.", new String[]{"final " + scalarType + "Scalar<?>|"
                + (increment ? "in" : "de") + "crement|the amount by which to " + (increment ? "in" : "de")
                + "crement the stored value"}, null, null, new String[]{
                "setValueSI(getValueSI() " + (increment ? "+ in" : "- de") + "crement.getValueSI());", "return this;"},
                false);
    }

    /**
     * Build the plus method for adding an array of relative scalars to an absolute or relative scalar.
     * @param indent String; prepended to each line
     * @param scalarType String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param absoluteResult boolean; if true the first operand and the result are absolute; if false, the first operand
     *            and the result are relative
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildScalarPlus(final String indent, final String scalarType, boolean absoluteResult,
            CodeGenerator cg)
    {
        final String absRel = absoluteResult ? "Abs" : "Rel";
        return cg.buildMethod(
                indent,
                "public static <U extends Unit<U>>|Mutable" + scalarType + "Scalar." + absRel
                        + "<U>|plus|the sum of the values as " + (absoluteResult ? "an absolute" : "a relative")
                        + " value",
                absoluteResult
                        ? "Add a number of relative values to an absolute value. Return a new instance of the value. "
                                + "The unit of the return\r\n" + indent
                                + " * value will be the unit of the first argument. "
                                + "Due to type erasure of generics, the method cannot check whether an\r\n" + indent
                                + " * array of arguments submitted to the varargs has a mixed-unit content at runtime."
                        : "Add a number of relative values. Return a new instance of the value. Due to type erasure "
                                + "of generics, the method\r\n" + indent
                                + " * cannot check whether an array of arguments submitted to the varargs has a "
                                + "mixed-unit content at runtime.",
                new String[]{
                        absoluteResult ? "final " + scalarType + "Scalar." + absRel + "<U>|value" + absRel
                                + "|the absolute base value" : "final U|targetUnit| the unit of the sum",
                        "final " + scalarType + "Scalar.Rel<U>...|valuesRel|zero or more relative values to add "
                                + (absoluteResult ? "to the absolute value" : "together"),
                        "Unit|<U>|the unit of the parameters and the result"},
                null,
                "@SafeVarargs",
                new String[]{
                        "Mutable"
                                + scalarType
                                + "Scalar."
                                + absRel
                                + "<U> result = new Mutable"
                                + scalarType
                                + "Scalar."
                                + absRel
                                + "<U>("
                                + (absoluteResult ? "valueAbs);" : "0.0" + (scalarType.startsWith("F") ? "f" : "")
                                        + ", targetUnit);"), "for (" + scalarType + "Scalar.Rel<U> v : valuesRel)",
                        "{", cg.indent(1) + "result.incrementBy(v);", "}", "return result;"}, false);
    }

    /**
     * Build the minus method for adding an array of relative scalars to an absolute or relative scalar.
     * @param indent String; prepended to each line
     * @param scalarType String; either <cite>Float</cite>, or <cite>Double</cite>
     * @param absoluteResult boolean; if true the first operand and the result are absolute; if false, the first operand
     *            and the result are relative
     * @param cg CodeGenerator; the code generator
     * @return String; java code
     */
    private static String buildScalarMinus(final String indent, final String scalarType, boolean absoluteResult,
            CodeGenerator cg)
    {
        final String absRel = absoluteResult ? "Abs" : "Rel";
        return cg.buildMethod(indent, "public static <U extends Unit<U>>|Mutable" + scalarType + "Scalar." + absRel
                + "<U>|minus|the resulting value as " + (absoluteResult ? "an absolute" : "a relative") + " value",
                absoluteResult
                        ? "Subtract a number of relative values from an absolute value. Return a new instance of the "
                                + "value. The unit of the\r\n" + indent
                                + " * return value will be the unit of the first argument. "
                                + "Due to type erasure of generics, the method cannot check\r\n" + indent
                                + " * whether an array of arguments submitted to the varargs has a mixed-unit content "
                                + "at runtime."
                        : "Subtract a number of relative values from a relative value. Return a new instance of the "
                                + "value. The unit of the\r\n" + indent
                                + " * value will be the unit of the first argument. Due to type erasure of generics, "
                                + "the method cannot check whether an\r\n" + indent
                                + " * array of arguments submitted to the varargs has a "
                                + "mixed-unit content at runtime.", new String[]{
                        "final " + scalarType + "Scalar." + absRel + "<U>|value" + absRel + "|the "
                                + (absoluteResult ? "absolute" : "relative") + " base value",
                        "final " + scalarType + "Scalar.Rel<U>...|valuesRel|zero or more relative values to subtract "
                                + (absoluteResult ? "from the absolute value" : "from the first value"),
                        "Unit|<U>|the unit of the parameters and the result"}, null, "@SafeVarargs", new String[]{
                        "Mutable" + scalarType + "Scalar." + absRel + "<U> result = new Mutable" + scalarType
                                + "Scalar." + absRel + "<U>(value" + absRel + ");",
                        "for (" + scalarType + "Scalar.Rel<U> v : valuesRel)", "{",
                        cg.indent(1) + "result.decrementBy(v);", "}", "return result;"}, false);
    }

    /**
     * Generate the Java code that implements the Number methods.
     * @param cg CodeGenerator; the code generator
     * @return
     */
    private static String buildNumberMethods(String type, CodeGenerator cg)
    {
        final String cast = type.equals("Float") ? "" : "(float) ";
        final String lowerCaseType = type.toLowerCase();
        final String indent = cg.indent(1);
        StringBuilder construction = new StringBuilder();
        construction.append(cg.buildBlockComment(indent, "NUMBER METHODS"));
        construction.append(cg.buildMethod(indent, "public final|int|intValue", null, null, null, null,
                new String[]{"return " + (type.equals("Float") ? "" : "(int) ") + "Math.round(this.valueSI);"}, false));
        construction.append(cg.buildMethod(indent, "public final|long|longValue", null, null, null, null,
                new String[]{"return Math.round(this.valueSI);"}, false));
        construction.append(cg.buildMethod(indent, "public final|float|floatValue", null, null, null, null,
                new String[]{"return " + cast + "this.valueSI;"}, false));
        construction.append(cg.buildMethod(indent, "public final|double|doubleValue", null, null, null, null,
                new String[]{"return this.valueSI;"}, false));
        construction.append(cg.buildMethod(indent, "public final|String|toString", null, null, null, null,
                new String[]{"return this.getValueInUnit() + \" \" + this.getUnit().getAbbreviationKey();"}, false));
        construction.append(cg.buildMethod(indent, "public final|int|hashCode", null, null, null, null, type
                .equals("Float") ? new String[]{"final int prime = 31;", "int result = 1;",
                "result = prime * result + Float.floatToIntBits(this.valueSI);", "return result;"} : new String[]{
                "final int prime = 31;", "int result = 1;", "long temp;",
                "temp = Double.doubleToLongBits(this.valueSI);",
                "result = prime * result + (int) (temp ^ (temp >>> 32));", "return result;"}, false));
        construction.append(cg.buildMethod(
                indent,
                "public final|boolean|equals",
                null,
                new String[]{"final Object|obj|the Object to compare with"},
                null,
                null,
                new String[]{
                        "if (this == obj)",
                        "{",
                        cg.indent(1) + "return true;",
                        "}",
                        "if (obj == null)",
                        "{",
                        cg.indent(1) + "return false;",
                        "}",
                        "if (!(obj instanceof " + type + "Scalar))",
                        "{",
                        cg.indent(1) + "return false;",
                        "}",
                        type + "Scalar<?> other = (" + type + "Scalar<?>) obj;",
                        "// unequal if not both Absolute or both Relative",
                        "if (this.isAbsolute() != other.isAbsolute() || this.isRelative() != other.isRelative())",
                        "{",
                        cg.indent(1) + "return false;",
                        "}",
                        "// unequal if the underlying standard SI unit is different",
                        "if (!this.getUnit().getStandardUnit().equals(other.getUnit().getStandardUnit()))",
                        "{",
                        cg.indent(1) + "return false;",
                        "}",
                        "if (" + type + "." + lowerCaseType + (type.equals("Float") ? "ToIntBits" : "ToLongBits")
                                + "(this.valueSI) != " + type + "." + lowerCaseType
                                + (type.equals("Float") ? "ToIntBits" : "ToLongBits") + "(other.valueSI))", "{",
                        cg.indent(1) + "return false;", "}", "return true;"}, false));
        return construction.toString();
    }

    /**
     * Generate the Java code for a sub class of scalar
     * @param indent String; prefix for each output line
     * @param name String; name of the sub class, e.g. <cite>Abs</cite> or <cite>Rel</cite>
     * @param longName String; full name of the sub class, e.g. <cite>Absolute Immutable FloatScalar</cite> or
     *            <cite>Relative Mutable DoubleScalar</cite>
     * @param extendsString String; something like <cite>DoubleScalar&lt;U&gt;</cite>
     * @param implementsString String; something like <cite>Absolute, Comparable&lt;Abs&lt;U&gt;&gt;</cite>
     * @param parentClassName String; name of the class that is being sub-classed
     * @param mutable boolean; if true; the class file for the mutable version is generated; if false; the class file
     *            for the immutable version is generated
     * @param cg CodeGenerator; the code generator
     * @return String; java code implementing the sub class
     */
    private static String buildScalarSubClass(final String indent, final String name, final String longName,
            final String extendsString, final String implementsString, final String parentClassName, boolean mutable,
            CodeGenerator cg)
    {
        final String absRelType = longName.split(" ")[0];
        final String floatType = extendsString.contains("Float") ? "Float" : "Double";
        StringBuilder construction = new StringBuilder();
        construction.append(indent + "/**\r\n" + indent + " * @param <U> Unit\r\n" + indent + " */\r\n");
        construction.append(indent + "public static class " + name + "<U extends Unit<U>> extends "
                + (mutable ? "Mutable" : "") + extendsString + " implements " + implementsString + "\r\n" + indent
                + "{\r\n");
        final String contentIndent = indent + cg.indent(1);
        construction.append(cg.buildSerialVersionUID(contentIndent));
        construction.append(cg.buildMethod(contentIndent, "public||" + name, "Construct a new " + longName + ".",
                new String[]{"final " + floatType.toLowerCase() + "|value|the value of the new " + longName,
                        "final U|unit|the unit of the new " + longName}, null, null, new String[]{"super(unit);",
                        "// System.out.println(\"Created " + name + "\");", "initialize(value);"}, true));
        construction.append(cg.buildMethod(contentIndent, "public||" + name, "Construct a new " + longName
                + " from an existing " + absRelType + " Immutable " + floatType + "Scalar.", new String[]{"final "
                + parentClassName + "." + name + "<U>|value|the reference"}, null, null, new String[]{
                "super(value.getUnit());", "// System.out.println(\"Created " + name + "\");", "initialize(value);"},
                true));
        construction.append(cg.buildMethod(contentIndent, "public||" + name, "Construct a new " + longName
                + " from an existing " + absRelType + " Mutable" + floatType + "Scalar.", new String[]{"final "
                + " Mutable" + floatType + "Scalar." + name + "<U>|value|the reference"}, null, null, new String[]{
                "super(value.getUnit());", "// System.out.println(\"Created " + name + "\");", "initialize(value);"},
                true));
        construction.append(cg.buildMethod(contentIndent, "public final|" + "Mutable" + floatType + "Scalar." + name
                + "<U>|mutable", null, null, null, null, new String[]{"return new Mutable" + floatType + "Scalar."
                + name + "<U>(this);"}, false));
        if (mutable)
        {
            construction.append(cg.buildMethod(contentIndent, "public final|" + parentClassName + "." + name
                    + "<U>|immutable", null, null, null, null, new String[]{"return new " + parentClassName + "."
                    + name + "<U>(this);"}, false));
        }
        construction.append(cg.buildMethod(contentIndent, "public final|int|compareTo", null, new String[]{"final|"
                + name + "<U> o|"}, null, null, new String[]{"return new " + floatType
                + "(getValueSI()).compareTo(o.getValueSI());"}, false));
        construction.append(cg.buildMethod(contentIndent, "public final|" + (mutable ? "Mutable" : "")
                + parentClassName + "." + name + "<U>|copy", null, null, null, null, new String[]{mutable
                ? "return new Mutable" + floatType + "Scalar." + name + "<U>(this);" : "return this;"}, false));
        construction.append(indent + "}\r\n\r\n");
        return construction.toString();
    }

    /**
     * Generate the three format functions for either float or double typed value.
     * @param valueType String; should be <cite>float</cite> or <cite>double</cite>
     * @param cg CodeGenerator; the code generator
     * @return String; java code for the three format functions
     */
    private static String buildFormatMethods(String valueType, CodeGenerator cg)
    {
        return cg.buildMethod(cg.indent(1), "public static|String|format|the formatted floating point value",
                "Format a floating point value.", new String[]{"final " + valueType + "|value|the value to format",
                        "final int|width|the number of characters in the result",
                        "final int|precision|the number of fractional digits in the result"}, null, null, new String[]{
                        "if (0 == value || Math.abs(value) > 0.01 && Math.abs(value) < 999.0)", "{",
                        cg.indent(1) + "return String.format(formatString(width, precision, \"f\"), value);", "}",
                        "return String.format(formatString(width, precision, \"e\"), value);"}, false)
                + cg.buildMethod(cg.indent(1), "public static|String|format|the formatted floating point value",
                        "Format a floating point value.", new String[]{
                                "final " + valueType + "|value|the value to format",
                                "final int|size|the number of characters in the result"}, null, null,
                        new String[]{"return Format.format(value, size, Format.DEFAULTPRECISION);"}, false)
                + cg.buildMethod(cg.indent(1), "public static|String|format|the formatted floating point value",
                        "Format a floating point value.", new String[]{"final " + valueType
                                + "|value|the value to format",}, null, null,
                        new String[]{"return format(value, Format.DEFAULTSIZE, Format.DEFAULTPRECISION);"}, false);
    }

}
