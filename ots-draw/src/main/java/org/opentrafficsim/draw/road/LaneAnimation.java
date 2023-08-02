package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.draw.core.PaintLine;
import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class LaneAnimation extends Renderable2D<Lane> implements Renderable2DInterface<Lane>, Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** Color of the lane. */
    private final Color color;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Animate a Lane.
     * @param lane Lane; the lane
     * @param simulator OtsSimulatorInterface; the simulator
     * @param color Color; Color of the lane.
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public LaneAnimation(final Lane lane, final OtsSimulatorInterface simulator, final Color color)
            throws NamingException, RemoteException
    {
        super(lane, simulator);
        this.color = color;
        this.text = new Text(lane, lane.getLink().getId() + "." + lane.getId(), 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK,
                simulator);
        new CenterLineAnimation(new CenterLine(lane.getCenterLine()), simulator);
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
        Lane lane = getSource();
        if (this.color != null)
        {
            PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour().getPointList(), true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        this.text.destroy(contextProvider);
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
        private final OtsLine2d centerLine;

        /** Bounds. */
        private final Bounds2d bounds;

        /**
         * Construct a new CenterLine.
         * @param centerLine OtsLine2d; the center line of a lane
         */
        CenterLine(final OtsLine2d centerLine)
        {
            this.centerLine = centerLine;
            this.bounds = new Bounds2d(centerLine.getBounds().getDeltaX(), centerLine.getBounds().getDeltaY());
        }

        @Override
        public final Point2d getLocation()
        {
            return this.centerLine.getLocation();
        }

        @Override
        public final Bounds2d getBounds()
        {
            return this.bounds;
        }

        /**
         * Retrieve the center line.
         * @return OtsLine2d; the center line
         */
        public OtsLine2d getCenterLine()
        {
            return this.centerLine;
        }

    }

    /**
     * Animation for center line of a lane.
     */
    public static class CenterLineAnimation extends Renderable2D<CenterLine>
            implements Renderable2DInterface<CenterLine>, Serializable
    {
        /** Drawing color for the center line. */
        private static final Color COLOR = Color.MAGENTA.darker().darker();

        /**  */
        private static final long serialVersionUID = 20180426L;

        /**
         * Construct a new CenterLineAnimation.
         * @param centerLine CemterLine; the center line of a lane
         * @param simulator OtsSimulatorInterface; the simulator
         * @throws NamingException when the name of this object is not unique
         * @throws RemoteException when communication with a remote process fails
         */
        public CenterLineAnimation(final CenterLine centerLine, final OtsSimulatorInterface simulator)
                throws NamingException, RemoteException
        {
            super(centerLine, simulator);
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
         * @param text String; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param simulator OtsSimulatorInterface; the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final OtsSimulatorInterface simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, simulator, TextAnimation.RENDERALWAYS);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public OrientedPoint2d getLocation()
        {
            // draw always on top.
            OrientedPoint2d p = ((Lane) getSource()).getCenterLine().getLocationFractionExtended(0.5);
            double a = Angle.normalizePi(p.getDirZ());
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

}
