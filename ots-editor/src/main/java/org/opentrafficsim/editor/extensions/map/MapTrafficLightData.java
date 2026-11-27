package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;

import org.djutils.event.Event;
import org.opentrafficsim.animation.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeUtil;

/**
 * Traffic light data for the editor Map.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class MapTrafficLightData extends MapLaneBasedObjectData implements TrafficLightData
{

    /** Listener for changes to the traffic light node and its TurnOnRed children. */
    private final ChangeListener changeListener;

    /**
     * Constructor.
     * @param map map.
     * @param node node Ots.Network.Link.TrafficLight.
     * @param editor editor.
     */
    public MapTrafficLightData(final EditorMap map, final XsdTreeNode node, final OtsEditor editor)
    {
        super(map, node, editor);
        setLinkNode(getNode().getParent());
        this.changeListener = new ChangeListener(node); // update on TurnOnRed change to show/hide bulb
        this.changeListener.addListener(this, ChangeListener.CHANGE_EVENT);
    }

    @Override
    public void destroy()
    {
        this.changeListener.destroy();
        super.destroy();
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(ChangeListener.CHANGE_EVENT))
        {
            update();
        }
        else
        {
            super.notify(event);
        }
    }

    @Override
    public Color getColor()
    {
        return Color.RED;
    }

    @Override
    public boolean hasTurnOnRed()
    {
        return XsdTreeNodeUtil.findActiveChild(getNode(), "TurnOnRed") != null;
    }

    @Override
    public String toString()
    {
        return "Traffic light " + getId();
    }

}
