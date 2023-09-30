package org.opentrafficsim.swing.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.opentrafficsim.draw.graphs.AbstractContourPlot;
import org.opentrafficsim.draw.graphs.ContourDataSource.Dimension;

/**
 * Embed a ContourPlot in a Swing JPanel.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SwingContourPlot extends SwingSpaceTimePlot
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

    /**
     * Create a new SwingContourPlot with embedded plot.
     * @param plot AbstractContourPlot&lt;?&gt;; the plot to embed
     */
    public SwingContourPlot(final AbstractContourPlot<?> plot)
    {
        super(plot);
    }

    /** {@inheritDoc} */
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
        this.smoothCheckBox.addActionListener(new ActionListener()
        {
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                getPlot().getDataPool().setSmooth(((JCheckBoxMenuItem) e.getSource()).isSelected());
                getPlot().notifyPlotChange();
            }
        });
        popupMenu.insert(this.smoothCheckBox, 2);
        this.interpolateCheckBox = new JCheckBoxMenuItem("Bilinear interpolation", true);
        this.interpolateCheckBox.addActionListener(new ActionListener()
        {
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                boolean interpolate = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                getPlot().getBlockRenderer().setInterpolate(interpolate);
                getPlot().getDataPool().setInterpolate(interpolate);
                getPlot().notifyPlotChange();
            }
        });
        popupMenu.insert(this.interpolateCheckBox, 3);
    }

    /**
     * Create a JMenu to let the user set the granularity.
     * @param menuName String; caption for the new JMenu
     * @param format1 String; format string for the values in the items under the new JMenu, below formatValue
     * @param formatValue double; format value
     * @param format2 String; format string for the values in the items under the new JMenu, above and equal to formatValue
     * @param command String; prefix for the actionCommand of the items under the new JMenu
     * @param values double[]; array of values to be formatted using the format strings to yield the items under the new JMenu
     * @param initialValue double; the currently selected value (used to put the bullet on the correct item)
     * @param granularityButtons Map&lt;JRadioButtonMenuItem, Double&gt;; map in to which buttons should be added
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
            item.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent actionEvent)
                {
                    if (command.equalsIgnoreCase("setSpaceGranularity"))
                    {
                        double granularity = SwingContourPlot.this.spaceGranularityButtons.get(actionEvent.getSource());
                        getPlot().getDataPool().setGranularity(Dimension.DISTANCE, granularity);
                    }
                    else if (command.equalsIgnoreCase("setTimeGranularity"))
                    {
                        double granularity = SwingContourPlot.this.timeGranularityButtons.get(actionEvent.getSource());
                        getPlot().getDataPool().setGranularity(Dimension.TIME, granularity);
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

    /** {@inheritDoc} */
    @Override
    public AbstractContourPlot<?> getPlot()
    {
        return (AbstractContourPlot<?>) super.getPlot();
    }

    /**
     * Sets the correct space granularity radio button to selected. This is done from a {@code DataPool} to keep multiple plots
     * consistent.
     * @param granularity double; space granularity
     */
    protected final void setSpaceGranularityRadioButton(final double granularity)
    {
        getPlot().setSpaceGranularity(granularity);
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
        getPlot().setTimeGranularity(granularity);
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
        getPlot().setInterpolation(interpolate);
        this.interpolateCheckBox.setSelected(interpolate);
    }

}
