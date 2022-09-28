package com.bric.multislider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VistaMultiThumbSliderUI<T> extends DefaultMultiThumbSliderUI<T>
{

    /**
     * @param slider the slider
     */
    public VistaMultiThumbSliderUI(MultiThumbSlider<T> slider)
    {
        super(slider);
        this.DEPTH = 8;// PK 4;
        this.FOCUS_PADDING = 2;
        this.trackHighlightColor = new Color(0x3a99fc);
    }

    @Override
    protected int getPreferredComponentDepth()
    {
        return 22;
    }

    @Override
    protected void paintFocus(Graphics2D g)
    {
        // do nothing, this is really handled in paintThumb now
    }

    @Override
    protected Dimension getThumbSize(int thumbIndex)
    {
        Thumb thumb = getThumb(thumbIndex);
        if (Thumb.Hourglass.equals(thumb))
        {
            return new Dimension(8, 16);
        }
        else if (Thumb.Triangle.equals(thumb))
        {
            return new Dimension(10, 18);
        }
        else if (Thumb.Rectangle.equals(thumb))
        {
            return new Dimension(10, 20);
        }
        else
        {
            return new Dimension(16, 16);
        }
    }

    @Override
    protected void paintTrack(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape trackShape = getTrackOutline();

        Paint fill = new Color(0xc0c0c0/* PK 0xe7eaea */);
        g.setPaint(fill);
        g.fill(trackShape);
        g.setPaint(new Color(0, 0, 0, 96/* PK 16 */));
        g.drawLine(this.trackRect.x, this.trackRect.y, this.trackRect.x + this.trackRect.width, this.trackRect.y);
        g.drawLine(this.trackRect.x, this.trackRect.y, this.trackRect.x, this.trackRect.y + this.trackRect.height);
        g.drawLine(this.trackRect.x + this.trackRect.width, this.trackRect.y, this.trackRect.x + this.trackRect.width,
                this.trackRect.y + this.trackRect.height);
        g.setPaint(new Color(255, 255, 255, 16));
        g.drawLine(this.trackRect.x, this.trackRect.y + this.trackRect.height, this.trackRect.x + this.trackRect.width,
                this.trackRect.y + this.trackRect.height);

        paintTrackHighlight(g);

        if (this.slider.isPaintTicks())
        {
            g.setColor(new Color(0, 0, 0, 40));
            g.setStroke(new BasicStroke(1));
            paintTick(g, .25f, 4, 8, false);
            paintTick(g, .5f, 4, 8, false);
            paintTick(g, .75f, 4, 8, false);
            paintTick(g, 0f, 4, 8, false);
            paintTick(g, 1f, 4, 8, false);
        }
    }

    @Override
    protected Shape getTrackOutline()
    {
        this.trackRect = calculateTrackRect();
        return this.trackRect;
    }

    @Override
    protected void paintThumb(Graphics2D g, int thumbIndex, float selected)
    {
        Shape outline = getThumbShape(thumbIndex);

        Rectangle2D thumbBounds = ShapeBounds.getBounds(outline);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Paint fill;
        Paint strokePaint;
        if (this.mouseIsDown && thumbIndex == this.slider.getSelectedThumb())
        {
            fill = new LinearGradientPaint(new Point2D.Double(0, thumbBounds.getMinY()),
                    new Point2D.Double(0, thumbBounds.getMaxY()), new float[] {0, .55f, .5501f, 1},
                    new Color[] {new Color(0xe5f4fc), new Color(0x9dd5f3), new Color(0x6cbbe5), new Color(0x50a1cc)});
            strokePaint = new Color(0x2c628b);
        }
        else
        {
            fill = new LinearGradientPaint(new Point2D.Double(0, thumbBounds.getMinY()),
                    new Point2D.Double(0, thumbBounds.getMaxY()), new float[] {0, .55f, .5501f, 1},
                    new Color[] {tween(new Color(0xf2f2f2), new Color(0xe9f6fd), selected),
                            tween(new Color(0xebebeb), new Color(0xd8effc), selected),
                            tween(new Color(0xdbdbdb), new Color(0xbde6fd), selected),
                            tween(new Color(0xd7d7d7), new Color(0xaedef8), selected)});
            strokePaint = tween(new Color(0x707070), new Color(0x3c7fb1), selected);
        }
        g.setPaint(fill);
        g.fill(outline);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.clip(outline);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(4));
        g2.draw(outline);
        g2.dispose();

        g.setStroke(new BasicStroke(1f));
        g.setPaint(strokePaint);
        g.draw(outline);
    }

    private static final Color tween(Color c1, Color c2, float f)
    {
        if (f < 0)
            f = 0;
        if (f > 1)
            f = 1;
        int r = (int) (c1.getRed() * (1 - f) + f * c2.getRed());
        int g = (int) (c1.getGreen() * (1 - f) + f * c2.getGreen());
        int b = (int) (c1.getBlue() * (1 - f) + f * c2.getBlue());
        int a = (int) (c1.getAlpha() * (1 - f) + f * c2.getAlpha());
        return new Color(r, g, b, a);
    }
}
