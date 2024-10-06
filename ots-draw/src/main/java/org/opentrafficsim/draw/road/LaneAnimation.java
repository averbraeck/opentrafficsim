package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djutils.base.Identifiable;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsRenderable;
import org.opentrafficsim.draw.ClickableLineLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.PaintLine;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
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
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public LaneAnimation(final LaneData lane, final Contextualized contextualized, final Color color)
            throws NamingException, RemoteException
    {
        super(lane, contextualized, color);
        this.color = color;
        this.text = new Text(lane, lane::getId, 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, contextualized);
        this.centerLineAnimation = new CenterLineAnimation(
                new CenterLine(lane.getCenterLine(), lane.getLinkId() + "." + lane.getId()), contextualized);
    }

    /**
     * @return text.
     */
    public final Text getText()
    {
        return this.text;
    }

    /** {@inheritDoc} */
    @Override
    public void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        this.text.destroy(contextProvider);
        this.centerLineAnimation.destroy(contextProvider);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneAnimation [lane = " + getSource().toString() + ", color=" + this.color + "]";
    }

    /**
     * Draw center line of a lane.
     */
    public static class CenterLine implements ClickableLineLocatable
    {
        /** The center line. */
        private final PolyLine2d centerLine;

        /** Location. */
        private final OrientedPoint2d location;

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
            this.location = new OrientedPoint2d(this.centerLine.getBounds().midPoint(), 0.0);
            this.fullId = fullId;
        }

        /** {@inheritDoc} */
        @Override
        public final OrientedPoint2d getLocation()
        {
            return this.location;
        }

        /** {@inheritDoc} */
        @Override
        public Polygon2d getContour()
        {
            return new Polygon2d(this.centerLine.getPoints());
        }

        /**
         * Returns the center line.
         * @return the center line
         */
        public PolyLine2d getCenterLine()
        {
            return this.centerLine;
        }

        /** {@inheritDoc} */
        @Override
        public PolyLine2d getLine()
        {
            return OtsLocatable.transformLine(this.centerLine, getLocation());
        }

        /** {@inheritDoc} */
        @Override
        public double getZ()
        {
            return DrawLevel.CENTER_LINE.getZ();
        }

        /** {@inheritDoc} */
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
         * @throws NamingException when the name of this object is not unique
         * @throws RemoteException when communication with a remote process fails
         */
        public CenterLineAnimation(final CenterLine centerLine, final Contextualized contextualized)
                throws NamingException, RemoteException
        {
            super(centerLine, contextualized);
            this.path = PaintLine.getPath(getSource().getLocation(), getSource().getCenterLine());
        }

        /** {@inheritDoc} */
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
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public class Text extends TextAnimation<LaneData, Text>
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param contextualized context provider
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final LaneData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, contextualized, TextAnimation.RENDERWHEN10);
        }

        /** {@inheritDoc} */
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
        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.LANE.getZ();
        }
    }

}
