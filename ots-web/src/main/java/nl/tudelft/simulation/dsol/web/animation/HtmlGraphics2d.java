package nl.tudelft.simulation.dsol.web.animation;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.animation.gis.SerializablePath;
import nl.tudelft.simulation.dsol.logger.Cat;

/**
 * HTMLGraphics.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HtmlGraphics2d extends Graphics2D
{
    /** the current color of the background for drawing. */
    Color background = Color.WHITE;

    /** the current drawing color. */
    Color color = Color.BLACK;

    /** the current font. */
    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

    /** the drawing canvas. */
    Canvas canvas = new Canvas();

    /** the cached current font properties. */
    FontMetrics fontMetrics = this.canvas.getFontMetrics(this.font);

    /** the current paint. */
    Paint paint = Color.BLACK;

    /** the current stroke. */
    Stroke stroke = new BasicStroke();

    /** TODO: the current rendering hints. */
    RenderingHints renderingHints = new RenderingHints(new LinkedHashMap<Key, Object>());

    /** the current affine transform. */
    AffineTransform affineTransform = new AffineTransform();

    /** TODO: the current composite. What is that? */
    Composite composite = AlphaComposite.Clear;

    /** the commands to send over the channel to the HTML5 code. */
    StringBuffer commands = new StringBuffer();

    /**
     * Clear the commands and put the start tag in.
     */
    public void clearCommand()
    {
        this.commands = new StringBuffer();
        this.commands.append("<animate>\n");
    }

    /**
     * Close the commands and put the end tag in.
     * @return the current set of commands
     */
    public String closeAndGetCommands()
    {
        this.commands.append("</animate>\n");
        return this.commands.toString();
    }

    /**
     * Add a draw command.
     * @param drawCommand String; the tag for the draw command
     * @param params Object...; the params for the draw command
     */
    protected void addDraw(final String drawCommand, final Object... params)
    {
        this.commands.append("<draw>" + drawCommand);
        for (Object param : params)
        {
            this.commands.append("," + param.toString());
        }
        this.commands.append("</draw>\n");
    }

    /**
     * add AffineTransform to the command.
     */
    protected void addAffineTransform()
    {
        this.commands.append(",");
        this.commands.append(this.affineTransform.getScaleX());
        this.commands.append(",");
        this.commands.append(this.affineTransform.getShearY());
        this.commands.append(",");
        this.commands.append(this.affineTransform.getShearX());
        this.commands.append(",");
        this.commands.append(this.affineTransform.getScaleY());
        this.commands.append(",");
        this.commands.append(this.affineTransform.getTranslateX());
        this.commands.append(",");
        this.commands.append(this.affineTransform.getTranslateY());
    }

    /**
     * add Color to the command.
     * @param c Color; the color
     */
    protected void addColor(final Color c)
    {
        this.commands.append(",");
        this.commands.append(c.getRed());
        this.commands.append(",");
        this.commands.append(c.getGreen());
        this.commands.append(",");
        this.commands.append(c.getBlue());
        this.commands.append(",");
        this.commands.append(c.getAlpha());
        this.commands.append(",");
        this.commands.append(c.getTransparency());
    }

    /**
     * add font data to the command, font-name, font-size, bold/italic/plain.
     */
    protected void addFontData()
    {
        this.commands.append(",");
        String javaFontName = this.font.getFontName().toLowerCase();
        String htmlFontName;
        if (javaFontName.contains("arial") || javaFontName.contains("helvetica") || javaFontName.contains("verdana")
                || javaFontName.contains("tahoma") || javaFontName.contains("segoe") || javaFontName.contains("sans"))
            htmlFontName = "sans-serif";
        else if (javaFontName.contains("times") || javaFontName.contains("cambria") || javaFontName.contains("georgia")
                || javaFontName.contains("serif"))
            htmlFontName = "serif";
        else if (javaFontName.contains("courier") || javaFontName.contains("consol") || javaFontName.contains("mono"))
            htmlFontName = "monospace";
        else
            htmlFontName = "sans-serif";
        this.commands.append(htmlFontName);
        this.commands.append(",");
        this.commands.append(this.font.getSize2D());
        this.commands.append(",");
        if (this.font.isBold())
            this.commands.append("bold");
        else if (this.font.isItalic())
            this.commands.append("italic");
        else
            this.commands.append("plain");
    }

    /**
     * Add fill command, transform.m11(h-scale), transform.m12(h-skew), transform.m21(v-skew), transform.m22(v-scale),
     * transform.dx(h-translate), transform.dy(v-translate), color.r, color.g, color.b, color.alpha, color.transparency,
     * params...
     * @param fillCommand String; the tag to use
     * @param params Object...; the params to send
     */
    protected void addTransformFill(final String fillCommand, final Object... params)
    {
        this.commands.append("<transformFill>" + fillCommand);
        addAffineTransform();
        if (this.paint instanceof Color)
            addColor((Color) this.paint);
        else
            addColor(this.color);
        for (Object param : params)
        {
            this.commands.append("," + param.toString());
        }
        this.commands.append("</transformFill>\n");
    }

    /**
     * Add command, transform.m11(h-scale), transform.m12(h-skew), transform.m21(v-skew), transform.m22(v-scale),
     * transform.dx(h-translate), transform.dy(v-translate), linecolor.r, linecolor.g, linecolor.b, linecolor.alpha,
     * linecolor.transparency, line-width, params...
     * @param drawCommand String; the tag to use
     * @param params Object...; the params
     */
    protected void addTransformDraw(final String drawCommand, final Object... params)
    {
        this.commands.append("<transformDraw>" + drawCommand);
        addAffineTransform();
        if (this.paint instanceof Color)
            addColor((Color) this.paint);
        else
            addColor(this.color);
        if (this.stroke instanceof BasicStroke)
            this.commands.append("," + ((BasicStroke) this.stroke).getLineWidth());
        else
            this.commands.append(", 0.1");
        for (Object param : params)
        {
            this.commands.append("," + param.toString());
        }
        this.commands.append("</transformDraw>\n");
    }

    /**
     * adds a float array to the command.
     * @param array float[]; the array
     * @param length int; the number of points from the array to write
     */
    private void addFloatArray(final float[] array, final int length)
    {
        for (int i = 0; i < length; i++)
        {
            this.commands.append(", " + array[i]);
        }
    }

    /**
     * adds a double array to the command.
     * @param array double[]; the array
     * @param length int; the number of points from the array to write
     */
    private void addDoubleArray(final double[] array, final int length)
    {
        for (int i = 0; i < length; i++)
        {
            this.commands.append(", " + array[i]);
        }
    }

    /**
     * Add a path2D to the command. In case of fill:<br>
     * FILL, transform.m11(h-scale), transform.m12(h-skew), transform.m21(v-skew), transform.m22(v-scale),
     * transform.dx(h-translate), transform.dy(v-translate), fillcolor.r, fillcolor.g, fillcolor.b, fillcolor.alpha,
     * fillcolor.transparency, winding_rule[WIND_EVEN_ODD/WIND_NON_ZERO], COMMAND, coords, COMMAND, coords, ... <br>
     * In case of draw:<br>
     * DRAW, transform.m11(h-scale), transform.m12(h-skew), transform.m21(v-skew), transform.m22(v-scale),
     * transform.dx(h-translate), transform.dy(v-translate), strokecolor.r, strokecolor.g, strokecolor.b, strokecolor.alpha,
     * strokecolor.transparency, line_width, COMMAND, coords, COMMAND, coords, ... <br>
     * where command can be one of the following:<br>
     * - CLOSE, followed by no coordinates<br>
     * - CUBICTO, followed by 3 coordinates (6 numbers)<br>
     * - LINETO, followed by 1 coordinate (2 numbers)<br>
     * - MOVETO, followed by 1 coordinate (2 numbers)<br>
     * - QUADTO, followed by 2 coordinates (4 numbers)<br>
     * @param path Path2D.Float; the path to draw
     * @param fill boolean;
     */
    protected void addTransformPathFloat(final Path2D.Float path, final boolean fill)
    {
        if (fill)
            this.commands.append("<transformPath>FILL");
        else
            this.commands.append("<transformPath>DRAW");
        addAffineTransform();
        addColor(this.color);
        if (fill)
        {
            if (path.getWindingRule() == Path2D.WIND_EVEN_ODD)
                this.commands.append(",WIND_EVEN_ODD");
            else
                this.commands.append(",WIND_NON_ZERO");
        }
        else
        {
            if (this.stroke instanceof BasicStroke)
                this.commands.append("," + ((BasicStroke) this.stroke).getLineWidth());
            else
                this.commands.append(", 0.1");
        }
        float[] coords = new float[6];
        PathIterator i = path.getPathIterator(null);
        while (!i.isDone())
        {
            int segment = i.currentSegment(coords);
            switch (segment)
            {
                case PathIterator.SEG_CLOSE:
                    this.commands.append(",CLOSE");
                    break;
                case PathIterator.SEG_CUBICTO:
                    this.commands.append(",CUBICTO");
                    addFloatArray(coords, 6);
                    break;
                case PathIterator.SEG_LINETO:
                    this.commands.append(",LINETO");
                    addFloatArray(coords, 2);
                    break;
                case PathIterator.SEG_MOVETO:
                    this.commands.append(",MOVETO");
                    addFloatArray(coords, 2);
                    break;
                case PathIterator.SEG_QUADTO:
                    this.commands.append(",QUADTO");
                    addFloatArray(coords, 4);
                    break;
                default:
                    throw new RuntimeException("unkown segment");
            }
            i.next();
        }
        this.commands.append("</transformPath>\n");
    }

    /**
     * Add a path2D to the command. In case of fill:<br>
     * FILL, transform.m11(h-scale), transform.m12(h-skew), transform.m21(v-skew), transform.m22(v-scale),
     * transform.dx(h-translate), transform.dy(v-translate), fillcolor.r, fillcolor.g, fillcolor.b, fillcolor.alpha,
     * fillcolor.transparency, winding_rule[WIND_EVEN_ODD/WIND_NON_ZERO], COMMAND, coords, COMMAND, coords, ... <br>
     * In case of draw:<br>
     * DRAW, transform.m11(h-scale), transform.m12(h-skew), transform.m21(v-skew), transform.m22(v-scale),
     * transform.dx(h-translate), transform.dy(v-translate), strokecolor.r, strokecolor.g, strokecolor.b, strokecolor.alpha,
     * strokecolor.transparency, line_width, COMMAND, coords, COMMAND, coords, ... <br>
     * where command can be one of the following:<br>
     * - CLOSE, followed by no coordinates<br>
     * - CUBICTO, followed by 3 coordinates (6 numbers)<br>
     * - LINETO, followed by 1 coordinate (2 numbers)<br>
     * - MOVETO, followed by 1 coordinate (2 numbers)<br>
     * - QUADTO, followed by 2 coordinates (4 numbers)<br>
     * @param path Path2D.Double; the path to draw
     * @param fill boolean;
     */
    protected void addTransformPathDouble(final Path2D.Double path, final boolean fill)
    {
        if (fill)
            this.commands.append("<transformPath>FILL");
        else
            this.commands.append("<transformPath>DRAW");
        addAffineTransform();
        addColor(this.color);
        if (fill)
        {
            if (path.getWindingRule() == Path2D.WIND_EVEN_ODD)
                this.commands.append(",WIND_EVEN_ODD");
            else
                this.commands.append(",WIND_NON_ZERO");
        }
        else
        {
            if (this.stroke instanceof BasicStroke)
                this.commands.append("," + ((BasicStroke) this.stroke).getLineWidth());
            else
                this.commands.append(", 0.1");
        }
        double[] coords = new double[6];
        PathIterator i = path.getPathIterator(null);
        while (!i.isDone())
        {
            int segment = i.currentSegment(coords);
            switch (segment)
            {
                case PathIterator.SEG_CLOSE:
                    this.commands.append(",CLOSE");
                    break;
                case PathIterator.SEG_CUBICTO:
                    this.commands.append(",CUBICTO");
                    addDoubleArray(coords, 6);
                    break;
                case PathIterator.SEG_LINETO:
                    this.commands.append(",LINETO");
                    addDoubleArray(coords, 2);
                    break;
                case PathIterator.SEG_MOVETO:
                    this.commands.append(",MOVETO");
                    addDoubleArray(coords, 2);
                    break;
                case PathIterator.SEG_QUADTO:
                    this.commands.append(",QUADTO");
                    addDoubleArray(coords, 4);
                    break;
                default:
                    throw new RuntimeException("unkown segment");
            }
            i.next();
        }
        this.commands.append("</transformPath>\n");
    }

    /**
     * Add string, 0=command, 1=transform.m11(h-scale), 2=transform.m12(h-skew), 3=transform.m21(v-skew),
     * 4=transform.m22(v-scale), 5=transform.dx(h-translate), 6=transform.dy(v-translate), 7=color.r, 8=color.g, 9=color.b,
     * 10=color.alpha, 11=color.transparency, 12=fontname, 13=fontsize, 14=fontstyle(normal/italic/bold), 15=x, 16=y, 17=text.
     * @param drawCommand String; the tag to use
     * @param params Object...; the params
     */
    protected void addTransformText(final String drawCommand, final Object... params)
    {
        this.commands.append("<transformText>" + drawCommand);
        addAffineTransform();
        addColor(this.color);
        addFontData();
        for (Object param : params)
        {
            this.commands.append("," + param.toString());
        }
        this.commands.append("</transformText>\n");
    }

    /** {@inheritDoc} */
    @Override
    public void draw(final Shape shape)
    {
        drawFillShape(shape, false);
    }

    /**
     * Draw or fill a shape.
     * @param shape Shape; the shape
     * @param fill boolean; filled or not
     */
    protected void drawFillShape(final Shape shape, final boolean fill)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.draw(shape: {})", shape.getClass().getSimpleName());
        if (shape instanceof Ellipse2D.Double)
        {
            Ellipse2D.Double ellipse = (Ellipse2D.Double) shape;
            if (fill)
                addTransformFill("fillOval", ellipse.getCenterX(), ellipse.getCenterY(), ellipse.width / 2.0,
                        ellipse.height / 2.0);
            else
                addTransformDraw("drawOval", ellipse.getCenterX(), ellipse.getCenterY(), ellipse.width / 2.0,
                        ellipse.height / 2.0);
        }
        else if (shape instanceof Ellipse2D.Float)
        {
            Ellipse2D.Float ellipse = (Ellipse2D.Float) shape;
            if (fill)
                addTransformFill("fillOval", ellipse.getCenterX(), ellipse.getCenterY(), ellipse.width / 2.0,
                        ellipse.height / 2.0);
            else
                addTransformDraw("drawOval", ellipse.getCenterX(), ellipse.getCenterY(), ellipse.width / 2.0,
                        ellipse.height / 2.0);
        }
        else if (shape instanceof Line2D.Double)
        {
            Line2D.Double line = (Line2D.Double) shape;
            addTransformDraw("drawLine", line.x1, line.y1, line.x2, line.y2);
        }
        else if (shape instanceof Line2D.Float)
        {
            Line2D.Float line = (Line2D.Float) shape;
            addTransformDraw("drawLine", line.x1, line.y1, line.x2, line.y2);
        }
        else if (shape instanceof Rectangle2D.Double)
        {
            Rectangle2D.Double rect = (Rectangle2D.Double) shape;
            if (fill)
                addTransformFill("fillRect", rect.x, rect.y, rect.width, rect.height);
            else
                addTransformDraw("drawRect", rect.x, rect.y, rect.width, rect.height);
        }
        else if (shape instanceof Rectangle2D.Float)
        {
            Rectangle2D.Float rect = (Rectangle2D.Float) shape;
            if (fill)
                addTransformFill("fillRect", rect.x, rect.y, rect.width, rect.height);
            else
                addTransformDraw("drawRect", rect.x, rect.y, rect.width, rect.height);
        }
        else if (shape instanceof SerializablePath)
        {
            Path2D.Float path = (Path2D.Float) shape;
            addTransformPathFloat(path, fill);
        }
        else if (shape instanceof Path2D.Float)
        {
            Path2D.Float path = (Path2D.Float) shape;
            addTransformPathFloat(path, fill);
        }
        else if (shape instanceof Path2D.Double)
        {
            Path2D.Double path = (Path2D.Double) shape;
            addTransformPathDouble(path, fill);
        }

    }

    /** {@inheritDoc} */
    @Override
    public boolean drawImage(final Image img, final AffineTransform xform, final ImageObserver obs)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void drawImage(final BufferedImage img, final BufferedImageOp op, final int x, final int y)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawRenderedImage(final RenderedImage img, final AffineTransform xform)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawRenderedImage()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawRenderableImage(final RenderableImage img, final AffineTransform xform)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawRenderableImage()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawString(final String str, final int x, final int y)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawString()");
        addTransformText("drawString", x, y, str);
    }

    /** {@inheritDoc} */
    @Override
    public void drawString(final String str, final float x, final float y)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawString()");
        addTransformText("drawString", x, y, str);
    }

    /** {@inheritDoc} */
    @Override
    public void drawString(final AttributedCharacterIterator iterator, final int x, final int y)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawString()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawString(final AttributedCharacterIterator iterator, final float x, final float y)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawString()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawGlyphVector(final GlyphVector g, final float x, final float y)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawGlyphVector()");
    }

    /** {@inheritDoc} */
    @Override
    public void fill(final Shape shape)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.fill()");
        drawFillShape(shape, true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hit(final Rectangle rect, final Shape s, final boolean onStroke)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.hit()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsConfiguration getDeviceConfiguration()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getDeviceConfiguration()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setComposite(final Composite comp)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setComposite()");
    }

    /** {@inheritDoc} */
    @Override
    public void setPaint(final Paint paint)
    {
        this.paint = paint;
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setPaint()");
    }

    /** {@inheritDoc} */
    @Override
    public void setStroke(final Stroke s)
    {
        this.stroke = s;
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setStroke()");
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderingHint(final Key hintKey, final Object hintValue)
    {
        this.renderingHints.put(hintKey, hintValue);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setRenderingHint()");
    }

    /** {@inheritDoc} */
    @Override
    public Object getRenderingHint(final Key hintKey)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getRenderingHint()");
        return this.renderingHints.get(hintKey);
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderingHints(final Map<?, ?> hints)
    {
        this.renderingHints.clear();
        this.renderingHints.putAll(hints);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setRenderingHints()");
    }

    /** {@inheritDoc} */
    @Override
    public void addRenderingHints(final Map<?, ?> hints)
    {
        this.renderingHints.putAll(hints);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.addRenderingHints()");
    }

    /** {@inheritDoc} */
    @Override
    public RenderingHints getRenderingHints()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getRenderingHints()");
        return this.renderingHints;
    }

    /** {@inheritDoc} */
    @Override
    public void translate(final int x, final int y)
    {
        this.affineTransform.translate(x, y);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.translate()");
    }

    /** {@inheritDoc} */
    @Override
    public void translate(final double tx, final double ty)
    {
        this.affineTransform.translate(tx, ty);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.translate()");
    }

    /** {@inheritDoc} */
    @Override
    public void rotate(final double theta)
    {
        this.affineTransform.rotate(theta);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.rotate()");
    }

    /** {@inheritDoc} */
    @Override
    public void rotate(final double theta, final double x, final double y)
    {
        this.affineTransform.rotate(theta, x, y);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.rotate()");
    }

    /** {@inheritDoc} */
    @Override
    public void scale(final double sx, final double sy)
    {
        this.affineTransform.scale(sx, sy);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.scale()");
    }

    /** {@inheritDoc} */
    @Override
    public void shear(final double shx, final double shy)
    {
        this.affineTransform.shear(shx, shy);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.shear()");
    }

    /** {@inheritDoc} */
    @Override
    public void transform(final AffineTransform Tx)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.transform()");
    }

    /** {@inheritDoc} */
    @Override
    public void setTransform(final AffineTransform Tx)
    {
        this.affineTransform = (AffineTransform) Tx.clone();
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setTransform()");
    }

    /** {@inheritDoc} */
    @Override
    public AffineTransform getTransform()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getTransform()");
        return this.affineTransform;
    }

    /** {@inheritDoc} */
    @Override
    public Paint getPaint()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getPaint()");
        return this.paint;
    }

    /** {@inheritDoc} */
    @Override
    public Composite getComposite()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getComposite()");
        return this.composite;
    }

    /** {@inheritDoc} */
    @Override
    public void setBackground(final Color color)
    {
        this.background = color;
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setBackground()");
    }

    /** {@inheritDoc} */
    @Override
    public Color getBackground()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getBackground()");
        return this.background;
    }

    /** {@inheritDoc} */
    @Override
    public Stroke getStroke()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getStroke()");
        return this.stroke;
    }

    /** {@inheritDoc} */
    @Override
    public void clip(final Shape s)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.clip()");
    }

    /** {@inheritDoc} */
    @Override
    public FontRenderContext getFontRenderContext()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getFontRenderContext()");
        return new FontRenderContext(this.affineTransform, true, true);
    }

    /** {@inheritDoc} */
    @Override
    public Graphics create()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.create()");
        return new HtmlGraphics2d(); // TODO: clone
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getColor()");
        return this.color;
    }

    /** {@inheritDoc} */
    @Override
    public void setColor(final Color c)
    {
        this.color = c;
        this.paint = c; // TODO see how difference between paint and color should be handled
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setColor()");
    }

    /** {@inheritDoc} */
    @Override
    public void setPaintMode()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setPaintMode()");
    }

    /** {@inheritDoc} */
    @Override
    public void setXORMode(final Color c1)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setXORMode()");
    }

    /** {@inheritDoc} */
    @Override
    public Font getFont()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getFont()");
        return this.font;
    }

    /** {@inheritDoc} */
    @Override
    public void setFont(final Font font)
    {
        this.font = font;
        this.fontMetrics = this.canvas.getFontMetrics(this.font);
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setFont()");
    }

    /** {@inheritDoc} */
    @Override
    public FontMetrics getFontMetrics(final Font f)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getFontMetrics()");
        return this.fontMetrics;
    }

    /** {@inheritDoc} */
    @Override
    public Rectangle getClipBounds()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getClipBounds()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void clipRect(final int x, final int y, final int width, final int height)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.clipRect()");
    }

    /** {@inheritDoc} */
    @Override
    public void setClip(final int x, final int y, final int width, final int height)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setClip()");
    }

    /** {@inheritDoc} */
    @Override
    public Shape getClip()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.getClip()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setClip(final Shape clip)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.setClip()");
    }

    /** {@inheritDoc} */
    @Override
    public void copyArea(final int x, final int y, final int width, final int height, final int dx, final int dy)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.copyArea()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawLine(final int x1, final int y1, final int x2, final int y2)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawLine()");
        addTransformDraw("drawLine", x1, y1, x2, y2);
    }

    /** {@inheritDoc} */
    @Override
    public void fillRect(final int x, final int y, final int width, final int height)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.fillRect()");
        addTransformFill("fillRect", x, y, width, height);
    }

    /** {@inheritDoc} */
    @Override
    public void clearRect(final int x, final int y, final int width, final int height)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.clearRect()");
        addTransformDraw("clearRect", x, y, width, height);
    }

    /** {@inheritDoc} */
    @Override
    public void drawRoundRect(final int x, final int y, final int width, final int height, final int arcWidth,
            final int arcHeight)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawRoundRect()");
    }

    /** {@inheritDoc} */
    @Override
    public void fillRoundRect(final int x, final int y, final int width, final int height, final int arcWidth,
            final int arcHeight)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.fillRoundRect()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawOval(final int x, final int y, final int width, final int height)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawOval()");
        addTransformDraw("drawOval", x, y, width, height);
    }

    /** {@inheritDoc} */
    @Override
    public void fillOval(final int x, final int y, final int width, final int height)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.fillOval()");
        addTransformFill("fillOval", x, y, width, height);
    }

    /** {@inheritDoc} */
    @Override
    public void drawArc(final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawArc()");
    }

    /** {@inheritDoc} */
    @Override
    public void fillArc(final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.fillArc()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawPolyline(final int[] xPoints, final int[] yPoints, final int nPoints)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.fillPolyline()");
    }

    /** {@inheritDoc} */
    @Override
    public void drawPolygon(final int[] xPoints, final int[] yPoints, final int nPoints)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawPolygon()");
    }

    /** {@inheritDoc} */
    @Override
    public void fillPolygon(final int[] xPoints, final int[] yPoints, final int nPoints)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.fillPolygon()");
    }

    /** {@inheritDoc} */
    @Override
    public boolean drawImage(final Image img, final int x, final int y, final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean drawImage(final Image img, final int x, final int y, final int width, final int height,
            final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean drawImage(final Image img, final int x, final int y, final Color bgcolor, final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean drawImage(final Image img, final int x, final int y, final int width, final int height, final Color bgcolor,
            final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1,
            final int sy1, final int sx2, final int sy2, final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1,
            final int sy1, final int sx2, final int sy2, final Color bgcolor, final ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.drawImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLGraphics2D.dispose()");
    }

}
