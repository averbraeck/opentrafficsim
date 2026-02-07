package org.opentrafficsim.editor.decoration.validation;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.DocumentReader.NodeAnnotation;
import org.opentrafficsim.editor.Schema;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Interface for validators of element attributes and values.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ValueValidator extends Comparable<ValueValidator>
{

    /** Set to store tags for which an error has been printed, to prevent repeated printing on repeated validation. */
    Set<String> SUPPRESS_ERRORS = new LinkedHashSet<>();

    /**
     * Returns message why a value is invalid, or {@code null} if the value is valid.
     * @param node supplied to verify with context, e.g. value combinations.
     * @return message why a value is invalid, or empty if the value is valid.
     */
    Optional<String> validate(XsdTreeNode node);

    /**
     * Returns the options that a validator allows, typically an xsd:keyref returning defined values under the referred xsd:key
     * or xsd:unique. The field object is any object that a validator uses to know what particular information from the node is
     * required. The field object is stored in an {@code XsdTreeNode} when the validator is assigned to a particular attribute
     * or the node value. Note that only the field name is usually insufficient, as the node itself, an attribute, or any child,
     * may have the same name.
     * @param node node that is in the appropriate context.
     * @param field field for which to obtain the options.
     * @return options, empty if this validator is not a restriction of limited options.
     */
    // Optional List because an empty list means no options allowed
    default Optional<List<String>> getOptions(final XsdTreeNode node, final Object field)
    {
        return Optional.empty();
    }

    /**
     * Validates an includes file by checking whether it can be found.
     * @param directory base directory for relative paths.
     * @param fileName file name and path, possibly relative.
     * @param fallback fallback file name and path, possibly relative.
     * @return first encountered problem in validating the value of the include, empty if there is no problem.
     */
    static Optional<String> reportInvalidInclude(final String directory, final String fileName, final String fallback)
    {
        if (fileName == null && fallback == null)
        {
            return Optional.of("Value is empty.");
        }
        if (fileName == null)
        {
            return Optional.of("Fallback may only be provided if a file is also provided.");
        }
        File file = new File(fileName);
        if (!file.isAbsolute())
        {
            if (directory == null)
            {
                return Optional.of("Relative path defined but directory unknown. Try saving your work.");
            }
            file = new File(directory + fileName);
        }
        if (!file.exists())
        {
            if (fallback == null)
            {
                return Optional.of("The file cannot be found.");
            }
            return reportInvalidInclude(directory, fallback, null); // check fallback instead
        }
        return Optional.empty();
    }

    /**
     * Report first encountered problem in validating the value of the node based on XSD type.
     * @param xsdNode node.
     * @param value value.
     * @param schema schema for type retrieval.
     * @return first encountered problem in validating the value of the node, empty if there is no problem.
     */
    static Optional<String> reportInvalidValue(final Node xsdNode, final String value, final Schema schema)
    {
        if (xsdNode.getChildNodes().getLength() == DocumentReader.getChildren(xsdNode, "#text").size()
                && DocumentReader.getAttribute(xsdNode, "type").isEmpty())
        {
            // no children and no type, this is a plain tag, e.g. <Straight />, it needs no input.
            return null;
        }
        if (value == null || value.isEmpty())
        {
            return Optional.of("Value is empty.");
        }
        return Optional.ofNullable(reportTypeNonCompliance(xsdNode, xsdNode, "type", value, schema, null, null));
    }

    /**
     * Report first encountered problem in validating the attribute value.
     * @param xsdNode node, should be an xsd:attribute.
     * @param value value.
     * @param schema schema for type retrieval.
     * @return first encountered problem in validating the attribute value, empty if there is no problem.
     */
    static Optional<String> reportInvalidAttributeValue(final Node xsdNode, final String value, final Schema schema)
    {
        String use = DocumentReader.getAttribute(xsdNode, "use").orElse(null);
        if (value == null || value.isEmpty())
        {
            return Optional.ofNullable("required".equals(use) ? "Required value is empty." : null);
        }
        return Optional.ofNullable(reportTypeNonCompliance(xsdNode, xsdNode, "type", value, schema, null, null));
    }

    /**
     * Returns all restrictions for the given node.
     * @param xsdNode node.
     * @param schema schema.
     * @return list of xsd:restriction nodes applicable to the input node.
     */
    static List<Node> getRestrictions(final Node xsdNode, final Schema schema)
    {
        List<Node> restrictions = new ArrayList<>();
        reportTypeNonCompliance(xsdNode, xsdNode, "type", null, schema, restrictions, null);
        return restrictions;
    }

    /**
     * Returns the base type of the given node, e.g. xsd:double. In case an xsd:union is encountered, this is returned.
     * @param xsdNode node.
     * @param schema schema.
     * @return base type of the given node, e.g. xsd:double.
     */
    static String getBaseType(final Node xsdNode, final Schema schema)
    {
        List<String> baseType = new ArrayList<>();
        reportTypeNonCompliance(xsdNode, xsdNode, "type", null, schema, null, baseType);
        return baseType.get(baseType.size() - 1);
    }

    /**
     * Report first encountered problem in validating the value by a type, or when {@code value = null} scan all restrictions
     * and place them in the input list, and/or find the base type and store it in the base type list. The attribute input
     * defines the attribute in the node that may refer to a type containing restrictions.
     * @param appInfoNode node having possible xsd:appinfo for a message.
     * @param node type node.
     * @param typeAttribute "type" on normal calls, "base" or "memberTypes" on recursive calls.
     * @param value value, may be {@code null} (to have restrictions/base types returned).
     * @param schema schema for type retrieval.
     * @param restrictions list that xsd:restriction nodes will be placed in to, may be {@code null}.
     * @param baseType is filled by this method with encountered base types, e.g. xsd:double, may be {@code null}.
     * @return first encountered problem in validating the value by a type, {@code null} if there is no problem.
     */
    private static String reportTypeNonCompliance(final Node appInfoNode, final Node node, final String typeAttribute,
            final String value, final Schema schema, final List<Node> restrictions, final List<String> baseType)
    {
        // can request "base" or "memberTypes" on recursion
        String type = DocumentReader.getAttribute(node, typeAttribute).orElse(null);
        // multiple possible when memberTypes in xsd:union
        String[] types = type == null ? new String[0] : type.split("\\s+");
        List<String> reports = new ArrayList<>(types.length);
        for (String singleType : types)
        {
            reports.add(reportSingleTypeNonCompliance(singleType, value, schema, restrictions, baseType));
        }
        if (reports.size() == 1 && reports.get(0) != null && !node.getNodeName().equals("xsd:union"))
        {
            return reports.get(0);
        }
        if (type != null && !node.getNodeName().equals("xsd:restriction") && !node.getNodeName().equals("xsd:union"))
        {
            return null;
        }

        switch (node.getNodeName())
        {
            case "xsd:complexType":
                Node simpleContent = DocumentReader.getChild(node, "xsd:simpleContent").get();
                Optional<Node> extension = DocumentReader.getChild(simpleContent, "xsd:extension");
                if (extension.isPresent())
                {
                    return reportTypeNonCompliance(extension.get(), extension.get(), "base", value, schema, restrictions,
                            baseType);
                }
                return reportTypeNonCompliance(appInfoNode, DocumentReader.getChild(simpleContent, "xsd:restriction").get(),
                        "base", value, schema, restrictions, baseType);
            case "xsd:simpleType":
                Optional<Node> union = DocumentReader.getChild(node, "xsd:union");
                if (union.isPresent())
                {
                    return reportTypeNonCompliance(appInfoNode, union.get(), "memberTypes", value, schema, restrictions,
                            baseType);
                }
                return reportTypeNonCompliance(appInfoNode, DocumentReader.getChild(node, "xsd:restriction").get(), "base",
                        value, schema, restrictions, baseType);
            case "xsd:element":
                if (node.getChildNodes().getLength() == 0)
                {
                    return null;
                }
                Optional<Node> complexType = DocumentReader.getChild(node, "xsd:complexType");
                if (complexType.isPresent())
                {
                    return reportTypeNonCompliance(complexType.get(), complexType.get(), "type", value, schema, restrictions,
                            baseType);
                }
                Node simpleType = DocumentReader.getChild(node, "xsd:simpleType").get();
                return reportTypeNonCompliance(simpleType, simpleType, "type", value, schema, restrictions, baseType);
            case "xsd:attribute":
                Node simpleTypeAttr = DocumentReader.getChild(node, "xsd:simpleType").get();
                return reportTypeNonCompliance(simpleTypeAttr, simpleTypeAttr, "type", value, schema, restrictions, baseType);
            case "xsd:restriction":
                if (value == null)
                {
                    if (restrictions != null)
                    {
                        restrictions.add(node);
                    }
                    return null;
                }
                return reportRestrictionNonCompliance(appInfoNode, node, value);
            case "xsd:union":
                if (baseType != null)
                {
                    baseType.add("xsd:union");
                }
                List<Node> simpleTypes = DocumentReader.getChildren(node, "xsd:simpleType");
                for (Node simpleTypeUnion : simpleTypes)
                {
                    reports.add(reportTypeNonCompliance(node, simpleTypeUnion, "type", value, schema, restrictions, baseType));
                }
                if (!reports.contains(null))
                {
                    if (reports.size() == 1)
                    {
                        return reports.get(0);
                    }
                    StringBuilder builder = new StringBuilder();
                    String sep = "";
                    for (String report : reports)
                    {
                        builder.append(sep).append(report.endsWith(".") ? report.substring(0, report.length() - 1) : report);
                        sep = ", or ";
                    }
                    return builder.append(".").toString();
                }
                return null;
            case "xi:include":
                return null;
            default:
                throw new OtsRuntimeException("Unable to validate " + node.getNodeName() + ".");
        }
    }

    /**
     * Report non-compliance of a single type, e.g. for each in {@code memberTypes} of an {@code xsd:union}.
     * @param type type name.
     * @param value value.
     * @param schema schema for type retrieval.
     * @param restrictions list that xsd:restriction nodes will be placed in to.
     * @param baseType is filled by this method with encountered base types, e.g. xsd:double, may be {@code null}.
     * @return first encountered problem in validating the value by a type, {@code null} if there is no problem.
     */
    private static String reportSingleTypeNonCompliance(final String type, final String value, final Schema schema,
            final List<Node> restrictions, final List<String> baseType)
    {
        boolean isNativeType = type != null && type.startsWith("xsd:");
        if (isNativeType)
        {
            if (value == null)
            {
                if (baseType != null)
                {
                    baseType.add(type);
                }
                return null;
            }
            else
            {
                return reportNativeTypeNonCompliance(type, value);
            }
        }
        if (type != null)
        {
            Optional<Node> typeNode = schema.getType(type);
            if (typeNode.isPresent())
            {
                String report =
                        reportTypeNonCompliance(typeNode.get(), typeNode.get(), "base", value, schema, restrictions, baseType);
                if (value != null && report != null)
                {
                    return report;
                }
            }
        }
        return null;
    }

    /**
     * Report first encountered problem in validating a native type, e.g. xsd:int or xsd:anyURI.
     * @param type type.
     * @param value value.
     * @return first encountered problem in validating a native type, {@code null} if there is no problem.
     */
    private static String reportNativeTypeNonCompliance(final String type, final String value)
    {
        String valueType = "number";
        try
        {
            switch (type)
            {
                case "xsd:string":
                    return null;
                case "xsd:boolean": // "true" or "false"
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
                    {
                        return "Boolean value must be \"true\" or \"false\".";
                    }
                    return null;
                case "xsd:double": // 64-bit
                    Double.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:float": // 32-bit
                    Float.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:decimal":
                    Double.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:int": // 32-bit signed
                    valueType = "integer";
                    Integer.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:long": // 64-bit signed
                    valueType = "integer";
                    Long.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:unsignedInt": // 32-bits, i.e. max is 2^32 - 1 = 4294967295
                    valueType = "integer";
                    long val = Long.valueOf(value); // might throw NumberFormatException
                    if (val < 0)
                    {
                        return "Integer value must be a positive integer.";
                    }
                    if (val > 4294967295L)
                    {
                        return "Integer value must be at most 4294967295.";
                    }
                    return null;
                case "xsd:positiveInteger": // arbitrary length
                    valueType = "integer";
                    if (Long.valueOf(value) < 1) // might throw NumberFormatException
                    {
                        return "Integer value must be a positive integer.";
                    }
                    return null;
                case "xsd:integer": // arbitrary length
                    valueType = "integer";
                    Long.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:anyURI": // RFC2396 compliant, just as URI in java
                    try
                    {
                        new URI(value);
                    }
                    catch (URISyntaxException exception)
                    {
                        return "Invalid URI.";
                    }
                    return null;
                default:
                    if (!type.startsWith("ots:"))
                    {
                        String message = "Type " + type + " cannot be validated.";
                        if (!SUPPRESS_ERRORS.contains(message))
                        {
                            Logger.ots().error(message);
                            SUPPRESS_ERRORS.add(message);
                        }
                    }
                    return null;
            }
        }
        catch (NumberFormatException exception)
        {
            if (type.length() > 5)
            {
                String t = type.replace("xsd:", "");
                return t.substring(0, 1).toUpperCase() + t.substring(1) + " value must be a valid " + valueType + ".";
            }
            return type + " value must be a valid " + valueType + ".";
        }
    }

    /**
     * Report first encountered problem in validating the value by a restriction.
     * @param appInfoNode node having possible xsd:appinfo for a message with source="pattern".
     * @param node node, must be an xsd:restriction.
     * @param value value.
     * @return first encountered problem in validating the value by a restriction.
     */
    private static String reportRestrictionNonCompliance(final Node appInfoNode, final Node node, final String value)
    {
        Optional<Node> pattern = DocumentReader.getChild(node, "xsd:pattern");
        if (pattern.isPresent())
        {
            String patternString = DocumentReader.getAttribute(pattern.get(), "value").get();
            try
            {
                if (!Pattern.matches(patternString, value))
                {
                    Optional<String> patternMessage = NodeAnnotation.APPINFO_PATTERN.get(appInfoNode);
                    return patternMessage.isEmpty() ? "Value does not match pattern " + patternString : patternMessage.get();
                }
            }
            catch (PatternSyntaxException exception)
            {
                if (!SUPPRESS_ERRORS.contains(patternString))
                {
                    Logger.ots().error("Could not validate value by pattern due to a PatternSyntaxException."
                            + " This means the pattern is not valid.");
                    Logger.ots().error(exception.getMessage());
                    SUPPRESS_ERRORS.add(patternString);
                }
            }
        }
        List<Node> enumerations = DocumentReader.getChildren(node, "xsd:enumeration");
        List<String> options = new ArrayList<>();
        for (Node enumeration : enumerations)
        {
            options.add(DocumentReader.getAttribute(enumeration, "value").get());
        }
        if (!options.isEmpty() && !options.contains(value))
        {
            String arrayString = options.toString();
            return "Must be any of " + arrayString.substring(1, arrayString.length() - 1) + ".";
        }
        Optional<Node> minInclusive = DocumentReader.getChild(node, "xsd:minInclusive");
        if (minInclusive.isPresent())
        {
            String val = DocumentReader.getAttribute(minInclusive.get(), "value").get();
            if (Double.valueOf(value) < Double.valueOf(val))
            {
                return "Value must be above or equal to " + val + ".";
            }
        }
        Optional<Node> minExclusive = DocumentReader.getChild(node, "xsd:minExclusive");
        if (minExclusive.isPresent())
        {
            String val = DocumentReader.getAttribute(minExclusive.get(), "value").get();
            if (Double.valueOf(value) <= Double.valueOf(val))
            {
                return "Value must be above " + val + ".";
            }
        }
        Optional<Node> maxInclusive = DocumentReader.getChild(node, "xsd:maxInclusive");
        if (maxInclusive.isPresent())
        {
            String val = DocumentReader.getAttribute(maxInclusive.get(), "value").get();
            if (Double.valueOf(value) > Double.valueOf(val))
            {
                return "Value must be below or equal to " + val + ".";
            }
        }
        Optional<Node> maxExclusive = DocumentReader.getChild(node, "xsd:maxExclusive");
        if (maxExclusive.isPresent())
        {
            String val = DocumentReader.getAttribute(maxExclusive.get(), "value").get();
            if (Double.valueOf(value) >= Double.valueOf(val))
            {
                return "Value must be below " + val + ".";
            }
        }
        return null;
    }

    @Override
    default int compareTo(final ValueValidator o)
    {
        /*
         * CoupledValidators are sorted first in a SortedSet. This is to prevent the following: i) another validator finds an
         * attribute not valid, ii) the coupled validator is never called, if it would have been it would have coupled a node
         * and registered itself to the relevant node, iii) the relevant node value is changed, but the value pointing to it is
         * not updated as the registration of the coupled value was never done.
         */
        if (this instanceof CoupledValidator)
        {
            if (o != null && o instanceof CoupledValidator)
            {
                return 1; // no matter
            }
            return -1;
        }
        if (o instanceof CoupledValidator)
        {
            return 1;
        }
        return 1; // no matter
    }

}
