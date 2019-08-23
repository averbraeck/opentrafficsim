package org.opentrafficsim.draw.graphs;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * Plots with space-time. This class adds some zoom control, where a user can manually select a zoom range, or the plot
 * automatically zooms over the entire space range, and either the entire or some most recent fixed period in time.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractSpaceTimePlot extends AbstractBoundedPlot
{

    /** Initial end time of plot. */
    private final Time initialEnd;

    /** Whether to update the axes. */
    private boolean autoBoundAxes = true;

    /** Whether to disable auto bounds on the axes on any change on the axes. */
    private boolean virtualAutoBounds = false;

    /** Fixed domain range. */
    private Double fixedDomainRange = null;

    /**
     * Constructor.
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param simulator OTSSimulatorInterface; simulator
     * @param delay Duration; amount of time that chart runs behind simulation to prevent gaps in the charted data
     * @param initialEnd Time; initial end time of plots, will be expanded if simulation time exceeds it
     */
    public AbstractSpaceTimePlot(final String caption, final Duration updateInterval, final OTSSimulatorInterface simulator,
            final Duration delay, final Time initialEnd)
    {
        super(simulator, caption, updateInterval, delay);
        this.initialEnd = initialEnd;
    }

    /** {@inheritDoc} */
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
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public void axisChanged(final AxisChangeEvent event)
            {
                if (!AbstractSpaceTimePlot.this.virtualAutoBounds)
                {
                    // the axis was changed, but not by a command from this class, auto bounds should be disabled
                    AbstractSpaceTimePlot.this.autoBoundAxes = false;
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected void update()
    {
        if (getUpdateTime() != null && this.initialEnd != null)
        {
            setUpperDomainBound(Math.max(getUpdateTime().si, this.initialEnd.si));
        }
        if (this.autoBoundAxes && getChart() != null) // null during construction
        {
            setAutoBounds(getChart().getXYPlot());
        }
        super.update();
    }

    /**
     * Update the fixed-ness of the domain range.
     * @param fixed boolean; if true; the domain range will not update when new data becomes available; if false; the domain
     *            range will update to show newly available data
     */
    public void updateFixedDomainRange(final boolean fixed)
    {
        this.fixedDomainRange = fixed ? getChart().getXYPlot().getDomainAxis().getRange().getLength() : null;
        notifyPlotChange();
    }

    /**
     * Sets the auto bounds without deactivating auto bounds through the axis change listener. This is used to initialize the
     * plot, and to update the plot when time is increased.
     * @param plot XYPlot; plot with default zoom-all bounds set
     */
    private void setAutoBounds(final XYPlot plot)
    {
        // disables the axis change listener from registering a user input that is actually an update of bounds as the time
        // increases
        this.virtualAutoBounds = true;
        if (this.fixedDomainRange != null && getUpdateTime().si > 0.0)
        {
            plot.getDomainAxis().setRange(Math.max(getUpdateTime().si - this.fixedDomainRange, 0.0), getUpdateTime().si);
        }
        else
        {
            super.setAutoBoundDomain(plot); // super to skip setting autoBoundAxes = true
        }
        super.setAutoBoundRange(plot); // super to skip setting autoBoundAxes = true
        this.virtualAutoBounds = false;
    }

    /** {@inheritDoc} This implementation overrides to enable it's own form of auto bounds. */
    @Override
    public final void setAutoBoundDomain(final XYPlot plot)
    {
        super.setAutoBoundDomain(plot);
        this.autoBoundAxes = true;
    }

    /** {@inheritDoc} This implementation overrides to enable it's own form of auto bounds. */
    @Override
    public final void setAutoBoundRange(final XYPlot plot)
    {
        super.setAutoBoundRange(plot);
        this.autoBoundAxes = true;
    }

    /**
     * Returns the total path length.
     * @return Length; total path length
     */
    protected abstract Length getEndLocation();

}
