package org.opentrafficsim.gui.multislider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

public class DefaultMultiThumbSliderUI<T> extends MultiThumbSliderUI<T>
{

    int FOCUS_PADDING = 3;

    float THUMB_RADIUS = 7;

    PropertyChangeListener thumbShapeListener = new PropertyChangeListener()
    {

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            updateThumbRadius();
        }

    };

    public DefaultMultiThumbSliderUI(MultiThumbSlider<T> slider)
    {
        super(slider);
        DEPTH = 10;
    }

    private void updateThumbRadius()
    {
        if ("circular".equals(getProperty(slider, "thumbShape", "circular")))
        {
            THUMB_RADIUS = 7;
        }
        else
        {
            THUMB_RADIUS = 5;
        }
    }

    @Override
    public void installUI(JComponent slider)
    {
        super.installUI(slider);
        slider.addPropertyChangeListener("thumbShape", thumbShapeListener);
        updateThumbRadius();
    }

    @Override
    public void uninstallUI(JComponent slider)
    {
        super.uninstallUI(slider);
        slider.removePropertyChangeListener("thumbShape", thumbShapeListener);
    }

    @Override
    public Dimension getMaximumSize(JComponent s)
    {
        return accomodateThumb(super.getMaximumSize(s));
    }

    @Override
    public Dimension getMinimumSize(JComponent s)
    {
        return accomodateThumb(super.getMinimumSize(s));
    }

    @Override
    public Dimension getPreferredSize(JComponent s)
    {
        return accomodateThumb(super.getPreferredSize(s));
    }

    private Dimension accomodateThumb(Dimension d)
    {
        if (slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            d.height = Math.max(d.height, (int) (2 * THUMB_RADIUS + .5f + FOCUS_PADDING * 2));
        }
        else
        {
            d.width = Math.max(d.width, (int) (2 * THUMB_RADIUS + .5f + FOCUS_PADDING * 2));
        }
        return d;
    }

    @Override
    public int getThumbSpan()
    {
        return (int) (2 * THUMB_RADIUS + .5f);
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
        g.setColor(new Color(0x888888));
        g.setStroke(new BasicStroke(1));
        g.draw(trackOutline);

        if (slider.isPaintTicks())
        {
            g.setColor(new Color(0x777777));
            g.setStroke(new BasicStroke(1));
            paintTick(g, .25f, 4);
            paintTick(g, .5f, 4);
            paintTick(g, .75f, 4);
            paintTick(g, 0f, 4);
            paintTick(g, 1f, 4);
        }
        g.dispose();
    }

    protected void paintTick(Graphics2D g, float f, int d)
    {
        if (slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            int x = (int) (trackRect.x + trackRect.width * f + .5f);
            int y = trackRect.y + trackRect.height;
            g.drawLine(x, y, x, y + d);
            y = trackRect.y;
            g.drawLine(x, y, x, y - d);
        }
        else
        {
            int y = (int) (trackRect.y + trackRect.height * f + .5f);
            int x = trackRect.x + trackRect.width;
            g.drawLine(x, y, x + d, y);
            x = trackRect.x;
            g.drawLine(x, y, x - d, y);
        }
    }

    @Override
    protected void paintFocus(Graphics2D g)
    {
        Shape trackOutline = getTrackOutline();
        g = (Graphics2D) g.create();
        PlafPaintUtils.paintFocus(g, trackOutline, FOCUS_PADDING);
        g.dispose();
    }

    @Override
    protected Rectangle calculateTrackRect()
    {
        int k = (int) (THUMB_RADIUS + FOCUS_PADDING + .5);
        if (slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
        {
            return new Rectangle(k, slider.getHeight() / 2 - DEPTH / 2, slider.getWidth() - 2 * k - 1, DEPTH);
        }
        else
        {
            return new Rectangle(slider.getWidth() / 2 - DEPTH / 2, k, DEPTH, slider.getHeight() - 2 * k - 1);
        }
    }

    protected Shape getTrackOutline()
    {
        trackRect = calculateTrackRect();
        float k = Math.max(THUMB_RADIUS, FOCUS_PADDING) + 1;
        if (slider.getOrientation() == MultiThumbSlider.VERTICAL)
        {
            return new RoundRectangle2D.Float(trackRect.x, trackRect.y - THUMB_RADIUS, trackRect.width, trackRect.height + 2
                * THUMB_RADIUS, k, k);
        }
        return new RoundRectangle2D.Float(trackRect.x - THUMB_RADIUS, trackRect.y, trackRect.width + 2 * THUMB_RADIUS,
            trackRect.height, k, k);
    }

    @Override
    protected void paintThumbs(Graphics2D g)
    {
        float[] values = slider.getThumbPositions();
        for (int a = 0; a < values.length; a++)
        {
            float value = values[a];
            Number n = (Number) value;
            float darkness = a == slider.getSelectedThumb() ? 1 : thumbIndications[a] * .5f;
            if (n.floatValue() >= 0 && n.floatValue() <= 1)
            {
                if (slider.getOrientation() == MultiThumbSlider.VERTICAL)
                {
                    float y;
                    float height = (float) trackRect.height;
                    float x = (float) trackRect.getCenterX();
                    if (slider.isInverted())
                    {
                        y = (float) (n.floatValue() * height + trackRect.y);
                    }
                    else
                    {
                        y = (float) ((1 - n.floatValue()) * height + trackRect.y);
                    }
                    paintThumb(g, darkness, x, y);
                }
                else
                {
                    float x;
                    float width = (float) trackRect.width;
                    float y = (float) trackRect.getCenterY();
                    if (slider.isInverted())
                    {
                        x = (float) ((1 - n.floatValue()) * width + trackRect.x);
                    }
                    else
                    {
                        x = (float) (n.floatValue() * width + trackRect.x);
                    }
                    paintThumb(g, darkness, x, y);
                }
            }
            else
            {
                System.err.println("DefaultMultiThumbSliderUI Error: thumb index " + a
                    + " was not within [0,1]. Current value = " + n);
            }
        }
    }

    protected void paintThumb(Graphics2D g, float selected, float x, float y)
    {
        g = (Graphics2D) g.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape outline;
        if ("circular".equals(getProperty(slider, "thumbShape", "circular")))
        {
            outline = new Ellipse2D.Float(x - THUMB_RADIUS, y - THUMB_RADIUS, 2 * THUMB_RADIUS, 2 * THUMB_RADIUS);
        }
        else
        {
            GeneralPath p = new GeneralPath();
            int j = 2;
            float k = THUMB_RADIUS;
            if (slider.getOrientation() == MultiThumbSlider.HORIZONTAL)
            {
                p.moveTo(x - k, trackRect.y - k + j);
                p.lineTo(x + k, trackRect.y - k + j);
                p.lineTo(x, trackRect.y + j);
                p.lineTo(x, trackRect.y + trackRect.height - j);
                p.lineTo(x - k, trackRect.y + trackRect.height + k - j);
                p.lineTo(x + k, trackRect.y + trackRect.height + k - j);
                p.lineTo(x, trackRect.y + trackRect.height - j);
                p.lineTo(x, trackRect.y + j);
                p.closePath();
            }
            else
            {
                p.moveTo(trackRect.x - k + j, y - k);
                p.lineTo(trackRect.x - k + j, y + k);
                p.lineTo(trackRect.x + j, y);
                p.lineTo(trackRect.x + trackRect.width - j, y);
                p.lineTo(trackRect.x + trackRect.width + k - j, y + k);
                p.lineTo(trackRect.x + trackRect.width + k - j, y - k);
                p.lineTo(trackRect.x + trackRect.width - j, y);
                p.lineTo(trackRect.x + j, y);
                p.closePath();
            }
            outline = p;
        }
        int gray = (int) ((1 - selected) * 100 + 30);
        g.setColor(new Color(gray, gray, gray));
        g.fill(outline);
        gray = (int) ((1 - selected) * 100);
        g.setColor(new Color(gray, gray, gray));
        g.draw(outline);
        g.dispose();
    }
}
