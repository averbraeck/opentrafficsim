package org.opentrafficsim.editor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.w3c.dom.Node;

/**
 * Utility class to validate values using XSD nodes and an XSD schema.
 * @author wjschakel
 */
public final class ValueValidator
{

    /** Set to store tags for which an error has been printed, to prevent repeated printing on repeated validation. */
    private static Set<String> suppressError = new LinkedHashSet<>();

    /**
     * Private constructor.
     */
    private ValueValidator()
    {

    }

    /**
     * Report first encountered problem in validating the value of the node.
     * @param xsdNode Node; node.
     * @param value String; value.
     * @param schema XsdSchema; schema for type retrieval.
     * @return String; first encountered problem in validating the value of the node, {@code null} if there is no problem.
     */
    public static String reportInvalidValue(final Node xsdNode, final String value, final XsdSchema schema)
    {
        if (xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            // TODO: how to check xi:includes?
            return null;
        }
        if (xsdNode.getChildNodes().getLength() == XsdSchema.getChildren(xsdNode, "#text").size()
                && XsdSchema.getAttribute(xsdNode, "type") == null)
        {
            // no children and no type, this is a plain tag, e.g. <STRAIGHT />, it needs no input.
            return null;
        }
        if (value == null || value.isBlank())
        {
            return "Value is empty.";
        }
        return reportTypeNonCompliance(xsdNode, "type", value, schema);
    }

    /**
     * Report first encountered problem in validating the attribute value.
     * @param xsdNode Node; node, should be an xsd:attribute.
     * @param value String; value.
     * @param schema XsdSchema; schema for type retrieval.
     * @return String; first encountered problem in validating the attribute value, {@code null} if there is no problem.
     */
    public static String reportInvalidAttributeValue(final Node xsdNode, final String value, final XsdSchema schema)
    {
        String use = XsdSchema.getAttribute(xsdNode, "use");
        if ("required".equals(use) && (value == null || value.isBlank()))
        {
            return "Required value is empty.";
        }
        if (value == null || value.isBlank())
        {
            return null;
        }
        return reportTypeNonCompliance(xsdNode, "type", value, schema);
    }

    /**
     * Report first encountered problem in validating the value by a type.
     * @param node Node; type node.
     * @param attribute String; "type" on normal calls, "base" on recursive calls.
     * @param value String; value.
     * @param schema XsdSchema; schema for type retrieval.
     * @return String; first encountered problem in validating the value by a type, {@code null} if there is no problem.
     */
    private static String reportTypeNonCompliance(final Node node, final String attribute, final String value,
            final XsdSchema schema)
    {
        String type = XsdSchema.getAttribute(node, attribute); // can request "base" on recursion
        boolean isNativeType = type != null && type.startsWith("xsd:");
        if (isNativeType)
        {
            String report = reportNativeTypeNonCompliance(type, value);
            if (report != null || !node.getNodeName().equals("xsd:restriction"))
            {
                return report;
            }
        }
        if (type != null && !isNativeType)
        {
            String report = reportTypeNonCompliance(schema.getType(type), "base", value, schema);
            if (report != null)
            {
                return report;
            }
            if (!node.getNodeName().equals("xsd:restriction"))
            {
                return null;
            }
        }

        switch (node.getNodeName())
        {
            case "xsd:complexType":
                Node simpleContent = XsdSchema.getChild(node, "xsd:simpleContent");
                Node extension = XsdSchema.getChild(simpleContent, "xsd:extension");
                if (extension != null)
                {
                    return reportTypeNonCompliance(extension, "base", value, schema);
                }
                return reportTypeNonCompliance(XsdSchema.getChild(simpleContent, "xsd:restriction"), "base", value, schema);
            case "xsd:simpleType":
                return reportTypeNonCompliance(XsdSchema.getChild(node, "xsd:restriction"), "base", value, schema);
            case "xsd:element":
                Node complexType = XsdSchema.getChild(node, "xsd:complexType");
                if (complexType != null)
                {
                    return reportTypeNonCompliance(complexType, "type", value, schema);
                }
                Node simpleType = XsdSchema.getChild(node, "xsd:simpleType");
                return reportTypeNonCompliance(simpleType, "type", value, schema);
            case "xsd:attribute":
                return reportTypeNonCompliance(XsdSchema.getChild(node, "xsd:simpleType"), "type", value, schema);
            case "xsd:restriction":
                return reportRestrictionNonCompliance(node, value);
            default:
                throw new RuntimeException("Unable to validate " + node.getNodeName() + ".");
        }
    }

    /**
     * Report first encountered problem in validating a native type, e.g. xsd:int or xsd:anyURI.
     * @param type String; type.
     * @param value String; value.
     * @return String; first encountered problem in validating a native type, {@code null} if there is no problem.
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
                    if (Long.valueOf(value) < 0) // might throw NumberFormatException
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
                        if (!suppressError.contains(message))
                        {
                            System.err.println(message);
                            suppressError.add(message);
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
     * @param node Node; node, must be an xsd:restriction.
     * @param value String; value.
     * @return String; first encountered problem in validating the value by a restriction.
     */
    private static String reportRestrictionNonCompliance(final Node node, final String value)
    {
        Node pattern = XsdSchema.getChild(node, "xsd:pattern");
        if (pattern != null)
        {
            String patternString = XsdSchema.getAttribute(pattern, "value");
            try
            {
                if (!Pattern.matches(patternString, value))
                {
                    return "Value does not match pattern " + patternString;
                }
            }
            catch (PatternSyntaxException exception)
            {
                if (!suppressError.contains(patternString))
                {
                    System.err.println("Could not validate value by pattern due to a PatternSyntaxException."
                            + " This means the pattern is not valid.");
                    System.err.println(exception.getMessage());
                    suppressError.add(patternString);
                }
            }
        }
        List<Node> enumerations = XsdSchema.getChildren(node, "xsd:enumeration");
        List<String> options = new ArrayList<>();
        for (Node enumeration : enumerations)
        {
            options.add(XsdSchema.getAttribute(enumeration, "value"));
        }
        if (!options.isEmpty() && !options.contains(value))
        {
            String arrayString = options.toString();
            return "Must be any of " + arrayString.substring(1, arrayString.length() - 1) + ".";
        }
        Node minInclusive = XsdSchema.getChild(node, "xsd:minInclusive");
        if (minInclusive != null)
        {
            String val = XsdSchema.getAttribute(minInclusive, "value");
            if (Double.valueOf(value) < Double.valueOf(val))
            {
                return "Value must be above or equal to " + val + ".";
            }
        }
        Node minExclusive = XsdSchema.getChild(node, "xsd:minExclusive");
        if (minExclusive != null)
        {
            String val = XsdSchema.getAttribute(minExclusive, "value");
            if (Double.valueOf(value) <= Double.valueOf(val))
            {
                return "Value must be above " + val + ".";
            }
        }
        Node maxInclusive = XsdSchema.getChild(node, "xsd:maxInclusive");
        if (maxInclusive != null)
        {
            String val = XsdSchema.getAttribute(maxInclusive, "value");
            if (Double.valueOf(value) > Double.valueOf(val))
            {
                return "Value must be below or equal to " + val + ".";
            }
        }
        Node maxExclusive = XsdSchema.getChild(node, "xsd:maxExclusive");
        if (maxExclusive != null)
        {
            String val = XsdSchema.getAttribute(maxExclusive, "value");
            if (Double.valueOf(value) >= Double.valueOf(val))
            {
                return "Value must be below " + val + ".";
            }
        }
        return null;
    }

}
