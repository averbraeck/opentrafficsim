package org.opentrafficsim.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.animation.PerceptionAnimation.ChannelAttention;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelFuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTask;
import org.opentrafficsim.road.network.lane.conflict.Conflict;

/**
 * Draws circles around a GTU indicating the level of attention and perception delay.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceptionAnimation extends OtsRenderable<ChannelAttention>
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
     * @param gtu GTU
     */
    public PerceptionAnimation(final LaneBasedGtu gtu)
    {
        super(new ChannelAttention(gtu), gtu.getSimulator());
    }

    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        LaneBasedGtu gtu = getSource().getGtu();
        Optional<Mental> mental = gtu.getTacticalPlanner().getPerception().getMental();
        if (mental.isPresent() && mental.get() instanceof ChannelFuller fuller)
        {
            AffineTransform transform = graphics.getTransform();
            graphics.setStroke(new BasicStroke(LINE_WIDTH));
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Set<Object> channels = fuller.getChannels();
            boolean hasInVehicle = channels.contains(ChannelTask.IN_VEHICLE);
            for (Object channel : channels)
            {
                double attention = fuller.getAttention(channel);
                Duration perceptionDelay = fuller.getPerceptionDelay(channel);
                double angle;
                double radius = CENTER_RADIUS;
                boolean drawLine = !hasInVehicle;
                if (ChannelTask.LEFT.equals(channel))
                {
                    angle = Math.PI / 2.0;
                }
                else if (ChannelTask.FRONT.equals(channel))
                {
                    angle = 0.0;
                }
                else if (ChannelTask.RIGHT.equals(channel))
                {
                    angle = -Math.PI / 2.0;
                }
                else if (ChannelTask.REAR.equals(channel))
                {
                    angle = Math.PI;
                }
                else if (ChannelTask.IN_VEHICLE.equals(channel))
                {
                    angle = 0.0;
                    radius = 0.0;
                }
                else if (channel instanceof OtsShape object)
                {
                    Point2d point;
                    if (channel instanceof Conflict conflict)
                    {
                        // on a conflict we take a point 25m upstream, or the upstream conflicting node if closer
                        double x = conflict.getOtherConflict().getLongitudinalPosition().si - 25.0;
                        point = conflict.getOtherConflict().getLane().getCenterLine().getLocationExtendedSI(x < 0.0 ? 0.0 : x);
                    }
                    else
                    {
                        point = object.getLocation();
                    }
                    angle = AngleUtil.normalizeAroundZero(gtu.getLocation().directionTo(point) - gtu.getLocation().dirZ);
                    radius = CENTER_RADIUS_OBJECTS;
                    drawLine = true;
                }
                else
                {
                    continue;
                }
                drawAttentionCircle(graphics, gtu.getCenter().dx().si, attention, perceptionDelay, angle, radius, drawLine);
                graphics.setTransform(transform);
            }
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
            final Duration perceptionDelay, final double angle, final double radius, final boolean drawLine)
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
        Color color = SCALE.getPaint(Math.min(1.0, perceptionDelay.si));
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
     * Paints the icon (Attention24.png).
     * @param args not used
     * @throws IOException if icon cannot be written
     */
    public static void main(final String[] args) throws IOException
    {
        BufferedImage im = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) im.getGraphics();
        g.setStroke(new BasicStroke(LINE_WIDTH));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(12.0, 12.0);
        g.scale(3.2, 3.2);
        AffineTransform transform = g.getTransform();
        drawAttentionCircle(g, 0.0, 0.8, Duration.ZERO, 0.25 * Math.PI, CENTER_RADIUS, true); // front
        g.setTransform(transform);
        drawAttentionCircle(g, 0.0, 0.4, Duration.ofSI(0.2), 0.75 * Math.PI, CENTER_RADIUS, true); // left
        g.setTransform(transform);
        drawAttentionCircle(g, 0.0, 0.2, Duration.ofSI(0.4), -0.25 * Math.PI, CENTER_RADIUS, true); // right
        g.setTransform(transform);
        drawAttentionCircle(g, 0.0, 0.1, Duration.ofSI(0.8), -0.75 * Math.PI, CENTER_RADIUS, true); // rear
        File outputFile = new File(".." + File.separator + "ots-swing" + File.separator + "src" + File.separator + "main"
                + File.separator + "resources" + File.separator + "icons" + File.separator + "Perception24.png");
        ImageIO.write(im, "png", outputFile);
        System.out.println("Icon written to: " + outputFile.getAbsolutePath());
    }

    /**
     * Locatable for GTU in attention context.
     */
    public static class ChannelAttention implements OtsShape
    {
        /** GTU. */
        private final LaneBasedGtu gtu;

        /**
         * Constructor.
         * @param gtu GTU
         */
        public ChannelAttention(final LaneBasedGtu gtu)
        {
            this.gtu = gtu;
        }

        /**
         * Returns the GTU.
         * @return GTU
         */
        public LaneBasedGtu getGtu()
        {
            return this.gtu;
        }

        @Override
        public DirectedPoint2d getLocation()
        {
            return this.gtu.getLocation();
        }

        @Override
        public double getZ()
        {
            return DrawLevel.LABEL.getZ();
        }

        @Override
        public Polygon2d getRelativeContour()
        {
            return this.gtu.getRelativeContour();
        }

    }

}
