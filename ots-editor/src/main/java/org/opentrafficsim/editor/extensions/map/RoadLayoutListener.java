package org.opentrafficsim.editor.extensions.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.Adapters;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.CseData;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.OffsetElement;

/**
 * Listens to changes in a road layout. Notifies all listeners that need to repaint. This also provides the offsets of the road
 * layout.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class RoadLayoutListener extends ChangeListener<Map<XsdTreeNode, CseData>>
{

    /** */
    private static final long serialVersionUID = 20231114L;

    /**
     * Constructor.
     * @param layoutNode XsdTreeNode; node of the layout, either in definitions or under a link.
     * @param eval Supplier&lt;Eval&gt;; supplier of expression evaluator, either from the main map, or from a map link data.
     */
    public RoadLayoutListener(final XsdTreeNode layoutNode, final Supplier<Eval> eval)
    {
        super(layoutNode, eval);
    }

    /**
     * Obtain a {@code Length} from the value of the given node. This method will return {@code null} for invalid values.
     * @param valueNode XsdTreeNode; node with value that is a length.
     * @return Length; value of the given node.
     */
    private Length getLength(final XsdTreeNode valueNode)
    {
        try
        {
            return valueNode.isActive() ? Adapters.getAdapter(Length.class).unmarshal(valueNode.getValue()).get(getEval())
                    : null;
        }
        catch (RuntimeException exception)
        {
            // probably an invalid length value specified
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void destroyData()
    {
        getData().clear();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean canBeIgnored(final XsdTreeNode node)
    {
        return node.getNodeName().equals("SpeedLimit");
    }

    /**
     * Returns the offsets of the layout this listener is listening to. This is cached so defined road layouts can be
     * efficiently used.
     * @return Map&lt;XsdTreeNode, CseData&gt;; offsets.
     */
    @Override
    Map<XsdTreeNode, CseData> calculateData()
    {
        Map<XsdTreeNode, CseData> cseData = new LinkedHashMap<>();
        List<CseData> cseList = new ArrayList<>();
        Map<Object, Integer> indices = new LinkedHashMap<>();
        List<XsdTreeNode> children = getNode().getChildren();
        Iterator<OffsetElement> iterator = new Iterator<>()
        {
            /** Index. */
            private int index = 0;

            /** Cached start width. */
            private Length widthStart;

            /** Cached end width. */
            private Length widthEnd;

            /** Cached start offset. */
            private Length offsetStart;

            /** Cached end offset. */
            private Length offsetEnd;

            /** {@inheritDoc} */
            @Override
            public boolean hasNext()
            {
                if (this.index >= children.size())
                {
                    return false;
                }
                while (!children.get(this.index).isValid() || !children.get(this.index).isActive()
                        || children.get(this.index).getNodeName().equals("SpeedLimit"))
                {
                    this.index++;
                    if (this.index >= children.size())
                    {
                        return false;
                    }
                }

                // the following performs a further check whether the next element is ready
                try
                {
                    XsdTreeNode node = children.get(this.index);
                    if (node.getNodeName().equals("Stripe"))
                    {
                        this.widthStart = Length.ZERO;
                        this.widthEnd = Length.ZERO;
                        if (!node.getChild(0).getNodeName().equals("xsd:sequence"))
                        {
                            // CenterOffset
                            this.offsetStart = getLength(node.getChild(0));
                            this.offsetEnd = this.offsetStart;
                        }
                        else if (node.getChild(0).isActive())
                        {
                            // CenterOffsetStart and CenterOffsetEnd
                            this.offsetStart = getLength(node.getChild(0).getChild(0));
                            this.offsetEnd = getLength(node.getChild(0).getChild(1));
                        }
                        else
                        {
                            this.offsetStart = null;
                            this.offsetEnd = null;
                        }
                        getLength(node.getChild(1)); // test drawing width availability, or catch otherwise
                    }
                    else
                    {
                        if (!node.getChild(1).getNodeName().equals("xsd:sequence"))
                        {
                            // Width
                            this.widthStart = getLength(node.getChild(1));
                            this.widthEnd = this.widthStart;
                        }
                        else
                        {
                            // WidthStart and WidthEnd
                            this.widthStart = getLength(node.getChild(1).getChild(0));
                            this.widthEnd = getLength(node.getChild(1).getChild(1));
                        }
                        Length halfWidthStart = this.widthStart.times(0.5);
                        Length halfWidthEnd = this.widthEnd.times(0.5);

                        if (node.getChild(0).getNodeName().equals("CenterOffset"))
                        {
                            this.offsetStart = getLength(node.getChild(0));
                            this.offsetEnd = this.offsetStart;
                        }
                        else if (node.getChild(0).getNodeName().equals("LeftOffset"))
                        {
                            Length leftOffset = getLength(node.getChild(0));
                            this.offsetStart = leftOffset.minus(halfWidthStart);
                            this.offsetEnd = leftOffset.minus(halfWidthEnd);
                        }
                        else if (node.getChild(0).getNodeName().equals("RightOffset"))
                        {
                            Length rightOffset = getLength(node.getChild(0));
                            this.offsetStart = rightOffset.plus(halfWidthStart);
                            this.offsetEnd = rightOffset.plus(halfWidthEnd);
                        }
                        else if (node.getChild(0).isActive())
                        {
                            if (node.getChild(0).getChild(0).getNodeName().equals("CenterOffsetStart"))
                            {
                                this.offsetStart = getLength(node.getChild(0).getChild(0));
                            }
                            else if (node.getChild(0).getChild(0).getNodeName().equals("LeftOffsetStart"))
                            {
                                this.offsetStart = getLength(node.getChild(0).getChild(0)).minus(halfWidthStart);
                            }
                            else if (node.getChild(0).getChild(0).getNodeName().equals("RightOffsetStart"))
                            {
                                this.offsetStart = getLength(node.getChild(0).getChild(0)).plus(halfWidthStart);
                            }
                            else
                            {
                                this.offsetStart = null;
                            }

                            if (node.getChild(0).getChild(1).getNodeName().equals("CenterOffsetEnd"))
                            {
                                this.offsetEnd = getLength(node.getChild(0).getChild(1));
                            }
                            else if (node.getChild(0).getChild(1).getNodeName().equals("LeftOffsetEnd"))
                            {
                                this.offsetEnd = getLength(node.getChild(0).getChild(1)).minus(halfWidthEnd);
                            }
                            else if (node.getChild(0).getChild(1).getNodeName().equals("RightOffsetEnd"))
                            {
                                this.offsetEnd = getLength(node.getChild(0).getChild(1)).plus(halfWidthEnd);
                            }
                            else
                            {
                                this.offsetEnd = null;
                            }
                        }
                        else
                        {
                            this.offsetStart = null;
                            this.offsetEnd = null;
                        }
                    }
                }
                catch (Exception e)
                {
                    // node is being copied, or some other intermediate state that is incomplete
                    this.index++;
                    return hasNext();
                }

                return true;
            }

            /** {@inheritDoc} */
            @Override
            public OffsetElement next()
            {
                Throw.when(!hasNext(), IllegalStateException.class, "Iterator does not have a next element.");
                XsdTreeNode node = children.get(this.index);
                OffsetElement offsetElement =
                        new OffsetElement(this.widthStart, this.widthEnd, this.offsetStart, this.offsetEnd, node);
                this.index++;
                return offsetElement;
            }
        };
        if (iterator.hasNext())
        {
            RoadLayoutOffsets.calculateOffsets(iterator, cseList, indices);
        }
        for (Entry<Object, Integer> entry : indices.entrySet())
        {
            cseData.put((XsdTreeNode) entry.getKey(), cseList.get(entry.getValue()));
        }
        return cseData;
    }

}
