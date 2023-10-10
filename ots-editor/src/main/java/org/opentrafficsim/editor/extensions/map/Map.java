package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.event.Event;
import org.djutils.exceptions.Try;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.dsol.swing.animation.d2.AnimationUpdaterThread;
import nl.tudelft.simulation.dsol.swing.animation.d2.VisualizationPanel;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.Contextualized;
import nl.tudelft.simulation.naming.context.JvmContext;

/**
 * Editor map.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Map extends VisualizationPanel
{

    /** */
    private static final long serialVersionUID = 20231010L;

    /** All types that are valid to show in the map. */
    private static final Set<String> TYPES = Set.of("Ots.Network.Node", "Ots.Network.Link");

    /** Context provider. */
    private final Contextualized contextualized;

    /** Editor. */
    private final OtsEditor editor;

    /** All map data's drawn (or hidden as they are invalid). */
    private final LinkedHashMap<XsdTreeNode, MapData> datas = new LinkedHashMap<>();

    /** Animation objects of all data's drawn. */
    private final LinkedHashMap<XsdTreeNode, Renderable2d<?>> animations = new LinkedHashMap<>();

    /** Last x-scale. */
    private Double lastXScale = null;

    /** Last y-scale. */
    private Double lastYScale = null;

    /** Last screen size. */
    private Dimension lastScreen = null;

    /**
     * Constructor.
     * @param animator AnimationUpdaterThread; thread for frequent painting.
     * @param contextualized Contextualized; context provider.
     * @param editor OtsEditor; editor.
     * @throws RemoteException context binding problem.
     * @throws NamingException context binding problem.
     */
    private Map(final AnimationUpdaterThread animator, final Contextualized contextualized, final OtsEditor editor)
            throws RemoteException, NamingException
    {
        super(new Bounds2d(500, 500), animator, contextualized.getContext());
        this.contextualized = contextualized;
        this.editor = editor;
        setBackground(Color.GRAY);
        editor.addListener(this, OtsEditor.NEW_FILE);
        setRenderableScale(new RenderableScale()
        {
            /** {@inheritDoc} */
            @Override
            public Bounds2d computeVisibleExtent(final Bounds2d extent, final Dimension screen)
            {
                // overriden to preserve zoom scale, otherwise dragging the split screen may pump up the zoom factor
                double xScale = getXScale(extent, screen);
                double yScale = getYScale(extent, screen);
                Bounds2d result;
                if (Map.this.lastYScale != null && yScale == Map.this.lastYScale)
                {
                    result = new Bounds2d(extent.midPoint().getX() - 0.5 * screen.getWidth() * yScale,
                            extent.midPoint().getX() + 0.5 * screen.getWidth() * yScale, extent.getMinY(), extent.getMaxY());
                    xScale = yScale;
                }
                else if (Map.this.lastXScale != null && xScale == Map.this.lastXScale)
                {
                    result = new Bounds2d(extent.getMinX(), extent.getMaxX(),
                            extent.midPoint().getY() - 0.5 * screen.getHeight() * xScale * getYScaleRatio(),
                            extent.midPoint().getY() + 0.5 * screen.getHeight() * xScale * getYScaleRatio());
                    yScale = xScale;
                }
                else
                {
                    double scale = Map.this.lastXScale == null ? Math.min(xScale, yScale)
                            : Map.this.lastXScale * Map.this.lastScreen.getWidth() / screen.getWidth();
                    result = new Bounds2d(extent.midPoint().getX() - 0.5 * screen.getWidth() * scale,
                            extent.midPoint().getX() + 0.5 * screen.getWidth() * scale,
                            extent.midPoint().getY() - 0.5 * screen.getHeight() * scale * getYScaleRatio(),
                            extent.midPoint().getY() + 0.5 * screen.getHeight() * scale * getYScaleRatio());
                    yScale = scale;
                    xScale = scale;
                }
                Map.this.lastXScale = xScale;
                Map.this.lastYScale = yScale;
                Map.this.lastScreen = screen;
                return result;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setExtent(final Bounds2d extent)
    {
        if (this.lastScreen != null)
        {
            // this prevents zoom being undone when resizing the screen afterwards
            Map.this.lastXScale = getRenderableScale().getXScale(extent, this.lastScreen);
            Map.this.lastYScale = getRenderableScale().getYScale(extent, this.lastScreen);
        }
        super.setExtent(extent);
    }

    /**
     * Builds a map panel with an animator and context.
     * @param editor OtsEditor; editor.
     * @return Map; map.
     * @throws RemoteException context binding problem.
     * @throws NamingException context binding problem.
     */
    public static Map build(final OtsEditor editor) throws RemoteException, NamingException
    {
        AnimationUpdaterThread animator = new AnimationUpdaterThread();
        animator.start();
        ContextInterface context = new JvmContext("ots-context");
        Contextualized contextualized = new Contextualized()
        {
            /** {@inheritDoc} */
            @Override
            public ContextInterface getContext()
            {
                return context;
            }
        };
        return new Map(animator, contextualized, editor);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            for (XsdTreeNode node : new LinkedHashSet<>(this.datas.keySet()))
            {
                remove(node);
            }
            this.animations.values().forEach((animation) -> objectRemoved(animation));
            this.animations.clear();
            this.datas.clear();
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (isType(node))
            {
                node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
                node.addListener(this, XsdTreeNode.OPTION_CHANGED);
                if (node.isActive())
                {
                    add(node);
                }
            }
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (this.datas.containsKey(node)) // node.isType does not work as parent is gone, i.e. type is just "Node"
            {
                remove(node);
            }
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (isType(node))
            {
                if (node.isActive())
                {
                    add(node);
                }
                else
                {
                    remove(node);
                }
            }
        }
        else if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            XsdTreeNode selected = (XsdTreeNode) content[1];
            XsdTreeNode previous = (XsdTreeNode) content[2];
            if (node.equals(selected))
            {
                if (isType(previous))
                {
                    remove(previous);
                }
                if (isType(selected) && selected.isActive())
                {
                    add(selected);
                }
            }
        }
        else
        {
            super.notify(event);
        }
    }

    /**
     * Returns whether the node is any of the visualized types.
     * @param node XsdTreeNode; node.
     * @return boolean; whether the node is any of the visualized types.
     */
    private boolean isType(final XsdTreeNode node)
    {
        for (String type : TYPES)
        {
            if (node.isType(type))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the data as being valid to draw.
     * @param data MapData; data that is valid to draw.
     */
    public void setValid(final MapData data)
    {
        XsdTreeNode node = data.getNode();
        if (this.animations.containsKey(node))
        {
            return;
        }
        Renderable2d<?> animation;
        if (node.isType("Ots.Network.Node"))
        {
            animation = Try.assign(() -> new NodeAnimation((MapNodeData) data, this.contextualized), "");
        }
        else if (node.isType("Ots.Network.Link"))
        {
            animation = Try.assign(() -> new LinkAnimation((MapLinkData) data, this.contextualized, 0.5f), "");
        }
        else
        {
            throw new UnsupportedOperationException("Node cannot be added by the map editor.");
        }
        this.animations.put(node, animation);
    }

    /**
     * Set the data as being invalid to draw.
     * @param data MapData; data that is invalid to draw.
     */
    // TODO: for some reason, this does not work... because data remains in JVM?
    public void setInvalid(final MapData data)
    {
        //
    }

    /**
     * Adds a data representation of the node. This will not yet be drawn until the data object itself tells the map it is valid
     * to be drawn.
     * @param node XsdTreeNode; node of element to draw.
     * @throws RemoteException context binding problem.
     */
    private void add(final XsdTreeNode node) throws RemoteException
    {
        MapData data;
        if (node.isType("Ots.Network.Node"))
        {
            data = new MapNodeData(this, node, this.editor);
        }
        else if (node.isType("Ots.Network.Link"))
        {
            data = new MapLinkData(this, node, this.editor);
        }
        else
        {
            throw new UnsupportedOperationException("Node cannot be added by the map editor.");
        }
        this.datas.put(node, data);
    }

    /**
     * Remove the drawing data of pertaining to the node.
     * @param node XsdTreeNode; node.
     */
    private void remove(final XsdTreeNode node)
    {
        MapData data = this.datas.remove(node);
        if (data != null)
        {
            data.destroy();
        }
        Renderable2d<?> animation = this.animations.remove(node);
        if (animation != null)
        {
            objectRemoved(animation);
            animation.destroy(this.contextualized);
        }
    }

}
