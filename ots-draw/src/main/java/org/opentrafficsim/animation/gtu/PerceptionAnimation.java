package org.opentrafficsim.animation.gtu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.animation.BoundsPaintScale;
import org.opentrafficsim.animation.Colors;
import org.opentrafficsim.animation.OtsRenderable;
import org.opentrafficsim.animation.gtu.PerceptionAnimation.PerceptionData;
import org.opentrafficsim.animation.gtu.PerceptionAnimation.PerceptionData.ChannelData;
import org.opentrafficsim.animation.gtu.PerceptionAnimation.PerceptionData.ChannelRadius;
import org.opentrafficsim.base.geometry.OtsShape;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws circles around a GTU indicating the level of attention and perception delay.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class PerceptionAnimation extends OtsRenderable<PerceptionData>
{

    /** Maximum radius of attention circles. */
    private static final double MAX_RADIUS = 1.425; // 1.5 minus half of LINE_WIDTH

    /** Radius around GTU along which the regular attention circles are placed. */
    private static final double CENTER_RADIUS = 3.0;

    /** Radius around GTU along which the attention circles of objects are placed. */
    private static final double CENTER_RADIUS_OBJECTS = 6.0;

    /** Line width around circle. */
    private static final float LINE_WIDTH = 0.15f;

    /** Color scale for perception delay. */
    private static final BoundsPaintScale SCALE =
            new BoundsPaintScale(new double[] {0.0, 0.25, 0.5, 0.75, 1.0}, Colors.GREEN_RED_DARK);

    /**
     * Constructor.
     * @param perceptionData perception data
     * @param contextualized contextualized, e.g. simulator
     */
    public PerceptionAnimation(final PerceptionData perceptionData, final Contextualized contextualized)
    {
        super(perceptionData, contextualized);
    }

    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (getSource().getChannels() == null || getSource().getChannels().isEmpty())
        {
            return;
        }

        AffineTransform transform = graphics.getTransform();
        graphics.setStroke(new BasicStroke(LINE_WIDTH));
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean hasInVehicle = getSource().getChannels().stream().anyMatch((cd) -> cd.radius.equals(ChannelRadius.ZERO));
        double dx = getSource().getDeltaCenterX().si;
        for (ChannelData channelData : getSource().getChannels())
        {
            double radius = channelData.radius().equals(ChannelRadius.NEAR) ? CENTER_RADIUS
                    : (channelData.radius().equals(ChannelRadius.FAR) ? CENTER_RADIUS_OBJECTS : 0.0);
            boolean drawLine = (channelData.radius().equals(ChannelRadius.NEAR) && !hasInVehicle)
                    || channelData.radius().equals(ChannelRadius.FAR);
            drawAttentionCircle(graphics, dx, channelData.attention(), channelData.perceptionDelay().si, channelData.angle().si,
                    radius, drawLine);
            graphics.setTransform(transform);
        }

    }

    /**
     * Draws attention circle.
     * @param graphics graphics
     * @param dx longitudinal shift
     * @param attention attention level
     * @param perceptionDelay perception delay
     * @param angle angle to draw circle at relative to GTU
     * @param radius center circle radius around GTU
     * @param drawLine whether to draw the line
     */
    private static void drawAttentionCircle(final Graphics2D graphics, final double dx, final double attention,
            final double perceptionDelay, final double angle, final double radius, final boolean drawLine)
    {
        // on center of GTU
        graphics.translate(dx, 0.0);
        graphics.rotate(-angle, 0.0, 0.0);

        // connecting line
        if (drawLine)
        {
            graphics.setColor(Color.GRAY);
            graphics.draw(new Line2D.Double(0.0, 0.0, radius - MAX_RADIUS - LINE_WIDTH, 0.0));
        }

        // transparent background fill
        Color color = SCALE.getPaint(Math.min(1.0, perceptionDelay));
        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 48));
        graphics.fill(new Ellipse2D.Double(radius - MAX_RADIUS, -MAX_RADIUS, 2.0 * MAX_RADIUS, 2.0 * MAX_RADIUS));

        // non-transparent attention fill
        graphics.setColor(color);
        double r = Math.sqrt(attention);
        graphics.fill(
                new Ellipse2D.Double(radius - r * MAX_RADIUS, -r * MAX_RADIUS, 2.0 * r * MAX_RADIUS, 2.0 * r * MAX_RADIUS));

        // edge of circle
        graphics.setColor(Color.GRAY);
        float lineWidth = LINE_WIDTH - 0.02f; // prevent tiny edges between fill and border
        graphics.draw(new Ellipse2D.Double(radius - MAX_RADIUS - .5 * lineWidth, -MAX_RADIUS - .5 * lineWidth,
                2.0 * MAX_RADIUS + lineWidth, 2.0 * MAX_RADIUS + lineWidth));
    }

    /**
     * Perception animation data.
     */
    public interface PerceptionData extends OtsShape
    {

        /**
         * Returns the forward delta from the reference to the center of the channel animation. This value may be negative.
         * @return forward delta from the reference to the center of the channel animation
         */
        Length getDeltaCenterX();

        /**
         * Returns the channels.
         * @return channels
         */
        Set<ChannelData> getChannels();

        /**
         * Data required per channel.
         * @param angle angle relative to GTU
         * @param radius radius from GTU reference
         * @param attention attention level
         * @param perceptionDelay perception delay
         */
        record ChannelData(Angle angle, ChannelRadius radius, double attention, Duration perceptionDelay)
        {
            /**
             * Constructor.
             * @param angle angle relative to GTU
             * @param radius radius from GTU reference
             * @param attention attention level
             * @param perceptionDelay perception delay
             */
            public ChannelData
            {
                Throw.whenNull(angle, "angle");
                Throw.whenNull(radius, "radius");
                Throw.whenNull(attention, "attention");
                Throw.whenNull(perceptionDelay, "perceptionDelay");
            }
        }

        /**
         * Radius type.
         */
        enum ChannelRadius
        {
            /** Drawn with 0 radius. */
            ZERO,

            /** Drawn with small radius. */
            NEAR,

            /** Drawn with large radius. */
            FAR;
        }

    }

}
