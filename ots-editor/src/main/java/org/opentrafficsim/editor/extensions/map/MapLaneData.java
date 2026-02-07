package org.opentrafficsim.editor.extensions.map;

import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.CrossSectionGeometry;

/**
 * Lane data for in the editor.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MapLaneData extends MapCrossSectionData implements LaneData
{

    /** Id. */
    private final String id;

    /**
     * Constructor.
     * @param id id
     * @param linkNode node representing the element
     * @param geometry geometry
     */
    public MapLaneData(final String id, final XsdTreeNode linkNode, final CrossSectionGeometry geometry)
    {
        super(linkNode, geometry);
        this.id = id;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String toString()
    {
        return "Lane " + getLinkId() + "." + this.id;
    }

}
