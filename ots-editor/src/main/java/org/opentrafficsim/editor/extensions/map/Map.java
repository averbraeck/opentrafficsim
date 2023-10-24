package org.opentrafficsim.editor.extensions.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;

import javax.naming.NamingException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Try;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
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
public class Map extends JPanel implements EventListener
{

    /** */
    private static final long serialVersionUID = 20231010L;

    /** Color for toolbar and toggle bar. */
    private static final Color BAR_COLOR = Color.LIGHT_GRAY;

    /** All types that are valid to show in the map. */
    private static final Set<String> TYPES = Set.of(XsdPaths.NODE, XsdPaths.LINK);

    /** Context provider. */
    private final Contextualized contextualized;

    /** Editor. */
    private final OtsEditor editor;

    /** Panel to draw in. */
    private final VisualizationPanel drawPanel;

    /** Panel with tools. */
    private final JPanel toolPanel;

    /** Panel with toggles. */
    private final JPanel togglePanel;

    /** All map data's drawn (or hidden as they are invalid). */
    private final LinkedHashMap<XsdTreeNode, MapData> datas = new LinkedHashMap<>();

    /** Weak references to all created link data's. */
    private final WeakHashMap<MapLinkData, Object> links = new WeakHashMap<>();

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
        super(new BorderLayout());
        this.contextualized = contextualized;
        this.editor = editor;
        this.drawPanel = new VisualizationPanel(new Bounds2d(500, 500), animator, contextualized.getContext())
        {
            /** */
            private static final long serialVersionUID = 20231016L;

            /** {@inheritDoc} */
            @Override
            public void setExtent(final Bounds2d extent)
            {
                if (Map.this.lastScreen != null)
                {
                    // this prevents zoom being undone when resizing the screen afterwards
                    Map.this.lastXScale = this.getRenderableScale().getXScale(extent, Map.this.lastScreen);
                    Map.this.lastYScale = this.getRenderableScale().getYScale(extent, Map.this.lastScreen);
                }
                super.setExtent(extent);
            }
        };
        this.drawPanel.setBackground(Color.GRAY);
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.drawPanel.setRenderableScale(new RenderableScale()
        {
            /** {@inheritDoc} */
            @Override
            public Bounds2d computeVisibleExtent(final Bounds2d extent, final Dimension screen)
            {
                // overridden to preserve zoom scale, otherwise dragging the split screen may pump up the zoom factor
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
        add(this.drawPanel, BorderLayout.CENTER);

        this.toolPanel = new JPanel();
        this.toolPanel.setBackground(BAR_COLOR);
        this.toolPanel.add(new JLabel("tools")); // TODO: temporary while empty
        this.toolPanel.setLayout(new BoxLayout(this.toolPanel, BoxLayout.X_AXIS));
        add(this.toolPanel, BorderLayout.NORTH);

        this.togglePanel = new JPanel();
        this.togglePanel.setBackground(BAR_COLOR);
        this.togglePanel.add(new JLabel("toggles")); // TODO: temporary while empty
        this.togglePanel.setLayout(new BoxLayout(this.togglePanel, BoxLayout.Y_AXIS));
        add(this.togglePanel, BorderLayout.WEST);
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
            this.links.clear();
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
            else if (node.getPathString().equals(XsdPaths.POLYLINE_COORDINATE))
            {
                for (MapLinkData linkData : this.links.keySet())
                {
                    linkData.addCoordinate(node);
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
            else if (node.getPathString().equals(XsdPaths.POLYLINE_COORDINATE))
            {
                for (MapLinkData linkData : this.links.keySet())
                {
                    linkData.removeCoordinate(node);
                }
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
        else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            for (MapLinkData linkData : this.links.keySet())
            {
                linkData.notifyNodeIdChanged(linkData.getNode());
            }
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
        if (node.getPathString().equals(XsdPaths.NODE))
        {
            animation = Try.assign(() -> new NodeAnimation((MapNodeData) data, this.contextualized), "");
        }
        else if (node.getPathString().equals(XsdPaths.LINK))
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
        if (node.getPathString().equals(XsdPaths.NODE))
        {
            data = new MapNodeData(this, node, this.editor);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        else if (node.getPathString().equals(XsdPaths.LINK))
        {
            MapLinkData linkData = new MapLinkData(this, node, this.editor);
            data = linkData;
            this.links.put(linkData, null);
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
        if (node.getPathString().equals(XsdPaths.NODE))
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        if (node.getPathString().equals(XsdPaths.LINK))
        {
            Iterator<MapLinkData> it = this.links.keySet().iterator();
            while (it.hasNext())
            {
                MapLinkData link = it.next();
                if (link.getNode().equals(node))
                {
                    it.remove();
                }
            }
        }
        MapData data = this.datas.remove(node);
        if (data != null)
        {
            data.destroy();
        }
        Renderable2d<?> animation = this.animations.remove(node);
        if (animation != null)
        {
            this.drawPanel.objectRemoved(animation);
            animation.destroy(this.contextualized);
        }
    }

}
