package org.opentrafficsim.editor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.djutils.exceptions.Throw;
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
     * @param attribute String; "type" on normal calls, "base" on recursive calls for extended types.
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
            return reportNativeTypeNonCompliance(type, value);
        }

        Node simpleType;
        if (node.getNodeName().equals("xsd:attribute"))
        {
            simpleType = schema.getType(type.replace("ots:", ""));
        }
        else if (node.getNodeName().equals("xsd:element"))
        {
            simpleType = XsdSchema.getChild(node, "xsd:simpleType");
            if (simpleType == null)
            {
                Node complexType = XsdSchema.getChild(node, "xsd:complexType");
                Node simpleContent = XsdSchema.getChild(complexType, "xsd:simpleContent");
                Node extension = XsdSchema.getChild(simpleContent, "xsd:extension");
                return reportTypeNonCompliance(extension, "base", value, schema);
            }
        }
        else
        {
            Throw.when(!node.getNodeName().equals("xsd:simpleType"), RuntimeException.class,
                    "Unable to validate type of node %s.", node);
            simpleType = node;
        }

        // TODO: need to check the base <xsd:restriction base="base">? With a pattern or enumeration there might be no point.
        Node restriction = XsdSchema.getChild(simpleType, "xsd:restriction");
        if (restriction != null)
        {
            Node pattern = XsdSchema.getChild(restriction, "xsd:pattern");
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
            List<Node> enumerations = XsdSchema.getChildren(restriction, "xsd:enumeration");
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
        }

        return null;
    }

    /**
     * Report first encountered problem in validating a native type, e.g. xsd:int or xsd:anyURI.
     * @param type String; type.
     * @param value String; value.
     * @return String; first encountered problem in validating a native type, {@code null} if there is no problem.
     */
    private static String reportNativeTypeNonCompliance(final String type, final String value)
    {
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
                case "xsd:int": // 32-bit signed
                    Integer.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:long": // 64-bit signed
                    Long.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:unsignedInt": // 32-bits, i.e. max is 2^32 - 1 = 4294967295
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
                    Throw.when(!type.startsWith("ots:"), RuntimeException.class, "Type " + type + " cannot be validated.");
                    return null;
            }
        }
        catch (NumberFormatException exception)
        {
            if (type.length() > 5)
            {
                String t = type.replace("xsd:", "");
                return t.substring(0, 1).toUpperCase() + t.substring(1) + " value must be a valid number.";
            }
            return type + " value must be a valid number.";
        }
    }

}
