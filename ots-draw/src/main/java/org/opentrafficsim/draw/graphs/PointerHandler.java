package org.opentrafficsim.draw.graphs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

/**
 * Handle mouse events for a graph.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public abstract class PointerHandler implements MouseListener, MouseMotionListener
{

    /**
     * Constructor.
     */
    public PointerHandler()
    {
        //
    }

    @Override
    public void mouseDragged(final MouseEvent e)
    {
        // No action
    }

    @Override
    public void mouseMoved(final MouseEvent mouseEvent)
    {
        final ChartPanel cp = (ChartPanel) mouseEvent.getSource();
        final XYPlot plot = cp.getChart().getXYPlot();
        final boolean onChartArea = cp.getScreenDataArea().contains(mouseEvent.getPoint());
        if (cp.getHorizontalAxisTrace() != onChartArea)
        {
            cp.setHorizontalAxisTrace(onChartArea);
            cp.setVerticalAxisTrace(onChartArea);
        }
        if (onChartArea)
        {
            Point2D p = cp.translateScreenToJava2D(mouseEvent.getPoint());
            PlotRenderingInfo pi = cp.getChartRenderingInfo().getPlotInfo();
            updateHint(plot.getDomainAxis().java2DToValue(p.getX(), pi.getDataArea(), plot.getDomainAxisEdge()),
                    plot.getRangeAxis().java2DToValue(p.getY(), pi.getDataArea(), plot.getRangeAxisEdge()));
        }
        else
        {
            updateHint(Double.NaN, Double.NaN);
        }

        /*
         * Due to the updateHint() above, the chart panel is repainted and causes paintComponent to be invoked. This will remove
         * pointers to the vertical and horizontal trace lines. The chart panel's own mouseMoved method can then no longer
         * un-paint the old line when the mouse is moved. A large collection of visible crosshairs results. Therefore, we need
         * to repaint again to remove the old crosshair.
         */
        cp.repaint();

        if (onChartArea)
        {
            /*
             * Because of the required repaint() above, we need to trigger painting of the crosshair later.
             */
            SwingUtilities.invokeLater(() -> cp.mouseMoved(mouseEvent));
        }
    }

    /**
     * Called when the pointer is positioned inside the data area of the graph, or when it leaves the data area. <br>
     * When the mouse is outside the data area both parameters are set to Double.NaN.
     * @param domainValue the X-value (in domain units), or Double.NaN if the pointer is outside the data area
     * @param rangeValue the Y-value (in domain units), or Double.NaN if the pointer is outside the data area
     */
    public abstract void updateHint(double domainValue, double rangeValue);

    @Override
    public void mouseClicked(final MouseEvent e)
    {
        // No action
    }

    @Override
    public void mousePressed(final MouseEvent e)
    {
        // No action
    }

    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // No action
    }

    @Override
    public void mouseEntered(final MouseEvent e)
    {
        // No action
    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent)
    {
        final ChartPanel cp = (ChartPanel) mouseEvent.getSource();
        final XYPlot plot = (XYPlot) cp.getChart().getPlot();
        // Remove the cross hair cursor when the cursor moves outside the graph
        if (cp.getHorizontalAxisTrace())
        {
            cp.setHorizontalAxisTrace(false);
            cp.setVerticalAxisTrace(false);
            plot.notifyListeners(new PlotChangeEvent(plot));
        }
        updateHint(Double.NaN, Double.NaN); // Clear the hint text
    }
}
