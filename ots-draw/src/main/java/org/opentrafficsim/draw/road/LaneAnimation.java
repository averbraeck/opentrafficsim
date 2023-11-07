package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.ClickableBounds;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.PaintLine;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2dInterface;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws LaneData.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LaneAnimation extends Renderable2d<LaneData> implements Renderable2dInterface<LaneData>, Serializable
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
     * @param lane LaneData; the lane
     * @param contextualized Contextualized; context provider
     * @param color Color; Color of the lane.
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public LaneAnimation(final LaneData lane, final Contextualized contextualized, final Color color)
            throws NamingException, RemoteException
    {
        super(lane, contextualized);
        this.color = color;
        this.text = new Text(lane, lane::getId, 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, contextualized);
        this.centerLineAnimation = new CenterLineAnimation(new CenterLine(lane.getCenterLine(), lane.getId()), contextualized);
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
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        LaneData lane = getSource();
        if (this.color != null)
        {
            PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour(), true);
        }
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
    public static class CenterLine implements Locatable
    {
        /** The center line. */
        private final PolyLine2d centerLine;

        /** Bounds. */
        private final Bounds2d bounds;

        /** Lane id. */
        private final String laneId;

        /**
         * Construct a new CenterLine.
         * @param centerLine OtsLine2d; the center line of a lane
         * @param laneId String; lane id.
         */
        CenterLine(final PolyLine2d centerLine, final String laneId)
        {
            this.centerLine = centerLine;
            this.bounds = new Bounds2d(centerLine.getBounds().getDeltaX(), centerLine.getBounds().getDeltaY());
            this.laneId = laneId;
        }

        /** {@inheritDoc} */
        @Override
        public final Point2d getLocation()
        {
            return this.centerLine.getBounds().midPoint();
        }

        /** {@inheritDoc} */
        @Override
        public final Bounds2d getBounds()
        {
            return ClickableBounds.get(this.bounds);
        }

        /**
         * Retrieve the center line.
         * @return OtsLine2d; the center line
         */
        public PolyLine2d getCenterLine()
        {
            return this.centerLine;
        }

        /** {@inheritDoc} */
        @Override
        public double getZ()
        {
            return DrawLevel.LINE.getZ();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "Center line " + this.laneId;
        }
    }

    /**
     * Animation for center line of a lane.
     */
    public static class CenterLineAnimation extends Renderable2d<CenterLine>
            implements Renderable2dInterface<CenterLine>, Serializable
    {
        /** Drawing color for the center line. */
        private static final Color COLOR = Color.MAGENTA.darker().darker();

        /**  */
        private static final long serialVersionUID = 20180426L;

        /**
         * Construct a new CenterLineAnimation.
         * @param centerLine CemterLine; the center line of a lane
         * @param contextualized Contextualized; context provider
         * @throws NamingException when the name of this object is not unique
         * @throws RemoteException when communication with a remote process fails
         */
        public CenterLineAnimation(final CenterLine centerLine, final Contextualized contextualized)
                throws NamingException, RemoteException
        {
            super(centerLine, contextualized);
        }

        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer)
        {
            PaintLine.paintLine(graphics, COLOR, 0.1, getSource().getLocation(), getSource().getCenterLine());
        }

    }

    /**
     * Text animation for the Node. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source Locatable; the object for which the text is displayed
         * @param text Supplier&lt;String&gt;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, contextualized, TextAnimation.RENDERALWAYS);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public OrientedPoint2d getLocation()
        {
            // draw always on top.
            Ray2d p = ((LaneData) getSource()).getCenterLine().getLocationFractionExtended(0.5);
            double a = Angle.normalizePi(p.getPhi());
            if (a > Math.PI / 2.0 || a < -0.99 * Math.PI / 2.0)
            {
                a += Math.PI;
            }
            return new OrientedPoint2d(p.x, p.y, a);
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
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface LaneData extends Locatable, Identifiable
    {
        /**
         * Returns the center line.
         * @return PolyLine2d; center line.
         */
        PolyLine2d getCenterLine();

        /**
         * Returns the contour.
         * @return List&lt;Point2d&gt;; points.
         */
        List<Point2d> getContour();

        /** {@inheritDoc} */
        @Override
        Point2d getLocation();

        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.LANE.getZ();
        }
    }

}
