package org.opentrafficsim.editor.decoration;

import java.util.function.Predicate;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Sets the Id of a link when it is not defined yet and both NodeStart and NodeEnd are defined. An Id that follows the
 * {@code StartNode-EndNode} pattern is updated immediately when a node changes, including changes caused by coupled
 * node Id updates.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class AutomaticLinkId extends AbstractNodeDecoratorAttribute
{

    /**
     * Constructor.
     * @param editor editor.
     */
    public AutomaticLinkId(final OtsEditor editor)
    {
        super(editor, (node) -> node.isType(XsdPaths.LINK), "NodeStart", "NodeEnd");
    }

    /**
     * Constructor for sub classes.
     * @param editor editor.
     * @param predicate predicate to accept nodes that should have this attribute decorator.
     * @param attributes attributes to trigger on.
     */
    protected AutomaticLinkId(final OtsEditor editor, final Predicate<XsdTreeNode> predicate, final String... attributes)
    {
        super(editor, predicate, attributes);
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
    }

    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        String nodeStart = node.getAttributeValueOrDefault("NodeStart");
        String nodeEnd = node.getAttributeValueOrDefault("NodeEnd");
        if (nodeStart == null || nodeEnd == null)
        {
            return;
        }
        String start = debrace(nodeStart);
        String end = debrace(nodeEnd);
        String id = node.getAttributeValueOrDefault("Id");
        boolean startChanged = "NodeStart".equals(attribute);
        boolean endChanged = "NodeEnd".equals(attribute);
        if (!isPatternId(startChanged, endChanged, start, end, id))
        {
            return;
        }
        String newId = start + "-" + end;
        if (!newId.equals(id) && node.canSetId(newId))
        {
            node.setId(newId);
        }
    }

    /**
     * Returns whether the current Id follows the start-end pattern for a change of one side of the pattern.
     * @param startChanged whether the start value changed
     * @param endChanged whether the end value changed
     * @param start debraced start value
     * @param end debraced end value
     * @param id current Id
     * @return whether the Id follows the pattern
     */
    protected static boolean isPatternId(final boolean startChanged, final boolean endChanged, final String start,
            final String end, final String id)
    {
        if (id == null || id.isEmpty())
        {
            return true;
        }
        String currentId = debrace(id);
        if (endChanged)
        {
            String prefix = start + "-";
            return currentId.startsWith(prefix) && currentId.length() > prefix.length();
        }
        if (startChanged)
        {
            String suffix = "-" + end;
            return currentId.endsWith(suffix) && currentId.length() > suffix.length();
        }
        return false;
    }

    /**
     * Returns whether the current Id exactly follows the start-end pattern.
     * @param start debraced start value
     * @param end debraced end value
     * @param id current Id
     * @return whether the Id follows the pattern
     */
    protected static boolean isPatternId(final String start, final String end, final String id)
    {
        return id == null || id.isEmpty() || (start + "-" + end).equals(debrace(id));
    }

    /**
     * Returns a string with the { and } removed.
     * @param value candidate value.
     * @return string with the { and } removed
     */
    protected static String debrace(final String value)
    {
        return value.replace("{", "").replace("}", "");
    }

}
