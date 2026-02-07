package org.opentrafficsim.swing.graphs;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.draw.graphs.AbstractContourPlot;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourDataSource.Dimension;

/**
 * Embed a ContourPlot in a Swing JPanel.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SwingContourPlot extends SwingSpaceTimePlot implements EventListener
{
    /** */
    private static final long serialVersionUID = 20190823L;

    /** Map to set time granularity. */
    private Map<JRadioButtonMenuItem, Double> timeGranularityButtons;

    /** Map to set space granularity. */
    private Map<JRadioButtonMenuItem, Double> spaceGranularityButtons;

    /** Check box for smoothing. */
    private JCheckBoxMenuItem smoothCheckBox;

    /** Check box for interpolation. */
    private JCheckBoxMenuItem interpolateCheckBox;

    /** Whether to ignore events as this is itself the plot causing change of granularity/interpolate/smooth setting. */
    private boolean ignoreEvent = false;

    /**
     * Create a new SwingContourPlot with embedded plot.
     * @param plot the plot to embed
     */
    public SwingContourPlot(final AbstractContourPlot<?> plot)
    {
        super(plot);
        plot.getDataPool().addListener(this, ContourDataSource.GRANULARITY);
        plot.getDataPool().addListener(this, ContourDataSource.INTERPOLATE);
        plot.getDataPool().addListener(this, ContourDataSource.SMOOTH);
    }

    @Override
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        this.timeGranularityButtons = new LinkedHashMap<>();
        this.spaceGranularityButtons = new LinkedHashMap<>();
        super.addPopUpMenuItems(popupMenu);
        JMenu spaceGranularityMenu = buildMenu("Distance granularity", "%.0f m", 1000, "%.0f km", "setSpaceGranularity",
                getPlot().getDataPool().getGranularities(Dimension.DISTANCE),
                getPlot().getDataPool().getGranularity(Dimension.DISTANCE), this.spaceGranularityButtons);
        popupMenu.insert(spaceGranularityMenu, 0);
        JMenu timeGranularityMenu = buildMenu("Time granularity", "%.0f s", 60.0, "%.0f min", "setTimeGranularity",
                getPlot().getDataPool().getGranularities(Dimension.TIME),
                getPlot().getDataPool().getGranularity(Dimension.TIME), this.timeGranularityButtons);
        popupMenu.insert(timeGranularityMenu, 1);
        this.smoothCheckBox = new JCheckBoxMenuItem("Adaptive smoothing method", false);
        this.smoothCheckBox.addActionListener((e) ->
        {
            SwingContourPlot.this.ignoreEvent = true;
            getPlot().getDataPool().setSmooth(((JCheckBoxMenuItem) e.getSource()).isSelected());
            getPlot().notifyPlotChange();
            SwingContourPlot.this.ignoreEvent = false;
        });
        popupMenu.insert(this.smoothCheckBox, 2);
        this.interpolateCheckBox = new JCheckBoxMenuItem("Bilinear interpolation", true);
        this.interpolateCheckBox.addActionListener((e) ->
        {
            SwingContourPlot.this.ignoreEvent = true;
            boolean interpolate = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            getPlot().getBlockRenderer().setInterpolate(interpolate);
            getPlot().getDataPool().setInterpolate(interpolate);
            getPlot().notifyPlotChange();
            SwingContourPlot.this.ignoreEvent = false;
        });
        popupMenu.insert(this.interpolateCheckBox, 3);
    }

    /**
     * Create a JMenu to let the user set the granularity.
     * @param menuName caption for the new JMenu
     * @param format1 format string for the values in the items under the new JMenu, below formatValue
     * @param formatValue format value
     * @param format2 format string for the values in the items under the new JMenu, above and equal to formatValue
     * @param command prefix for the actionCommand of the items under the new JMenu
     * @param values array of values to be formatted using the format strings to yield the items under the new JMenu
     * @param initialValue the currently selected value (used to put the bullet on the correct item)
     * @param granularityButtons map in to which buttons should be added
     * @return JMenu with JRadioMenuItems for the values and a bullet on the currentValue item
     */
    private JMenu buildMenu(final String menuName, final String format1, final double formatValue, final String format2,
            final String command, final double[] values, final double initialValue,
            final Map<JRadioButtonMenuItem, Double> granularityButtons)
    {
        JMenu result = new JMenu(menuName);
        ButtonGroup group = new ButtonGroup();
        for (double value : values)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                    String.format(value < formatValue ? format1 : format2, value < formatValue ? value : value / formatValue));
            granularityButtons.put(item, value);
            item.setSelected(value == initialValue);
            item.setActionCommand(command);
            item.addActionListener((actionEvent) ->
            {
                SwingContourPlot.this.ignoreEvent = true;
                if (command.equalsIgnoreCase("setSpaceGranularity"))
                {
                    double granularity = SwingContourPlot.this.spaceGranularityButtons.get(actionEvent.getSource());
                    // offer instead of setting as data pool may be working in the background using the granularity
                    getPlot().getDataPool().offerGranularity(Dimension.DISTANCE, granularity);
                }
                else if (command.equalsIgnoreCase("setTimeGranularity"))
                {
                    double granularity = SwingContourPlot.this.timeGranularityButtons.get(actionEvent.getSource());
                    // offer instead of setting as data pool may be working in the background using the granularity
                    getPlot().getDataPool().offerGranularity(Dimension.TIME, granularity);
                }
                else
                {
                    throw new OtsRuntimeException("Unknown ActionEvent");
                }
                SwingContourPlot.this.ignoreEvent = false;
            });
            result.add(item);
            group.add(item);
        }
        return result;
    }

    @Override
    public AbstractContourPlot<?> getPlot()
    {
        return (AbstractContourPlot<?>) super.getPlot();
    }

    @Override
    public void notify(final Event event)
    {
        if (this.ignoreEvent)
        {
            return;
        }
        if (event.getType().equals(ContourDataSource.GRANULARITY))
        {
            Object[] payload = (Object[]) event.getContent();
            Dimension dimension = (Dimension) payload[0];
            double granularity = (double) payload[1];
            Map<JRadioButtonMenuItem, Double> buttonMap =
                    Dimension.DISTANCE.equals(dimension) ? this.spaceGranularityButtons : this.timeGranularityButtons;
            for (JRadioButtonMenuItem button : buttonMap.keySet())
            {
                button.setSelected(Math.abs(buttonMap.get(button) - granularity) < 0.001);
            }
        }
        else if (event.getType().equals(ContourDataSource.INTERPOLATE))
        {
            this.interpolateCheckBox.setSelected((boolean) event.getContent());
        }
        else if (event.getType().equals(ContourDataSource.SMOOTH))
        {
            this.smoothCheckBox.setSelected((boolean) event.getContent());
        }
    }

}
