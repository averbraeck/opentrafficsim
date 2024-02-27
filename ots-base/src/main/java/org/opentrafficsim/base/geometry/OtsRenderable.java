package org.opentrafficsim.base.geometry;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.util.Map;
import java.util.WeakHashMap;

import org.djutils.draw.Oriented;
import org.djutils.draw.Transform2d;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Extends {@code Renderable2d} to let the {@code contains} method look at the actual bounds shape, rather than only the box.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <L> locatable type
 */
public abstract class OtsRenderable<L extends OtsLocatable> extends Renderable2d<L>
{

    /** */
    private static final long serialVersionUID = 20240223L;

    /** Standard rendering keys. */
    public static Key[] RENDERING_KEYS = new Key[] {RenderingHints.KEY_ANTIALIASING};

    /** Standard rendering values. */
    public static Object[] RENDERING_VALUES = new Object[] {RenderingHints.VALUE_ANTIALIAS_ON};

    /** Stored hints to reset. */
    private static Map<Object, Object[]> OLD_RENDERING_HINTS = new WeakHashMap<>();

    /**
     * Constructs a new Renderable2d.
     * @param source T; the source
     * @param contextProvider Contextualized; the object that can provide the context to store the animation objects
     */
    public OtsRenderable(final L source, final Contextualized contextProvider)
    {
        super(source, contextProvider);
    }

    /**
     * Set standard rendering hints for this renderable to paint. The graphics should be reset using {@code resetRendering()}
     * after painting.
     * @param graphics Graphics2D; graphics.
     */
    protected void setRendering(final Graphics2D graphics)
    {
        Object[] old = OLD_RENDERING_HINTS.computeIfAbsent(this, (o) -> new Object[RENDERING_KEYS.length]);
        for (int i = 0; i < RENDERING_KEYS.length; i++)
        {
            old[i] = graphics.getRenderingHint(RENDERING_KEYS[i]);
            graphics.setRenderingHint(RENDERING_KEYS[i], RENDERING_VALUES[i]);
        }
    }

    /**
     * Resets rendering hints that this renderable changed through {@code setRendering()}.
     * @param graphics Graphics2D; graphics.
     */
    protected void resetRendering(final Graphics2D graphics)
    {
        Object[] old = OLD_RENDERING_HINTS.computeIfAbsent(this, (o) -> new Object[RENDERING_KEYS.length]);
        Throw.when(old == null, IllegalStateException.class,
                "Renderable %s resets rendering hints, but it never changed rendering hints with setRendering().", this);
        for (int i = 0; i < RENDERING_KEYS.length; i++)
        {
            // If ever a null valued hint is used, just check for null values and do not reset the value in that case.
            // For now the check is not implemented as no such hint is used.
            // if (old[i] != null)
            // {
            graphics.setRenderingHint(RENDERING_KEYS[i], old[i]);
            // }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2d pointWorldCoordinates, final Bounds2d extent)
    {
        Transform2d transformation = toBoundsTransform(getSource().getLocation());
        Point2d pointObjectCoordinates = transformation.transform(pointWorldCoordinates);
        return getSource().getBounds().contains(pointObjectCoordinates);
    }

    /**
     * Returns a transformation by which absolute coordinates can be translated and rotated to the frame of the possibly
     * oriented location around which bounds are defined.
     * @param location Point2d; location (can be an {@code Oriented}).
     * @return Transform2d; transformation.
     */
    public static Transform2d toBoundsTransform(final Point2d location)
    {
        Transform2d transformation = new Transform2d();
        if (location instanceof Oriented<?>)
        {
            transformation.rotation(-((Oriented<?>) location).getDirZ());
        }
        transformation.translate(-location.getX(), -location.getY());
        return transformation;
    }

}
