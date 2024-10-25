package org.opentrafficsim.swing.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.djutils.draw.point.Point2d;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.plot.XYPlot;
import org.opentrafficsim.draw.graphs.GraphUtil;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;

/**
 * Embed a TrajectoryPlot in a Swing JPanel.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SwingTrajectoryPlot extends SwingSpaceTimePlot
{
    /** */
    private static final long serialVersionUID = 20190823L;

    /** Calculate density (vertical line). */
    private boolean density;

    /** Calculate flow ((horizontal line). */
    private boolean flow;

    /** From point for line statistics. */
    private Point2D.Double from;

    /** From point for line statistics. */
    private Point2D.Double to;

    /** Line annotation for line statistics. */
    private XYLineAnnotation lineAnnotation;

    /** Text annotation for line statistics. */
    private XYTextAnnotation textAnnotation;

    /**
     * Construct a new Swing container for a TrajectoryPlot.
     * @param plot the plot to embed
     */
    public SwingTrajectoryPlot(final TrajectoryPlot plot)
    {
        super(plot);
    }

    /**
     * {@inheritDoc} This implementation creates a listener to disable and enable lanes through the legend, and to display
     * density, flow of speed of a line.
     */
    @Override
    protected ChartMouseListener getChartMouseListener()
    {
        // Second listener for legend clicks
        ChartMouseListener toggle = getPlot().getPath().getNumberOfSeries() < 2 ? null
                : GraphUtil.getToggleSeriesByLegendListener(getPlot().getLegend(), getPlot().getLaneVisible());
        return new ChartMouseListener()
        {
            @Override
            public void chartMouseClicked(final ChartMouseEvent event)
            {
                if (toggle != null)
                {
                    toggle.chartMouseClicked(event); // forward to second listener
                }
                if (event.getEntity() instanceof PlotEntity)
                {
                    removeAnnotations();
                    if (SwingTrajectoryPlot.this.from == null)
                    {
                        if (event.getTrigger().isControlDown())
                        {
                            SwingTrajectoryPlot.this.density = false;
                            SwingTrajectoryPlot.this.flow = false;
                        }
                        else if (event.getTrigger().isShiftDown())
                        {
                            SwingTrajectoryPlot.this.density = true;
                            SwingTrajectoryPlot.this.flow = false;
                        }
                        else if (event.getTrigger().isAltDown())
                        {
                            SwingTrajectoryPlot.this.density = false;
                            SwingTrajectoryPlot.this.flow = true;
                        }
                        else
                        {
                            SwingTrajectoryPlot.this.from = null;
                            SwingTrajectoryPlot.this.to = null;
                            return;
                        }
                        SwingTrajectoryPlot.this.from = getValuePoint(event);
                        SwingTrajectoryPlot.this.to = null;
                    }
                    else
                    {
                        SwingTrajectoryPlot.this.to = getValuePoint(event);
                        removeAnnotations();
                        snap(SwingTrajectoryPlot.this.to);
                        drawLine(SwingTrajectoryPlot.this.to);
                        drawStatistics();
                        SwingTrajectoryPlot.this.from = null;
                        SwingTrajectoryPlot.this.to = null;
                    }
                }
            }

            @Override
            public void chartMouseMoved(final ChartMouseEvent event)
            {
                if (toggle != null)
                {
                    toggle.chartMouseClicked(event); // forward to second listener
                }
                if (event.getEntity() instanceof PlotEntity && SwingTrajectoryPlot.this.from != null
                        && SwingTrajectoryPlot.this.to == null)
                {
                    removeAnnotations();
                    Point2D.Double toPoint = getValuePoint(event);
                    snap(toPoint);
                    drawLine(toPoint);
                }
            }

        };
    }

    /**
     * Returns point in data coordinates based on mouse coordinates.
     * @param event event.
     * @return point in data coordinates
     */
    private Point2D.Double getValuePoint(final ChartMouseEvent event)
    {
        Point2D p = getChartPanel().translateScreenToJava2D(new Point(event.getTrigger().getX(), event.getTrigger().getY()));
        XYPlot plot = getChartPanel().getChart().getXYPlot();
        Rectangle2D dataArea = getChartPanel().getChartRenderingInfo().getPlotInfo().getDataArea();
        double x = plot.getDomainAxis().java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
        double y = plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
        return new Point2D.Double(x, y);
    }

    /**
     * Draw line towards point.
     * @param toPoint Point2D.Double; to point.
     */
    private void drawLine(final Point2D.Double toPoint)
    {
        this.lineAnnotation =
                new XYLineAnnotation(this.from.x, this.from.y, toPoint.x, toPoint.y, new BasicStroke(2.0f), Color.WHITE);
        getPlot().getChart().getXYPlot().addAnnotation(this.lineAnnotation);
    }

    /**
     * Draw statistics label.
     */
    private void drawStatistics()
    {
        double dx = this.to.x - this.from.x;
        double dy = this.to.y - this.from.y;
        double v = 3.6 * dy / dx;

        String label;
        if (this.density || this.flow)
        {
            int n = 0;
            for (int i = 0; i < getPlot().getSeriesCount(); i++)
            {
                // quick filter
                int k = getPlot().getItemCount(i) - 1;
                double x1 = Math.min(this.from.x, this.to.x);
                double y1 = Math.min(this.from.y, this.to.y);
                double x2 = Math.max(this.from.x, this.to.x);
                double y2 = Math.max(this.from.y, this.to.y);
                double x3 = Math.min(getPlot().getXValue(i, 0), getPlot().getXValue(i, k));
                double y3 = Math.min(getPlot().getYValue(i, 0), getPlot().getYValue(i, k));
                double x4 = Math.max(getPlot().getXValue(i, 0), getPlot().getXValue(i, k));
                double y4 = Math.max(getPlot().getYValue(i, 0), getPlot().getYValue(i, k));
                if (x3 <= x2 && y3 <= y2 && x1 <= x4 && y1 <= y4)
                {
                    for (int j = 0; j < k; j++)
                    {
                        if (Point2d.intersectionOfLineSegments(this.from.x, this.from.y, this.to.x, this.to.y,
                                getPlot().getXValue(i, j), getPlot().getYValue(i, j), getPlot().getXValue(i, j + 1),
                                getPlot().getYValue(i, j + 1)) != null)
                        {
                            n++;
                            break;
                        }
                    }
                }
            }
            if (this.density)
            {
                label = String.format("%.1f veh/km", Math.abs(1000.0 * n / dy));
            }
            else
            {
                label = String.format("%.1f veh/h", Math.abs(3600.0 * n / dx));
            }
        }
        else
        {
            label = String.format("%.1f km/h", v);
        }

        this.textAnnotation = new XYTextAnnotation(label, this.from.x, this.from.y);
        getPlot().getChart().getXYPlot().addAnnotation(this.textAnnotation);

    }

    /**
     * Remove line and statistic annotations, if any.
     */
    private void removeAnnotations()
    {
        if (SwingTrajectoryPlot.this.lineAnnotation != null)
        {
            getPlot().getChart().getXYPlot().removeAnnotation(SwingTrajectoryPlot.this.lineAnnotation);
        }
        if (SwingTrajectoryPlot.this.textAnnotation != null)
        {
            getPlot().getChart().getXYPlot().removeAnnotation(SwingTrajectoryPlot.this.textAnnotation);
        }
    }

    /**
     * Snap to point for density or flow.
     * @param toPoint Point2D.Double; to point
     */
    private void snap(final Point2D.Double toPoint)
    {
        if (this.density)
        {
            toPoint.x = this.from.x;
        }
        if (this.flow)
        {
            toPoint.y = this.from.y;
        }
    }

    /**
     * Retrieve the plot.
     * @return the plot
     */
    @Override
    public TrajectoryPlot getPlot()
    {
        return (TrajectoryPlot) super.getPlot();
    }

}
