package com.bric.multislider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

public class AquaMultiThumbSliderUI<T> extends DefaultMultiThumbSliderUI<T>
{

    private static Color UPPER_GRAY = new Color(168, 168, 168);

    private static Color LOWER_GRAY = new Color(218, 218, 218);

    private static Color OUTLINE_OPACITY = new Color(0, 0, 0, 75);

    public AquaMultiThumbSliderUI(MultiThumbSlider<T> slider)
    {
        super(slider);
        DEPTH = 4;
        FOCUS_PADDING = 2;
        trackHighlightColor = new Color(0x3a99fc);
    }

    @Override
    protected int getPreferredComponentDepth()
    {
        return 24;
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
            return new Dimension(5, 16);
        }
        else if (Thumb.Triangle.equals(thumb))
        {
            return new Dimension(14, 20);
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

        GradientPaint gradient;
        if (slider.getOrientation() == SwingConstants.HORIZONTAL)
        {
            gradient =
                    new GradientPaint(new Point(trackRect.x, trackRect.y), UPPER_GRAY, new Point(trackRect.x,
                            trackRect.y + trackRect.height), LOWER_GRAY);
        }
        else
        {
            gradient =
                    new GradientPaint(new Point(trackRect.x, trackRect.y), UPPER_GRAY, new Point(trackRect.x
                            + trackRect.width, trackRect.y), LOWER_GRAY);
        }
        g.setPaint(gradient);
        g.fill(trackShape);

        paintTrackHighlight(g);

        g.setPaint(OUTLINE_OPACITY);
        g.setStroke(new BasicStroke(1));
        g.draw(trackShape);

        if (slider.isPaintTicks())
        {
            g.setColor(new Color(0x777777));
            g.setStroke(new BasicStroke(1));
            paintTick(g, .25f, 4, 9, false);
            paintTick(g, .5f, 4, 9, false);
            paintTick(g, .75f, 4, 9, false);
            paintTick(g, 0f, 4, 9, false);
            paintTick(g, 1f, 4, 9, false);
        }
    }

    @Override
    protected Rectangle calculateTrackRect()
    {
        Rectangle r = super.calculateTrackRect();

        // why so much dead space? I don't know. This only tries to emulate
        // what Apple is doing.
        int k = 22;
        if (slider.getOrientation() == SwingConstants.HORIZONTAL)
        {
            r.x = k;
            r.width = slider.getWidth() - k * 2;
        }
        else
        {
            r.y = k;
            r.height = slider.getHeight() - k * 2;
        }
        return r;

    }

    @Override
    protected void paintThumb(Graphics2D g, int thumbIndex, float selected)
    {
        Shape outline = getThumbShape(thumbIndex);

        Rectangle2D thumbBounds = ShapeBounds.getBounds(outline);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Paint fill =
                new LinearGradientPaint(new Point2D.Double(0, thumbBounds.getMinY()), new Point2D.Double(0,
                        thumbBounds.getMaxY()), new float[]{0, .5f, .501f, 1}, new Color[]{new Color(0xFFFFFF),
                        new Color(0xF4F4F4), new Color(0xECECEC), new Color(0xEDEDED)});
        g.setPaint(fill);
        g.fill(outline);

        if (mouseIsDown && thumbIndex == slider.getSelectedThumb())
        {
            g.setPaint(new Color(0, 0, 0, 28));
            g.fill(outline);
        }

        g.setStroke(new BasicStroke(1f));
        g.setPaint(new Color(0, 0, 0, 110));
        g.draw(outline);

        if (thumbIndex == slider.getSelectedThumb())
        {
            Color focusColor = new Color(0xa7, 0xd5, 0xff, 240);
            PlafPaintUtils.paintFocus(g, outline, FOCUS_PADDING, focusColor, false);
            g.setStroke(new BasicStroke(1f));
            g.setPaint(new Color(0, 0, 0, 23));
            g.draw(outline);
        }
    }
}
