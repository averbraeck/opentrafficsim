package org.opentrafficsim.core.animation.network;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.LocalEventProducer;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.animation.DrawingInfo;
import org.opentrafficsim.core.network.Network;

/**
 * NetworkAnimation stores the relations between drawable objects and their drawing info.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class NetworkAnimation extends LocalEventProducer implements EventListener
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
    public NetworkAnimation(final Network network) throws RemoteException
    {
        this.network = network;
        this.network.addListener(this, Network.GTU_ADD_EVENT);
        this.network.addListener(this, Network.GTU_REMOVE_EVENT);
        this.network.addListener(this, Network.NODE_ADD_EVENT);
        this.network.addListener(this, Network.NODE_REMOVE_EVENT);
        this.network.addListener(this, Network.LINK_ADD_EVENT);
        this.network.addListener(this, Network.LINK_REMOVE_EVENT);
        this.network.addListener(this, Network.OBJECT_ADD_EVENT);
        this.network.addListener(this, Network.OBJECT_REMOVE_EVENT);
        this.network.addListener(this, Network.NONLOCATED_OBJECT_ADD_EVENT);
        this.network.addListener(this, Network.NONLOCATED_OBJECT_REMOVE_EVENT);
        this.network.addListener(this, Network.ROUTE_ADD_EVENT);
        this.network.addListener(this, Network.ROUTE_REMOVE_EVENT);
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
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(Network.NODE_ADD_EVENT))
        {
            //
        }
    }

}
