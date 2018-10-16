package org.opentrafficsim.graphs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DomainOrder;
import org.opentrafficsim.graphs.XContourDataPool.ContourDataType;
import org.opentrafficsim.graphs.XContourDataPool.Dimension;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

import nl.tudelft.simulation.language.Throw;

/**
 * Class for contour plots. The data that is plotted is stored in a {@code ContourDataPool}, which may be shared among several
 * contour plots along the same path. This abstract class takes care of the interactions between the plot and the data pool. Sub
 * classes only need to specify a few plot specific variables and functionalities.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <Z> z-value type
 */
public abstract class XAbstractContourPlot<Z extends Number> extends XAbstractSamplerPlot implements XXYInterpolatedDataset
{

    /** */
    private static final long serialVersionUID = 20181004L;

    /** Color scale for the graph. */
    private final XBoundsPaintScale paintScale;

    /** Difference of successive values in the legend. */
    private final Z legendStep;

    /** Format string used to create the captions in the legend. */
    private final String legendFormat;

    /** Format string used to create status label (under the mouse). */
    private final String valueFormat;

    /** Data pool. */
    private final XContourDataPool dataPool;

    /** Map to set time granularity. */
    private Map<JRadioButtonMenuItem, Double> timeGranularityButtons = new LinkedHashMap<>();

    /** Map to set space granularity. */
    private Map<JRadioButtonMenuItem, Double> spaceGranularityButtons = new LinkedHashMap<>();

    /** Check box for smoothing. */
    private JCheckBoxMenuItem smoothCheckBox;

    /** Check box for interpolation. */
    private JCheckBoxMenuItem interpolateCheckBox;

    /** Block renderer in chart. */
    private XXYInterpolatedBlockRenderer blockRenderer = null;

    /**
     * Constructor with specified paint scale.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataPool; data pool
     * @param paintScale BoundsPaintScale; paint scale
     * @param legendStep Z; increment between color legend entries
     * @param legendFormat String; format string for the captions in the color legend
     * @param valueFormat String; format string used to create status label (under the mouse)
     */
    public XAbstractContourPlot(final String caption, final OTSSimulatorInterface simulator, final XContourDataPool dataPool,
            final XBoundsPaintScale paintScale, final Z legendStep, final String legendFormat, final String valueFormat)
    {
        super(caption, dataPool.getUpdateInterval(), simulator, dataPool.getSampler(), dataPool.getPath(), dataPool.getDelay());
        dataPool.registerContourPlot(this);
        this.dataPool = dataPool;
        this.paintScale = paintScale;
        this.legendStep = legendStep;
        this.legendFormat = legendFormat;
        this.valueFormat = valueFormat;
        this.blockRenderer = new XXYInterpolatedBlockRenderer(this);
        this.blockRenderer.setPaintScale(this.paintScale);
        this.blockRenderer.setBlockHeight(dataPool.getGranularity(Dimension.DISTANCE));
        this.blockRenderer.setBlockWidth(dataPool.getGranularity(Dimension.TIME));
        setChart(createChart());
    }

    /**
     * Constructor with default paint scale.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataPool; data pool
     * @param legendStep Z; increment between color legend entries
     * @param legendFormat String; format string for the captions in the color legend
     * @param minValue Z; minimum value
     * @param maxValue Z; maximum value
     * @param valueFormat String; format string used to create status label (under the mouse)
     */
    @SuppressWarnings("parameternumber")
    public XAbstractContourPlot(final String caption, final OTSSimulatorInterface simulator, final XContourDataPool dataPool,
            final Z legendStep, final String legendFormat, final Z minValue, final Z maxValue, final String valueFormat)
    {
        this(caption, simulator, dataPool, createPaintScale(minValue, maxValue), legendStep, legendFormat, valueFormat);
    }

    /**
     * Creates a default paint scale from red, via yellow to green.
     * @param minValue Number; minimum value
     * @param maxValue Number; maximum value
     * @return BoundsPaintScale; default paint scale
     */
    private static XBoundsPaintScale createPaintScale(final Number minValue, final Number maxValue)
    {
        Throw.when(minValue.doubleValue() >= maxValue.doubleValue(), IllegalArgumentException.class,
                "Minimum value %s is below or equal to maxumum value %s.", minValue, maxValue);
        double[] boundaries =
                { minValue.doubleValue(), (minValue.doubleValue() + maxValue.doubleValue()) / 2.0, maxValue.doubleValue() };
        Color[] colorValues = { Color.RED, Color.YELLOW, Color.GREEN };
        return new XBoundsPaintScale(boundaries, colorValues);
    }

    /**
     * Create a chart.
     * @return JFreeChart; chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis("\u2192 " + "Time [s]");
        NumberAxis yAxis = new NumberAxis("\u2192 " + "Distance [m]");
        XYPlot plot = new XYPlot(this, xAxis, yAxis, this.blockRenderer);
        LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0;; i++)
        {
            double value = this.paintScale.getLowerBound() + i * this.legendStep.doubleValue();
            if (value > this.paintScale.getUpperBound() + 1e-6)
            {
                break;
            }
            legend.add(new LegendItem(String.format(this.legendFormat, scale(value)), this.paintScale.getPaint(value)));
        }
        legend.add(new LegendItem("No data", Color.BLACK));
        plot.setFixedLegendItems(legend);
        final JFreeChart chart = new JFreeChart(getCaption(), plot);
        return chart;
    }

    /** {@inheritDoc} */
    @Override
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        super.addPopUpMenuItems(popupMenu);
        JMenu spaceGranularityMenu = buildMenu("Distance granularity", "%.0f m", "setSpaceGranularity",
                this.dataPool.getGranularities(Dimension.DISTANCE), this.dataPool.getGranularity(Dimension.DISTANCE),
                this.spaceGranularityButtons);
        popupMenu.insert(spaceGranularityMenu, 0);
        JMenu timeGranularityMenu =
                buildMenu("Time granularity", "%.0f s", "setTimeGranularity", this.dataPool.getGranularities(Dimension.TIME),
                        this.dataPool.getGranularity(Dimension.TIME), this.timeGranularityButtons);
        popupMenu.insert(timeGranularityMenu, 1);
        this.smoothCheckBox = new JCheckBoxMenuItem("Adaptive smoothing method", false);
        this.smoothCheckBox.addActionListener(new ActionListener()
        {
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                XAbstractContourPlot.this.dataPool.setSmooth(((JCheckBoxMenuItem) e.getSource()).isSelected());
                notifyPlotChange();
            }
        });
        popupMenu.insert(this.smoothCheckBox, 2);
        this.interpolateCheckBox = new JCheckBoxMenuItem("Bilinear interpolation", true);
        this.interpolateCheckBox.addActionListener(new ActionListener()
        {
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                boolean interpolate = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                XAbstractContourPlot.this.blockRenderer.setInterpolate(interpolate);
                XAbstractContourPlot.this.dataPool.setInterpolate(interpolate);
                notifyPlotChange();
            }
        });
        popupMenu.insert(this.interpolateCheckBox, 3);
    }

    /**
     * Create a JMenu to let the user set the granularity.
     * @param menuName String; caption for the new JMenu
     * @param format String; format string for the values in the items under the new JMenu
     * @param command String; prefix for the actionCommand of the items under the new JMenu
     * @param values double[]; array of values to be formatted using the format strings to yield the items under the new JMenu
     * @param initialValue double; the currently selected value (used to put the bullet on the correct item)
     * @param granularityButtons Map; map in to which buttons should be added
     * @return JMenu with JRadioMenuItems for the values and a bullet on the currentValue item
     */
    private JMenu buildMenu(final String menuName, final String format, final String command, final double[] values,
            final double initialValue, final Map<JRadioButtonMenuItem, Double> granularityButtons)
    {
        JMenu result = new JMenu(menuName);
        ButtonGroup group = new ButtonGroup();
        for (double value : values)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, value));
            granularityButtons.put(item, value);
            item.setSelected(value == initialValue);
            item.setActionCommand(command);
            item.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public void actionPerformed(final ActionEvent actionEvent)
                {
                    if (command.equalsIgnoreCase("setSpaceGranularity"))
                    {
                        double granularity = XAbstractContourPlot.this.spaceGranularityButtons.get(actionEvent.getSource());
                        //XAbstractContourPlot.this.blockRenderer.setBlockHeight(granularity);
                        XAbstractContourPlot.this.dataPool.setGranularity(Dimension.DISTANCE, granularity);
                    }
                    else if (command.equalsIgnoreCase("setTimeGranularity"))
                    {
                        double granularity = XAbstractContourPlot.this.timeGranularityButtons.get(actionEvent.getSource());
                        //XAbstractContourPlot.this.blockRenderer.setBlockWidth(granularity);
                        XAbstractContourPlot.this.dataPool.setGranularity(Dimension.TIME, granularity);
                    }
                    else
                    {
                        throw new RuntimeException("Unknown ActionEvent");
                    }
                }
            });
            result.add(item);
            group.add(item);
        }
        return result;
    }

    /**
     * Sets the correct space granularity radio button to selected. This is done from a {@code DataPool} to keep multiple plots
     * consistent.
     * @param granularity double; space granularity
     */
    protected final void setSpaceGranularityRadioButton(final double granularity)
    {
        this.blockRenderer.setBlockHeight(granularity);
        for (JRadioButtonMenuItem button : this.spaceGranularityButtons.keySet())
        {
            button.setSelected(this.spaceGranularityButtons.get(button) == granularity);
        }
    }

    /**
     * Sets the correct time granularity radio button to selected. This is done from a {@code DataPool} to keep multiple plots
     * consistent.
     * @param granularity double; time granularity
     */
    protected final void setTimeGranularityRadioButton(final double granularity)
    {
        this.blockRenderer.setBlockWidth(granularity);
        for (JRadioButtonMenuItem button : this.timeGranularityButtons.keySet())
        {
            button.setSelected(this.timeGranularityButtons.get(button) == granularity);
        }
    }

    /**
     * Sets the check box for smooth rendering. This is done from a {@code DataPool} to keep multiple plots consistent.
     * @param smooth boolean; selected or not
     */
    protected final void setSmoothing(final boolean smooth)
    {
        this.smoothCheckBox.setSelected(smooth);
    }

    /**
     * Sets the check box for interpolated rendering and block renderer setting. This is done from a {@code DataPool} to keep
     * multiple plots consistent.
     * @param interpolate boolean; selected or not
     */
    protected final void setInterpolation(final boolean interpolate)
    {
        this.blockRenderer.setInterpolate(interpolate);
        this.interpolateCheckBox.setSelected(interpolate);
    }

    /**
     * Returns the data pool for sub classes.
     * @return ContourDataPool; data pool for subclasses
     */
    protected final XContourDataPool getDataPool()
    {
        return this.dataPool;
    }

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        return this.dataPool.getBinCount(Dimension.DISTANCE) * this.dataPool.getBinCount(Dimension.TIME);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getXValue(final int series, final int item)
    {
        return this.dataPool.getAxisValue(Dimension.TIME, item);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getYValue(final int series, final int item)
    {
        return this.dataPool.getAxisValue(Dimension.DISTANCE, item);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getZ(final int series, final int item)
    {
        return getZValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final Comparable<String> getSeriesKey(final int series)
    {
        return getCaption();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public final int indexOf(final Comparable seriesKey)
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public final DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /** {@inheritDoc} */
    @Override
    public final double getZValue(final int series, final int item)
    {
        // default 1 series
        return getValue(item, this.dataPool.getGranularity(Dimension.DISTANCE), this.dataPool.getGranularity(Dimension.TIME));
    }

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        return 1; // default
    }

    /** {@inheritDoc} */
    @Override
    public int getRangeBinCount()
    {
        return this.dataPool.getBinCount(Dimension.DISTANCE);
    }

    /**
     * Returns the status label when the mouse is over the given location.
     * @param domainValue double; domain value (x-axis)
     * @param rangeValue double; range value (y-axis)
     * @return String; status label when the mouse is over the given location
     */
    protected final String getStatusLabel(final double domainValue, final double rangeValue)
    {
        if (this.dataPool == null)
        {
            return String.format("time %.0fs, distance %.0fm", domainValue, rangeValue);
        }
        int i = this.dataPool.getAxisBin(Dimension.DISTANCE, rangeValue);
        int j = this.dataPool.getAxisBin(Dimension.TIME, domainValue);
        int item = j * this.dataPool.getBinCount(Dimension.DISTANCE) + i;
        double zValue = scale(
                getValue(item, this.dataPool.getGranularity(Dimension.DISTANCE), this.dataPool.getGranularity(Dimension.TIME)));
        return String.format("time %.0fs, distance %.0fm, " + this.valueFormat, domainValue, rangeValue, zValue);
    }

    /** {@inheritDoc} */
    @Override
    protected final void increaseTime(final Time time)
    {
        if (this.dataPool != null) // dataPool is null at construction
        {
            this.dataPool.increaseTime(time);
        }
    }

    /**
     * Obtain value for cell from the data pool.
     * @param item int; item number
     * @param cellLength double; cell length
     * @param cellSpan double; cell duration
     * @return double; value for cell from the data pool
     */
    protected abstract double getValue(int item, double cellLength, double cellSpan);

    /**
     * Scale the value from SI to the desired unit for users.
     * @param si double; SI value
     * @return double; scaled value
     */
    protected abstract double scale(double si);

    /**
     * Returns the contour data type for use in a {@code ContourDataPool}.
     * @return CountorDataType; contour data type
     */
    protected abstract ContourDataType<Z> getContourDataType();

}
