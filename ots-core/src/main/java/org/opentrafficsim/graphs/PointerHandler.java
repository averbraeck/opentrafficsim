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
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Aug 13, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
abstract class PointerHandler implements MouseListener, MouseMotionListener
{
    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(final MouseEvent e)
    {
        // No action
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
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
            updateHint(plot.getDomainAxis().java2DToValue(p.getX(), pi.getDataArea(), plot.getDomainAxisEdge()),
                    plot.getRangeAxis().java2DToValue(p.getY(), pi.getDataArea(), plot.getRangeAxisEdge()));
        }
        else
            updateHint(Double.NaN, Double.NaN);
    }

    /**
     * Called when the pointer is positioned inside the data area of the graph, or when it leaves the data area. <br />
     * When the mouse is outside the data area both parameters are set to Double.NaN.
     * @param domainValue Double; the X-value (in domain units), or Double.NaN if the pointer is outside the data area
     * @param rangeValue Double; the Y-value (in domain units), or Double.NaN if the pointer is outside the data area
     */
    abstract void updateHint(double domainValue, double rangeValue);

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e)
    {
        // No action
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e)
    {
        // No action
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // No action
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent e)
    {
        // No action
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
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
