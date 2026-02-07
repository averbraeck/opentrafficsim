package org.opentrafficsim.editor.extensions.map;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Generator data for the editor Map.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapGeneratorData extends MapLaneBasedObjectData implements GtuGeneratorPositionData
{

    /** Type, 'Generator' or 'List generator'. */
    private final String type;

    /** Bounds. */
    private Bounds2d bounds = new Bounds2d(0.0, 4.75, -1.0, 1.0);

    /**
     * Constructor.
     * @param map map.
     * @param node node.
     * @param editor editor.
     */
    public MapGeneratorData(final EditorMap map, final XsdTreeNode node, final OtsEditor editor)
    {
        super(map, node, editor);
        this.type = node.getNodeName().equals("Generator") ? "Generator " : "List generator ";
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return this.bounds;
    }

    @Override
    public int getQueueCount()
    {
        return 0;
    }

    @Override
    public double getZ()
    {
        return DrawLevel.OBJECT.getZ();
    }

    @Override
    public String toString()
    {
        return this.type + getLinkLanePositionId();
    }

    /**
     * Signed distance function. The point must be relative. As this is a line object, only positive values are returned.
     * @param point point for which distance is returned
     * @return distance from point to these bounds
     */
    @Override
    public double signedDistance(final Point2d point)
    {
        return getLine().closestPointOnPolyLine(point).distance(point);
    }

}
