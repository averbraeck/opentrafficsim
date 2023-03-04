package org.opentrafficsim.draw.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djutils.draw.Oriented;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Display a text for another Locatable object.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class TextAnimation implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /** The object for which the text is displayed. */
    private final Locatable source;

    /** The text to display. */
    private String text;

    /** The horizontal movement of the text, in meters. */
    private float dx;

    /** The vertical movement of the text, in meters. */
    private float dy;

    /** Whether to center or not. */
    private final TextAlignment textAlignment;

    /** The color of the text. */
    private Color color;

    /** FontSize the size of the font; default = 2.0 (meters). */
    private final float fontSize;

    /** Minimum font size to trigger scaling. */
    private final float minFontSize;

    /** Maximum font size to trigger scaling. */
    private final float maxFontSize;

    /** The animation implementation. */
    private final AnimationImpl animationImpl;

    /** The font. */
    private Font font;

    /** Access to the current background color. */
    private final ContrastToBackground background;

    /** The font rectangle. */
    private Rectangle2D fontRectangle = null;

    /** Render dependent on font scale. */
    private final ScaleDependentRendering scaleDependentRendering;

    /**
     * Construct a new TextAnimation.
     * @param source Locatable; the object for which the text is displayed
     * @param text String; the text to display
     * @param dx float; the horizontal movement of the text, in meters
     * @param dy float; the vertical movement of the text, in meters
     * @param textAlignment TextAlignment; where to place the text
     * @param color Color; the color of the text
     * @param fontSize float; the size of the font; default = 2.0 (meters)
     * @param minFontSize float; minimum font size resulting from scaling
     * @param maxFontSize float; maximum font size resulting from scaling
     * @param simulator OtsSimulatorInterface; the simulator
     * @param background ContrastToBackground; allows querying the background color and adaptation of the actual color of the
     *            text to ensure contrast
     * @param scaleDependentRendering ScaleDependentRendering; suppress rendering when font scale is too small
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TextAnimation(final Locatable source, final String text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final float fontSize, final float minFontSize,
            final float maxFontSize, final OtsSimulatorInterface simulator, final ContrastToBackground background,
            final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
    {
        this.source = source;
        this.text = text;
        this.dx = dx;
        this.dy = dy;
        this.textAlignment = textAlignment;
        this.color = color;
        this.fontSize = fontSize;
        this.minFontSize = minFontSize;
        this.maxFontSize = maxFontSize;
        this.background = background;
        this.scaleDependentRendering = scaleDependentRendering;

        this.font = new Font("SansSerif", Font.PLAIN, 2);
        if (this.fontSize != 2.0f)
        {
            this.font = this.font.deriveFont(this.fontSize);
        }

        this.animationImpl = new AnimationImpl(this, simulator);
    }

    /**
     * Construct a new TextAnimation without contrast to background protection and no minimum font scale.
     * @param source Locatable; the object for which the text is displayed
     * @param text String; the text to display
     * @param dx float; the horizontal movement of the text, in meters
     * @param dy float; the vertical movement of the text, in meters
     * @param textAlignment TextAlignment; where to place the text
     * @param color Color; the color of the text
     * @param fontSize float; the size of the font; default = 2.0 (meters)
     * @param minFontSize float; minimum font size resulting from scaling
     * @param maxFontSize float; maximum font size resulting from scaling
     * @param simulator OtsSimulatorInterface; the simulator
     * @param scaleDependentRendering ScaleDependentRendering; render text only when bigger than minimum scale
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TextAnimation(final Locatable source, final String text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final float fontSize, final float minFontSize,
            final float maxFontSize, final OtsSimulatorInterface simulator,
            final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
    {
        this(source, text, dx, dy, textAlignment, color, fontSize, minFontSize, maxFontSize, simulator, null,
                scaleDependentRendering);
    }

    /**
     * @param source Locatable; the object for which the text is displayed
     * @param text String; the text to display
     * @param dx float; the horizontal movement of the text, in meters
     * @param dy float; the vertical movement of the text, in meters
     * @param textAlignment TextAlignment; where to place the text
     * @param color Color; the color of the text
     * @param simulator OtsSimulatorInterface; the simulator
     * @param scaleDependentRendering ScaleDependentRendering; render text only when bigger than minimum scale
     * @throws NamingException when animation context cannot be created or retrieved
     * @throws RemoteException when remote context cannot be found
     */
    public TextAnimation(final Locatable source, final String text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final OtsSimulatorInterface simulator,
            final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
    {
        this(source, text, dx, dy, textAlignment, color, 2.0f, 12.0f, 50f, simulator, scaleDependentRendering);
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        // draw always on top.
        try
        {
            Point<?> p = this.source.getLocation();
            return new DirectedPoint(p.getX(), p.getY(), Double.MAX_VALUE, 0.0, 0.0,
                    p instanceof Oriented ? ((Oriented<?>) p).getDirZ() : 0.0);
        }
        catch (RemoteException exception)
        {
            CategoryLogger.always().warn(exception);
            return new DirectedPoint(0, 0, 0);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new Bounds(0.0, 0.0, 0.0);
    }

    /**
     * paint() method so it can be overridden or extended.
     * @param graphics Graphics2D; the graphics object
     * @param observer ImageObserver; the observer
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        double scale = Math.sqrt(graphics.getTransform().getDeterminant());
        Rectangle2D scaledFontRectangle;
        synchronized (this.font)
        {
            if (!this.scaleDependentRendering.isRendered(scale))
            {
                return;
            }
            if (scale < this.minFontSize / this.fontSize)
            {
                graphics.setFont(this.font.deriveFont((float) (this.minFontSize / scale)));
                FontMetrics fm = graphics.getFontMetrics();
                scaledFontRectangle = fm.getStringBounds(this.text, graphics);
            }
            else if (scale > this.maxFontSize / this.fontSize)
            {
                graphics.setFont(this.font.deriveFont((float) (this.maxFontSize / scale)));
                FontMetrics fm = graphics.getFontMetrics();
                scaledFontRectangle = fm.getStringBounds(this.text, graphics);
            }
            else
            {
                graphics.setFont(this.font);
                if (this.fontRectangle == null)
                {
                    FontMetrics fm = graphics.getFontMetrics();
                    this.fontRectangle = fm.getStringBounds(this.text, graphics);
                }
                scaledFontRectangle = this.fontRectangle;
            }
            Color useColor = this.color;
            if (null != this.background && useColor.equals(this.background.getBackgroundColor()))
            {
                // Construct an alternative color
                if (Color.BLACK.equals(useColor))
                {
                    useColor = Color.WHITE;
                }
                else
                {
                    useColor = Color.BLACK;
                }
            }
            graphics.setColor(useColor);
            float dxText =
                    this.textAlignment.equals(TextAlignment.LEFT) ? 0.0f : this.textAlignment.equals(TextAlignment.CENTER)
                            ? (float) -scaledFontRectangle.getWidth() / 2.0f : (float) -scaledFontRectangle.getWidth();
            graphics.drawString(this.text, dxText + this.dx, -this.dy);
        }
    }

    /**
     * Destroy the text animation.
     * @param contextProvider Contextualized; the object with a Context
     */
    public final void destroy(final Contextualized contextProvider)
    {
        this.animationImpl.destroy(contextProvider);
    }

    /**
     * Retrieve the source.
     * @return Locatable; the source
     */
    protected final Locatable getSource()
    {
        return this.source;
    }

    /**
     * Retrieve dx.
     * @return float; the value of dx
     */
    protected final float getDx()
    {
        return this.dx;
    }

    /**
     * Retrieve dy.
     * @return float; the value of dy
     */
    protected final float getDy()
    {
        return this.dy;
    }

    /**
     * Sets a new offset.
     * @param x float; dx
     * @param y float; dy
     */
    protected final void setXY(final float x, final float y)
    {
        this.dx = x;
        this.dy = y;
    }

    /**
     * Retrieve the text alignment.
     * @return TextAlignment; the text alignment
     */
    protected final TextAlignment getTextAlignment()
    {
        return this.textAlignment;
    }

    /**
     * Retrieve the font size.
     * @return float; the font size
     */
    protected final float getFontSize()
    {
        return this.fontSize;
    }

    /**
     * Retrieve the font.
     * @return Font; the font
     */
    protected final Font getFont()
    {
        return this.font;
    }

    /**
     * Retrieve the current text.
     * @return String; the current text
     */
    protected final String getText()
    {
        return this.text;
    }

    /**
     * Update the text.
     * @param text String; the new text
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
     * Retrieve the current color.
     * @return Color; the current color
     */
    protected final Color getColor()
    {
        return this.color;
    }

    /**
     * Update the color.
     * @param color Color; the new color
     */
    protected final void setColor(final Color color)
    {
        this.color = color;
    }

    /**
     * Retrieve the current flip status.
     * @return boolean; the current flip status
     */
    public final boolean isFlip()
    {
        return this.animationImpl.isFlip();
    }

    /**
     * Update the flip status.
     * @param flip boolean; the new flip status
     */
    public final void setFlip(final boolean flip)
    {
        this.animationImpl.setFlip(flip);
    }

    /**
     * Retrieve the current rotation status.
     * @return boolean; the current rotation status
     */
    public final boolean isRotate()
    {
        return this.animationImpl.isRotate();
    }

    /**
     * Update the rotation status.
     * @param rotate boolean; the new rotation status
     */
    public final void setRotate(final boolean rotate)
    {
        this.animationImpl.setRotate(rotate);

    }

    /**
     * Retrieve the current scale status.
     * @return boolean; the current scale status
     */
    public final boolean isScale()
    {
        return this.animationImpl.isScale();
    }

    /**
     * Update the scale status.
     * @param scale boolean; the new scale status
     */
    public final void setScale(final boolean scale)
    {
        this.animationImpl.setScale(scale);
    }

    /**
     * Retrieve the current translate status.
     * @return boolean; the current translate status
     */
    public final boolean isTranslate()
    {
        return this.animationImpl.isTranslate();
    }

    /**
     * Update the translate status.
     * @param translate boolean; the new translate status
     */
    public final void setTranslate(final boolean translate)
    {
        this.animationImpl.setTranslate(translate);
    }

    /**
     * The implementation of the text animation. Cloning will be taken care of by the overarching TextAnimation-derived class.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private static class AnimationImpl extends Renderable2D<Locatable> implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

        /**
         * Construct a new AnimationImpl.
         * @param source Locatable; the source
         * @param simulator OtsSimulatorInterface; the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException when remote context cannot be found
         */
        AnimationImpl(final Locatable source, final OtsSimulatorInterface simulator) throws NamingException, RemoteException
        {
            super(source, simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer)
        {
            TextAnimation ta = ((TextAnimation) getSource());
            ta.paint(graphics, observer);
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(final Point2d pointWorldCoordinates, final Bounds2d extent)
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TextAnimation.AnimationImpl []";
        }

    }

    /**
     * Retrieve the scale dependent rendering qualifier (used in cloning).
     * @return ScaleDependentRendering; the rendering qualifier of this TextAnimation
     */
    protected ScaleDependentRendering getScaleDependentRendering()
    {
        return this.scaleDependentRendering;
    }

    /**
     * Interface to obtain the color of the background.
     */
    public interface ContrastToBackground
    {
        /**
         * Retrieve the color of the background.
         * @return Color; the (current) color of the background
         */
        Color getBackgroundColor();
    }

    /**
     * Determine if a Feature object should be rendered.
     */
    public interface ScaleDependentRendering
    {
        /**
         * Determine if a Text should be rendered, depending on the scale.
         * @param scale double; the current font scale
         * @return boolean; true if the text should be rendered at the scale; false if the text should not be rendered at the
         *         scale
         */
        boolean isRendered(double scale);
    }

    /** Always render the Text. */
    public static final ScaleDependentRendering RENDERALWAYS = new ScaleDependentRendering()
    {

        @Override
        public boolean isRendered(final double scale)
        {
            return true;
        }
    };

    /** Don't render texts when smaller than 1. */
    public static final ScaleDependentRendering RENDERWHEN1 = new ScaleDependentRendering()
    {

        @Override
        public boolean isRendered(final double scale)
        {
            return scale >= 1.0;
        }
    };

    /** Don't render texts when smaller than 2. */
    public static final ScaleDependentRendering RENDERWHEN10 = new ScaleDependentRendering()
    {

        @Override
        public boolean isRendered(final double scale)
        {
            return scale >= 0.1;
        }
    };

    /** Don't render texts when smaller than 2. */
    public static final ScaleDependentRendering RENDERWHEN100 = new ScaleDependentRendering()
    {

        @Override
        public boolean isRendered(final double scale)
        {
            return scale >= 0.01;
        }
    };

}
