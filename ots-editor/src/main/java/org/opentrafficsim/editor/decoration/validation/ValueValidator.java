package org.opentrafficsim.editor.decoration.validation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.DocumentReader.NodeAnnotation;
import org.opentrafficsim.editor.Schema;
import org.opentrafficsim.editor.XiIncludeNode;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.ExpressionType;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.w3c.dom.Node;

/**
 * Interface for validators of element attributes and values. This class also provides many static utilities.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public interface ValueValidator extends Comparable<ValueValidator>
{

    /** Sub-class implementations should get their ordering ID from here. */
    AtomicLong NEXT_ID = new AtomicLong();

    /**
     * Returns a unique ordering ID among all {@link ValueValidator}, including beyond the specific sub class. Each class
     * implementing {@link ValueValidator} should obtain {@code NEXT_ID.incrementAndGet()} and store it, and return it in this
     * method. For example:
     *
     * <pre>
     * &#47;** Ordering id. **&#47;
     * private final long orderingId = NEXT_ID.incrementAndGet();
     *
     * &#64;Override
     * public long getOrderingId()
     * {
     *     return this.orderingId;
     * }
     * </pre>
     *
     * @return ordering ID
     */
    long getOrderingId();

    /**
     * Returns message why a value is invalid, or empty if the value is valid.
     * @param node supplied to verify with context, e.g. value combinations.
     * @return message why a value is invalid, or empty if the value is valid.
     */
    default Optional<String> validate(final XsdTreeNode node)
    {
        return ignoreValidation() ? Optional.empty() : validateDelegate(node);
    }

    /**
     * Whether to ignore validation.
     * @return whether to ignore validation
     */
    boolean ignoreValidation();

    /**
     * Returns message why a value is invalid, or empty if the value is valid. This method is called by {@link #validate} only
     * when validation should not be ignored.
     * @param node supplied to verify with context, e.g. value combinations.
     * @return message why a value is invalid, or empty if the value is valid.
     */
    Optional<String> validateDelegate(XsdTreeNode node);

    /**
     * Returns the options that a validator allows, typically an xsd:keyref returning defined values under the referred xsd:key
     * or xsd:unique. The field object is any object that a validator uses to know what particular information from the node is
     * required. The field object is stored in an {@link XsdTreeNode} when the validator is assigned to a particular attribute
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
            if (DocumentReader.getAttribute(xsdNode, "ref").isPresent())
            {
                throw new IllegalStateException("Validating value for node defined by ref=\"RefType\".");
            }
            // no children and no type, this is a plain tag, e.g. <Straight />, it needs no input.
            return Optional.empty();
        }
        if (value == null || value.isEmpty())
        {
            return Optional.of("Value is empty.");
        }
        return Optional.ofNullable(reportTypeNonCompliance(xsdNode, xsdNode, "type", schema, value, null, null));
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
        if (value == null || value.isEmpty())
        {
            String use = DocumentReader.getAttribute(xsdNode, "use").orElse(null);
            return Optional.ofNullable("required".equals(use) ? "Required value is empty." : null);
        }
        return Optional.ofNullable(reportTypeNonCompliance(xsdNode, xsdNode, "type", schema, value, null, null));
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
        reportTypeNonCompliance(xsdNode, xsdNode, "type", schema, null, restrictions, null);
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
        reportTypeNonCompliance(xsdNode, xsdNode, "type", schema, null, null, baseType);
        return baseType.get(baseType.size() - 1);
    }

    /**
     * Report first encountered problem in validating the value by a type, or when {@code value = null} scan all restrictions
     * and place them in the input list, and/or find the base type and store it in the base type list. The typeAttribute input
     * defines the attribute in the XSD node that may refer to a type containing restrictions. Initial invocation is typically:
     *
     * <pre>
     * reportTypeNonCompliance(xsdNode, xsdNode, "type", schema, ..., ..., ...);
     * </pre>
     *
     * Here, {@code ...} is either the input to trigger internal functionality (validation, listing restrictions, listing base
     * types), or {@code null} to skip the funtionality. This method recurs
     * @param appInfoNode node having possible xsd:appinfo for a message.
     * @param node type node.
     * @param typeAttribute "type" on normal calls, "base" or "memberTypes" on recursive calls.
     * @param schema schema for type retrieval.
     * @param value value, may be {@code null} (to have restrictions/base types returned).
     * @param restrictions list that xsd:restriction nodes will be placed in to, may be {@code null}.
     * @param baseType is filled by this method with encountered base types, e.g. xsd:double, may be {@code null}.
     * @return first encountered problem in validating the value by a type, {@code null} if there is no problem.
     */
    private static String reportTypeNonCompliance(final Node appInfoNode, final Node node, final String typeAttribute,
            final Schema schema, final String value, final List<Node> restrictions, final List<String> baseType)
    {
        // can request "base" or "memberTypes" on recursion
        String type = DocumentReader.getAttribute(node, typeAttribute).orElse(null);
        // multiple possible when memberTypes in xsd:union
        String[] types = type == null ? new String[0] : type.split("\\s+");
        List<String> reports = new ArrayList<>(types.length);
        for (String singleType : types)
        {
            reports.add(reportSingleTypeNonCompliance(singleType, schema, value, restrictions, baseType));
        }
        // reports of case of union with memberTypes="A B" are further appended for <simpleType>'s in the xsd:union case below
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
            {
                // complexType -> simpleContent | complexContent -> extension | restriction
                Node content = DocumentReader.getChild(node, "xsd:simpleContent").orElseGet(
                        () -> DocumentReader.getChild(node, "xsd:complexContent").orElseThrow(() -> new IllegalStateException(
                                "complexType does not contain simpleContent or complexContent")));
                Optional<Node> extension = DocumentReader.getChild(content, "xsd:extension");
                if (extension.isPresent())
                {
                    return reportTypeNonCompliance(extension.get(), extension.get(), "base", schema, value, restrictions,
                            baseType);
                }
                Node restriction = DocumentReader.getChild(content, "xsd:restriction").orElseThrow(
                        () -> new IllegalStateException("simpleContent contains neither extension nor restriction"));
                return reportTypeNonCompliance(appInfoNode, restriction, "base", schema, value, restrictions, baseType);
            }
            case "xsd:simpleType":
            {
                // simpleType -> union | restriction | list (not supported)
                Optional<Node> union = DocumentReader.getChild(node, "xsd:union");
                if (union.isPresent())
                {
                    return reportTypeNonCompliance(appInfoNode, union.get(), "memberTypes", schema, value, restrictions,
                            baseType);
                }
                Node restriction = DocumentReader.getChild(node, "xsd:restriction")
                        .orElseThrow(() -> new IllegalStateException("simpleType contains neither union nor restriction"));
                return reportTypeNonCompliance(appInfoNode, restriction, "base", schema, value, restrictions, baseType);
            }
            case "xsd:element":
            {
                // element -> {nothing} | complexType | simpleType (element defined with type="Type" caught before switch)
                if (node.getChildNodes().getLength() == 0)
                {
                    return null;
                }
                Optional<Node> complexType = DocumentReader.getChild(node, "xsd:complexType");
                if (complexType.isPresent())
                {
                    return reportTypeNonCompliance(complexType.get(), complexType.get(), "type", schema, value, restrictions,
                            baseType);
                }
                Node simpleType = DocumentReader.getChild(node, "xsd:simpleType").orElseThrow(
                        () -> new IllegalStateException("element contains neither type, complexType nor simpleType"));
                return reportTypeNonCompliance(simpleType, simpleType, "type", schema, value, restrictions, baseType);
            }
            case "xsd:attribute":
            {
                // attribute -> simpleType (attribute defined with type="Type" caught before switch, ref="Type" not supported)
                Node attributeSimpleType = DocumentReader.getChild(node, "xsd:simpleType").orElseThrow(() ->
                {
                    return new IllegalStateException("attribute contains neither type nor simpleType");
                });
                return reportTypeNonCompliance(attributeSimpleType, attributeSimpleType, "type", schema, value, restrictions,
                        baseType);
            }
            case "xsd:restriction":
            {
                if (value == null)
                {
                    if (restrictions != null)
                    {
                        restrictions.add(node);
                    }
                    return null;
                }
                return reportRestrictionNonCompliance(appInfoNode, node, value);
            }
            case "xsd:union":
            {
                // union -> simpleType (union with memberTypes="A B" filled reports above)
                if (baseType != null)
                {
                    baseType.add("xsd:union");
                }
                List<Node> simpleTypes = DocumentReader.getChildren(node, "xsd:simpleType");
                for (Node unionMemberType : simpleTypes)
                {
                    reports.add(reportTypeNonCompliance(node, unionMemberType, "type", schema, value, restrictions, baseType));
                }
                if (!reports.isEmpty() && !reports.contains(null)) // empty report = empty union, nothing to validate with
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
            }
            case "xi:include":
            case "xsd:choice":
                return null;
            default:
                throw new OtsRuntimeException("Unable to validate " + node.getNodeName() + ".");
        }
    }

    /**
     * Report non-compliance of a single type, e.g. for each in {@code memberTypes} of an {@code xsd:union}.
     * @param type type name.
     * @param schema schema for type retrieval.
     * @param value value.
     * @param restrictions list that xsd:restriction nodes will be placed in to.
     * @param baseType is filled by this method with encountered base types, e.g. xsd:double, may be {@code null}.
     * @return first encountered problem in validating the value by a type, {@code null} if there is no problem.
     */
    private static String reportSingleTypeNonCompliance(final String type, final Schema schema, final String value,
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
            Node typeNode = schema.getType(type).orElseThrow(() -> new IllegalStateException("Unknown type: " + type));
            String report = reportTypeNonCompliance(typeNode, typeNode, "base", schema, value, restrictions, baseType);
            if (value != null)
            {
                return report;
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
        String numberValueType = "number";
        try
        {
            switch (type)
            {
                case "xsd:string":
                    return null;
                case "xsd:boolean": // "true" or "false" or "1" or "0"
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false") && !value.equalsIgnoreCase("1")
                            && !value.equalsIgnoreCase("0"))
                    {
                        return "Boolean value must be \"true\" or \"false\".";
                    }
                    return null;
                case "xsd:double": // 64-bit
                    if ("INF".equals(value) || "-INF".equals(value))
                    {
                        return null;
                    }
                    Double.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:float": // 32-bit
                    if ("INF".equals(value) || "-INF".equals(value))
                    {
                        return null;
                    }
                    Float.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:decimal":
                    Throw.when(value.contains("e") || value.contains("E"), NumberFormatException.class, "invalid decimal");
                    new BigDecimal(value); // might throw NumberFormatException
                    return null;
                case "xsd:int": // 32-bit signed
                    numberValueType = "integer";
                    Integer.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:long": // 64-bit signed
                    numberValueType = "integer";
                    Long.valueOf(value); // might throw NumberFormatException
                    return null;
                case "xsd:unsignedInt": // 32-bits, i.e. max is 2^32 - 1 = 4294967295
                    numberValueType = "integer";
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
                    numberValueType = "integer";
                    if (new BigInteger(value).signum() <= 0) // might throw NumberFormatException
                    {
                        return "Integer value must be a positive integer.";
                    }
                    return null;
                case "xsd:integer": // arbitrary length
                    numberValueType = "integer";
                    new BigInteger(value); // might throw NumberFormatException
                    return null;
                case "xsd:anyURI": // RFC2396 compliant, just as URI in java (URI is slightly more restrictive in edge cases)
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
                        if (!Cache.SUPPRESS_ERRORS.contains(message))
                        {
                            Logger.ots().error(message);
                            Cache.SUPPRESS_ERRORS.add(message);
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
                return t.substring(0, 1).toUpperCase() + t.substring(1) + " value must be a valid " + numberValueType + ".";
            }
            return type + " value must be a valid " + numberValueType + ".";
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
        // does not check: length, minLength, maxLength, fractionDigits, totalDigits
        // (whiteSpace is not really a check, but is applied when values are set in tree nodes)
        for (String facet : new String[] {"xsd:length", "xsd:minLength", "xsd:maxLength", "xsd:fractionDigits",
                "xsd:totalDigits"})
        {
            Optional<Node> facetNode = DocumentReader.getChild(node, facet);
            if (facetNode.isPresent())
            {
                String facetValue = DocumentReader.getAttribute(facetNode.get(), "value").orElse("<unknown>");
                String message = "Restriction facet " + facet + "=\"" + facetValue + "\" is not validated.";
                if (!Cache.SUPPRESS_ERRORS.contains(message))
                {
                    Logger.ots().error(message);
                    Cache.SUPPRESS_ERRORS.add(message);
                }
            }
        }

        Optional<Node> pattern = DocumentReader.getChild(node, "xsd:pattern");
        if (pattern.isPresent())
        {
            String patternString = DocumentReader.getAttribute(pattern.get(), "value")
                    .orElseThrow(() -> new IllegalStateException("pattern does not define a value"));
            try
            {
                Pattern compiledPattern = getPattern(patternString);
                if (!compiledPattern.matcher(value).matches())
                {
                    Optional<String> patternMessage = NodeAnnotation.APPINFO_PATTERN.get(appInfoNode);
                    return patternMessage.isEmpty() ? "Value does not match pattern " + patternString : patternMessage.get();
                }
            }
            catch (PatternSyntaxException exception)
            {
                if (!Cache.SUPPRESS_ERRORS.contains(patternString))
                {
                    Logger.ots().error("Could not validate value by pattern due to a PatternSyntaxException."
                            + " This means the pattern is not valid.");
                    Logger.ots().error(exception.getMessage());
                    Cache.SUPPRESS_ERRORS.add(patternString);
                }
            }
        }

        List<Node> enumerations = DocumentReader.getChildren(node, "xsd:enumeration");
        List<String> options = new ArrayList<>();
        for (Node enumeration : enumerations)
        {
            options.add(DocumentReader.getAttribute(enumeration, "value")
                    .orElseThrow(() -> new IllegalStateException("enumeration does not define a value")));
        }
        if (!options.isEmpty() && !options.contains(value))
        {
            String arrayString = options.toString();
            return "Must be any of " + arrayString.substring(1, arrayString.length() - 1) + ".";
        }

        return reportLimitNonCompliance(node, value);
    }

    /**
     * Reports value not complying to numerical limits.
     * @param node node, must be an xsd:restriction
     * @param value value
     * @return value not complying to numerical limits
     */
    private static String reportLimitNonCompliance(final Node node, final String value)
    {
        Optional<Node> minInclusive = DocumentReader.getChild(node, "xsd:minInclusive");
        Optional<Node> minExclusive = DocumentReader.getChild(node, "xsd:minExclusive");
        Optional<Node> maxInclusive = DocumentReader.getChild(node, "xsd:maxInclusive");
        Optional<Node> maxExclusive = DocumentReader.getChild(node, "xsd:maxExclusive");
        if (minInclusive.isEmpty() && minExclusive.isEmpty() && maxInclusive.isEmpty() && maxExclusive.isEmpty())
        {
            return null;
        }
        boolean isInf = "INF".equals(value);
        boolean isNegInf = "-INF".equals(value);
        boolean isNaN = "NaN".equals(value);
        BigDecimal actual = null;
        if (!isInf && !isNegInf && !isNaN)
        {
            actual = new BigDecimal(value);
        }
        if (minInclusive.isPresent() && !isInf)
        {
            String val = DocumentReader.getAttribute(minInclusive.get(), "value")
                    .orElseThrow(() -> new IllegalStateException("minInclusive does not define a value"));
            if (isNaN || isNegInf || actual.compareTo(getLimit(val)) < 0)
            {
                return "Value must be above or equal to " + val + ".";
            }
        }
        if (minExclusive.isPresent() && !isInf)
        {
            String val = DocumentReader.getAttribute(minExclusive.get(), "value")
                    .orElseThrow(() -> new IllegalStateException("minExclusive does not define a value"));
            if (isNaN || isNegInf || actual.compareTo(getLimit(val)) <= 0)
            {
                return "Value must be above " + val + ".";
            }
        }
        if (maxInclusive.isPresent() && !isNegInf)
        {
            String val = DocumentReader.getAttribute(maxInclusive.get(), "value")
                    .orElseThrow(() -> new IllegalStateException("maxInclusive does not define a value"));
            if (isNaN || isInf || actual.compareTo(getLimit(val)) > 0)
            {
                return "Value must be below or equal to " + val + ".";
            }
        }
        if (maxExclusive.isPresent() && !isNegInf)
        {
            String val = DocumentReader.getAttribute(maxExclusive.get(), "value")
                    .orElseThrow(() -> new IllegalStateException("maxExclusive does not define a value"));
            if (isNaN || isInf || actual.compareTo(getLimit(val)) >= 0)
            {
                return "Value must be below " + val + ".";
            }
        }
        return null;
    }

    /**
     * Return limit.
     * @param val string value
     * @return big decimal value
     * @throws IllegalStateException if the limit value is INF, -INF or NaN
     */
    private static BigDecimal getLimit(final String val)
    {
        // xsd:double and xsd:float allow INF, -INF and NaN, which BigDecimal does not, but these values make no sense as limit
        Throw.when("INF".equals(val) || "-INF".equals(val) || "NaN".equals(val), IllegalStateException.class,
                "restriction limit %s is not sensical and cannot be processed", val);
        return new BigDecimal(val);
    }

    /**
     * Returns pattern from cache or by creating it.
     * @param patternString pattern
     * @return pattern from cache or by creating it
     */
    private static Pattern getPattern(final String patternString)
    {
        return Cache.PATTERN_CACHE.computeIfAbsent(patternString, Pattern::compile);
    }

    /**
     * Validate value or attribute of node, including expression evaluation. This results in two validation domains:
     * <ol>
     * <li>Stored domain: validates the value as stored and checks compatibility with XSD semantics. Type constraints and
     * key/keyref constraints are applied in this domain.</li>
     * <li>Effective domain: validates the effective value. For expressions this is the evaluated result; for ordinary values
     * this is the value itself. Type constraints, key/keyref constraints, and custom validators are applied in this
     * domain.</li>
     * </ol>
     * For non-expression values, the stored and effective value are identical. The stored domain validation is then skipped, as
     * it overlaps completely with the effective domain. For expression values the stored value is the expression string, and
     * the effective value is its evaluated result.
     * @param treeNode node
     * @param isExpression whether the current value is an expression
     * @param valueGetter returns the current value
     * @param valueSetter sets a temporary value
     * @param adapterSupplier supplies the adapter pertaining to the value type
     * @param validators validators for the value or attribute
     * @param typeValueValidation supplies validation of the currently set value by type restrictions (e.g. positive double)
     * @return invalid message, of any
     */
    static Optional<String> reportInvalidWithExpression(final XsdTreeNode treeNode, final boolean isExpression,
            final Supplier<String> valueGetter, final Consumer<String> valueSetter,
            final Supplier<Optional<ExpressionAdapter<?, ?>>> adapterSupplier, final SortedSet<ValueValidator> validators,
            final Supplier<Optional<String>> typeValueValidation)
    {
        String originalValue = valueGetter.get();
        boolean expressionValueSet = false;
        Optional<String> result = Optional.empty();
        try
        {
            if (originalValue != null && !originalValue.isEmpty())
            {
                // if there is an expression and an adapter for the type, try to evaluate the expression and set the result
                if (isExpression)
                {
                    Optional<ExpressionAdapter<?, ?>> adapter = adapterSupplier.get();
                    if (adapter.isPresent())
                    {
                        ExpressionResult expressionResult =
                                validateExpression(originalValue, adapter.get(), treeNode.getRoot().getEval());
                        if (expressionResult.value() == null)
                        {
                            result = Optional.of(expressionResult.exception());
                        }
                        else
                        {
                            expressionValueSet = true;
                            valueSetter.accept(expressionResult.value());
                        }
                    }
                }
                // apply validators on value, whether as was stored, or derived from expression
                if (result.isEmpty())
                {
                    for (ValueValidator validator : validators)
                    {
                        Optional<String> message = validator.validate(treeNode);
                        if (message.isPresent())
                        {
                            result = Optional.of(expressionValueSet
                                    ? "Expression value " + valueGetter.get() + ": " + message.get() : message.get());
                            break;
                        }
                    }
                }
                // type value validation on expression result
                if (result.isEmpty() && expressionValueSet && valueGetter.get() != null)
                {
                    result = typeValueValidation.get();
                }
            }
        }
        finally
        {
            if (expressionValueSet)
            {
                // reset value from expression result to expression itself
                valueSetter.accept(originalValue);
            }
        }
        if (result.isEmpty() && expressionValueSet)
        {
            // validate with XPathValidators on the original expression as these need to be checked to schema compliance
            for (ValueValidator validator : validators)
            {
                if (validator instanceof XPathValidator)
                {
                    Optional<String> message = validator.validate(treeNode);
                    if (message.isPresent())
                    {
                        result = message;
                        break;
                    }
                }
            }
        }
        // type value validation on expression or regular value
        if (result.isEmpty())
        {
            result = typeValueValidation.get();
        }
        return result;
    }

    /**
     * Checks that the expression evaluates, that the resulting object is proper input to the adapter for the value type, and
     * returns the value as string for further validation.
     * @param <T> value type
     * @param <E> expression type
     * @param expression expression
     * @param adapter adapter
     * @param eval evaluator
     * @return expression result, which is either the value, or an exception message
     */
    private static <T, E extends ExpressionType<T>> ExpressionResult validateExpression(final String expression,
            final ExpressionAdapter<T, E> adapter, final Eval eval)
    {
        // DurationType = PositiveDurationAdapter.unmarshal(String)
        // this.value is an expression, so DurationType will wrap an expression
        E eExpression = adapter.unmarshal(expression);
        // Duration = DurationType.get(Eval)
        // we now have an actual Duration from the eval, wherever it came from (e.g. String or Duration param)
        T t;
        try
        {
            t = eExpression.get(eval);
        }
        catch (IllegalArgumentException ex)
        {
            // expression did not evaluate
            String message = ex.getMessage();
            return new ExpressionResult(null, message == null ? "Expression could not be evaluated." : message);
        }
        try
        {
            if (eExpression instanceof StringType)
            {
                // StringType = new StringType(String, false), i.e. not an expression string
                // String = StringAdapter.marshal(StringType), i.e. String as value, not as expression
                String val = ((StringAdapter) adapter).marshal(new StringType((String) t, false));
                return new ExpressionResult(val, null);
            }
            else
            {
                // DurationType = new DurationType(Duration)
                @SuppressWarnings("unchecked")
                E eValue = (E) eExpression.getClass().getConstructor(t.getClass()).newInstance(t);
                // String = PositiveDurationAdapter.marshal(DurationType)
                return new ExpressionResult(adapter.marshal(eValue), null);
            }
        }
        catch (NoSuchElementException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException ex)
        {
            return new ExpressionResult(null, "Expression could not be evaluated.");
        }
    }

    /**
     * Result of expression evaluation, which is either the value or an exception message.
     * @param value value
     * @param exception exception message
     */
    record ExpressionResult(String value, String exception)
    {
    };

    /**
     * Returns the whitespace treatment for the value defining input node.
     * @param xsdNode node
     * @param schema schema
     * @return whitespace treatment for the value defining input node
     */
    static Whitespace getWhiteSpace(final Node xsdNode, final Schema schema)
    {
        if (xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            return Whitespace.NONE;
        }
        List<Node> restrictions = getRestrictions(xsdNode, schema);
        for (int i = restrictions.size() - 1; i >= 0; i--)
        {
            Node restriction = restrictions.get(i);
            Optional<Node> whiteSpace = DocumentReader.getChild(restriction, "xsd:whiteSpace");
            if (whiteSpace.isPresent())
            {
                String value = DocumentReader.getAttribute(whiteSpace.get(), "value")
                        .orElseThrow(() -> new IllegalStateException("whiteSpace does not contain value"));
                return Whitespace.valueOf(value.toUpperCase());
            }
        }
        return Whitespace.NONE;
    }

    /**
     * Whitespace treatment of node and attribute values in XML.
     */
    enum Whitespace
    {
        /** Not specified (same as preserve). */
        NONE,

        /** Preserve whitespace-like characters. */
        PRESERVE,

        /** Replace tab, line feed, and carriage return with space. */
        REPLACE,

        /** Replace tab, line feed, and carriage return with space, trim, and remove consecutive spaces. */
        COLLAPSE;

        /**
         * Parse string to comply to whitespace treatment.
         * @param value input value
         * @return output value with whitespace treatment
         */
        public String parse(final String value)
        {
            if (value == null)
            {
                return null;
            }
            switch (this)
            {
                case COLLAPSE:
                    return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim().replaceAll(" +", " ");
                case REPLACE:
                    return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
                case PRESERVE:
                case NONE:
                default:
                    return value;
            }
        }
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
        if (this == o)
        {
            return 0;
        }
        boolean thisIsCoupledValidator = this instanceof CoupledValidator;
        if (thisIsCoupledValidator != (o instanceof CoupledValidator))
        {
            return thisIsCoupledValidator ? -1 : 1;
        }
        // Same category: provide a stable ordering.
        return Long.compare(getOrderingId(), o.getOrderingId());
    }

    /**
     * Holder of private static caches for static methods.
     */
    final class Cache
    {
        /** Pattern cache. */
        private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

        /** Set to store tags for which an error has been printed, to prevent repeated printing on repeated validation. */
        private static final Set<String> SUPPRESS_ERRORS = ConcurrentHashMap.newKeySet();
    }

}
