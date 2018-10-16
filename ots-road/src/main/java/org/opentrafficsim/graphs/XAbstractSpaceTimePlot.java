package org.opentrafficsim.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

import nl.tudelft.simulation.language.Throw;

/**
 * Plots with space-time. This class adds some zoom control, where a user can manually select a zoom range, or the plot
 * automatically zooms over the entire space range, and either the entire or some most recent fixed period in time.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class XAbstractSpaceTimePlot extends XAbstractPlot
{

    /** */
    private static final long serialVersionUID = 20181014L;

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
     * @param delay Duration; delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories
     * @param initialEnd Time; initial end time of plots, will be expanded if simulation time exceeds it
     */
    public XAbstractSpaceTimePlot(final String caption, final Duration updateInterval, final OTSSimulatorInterface simulator,
            final Duration delay, final Time initialEnd)
    {
        super(caption, updateInterval, simulator, delay);
        this.initialEnd = initialEnd;
    }

    /** {@inheritDoc} */
    @Override
    protected void setChart(final JFreeChart chart)
    {
        Throw.when(!(chart.getPlot() instanceof XYPlot), IllegalArgumentException.class,
                "AbstractSpaceTimePlot can only work with XYPlot.");
        XYPlot xyPlot = chart.getXYPlot();
        // axis listeners to enable/disable auto zoom
        xyPlot.getDomainAxis().addChangeListener(new AxisChangeListener()
        {
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public void axisChanged(final AxisChangeEvent event)
            {
                if (!XAbstractSpaceTimePlot.this.virtualAutoBounds)
                {
                    // the axis was changed, but not by a command from this class, auto bounds should be disabled
                    XAbstractSpaceTimePlot.this.autoBoundAxes = false;
                }
            }
        });
        updateAutoBound(xyPlot);
        super.setChart(chart);
    }

    /** {@inheritDoc} */
    @Override
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        if (getChart().getPlot() instanceof XYPlot)
        {
            JCheckBoxMenuItem fixedDomainCheckBox = new JCheckBoxMenuItem("Fix time range", false);
            fixedDomainCheckBox.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    boolean fix = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                    XAbstractSpaceTimePlot.this.fixedDomainRange =
                            fix ? getChart().getXYPlot().getDomainAxis().getRange().getLength() : null;
                    notifyPlotChange();
                }
            });
            popupMenu.insert(fixedDomainCheckBox, 0);
        }
        popupMenu.insert(new JPopupMenu.Separator(), 1);
    }

    /** {@inheritDoc} */
    @Override
    protected void update()
    {
        if (this.autoBoundAxes && getChart() != null) // null during construction
        {
            updateAutoBound(getChart().getPlot());
        }
        super.update();
    }

    /**
     * Sets the auto bounds without deactivating auto bounds through the axis change listener.
     * @param plot Plot; plot with default zoom-all bounds set
     */
    private void updateAutoBound(final Plot plot)
    {
        // disables the axis change listener from registering a user input that is actually an update of bounds as the time
        // increases
        this.virtualAutoBounds = true;
        setAutoBoundsDomain(plot);
        setAutoBoundRange(plot);
        this.virtualAutoBounds = false;
    }

    /** {@inheritDoc} */
    @Override
    protected final void applyAutoBoundDomain(final Plot plot)
    {
        XAbstractSpaceTimePlot.this.autoBoundAxes = true;
        setAutoBoundsDomain(plot);
    }

    /** {@inheritDoc} */
    @Override
    protected final void applyAutoBoundRange(final Plot plot)
    {
        XAbstractSpaceTimePlot.this.autoBoundAxes = true;
        setAutoBoundRange(plot);
    }

    /**
     * Sets the auto domain bounds.
     * @param plot Plot; plot
     */
    private void setAutoBoundsDomain(final Plot plot)
    {
        double upper = this.initialEnd.si > getUpdateTime().si ? this.initialEnd.si : getUpdateTime().si;
        double lower = this.fixedDomainRange == null ? 0.0 : Math.max(upper - this.fixedDomainRange, 0.0);
        ((XYPlot) plot).getDomainAxis().setRange(lower, upper);
    }

    /**
     * Sets the auto range bounds.
     * @param plot Plot; plot
     */
    private void setAutoBoundRange(final Plot plot)
    {
        ((XYPlot) plot).getRangeAxis().setRange(0.0, getEndLocation().si);
    }

    /**
     * Returns the total path length.
     * @return Length; total path length
     */
    protected abstract Length getEndLocation();

}
