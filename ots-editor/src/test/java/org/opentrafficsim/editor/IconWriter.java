package org.opentrafficsim.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Writes some icons for the editor.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IconWriter
{

    /**
     * Private constructor.
     */
    private IconWriter()
    {

    }

    /**
     * Writes some icons for the editor.
     * @param args arguments.
     * @throws IOException on write exception.
     */
    public static void main(final String[] args) throws IOException
    {
        Color blue = new Color(32, 160, 222);
        Color orange = new Color(222, 160, 32);

        BufferedImage image = next(null, null, 16);
        Graphics2D g = getGraphics(image);

        g.setColor(blue);
        g.fillOval(3, 3, 9, 9);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(3, 3, 9, 9);
        image = next(image, "OTS_node.png", 16);
        g = getGraphics(image);

        g.setColor(orange);
        g.fillOval(3, 3, 9, 9);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(3, 3, 9, 9);
        image = next(image, "OTS_centroid.png", 16);
        g = getGraphics(image);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.0f));
        g.drawLine(3, 12, 11, 2);
        g.setColor(blue);
        g.fillOval(9, 0, 5, 5);
        g.fillOval(0, 9, 5, 5);
        g.setColor(Color.BLACK);
        g.drawOval(9, 0, 5, 5);
        g.drawOval(0, 9, 5, 5);
        image = next(image, "OTS_link.png", 16);
        g = getGraphics(image);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.0f));
        g.drawLine(3, 12, 11, 2);
        g.setColor(orange);
        g.fillOval(9, 0, 5, 5);
        g.setColor(blue);
        g.fillOval(0, 9, 5, 5);
        g.setColor(Color.BLACK);
        g.drawOval(9, 0, 5, 5);
        g.drawOval(0, 9, 5, 5);
        image = next(image, "OTS_connector.png", 16);
        g = getGraphics(image);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(0.75f));
        g.drawLine(8, 8, 1, 4);
        g.drawLine(8, 8, 1, 10);
        g.drawLine(8, 8, 11, 1);
        g.drawLine(8, 8, 13, 12);
        g.setColor(blue);
        g.fillOval(0, 3, 3, 3);
        g.fillOval(0, 9, 3, 3);
        g.fillOval(6, 6, 4, 4);
        g.fillOval(10, 2, 3, 3);
        g.fillOval(12, 11, 3, 3);
        g.setColor(Color.BLACK);
        g.drawOval(0, 3, 3, 3);
        g.drawOval(0, 9, 3, 3);
        g.drawOval(6, 6, 4, 4);
        g.drawOval(10, 2, 3, 3);
        g.drawOval(12, 11, 3, 3);
        image = next(image, "OTS_network.png", 16);
        g = getGraphics(image);

        g.setColor(Color.BLACK);
        g.fillOval(4, 0, 8, 8);
        g.fillRect(4, 4, 8, 8);
        g.fillOval(4, 8, 8, 8);
        g.setColor(new Color(160, 0, 160));
        g.fillOval(6, 2, 4, 4);
        g.setColor(new Color(160, 160, 0));
        g.fillOval(6, 6, 4, 4);
        g.setColor(new Color(0, 160, 160));
        g.fillOval(6, 10, 4, 4);
        image = next(image, "OTS_control.png", 16);
        g = getGraphics(image);

        g.setColor(new Color(0, 128, 160));
        g.fillRect(0, 2, 16, 11);
        g.setColor(Color.GRAY);
        g.fillPolygon(new int[] {0, 16, 13, 3}, new int[] {14, 14, 2, 2}, 4);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(0.75f));
        g.drawLine(1, 13, 3, 2);
        g.drawLine(14, 13, 12, 2);
        g.setStroke(new BasicStroke(0.75f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {3f, 1f}, 0));
        g.drawLine(5, 13, 6, 2);
        g.drawLine(10, 13, 9, 2);
        image = next(image, "OTS_road.png", 24);
        g = getGraphics(image);

        g.rotate(Math.PI / 4.0);
        g.setColor(Color.WHITE);
        g.fillRect((int) (6 * Math.sqrt(2.0)), (int) (-6 * Math.sqrt(2.0)), (int) (12 * Math.sqrt(2.0)),
                (int) (12 * Math.sqrt(2.0)));
        g.setColor(Color.BLACK);
        double r = 6.5;
        RoundRectangle2D.Double shape =
                new RoundRectangle2D.Double(11.5 * Math.sqrt(2.0) - r, -r, 2.0 * r, 2.0 * r, 0.25 * r, 0.25 * r);
        g.draw(shape);
        r = 13.0 / 3.0;
        g.setColor(new Color(255, 204, 0));
        shape = new RoundRectangle2D.Double(11.5 * Math.sqrt(2.0) - r, -r, 2.0 * r, 2.0 * r, 0.15 * r, 0.15 * r);
        g.fill(shape);
        image = next(image, ".\\icons\\Priority24.png", 16);
    }

    /**
     * Saves the previous image. Creates a new empty image for the next.
     * @param image image to save.
     * @param saveFile file name to same image.
     * @param size size of icon.
     * @return next empty image to draw in.
     * @throws IOException on read or write exception.
     */
    private static BufferedImage next(final BufferedImage image, final String saveFile, final int size) throws IOException
    {
        if (saveFile != null)
        {
            File f = new File("..\\ots-swing\\src\\main\\resources\\" + saveFile);
            ImageIO.write(image, "png", f);
        }
        BufferedImage imageOut = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        return imageOut;
    }

    /**
     * Gets the graphics from an image with anti-aliasing enabled.
     * @param image image to return the graphics for.
     * @return graphics.
     */
    private static Graphics2D getGraphics(final BufferedImage image)
    {
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

}
