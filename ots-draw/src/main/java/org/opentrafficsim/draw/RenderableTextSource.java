package org.opentrafficsim.draw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.ImageObserver;
import java.util.function.Supplier;

import org.djutils.draw.Directed;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsShape;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Display a text for another Locatable object.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <L> locatable type
 * @param <T> text animation type
 */
public abstract class RenderableTextSource<L extends OtsShape, T extends RenderableTextSource<L, T>> implements OtsShape
{
    /** The source. */
    private final L source;

    /** The text to display. */
    private Supplier<String> text;

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
    private final RenderableText animationImpl;

    /** The font. */
    private Font font;

    /** Access to the current background color. */
    private final ContrastToBackground background;

    /** Render dependent on font scale. */
    private final ScaleDependentRendering scaleDependentRendering;

    /** Whether the location is dynamic. */
    private boolean dynamic = false;

    /** Location of this text. */
    private DirectedPoint2d location;

    /**
     * Construct a new TextAnimation.
     * @param source the object for which the text is displayed
     * @param text the text to display
     * @param dx the horizontal movement of the text, in meters
     * @param dy the vertical movement of the text, in meters
     * @param textAlignment where to place the text
     * @param color the color of the text
     * @param fontSize the size of the font; default = 2.0 (meters)
     * @param minFontSize minimum font size resulting from scaling
     * @param maxFontSize maximum font size resulting from scaling
     * @param contextualized context provider.
     * @param background allows querying the background color and adaptation of the actual color of the text to ensure contrast
     * @param scaleDependentRendering suppress rendering when font scale is too small
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public RenderableTextSource(final L source, final Supplier<String> text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final float fontSize, final float minFontSize,
            final float maxFontSize, final Contextualized contextualized, final ContrastToBackground background,
            final ScaleDependentRendering scaleDependentRendering)
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

        this.animationImpl = new RenderableText(this, contextualized);
        setScaleY(false);
    }

    /**
     * Construct a new TextAnimation without contrast to background protection and no minimum font scale.
     * @param source the object for which the text is displayed
     * @param text the text to display
     * @param dx the horizontal movement of the text, in meters
     * @param dy the vertical movement of the text, in meters
     * @param textAlignment where to place the text
     * @param color the color of the text
     * @param fontSize the size of the font; default = 2.0 (meters)
     * @param minFontSize minimum font size resulting from scaling
     * @param maxFontSize maximum font size resulting from scaling
     * @param contextualized context provider
     * @param scaleDependentRendering render text only when bigger than minimum scale
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public RenderableTextSource(final L source, final Supplier<String> text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final float fontSize, final float minFontSize,
            final float maxFontSize, final Contextualized contextualized, final ScaleDependentRendering scaleDependentRendering)
    {
        this(source, text, dx, dy, textAlignment, color, fontSize, minFontSize, maxFontSize, contextualized, null,
                scaleDependentRendering);
    }

    /**
     * Constructor.
     * @param source the object for which the text is displayed
     * @param text the text to display
     * @param dx the horizontal movement of the text, in meters
     * @param dy the vertical movement of the text, in meters
     * @param textAlignment where to place the text
     * @param color the color of the text
     * @param contextualized context provider
     * @param scaleDependentRendering render text only when bigger than minimum scale
     */
    public RenderableTextSource(final L source, final Supplier<String> text, final float dx, final float dy,
            final TextAlignment textAlignment, final Color color, final Contextualized contextualized,
            final ScaleDependentRendering scaleDependentRendering)
    {
        this(source, text, dx, dy, textAlignment, color, 2.0f, 12.0f, 50f, contextualized, scaleDependentRendering);
    }

    /**
     * Sets whether the location of this text is dynamic.
     * @param dynamic whether the location of this text is dynamic.
     * @return for method chaining.
     */
    @SuppressWarnings("all")
    public T setDynamic(final boolean dynamic)
    {
        this.dynamic = dynamic;
        return (T) this;
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        if (this.location == null || this.dynamic)
        {
            Point2d p = getSource().getLocation();
            if (isRotate() && p instanceof Directed dir)
            {
                // draw not upside down.
                double a = Angle.normalizePi(dir.getDirZ());
                if (a > Math.PI / 2.0 || a < -0.99 * Math.PI / 2.0)
                {
                    a += Math.PI;
                }
                this.location = new DirectedPoint2d(p, a);
            }
            else
            {
                this.location = new DirectedPoint2d(p, 0.0);
            }
        }
        return this.location;
    }

    /**
     * Returns the source.
     * @return the source
     */
    public L getSource()
    {
        return this.source;
    }

    @Override
    public final Bounds2d getRelativeBounds()
    {
        return new Bounds2d(2.0, 2.0);
    }

    @Override
    public Polygon2d getAbsoluteContour()
    {
        return getSource().getAbsoluteContour();
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return getSource().getRelativeContour();
    }

    /**
     * paint() method so it can be overridden or extended.
     * @param graphics the graphics object
     * @param observer the observer
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        double scale = Math.sqrt(graphics.getTransform().getDeterminant());
        Rectangle2D scaledFontRectangle;
        String str = this.text.get();
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
                scaledFontRectangle = fm.getStringBounds(str, graphics);
            }
            else if (scale > this.maxFontSize / this.fontSize)
            {
                graphics.setFont(this.font.deriveFont((float) (this.maxFontSize / scale)));
                FontMetrics fm = graphics.getFontMetrics();
                scaledFontRectangle = fm.getStringBounds(str, graphics);
            }
            else
            {
                graphics.setFont(this.font);
                FontMetrics fm = graphics.getFontMetrics();
                scaledFontRectangle = fm.getStringBounds(str, graphics);
            }
            Color useColor = this.color;
            if (null != this.background && isSimilar(useColor, this.background.getBackgroundColor()))
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

            float dxText =
                    this.textAlignment.equals(TextAlignment.LEFT) ? 0.0f : this.textAlignment.equals(TextAlignment.CENTER)
                            ? (float) -scaledFontRectangle.getWidth() / 2.0f : (float) -scaledFontRectangle.getWidth();
            Object antialias = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (null != this.background)
            {
                // Draw transparent rectangle with background color to makes sure all of the text is visible, even when it is
                // drawn outside of the bounds of the object that supplies the background color, or on parts of the object that
                // have a different color (e.g. driver dot, brake lights, etc.).
                double r = scaledFontRectangle.getHeight() / 2.0; // rounding
                double dh = scaledFontRectangle.getHeight() / 5.0; // baseline shift
                Shape s = new RoundRectangle2D.Double(this.dx - scaledFontRectangle.getWidth() - dxText,
                        this.dy + dh - scaledFontRectangle.getHeight(), scaledFontRectangle.getWidth(),
                        scaledFontRectangle.getHeight(), r, r);
                Color bg = this.background.getBackgroundColor();
                graphics.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 92));
                graphics.fill(s);
            }
            graphics.setColor(useColor);
            graphics.drawString(str, dxText + this.dx, -this.dy);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias);
        }
    }

    /**
     * Returns whether two colors are similar.
     * @param color1 color 1.
     * @param color2 color 2.
     * @return whether two colors are similar.
     */
    private boolean isSimilar(final Color color1, final Color color2)
    {
        int r = color1.getRed() - color2.getRed();
        int g = color1.getGreen() - color2.getGreen();
        int b = color1.getBlue() - color2.getBlue();
        return r * r + g * g + b * b < 2000;
        // this threshold may need to be tweaked, it used to be color.equals(color) which is too narrow
    }

    /**
     * Destroy the text animation.
     * @param contextProvider the object with a Context
     */
    public final void destroy(final Contextualized contextProvider)
    {
        this.animationImpl.destroy(contextProvider);
    }

    /**
     * Retrieve dx.
     * @return the value of dx
     */
    protected final float getDx()
    {
        return this.dx;
    }

    /**
     * Retrieve dy.
     * @return the value of dy
     */
    protected final float getDy()
    {
        return this.dy;
    }

    /**
     * Sets a new offset.
     * @param x dx
     * @param y dy
     */
    protected final void setXY(final float x, final float y)
    {
        this.dx = x;
        this.dy = y;
    }

    @Override
    public double getZ()
    {
        return DrawLevel.LABEL.getZ();
    }

    /**
     * Retrieve the text alignment.
     * @return the text alignment
     */
    protected TextAlignment getTextAlignment()
    {
        return this.textAlignment;
    }

    /**
     * Retrieve the font size.
     * @return the font size
     */
    protected float getFontSize()
    {
        return this.fontSize;
    }

    /**
     * Retrieve the font.
     * @return the font
     */
    protected Font getFont()
    {
        return this.font;
    }

    /**
     * Retrieve the current text.
     * @return the current text
     */
    protected String getText()
    {
        return this.text.get();
    }

    /**
     * Update the text.
     * @param text the new text
     */
    public void setText(final Supplier<String> text)
    {
        this.text = text;
    }

    /**
     * Retrieve the current color.
     * @return the current color
     */
    protected Color getColor()
    {
        return this.color;
    }

    /**
     * Update the color.
     * @param color the new color
     */
    protected void setColor(final Color color)
    {
        this.color = color;
    }

    /**
     * Retrieve the current flip status.
     * @return the current flip status
     */
    public boolean isFlip()
    {
        return this.animationImpl.isFlip();
    }

    /**
     * Update the flip status.
     * @param flip the new flip status
     */
    public void setFlip(final boolean flip)
    {
        this.animationImpl.setFlip(flip);
    }

    /**
     * Retrieve the current rotation status.
     * @return the current rotation status
     */
    public boolean isRotate()
    {
        return this.animationImpl.isRotate();
    }

    /**
     * Update the rotation status.
     * @param rotate the new rotation status
     */
    public void setRotate(final boolean rotate)
    {
        this.animationImpl.setRotate(rotate);

    }

    /**
     * Retrieve the current scale status.
     * @return the current scale status
     */
    public boolean isScale()
    {
        return this.animationImpl.isScale();
    }

    /**
     * Update the scale status.
     * @param scale the new scale status
     */
    public void setScale(final boolean scale)
    {
        this.animationImpl.setScale(scale);
    }

    /**
     * Retrieve the current translate status.
     * @return the current translate status
     */
    public boolean isTranslate()
    {
        return this.animationImpl.isTranslate();
    }

    /**
     * Update the translate status.
     * @param translate the new translate status
     */
    public void setTranslate(final boolean translate)
    {
        this.animationImpl.setTranslate(translate);
    }

    /**
     * Return whether to scale the renderable in the Y-direction when there is a compressed Y-axis or not.
     * @return whether to scale the renderable in the Y-direction when there is a compressed Y-axis or not
     */
    public boolean isScaleY()
    {
        return this.animationImpl.isScaleY();
    }

    /**
     * Set whether to scale the renderable in the Y-direction when there is a compressed Y-axis or not.
     * @param scaleY whether to scale the renderable in the Y-direction when there is a compressed Y-axis or not
     */
    public void setScaleY(final boolean scaleY)
    {
        this.animationImpl.setScaleY(scaleY);
    }

    /**
     * Return whether to scale the renderable in the X/Y-direction with the value of RenderableScale.objectScaleFactor or not.
     * @return whether to scale the renderable in the X/Y-direction with the value of RenderableScale.objectScaleFactor or not
     */
    public boolean isScaleObject()
    {
        return this.animationImpl.isScaleObject();
    }

    /**
     * Set whether to scale the renderable in the X/Y-direction with the value of RenderableScale.objectScaleFactor or not.
     * @param scaleObject whether to scale the renderable in the X/Y-direction with the value of
     *            RenderableScale.objectScaleFactor or not
     */
    public void setScaleObject(final boolean scaleObject)
    {
        this.animationImpl.setScaleObject(scaleObject);
    }

    /**
     * Renderable text.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private static class RenderableText extends Renderable2d<RenderableTextSource<?, ?>>
    {
        /**
         * Construct a new AnimationImpl.
         * @param source the source
         * @param contextualized context provider.
         */
        RenderableText(final RenderableTextSource<?, ?> source, final Contextualized contextualized)
        {
            super(source, contextualized);
        }

        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer)
        {
            getSource().paint(graphics, observer);
        }

        @Override
        public boolean contains(final Point2D pointScreenCoordinates, final Bounds2d extent, final Dimension screenSize,
                final RenderableScale scale, final double worldMargin, final double pixelMargin)
        {
            return false;
        }

        @Override
        public final String toString()
        {
            return "RenderableText []";
        }
    }

    /**
     * Retrieve the scale dependent rendering qualifier (used in cloning).
     * @return the rendering qualifier of this TextAnimation
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
         * @return the (current) color of the background
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
         * @param scale the current font scale
         * @return true if the text should be rendered at the scale; false if the text should not be rendered at the scale
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
