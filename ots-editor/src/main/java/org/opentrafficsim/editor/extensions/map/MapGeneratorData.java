package org.opentrafficsim.editor.extensions.map;

import org.djutils.draw.bounds.Bounds2d;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Generator data for the editor Map.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapGeneratorData extends MapLaneBasedObjectData implements GtuGeneratorPositionData
{

    /** */
    private static final long serialVersionUID = 20240310L;

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

    /** {@inheritDoc} */
    @Override
    public Bounds2d getBounds()
    {
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    public int getQueueCount()
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public double getZ()
    {
        return DrawLevel.OBJECT.getZ();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return this.type + getLinkLanePositionId();
    }

}
