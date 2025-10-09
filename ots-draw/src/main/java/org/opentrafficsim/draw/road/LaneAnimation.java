package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.util.function.Supplier;

import org.djutils.base.Identifiable;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.LineLocatable;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.PaintLine;
import org.opentrafficsim.draw.RenderableTextSource;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws LaneData.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneAnimation extends CrossSectionElementAnimation<LaneData>
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** Color of the lane. */
    private final Color color;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /** Center line animation. */
    private final CenterLineAnimation centerLineAnimation;

    /**
     * Animate a Lane.
     * @param lane the lane
     * @param contextualized context provider
     * @param color Color of the lane.
     */
    public LaneAnimation(final LaneData lane, final Contextualized contextualized, final Color color)
    {
        super(lane, contextualized, color);
        this.color = color;
        this.text = new Text(lane, lane::getId, 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, contextualized);
        this.centerLineAnimation = new CenterLineAnimation(
                new CenterLine(lane.getCenterLine(), lane.getLinkId() + "." + lane.getId()), contextualized);
    }

    /**
     * Returns text object.
     * @return text.
     */
    public final Text getText()
    {
        return this.text;
    }

    @Override
    public void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        this.text.destroy(contextProvider);
        this.centerLineAnimation.destroy(contextProvider);
    }

    @Override
    public final String toString()
    {
        return "LaneAnimation [lane = " + getSource().toString() + ", color=" + this.color + "]";
    }

    /**
     * Draw center line of a lane.
     */
    public static class CenterLine implements LineLocatable
    {
        /** The center line. */
        private final PolyLine2d centerLine;

        /** Location. */
        private final DirectedPoint2d location;

        /** Shape (cached). */
        private Polygon2d relativeContour;

        /** Lane id. */
        private final String fullId;

        /**
         * Construct a new CenterLine.
         * @param centerLine the center line of a lane
         * @param fullId lane id.
         */
        CenterLine(final PolyLine2d centerLine, final String fullId)
        {
            this.centerLine = centerLine;
            this.location = new DirectedPoint2d(this.centerLine.getAbsoluteBounds().midPoint(), 0.0);
            this.fullId = fullId;
        }

        @Override
        public final DirectedPoint2d getLocation()
        {
            return this.location;
        }

        @Override
        public Polygon2d getRelativeContour()
        {
            if (this.relativeContour == null)
            {
                this.relativeContour =
                        new Polygon2d(OtsShape.toRelativeTransform(getLocation()).transform(getAbsoluteContour().iterator()));
            }
            return this.relativeContour;
        }

        @Override
        public Polygon2d getAbsoluteContour()
        {
            return new Polygon2d(this.centerLine.iterator());
        }

        /**
         * Returns the center line.
         * @return the center line
         */
        public PolyLine2d getCenterLine()
        {
            return this.centerLine;
        }

        @Override
        public PolyLine2d getLine()
        {
            return OtsShape.transformLine(this.centerLine, getLocation());
        }

        @Override
        public double getZ()
        {
            return DrawLevel.CENTER_LINE.getZ();
        }

        @Override
        public String toString()
        {
            return "Center line " + this.fullId;
        }

    }

    /**
     * Animation for center line of a lane.
     */
    public static class CenterLineAnimation extends OtsRenderable<CenterLine>
    {
        /** Drawing color for the center line. */
        private static final Color COLOR = Color.MAGENTA.darker().darker();

        /**  */
        private static final long serialVersionUID = 20180426L;

        /** Drawable path. */
        private final Path2D.Float path;

        /**
         * Construct a new CenterLineAnimation.
         * @param centerLine the center line of a lane
         * @param contextualized context provider
         */
        public CenterLineAnimation(final CenterLine centerLine, final Contextualized contextualized)
        {
            super(centerLine, contextualized);
            this.path = PaintLine.getPath(getSource().getLocation(), getSource().getCenterLine());
        }

        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer)
        {
            setRendering(graphics);
            PaintLine.paintLine(graphics, COLOR, 0.1, this.path);
            resetRendering(graphics);
        }

    }

    /**
     * Text animation for the Node. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class Text extends RenderableTextSource<LaneData, Text>
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * Constructor.
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param contextualized context provider
         */
        public Text(final LaneData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized)
        {
            super(source, text, dx, dy, textPlacement, color, contextualized, RenderableTextSource.RENDERWHEN10);
        }

        @Override
        public final String toString()
        {
            return "Text []";
        }

    }

    /**
     * LaneData provides the information required to draw a lane.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface LaneData extends CrossSectionElementData, Identifiable
    {
        @Override
        default double getZ()
        {
            return DrawLevel.LANE.getZ();
        }
    }

}
