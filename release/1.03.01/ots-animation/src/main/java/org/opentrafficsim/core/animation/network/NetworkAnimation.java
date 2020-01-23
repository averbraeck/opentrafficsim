package org.opentrafficsim.core.animation.network;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.animation.DrawingInfo;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;

/**
 * NetworkAnimation stores the relations between drawable objects and their drawing info. <br>
 * <br>
 * Copyright (c) 2003-2020 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class NetworkAnimation extends EventProducer implements EventListenerInterface
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the network to which the animation info is connected. */
    private final Network network;

    /** drawing info: base information per class. */
    private Map<Class<? extends Drawable>, DrawingInfo> classDrawingInfoMap = new LinkedHashMap<>();

    /** drawing info: base information per instance. */
    private Map<Drawable, DrawingInfo> baseDrawingInfoMap = new LinkedHashMap<>();

    /** drawing info: dynamic information per instance. */
    private Map<Drawable, DrawingInfo> dynamicDrawingInfoMap = new LinkedHashMap<>();

    /**
     * Construct this NetworkAnimation object with a connection to the Network.
     * @param network Network; the network to which the animation info is connected
     * @throws RemoteException in case of remote events and network error
     */
    public NetworkAnimation(Network network) throws RemoteException
    {
        super();
        this.network = network;
        this.network.addListener(this, Network.ANIMATION_GTU_ADD_EVENT);
        this.network.addListener(this, Network.ANIMATION_GTU_REMOVE_EVENT);
        this.network.addListener(this, Network.ANIMATION_NODE_ADD_EVENT);
        this.network.addListener(this, Network.ANIMATION_NODE_REMOVE_EVENT);
        this.network.addListener(this, Network.ANIMATION_LINK_ADD_EVENT);
        this.network.addListener(this, Network.ANIMATION_LINK_REMOVE_EVENT);
        this.network.addListener(this, Network.ANIMATION_OBJECT_ADD_EVENT);
        this.network.addListener(this, Network.ANIMATION_OBJECT_REMOVE_EVENT);
        this.network.addListener(this, Network.ANIMATION_INVISIBLE_OBJECT_ADD_EVENT);
        this.network.addListener(this, Network.ANIMATION_INVISIBLE_OBJECT_REMOVE_EVENT);
        this.network.addListener(this, Network.ANIMATION_ROUTE_ADD_EVENT);
        this.network.addListener(this, Network.ANIMATION_ROUTE_REMOVE_EVENT);
        this.network.addListener(this, Network.ANIMATION_GTU_ADD_EVENT);
        this.network.addListener(this, Network.ANIMATION_GTU_REMOVE_EVENT);
    }

    /**
     * Add the drawing info for a class. Here it can e.g., be specified that all lanes are filled with a light gray color and
     * drawn with a dark gray stroke. The class drawing info <b>can</b> be cached.
     * @param drawableClass Class&lt;? extends Drawable&gt;; the class to set the drawing info for
     * @param drawingInfo DrawingInfo; the default drawing info for the class
     */
    public void addDrawingInfoClass(final Class<? extends Drawable> drawableClass, final DrawingInfo drawingInfo)
    {
        this.classDrawingInfoMap.put(drawableClass, drawingInfo);
    }

    /**
     * Add the drawing info for an instance. This overrides the drawing info for the class. An example is that a bus lane can be
     * drawn using a black color to make it different from the standard lanes. The base drawing info <b>can</b> be cached.
     * @param drawable Drawable; the object to set the drawing info for
     * @param drawingInfo DrawingInfo; the default drawing info for the drawable
     */
    public void addDrawingInfoBase(final Drawable drawable, final DrawingInfo drawingInfo)
    {
        this.baseDrawingInfoMap.put(drawable, drawingInfo);
    }

    /**
     * Add the dynamic drawing information for an instance. This overrides the drawing info for the object and the class, and
     * should <b>not</b> be cached. An example is that a lane on a highway that turns red when it is forbidden for traffic to
     * use the lane.
     * @param drawable Drawable; the object to set the drawing info for
     * @param drawingInfo DrawingInfo; the dynamic drawing info for the drawable
     */
    public void addDrawingInfoDynamic(final Drawable drawable, final DrawingInfo drawingInfo)
    {
        this.dynamicDrawingInfoMap.put(drawable, drawingInfo);
    }

    /**
     * Get the drawing information for a drawable instance. It first checks the dynamic info, then the base info, and then the
     * class info.
     * @param drawable Drawable; the object to get the drawing info for
     * @return DrawingInfo; the drawing info for the instance, or null if no Drawing info could be found
     */
    public DrawingInfo getDrawingInfo(final Drawable drawable)
    {
        DrawingInfo drawingInfo = this.dynamicDrawingInfoMap.get(drawable);
        if (drawingInfo != null)
        {
            return drawingInfo;
        }
        return getDrawingInfoBase(drawable);
    }

    /**
     * Get the static drawing information for a drawable instance. It first checks the base info, and then the class info.
     * @param drawable Drawable; the object to get the drawing info for
     * @return DrawingInfo; the drawing info for the instance, or null if no Drawing info could be found
     */
    public DrawingInfo getDrawingInfoBase(final Drawable drawable)
    {
        DrawingInfo drawingInfo = this.baseDrawingInfoMap.get(drawable);
        if (drawingInfo != null)
        {
            return drawingInfo;
        }
        return getDrawingInfoClass(drawable.getClass());
    }

    /**
     * Get the static class-based drawing information for a drawable instance.
     * @param drawableClass Class&lt;? extends Drawable&gt;; the class to get the drawing info for
     * @return DrawingInfo; the drawing info for the class, or null if no Drawing info could be found
     */
    public DrawingInfo getDrawingInfoClass(final Class<? extends Drawable> drawableClass)
    {
        DrawingInfo drawingInfo = this.classDrawingInfoMap.get(drawableClass);
        if (drawingInfo != null)
        {
            return drawingInfo;
        }
        return null;
    }

    /**
     * @return the network backed by this NetworkAnimation object
     */
    public final Network getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(Network.ANIMATION_NODE_ADD_EVENT))
        {
            //
        }
    }

}
