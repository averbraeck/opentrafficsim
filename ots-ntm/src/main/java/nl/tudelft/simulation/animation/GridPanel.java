/*
 * @(#) GridPanel.java Oct 29, 2003 Copyright (c) 2002-2005 Delft University of
 * Technology Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * This software is proprietary information of Delft University of Technology
 * 
 */
package nl.tudelft.simulation.animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.NumberFormat;

import javax.swing.JPanel;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;

/**
 * The GridPanel introduces the gridPanel <br>
 * (c) copyright 2002-2005 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the
 * Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no
 * warranty.
 * 
 * @version $Revision: 1.2 $ $Date: 2010/08/10 11:37:49 $
 * @author <a href="mailto:nlang@fbk.eur.nl">Niels Lang </a>, <a href="http://www.peter-jacobs.com">Peter Jacobs </a>
 */
public class GridPanel extends JPanel
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the UP directions for moving/zooming */
    public static final int UP = 1;

    /** the DOWN directions for moving/zooming */
    public static final int DOWN = 2;

    /** the LEFT directions for moving/zooming */
    public static final int LEFT = 3;

    /** the RIGHT directions for moving/zooming */
    public static final int RIGHT = 4;

    /** the ZOOM_IN directions for moving/zooming */
    public static final int IN = 5;

    /** the ZOOM_OUT directions for moving/zooming */
    public static final int OUT = 6;

    /** gridColor */
    protected static final Color GRIDCOLOR = Color.BLACK;

    /** the extent of this panel */
    protected Rectangle2D extent = null;

    /** the extent of this panel */
    protected Rectangle2D homeExtent = null;

    /** show the grid */
    protected boolean showGrid = true;

    /** the gridSize in world Units */
    protected double gridSize = 100.0;

    /** the formatter to use */
    protected NumberFormat formatter = NumberFormat.getInstance();

    /** the last computed Dimension */
    protected Dimension lastDimension = null;

    /**
     * constructs a new GridPanel
     * 
     * @param extent the extent to show.
     */
    public GridPanel(final Rectangle2D extent)
    {
        this(extent, new Dimension(600, 600));
    }

    /**
     * constructs a new GridPanel
     * 
     * @param extent the initial extent
     * @param size the size of the panel in pixels.
     */
    public GridPanel(final Rectangle2D extent, final Dimension size)
    {
        super(true);
        this.extent = extent;
        this.homeExtent = (Rectangle2D) extent.clone();
        this.setBackground(Color.WHITE);
        this.setPreferredSize(size);
        this.lastDimension = this.getSize();
    }

    /**
     * returns the extent of this panel
     * 
     * @return Rectangle2D
     */
    public Rectangle2D getExtent()
    {
        return this.extent;
    }

    /**
     * @see javax.swing.JComponent #paintComponent(java.awt.Graphics)
     */
    @Override
    public synchronized void paintComponent(final Graphics g)
    {
        super.paintComponent(g);
        if (!this.getSize().equals(this.lastDimension))
        {
            this.lastDimension = this.getSize();
            this.extent = Renderable2DInterface.Util.computeVisibleExtent(this.extent, this.getSize());
        }
        if (this.showGrid)
        {
            this.drawGrid(g);
        }
    }

    /**
     * show the grid?
     * 
     * @param bool true/false
     */
    public synchronized void showGrid(final boolean bool)
    {
        this.showGrid = bool;
        this.repaint();
    }

    /**
     * pans the panel in a specified direction
     * 
     * @param direction the direction
     * @param percentage the percentage
     */
    public synchronized void pan(final int direction, final double percentage)
    {
        if (percentage <= 0 || percentage > 1.0)
        {
            throw new IllegalArgumentException("percentage<=0 || >1.0");
        }
        switch (direction)
        {
        case LEFT:
            this.extent.setRect(this.extent.getMinX() - percentage * this.extent.getWidth(), this.extent.getMinY(), this.extent.getWidth(),
                    this.extent.getHeight());
            break;
        case RIGHT:
            this.extent.setRect(this.extent.getMinX() + percentage * this.extent.getWidth(), this.extent.getMinY(), this.extent.getWidth(),
                    this.extent.getHeight());
            break;
        case UP:
            this.extent.setRect(this.extent.getMinX(), this.extent.getMinY() + percentage * this.extent.getHeight(), this.extent.getWidth(),
                    this.extent.getHeight());
            break;
        case DOWN:
            this.extent.setRect(this.extent.getMinX(), this.extent.getMinY() - percentage * this.extent.getHeight(), this.extent.getWidth(),
                    this.extent.getHeight());
            break;
        default:
            throw new IllegalArgumentException("direction unkown");
        }
        this.repaint();
    }

    /**
     * resets the panel to its original extent
     */
    public synchronized void home()
    {
        this.extent = Renderable2DInterface.Util.computeVisibleExtent(this.homeExtent, this.getSize());
        this.repaint();
    }

    /**
     * @return Returns the showGrid.
     */
    public boolean isShowGrid()
    {
        return this.showGrid;
    }

    /**
     * @param showGrid The showGrid to set.
     */
    public void setShowGrid(final boolean showGrid)
    {
        this.showGrid = showGrid;
    }

    /**
     * zooms in/out
     * 
     * @param direction the zoom direction
     * @param factor The Factor
     */
    public synchronized void zoom(final int direction, final double factor)
    {
        double newScale = Renderable2DInterface.Util.getScale(this.extent, this.getSize());
        switch (direction)
        {
        case IN:
            newScale = newScale * factor;
            break;
        case OUT:
            newScale = newScale / factor;
            break;
        default:
            throw new IllegalArgumentException("zoom direction unknown");
        }
        this.extent.setRect(this.extent.getCenterX() - 0.5 * newScale * this.getWidth(), this.extent.getCenterY() - 0.5 * newScale * this.getHeight(), newScale
                * this.getWidth(), newScale * this.getHeight());
        this.repaint();
    }

    // ------------------------ PRIVATE METHODS ---------------------------/
    /**
     * Added to make sure the recursive render-call calls THIS render method instead of a potential super-class defined
     * 'paintComponent' render method.
     * 
     * @param g the graphics object
     */
    protected synchronized void drawGrid(final Graphics g)
    {
        // we prepare the graphics object for the grid
        g.setFont(g.getFont().deriveFont(11.0f));
        g.setColor(GRIDCOLOR);
        double scale = Renderable2DInterface.Util.getScale(this.extent, this.getSize());

        int gridSizePixels = (int) Math.round(this.gridSize / scale);
        if (gridSizePixels < 40)
        {
            this.gridSize = 10 * this.gridSize;
            int maximumNumberOfDigits = (int) Math.max(0, 1 + Math.ceil(Math.log(1 / this.gridSize) / Math.log(10)));
            this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
            this.drawGrid(g);
            return;
        }
        if (gridSizePixels > 10 * 40)
        {
            int maximumNumberOfDigits = (int) Math.max(0, 2 + Math.ceil(Math.log(1 / this.gridSize) / Math.log(10)));
            this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
            this.gridSize = this.gridSize / 10;
            this.drawGrid(g);
            return;
        }
        // Let's draw the vertical lines
        double mod = this.extent.getMinX() % this.gridSize;
        int x = (int) -Math.round(mod / scale);
        while (x < this.getWidth())
        {
            Point2D point = Renderable2DInterface.Util.getWorldCoordinates(new Point2D.Double(x, 0), this.extent, this.getSize());
            if (point != null)
            {
                String label = this.formatter.format(Math.round(point.getX() / this.gridSize) * this.gridSize);
                double labelWidth = this.getFontMetrics(this.getFont()).getStringBounds(label, g).getWidth();
                if (x > labelWidth + 4)
                {
                    g.drawLine(x, 15, x, this.getHeight());
                    g.drawString(label, (int) Math.round(x - 0.5 * labelWidth), 11);
                }
            }
            x = x + gridSizePixels;
        }
        // Let's draw the horizontal lines
        mod = Math.abs(this.extent.getMinY()) % this.gridSize;
        int y = (int) Math.round(this.getSize().getHeight() - (mod / scale));
        while (y > 15)
        {
            Point2D point = Renderable2DInterface.Util.getWorldCoordinates(new Point2D.Double(0, y), this.extent, this.getSize());
            if (point != null)
            {
                String label = this.formatter.format(Math.round(point.getY() / this.gridSize) * this.gridSize);
                RectangularShape labelBounds = this.getFontMetrics(this.getFont()).getStringBounds(label, g);
                g.drawLine((int) Math.round(labelBounds.getWidth() + 4), y, this.getWidth(), y);
                g.drawString(label, 2, (int) Math.round(y + labelBounds.getHeight() * 0.3));
            }
            y = y - gridSizePixels;
        }
    }
}