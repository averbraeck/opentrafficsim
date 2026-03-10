package org.opentrafficsim.draw.graphs;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.opentrafficsim.draw.graphs.AbstractPlot.PaintState;

/**
 * Plots with space-time. This class adds some zoom control, where a user can manually select a zoom range, or the plot
 * automatically zooms over the entire space range, and either the entire or some most recent fixed period in time.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <S> paint state type
 */
public abstract class AbstractSpaceTimePlot<S extends PaintState> extends AbstractBoundedPlot<S>
{

    /** Initial end time of plot. */
    private final Duration initialEnd;

    /** Whether to update the axes. */
    private boolean autoBoundAxes = true;

    /** Whether to disable auto bounds on the axes on any change on the axes. */
    private boolean programmaticBoundChanges = false;

    /** Fixed domain range. */
    private Double fixedDomainRange = null;

    /**
     * Constructor.
     * @param caption caption
     * @param updateInterval regular update interval (simulation time)
     * @param scheduler scheduler
     * @param delay amount of time that chart runs behind simulation to prevent gaps in the charted data
     * @param initialEnd initial end time of plots, will be expanded if simulation time exceeds it
     */
    public AbstractSpaceTimePlot(final String caption, final Duration updateInterval, final PlotScheduler scheduler,
            final Duration delay, final Duration initialEnd)
    {
        super(scheduler, caption, updateInterval, delay);
        this.initialEnd = initialEnd;
    }

    @Override
    protected void setChart(final JFreeChart chart)
    {
        super.setChart(chart);
        XYPlot xyPlot = chart.getXYPlot();
        setLowerRangeBound(0.0);
        setUpperRangeBound(getEndLocation().si);
        setLowerDomainBound(0.0);
        setUpperDomainBound(this.initialEnd.si);
        setAutoBounds(xyPlot);
        // axis listeners to enable/disable auto zoom
        xyPlot.getDomainAxis().addChangeListener(new AxisChangeListener()
        {
            @Override
            public void axisChanged(final AxisChangeEvent event)
            {
                if (!AbstractSpaceTimePlot.this.programmaticBoundChanges)
                {
                    // axis was changed, but not by a command from this class (i.e. user zoomed), auto bounds should be disabled
                    AbstractSpaceTimePlot.this.autoBoundAxes = false;
                }
            }
        });
    }

    @Override
    protected void setPaintState()
    {
        super.setPaintState();
        this.programmaticBoundChanges = true;
        setUpperDomainBound(Math.max(getAvailableTime().si, this.initialEnd.si));
        this.programmaticBoundChanges = false;
        if (this.autoBoundAxes && getChart() != null) // null during construction
        {
            setAutoBounds(getChart().getXYPlot());
        }
    }

    /**
     * Update the fixed-ness of the domain range.
     * @param fixed if true; the domain range will not update when new data becomes available; if false; the domain range will
     *            update to show newly available data
     */
    public void updateFixedDomainRange(final boolean fixed)
    {
        this.fixedDomainRange = fixed ? getChart().getXYPlot().getDomainAxis().getRange().getLength() : null;
        notifyPlotChange();
    }

    /**
     * Sets the auto bounds without deactivating auto bounds through the axis change listener. This is used to initialize the
     * plot, and to update the plot when time is increased.
     * @param plot plot with default zoom-all bounds set
     */
    private void setAutoBounds(final XYPlot plot)
    {
        // disables the axis change listener from registering user input that is actually an update of bounds as time increases
        this.programmaticBoundChanges = true;
        if (this.fixedDomainRange != null && getAvailableTime().si > 0.0)
        {
            setLowerDomainBound(Math.max(getAvailableTime().si - this.fixedDomainRange, 0.0));
            setUpperDomainBound(Math.max(getAvailableTime().si, this.initialEnd.si));
        }
        else
        {
            super.setAutoBoundDomain(plot); // super to skip setting autoBoundAxes = true
        }
        super.setAutoBoundRange(plot); // super to skip setting autoBoundAxes = true
        this.programmaticBoundChanges = false;
    }

    /** {@inheritDoc} This implementation overrides to reset user-disabled auto-bounds. */
    @Override
    public final void setAutoBoundDomain(final XYPlot plot)
    {
        super.setAutoBoundDomain(plot);
        this.autoBoundAxes = true;
    }

    /** {@inheritDoc} This implementation overrides to reset user-disabled auto-bounds. */
    @Override
    public final void setAutoBoundRange(final XYPlot plot)
    {
        super.setAutoBoundRange(plot);
        this.autoBoundAxes = true;
    }

    /**
     * Returns the total space.
     * @return total space
     */
    protected abstract Length getEndLocation();

}
