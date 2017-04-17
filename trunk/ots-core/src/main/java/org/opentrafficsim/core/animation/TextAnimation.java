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
    private final Locatable source;

    /** the text to display. */
    private String text;

    /** the horizontal movement of the text, in meters. */
    private final float dx;

    /** the vertical movement of the text, in meters. */
    private final float dy;

    /** whether to center or not. */
    private final TextAlignment textAlignment;

    /** the color of the text. */
    private Color color;

    /** fontSize the size of the font; default = 2.0 (meters). */
    private final float fontSize;

    /** the animation implementation. */
    private final AnimationImpl animationImpl;

    /** the font. */
    private Font font;

    /** the font rectangle. */
    private Rectangle2D fontRectangle = null;

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
        synchronized (this.font)
        {
            if (this.fontRectangle == null)
            {
                FontMetrics fm = graphics.getFontMetrics();
                this.fontRectangle = fm.getStringBounds(this.text, graphics);
            }
            graphics.setColor(this.color);
            float dxText =
                    this.textAlignment.equals(TextAlignment.LEFT) ? 0.0f : this.textAlignment.equals(TextAlignment.CENTER)
                            ? (float) -this.fontRectangle.getWidth() / 2.0f : (float) -this.fontRectangle.getWidth();
            graphics.drawString(this.text, dxText + this.dx, this.fontSize / 2.0f - this.dy);
        }
    }

    /**
     * Destroy the text animation.
     */
    public final void destroy()
    {
        try
        {
            this.animationImpl.destroy();
        }
        catch (NamingException exception)
        {
            System.err.println("Tried to destroy Text for GTU animation of GTU " + this.source.toString());
        }
    }

    /**
     * Clone the TextAnimation and return a copy for the new source on the new simulator.
     * @param newSource the new source to link to the text animation
     * @param newSimulator the new simulator to register the animation on
     * @return a copy of the TextAnimation
     * @throws RemoteException when remote animation cannot be reached
     * @throws NamingException when animation name cannot be found or bound in the Context
     */
    public abstract TextAnimation clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
            throws RemoteException, NamingException;

    /**
     * @return source
     */
    protected final Locatable getSource()
    {
        return this.source;
    }

    /**
     * @return dx
     */
    protected final float getDx()
    {
        return this.dx;
    }

    /**
     * @return dy
     */
    protected final float getDy()
    {
        return this.dy;
    }

    /**
     * @return textAlignment
     */
    protected final TextAlignment getTextAlignment()
    {
        return this.textAlignment;
    }

    /**
     * @return fontSize
     */
    protected final float getFontSize()
    {
        return this.fontSize;
    }

    /**
     * @return font
     */
    protected final Font getFont()
    {
        return this.font;
    }

    /**
     * @return current text
     */
    protected final String getText()
    {
        return this.text;
    }

    /**
     * @param text set new text
     */
    protected final void setText(final String text)
    {
        this.text = text;
        synchronized (this.font)
        {
            this.fontRectangle = null;
        }
    }

    /**
     * @return current color
     */
    protected final Color getColor()
    {
        return this.color;
    }

    /**
     * @param color set new color
     */
    protected final void setColor(final Color color)
    {
        this.color = color;
    }

    /**
     * @return Returns the flip.
     */
    public final boolean isFlip()
    {
        return this.animationImpl.isFlip();
    }

    /**
     * @param flip The flip to set.
     */
    public final void setFlip(final boolean flip)
    {
        this.animationImpl.setFlip(flip);
    }

    /**
     * @return Returns the rotate.
     */
    public final boolean isRotate()
    {
        return this.animationImpl.isRotate();
    }

    /**
     * @param rotate The rotate to set.
     */
    public final void setRotate(final boolean rotate)
    {
        this.animationImpl.setRotate(rotate);

    }

    /**
     * @return Returns the scale.
     */
    public final boolean isScale()
    {
        return this.animationImpl.isScale();
    }

    /**
     * @param scale The scale to set.
     */
    public final void setScale(final boolean scale)
    {
        this.animationImpl.setScale(scale);
    }

    /**
     * @return Returns the translate.
     */
    public final boolean isTranslate()
    {
        return this.animationImpl.isTranslate();
    }

    /**
     * @param translate The translate to set.
     */
    public final void setTranslate(final boolean translate)
    {
        this.animationImpl.setTranslate(translate);
    }

    /**
     * The implementation of the text animation. Cloning will be taken care of by the overarching TextAnimation-derived class.
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
    private static class AnimationImpl extends Renderable2D implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

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
