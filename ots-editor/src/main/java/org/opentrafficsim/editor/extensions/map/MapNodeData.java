package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.Adapters;

/**
 * NodeData for the editor Map.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapNodeData extends MapData implements NodeData, EventListener
{

    /** */
    private static final long serialVersionUID = 20231003L;

    /** Bounds. */
    private static final Bounds2d BOUNDS = new Bounds2d(2.0, 2.0);

    /** String attribute. */
    private String id = "";

    /** Coordinate attribute. */
    private Point2d coordinate = null;

    /** Direction attribute. */
    private Direction direction = null;

    /** Location. */
    private DirectedPoint2d location = new DirectedPoint2d(0.0, 0.0, 0.0);

    /** Contour. */
    private final Polygon2d contour;

    /** Shape (cached). */
    private OtsShape shape;

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
        try
        {
            if (getNode().isActive())
            {
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Id", null}));
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Coordinate", null}));
                notify(new Event(XsdTreeNode.ATTRIBUTE_CHANGED, new Object[] {getNode(), "Direction", null}));
            }
        }
        catch (RemoteException e)
        {
            throw new RuntimeException(e);
        }
        this.contour = OtsLocatable.boundsAsContour(this);
    }

    @Override
    public Bounds2d getBounds()
    {
        return BOUNDS;
    }

    @Override
    public Polygon2d getContour()
    {
        return this.contour;
    }

    @Override
    public OtsShape getShape()
    {
        if (this.shape == null)
        {
            this.shape = NodeData.super.getShape();
        }
        return this.shape;
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
    public void notify(final Event event) throws RemoteException
    {
        String attribute = (String) ((Object[]) event.getContent())[1];
        String value = getNode().getAttributeValue(attribute);
        if ("Id".equals(attribute))
        {
            this.id = value == null ? "" : value;
            return;
        }
        else if ("Coordinate".equals(attribute))
        {
            setValue((v) -> this.coordinate = v, Adapters.get(Point2d.class), getNode(), "Coordinate");
        }
        else if ("Direction".equals(attribute))
        {
            setValue((v) -> this.direction = v, Adapters.get(Direction.class), getNode(), "Direction");
        }
        else
        {
            return;
        }
        setLocation();
    }

    @Override
    public void evalChanged()
    {
        this.id = getNode().getId() == null ? "" : getNode().getId();
        setValue((v) -> this.coordinate = v, Adapters.get(Point2d.class), getNode(), "Coordinate");
        setValue((v) -> this.direction = v, Adapters.get(Direction.class), getNode(), "Direction");
        setLocation();
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
        setValid();
    }

    @Override
    public String toString()
    {
        return "Node " + this.id;
    }

}
