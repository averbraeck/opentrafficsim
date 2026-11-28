package org.opentrafficsim.editor;

/**
 * Formats serialized XML. Start tags are wrapped before they exceed {@link #MAX_LINE_LENGTH}, without changing XML content.
 * This keeps text, CDATA, comments, processing instructions and attribute values intact.
 * <p>
 * Copyright (c) 2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public final class XmlFormatter
{

    /** Maximum line length aimed for. Individual values that cannot be wrapped may exceed this. */
    public static final int MAX_LINE_LENGTH = OtsEditor.PROPERTIES_STORE.getIntegerOrDefault("xml.maxLineLength", 128);

    /** Indent used in saved XML. */
    public static final int INDENT = OtsEditor.PROPERTIES_STORE.getIntegerOrDefault("xml.indent", 2);

    /** Line separator used in saved XML. */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Constructor.
     */
    private XmlFormatter()
    {
    }

    /**
     * Formats serialized XML. A line separator is added after the XML declaration if the serializer omitted it. Start tags
     * are wrapped before they exceed {@link #MAX_LINE_LENGTH}; only whitespace between attributes is changed. Text, CDATA,
     * comments and processing instructions are copied verbatim, so values that cannot be wrapped may exceed the maximum line
     * length.
     * @param xml serialized XML
     * @return formatted XML
     */
    public static String format(final String xml)
    {
        StringBuilder result = new StringBuilder(xml.length() + MAX_LINE_LENGTH);
        int index = 0;
        int column = 0;
        while (index < xml.length())
        {
            char c = xml.charAt(index);
            if (c != '<')
            {
                result.append(c);
                column = c == '\n' || c == '\r' ? 0 : column + 1;
                index++;
            }
            else if (xml.startsWith("<!--", index))
            {
                index = appendUntil(xml, index, 4, "-->", result);
                column = columnAfterLastLineBreak(result);
            }
            else if (xml.startsWith("<![CDATA[", index))
            {
                index = appendUntil(xml, index, 9, "]]>", result);
                column = columnAfterLastLineBreak(result);
            }
            else if (xml.startsWith("<?", index))
            {
                index = appendUntil(xml, index, 2, "?>", result);
                column = columnAfterLastLineBreak(result);
            }
            else if (xml.startsWith("<!", index) || xml.startsWith("</", index))
            {
                index = appendMarkup(xml, index, result);
                column = columnAfterLastLineBreak(result);
            }
            else
            {
                int[] next = appendStartTag(xml, index, column, result);
                index = next[0];
                column = next[1];
            }
        }
        addDeclarationLineBreak(result);
        return result.toString();
    }

    /**
     * Appends markup up to and including the first occurrence of the end marker.
     * @param xml serialized XML
     * @param start index where the markup starts
     * @param prefixLength length of the markup prefix, which is skipped when looking for the end marker
     * @param endMarker marker that ends the markup
     * @param result result to append to
     * @return index just after the markup
     */
    private static int appendUntil(final String xml, final int start, final int prefixLength, final String endMarker,
            final StringBuilder result)
    {
        int end = xml.indexOf(endMarker, start + prefixLength);
        int next = end < 0 ? xml.length() : Math.min(xml.length(), end + endMarker.length());
        result.append(xml, start, next);
        return next;
    }

    /**
     * Appends a markup declaration or end tag, skipping over quotes and any internal subset.
     * @param xml serialized XML
     * @param start index where the markup starts
     * @param result result to append to
     * @return index just after the markup
     */
    private static int appendMarkup(final String xml, final int start, final StringBuilder result)
    {
        char quote = 0;
        int depth = 0;
        for (int index = start + 1; index < xml.length(); index++)
        {
            char c = xml.charAt(index);
            if (quote != 0)
            {
                if (c == quote)
                {
                    quote = 0;
                }
            }
            else if (c == '"' || c == '\'')
            {
                quote = c;
            }
            else if (c == '[')
            {
                depth++;
            }
            else if (c == ']')
            {
                depth--;
            }
            else if (c == '>' && depth <= 0)
            {
                int next = index + 1;
                result.append(xml, start, next);
                return next;
            }
        }
        result.append(xml, start, xml.length());
        return xml.length();
    }

    /**
     * Appends a start tag, wrapping before an attribute when that attribute and, for the last attribute, its closing delimiter
     * would exceed the maximum line length. Wrapped attributes use twice the normal indent to distinguish them from child
     * elements. Without attributes, the closing delimiter stays with the tag name. Attribute values are copied as a whole,
     * including quotes, so markup-like text inside an attribute is not mistaken for a new tag.
     * @param xml serialized XML
     * @param start index where the start tag starts
     * @param column column at which the start tag starts
     * @param result result to append to
     * @return array with the index just after the start tag and the resulting column
     */
    private static int[] appendStartTag(final String xml, final int start, final int column, final StringBuilder result)
    {
        int continuationIndent = lineIndent(result) + 2 * INDENT;
        int index = start + 1;
        result.append('<');
        int currentColumn = column + 1;
        while (index < xml.length())
        {
            char c = xml.charAt(index);
            if (Character.isWhitespace(c) || c == '>' || c == '/')
            {
                break;
            }
            result.append(c);
            currentColumn++;
            index++;
        }
        while (index < xml.length())
        {
            int whiteSpaceStart = index;
            while (index < xml.length() && Character.isWhitespace(xml.charAt(index)))
            {
                index++;
            }
            int whiteSpaceLength = index - whiteSpaceStart;
            if (index >= xml.length())
            {
                break;
            }
            char c = xml.charAt(index);
            if (c == '>' || c == '/')
            {
                int end = c == '/' ? Math.min(index + 2, xml.length()) : index + 1;
                result.append(xml, whiteSpaceStart, end);
                return new int[] {end, columnAfterLastLineBreak(result)};
            }
            int attributeEnd = attributeEnd(xml, index);
            int attributeLength = Math.max(1, attributeEnd - index);
            int blockEnd = closingEnd(xml, attributeEnd);
            int blockLength = blockEnd < 0 ? attributeLength : blockEnd - index;
            if (currentColumn + whiteSpaceLength + blockLength > MAX_LINE_LENGTH)
            {
                result.append(LINE_SEPARATOR);
                appendSpaces(result, continuationIndent);
                currentColumn = continuationIndent;
            }
            else
            {
                result.append(xml, whiteSpaceStart, index);
                currentColumn += whiteSpaceLength;
            }
            result.append(xml, index, index + attributeLength);
            currentColumn += attributeLength;
            index += attributeLength;
        }
        throw new IllegalArgumentException("Unterminated XML start tag.");
    }

    /**
     * Returns the index where the current attribute ends. Quoted attribute values are copied as a whole.
     * @param xml serialized XML
     * @param start index where the attribute starts
     * @return index just after the attribute
     */
    private static int attributeEnd(final String xml, final int start)
    {
        char quote = 0;
        for (int index = start; index < xml.length(); index++)
        {
            char c = xml.charAt(index);
            if (quote != 0)
            {
                if (c == quote)
                {
                    quote = 0;
                }
            }
            else if (c == '"' || c == '\'')
            {
                quote = c;
            }
            else if (Character.isWhitespace(c) || c == '>' || c == '/')
            {
                return index;
            }
        }
        return xml.length();
    }

    /**
     * Returns the index just after the closing delimiter when the current attribute is the last one.
     * @param xml serialized XML
     * @param attributeEnd index just after the attribute
     * @return index just after the closing delimiter, or -1 when another attribute follows
     */
    private static int closingEnd(final String xml, final int attributeEnd)
    {
        int index = attributeEnd;
        while (index < xml.length() && Character.isWhitespace(xml.charAt(index)))
        {
            index++;
        }
        if (index >= xml.length())
        {
            return -1;
        }
        char c = xml.charAt(index);
        if (c == '/')
        {
            return Math.min(index + 2, xml.length());
        }
        if (c == '>')
        {
            return index + 1;
        }
        return -1;
    }

    /**
     * Adds a line separator after the XML declaration if the serializer did not add one.
     * @param result serialized XML
     */
    private static void addDeclarationLineBreak(final StringBuilder result)
    {
        if (result.indexOf("<?xml") != 0)
        {
            return;
        }
        int end = result.indexOf("?>");
        if (end < 0)
        {
            return;
        }
        if (end + 2 == result.length())
        {
            result.append(LINE_SEPARATOR);
        }
        else
        {
            char c = result.charAt(end + 2);
            if (c != '\n' && c != '\r')
            {
                result.insert(end + 2, LINE_SEPARATOR);
            }
        }
    }

    /**
     * Returns the indentation of the line currently being appended.
     * @param result result appended so far
     * @return number of leading spaces on the current line
     */
    private static int lineIndent(final StringBuilder result)
    {
        int start = 0;
        for (int index = result.length() - 1; index >= 0; index--)
        {
            char c = result.charAt(index);
            if (c == '\n' || c == '\r')
            {
                start = index + 1;
                break;
            }
        }
        int indent = 0;
        while (start + indent < result.length() && result.charAt(start + indent) == ' ')
        {
            indent++;
        }
        return indent;
    }

    /**
     * Returns the number of characters after the last line break in the result.
     * @param result result appended so far
     * @return current column
     */
    private static int columnAfterLastLineBreak(final StringBuilder result)
    {
        for (int index = result.length() - 1; index >= 0; index--)
        {
            char c = result.charAt(index);
            if (c == '\n' || c == '\r')
            {
                return result.length() - index - 1;
            }
        }
        return result.length();
    }

    /**
     * Appends a number of spaces.
     * @param result result to append to
     * @param count number of spaces
     */
    private static void appendSpaces(final StringBuilder result, final int count)
    {
        for (int index = 0; index < count; index++)
        {
            result.append(' ');
        }
    }
}
