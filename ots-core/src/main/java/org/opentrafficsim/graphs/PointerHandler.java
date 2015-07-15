package org.opentrafficsim.graphs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionAug 13, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
abstract class PointerHandler implements MouseListener, MouseMotionListener
{
    /** {@inheritDoc} */
    @Override
    public void mouseDragged(final MouseEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public void mouseMoved(final MouseEvent mouseEvent)
    {
        final ChartPanel cp = (ChartPanel) mouseEvent.getSource();
        final XYPlot plot = (XYPlot) cp.getChart().getPlot();
        // Show a cross hair cursor while the mouse is on the graph
        final boolean showCrossHair = cp.getScreenDataArea().contains(mouseEvent.getPoint());
        if (cp.getHorizontalAxisTrace() != showCrossHair)
        {
            cp.setHorizontalAxisTrace(showCrossHair);
            cp.setVerticalAxisTrace(showCrossHair);
            plot.notifyListeners(new PlotChangeEvent(plot));
        }
        if (showCrossHair)
        {
            Point2D p = cp.translateScreenToJava2D(mouseEvent.getPoint());
            PlotRenderingInfo pi = cp.getChartRenderingInfo().getPlotInfo();
            updateHint(plot.getDomainAxis().java2DToValue(p.getX(), pi.getDataArea(), plot.getDomainAxisEdge()), plot
                .getRangeAxis().java2DToValue(p.getY(), pi.getDataArea(), plot.getRangeAxisEdge()));
        }
        else
        {
            updateHint(Double.NaN, Double.NaN);
        }
    }

    /**
     * Called when the pointer is positioned inside the data area of the graph, or when it leaves the data area. <br>
     * When the mouse is outside the data area both parameters are set to Double.NaN.
     * @param domainValue Double; the X-value (in domain units), or Double.NaN if the pointer is outside the data area
     * @param rangeValue Double; the Y-value (in domain units), or Double.NaN if the pointer is outside the data area
     */
    abstract void updateHint(double domainValue, double rangeValue);

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(final MouseEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(final MouseEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent e)
    {
        // No action
    }

    /** {@inheritDoc} */
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
