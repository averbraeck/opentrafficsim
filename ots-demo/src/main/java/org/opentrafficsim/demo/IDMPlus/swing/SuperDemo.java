package org.opentrafficsim.demo.IDMPlus.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.IncompatiblePropertyException;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
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
     * Build the GUI.
     * @param args
     */
    public static void main(final String[] args)
    {
        final JPanel mainPanel = new JPanel(new BorderLayout());
        // Ensure that the window does not shrink into (almost) nothingness when un-maximized
        mainPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        final ArrayList<WrappableSimulation> demonstrations = new ArrayList<WrappableSimulation>();
        demonstrations.add(new ContourPlots());
        demonstrations.add(new Trajectories());
        demonstrations.add(new CircularLane());
        demonstrations.add(new CircularRoad());
        final JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        final JLabel description = new JLabel();
        final JPanel propertyPanel = new JPanel();
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
                        if (p instanceof ProbabilityDistributionProperty)
                        {
                            final ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) p;
                            final ProbabilityDistributionEditor pdpe =
                                    new ProbabilityDistributionEditor(pdp.getElementNames(), pdp.getValue());
                            pdpe.addPropertyChangeListener("probabilitiesChanged", new PropertyChangeListener()
                            {

                                @Override
                                public void propertyChange(PropertyChangeEvent arg0)
                                {
                                    //System.out.println("Received a PropertyChangeEvent: " + arg0);
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
                            propertyPanel.add(pdpe);
                        }
                    }
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
        /*
         * ProbabilityDistributionEditor carFollowingModels = new ProbabilityDistributionEditor(new String[]{"IDM",
         * "IDM+", "aaa"}); left.add(carFollowingModels); ProbabilityDistributionEditor carTypes = new
         * ProbabilityDistributionEditor(new String[]{"passenger car", "truck"}, new Double[]{0.8, 0.2});
         * left.add(carTypes);
         */
        new SimulatorFrame("Open Traffic Simulator Demonstrations", mainPanel);
    }

}

/**
 * Visual control that displays a set of probabilities that add up to 1. <br>
 * The user can drag the boundaries between the probabilities to change their values. The sum of the probabilities will
 * always add up to 1.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class ProbabilityDistributionEditor extends JPanel implements TableColumnModelListener, MouseListener
{
    /** */
    private static final long serialVersionUID = 20141217L;

    /** The JTable that does the visualization. */
    private JTable table;

    /** The names of the fields. */
    private String[] fieldNames;

    /** Set when columns are resized. */
    private boolean columnSizeChanged;

    /** Probabilities for each column */
    private double[] probabilities = null;

    /** Register for propertyChange event listeners */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Common initialization code.
     * @param theFieldNames String[]; the names of the fields.
     */
    private void initialize(final String[] theFieldNames)
    {
        this.fieldNames = theFieldNames;
        this.columnSizeChanged = false;
        Object[][] fields = {};
        this.table = new JTable(fields, theFieldNames);
        this.table.getTableHeader().setReorderingAllowed(false);
        for (int column = 0; column < theFieldNames.length; column++)
        {
            this.table.getColumn(theFieldNames[column]).setMinWidth(0);
        }
        this.table.getColumnModel().addColumnModelListener(this);
        this.table.getTableHeader().addMouseListener(this);
        super.add(new JScrollPane(this.table));
    }

    /**
     * Construct a new ProbabilityDistributionEditor with uniform probability distribution.
     * @param fieldNames String[]; the names of the fields.
     */
    ProbabilityDistributionEditor(final String[] fieldNames)
    {
        super();
        initialize(fieldNames);
        this.columnSizeChanged = true;
        reComputeFractions();
    }

    /**
     * Construct a new ProbabilityDistributionEditor with initial values for the probabilities.
     * @param fieldNames
     * @param doubles
     */
    ProbabilityDistributionEditor(final String[] fieldNames, final Double[] doubles)
    {
        super();
        initialize(fieldNames);
        TableColumnModel tcm = this.table.getTableHeader().getColumnModel();
        double totalWidth = tcm.getTotalColumnWidth();
        for (int column = 0; column < fieldNames.length; column++)
        {
            this.table.getColumn(fieldNames[column]).setPreferredWidth((int) (doubles[column] * totalWidth));
            this.table.getColumn(fieldNames[column]).setWidth((int) (doubles[column] * totalWidth));
        }
        this.columnSizeChanged = true;
        reComputeFractions();
    }
    
    /**
     * Retrieve the current probability values.
     * @return Double[]; thhe current probability values
     */
    public Double[] getProbabilities()
    {
        Double[] result = new Double[this.probabilities.length];
        for (int column = 0; column < this.probabilities.length; column++)
        {
            result[column] = this.probabilities[column];
        }
        return result;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        this.propertyChangeSupport.addPropertyChangeListener(pcl);
        System.out.println("Added property change listener: " + pcl);
    }
    
    @Override
    public void addPropertyChangeListener(String key, PropertyChangeListener pcl)
    {
        this.propertyChangeSupport.addPropertyChangeListener(key, pcl);
        System.out.println("Added property change listener for " + key + ": " + pcl);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl)
    {
        this.propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(String key, PropertyChangeListener pcl)
    {
        this.propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    /**
     * Retrieve the name of one of the fields.
     * @param index int; the index of the field to retrieve
     * @return String; the name of the indicated field
     */
    public final String getFieldName(int index)
    {
        return this.fieldNames[index];
    }

    /**
     * Retrieve the probability of one of the fields.
     * @param index int; the index of the field
     * @return double; the probability of the indicated field
     */
    public final double getProbability(int index)
    {
        return this.probabilities[index];
    }

    /** {@inheritDoc} */
    @Override
    public void columnAdded(TableColumnModelEvent e)
    {
        // Ignored -- cannot happen
    }

    /** {@inheritDoc} */
    @Override
    public void columnMarginChanged(ChangeEvent e)
    {
        this.columnSizeChanged = true;
    }

    /** {@inheritDoc} */
    @Override
    public void columnMoved(TableColumnModelEvent e)
    {
        // Ignored -- cannot happen
    }

    /** {@inheritDoc} */
    @Override
    public void columnRemoved(TableColumnModelEvent e)
    {
        // Ignored -- cannot happen
    }

    /** {@inheritDoc} */
    @Override
    public void columnSelectionChanged(ListSelectionEvent e)
    {
        // Ignored
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        // Ignored -- irrelevant
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(MouseEvent arg0)
    {
        // Ignored -- irrelevant
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(MouseEvent arg0)
    {
        // Ignored -- irrelevant
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(MouseEvent arg0)
    {
        // Ignored -- irrelevant
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        // This is the one we need
        if (this.columnSizeChanged)
        {
            reComputeFractions();
        }
    }

    /**
     * Re computer the probabilities and inform all propertyChangeListeners.
     */
    private void reComputeFractions()
    {
        TableColumnModel tcm = this.table.getTableHeader().getColumnModel();
        double totalWidth = tcm.getTotalColumnWidth();
        double[] newProbabilities = new double[tcm.getColumnCount()];
        for (int column = 0; column < newProbabilities.length; column++)
        {
            newProbabilities[column] = tcm.getColumn(column).getWidth() / totalWidth;
            System.out.println("probabilities[" + column + "]: " + newProbabilities[column]);
        }
        double[] oldProbabilities = this.probabilities;
        this.probabilities = newProbabilities;
        this.propertyChangeSupport.firePropertyChange("probabilitiesChanged", oldProbabilities, this.probabilities);
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
