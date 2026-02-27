package org.opentrafficsim.draw;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.geom.Point2D;

import org.djutils.draw.Transform2d;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsShape;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Extends {@code Renderable2d} to let the {@code contains} method look at the actual bounds shape, rather than only the box.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <L> locatable type
 */
public abstract class OtsRenderable<L extends OtsShape> extends Renderable2d<L>
{

    /** Standard rendering keys. */
    private static final Key[] RENDERING_KEYS = new Key[] {RenderingHints.KEY_ANTIALIASING};

    /** Standard rendering values. */
    private static final Object[] RENDERING_VALUES = new Object[] {RenderingHints.VALUE_ANTIALIAS_ON};

    /** Stored hints to reset. */
    private Object[] oldRenderingHints = new Object[RENDERING_KEYS.length];

    /** Whether the object is dynamic. */
    private boolean isDynamic;

    /**
     * Constructor.
     * @param source the source
     * @param contextProvider the object that can provide the context to store the animation objects
     */
    public OtsRenderable(final L source, final Contextualized contextProvider)
    {
        super(source, contextProvider);
        setScaleY(true);
    }

    /**
     * Sets the dynamic state.
     * @param dynamic dynamic state
     * @return this renderable for method changing
     */
    public OtsRenderable<L> setDynamic(final boolean dynamic)
    {
        this.isDynamic = dynamic;
        return this;
    }

    /**
     * Returns whether the renderable is dynamic.
     * @return whether the renderable is dynamic
     */
    public boolean isDynamic()
    {
        return this.isDynamic;
    }

    /**
     * Set standard rendering hints for this renderable to paint. The graphics should be reset using {@link #resetRendering}
     * after painting.
     * @param graphics graphics
     */
    protected void setRendering(final Graphics2D graphics)
    {
        for (int i = 0; i < RENDERING_KEYS.length; i++)
        {
            this.oldRenderingHints[i] = graphics.getRenderingHint(RENDERING_KEYS[i]);
            graphics.setRenderingHint(RENDERING_KEYS[i], RENDERING_VALUES[i]);
        }
    }

    /**
     * Resets rendering hints that this renderable changed through {@link #setRendering}.
     * @param graphics graphics
     */
    protected void resetRendering(final Graphics2D graphics)
    {
        for (int i = 0; i < RENDERING_KEYS.length; i++)
        {
            // If ever a null valued hint is used, just check for null values and do not reset the value in that case.
            // For now the check is not implemented as no such hint is used.
            // if (this.oldRenderingHints[i] != null)
            // {
            graphics.setRenderingHint(RENDERING_KEYS[i], this.oldRenderingHints[i]);
            // }
        }
    }

    @Override
    public boolean contains(final Point2D pointScreenCoordinates, final Bounds2d extent, final Dimension screenSize,
            final RenderableScale scale, final double worldMargin, final double pixelMargin)
    {
        // super implementation seems to not handle rotation well, so we apply a different transformation order here
        Point2d screenLocation = scale.getScreenCoordinatesAsPoint2d(getSource().getLocation(), extent, screenSize);
        double xScale = scale.getXScale(extent, screenSize);
        double yScale = scale.getYScale(extent, screenSize);
        Transform2d transformation = new Transform2d();
        transformation.rotation(-getSource().getDirZ());
        transformation.scale(xScale, yScale);
        transformation.reflectY();
        transformation.translate(screenLocation.neg());
        Point2d pointRelativeTo00 =
                transformation.transform(new Point2d(pointScreenCoordinates.getX(), pointScreenCoordinates.getY()));
        return contains(pointRelativeTo00, scale, worldMargin, pixelMargin, xScale, yScale);
    }

    @Override
    public boolean contains(final Point2d pointRelativeTo00, final RenderableScale scale, final double worldMargin,
            final double pixelMargin, final double xScale, final double yScale)
    {
        return getSource().contains(pointRelativeTo00.x, pointRelativeTo00.y);
    }

}
