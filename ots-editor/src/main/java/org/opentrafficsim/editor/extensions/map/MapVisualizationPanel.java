package org.opentrafficsim.editor.extensions.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.rmi.RemoteException;
import java.util.List;

import javax.naming.NamingException;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.swing.animation.d2.VisualizationPanel;
import nl.tudelft.simulation.naming.context.ContextInterface;

/**
 * Visualization panel to show the map. This implementation has an adaptive grid color, fixed grid drawing imprecision, and
 * makes certain methods accessible.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class MapVisualizationPanel extends VisualizationPanel
{

    /** */
    private static final long serialVersionUID = 20260212L;

    /** Event when extent changed. */
    public static final EventType EXTENT_CHANGED = new EventType(new MetaData("EXTENT_CHANGED", "EXTENT_CHANGED"));

    /**
     * Constructor.
     * @param homeExtent the initial extent
     * @param producer the object firing animation update events
     * @param context the context that contains the drawing objects
     * @throws RemoteException on error when remote panel and producer cannot connect
     * @throws NamingException on context error
     */
    public MapVisualizationPanel(final Bounds2d homeExtent, final EventProducer producer, final ContextInterface context)
            throws RemoteException, NamingException
    {
        super(homeExtent, producer, context);
    }

    @Override
    public void setBackground(final Color bg)
    {
        int threshold = 64;
        int alternative = 96;
        if (bg.getRed() <= threshold && bg.getGreen() <= threshold && bg.getBlue() <= threshold)
        {
            setGridColor(new Color(alternative, alternative, alternative));
        }
        else
        {
            setGridColor(Color.BLACK);
        }
        super.setBackground(bg);
    }

    // Overridden because there are rounding and vertical mod errors in the super implementation.
    // See https://github.com/averbraeck/dsol/issues/116.
    @Override
    protected synchronized void drawGrid(final Graphics g)
    {
        // we prepare the graphics object for the grid
        g.setFont(g.getFont().deriveFont(11.0f));
        g.setColor(this.getGridColor());
        double scaleX = this.getRenderableScale().getXScale(this.getExtent(), this.getSize());
        double scaleY = this.getRenderableScale().getYScale(this.getExtent(), this.getSize());

        int count = 0;
        double gridSizePixelsX = this.gridSizeX / scaleX;
        while (gridSizePixelsX < 40)
        {
            this.gridSizeX = 10 * this.gridSizeX;
            int maximumNumberOfDigits = (int) Math.max(0, 1 + Math.ceil(Math.log(1 / this.gridSizeX) / Math.log(10)));
            this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
            gridSizePixelsX = (int) Math.round(this.gridSizeX / scaleX);
            if (count++ > 10)
            {
                break;
            }
        }

        count = 0;
        while (gridSizePixelsX > 10 * 40)
        {
            int maximumNumberOfDigits = (int) Math.max(0, 2 + Math.ceil(Math.log(1 / this.gridSizeX) / Math.log(10)));
            this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
            this.gridSizeX = this.gridSizeX / 10;
            gridSizePixelsX = (int) Math.round(this.gridSizeX / scaleX);
            if (count++ > 10)
            {
                break;
            }
        }

        double gridSizePixelsY = this.gridSizeY / scaleY;
        while (gridSizePixelsY < 40)
        {
            this.gridSizeY = 10 * this.gridSizeY;
            int maximumNumberOfDigits = (int) Math.max(0, 1 + Math.ceil(Math.log(1 / this.gridSizeY) / Math.log(10)));
            this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
            gridSizePixelsY = (int) Math.round(this.gridSizeY / scaleY);
            if (count++ > 10)
            {
                break;
            }
        }

        count = 0;
        while (gridSizePixelsY > 10 * 40)
        {
            int maximumNumberOfDigits = (int) Math.max(0, 2 + Math.ceil(Math.log(1 / this.gridSizeY) / Math.log(10)));
            this.formatter.setMaximumFractionDigits(maximumNumberOfDigits);
            this.gridSizeY = this.gridSizeY / 10;
            gridSizePixelsY = (int) Math.round(this.gridSizeY / scaleY);
            if (count++ > 10)
            {
                break;
            }
        }

        // Let's draw the vertical lines
        double mod = this.getExtent().getMinX() % this.gridSizeX;
        double x = -mod / scaleX;
        while (x < this.getWidth())
        {
            Point2d point =
                    this.getRenderableScale().getWorldCoordinates(new Point2D.Double(x, 0), this.getExtent(), this.getSize());
            if (point != null)
            {
                String label = this.formatter.format(Math.round(point.getX() / this.gridSizeX) * this.gridSizeX);
                double labelWidth = this.getFontMetrics(this.getFont()).getStringBounds(label, g).getWidth();
                if (x > labelWidth + 4)
                {
                    int xInt = (int) Math.round(x);
                    g.drawLine(xInt, 15, xInt, this.getHeight());
                    g.drawString(label, (int) Math.round(x - 0.5 * labelWidth), 11);
                }
            }
            x = x + gridSizePixelsX;
        }

        // Let's draw the horizontal lines
        mod = this.getExtent().getMinY() % this.gridSizeY;
        double y = this.getSize().getHeight() + (mod / scaleY);
        while (y > 15)
        {
            Point2d point =
                    this.getRenderableScale().getWorldCoordinates(new Point2D.Double(0, y), this.getExtent(), this.getSize());
            if (point != null)
            {
                String label = this.formatter.format(Math.round(point.getY() / this.gridSizeY) * this.gridSizeY);
                RectangularShape labelBounds = this.getFontMetrics(this.getFont()).getStringBounds(label, g);
                int yInt = (int) Math.round(y);
                g.drawLine((int) Math.round(labelBounds.getWidth() + 4), yInt, this.getWidth(), yInt);
                g.drawString(label, 2, (int) Math.round(y + labelBounds.getHeight() * 0.3));
            }
            y = y - gridSizePixelsY;
        }
    }

    // convenience method

    /**
     * Returns the current world size of 1px.
     * @return current world size of 1px
     */
    public double pxScale()
    {
        double x = getRenderableScale().getXScale(getExtent(), getSize());
        double y = x / getRenderableScale().getYScaleRatio();
        return Math.min(x, y);
    }

    // throw event

    @Override
    public void setExtent(final Bounds2d extent)
    {
        super.setExtent(extent);
        fireEvent(EXTENT_CHANGED);
    }

    // make public for MapInputListener

    @Override
    public void pan(final Point2D mouseClickedPoint, final Point2D mouseReleasedPoint)
    {
        super.pan(mouseClickedPoint, mouseReleasedPoint);
    }

    @Override
    public List<Locatable> getSelectedObjects(final Point2D mousePoint)
    {
        return super.getSelectedObjects(mousePoint);
    }

    @Override
    public Object getSelectedObject(final List<Locatable> targets)
    {
        return super.getSelectedObject(targets);
    }

}
