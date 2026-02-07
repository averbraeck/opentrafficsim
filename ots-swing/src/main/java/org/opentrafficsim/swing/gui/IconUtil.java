package org.opentrafficsim.swing.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.djutils.io.ResourceResolver;

/**
 * Utility to obtain icon with image.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IconUtil
{

    /** Path to image. */
    private final String imagePath;

    /** Image width. */
    private int imageWidth = -1;

    /** Image height. */
    private int imageHeight = -1;

    /** Icon width. */
    private int iconWidth = -1;

    /** Icon height. */
    private int iconHeight = -1;

    /** Gray scale image. */
    private boolean gray = false;

    /**
     * Constructor.
     * @param imagePath path to image
     */
    private IconUtil(final String imagePath)
    {
        this.imagePath = imagePath;
    }

    /**
     * Initiate util with path to image.
     * @param imagePath path to image
     * @return this util for method changing
     */
    public static IconUtil of(final String imagePath)
    {
        return new IconUtil(imagePath);
    }

    /**
     * Set desired image size. The image can be smaller than the whole icon.
     * @param w width
     * @param h height
     * @return this util for method changing
     */
    public IconUtil imageSize(final int w, final int h)
    {
        this.imageWidth = w;
        this.imageHeight = h;
        return this;
    }

    /**
     * Set desired icon size. The icon can be bigger than the image. It will be padded with transparency.
     * @param w width
     * @param h height
     * @return this util for method changing
     */
    public IconUtil iconSize(final int w, final int h)
    {
        this.iconWidth = w;
        this.iconHeight = h;
        return this;
    }

    /**
     * Make image gray scale.
     * @return this util for method changing
     */
    public IconUtil gray()
    {
        this.gray = true;
        return this;
    }

    /**
     * Get the icon.
     * @return icon
     */
    public Icon get()
    {
        Image im;
        try
        {
            im = ImageIO.read(ResourceResolver.resolve(this.imagePath).openStream());
            if (this.gray)
            {
                im = GrayFilter.createDisabledImage(im);
            }
            if (this.imageWidth > 0 || this.imageHeight > 0)
            {
                im = im.getScaledInstance(this.imageWidth > 0 ? this.imageWidth : im.getWidth(null),
                        this.imageHeight > 0 ? this.imageHeight : im.getHeight(null), Image.SCALE_SMOOTH);
            }
        }
        catch (IOException ex)
        {
            // Return some image, we do not want the program to crash on an icon not being available
            int w = this.imageWidth > 0 ? this.imageWidth : 24;
            int h = this.imageHeight > 0 ? this.imageHeight : 24;
            im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) im.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.setColor(Color.BLACK);
            int s = Math.min(w, h);
            g.setFont(new Font("TimesRoman", Font.PLAIN, s));
            FontMetrics metrics = g.getFontMetrics();
            float ws = metrics.stringWidth("?");
            float hs = metrics.getHeight();
            System.out.println("hs: " + hs);
            g.drawString("?", (((float) w) - ws) / 2.0f, ((float) h - hs) / 2.0f + metrics.getAscent());
        }
        if (this.iconWidth > 0 && this.iconHeight > 0)
        {
            BufferedImage bg = new BufferedImage(this.iconWidth > 0 ? this.iconWidth : im.getWidth(null),
                    this.iconHeight > 0 ? this.iconHeight : im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bg.getGraphics();
            g.drawImage(im, (bg.getWidth() - im.getWidth(null)) / 2, (bg.getHeight() - im.getHeight(null)) / 2, null);
            im = bg;
        }
        return new InterpolatedImageIcon(im);
    }

    /**
     * Icon class with image that paints the image using interpolation. This is especially useful for monitors that are not at
     * 100%.
     */
    public static final class InterpolatedImageIcon extends ImageIcon
    {
        /** */
        private static final long serialVersionUID = 20260206L;

        /**
         * Constructor.
         * @param im image
         */
        public InterpolatedImageIcon(final Image im)
        {
            super(im);
        }

        @Override
        public synchronized void paintIcon(final Component c, final Graphics g, final int x, final int y)
        {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            super.paintIcon(c, g, x, y);
        }
    }

}
