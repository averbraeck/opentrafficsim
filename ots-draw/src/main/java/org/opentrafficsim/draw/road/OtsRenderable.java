package org.opentrafficsim.draw.road;

import java.rmi.RemoteException;

import org.djutils.draw.Oriented;
import org.djutils.draw.Transform2d;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.draw.TransformableBounds;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Extends {@code Renderable2d} to let the {@code contains} method look at the actual bounds shape, rather than only the box.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <L> locatable type
 */
public abstract class OtsRenderable<L extends Locatable> extends Renderable2d<L>
{

    /** */
    private static final long serialVersionUID = 20240223L;

    /**
     * Constructs a new Renderable2d.
     * @param source T; the source
     * @param contextProvider Contextualized; the object that can provide the context to store the animation objects
     */
    public OtsRenderable(final L source, final Contextualized contextProvider)
    {
        super(source, contextProvider);
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2d pointWorldCoordinates, final Bounds2d extent)
    {
        try
        {
            if (getSource().getBounds() instanceof TransformableBounds<?> && getSource().getLocation() instanceof Oriented)
            {
                Point<?> center = getSource().getLocation();
                TransformableBounds<?> bounds = (TransformableBounds<?>) getSource().getBounds();

                Point2d c = new Point2d(center.getX(), center.getY());
                Oriented<?> o = (Oriented<?>) center;
                Transform2d transformation = new Transform2d();
                transformation.translate(c);
                transformation.rotation(o.getDirZ());
                return bounds.transform(transformation).contains(pointWorldCoordinates);
            }
            else
            {
                return super.contains(pointWorldCoordinates, extent);
            }
        }
        catch (RemoteException ex)
        {
            CategoryLogger.always().warn(ex, "contains");
            return false;
        }
    }

}
