package org.opentrafficsim.draw.graphs;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.XYPlot;

/**
 * Plot that allows hard bounds to be set, with upper and lower bound independent. Manual zooming and auto ranges are bounded
 * within the bounds.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractBoundedPlot extends AbstractPlot
{

    /** Lower bound of domain axis. */
    private Double lowerDomainBound = null;

    /** Upper bound of domain axis. */
    private Double upperDomainBound = null;

    /** Lower bound of range axis. */
    private Double lowerRangeBound = null;

    /** Upper bound of range axis. */
    private Double upperRangeBound = null;

    /**
     * Constructor.
     * @param scheduler scheduler
     * @param caption caption
     * @param updateInterval regular update interval (simulation time)
     * @param delay amount of time that chart runs behind simulation to prevent gaps in the charted data
     */
    public AbstractBoundedPlot(final PlotScheduler scheduler, final String caption, final Duration updateInterval,
            final Duration delay)
    {
        super(scheduler, caption, updateInterval, delay);
    }

    @Override
    protected void setChart(final JFreeChart chart)
    {
        Throw.when(!(chart.getPlot() instanceof XYPlot), IllegalArgumentException.class,
                "AbstractBoundedPlot can only work with XYPlot.");

        super.setChart(chart);

        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.getDomainAxis().addChangeListener(new AxisChangeListener()
        {
            /** Whether to listen, this prevents a stack overflow. */
            private boolean listen = true;

            @SuppressWarnings("synthetic-access")
            @Override
            public void axisChanged(final AxisChangeEvent event)
            {
                if (!this.listen)
                {
                    return;
                }
                this.listen = false;
                constrainAxis(xyPlot.getDomainAxis(), AbstractBoundedPlot.this.lowerDomainBound,
                        AbstractBoundedPlot.this.upperDomainBound);
                this.listen = true;
            }
        });
        xyPlot.getRangeAxis().addChangeListener(new AxisChangeListener()
        {
            /** Whether to listen, this prevents a stack overflow. */
            private boolean listen = true;

            @SuppressWarnings("synthetic-access")
            @Override
            public void axisChanged(final AxisChangeEvent event)
            {
                if (!this.listen)
                {
                    return;
                }
                this.listen = false;
                constrainAxis(xyPlot.getRangeAxis(), AbstractBoundedPlot.this.lowerRangeBound,
                        AbstractBoundedPlot.this.upperRangeBound);
                this.listen = true;
            }
        });
    }

    /**
     * Sets the lower domain bound.
     * @param bound use {@code null} to disable bound
     */
    public void setLowerDomainBound(final Double bound)
    {
        this.lowerDomainBound = bound;
        constrainAxis(getChart().getXYPlot().getDomainAxis(), this.lowerDomainBound, this.upperDomainBound);
    }

    /**
     * Sets the upper domain bound.
     * @param bound use {@code null} to disable bound
     */
    public void setUpperDomainBound(final Double bound)
    {
        this.upperDomainBound = bound;
        constrainAxis(getChart().getXYPlot().getDomainAxis(), this.lowerDomainBound, this.upperDomainBound);
    }

    /**
     * Sets the lower range bound.
     * @param bound use {@code null} to disable bound
     */
    public void setLowerRangeBound(final Double bound)
    {
        this.lowerRangeBound = bound;
        constrainAxis(getChart().getXYPlot().getRangeAxis(), this.lowerRangeBound, this.upperRangeBound);
    }

    /**
     * Sets the upper range bound.
     * @param bound use {@code null} to disable bound
     */
    public void setUpperRangeBound(final Double bound)
    {
        this.upperRangeBound = bound;
        constrainAxis(getChart().getXYPlot().getRangeAxis(), this.lowerRangeBound, this.upperRangeBound);
    }

    /**
     * Constrains axis.
     * @param axis axis
     * @param min minimum value, use {@code null} to apply no bound
     * @param max maximum value, use {@code null} to apply no bound
     */
    private void constrainAxis(final ValueAxis axis, final Double min, final Double max)
    {
        double xLow = axis.getLowerBound();
        double xUpp = axis.getUpperBound();
        if (min != null && max != null && xUpp - xLow > max - min)
        {
            axis.setLowerBound(min);
            axis.setUpperBound(max);
        }
        else if (min != null && xLow < min)
        {
            axis.setLowerBound(min);
            axis.setUpperBound(xUpp + (min - xLow));
        }
        else if (max != null && xUpp > max)
        {
            axis.setLowerBound(xLow - (xUpp - max));
            axis.setUpperBound(max);
        }
    }

    @Override
    public void setAutoBoundDomain(final XYPlot plot)
    {
        if (this.lowerDomainBound != null)
        {
            plot.getDomainAxis().setLowerBound(this.lowerDomainBound);
        }
        if (this.upperDomainBound != null)
        {
            plot.getDomainAxis().setUpperBound(this.upperDomainBound);
        }
    }

    @Override
    public void setAutoBoundRange(final XYPlot plot)
    {
        if (this.lowerRangeBound != null)
        {
            plot.getRangeAxis().setLowerBound(this.lowerRangeBound);
        }
        if (this.upperRangeBound != null)
        {
            plot.getRangeAxis().setUpperBound(this.upperRangeBound);
        }
    }

}
