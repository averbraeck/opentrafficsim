package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.eval.Eval;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.event.reference.ReferenceType;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.CseData;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.OffsetElement;
import org.opentrafficsim.xml.bindings.LengthAdapter;

/**
 * Listens to changes in a road layout. Notifies all listeners that need to repaint. This also provides the offsets of the road
 * layout.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class RoadLayoutListener extends LocalEventProducer implements EventListener
{

    /** */
    private static final long serialVersionUID = 1L;

    /** Event that any change happened to the road layout. */
    public static final EventType LAYOUT_CHANGED =
            new EventType("LAYOUTCHANGED", new MetaData("Layout changed", "When any element in a layout has changed",
                    new ObjectDescriptor("Node", "Node of the road layout", XsdTreeNode.class)));

    /** Adapter for length values. */
    private static final LengthAdapter LENGTH_ADAPTER = new LengthAdapter();

    /** Node of the layout, under which the lanes etc. are located. */
    private final XsdTreeNode layoutNode;
    
    /** Set of all elements that, when removed, change the layout. */
    private final Set<XsdTreeNode> elementNodes = new LinkedHashSet<>();

    /** Expression evaluator for length values. */
    private final Supplier<Eval> eval;

    /** Flag when change occurred and offsets need to be recalculated. */
    private boolean cseDataDirty = true;

    /** Cached offset data. */
    private final java.util.Map<XsdTreeNode, CseData> cseData = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param layoutNode XsdTreeNode; node of the layout, either in definitions or under a link.
     * @param eval Supplier&lt;Eval&gt;; supplier of expression evaluator, either from the main map, or from a map link data.
     */
    public RoadLayoutListener(final XsdTreeNode layoutNode, final Supplier<Eval> eval)
    {
        this.layoutNode = layoutNode;
        this.eval = eval;
        layoutNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) this.layoutNode.getPath().get(0);
        root.addListener(this, XsdTreeNodeRoot.NODE_CREATED, ReferenceType.WEAK);
        root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED, ReferenceType.WEAK);
    }

    /**
     * Remove all listeners.
     */
    public void destroy()
    {
        removeAsListener(this.layoutNode);
        this.layoutNode.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) this.layoutNode.getPath().get(0);
        root.removeListener(this, XsdTreeNodeRoot.NODE_CREATED);
        root.removeListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        removeAllListeners();
        this.cseData.clear();
    }

    /**
     * Removes this as listener from all nodes under the layout node recursively.
     * @param node XsdTreeNode; node to remove as listener from, including from child nodes.
     */
    private void removeAsListener(final XsdTreeNode node)
    {
        if (node.getNodeName().equals("SpeedLimit"))
        {
            return;
        }
        if (node.getChildCount() > 0)
        {
            for (XsdTreeNode child : node.getChildren())
            {
                removeAsListener(child);
            }
        }
        node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        node.removeListener(this, XsdTreeNode.MOVED);
        node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
        node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        // for all events this listens to, the node is the first object in the content
        XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
        if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            if (!node.getPath().contains(this.layoutNode) || node.getNodeName().equals("SpeedLimit"))
            {
                // not the road layout node of this listener (with node created or removed event), or speed limit changed
                return;
            }
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.MOVED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
            this.elementNodes.add(node);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            if (!this.elementNodes.contains(node))
            {
                return;
            }
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.MOVED);
            node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
        }
        // for all events, indicate a change
        this.cseDataDirty = true;
        fireEvent(LAYOUT_CHANGED, this.layoutNode);
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
            return valueNode.isActive() ? LENGTH_ADAPTER.unmarshal(valueNode.getValue()).get(this.eval.get()) : null;
        }
        catch (RuntimeException exception)
        {
            // probably an invalid length value specified
            return null;
        }
    }

    /**
     * Returns the offsets of the layout this listener is listening to. This is cached so defined road layouts can be
     * efficiently used.
     * @return Map&lt;XsdTreeNode, CseData&gt;; offsets.
     */
    public java.util.Map<XsdTreeNode, CseData> getOffsets()
    {
        if (this.cseDataDirty)
        {
            this.cseData.clear();
            List<CseData> cseList = new ArrayList<>();
            java.util.Map<Object, Integer> indices = new LinkedHashMap<>();
            List<XsdTreeNode> children = this.layoutNode.getChildren();
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
                    Length widthStart = this.widthStart == null ? null : Length.instantiateSI(this.widthStart.si);
                    Length widthEnd = this.widthEnd == null ? null : Length.instantiateSI(this.widthEnd.si);
                    Length offsetStart = this.offsetStart == null ? null : Length.instantiateSI(this.offsetStart.si);
                    Length offsetEnd = this.offsetEnd == null ? null : Length.instantiateSI(this.offsetEnd.si);
                    OffsetElement offsetElement = new OffsetElement()
                    {
                        /** {@inheritDoc} */
                        @Override
                        public Length getWidthStart()
                        {
                            return widthStart;
                        }

                        /** {@inheritDoc} */
                        @Override
                        public Length getWidthEnd()
                        {
                            return widthEnd;
                        }

                        /** {@inheritDoc} */
                        @Override
                        public Length getCenterOffsetStart()
                        {
                            return offsetStart;
                        }

                        /** {@inheritDoc} */
                        @Override
                        public Length getCenterOffsetEnd()
                        {
                            return offsetEnd;
                        }

                        /** {@inheritDoc} */
                        @Override
                        public Object getObject()
                        {
                            return node;
                        }
                    };
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
                this.cseData.put((XsdTreeNode) entry.getKey(), cseList.get(entry.getValue()));
            }
            this.cseDataDirty = false;
        }
        return this.cseData;
    }

}
