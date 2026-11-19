package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.animation.network.NodeAnimation.NodeData;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.OtsEditorProperties;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.Adapters;

/**
 * NodeData for the editor Map.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class MapNodeData extends MapData implements NodeData, EventListener
{

    /** Bounds. */
    private static final Bounds2d BOUNDS = new Bounds2d(2.0, 2.0);

    /** Invalid alignment color. */
    private static final Color INVALID_COLOR =
            OtsEditorProperties.PROPERTIES_STORE.getColorOrDefault("map.node.invalidAlignmentColor", new Color(255, 103, 0));

    /** Invalid angle alignment threshold. */
    private static final double INVALID_ANG = Math.PI / 360.0; // 0.5 deg

    /** Invalid distance alignment threshold. */
    private static final double INVALID_DIST = 0.05; // 5cm

    /** String attribute. */
    private String id = "";

    /** Coordinate attribute. */
    private Point2d coordinate = null;

    /** Direction attribute. */
    private Direction direction = null;

    /** Location. */
    private DirectedPoint2d location = new DirectedPoint2d(0.0, 0.0, 0.0);

    /** Absolute contour. */
    private Polygon2d absoluteContour;

    /** Relative contour. */
    private Polygon2d relativeContour;

    /** Color. */
    private Color color = NodeData.super.getColor();

    /**
     * Constructor.
     * @param map map.
     * @param nodeNode node Ots.Network.Node.
     * @param editor editor.
     */
    public MapNodeData(final EditorMap map, final XsdTreeNode nodeNode, final OtsEditor editor)
    {
        super(map, nodeNode, editor);
        getNode().addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        // for when node is duplicated, set data immediately
        if (getNode().isActive())
        {
            notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
            notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Coordinate", null}));
            if (!isCentroid())
            {
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Direction", null}));
            }
        }
        this.absoluteContour = OtsShape.boundsAsAbsoluteContour(this);
        this.relativeContour =
                new Polygon2d(0.0, OtsShape.toRelativeTransform(this.location).transform(this.absoluteContour.iterator()));
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return BOUNDS;
    }

    @Override
    public Polygon2d getAbsoluteContour()
    {
        return this.absoluteContour;
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return this.relativeContour;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public void destroy()
    {
        super.destroy();
        getNode().removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        return this.location;
    }

    @Override
    public boolean isCentroid()
    {
        return getNode().getNodeName().equals("Centroid");
    }

    @Override
    public Color getColor()
    {
        return this.color;
    }

    @Override
    public void notify(final Event event)
    {
        String attribute = (String) ((Object[]) event.getContent())[1];
        String value = getNode().getAttributeValueOrDefault(attribute);
        if ("Id".equals(attribute))
        {
            this.id = value == null ? "" : value;
            return;
        }
        else if ("Coordinate".equals(attribute))
        {
            executeForAttribute((v) -> this.coordinate = v, getNode(), "Coordinate", Adapters.get(Point2d.class));
        }
        else if ("Direction".equals(attribute))
        {
            executeForAttribute((v) -> this.direction = v, getNode(), "Direction", Adapters.get(Direction.class));
        }
        else
        {
            return;
        }
        setLocation();
    }

    @Override
    public void update()
    {
        this.id = getNode().getId() == null ? "" : getNode().getId();
        executeForAttribute((v) -> this.coordinate = v, getNode(), "Coordinate", Adapters.get(Point2d.class));
        XsdTreeNode node = getNode();
        if (node != null && node.getPathString().equals(XsdPaths.NODE)) // Centroids have no direction
        {
            executeForAttribute((v) -> this.direction = v, node, "Direction", Adapters.get(Direction.class));
        }
        setLocation();
        setColor();
    }

    /**
     * Sets the color based on the alignment of the links connected to this node. The color is invalid if any connected link has
     * a direction that differs too much from the node direction, or an end-point that is too far from the offset node location.
     */
    private void setColor()
    {
        XsdTreeNode node = getNode();
        XsdTreeNode parent = node == null ? null : node.getParent();
        if (parent == null)
        {
            this.color = NodeData.super.getColor();
            return;
        }
        DirectedPoint2d loc = getLocation();
        for (XsdTreeNode child : parent.getChildren())
        {
            if (!child.getPathString().equals(XsdPaths.LINK))
            {
                continue;
            }
            MapData data = getMap().getData(child).orElse(null);
            if (!(data instanceof MapLinkData linkData))
            {
                continue;
            }
            boolean atStart = node.equals(linkData.getStartNode());
            if (!atStart && !node.equals(linkData.getEndNode()))
            {
                continue;
            }
            OffsetCurve2d designLine = linkData.getDesignLine();
            if (designLine == null)
            {
                continue;
            }
            double linkEndPointDirection = atStart ? designLine.getStartDirection() : designLine.getEndDirection();
            if (Math.abs(AngleUtil.normalizeAroundZero(linkEndPointDirection - loc.dirZ)) > INVALID_ANG)
            {
                this.color = INVALID_COLOR;
                return;
            }
            Point2d linkEndPointPoint = atStart ? designLine.getStartPoint() : designLine.getEndPoint();
            Length offset = atStart ? linkData.getStartOffset() : linkData.getEndOffset();
            Point2d offsetLocation = OtsGeometryUtil.offsetPoint(loc, offset == null ? 0.0 : offset.si);
            if (linkEndPointPoint.distance(offsetLocation) > INVALID_DIST)
            {
                this.color = INVALID_COLOR;
                return;
            }
        }
        this.color = NodeData.super.getColor();
    }

    /**
     * Set the location from the coordinate and direction. Notify when invalid or valid.
     */
    private void setLocation()
    {
        if (this.coordinate == null)
        {
            setInvalid();
            return;
        }
        this.location = new DirectedPoint2d(this.coordinate, this.direction == null ? 0.0 : this.direction.si);
        this.absoluteContour = OtsShape.boundsAsAbsoluteContour(this);
        this.relativeContour =
                new Polygon2d(0.0, OtsShape.toRelativeTransform(this.location).transform(this.absoluteContour.iterator()));
        setValid();
    }

    @Override
    public String toString()
    {
        return "Node " + this.id;
    }

}
