package org.opentrafficsim.animation.road;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.animation.DrawLevel;
import org.opentrafficsim.animation.LineLocatable;
import org.opentrafficsim.animation.OtsRenderableLabeled;
import org.opentrafficsim.animation.RenderableTextSource;
import org.opentrafficsim.animation.road.AbstractLineAnimation.LaneBasedObjectData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Abstract class for objects that draw a line perpendicular on the lane.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <L> source type
 * @param <T> text renderable type
 */
public abstract class AbstractLineAnimation<L extends LaneBasedObjectData, T extends RenderableTextSource<L, T>>
        extends OtsRenderableLabeled<L, T>
{

    /** Rectangle to color. */
    private final Rectangle2D rectangle;

    /**
     * Construct the line animation. This constructor uses an empty prefix for the label.
     * @param source source
     * @param contextualized context provider
     * @param width line width
     */
    public AbstractLineAnimation(final L source, final Contextualized contextualized, final Length width)
    {
        this(source, contextualized, width, "");
    }

    /**
     * Construct the line animation.
     * @param source source
     * @param contextualized context provider
     * @param width line width
     * @param prefix label prefix
     */
    public AbstractLineAnimation(final L source, final Contextualized contextualized, final Length width, final String prefix)
    {
        super(source, contextualized, prefix);
        double halfLength = .5 * source.getLine().getLength();
        this.rectangle = new Rectangle2D.Double(-.5 * width.si, -halfLength, width.si, 2 * halfLength);
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        setRendering(graphics);
        graphics.fill(this.rectangle);
        resetRendering(graphics);
    }

    /**
     * Object data to draw a lane based object.
     */
    public interface LaneBasedObjectData extends LineLocatable, Identifiable
    {

        /**
         * Returns the width of the lane.
         * @return width of the lane.
         */
        Length getLaneWidth();

        @Override
        DirectedPoint2d getLocation();

        @Override
        default double getZ()
        {
            return DrawLevel.OBJECT.getZ();
        }

    }

}
