package org.opentrafficsim.core.animation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Display a text for another Locatable object.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class TextAnimation implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /** the object for which the text is displayed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Locatable source;

    /** the text to display. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final String text;

    /** the horizontal movement of the text, in meters. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final float dx;

    /** the vertical movement of the text, in meters. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final float dy;

    /** whether to center or not. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final TextAlignment textAlignment;

    /** the color of the text. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Color color;

    /** fontSize the size of the font; default = 2.0 (meters). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final float fontSize;

    /** the animation implementation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final AnimationImpl animationImpl;
    
    /** the font. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Font font;

    /**
     * @param source the object for which the text is displayed
     * @param text the text to display
     * @param dx the horizontal movement of the text, in meters
     * @param dy the vertical movement of the text, in meters
     * @param textAlignment where to place the text
     * @param color the color of the text
     * @param fontSize the size of the font; default = 2.0 (meters)
     * @param simulator the simulator
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException - when remote context cannot be found
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TextAnimation(final Locatable source, final String text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final float fontSize, final OTSSimulatorInterface simulator)
            throws RemoteException, NamingException
    {
        this.source = source;
        this.text = text;
        this.dx = dx;
        this.dy = dy;
        this.textAlignment = textAlignment;
        this.color = color;
        this.fontSize = fontSize;

        this.font = new Font("SansSerif", Font.PLAIN, 2);
        if (this.fontSize != 2.0f)
        {
            this.font = this.font.deriveFont(this.fontSize);
        }
        
        this.animationImpl = new AnimationImpl(this, simulator);
    }

    /**
     * @param source the object for which the text is displayed
     * @param text the text to display
     * @param dx the horizontal movement of the text, in meters
     * @param dy the vertical movement of the text, in meters
     * @param textAlignment where to place the text
     * @param color the color of the text
     * @param simulator the simulator
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException - when remote context cannot be found
     */
    public TextAnimation(final Locatable source, final String text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final OTSSimulatorInterface simulator)
            throws RemoteException, NamingException
    {
        this(source, text, dx, dy, textAlignment, color, 2.0f, simulator);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation() throws RemoteException
    {
        // draw always on top.
        DirectedPoint p = this.source.getLocation();
        return new DirectedPoint(p.x, p.y, Double.MAX_VALUE, 0.0, 0.0, p.getRotZ());
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(0.0, 0.0, 0.0);
    }

    /**
     * paint() method so it can be overridden or extended.
     * @param graphics the graphics object
     * @param observer the observer
     * @throws RemoteException on network exception
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setFont(this.font);
        FontMetrics fm = graphics.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(this.text, graphics);
        graphics.setColor(this.color);
        float dxText = this.textAlignment.equals(TextAlignment.LEFT) ? 0.0f
                : this.textAlignment.equals(TextAlignment.CENTER) ? (float) -r.getWidth() / 2.0f : (float) -r.getWidth();
        graphics.drawString(this.text, dxText + this.dx, this.fontSize / 2.0f - this.dy);
    }

    /**
     * The implementation of the text animation.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Dec 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    protected static class AnimationImpl extends Renderable2D
    {
        /**
         * @param source the source
         * @param simulator the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        AnimationImpl(final Locatable source, final OTSSimulatorInterface simulator) throws NamingException, RemoteException
        {
            super(source, simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
        {
            TextAnimation ta = ((TextAnimation) getSource());
            ta.paint(graphics, observer);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TextAnimation.AnimationImpl []";
        }

    }
}
