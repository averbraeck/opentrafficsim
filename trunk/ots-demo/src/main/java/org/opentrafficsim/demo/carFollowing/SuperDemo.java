package org.opentrafficsim.demo.carFollowing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.locale.DefaultLocale;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.gui.LabeledPanel;
import org.opentrafficsim.gui.ProbabilityDistributionEditor;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.BooleanProperty;
import org.opentrafficsim.simulationengine.CompoundProperty;
import org.opentrafficsim.simulationengine.ContinuousProperty;
import org.opentrafficsim.simulationengine.IDMPropertySet;
import org.opentrafficsim.simulationengine.IntegerProperty;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.PropertyException;
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
    /** The JPanel that holds the user settable properties. */
    private JPanel propertyPanel;
    
    /** The JPanel that holds the simulation selection radio buttons. */
    private JPanel simulationSelection;

    /** Properties of the currently selected demonstration. */
    private ArrayList<AbstractProperty<?>> activeProperties = null;

    /**
     * Start the application.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JFrame frame = new SimulatorFrame("Open Traffic Simulator Demonstrations", new SuperDemo().buildGUI());
                frame.setExtendedState(frame.getExtendedState() & ~ Frame.MAXIMIZED_BOTH);
                // frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_VERT);
                // The code above does not work; the code below does work. Code found on
                // http://stackoverflow.com/questions/5195634/how-to-maximize-a-the-height-of-a-jframe
                Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
                int taskHeight=screenInsets.bottom;
                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setSize(d.width/2, d.height - taskHeight);
                frame.setLocation(0, 0);
            }
        });
    }

    /**
     * Build the GUI.
     * @return JPanel; the JPanel that holds the application
     */
    public final JPanel buildGUI()
    {
        final JPanel mainPanel = new JPanel(new BorderLayout());
        // Ensure that the window does not shrink into (almost) nothingness when un-maximized
        mainPanel.setPreferredSize(new Dimension(800, 800));
        final ArrayList<WrappableSimulation> demonstrations = new ArrayList<WrappableSimulation>();
        demonstrations.add(new Straight());
        demonstrations.add(new SequentialLanes());
        demonstrations.add(new CircularLane());
        demonstrations.add(new CircularRoad());
        demonstrations.add(new XMLNetworks());
        demonstrations.add(new OpenStreetMap());
        // final JPanel left = new LabeledPanel("Simulation Settings");
        this.simulationSelection = new LabeledPanel("Network");
        this.simulationSelection.setLayout(new GridBagLayout());
        GridBagConstraints gbcSimulation = new GridBagConstraints();
        gbcSimulation.gridx = 0;
        gbcSimulation.gridy = -1;
        gbcSimulation.anchor = GridBagConstraints.LINE_START;
        gbcSimulation.weighty = 0;
        gbcSimulation.fill = GridBagConstraints.HORIZONTAL;
        final JPanel left = new JPanel(new GridBagLayout());
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.anchor = GridBagConstraints.LINE_START;
        gbcLeft.weighty = 0;
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        final JLabel description = new JLabel("Please select a demonstration from the buttons on the left");
        this.propertyPanel = new JPanel();
        this.propertyPanel.setLayout(new BoxLayout(this.propertyPanel, BoxLayout.Y_AXIS));
        rebuildPropertyPanel(new ArrayList<AbstractProperty<?>>());
        final JPanel descriptionPanel = new LabeledPanel("Description");
        descriptionPanel.add(description);
        mainPanel.add(descriptionPanel, BorderLayout.CENTER);
        final JButton startButton = new JButton("Start simulation");
        ButtonGroup buttonGroup = new ButtonGroup();
        for (final WrappableSimulation demo : demonstrations)
        {
            CleverRadioButton button = new CleverRadioButton(demo);
            // button.setPreferredSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    // System.out.println("selected " + demo.shortName());
                    // Clear out the main panel and put the description of the selected simulator in it.
                    mainPanel.remove(((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER));
                    mainPanel.add(description, BorderLayout.CENTER);
                    description.setText(demo.description());
                    startButton.setEnabled(true);
                    startButton.setVisible(true);
                    rebuildPropertyPanel(demo.getProperties());
                }

            });
            buttonGroup.add(button);
            gbcSimulation.gridy++;
            this.simulationSelection.add(button, gbcSimulation);
        }
        JScrollPane scrollPane = new JScrollPane(left);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.LINE_START);
        startButton.setEnabled(false);
        startButton.setVisible(false);
        startButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                WrappableSimulation simulation = null;
                for (Component c : SuperDemo.this.simulationSelection.getComponents())
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
                /*
                // Clear out the main panel and put the selected simulator in it.
                mainPanel.remove(((BorderLayout) mainPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER));
                try
                {
                    mainPanel.add(simulation.buildSimulator(SuperDemo.this.activeProperties).getPanel(),
                            BorderLayout.CENTER);
                    
                }
                catch (RemoteException | SimRuntimeException | NetworkException | NamingException exception)
                {
                    exception.printStackTrace();
                }
                startButton.setEnabled(false);
                */
                // Create a new JFrame for the simulation.
                JFrame simulatorFrame = new JFrame();
                try
                {
                    simulatorFrame.add(simulation.buildSimulator(SuperDemo.this.activeProperties).getPanel());
                    simulatorFrame.setExtendedState(simulatorFrame.getExtendedState() | Frame.MAXIMIZED_BOTH);
                    simulatorFrame.pack();
                    simulatorFrame.setVisible(true);
                    simulatorFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                }
                catch (RemoteException | SimRuntimeException | NetworkException | NamingException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
        gbcLeft.gridy++;
        left.add(this.propertyPanel, gbcLeft);
        gbcLeft.gridy++;
        gbcLeft.weighty = 1;
        JPanel filler = new JPanel();
        filler.setMinimumSize(new Dimension(400, 0));
        filler.setPreferredSize(new Dimension(400, 0));
        left.add(filler, gbcLeft); // add a filler that also enforces a reasonable width
        gbcLeft.weighty = 0;
        gbcLeft.anchor = GridBagConstraints.SOUTH;
        left.add(startButton, gbcLeft);
        JPanel rightFiller = new JPanel();
        rightFiller.setPreferredSize(new Dimension((int) new JScrollBar().getPreferredSize().getWidth(), 0));
        gbcLeft.gridx = 2;
        left.add(rightFiller, gbcLeft);
        return mainPanel;
    }

    /**
     * Regenerate the contents of the propertyPanel.
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the demo-specific properties to display
     */
    final void rebuildPropertyPanel(final ArrayList<AbstractProperty<?>> properties)
    {
        this.propertyPanel.removeAll();
        try
        {
            CompoundProperty simulationSettings =
                    new CompoundProperty("Simulation settings",
                            "Select the simulation network and traffic composition", null, false, 0);
            /*
             * This is ugly, but it gets the job done... Insert a dummy property at the top and later replace the
             * property editor for the dummy property by the simulationSelection JPanel.
             */
            simulationSettings.add(new BooleanProperty("Dummy", "Dummy", false, false, 0));
            if (properties.size() > 0)
            {
                while (true)
                {
                    boolean movedAny = false;
                    // Move the properties that has display priority < 100 into the simulationSettings group.
                    for (AbstractProperty<?> ap : properties)
                    {
                        if (ap.getDisplayPriority() < 100)
                        {
                            // Move it into the simulationSettings group
                            simulationSettings.add(ap);
                            properties.remove(ap);
                            movedAny = true;
                            break;
                        }
                    }
                    if (!movedAny)
                    {
                        break;
                    }
                }
                simulationSettings.add(new ProbabilityDistributionProperty("Traffic composition",
                        "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"},
                        new Double[]{0.8, 0.2}, false, 5));
                CompoundProperty modelSelection =
                        new CompoundProperty("Model selection", "Modeling specific settings", null, false, 300);
                modelSelection.add(new SelectionProperty("Simulation scale", "Level of detail of the simulation",
                        new String[]{"Micro", "Macro", "Meta"}, 0, true, 0));
                modelSelection.add(new SelectionProperty("Car following model",
                        "<html>The car following model determines "
                                + "the acceleration that a vehicle will make taking into account "
                                + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                + "curvature of the road) capabilities of the vehicle and personality "
                                + "of the driver.</html>", new String[]{"IDM", "IDM+"}, 1, false, 1));
                modelSelection.add(IDMPropertySet.makeIDMPropertySet("Car", new DoubleScalar.Abs<AccelerationUnit>(1.0,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.5,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER),
                        new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 2));
                modelSelection.add(IDMPropertySet.makeIDMPropertySet("Truck", new DoubleScalar.Abs<AccelerationUnit>(
                        0.5, AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.25,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER),
                        new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 3));
                properties.add(properties.size() > 0 ? 1 : 0, modelSelection);
            }
            properties.add(0, simulationSettings);
        }
        catch (PropertyException exception)
        {
            exception.printStackTrace();
        }
        boolean fixedDummy = false;
        for (AbstractProperty<?> p : new CompoundProperty("", "", properties, false, 0).displayOrderedValue())
        {
            JPanel propertySubPanel = makePropertyEditor(p);
            if (!fixedDummy)
            {
                // Replace the dummy property editor by the simulationSelection JPanel.
                JPanel subPanel = (JPanel) propertySubPanel.getComponent(0);
                subPanel.removeAll();
                subPanel.add(this.simulationSelection);
                fixedDummy = true;
            }
            this.propertyPanel.add(propertySubPanel);
        }
        SuperDemo.this.activeProperties = properties;
    }

    /**
     * Create a graphical editor for an AbstractProperty.
     * @param ap AbstractProperty; the abstract property for which an editor must be created
     * @return JPanel
     */
    final JPanel makePropertyEditor(final AbstractProperty<?> ap)
    {
        JPanel result;
        if (ap instanceof SelectionProperty)
        {
            result = new JPanel();
            result.setLayout(new BorderLayout());
            final SelectionProperty sp = (SelectionProperty) ap;
            final JComboBox<String> comboBox = new JComboBox<String>(sp.getOptionNames());
            comboBox.setSelectedItem(sp.getValue());
            comboBox.setToolTipText(sp.getDescription());
            comboBox.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(final ItemEvent itemEvent)
                {
                    if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                    {
                        String itemText = (String) itemEvent.getItem();
                        try
                        {
                            sp.setValue(itemText);
                        }
                        catch (PropertyException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                }
            });
            if (ap.isReadOnly())
            {
                comboBox.removeItemListener(comboBox.getItemListeners()[0]);
                comboBox.addActionListener(new ActionListener()
                {

                    @Override
                    public void actionPerformed(final ActionEvent actionEvent)
                    {
                        if (comboBox.getSelectedIndex() != 0)
                        {
                            comboBox.setSelectedIndex(0);
                        }
                    }
                });
            }
            result.setToolTipText(sp.getDescription());
            result.add(new JLabel(ap.getShortName() + ": "), BorderLayout.LINE_START);
            result.add(comboBox, BorderLayout.CENTER);
            result.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) comboBox.getPreferredSize().getHeight()));
        }
        else if (ap instanceof ProbabilityDistributionProperty)
        {
            result = new LabeledPanel(ap.getShortName());
            result.setLayout(new BorderLayout());
            final ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) ap;
            final ProbabilityDistributionEditor pdpe =
                    new ProbabilityDistributionEditor(pdp.getElementNames(), pdp.getValue());
            pdpe.addPropertyChangeListener(new PropertyChangeListener()
            {
                @Override
                public void propertyChange(final PropertyChangeEvent arg0)
                {
                    try
                    {
                        pdp.setValue(pdpe.getProbabilities());
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                }

            });
            result.add(pdpe, BorderLayout.LINE_END);
            result.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) new JLabel("ABC").getPreferredSize()
                    .getHeight()));
            result.setToolTipText(pdp.getDescription());
        }
        else if (ap instanceof IntegerProperty)
        {
            final IntegerProperty ip = (IntegerProperty) ap;
            result = new LabeledPanel(ap.getShortName());
            result.setLayout(new BorderLayout());
            final JSlider slider = new JSlider();
            slider.setMaximum(ip.getMaximumValue());
            slider.setMinimum(ip.getMinimumValue());
            slider.setValue(ip.getValue());
            slider.setPaintTicks(true);
            final JLabel currentValue =
                    new JLabel(String.format(DefaultLocale.getLocale(), ip.getFormatString(), ip.getValue()),
                            SwingConstants.RIGHT);
            slider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(final ChangeEvent changeEvent)
                {
                    int value = slider.getValue();
                    currentValue.setText(String.format(DefaultLocale.getLocale(), ip.getFormatString(), value));
                    if (slider.getValueIsAdjusting())
                    {
                        return;
                    }
                    try
                    {
                        ip.setValue(value);
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            });
            result.setToolTipText(ap.getDescription());
            result.add(slider, BorderLayout.CENTER);
            result.add(currentValue, BorderLayout.SOUTH);
            result.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) slider.getPreferredSize().getHeight()));
        }
        else if (ap instanceof ContinuousProperty)
        {
            final ContinuousProperty cp = (ContinuousProperty) ap;
            result = new LabeledPanel(ap.getShortName());
            result.setLayout(new BorderLayout());
            final JSlider slider = new JSlider();
            final int useSteps = 1000;
            slider.setMaximum(useSteps);
            slider.setMinimum(0);
            slider.setValue((int) (useSteps * (cp.getValue() - cp.getMinimumValue()) / (cp.getMaximumValue() - cp
                    .getMinimumValue())));
            final JLabel currentValue =
                    new JLabel(String.format(DefaultLocale.getLocale(), cp.getFormatString(), cp.getValue()),
                            SwingConstants.RIGHT);
            slider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(final ChangeEvent changeEvent)
                {
                    double value =
                            slider.getValue() * (cp.getMaximumValue() - cp.getMinimumValue()) / useSteps
                                    + cp.getMinimumValue();
                    currentValue.setText(String.format(DefaultLocale.getLocale(), cp.getFormatString(), value));
                    if (slider.getValueIsAdjusting())
                    {
                        return;
                    }
                    try
                    {
                        cp.setValue(value);
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            });
            result.setToolTipText(ap.getDescription());
            result.add(slider, BorderLayout.CENTER);
            result.add(currentValue, BorderLayout.SOUTH);
            result.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) slider.getPreferredSize().getHeight() * 4));
        }
        else if (ap instanceof BooleanProperty)
        {
            final BooleanProperty bp = (BooleanProperty) ap;
            result = new JPanel(new BorderLayout());
            final JCheckBox checkBox = new JCheckBox(bp.getShortName(), bp.getValue());
            checkBox.setToolTipText(bp.getDescription());
            checkBox.setEnabled(!bp.isReadOnly());
            checkBox.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(final ChangeEvent arg0)
                {
                    try
                    {
                        bp.setValue(checkBox.isSelected());
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            });
            JPanel filler = new JPanel();
            filler.setPreferredSize(new Dimension(0, (int) checkBox.getPreferredSize().getHeight()));
            result.add(checkBox, BorderLayout.CENTER);
            result.add(filler, BorderLayout.LINE_END);
        }
        else if (ap instanceof CompoundProperty)
        {
            CompoundProperty cp = (CompoundProperty) ap;
            result = new LabeledPanel(ap.getShortName());
            result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
            for (AbstractProperty<?> subProperty : cp.displayOrderedValue())
            {
                result.add(makePropertyEditor(subProperty));
            }
        }
        else
        {
            throw new Error("Unhandled property: " + ap.getDescription());
        }
        return result;
    }
}

/** JRadioButton that also stores a WrappableSimulation. */
class CleverRadioButton extends JRadioButton
{
    /** */
    private static final long serialVersionUID = 20141217L;

    /** The WrappableSimulation. */
    private final WrappableSimulation simulation;

    /**
     * Construct a JRadioButton that also stores a WrappableSimulation.
     * @param simulation WrappableSimulation; the simulation to run if this radio button is selected and the start
     *            simulation button is clicked
     */
    CleverRadioButton(final WrappableSimulation simulation)
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
