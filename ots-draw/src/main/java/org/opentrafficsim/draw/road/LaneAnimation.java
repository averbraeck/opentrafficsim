package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.draw.core.ClonableRenderable2DInterface;
import org.opentrafficsim.draw.core.PaintLine;
import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneAnimation extends Renderable2D<Lane> implements ClonableRenderable2DInterface<Lane>, Serializable
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
     * @param simulator OTSSimulatorInterface; the simulator
     * @param color Color; Color of the lane.
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public LaneAnimation(final Lane lane, final OTSSimulatorInterface simulator, final Color color)
            throws NamingException, RemoteException
    {
        super(lane, simulator);
        this.color = color;
        this.text = new Text(lane, lane.getParentLink().getId() + "." + lane.getId(), 0.0f, 0.0f, TextAlignment.CENTER,
                Color.BLACK, simulator);
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
            PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour(), true);
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
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<Lane> clone(final Lane newSource, final OTSSimulatorInterface newSimulator)
            throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new LaneAnimation(newSource, newSimulator, this.color);
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
        private final OTSLine3D centerLine;

        /**
         * Construct a new CenterLine.
         * @param centerLine OTSLine3D; the center line of a lane
         */
        CenterLine(final OTSLine3D centerLine)
        {
            this.centerLine = centerLine;
        }

        @Override
        public final DirectedPoint getLocation()
        {
            DirectedPoint dp = this.centerLine.getLocation();
            return new DirectedPoint(dp.x, dp.y, dp.z + 0.1);
        }

        @Override
        public final Bounds getBounds() throws RemoteException
        {
            return this.centerLine.getBounds();
        }

        /**
         * Retrieve the center line.
         * @return OTSLine3D; the center line
         */
        public OTSLine3D getCenterLine()
        {
            return this.centerLine;
        }

    }

    /**
     * Animation for center line of a lane.
     */
    public static class CenterLineAnimation extends Renderable2D<CenterLine>
            implements ClonableRenderable2DInterface<CenterLine>, Serializable
    {
        /** Drawing color for the center line. */
        private static final Color COLOR = Color.MAGENTA.darker().darker();

        /**  */
        private static final long serialVersionUID = 20180426L;

        /**
         * Construct a new CenterLineAnimation.
         * @param centerLine CemterLine; the center line of a lane
         * @param simulator OTSSimulatorInterface; the simulator
         * @throws NamingException when the name of this object is not unique
         * @throws RemoteException when communication with a remote process fails
         */
        public CenterLineAnimation(final CenterLine centerLine, final OTSSimulatorInterface simulator)
                throws NamingException, RemoteException
        {
            super(centerLine, simulator);
        }

        @Override
        public final ClonableRenderable2DInterface<CenterLine> clone(final CenterLine newSource,
                final OTSSimulatorInterface newSimulator) throws NamingException, RemoteException
        {
            // TODO Auto-generated method stub
            return null;
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
         * @param simulator OTSSimulatorInterface; the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final OTSSimulatorInterface simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, simulator, TextAnimation.RENDERALWAYS);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public DirectedPoint getLocation()
        {
            // draw always on top.
            DirectedPoint p = ((Lane) getSource()).getCenterLine().getLocationFractionExtended(0.5);
            double a = Angle.normalizePi(p.getRotZ());
            if (a > Math.PI / 2.0 || a < -0.99 * Math.PI / 2.0)
            {
                a += Math.PI;
            }
            return new DirectedPoint(p.x, p.y, Double.MAX_VALUE, 0.0, 0.0, a);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public TextAnimation clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
                throws RemoteException, NamingException
        {
            return new Text(newSource, getText(), getDx(), getDy(), getTextAlignment(), getColor(), newSimulator);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Text []";
        }

    }

}
