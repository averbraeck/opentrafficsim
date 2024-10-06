package org.opentrafficsim.draw.road;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsRenderable;
import org.opentrafficsim.draw.ClickableLineLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Abstract class for objects that draw a line perpendicular on the lane.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> the LaneBasedObject class of the source that indicates the location of the Renderable on the screen
 */
public abstract class AbstractLineAnimation<T extends LaneBasedObjectData> extends OtsRenderable<T>
{

    /** */
    private static final long serialVersionUID = 20230929L;

    /** Rectangle to color. */
    private final Rectangle2D rectangle;

    /**
     * Construct the line animation.
     * @param source source
     * @param contextualized context provider
     * @param width line width
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public AbstractLineAnimation(final T source, final Contextualized contextualized, final Length width)
            throws NamingException, RemoteException
    {
        super(source, contextualized);
        double halfLength = .5 * source.getLine().getLength();
        this.rectangle = new Rectangle2D.Double(-.5 * width.si, -halfLength, width.si, 2 * halfLength);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        setRendering(graphics);
        graphics.fill(this.rectangle);
        resetRendering(graphics);
    }

    /**
     * LaneBasedObjectData provides the information required to draw a lane based object.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface LaneBasedObjectData extends ClickableLineLocatable, Identifiable
    {
        /**
         * Returns the width of the lane.
         * @return width of the lane.
         */
        Length getLaneWidth();

        /** {@inheritDoc} */
        @Override
        OrientedPoint2d getLocation();

        /** {@inheritDoc} */
        @Override
        default double getZ() throws RemoteException
        {
            return DrawLevel.OBJECT.getZ();
        }
    }

}
