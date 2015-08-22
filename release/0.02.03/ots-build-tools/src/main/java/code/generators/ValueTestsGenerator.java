package code.generators;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 6 okt. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ValueTestsGenerator
{

    /**
     * Generate the test classes for the value classes.
     * @param args String[]; the command line arguments (ignored)
     */
    public static void main(final String[] args)
    {
        Date now = new Date();
        CodeGenerator cg =
                new CodeGenerator("the OpenTrafficSim value test classes generator",
                        "d:\\java\\ots-core\\src\\test\\java\\org\\opentrafficsim\\core\\value",
                        "org.opentrafficsim.core.value", new SimpleDateFormat("dd MMM, yyyy").format(now), new Long(
                                new SimpleDateFormat("yyyyMMdd").format(now)));
        for (String type : new String[]{"Float", "Double"})
        {
            for (int dimensions = 0; dimensions <= 2; dimensions++)
            {
                if (dimensions > 0)
                {
                    generateClass(cg, dimensions, type, "Dense");
                    generateClass(cg, dimensions, type, "Sparse");
                }
                else
                {
                    generateClass(cg, dimensions, type, null);
                }
            }
        }
    }

    /**
     * @param cg CodeGenerator
     * @param dimensions int; number of dimensions of the stored value(s)
     * @param type String; <cite>Float</cite>, or <cite>Double</cite>
     * @param denseness String; <cite>Dense</cite>, <cite>Sparse</cite>, or null
     */
    private static void generateClass(final CodeGenerator cg, final int dimensions, final String type,
            final String denseness)
    {
        final String aggregate = 0 == dimensions ? "Scalar" : 1 == dimensions ? "Vector" : "Matrix";
        final String otscu = "org.opentrafficsim.core.unit.";
        final String pragma = "@SuppressWarnings(\"static-method\")\r\n" + cg.indent(1) + "@Test";
        final String suffix = type.startsWith("F") ? "f" : "";
        final String ds = null == denseness ? "" : denseness;
        final String dotDS = null == denseness ? "" : "." + ds;
        final String zeroArgs = (dimensions > 0 ? "0, " : "") + (dimensions > 1 ? "0, " : "");
        final String zeroArgsNoComma = (dimensions > 0 ? "0" : "") + (dimensions > 1 ? ", 0" : "");
        final String firstIndexName = 1 == dimensions ? "index" : "row";
        final String firstIndexRange = 1 == dimensions ? "size" : "rows";
        StringBuilder code = new StringBuilder();
        code.append(generateInitializer(cg, dimensions, type));
        final String shortName = type.substring(0, 1).toLowerCase() + aggregate.substring(0, 1).toLowerCase();
        code.append(cg.buildMethod(cg.indent(1), "private static|void|checkContentsAndType", "Check that the value"
                + (dimensions > 0 ? "s" : "") + " in a " + type + aggregate + (dimensions > 0 ? "match" : "matches")
                + " the expected value" + (dimensions > 0 ? "s" : "") + ".", new String[]{
                "final " + type + aggregate + "<?>|" + shortName + "|the " + type + aggregate + " to match",
                "final " + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + "|reference|the reference value"
                        + (dimensions > 0 ? "s" : ""),
                "final " + type.toLowerCase() + "|precision|the maximum allowed error",
                "final Unit<?>|u|the expected type",
                "final boolean|expectAbsolute|if true; " + shortName + " should be Absolute; if false; " + shortName
                        + " should be Relative"}, null, null,
                new String[]{
                        "assertTrue(\"" + type + aggregate + " should not be null\", null != " + shortName + ");",
                        0 == dimensions ? "assertEquals(\"Value should match\", reference, " + shortName
                                + ".getInUnit(), precision);" : null,
                        dimensions > 0 ? "for (int " + firstIndexName + " = " + shortName + "." + firstIndexRange
                                + "(); --" + firstIndexName + " >= 0;)" : null,
                        dimensions > 0 ? "{" : null,
                        2 == dimensions ? cg.indent(1) + "for (int column = " + shortName
                                + ".columns(); --column >= 0;)" : null,
                        2 == dimensions ? cg.indent(2) + "{" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "try" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "{" : null,
                        dimensions > 0 ? cg.indent(dimensions + 1) + "assertEquals(\"Value should match\", reference["
                                + firstIndexName + (dimensions == 1 ? "" : "][column") + "], " + shortName
                                + ".getInUnit(" + firstIndexName + (dimensions == 1 ? "" : ", column")
                                + "), precision);" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "}" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "catch (ValueException exception)" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "{" : null,
                        dimensions > 0 ? cg.indent(dimensions + 1) + "fail(\"Unexpected exception\");" : null,
                        dimensions > 1 ? cg.indent(dimensions) + "}" : null,
                        dimensions > 0 ? cg.indent(1) + "}" : null,
                        dimensions > 0 ? "}" : null,
                        "assertEquals(\"Unit should be \" + u.toString(), u, " + shortName + ".getUnit());",
                        "assertTrue(\"Should be \" + (expectAbsolute ? \"Absolute\" : \"Relative\"), ",
                        cg.indent(3) + "expectAbsolute ? " + shortName + ".isAbsolute() : " + shortName
                                + ".isRelative());"}, false));

        for (String absoluteRelative : new String[]{"Absolute", "Relative"})
        {
            final String absRel = absoluteRelative.substring(0, 3);
            for (String mutable : new String[]{"", "Mutable"})
            {
                final String in = cg.indent(dimensions > 1 ? 1 : 0);
                code.append(cg.buildMethod(
                        cg.indent(1),
                        "public final|void|toString" + mutable + absRel + "Test",
                        "Test that the toString method returns something sensible.",
                        null,
                        null,
                        pragma,
                        new String[]{
                                dimensions > 0 ? "try" : null,
                                dimensions > 0 ? "{" : null,
                                in + "TemperatureUnit tempUnit = TemperatureUnit.KELVIN;",
                                in + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " value = "
                                        + makeInitializer(type, dimensions, "38.0", false) + ";",
                                in + mutable + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> "
                                        + shortName + " = new " + mutable + type + aggregate + "." + absRel + dotDS
                                        + "<TemperatureUnit>(value, tempUnit);",
                                in + "String result = " + shortName + ".toString(true, true);",
                                // "System.out.println(result);",
                                in + "assertTrue(\"toString result contains \\\" " + absRel
                                        + " \\\"\", result.contains(\" " + absRel + " \"));",
                                in + "assertTrue(\"toString result contains \\\"K\\\"\", result.contains(\"K\"));",
                                in + "assertTrue(\"toString result starts with \\\"Immutable \\\"\", "
                                        + "result.startsWith(\"" + (mutable.length() > 0 ? "Mutable" : "Immutable")
                                        + "\"));",
                                dimensions > 0 && null != denseness ? in + "assertTrue(\"toString contains \\\""
                                        + denseness + "\\\"\", result.contains(\"" + denseness + "\"));" : null,
                                dimensions > 0 ? "}" : null,
                                dimensions > 0 ? "catch (ValueException ve)" : null,
                                dimensions > 0 ? "{" : null,
                                dimensions > 0 ? cg.indent(1)
                                        + "fail(\"Caught unexpected exception: \" + ve.toString());" : null,
                                dimensions > 0 ? "}" : null}, false));
            }
            final String varName1 = "temperature" + type.substring(0, 1) + aggregate.substring(0, 1).toUpperCase();
            final String varName2 = "temperature2" + type.substring(0, 1) + aggregate.substring(0, 1).toUpperCase();
            final String in = dimensions > 0 ? cg.indent(1) : "";
            code.append(cg.buildMethod(
                    cg.indent(1),
                    "public final|void|basics" + absRel + "Test",
                    "Test constructor, verify the various fields in the constructed objects, test conversions to "
                            + "related units.",
                    null,
                    null,
                    dimensions == 0 ? pragma : "@SuppressWarnings({\"static-method\", \"unchecked\"})\r\n"
                            + cg.indent(1) + "@Test",
                    new String[]{
                            dimensions > 0 ? "try" : null,
                            dimensions > 0 ? "{" : null,
                            in + "TemperatureUnit tempUnit = TemperatureUnit.DEGREE_CELSIUS;",
                            in + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " value = "
                                    + makeInitializer(type, dimensions, "38.0", false) + ";",
                            in + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> " + varName1 + " = new "
                                    + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit>(value, tempUnit);",
                            in + "checkContentsAndType(" + varName1 + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absoluteRelative.startsWith("A") + ");",
                            in + "assertEquals(\"Value in SI is equivalent in Kelvin\", 311.15" + suffix + ", "
                                    + varName1 + ".getSI(" + zeroArgsNoComma + "), 0.05);",
                            in + "assertEquals(\"Value in Fahrenheit\", 100.4" + suffix + ", " + varName1
                                    + ".getInUnit(" + zeroArgs + "TemperatureUnit.DEGREE_FAHRENHEIT), 0.1);",
                            in + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " out = " + varName1 + ".get"
                                    + (dimensions > 0 ? "Values" : "") + "InUnit();",
                            dimensions > 0 ? in + "for (int " + firstIndexName + " = 0; " + firstIndexName
                                    + " < value.length; " + firstIndexName + "++)" : null,
                            dimensions > 0 ? in + "{" : null,
                            dimensions > 1 ? in + cg.indent(1)
                                    + "for (int column = 0; column < value[row].length; column++)" : null,
                            dimensions > 1 ? in + cg.indent(1) + "{" : null,
                            in + cg.indent(dimensions) + "assertEquals(\"Value should match\", value"
                                    + (dimensions > 0 ? "[" + firstIndexName + "]" : "")
                                    + (dimensions > 1 ? "[column]" : "") + ", out"
                                    + (dimensions > 0 ? "[" + firstIndexName + "]" : "")
                                    + (dimensions > 1 ? "[column]" : "") + ", 0.001);",
                            dimensions > 1 ? in + cg.indent(1) + "}" : null,
                            dimensions > 0 ? in + "}" : null,
                            in + "Mutable" + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> m"
                                    + shortName + " = new Mutable" + type + aggregate + "." + absRel + dotDS
                                    + "<TemperatureUnit>(value, tempUnit);",
                            in + "checkContentsAndType(m" + shortName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absoluteRelative.startsWith("A") + ");",
                            in + "m" + shortName + ".setSI(" + zeroArgs + "73);",
                            in + type.toLowerCase() + " safe = value" + cg.buildBrackets(dimensions, "0") + ";",
                            in + "value" + cg.buildBrackets(dimensions, "0")
                                    + " = -200; // Approximate Celsius equivalent of 73 Kelvin",
                            in + "checkContentsAndType(m" + shortName + ", value, 1" + ", tempUnit, "
                                    + absoluteRelative.startsWith("A") + ");",
                            in + "value" + cg.buildBrackets(dimensions, "0") + " = safe; // Restore",
                            in + "m" + shortName + ".set(" + (dimensions > 0 ? "0, " : "")
                                    + (dimensions > 1 ? "0, " : "") + varName1
                                    + (dimensions > 0 ? ".get(0" + (dimensions > 1 ? ", 0)" : ")") : "") + ");",
                            in + "checkContentsAndType(m" + shortName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absoluteRelative.startsWith("A") + ");",
                            in + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> " + varName2 + " = new "
                                    + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit>(" + varName1
                                    + (dimensions > 0 ? ".get" + aggregate + "SI(), TemperatureUnit.KELVIN" : "")
                                    + ");",
                            in + "assertTrue(\"" + varName2 + " should be equal to " + varName1 + "\", " + varName2
                                    + ".equals(" + varName1 + "));",
                            in + "assertTrue(\"Value is " + absoluteRelative + "\", " + varName1 + ".is"
                                    + absoluteRelative + "());",
                            in + "assertFalse(\"Value is not "
                                    + (absoluteRelative.startsWith("A") ? "Relative" : "Absolute") + "\", " + varName1
                                    + ".is" + (absoluteRelative.startsWith("A") ? "Relative" : "Absolute") + "());",
                            in + varName1 + " = new " + type + aggregate + "." + absRel + dotDS
                                    + "<TemperatureUnit>(value, TemperatureUnit.KELVIN);",
                            in + "checkContentsAndType(" + varName1 + ", value, 0.001" + suffix
                                    + ", TemperatureUnit.KELVIN, " + absoluteRelative.startsWith("A") + ");",
                            in + "out = " + varName1 + ".get" + (dimensions > 0 ? "Values" : "") + "SI();",
                            dimensions > 0 ? in + "for (int " + firstIndexName + " = 0; " + firstIndexName
                                    + " < value.length; " + firstIndexName + "++)" : null,
                            dimensions > 0 ? in + "{" : null,
                            dimensions > 1 ? in + cg.indent(1)
                                    + "for (int column = 0; column < value[row].length; column++)" : null,
                            dimensions > 1 ? in + cg.indent(1) + "{" : null,
                            in + cg.indent(dimensions) + "assertEquals(\"Value should match\", value"
                                    + (dimensions > 0 ? "[" + firstIndexName + "]" : "")
                                    + (dimensions > 1 ? "[column]" : "") + ", out"
                                    + (dimensions > 0 ? "[" + firstIndexName + "]" : "")
                                    + (dimensions > 1 ? "[column]" : "") + ", 0.001);",
                            dimensions > 1 ? in + cg.indent(1) + "}" : null,
                            dimensions > 0 ? in + "}" : null,

                            dimensions > 0 ? in + type + "Scalar." + absRel + "<TemperatureUnit>"
                                    + cg.buildEmptyBrackets(dimensions) + " scalar = new " + type + "Scalar." + absRel
                                    + "[value.length]" + cg.buildEmptyBrackets(dimensions - 1) + ";" : null,
                            dimensions > 0 ? "for (int " + firstIndexName + " = 0; " + firstIndexName
                                    + " < value.length; " + firstIndexName + "++)" : null,
                            dimensions > 0 ? in + "{" : null,
                            dimensions > 1 ? in + cg.indent(1) + "scalar[row] = new " + type + "Scalar." + absRel
                                    + "[value[row].length];" : null,
                            dimensions > 1 ? in + cg.indent(1)
                                    + "for (int column = 0; column < value[row].length; column++)" : null,
                            dimensions > 1 ? in + cg.indent(1) + "{" : null,
                            dimensions > 0 ? in + cg.indent(dimensions) + "scalar["
                                    + (dimensions > 0 ? firstIndexName : "") + "]" + (dimensions > 1 ? "[column]" : "")
                                    + " = new " + type + "Scalar." + absRel + "<TemperatureUnit>(value["
                                    + (dimensions > 0 ? firstIndexName : "") + (dimensions > 1 ? "][column" : "")
                                    + "], TemperatureUnit.DEGREE_CELSIUS);" : null,
                            dimensions > 1 ? in + cg.indent(1) + "}" : null,
                            dimensions > 0 ? in + "}" : null,
                            dimensions > 0 ? in + varName1 + " = new " + type + aggregate + "." + absRel + dotDS
                                    + "<TemperatureUnit>(scalar);" : null,
                            dimensions > 0 ? in + "checkContentsAndType(" + varName1 + ", value, 0.001" + suffix
                                    + ", tempUnit, " + absoluteRelative.startsWith("A") + ");" : null,
                            dimensions > 0 ? in + "assertEquals(\"All cells != 0; cardinality should equal number of "
                                    + "cells\", value.length" + (dimensions > 1 ? " * value[0].length" : "") + ", "
                                    + varName1 + ".cardinality());" : null,
                            dimensions > 0 ? in + type.toLowerCase() + " sum = 0;" : null,
                            dimensions > 0 ? in + "for (int " + firstIndexName + " = 0; " + firstIndexName
                                    + " < value.length; " + firstIndexName + "++)" : null,
                            dimensions > 0 ? in + "{" : null,
                            dimensions > 1 ? in + cg.indent(1) + "scalar[row] = new " + type + "Scalar." + absRel
                                    + "[value[row].length];" : null,
                            dimensions > 1 ? in + cg.indent(1)
                                    + "for (int column = 0; column < value[row].length; column++)" : null,
                            dimensions > 1 ? in + cg.indent(1) + "{" : null,
                            dimensions > 0 ? in + cg.indent(dimensions) + "sum += " + varName1 + ".getSI("
                                    + (dimensions > 0 ? firstIndexName : "") + (dimensions > 1 ? ", column" : "")
                                    + ");" : null,
                            dimensions > 1 ? in + cg.indent(1) + "}" : null,
                            dimensions > 0 ? in + "}" : null,
                            dimensions > 0 ? in + "assertEquals(\"zSum should be sum of all values\", sum, " + varName1
                                    + ".zSum(), 0.001);" : null,
                            0 == dimensions ? "for (int i = -100; i <= 100; i++)" : null,
                            0 == dimensions ? "{" : null,
                            0 == dimensions ? cg.indent(1) + type.toLowerCase() + " v = i / 10.0" + suffix + ";" : null,
                            0 == dimensions ? cg.indent(1) + "m" + shortName + ".setSI(v);" : null,
                            0 == dimensions ? cg.indent(1)
                                    + "assertEquals(\"intValue should round like Math.round\", Math.round(v), m"
                                    + shortName + ".intValue(), 0.0001);" : null,
                            0 == dimensions ? cg.indent(1)
                                    + "assertEquals(\"longValue should round like Math.round\", Math.round(v), m"
                                    + shortName + ".longValue(), 0.0001);" : null,
                            0 == dimensions ? cg.indent(1)
                                    + "assertEquals(\"floatValue should return the value\", v, m" + shortName
                                    + ".floatValue(), 0.0001);" : null,
                            0 == dimensions ? cg.indent(1)
                                    + "assertEquals(\"doubleValue should return the value\", v, m" + shortName
                                    + ".doubleValue(), 0.0001);" : null,
                            dimensions > 0 ? "}" : null,
                            dimensions > 0 ? "catch (ValueException ve)" : null,
                            dimensions > 0 ? "{" : null,
                            dimensions > 0 ? cg.indent(1)
                                    + "fail(\"Caught unexpected ValueException: \" + ve.toString());" : null, "}"},
                    false));
            final String mutableName = "m" + shortName;
            final String mutableMutableName = "m" + mutableName;
            final String secondImmutableName = "i" + shortName;
            final String copyName = shortName + "Copy";
            final String mutableCopyName = mutableName + "Copy";
            code.append(cg.buildMethod(
                    cg.indent(1),
                    "public final|void|toMutableAndBack" + absRel + "Test",
                    "Test conversion to mutable equivalent and back.",
                    null,
                    null,
                    pragma,
                    new String[]{
                            dimensions > 0 ? "try" : null,
                            dimensions > 0 ? "{" : null,
                            in + "TemperatureUnit tempUnit = TemperatureUnit.DEGREE_CELSIUS;",
                            in + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " value = "
                                    + makeInitializer(type, dimensions, "38.0", false) + ";",
                            in + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " value2 = "
                                    + makeInitializer(type, dimensions, "38.0", false) + ";",
                            in + "value2" + cg.buildBrackets(dimensions, "0") + " = 12345;",
                            in + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> " + shortName + " = new "
                                    + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit>(value, tempUnit);",
                            in + type + aggregate + "." + absRel + "<TemperatureUnit> " + copyName + " = " + shortName
                                    + ".copy();",
                            in + "Mutable" + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> "
                                    + mutableName + " = " + shortName + ".mutable();",
                            in + "checkContentsAndType(" + shortName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "checkContentsAndType(" + mutableName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "checkContentsAndType(" + copyName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "Mutable" + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> "
                                    + mutableCopyName + " = " + mutableName + ".copy();",
                            in + "checkContentsAndType(" + mutableCopyName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "Mutable" + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> "
                                    + mutableMutableName + " = " + mutableName + ".mutable();",
                            in + "checkContentsAndType(" + mutableMutableName + ", value, 0.001" + suffix
                                    + ", tempUnit, " + absRel.startsWith("A") + ");",
                            in + "assertEquals(\"hashCode is independent on mutability\", " + shortName
                                    + ".hashCode(), " + mutableName + ".hashCode());",
                            in + "// Modify " + mutableName,
                            in + mutableName + ".setInUnit" + "(" + zeroArgs
                                    + "12345, TemperatureUnit.DEGREE_CELSIUS);",
                            in + "checkContentsAndType(" + shortName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "checkContentsAndType(" + mutableName + ", value2, 0.01" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "checkContentsAndType(" + mutableCopyName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "checkContentsAndType(" + mutableMutableName + ", value, 0.001" + suffix
                                    + ", tempUnit, " + absRel.startsWith("A") + ");",
                            in + type + aggregate + "." + absRel + "<TemperatureUnit> " + secondImmutableName + " = "
                                    + mutableName + ".immutable();",
                            in + "assertTrue(\"Different value extremely likely results in different hashCode\", "
                                    + shortName + ".hashCode() != " + mutableName + ".hashCode());",
                            in + "// Restore value of " + mutableName,
                            in + mutableName + ".set" + (0 == dimensions ? "Value" : "") + "SI(" + zeroArgs + shortName
                                    + ".getSI(" + zeroArgsNoComma + "));",
                            in + "checkContentsAndType(" + secondImmutableName + ", value2, 0.01" + suffix
                                    + ", tempUnit, " + absRel.startsWith("A") + ");",
                            in + "checkContentsAndType(" + mutableName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "checkContentsAndType(" + mutableMutableName + ", value, 0.001" + suffix
                                    + ", tempUnit, " + absRel.startsWith("A") + ");",
                            in + mutableMutableName + ".setSI(" + zeroArgs + "0);",
                            in + "checkContentsAndType(" + mutableName + ", value, 0.001" + suffix + ", tempUnit, "
                                    + absRel.startsWith("A") + ");",
                            in + "assertEquals(\"value should be about -273\", -273, " + mutableMutableName
                                    + ".getInUnit(" + zeroArgs + "tempUnit), 0.2);",
                            dimensions > 0 ? "}" : null,
                            dimensions > 0 ? "catch (ValueException ve)" : null,
                            dimensions > 0 ? "{" : null,
                            dimensions > 0 ? cg.indent(1) + "fail(\"Caught unexpected exception: \" + ve.toString());"
                                    : null, dimensions > 0 ? "}" : null}, false));
            final String absRelInverse = absRel.startsWith("A") ? "Rel" : "Abs";
            code.append(cg.buildMethod(cg.indent(1), "public final|void|equals" + absRel + "Test",
                    "Test the equals method.", null, null, pragma, new String[]{
                            "LengthUnit lengthUnit = LengthUnit.METER;",
                            type.toLowerCase() + " value = 38.0" + suffix + ";",
                            type + "Scalar." + absRel + "<LengthUnit> " + shortName + " = new " + type + "Scalar."
                                    + absRel + "<LengthUnit>(value, lengthUnit);",
                            "assertTrue(\"Equal to itself\", " + shortName + ".equals(" + shortName + "));",
                            "assertFalse(\"Not equal to null\", " + shortName + ".equals(null));",
                            "assertFalse(\"Not equal to some other kind of object; e.g. a String\", " + shortName
                                    + ".equals(new String(\"abc\")));",
                            type + "Scalar." + absRelInverse + "<LengthUnit> " + shortName + "CounterPart = new "
                                    + type + "Scalar." + absRelInverse + "<LengthUnit>(value, lengthUnit);",
                            "assertFalse(\"Not equal if one Absolute and other Relative\", " + shortName + ".equals("
                                    + shortName + "CounterPart));",
                            type + "Scalar." + absRel + "<TemperatureUnit> " + shortName + "WrongBaseUnit = new "
                                    + type + "Scalar." + absRel + "<TemperatureUnit>(value, TemperatureUnit.KELVIN);",
                            "assertEquals(\"The underlying SI values are the same\", " + shortName + ".getSI(), "
                                    + shortName + "WrongBaseUnit.getSI(), 0.0001" + suffix + ");",
                            "assertFalse(\"Not equals because the standard SI unit differs\", " + shortName
                                    + ".equals(" + shortName + "WrongBaseUnit));",
                            type + "Scalar." + absRel + "<LengthUnit> " + shortName + "CompatibleUnit =",
                            cg.indent(2) + "new " + type + "Scalar." + absRel + "<LengthUnit>(38000.0" + suffix
                                    + ", LengthUnit.MILLIMETER);",
                            "assertFalse(\"Units are different\", " + shortName + ".getUnit().equals(" + shortName
                                    + "CompatibleUnit.getUnit()));",
                            "assertTrue(\"equals returns true\", " + shortName + ".equals(" + shortName
                                    + "CompatibleUnit));",
                            type + "Scalar." + absRel + "<LengthUnit> " + shortName + "DifferentValue =",
                            cg.indent(2) + "new " + type + "Scalar." + absRel + "<LengthUnit>(123.456" + suffix
                                    + ", LengthUnit.MILLIMETER);",
                            "assertFalse(\"Different value makes equals return false\", " + shortName + ".equals("
                                    + shortName + "DifferentValue));"

                    }, false));
            if (0 == dimensions)
            {
                for (String base : new String[]{"", "Mutable"})
                {
                    final String oppositeBase = base.equals("") ? "Mutable" : "";
                    code.append(cg.buildMethod(cg.indent(1), "public final|void|relOp" + base + absRel + "Test",
                            "Test the relational operations", null, null, pragma, new String[]{
                                    base + type + "Scalar." + absRel + "<LengthUnit> base = new " + base + type
                                            + "Scalar." + absRel + "<LengthUnit>(123, LengthUnit.KILOMETER);",
                                    base + type + "Scalar." + absRel + "<LengthUnit> same = new " + base + type
                                            + "Scalar." + absRel + "<LengthUnit>(123000, LengthUnit.METER);",
                                    base + type + "Scalar." + absRel + "<LengthUnit> smaller = new " + base + type
                                            + "Scalar." + absRel + "<LengthUnit>(122999, LengthUnit.METER);",
                                    base + type + "Scalar." + absRel + "<LengthUnit> larger = new " + base + type
                                            + "Scalar." + absRel + "<LengthUnit>(123001, LengthUnit.METER);",
                                    "assertFalse(\"123km < 123000m\", base.lt(same));",
                                    "assertTrue(\"123km <= 123000m\", base.le(same));",
                                    "assertTrue(\"123km >= 123000m\", base.ge(same));",
                                    "assertFalse(\"NOT 123km > 123000m\", base.gt(same));",
                                    "assertTrue(\"123km == 123000m\", base.eq(same));",
                                    "assertFalse(\"NOT 123km != 123000m\", base.ne(same));",
                                    "assertTrue(\"123km < 123001m\", base.lt(larger));",
                                    "assertTrue(\"123km > 122999m\", base.gt(smaller));",
                                    "assertTrue(\"123km >= 123000m\", base.ge(same));",
                                    "assertFalse(\"NOT 123km > 123000m\", base.gt(same));",
                                    "assertFalse(\"NOT 123km < 123000m\", base.lt(same));",
                                    "assertTrue(\"123km <= 123000m\", base.le(same));",
                                    "assertTrue(\"123km != 123001m\", base.ne(larger));",
                                    "assertFalse(\"NOT 123km == 123001m\", base.eq(larger));",
                                    "assertTrue(\"123km != 122999m\", base.ne(smaller));",
                                    "assertFalse(\"NOT 123km == 122999m\", base.eq(smaller));",
                                    "assertFalse(\"NOT 123km >= 123001m\", base.ge(larger));",
                                    "assertFalse(\"NOT 123km <= 122999m\", base.le(smaller));",
                                    oppositeBase + type + "Scalar." + absRel + "<LengthUnit> same2 = new "
                                            + oppositeBase + type + "Scalar." + absRel
                                            + "<LengthUnit>(123000, LengthUnit.METER);",
                                    oppositeBase + type + "Scalar." + absRel + "<LengthUnit> smaller2 = new "
                                            + oppositeBase + type + "Scalar." + absRel
                                            + "<LengthUnit>(122999, LengthUnit.METER);",
                                    oppositeBase + type + "Scalar." + absRel + "<LengthUnit> larger2 = new "
                                            + oppositeBase + type + "Scalar." + absRel
                                            + "<LengthUnit>(123001, LengthUnit.METER);",
                                    "assertFalse(\"NOT 123km < 123000m\", base.lt(same2));",
                                    "assertTrue(\"123km <= 123000m\", base.le(same2));",
                                    "assertTrue(\"123km >= 123000m\", base.ge(same2));",
                                    "assertFalse(\"NOT 123km > 123000m\", base.gt(same2));",
                                    "assertTrue(\"123km == 123000m\", base.eq(same2));",
                                    "assertFalse(\"NOT 123km != 123000m\", base.ne(same2));",
                                    "assertTrue(\"123km < 123001m\", base.lt(larger2));",
                                    "assertTrue(\"123km > 122999m\", base.gt(smaller2));",
                                    "assertTrue(\"123km >= 123000m\", base.ge(same2));",
                                    "assertFalse(\"NOT 123km > 123000m\", base.gt(same2));",
                                    "assertFalse(\"NOT 123km < 123000m\", base.lt(same2));",
                                    "assertTrue(\"123km <= 123000m\", base.le(same2));",
                                    "assertTrue(\"123km != 123001m\", base.ne(larger2));",
                                    "assertFalse(\"NOT 123km == 123001m\", base.eq(larger2));",
                                    "assertTrue(\"123km != 122999m\", base.ne(smaller2));",
                                    "assertFalse(\"NOT 123km == 122999m\", base.eq(smaller2));",
                                    "assertFalse(\"NOT 123km >= 123001m\", base.ge(larger2));",
                                    "assertFalse(\"NOT 123km <= 122999m\", base.le(smaller2));"}, false));
                }
            }
            ArrayList<String> mathCode = new ArrayList<String>();
            final String varName = type.toLowerCase().substring(0, 1);
            mathCode.add(type.toLowerCase() + "[] seedValues = {-10" + suffix + ", -2" + suffix + ", -1" + suffix
                    + ", -0.5" + suffix + ", -0.1" + suffix + ", 0" + suffix + ", 0.1" + suffix + ", 0.5" + suffix
                    + ", 1" + suffix + ", 2" + suffix + ", 10" + suffix + "};");
            mathCode.add("for (" + type.toLowerCase() + " seedValue : seedValues)");
            mathCode.add("{");
            mathCode.add(cg.indent(1) + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " input = "
                    + makeInitializer(type, dimensions, "seedValue", false) + ";");
            mathCode.add(cg.indent(1) + "Mutable" + type + aggregate + "." + absRel + dotDS + "<LengthUnit> "
                    + shortName + ";");
            if (dimensions > 0)
            {
                mathCode.add(cg.indent(1) + "try");
                mathCode.add(cg.indent(1) + "{");
            }
            for (MathFunction mf : MathFunction.mathFunctions)
            {
                final int baseIndent = (null == mf.argument ? 1 : 2) + (dimensions > 0 ? 1 : 0);
                if (null != mf.argument)
                {
                    mathCode.add(cg.indent(1) + "for (int i = -10; i <= 10; i++)");
                    mathCode.add(cg.indent(1) + "{");
                    mathCode.add(cg.indent(2) + "final " + type.toLowerCase() + " exponent = i * 0.5"
                            + type.toLowerCase().substring(0, 1) + ";");
                }
                mathCode.add(cg.indent(baseIndent) + shortName + " = new Mutable" + type + aggregate + "." + absRel
                        + dotDS + "<LengthUnit>(input, LengthUnit.METER);");
                mathCode.add(cg.indent(baseIndent) + shortName + "." + mf.name + "("
                        + (null != mf.argument ? "exponent" : "") + ");");
                mathCode.add(cg.indent(baseIndent) + "MathTester.tester(input, \"" + mf.name
                        + (null != mf.argument ? "(\" + exponent + \")" : "") + "\", " + shortName + ", 0.001, new "
                        + type + "To" + type + "()");
                mathCode.add(cg.indent(baseIndent) + "{");
                mathCode.add(cg.indent(baseIndent + 1) + "@Override");
                mathCode.add(cg.indent(baseIndent + 1) + "public " + type.toLowerCase() + " function(final "
                        + type.toLowerCase() + " " + varName + ")");
                mathCode.add(cg.indent(baseIndent + 1) + "{");
                mathCode.add(cg.indent(baseIndent + 2)
                        + "return "
                        + (mf.name.equals("inv") ? "1 / " + varName : (mf.castToFloatRequired && type.startsWith("F")
                                ? "(float) " : "")
                                + "Math."
                                + mf.name
                                + "("
                                + varName
                                + (null != mf.argument ? ", exponent" : "") + ")") + ";");
                mathCode.add(cg.indent(baseIndent + 1) + "}");
                mathCode.add(cg.indent(baseIndent) + "});");
                if (null != mf.argument)
                {
                    mathCode.add(cg.indent(1) + "}");
                }
            }
            if (dimensions > 0)
            {
                mathCode.add(cg.indent(1) + "}");
                mathCode.add(cg.indent(1) + "catch (ValueException ve)");
                mathCode.add(cg.indent(1) + "{");
                mathCode.add(cg.indent(2) + "fail(\"Caught unexpected ValueException: \" + ve.toString());");
                mathCode.add(cg.indent(1) + "}");
            }
            mathCode.add("}");
            code.append(cg.buildMethod(cg.indent(1), "public final|void|mathFunctionsTest" + absRel + "Test",
                    "Test the Math functions.", null, null, pragma, CodeGenerator.arrayListToArray(mathCode), false));
            String[] dsCombinations = dotDS.length() == 0 ? new String[]{""} : new String[]{".Dense", ".Sparse"};
            for (String otherDotDS : dsCombinations)
            {
                code.append(twoOpChecker(cg, type, dimensions, absRel + dotDS, "Rel" + otherDotDS,
                        absRel + (dotDS.contains("Dense") || otherDotDS.contains("Dense") ? ".Dense" : dotDS), "plus",
                        "+"));
                code.append(twoOpChecker(cg, type, dimensions, absRel + dotDS, "Rel" + otherDotDS,
                        absRel + (dotDS.contains("Dense") || otherDotDS.contains("Dense") ? ".Dense" : dotDS), "minus",
                        "-"));
                code.append(twoOpChecker(cg, type, dimensions, absRel + dotDS, absRel + otherDotDS,
                        absRel + (dotDS.contains("Sparse") || otherDotDS.contains("Sparse") ? ".Sparse" : dotDS),
                        0 == dimensions ? "multiply" : "times", "*"));
                if (0 == dimensions)
                {
                    code.append(twoOpChecker(cg, type, dimensions, absRel + dotDS, absRel + dotDS, absRel + dotDS,
                            "divide", "/"));
                }
            }
            if (dimensions > 0)
            {
                ArrayList<String> checkCode = new ArrayList<String>();
                checkCode.add("int junk = 0;");
                checkCode.addAll(wrap(cg, "", new String[]{
                        "// null array",
                        "new " + type + aggregate + "." + absRel + dotDS + "<TemperatureUnit>((" + type.toLowerCase()
                                + cg.buildEmptyBrackets(dimensions) + ") null, TemperatureUnit.DEGREE_FAHRENHEIT);"}));
                if (dimensions > 1)
                {
                    checkCode.addAll(wrap(
                            cg,
                            "",
                            new String[]{
                                    "// Matrix with null on first row",
                                    type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " in = "
                                            + makeInitializer(type, dimensions, "12.3", false) + ";",
                                    "in[0] = null;",
                                    "new " + type + aggregate + "." + absRel + dotDS
                                            + "<TemperatureUnit>(in, TemperatureUnit.DEGREE_CELSIUS);"}));
                    checkCode.addAll(wrap(
                            cg,
                            "",
                            new String[]{
                                    "// Matrix with null on last row",
                                    type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " in = "
                                            + makeInitializer(type, dimensions, "12.3", false) + ";",
                                    "in[in.length - 1] = null;",
                                    "new " + type + aggregate + "." + absRel + dotDS
                                            + "<TemperatureUnit>(in, TemperatureUnit.DEGREE_CELSIUS);"}));
                    checkCode.addAll(wrap(
                            cg,
                            "",
                            new String[]{
                                    "// Non-rectangular array",
                                    type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " in = "
                                            + makeInitializer(type, dimensions, "12.3", true) + ";",
                                    "new " + type + aggregate + "." + absRel + dotDS
                                            + "<TemperatureUnit>(in, TemperatureUnit.DEGREE_CELSIUS);"}));
                    checkCode.add("// Determinant of non-square Matrix");
                    checkCode.add(type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " in = "
                            + makeInitializer(type, dimensions, "12.3", false) + ";");
                    checkCode.addAll(wrap(cg, "", new String[]{
                            type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> matrix = null;",
                            "try",
                            "{",
                            cg.indent(1) + "matrix = new " + type + aggregate + "." + absRel + dotDS
                                    + "<TemperatureUnit>(in, TemperatureUnit.DEGREE_CELSIUS);", "}",
                            "catch (ValueException ve)", "{",
                            cg.indent(1) + "fail(\"Caught unexpected exception: \" + ve.toString());", "}",
                            "matrix.det();"}));
                    checkCode.addAll(wrap(cg, "",
                            new String[]{
                                    type + aggregate + "." + absRel + dotDS + "<TemperatureUnit> matrix = null;",
                                    type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " rowCountWrong = null;",
                                    "try",
                                    "{",
                                    cg.indent(1) + "matrix = new " + type + aggregate + "." + absRel + dotDS
                                            + "<TemperatureUnit>(in, TemperatureUnit.DEGREE_CELSIUS);",
                                    cg.indent(1) + " rowCountWrong = data(4, 5, false, 2);", "}",
                                    "catch (ValueException ve)", "{",
                                    cg.indent(1) + "fail(\"Caught unexpected exception: \" + ve.toString());", "}",
                                    type + aggregate + ".times(matrix, rowCountWrong);"}));
                }

                checkCode.add("assertTrue(\"The variable junk is only used to suppress annoying warnings of the code "
                        + "checker\", junk > 0);");
                code.append(cg.buildMethod(cg.indent(1), "public final|void|sizeCheck" + absRel + "Test",
                        "Test that malformed or mismatching arrays throw a ValueException.", null, null, pragma,
                        CodeGenerator.arrayListToArray(checkCode), false));
            }
            if (2 == dimensions)
            {
                code.append(cg.buildMethod(cg.indent(1), "public final|void|determinant" + absRel + "Test",
                        "Test the det method that computes and returns the determinant.", null, null, pragma,
                        new String[]{
                                "try",
                                "{",
                                cg.indent(1) + type.toLowerCase() + cg.buildEmptyBrackets(dimensions)
                                        + " values = {{1, 2, 3 }, {3, 5, 7 }, {5, 10, 0} };",
                                cg.indent(1) + type + aggregate + "." + absRel + dotDS
                                        + "<TemperatureUnit> matrix = new " + type + aggregate + "." + absRel + dotDS
                                        + "<TemperatureUnit>(values, TemperatureUnit.KELVIN);",
                                cg.indent(1) + "assertEquals(\"Determinant should be 15\", 15, matrix.det(), 0.001);",
                                "}", "catch (ValueException ve)", "{",
                                cg.indent(1) + "if (ve.toString().contains(\"Matrix must be sparse\"))",
                                cg.indent(1) + "{",
                                cg.indent(2) + "System.err.println(\"Ignoring bug in COLT library\");",
                                cg.indent(2) + "return;", cg.indent(1) + "}",
                                cg.indent(1) + "fail(\"Caught unexpected ValueException: \" + ve.toString());", "}"},
                        false));
            }
            if (dimensions > 0)
            {
                code.append(cg.buildMethod(
                        cg.indent(1),
                        "public final|void|scale" + absRel + "Test",
                        "Test that the times methods with a simple array as the 2nd argument.",
                        null,
                        null,
                        pragma,
                        new String[]{
                                "try",
                                "{",
                                cg.indent(1) + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " leftIn = "
                                        + makeInitializer(type, dimensions, "-12.34" + suffix, false) + ";",
                                cg.indent(1) + type + aggregate + "." + absRel + dotDS
                                        + "<TemperatureUnit> left = new " + type + aggregate + "." + absRel + dotDS
                                        + "<TemperatureUnit>(leftIn, TemperatureUnit.KELVIN);",
                                cg.indent(1) + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " right = "
                                        + makeInitializer(type, dimensions, "-4.321" + suffix, false) + ";",
                                cg.indent(1) + "Mutable" + type + aggregate + "." + absRel + dotDS
                                        + "<TemperatureUnit> result = " + type + aggregate + ".times(left, right);",
                                cg.indent(1)
                                        + "assertEquals(\"Result should be in Kelvin\", TemperatureUnit.KELVIN, result.getUnit());",
                                cg.indent(1) + "for (int " + firstIndexName + " = right.length; --" + firstIndexName
                                        + " >= 0;)",
                                cg.indent(1) + "{",
                                dimensions > 1 ? cg.indent(2) + "for (int column = right[row].length; --column >= 0;)"
                                        : null,
                                dimensions > 1 ? cg.indent(2) + "{" : null,
                                cg.indent(dimensions + 1)
                                        + "assertEquals(\"Content should match product of left and right\", leftIn["
                                        + firstIndexName + "]" + (dimensions > 1 ? "[column]" : "") + " * right["
                                        + firstIndexName + "]" + (dimensions > 1 ? "[column]" : "") + ", result.getSI("
                                        + firstIndexName + (dimensions > 1 ? ", column" : "") + "), 0.001" + suffix
                                        + ");", dimensions > 1 ? cg.indent(2) + "}" : null, cg.indent(1) + "}", "}",
                                "catch (ValueException ve)", "{",
                                cg.indent(1) + "fail(\"Caught unexpected exception: \" + ve.toString());", "}"}, false));
            }
        }

        code.append(cg.indent(1) + "/** */\r\n");
        code.append(cg.indent(1) + "interface " + type + "To" + type + "\r\n");
        code.append(cg.indent(1) + "{\r\n");
        code.append(cg.indent(2) + "/**\r\n");
        code.append(cg.indent(2) + " * @param " + type.toLowerCase().substring(0, 1) + " " + type.toLowerCase()
                + "; value\r\n");
        code.append(cg.indent(2) + " * @return " + type.toLowerCase() + " value\r\n");
        code.append(cg.indent(2) + " */\r\n");
        code.append(cg.indent(2) + type.toLowerCase() + " function(" + type.toLowerCase() + " "
                + type.toLowerCase().substring(0, 1) + ");\r\n");
        code.append(cg.indent(1) + "}\r\n");
        code.append("\r\n");
        code.append(cg.indent(1) + "/** */\r\n");
        code.append(cg.indent(1) + "abstract static class MathTester\r\n");
        code.append(cg.indent(1) + "{\r\n");
        code.append(cg.buildMethod(
                cg.indent(2),
                "public static|void|tester",
                "Test a math function.",
                new String[]{
                        "final " + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + "|inputValue"
                                + (dimensions > 0 ? "s" : "") + "|unprocessed value",
                        "final String|operation|description of method that is being tested",
                        "final " + type + aggregate + "<?>|actualResult|the actual result of the operation",
                        "final double|precision|expected accuracy",
                        "final " + type + "To" + type + "|function|encapsulated function that converts one "
                                + "inputValue to an outputValue"},
                null,
                null,
                new String[]{
                        dimensions > 0 ? "for (int i = 0; i < inputValues.length; i++)" : null,
                        dimensions > 0 ? "{" : null,
                        dimensions > 1 ? cg.indent(1) + "for (int j = 0; j < inputValues[i].length; j++)" : null,
                        dimensions > 1 ? cg.indent(1) + "{" : null,
                        cg.indent(dimensions) + type.toLowerCase() + " expectedResult = function.function(inputValue"
                                + (dimensions > 0 ? "s[i]" + (dimensions > 1 ? "[j]" : "") : "") + ");",
                        cg.indent(dimensions) + type.toLowerCase() + " got = "
                                + (dimensions == 0 ? "actualResult.getSI();" : "0;"),
                        dimensions > 0 ? cg.indent(dimensions) + "try" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "{" : null,
                        dimensions > 0 ? cg.indent(dimensions + 1) + "got = actualResult.getSI(i"
                                + (dimensions > 1 ? ", j" : "") + ");" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "}" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "catch (ValueException ve)" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "{" : null,
                        dimensions > 0 ? cg.indent(dimensions + 1)
                                + "fail(\"Caught unexpected exception: \" + ve.toString());" : null,
                        dimensions > 0 ? cg.indent(dimensions) + "}" : null,
                        cg.indent(dimensions) + "String description =",
                        cg.indent(dimensions + 2)
                                + "String.format(\"%s(%f->%f should be equal to %f with precision %f\", "
                                + "operation, inputValue"
                                + (dimensions > 0 ? "s[i]" + (dimensions > 1 ? "[j]" : "") : "") + ",",
                        cg.indent(dimensions + 4) + "expectedResult, got, precision);",
                        cg.indent(dimensions) + "// System.out.println(description);",
                        cg.indent(dimensions) + "assertEquals(description, expectedResult, got, precision);",
                        dimensions > 1 ? cg.indent(1) + "}" : null, dimensions > 0 ? "}" : null}, false));
        code.append(cg.indent(1) + "}\r\n");
        code.append("\r\n");

        cg.generateClass("v" + type.toLowerCase() + "." + aggregate.toLowerCase(), type + aggregate
                + (null != denseness ? denseness : "") + "Test", new String[]{"static org.junit.Assert.assertEquals",
                "static org.junit.Assert.assertFalse", "static org.junit.Assert.assertTrue",
                dimensions > 0 ? "static org.junit.Assert.fail" : null, "", dimensions > 0 ? "" : null,
                "org.junit.Test", otscu + "LengthUnit", otscu + "TemperatureUnit", "org.opentrafficsim.core.unit.Unit",
                dimensions > 0 ? "org.opentrafficsim.core.value.ValueException" : null,
                "org.opentrafficsim.core.value.v" + type.toLowerCase() + ".scalar." + type + "Scalar"}, "Test the "
                + type + aggregate + " class.", null, "public", "", false, code.toString());

    }

    /**
     * Embed Java code in a try - catch construct.
     * @param cg CodeGenerator; the code generator to use to generate additional indent for the embedded code
     * @param in String; prefix for all output lines
     * @param embeddedCode String; Java code to embed in try - catch construct
     * @return ArrayList&lt;String&gt;; Java code
     */
    private static ArrayList<String> wrap(final CodeGenerator cg, final String in, final String[] embeddedCode)
    {
        ArrayList<String> code = new ArrayList<String>();
        code.add(in + "try");
        code.add(in + "{");
        for (String line : embeddedCode)
        {
            code.add(in + cg.indent(1) + line);
        }
        code.add(in + cg.indent(1) + "fail(\"Preceding code should have thrown a ValueException\");");
        code.add(in + "}");
        code.add(in + "catch (ValueException ve)");
        code.add(in + "{");
        code.add(in + cg.indent(1) + "// Ignore (exception was expected)");
        code.add(in + cg.indent(1) + "junk++;");
        code.add(in + "}");
        return code;
    }

    /**
     * Generate the Java code to check a two operand operator.
     * @param cg CodeGenerator; the code generator
     * @param type String; <cite>Float</cite>, or <cite>Double</cite>
     * @param dimensions int; the number of dimensions of the data
     * @param leftType String; something like <cite>Abs</cite>, or <cite>Rel.Dense</cite>
     * @param rightType String; like leftType
     * @param resultType String; like leftType
     * @param operatorName String; something like <cite>plus</cite>
     * @param operatorSymbol String; something like <cite>+</cite>
     * @return String; Java code for a method that checks the operation
     */
    private static String twoOpChecker(final CodeGenerator cg, final String type, final int dimensions,
            final String leftType, final String rightType, final String resultType, final String operatorName,
            final String operatorSymbol)
    {
        final String aggregate = 0 == dimensions ? "Scalar" : 1 == dimensions ? "Vector" : "Matrix";
        final String suffix = type.startsWith("F") ? "f" : "";
        final ArrayList<String> code = new ArrayList<String>();
        final String indices = (dimensions > 0 ? "i" : "") + (dimensions > 1 ? ", j" : "");
        final String in = cg.indent(dimensions > 0 ? 1 : 0);
        // If dimensions > 0, all code should be in a try - catch construction
        if (dimensions > 0)
        {
            code.add("try");
            code.add("{");
        }
        // Generate the initializers
        code.add(in + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " leftValue = "
                + makeInitializer(type, dimensions, "123.4", false) + ";");
        code.add(in + type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " rightValue = "
                + makeInitializer(type, dimensions, "234.5", false) + ";");
        code.add(in + type + aggregate + "." + leftType + "<LengthUnit>" + " left = new " + type + aggregate + "."
                + leftType + "<LengthUnit>" + "(leftValue, LengthUnit.MILE);");
        code.add(in + type + aggregate + "." + rightType + "<LengthUnit>" + " right = new " + type + aggregate + "."
                + rightType + "<LengthUnit>" + "(rightValue, LengthUnit.MILE);");
        // Perform the operation
        code.add(in + "Mutable" + type + aggregate + "." + resultType + "<?> result = " + type + aggregate + "."
                + operatorName + "(left, right);");
        // Check the result
        if (dimensions > 0)
        {
            code.add(in + "for (int i = 0; i < leftValue.length; i++)");
            code.add(in + "{");
            if (dimensions > 1)
            {
                code.add(in + cg.indent(1) + "for (int j = 0; j < leftValue[i].length; j++)");
                code.add(in + cg.indent(1) + "{");
            }
        }
        code.add(in + cg.indent(dimensions) + "assertEquals(\"value of element should be SI " + operatorName
                + " of contributing elements\", left.getSI(" + indices + ") " + operatorSymbol + " right.getSI("
                + indices + "), result.getSI(" + indices + "), 0.001" + suffix + ");");
        if (dimensions > 0)
        {
            if (dimensions > 1)
            {
                code.add(in + cg.indent(1) + "}");
            }
            code.add(in + "}");
            code.add("}");
        }

        if (dimensions > 0)
        {
            // finish the try - catch construction
            code.add("catch (ValueException ve)");
            code.add("{");
            code.add(cg.indent(1) + "fail(\"Caught unexpected ValueException: \" + ve.toString());");
            code.add("}");
        }
        return cg.buildMethod(
                cg.indent(1),
                "public final|void|binary" + operatorName + "Of" + leftType.replaceAll("\\.", "") + "And"
                        + rightType.replaceAll("\\.", "") + "Test", "Test " + operatorName + "(" + type + aggregate
                        + leftType + ", " + type + aggregate + rightType + ").", null, null,
                "@SuppressWarnings(\"static-method\")\r\n" + cg.indent(1) + "@Test",
                CodeGenerator.arrayListToArray(code), false);
    }

    /**
     * Write Java code that creates a call to generate N-dimensional test data.
     * @param type String; <cite>Float</cite>, or <cite>Double</cite>
     * @param dimensions int; number of dimensions of the data
     * @param seed String; value of the first element in the test data
     * @param nonRectangular boolean; generate a non-rectangular array as result
     * @return String; java code
     */
    private static String makeInitializer(final String type, final int dimensions, final String seed,
            final boolean nonRectangular)
    {
        final String suffix = Character.isDigit(seed.charAt(0)) ? (type.startsWith("F") ? "f" : "") : "";
        if (0 == dimensions)
        {
            return seed + suffix;
        }
        String call = "data";
        for (int dimension = 0; dimension < dimensions; dimension++)
        {
            call += 0 == dimension ? "(3" : ", " + (dimension + 4);
        }
        call += ", " + (dimensions > 1 ? (nonRectangular ? "true" : "false") + ", " : "") + seed + suffix;
        call += ")";
        return call;
    }

    /**
     * Generate Java code for an initializer.
     * @param cg CodeGenerator
     * @param dimensions int; number of dimensions of the data
     * @param type String; <cite>Float</cite>, or <cite>Double</cite>
     * @return String; java code
     */
    private static String generateInitializer(final CodeGenerator cg, final int dimensions, final String type)
    {
        if (0 == dimensions)
        {
            return "";
        }
        final String firstIndexName = 1 == dimensions ? "index" : "row";
        return cg.buildMethod(cg.indent(1), "private static|" + type.toLowerCase() + cg.buildEmptyBrackets(dimensions)
                + "|data", "Generate test data.", new String[]{
                1 == dimensions ? "final int|size|number of values in the result" : null,
                2 == dimensions ? "final int|rows|the number of rows in the result" : null,
                2 == dimensions ? "final int|columns|the number of columns in the result" : null,
                dimensions > 1 ? "final boolean|nonRectangular|if true; return a non-rectangular "
                        + "array; if false; return a rectangular array" : null,
                "final " + type.toLowerCase() + "|startValue|seed value"}, null, null, new String[]{
                type.toLowerCase() + cg.buildEmptyBrackets(dimensions) + " result = new " + type.toLowerCase() + "["
                        + (1 == dimensions ? "size" : "rows") + "]" + cg.buildEmptyBrackets(dimensions - 1) + ";",
                dimensions > 1 ? "final int badRowIndex = nonRectangular ? rows - 1 : -1;" : null,
                "for (int " + firstIndexName + " = 0; " + firstIndexName + " < " + (1 == dimensions ? "size" : "rows")
                        + "; " + firstIndexName + "++)",
                "{",
                dimensions > 1 ? cg.indent(1) + "result[row] = new " + type.toLowerCase()
                        + "[row == badRowIndex ? columns + 1 : columns];" : null,
                dimensions > 1 ? cg.indent(1) + "for (int column = 0; column < result[row].length; column++)" : null,
                dimensions > 1 ? cg.indent(1) + "{" : null,
                cg.indent(dimensions) + "result[" + firstIndexName + (dimensions > 1 ? "][column" : "") + "] = "
                        + firstIndexName + (dimensions > 1 ? " * 1000 + column" : "") + " + startValue;",
                dimensions > 1 ? cg.indent(1) + "}" : null, "}", "return result;"}, false);
    }

}
