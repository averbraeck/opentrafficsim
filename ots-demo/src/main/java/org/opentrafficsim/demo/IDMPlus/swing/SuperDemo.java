package org.opentrafficsim.demo.IDMPlus.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.locale.DefaultLocale;
import org.opentrafficsim.gui.LabeledPanel;
import org.opentrafficsim.gui.ProbabilityDistributionEditor;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.IncompatiblePropertyException;
import org.opentrafficsim.simulationengine.IntegerProperty;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimulatorFrame;
import org.opentrafficsim.simulationengine.WrappableSimulation;

/**
 * Several demos in one application.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SuperDemo
{
    /**
     * Start the application.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new SimulatorFrame("Open Traffic Simulator Demonstrations", new SuperDemo().buildGUI());
            }
        });
    }

    /**
     * Build the GUI.
     * @return JPanel; the JPanel that holds the application.=
     */
    public JPanel buildGUI()
    {
        final JPanel mainPanel = new JPanel(new BorderLayout());
        // Ensure that the window does not shrink into (almost) nothingness when un-maximized
        mainPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        final ArrayList<WrappableSimulation> demonstrations = new ArrayList<WrappableSimulation>();
        demonstrations.add(new ContourPlots());
        demonstrations.add(new Trajectories());
        demonstrations.add(new FundamentalDiagrams());
        demonstrations.add(new CircularLane());
        demonstrations.add(new CircularRoad());
        final JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        final JLabel description = new JLabel();
        final JPanel propertyPanel = new JPanel();
        propertyPanel.setLayout(new BoxLayout(propertyPanel, BoxLayout.Y_AXIS));
        mainPanel.add(description, BorderLayout.CENTER);
        final JButton startButton = new JButton("Start simulation");
        ButtonGroup buttonGroup = new ButtonGroup();
        for (final WrappableSimulation demo : demonstrations)
        {
            CleverRadioButton button = new CleverRadioButton(demo);
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    description.setText(demo.description());
                    startButton.setEnabled(true);
                    propertyPanel.removeAll();
                    for (AbstractProperty<?> p : demo.getProperties())
                    {
                        if (p instanceof SelectionProperty)
                        {
                            JPanel panel = new JPanel();
                            panel.setLayout(new BorderLayout());
                            final SelectionProperty sp = (SelectionProperty) p;
                            JComboBox<String> comboBox = new JComboBox<String>(sp.getOptionNames());
                            comboBox.setSelectedItem(sp.getValue());
                            comboBox.setToolTipText(sp.getDescription());
                            comboBox.addItemListener(new ItemListener()
                            {
                                @Override
                                public void itemStateChanged(ItemEvent itemEvent)
                                {
                                    if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                                    {
                                        String itemText = (String) itemEvent.getItem();
                                        try
                                        {
                                            sp.setValue(itemText);
                                        }
                                        catch (IncompatiblePropertyException exception)
                                        {
                                            exception.printStackTrace();
                                        }
                                    }
                                }
                            });
                            panel.setToolTipText(sp.getDescription());
                            panel.add(new JLabel(p.getShortName() + ": "), BorderLayout.LINE_START);
                            panel.add(comboBox, BorderLayout.CENTER);
                            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) comboBox.getPreferredSize()
                                    .getHeight()));
                            propertyPanel.add(panel);
                        }
                        else if (p instanceof ProbabilityDistributionProperty)
                        {
                            LabeledPanel lp = new LabeledPanel(p.getShortName());
                            lp.setLayout(new BorderLayout());
                            final ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) p;
                            final ProbabilityDistributionEditor pdpe =
                                    new ProbabilityDistributionEditor(pdp.getElementNames(), pdp.getValue());
                            pdpe.addPropertyChangeListener(new PropertyChangeListener()
                            {
                                @Override
                                public void propertyChange(PropertyChangeEvent arg0)
                                {
                                    try
                                    {
                                        pdp.setValue(pdpe.getProbabilities());
                                    }
                                    catch (IncompatiblePropertyException exception)
                                    {
                                        exception.printStackTrace();
                                    }
                                }

                            });
                            lp.add(pdpe, BorderLayout.CENTER);
                            lp.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) new JLabel("ABC")
                                    .getPreferredSize().getHeight()));
                            lp.setToolTipText(pdp.getDescription());
                            propertyPanel.add(lp);
                        }
                        else if (p instanceof IntegerProperty)
                        {
                            final IntegerProperty ip = (IntegerProperty) p;
                            LabeledPanel lp = new LabeledPanel(p.getShortName());
                            lp.setLayout(new BorderLayout());
                            final JSlider slider = new JSlider();
                            slider.setMaximum(ip.getMaximumValue());
                            slider.setMinimum(ip.getMinimumValue());
                            slider.setValue(ip.getValue());
                            slider.setPaintTicks(true);
                            final JLabel currentValue =
                                    new JLabel(String.format(DefaultLocale.getLocale(), "Track length: %dm",
                                            ip.getValue()), SwingConstants.RIGHT);
                            slider.addChangeListener(new ChangeListener()
                            {
                                @Override
                                public void stateChanged(ChangeEvent changeEvent)
                                {
                                    int value = slider.getValue();
                                    currentValue.setText(String.format(DefaultLocale.getLocale(), "Track length: %dm",
                                            value));
                                    if (slider.getValueIsAdjusting())
                                    {
                                        return;
                                    }
                                    try
                                    {
                                        ip.setValue(value);
                                    }
                                    catch (IncompatiblePropertyException exception)
                                    {
                                        exception.printStackTrace();
                                    }
                                }
                            });
                            lp.setToolTipText(p.getDescription());
                            lp.add(slider, BorderLayout.CENTER);
                            lp.add(currentValue, BorderLayout.SOUTH);
                            lp.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) slider.getPreferredSize()
                                    .getHeight()));
                            propertyPanel.add(lp);
                        }
                        else
                        {
                            throw new Error("Unhandled property: " + p.getDescription());
                        }
                    }
                    propertyPanel.add(Box.createVerticalGlue());
                }
            });
            buttonGroup.add(button);
            left.add(button);
        }
        mainPanel.add(left, BorderLayout.LINE_START);
        startButton.setEnabled(false);
        startButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                WrappableSimulation simulation = null;
                for (Component c : left.getComponents())
                {
                    if (c instanceof CleverRadioButton)
                    {
                        CleverRadioButton crb = (CleverRadioButton) c;
                        if (crb.isSelected())
                        {
                            simulation = crb.getSimulation();
                        }
                    }
                }
                if (null == simulation)
                {
                    throw new Error("Cannot find a selected button");
                }
                // Clear out the main panel and put the selected simulator in it.
                mainPanel.removeAll();
                try
                {
                    // TODO figure out a way to get the properties to what builsDimulator builds
                    mainPanel.add(simulation.buildSimulator().getPanel(), BorderLayout.CENTER);
                }
                catch (RemoteException | SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
        left.add(startButton);
        left.add(propertyPanel);
        return mainPanel;
    }

}

/** JRadioButton that also stores a WrappableSimulation. */
class CleverRadioButton extends JRadioButton
{
    /** */
    private static final long serialVersionUID = 20141217L;

    /** The WrappableSimulation. */
    final WrappableSimulation simulation;

    /**
     * Construct a JRadioButton that also stores a WrappableSimulation.
     * @param simulation
     */
    CleverRadioButton(WrappableSimulation simulation)
    {
        super(simulation.shortName());
        this.simulation = simulation;
    }

    /**
     * Retrieve the simulation.
     * @return WrappableSimulation
     */
    public final WrappableSimulation getSimulation()
    {
        return this.simulation;
    }
}
