package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.eval.Eval;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeUtil;
import org.opentrafficsim.editor.extensions.Adapters;
import org.opentrafficsim.road.network.StripeData.StripePhaseSync;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;
import org.opentrafficsim.xml.bindings.types.ExpressionType;

/**
 * Utilities to resolve stripe definitions in the editor and turn them in to stripe elements.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
final class MapStripeDefinitionUtil
{

    /**
     * Constructor.
     */
    private MapStripeDefinitionUtil()
    {
        //
    }

    /**
     * Creates stripe elements from element nodes. Invalid values are ignored without affecting link validity.
     * @param elementNodes element nodes
     * @param eval expression evaluator
     * @return stripe elements
     */
    static List<StripeElement> createStripeElements(final List<XsdTreeNode> elementNodes, final Eval eval)
    {
        return createStripeElements(elementNodes, eval, () ->
        {
        });
    }

    /**
     * Creates stripe elements from element nodes.
     * @param elementNodes element nodes
     * @param eval expression evaluator
     * @param onInvalid execution for an invalid value
     * @return stripe elements
     */
    static List<StripeElement> createStripeElements(final List<XsdTreeNode> elementNodes, final Eval eval,
            final Runnable onInvalid)
    {
        List<StripeElement> elements = new ArrayList<>();
        ExpressionAdapter<Length, ExpressionType<Length>> lengthAdapter = Adapters.get(Length.class);
        for (XsdTreeNode elementNode : elementNodes)
        {
            if (elementNode.isValid())
            {
                Length w = lengthAdapter.unmarshal(elementNode.getAttributeValueOrDefault("Width")).get(eval);
                if (elementNode.getNodeName().equals("Continuous") || elementNode.getNodeName().equals("Dashed"))
                {
                    String colorName = elementNode.getAttributeValueOrDefault("Color");
                    Color color = Adapters.get(Color.class).unmarshal(colorName).get(eval);
                    if (elementNode.getNodeName().equals("Continuous"))
                    {
                        elements.add(StripeElement.continuous(w, color));
                    }
                    else
                    {
                        List<Double> gapsAndDashes = new ArrayList<>();
                        for (XsdTreeNode gapDash : elementNode.getChildren())
                        {
                            Double gap = getLengthValue(gapDash.getChild(0), lengthAdapter, eval, onInvalid);
                            Double dash = getLengthValue(gapDash.getChild(1), lengthAdapter, eval, onInvalid);
                            if (gap != null)
                            {
                                gapsAndDashes.add(gap);
                            }
                            if (dash != null)
                            {
                                gapsAndDashes.add(dash);
                            }
                        }
                        if (!gapsAndDashes.isEmpty())
                        {
                            elements.add(StripeElement.dashed(w, color,
                                    new LengthVector(gapsAndDashes.stream().mapToDouble(v -> v).toArray())));
                        }
                    }
                }
                else
                {
                    elements.add(StripeElement.gap(w));
                }
            }
        }
        return elements;
    }

    /**
     * Returns the length value of a node, or {@code null} if the node has no valid value.
     * @param node node with the value
     * @param adapter length expression adapter
     * @param eval expression evaluator
     * @param onInvalid execution for an invalid value
     * @return length in SI units, or {@code null} if the node has no valid value
     */
    private static Double getLengthValue(final XsdTreeNode node,
            final ExpressionAdapter<Length, ExpressionType<Length>> adapter, final Eval eval, final Runnable onInvalid)
    {
        if (node == null)
        {
            return null;
        }
        try
        {
            String value = node.getValueOrDefault();
            return value == null ? null : adapter.unmarshal(value).get(eval).si;
        }
        catch (IllegalArgumentException exception)
        {
            onInvalid.run();
            return null;
        }
    }

    /**
     * Resolves the node that contains the stripe data for a stripe in a road layout.
     * @param stripeNode node Stripe in a road layout.
     * @return node with stripe data, {@code null} if the reference could not be resolved.
     */
    static XsdTreeNode resolveStripeNode(final XsdTreeNode stripeNode)
    {
        if (stripeNode == null)
        {
            return null;
        }
        if (stripeNode.hasAttribute("StripeType"))
        {
            // Definition layouts refer to a StripeType through an attribute instead of a DefinedStripe child.
            XsdTreeNode coupled = stripeNode.getCoupledNodeAttribute("StripeType").orElse(null);
            if (coupled != null)
            {
                return coupled;
            }
            return XsdTreeNodeUtil.findNodeById(stripeNode.getRoot(), "StripeType",
                    stripeNode.getAttributeValueOrDefault("StripeType"));
        }
        XsdTreeNode defined = XsdTreeNodeUtil.findActiveChild(stripeNode, "DefinedStripe");
        if (defined != null)
        {
            return defined.getCoupledNodeValue().orElse(null);
        }
        return XsdTreeNodeUtil.findActiveChild(stripeNode, "Custom");
    }

    /**
     * Collect stripe data.
     * @param stripeNode node where the stripe data is defined.
     * @param overrideNode node with custom overrides, can be {@code null}.
     * @param eval expression evaluator
     * @return collected stripe data.
     */
    static StripeDefinition collectStripeData(final XsdTreeNode stripeNode, final XsdTreeNode overrideNode, final Eval eval)
    {
        StripePhaseSync phaseSync = StripePhaseSync.NONE;
        Length dashOffset = Length.ZERO;
        StripeLateralSync lateralSync = StripeLateralSync.LINK;
        List<XsdTreeNode> elementNodes;

        XsdTreeNode dashOffsetNode = XsdTreeNodeUtil.findActiveChild(overrideNode, "DashOffset");
        if (dashOffsetNode == null)
        {
            dashOffsetNode = XsdTreeNodeUtil.findActiveChild(stripeNode, "DashOffset");
        }
        if (dashOffsetNode != null && dashOffsetNode.getChildCount() > 0)
        {
            try
            {
                XsdTreeNode phaseNode = dashOffsetNode.getChild(0);
                switch (phaseNode.getNodeName())
                {
                    case "SyncUpstream":
                    {
                        phaseSync = StripePhaseSync.UPSTREAM;
                        break;
                    }
                    case "SyncDownstream":
                    {
                        phaseSync = StripePhaseSync.DOWNSTREAM;
                        break;
                    }
                    case "Fixed":
                    {
                        phaseSync = StripePhaseSync.NONE;
                        dashOffset =
                                Adapters.get(Length.class).unmarshal(phaseNode.getAttributeValueOrDefault("Offset")).get(eval);
                        break;
                    }
                    default:
                    {
                        Logger.ots().warn("Dash synchronization " + phaseNode.getNodeName() + " is unknown.");
                    }
                }
            }
            catch (Exception e)
            {
                // ignore
            }
        }
        String latSyncName = overrideNode != null && overrideNode.hasAttribute("LateralSync")
                ? overrideNode.getAttributeValueOrDefault("LateralSync") : null;
        if (latSyncName == null && stripeNode.hasAttribute("LateralSync"))
        {
            latSyncName = stripeNode.getAttributeValueOrDefault("LateralSync");
        }
        if (latSyncName != null)
        {
            lateralSync = Adapters.get(StripeLateralSync.class).unmarshal(latSyncName).get(eval);
        }

        XsdTreeNode elements = XsdTreeNodeUtil.findActiveChild(overrideNode, "Elements");
        if (elements == null)
        {
            elements = XsdTreeNodeUtil.findActiveChild(stripeNode, "Elements");
        }
        elementNodes = elements == null ? Collections.emptyList() : elements.getChildren();

        return new StripeDefinition(phaseSync, dashOffset, lateralSync, elementNodes);
    }

}
