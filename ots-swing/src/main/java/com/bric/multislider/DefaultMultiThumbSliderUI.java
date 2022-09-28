package com.bric.multislider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class DefaultMultiThumbSliderUI<T> extends MultiThumbSliderUI<T>
{

    protected int FOCUS_PADDING = 3;

    protected Color trackHighlightColor = new Color(0, 0, 0, 140);

    public DefaultMultiThumbSliderUI(MultiThumbSlider<T> slider)
    {
        super(slider);
        this.DEPTH = 10;
    }

    protected boolean isTrackHighlightActive()
    {
        return this.slider.getThumbCount() == 2;
    }

    @Override
    protected int getPreferredComponentDepth()
    {
        return 20;
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
        Shape trackOutline = getTrackOutline();
        g = (Graphics2D) g.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xBBBBBB));
        g.fill(trackOutline);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.clip(trackOutline);
        g2.setColor(new Color(0xAAAAAA));
        g2.setStroke(new BasicStroke(2));
        for (float y = 0; y < .5f; y += .1f)
        {
            g2.draw(trackOutline);
            g2.translate(0, .1f);
        }
        g2.dispose();

        paintTrackHighlight(g);

        g.setColor(new Color(0x888888));
        g.setStroke(new BasicStroke(1));
        g.draw(trackOutline);

        if (this.slider.isPaintTicks())
        {
            g.setColor(new Color(0x777777));
            g.setStroke(new BasicStroke(1));
            paintTick(g, .25f, 0, 4, true);
            paintTick(g, .5f, 0, 4, true);
            paintTick(g, .75f, 0, 4, true);
            paintTick(g, 0f, 0, 4, true);
            ;
            paintTick(g, 1f, 0, 4, true);
        }
        g.dispose();
    }

    /**
     * This optional method highlights the space on the track (by simply adding a shadow) between two thumbs.
     * @param g graphics
     */
    protected void paintTrackHighlight(Graphics2D g)
    {
        if (!isTrackHighlightActive())
            return;
        g = (Graphics2D) g.create();
        Point2D p1 = getThumbCenter(0);
        Point2D p2 = getThumbCenter(1);
        Shape outline;
        if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            float minX = (float) Math.min(p1.getX(), p2.getX());
            float maxX = (float) Math.max(p1.getX(), p2.getX());
            outline = new Rectangle2D.Float(minX, this.trackRect.y, maxX - minX, this.trackRect.height);
        }
        else
        {
            float minY = (float) Math.min(p1.getY(), p2.getY());
            float maxY = (float) Math.max(p1.getY(), p2.getY());
            outline = new Rectangle2D.Float(this.trackRect.x, minY, this.trackRect.width, maxY - minY);
        }
        g.setColor(this.trackHighlightColor);
        g.fill(outline);
        g.dispose();
    }

    protected void paintTick(Graphics2D g, float f, int d1, int d2, boolean mirror)
    {
        if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            int x = (int) (this.trackRect.x + this.trackRect.width * f + .5f);
            int y = this.trackRect.y + this.trackRect.height;
            g.drawLine(x, y + d1, x, y + d2);
            if (mirror)
            {
                y = this.trackRect.y;
                g.drawLine(x, y - d1, x, y - d2);
            }
        }
        else
        {
            int y = (int) (this.trackRect.y + this.trackRect.height * f + .5f);
            int x = this.trackRect.x + this.trackRect.width;
            g.drawLine(x + d1, y, x + d2, y);
            if (mirror)
            {
                x = this.trackRect.x;
                g.drawLine(x - d1, y, x - d2, y);
            }
        }
    }

    @Override
    protected void paintFocus(Graphics2D g)
    {
        Shape trackOutline = getTrackOutline();
        g = (Graphics2D) g.create();
        PlafPaintUtils.paintFocus(g, trackOutline, this.FOCUS_PADDING);
        g.dispose();
    }

    @Override
    protected Rectangle calculateTrackRect()
    {
        int k = (int) (10 + this.FOCUS_PADDING + .5);
        if (this.slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            return new Rectangle(k, this.slider.getHeight() / 2 - this.DEPTH / 2, this.slider.getWidth() - 2 * k - 1,
                    this.DEPTH);
        }
        else
        {
            return new Rectangle(this.slider.getWidth() / 2 - this.DEPTH / 2, k, this.DEPTH,
                    this.slider.getHeight() - 2 * k - 1);
        }
    }

    protected Shape getTrackOutline()
    {
        this.trackRect = calculateTrackRect();
        float k = Math.max(10, this.FOCUS_PADDING) + 1;
        int z = 3;
        if (this.slider.getOrientation() == MultiThumbSlider.VERTICAL)
        {
            return new RoundRectangle2D.Float(this.trackRect.x, this.trackRect.y - z, this.trackRect.width,
                    this.trackRect.height + 2 * z, k, k);
        }
        return new RoundRectangle2D.Float(this.trackRect.x - z, this.trackRect.y, this.trackRect.width + 2 * z,
                this.trackRect.height, k, k);
    }

    @Override
    protected void paintThumbs(Graphics2D g)
    {
        float[] values = this.slider.getThumbPositions();
        for (int a = 0; a < values.length; a++)
        {
            float darkness = a == this.slider.getSelectedThumb() ? 1 : this.thumbIndications[a] * .5f;
            paintThumb(g, a, darkness);
        }
    }

    protected void paintThumb(Graphics2D g, int thumbIndex, float selected)
    {
        g = (Graphics2D) g.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape outline = getThumbShape(thumbIndex);
        int gray = (int) ((1 - selected) * 100 + 30);
        g.setColor(new Color(gray, gray, gray));
        g.fill(outline);
        gray = (int) ((1 - selected) * 100);
        g.setColor(new Color(gray, gray, gray));
        g.draw(outline);
        g.dispose();
    }
}
