package org.opentrafficsim.demo;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterBoolean;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDistContinuousSelection;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDistDiscreteSelection;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDoubleScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterFloat;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterFloatScalar;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterLong;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionList;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterString;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputField;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldBoolean;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldDistContinuous;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldDistDiscrete;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldDouble;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldDoubleScalar;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldFloat;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldFloatScalar;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldInteger;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldLong;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldSelectionList;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldSelectionMap;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputFieldString;

/**
 * TabbedParameterDialog takes an InputParameterMap and displays the top selections of the tree as tabs. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TabbedParameterDialog extends JDialog implements ActionListener
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The parameter map. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final InputParameterMap inputParameterMap;

    /** the fields with the parameters. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected List<InputField> fields = new ArrayList<>();

    /** indication that the user has indicated to stop, leading to a dispose of the parameter dialog. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean stopped = false;

    /**
     * Construct a tabbed parameter dialog that is not a part of a higher dialog.
     * @param inputParameterMap the parameter map to use
     */
    public TabbedParameterDialog(final InputParameterMap inputParameterMap)
    {
        super(null, inputParameterMap.getShortName(), Dialog.ModalityType.DOCUMENT_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent windowEvent)
            {
                TabbedParameterDialog.this.stopped = true;
                super.windowClosing(windowEvent);
            }
        });
        setPreferredSize(new Dimension(1024, 600));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        this.inputParameterMap = inputParameterMap;
        JTabbedPane tabbedPane = new JTabbedPane();
        panel.add(tabbedPane);

        for (InputParameter<?, ?> tab : this.inputParameterMap.getSortedSet())
        {
            if (!this.stopped)
            {
                if (!(tab instanceof InputParameterMap))
                {
                    Object[] options = { "CONTINUE", "STOP" };
                    int choice = JOptionPane.showOptionDialog(null,
                            "Input parameter\n" + tab.getShortName() + "\ncannot be displayed in a tab", "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                    if (choice == 1)
                    {
                        dispose();
                        this.stopped = true;
                    }
                }
                else
                {
                    InputParameterMap tabbedMap = (InputParameterMap) tab;
                    JPanel tabbedPanel = new JPanel();
                    JPanel tabbedWrapper = new JPanel(new BorderLayout());
                    tabbedWrapper.add(tabbedPanel, BorderLayout.NORTH);
                    tabbedWrapper.add(Box.createGlue(), BorderLayout.CENTER);
                    tabbedPane.addTab(tab.getShortName(), tabbedWrapper);
                    // to accommodate different height fields we use a horizontal box layout within a vertical box layout
                    BoxLayout tabLayout = new BoxLayout(tabbedPanel, BoxLayout.Y_AXIS);
                    tabbedPanel.setLayout(tabLayout);
                    for (InputParameter<?, ?> parameter : tabbedMap.getSortedSet())
                    {
                        JPanel row = new JPanel();
                        GridLayout rowLayout = new GridLayout(1, 3, 5, 0);
                        row.setLayout(rowLayout);
                        tabbedPanel.add(row);
                        addParameterField(row, parameter);
                        tabbedPanel.add(Box.createVerticalStrut(2));
                    }
                }
            }
        }

        if (!this.stopped)
        {
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            centerPanel.add(buttonPanel);
            panel.add(centerPanel);

            JButton startSimulationButton = new JButton("Start simulation model");
            startSimulationButton.addActionListener(this);
            buttonPanel.add(startSimulationButton);

            JButton cancelButton = new JButton("Cancel");
            buttonPanel.add(cancelButton);
            cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    setVisible(false);
                    dispose();
                    TabbedParameterDialog.this.stopped = true;
                }
            });
        }

        if (!this.stopped)
        {
            add(panel);
            pack();
            setVisible(true);
        }
    }

    /**
     * Add the right type of field for this parameter and do the housekeeping to retrieve the value. When overriding, do not
     * forget to call super.addParameterField() for the options that should be handled in a standard way.
     * @param panel the panel in which to put the parameter
     * @param parameter the input parameter to display
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addParameterField(final JPanel panel, final InputParameter<?, ?> parameter)
    {
        if (parameter instanceof InputParameterDouble)
        {
            this.fields.add(new InputFieldDouble(panel, (InputParameterDouble) parameter));
        }
        else if (parameter instanceof InputParameterFloat)
        {
            this.fields.add(new InputFieldFloat(panel, (InputParameterFloat) parameter));
        }
        else if (parameter instanceof InputParameterBoolean)
        {
            this.fields.add(new InputFieldBoolean(panel, (InputParameterBoolean) parameter));
        }
        else if (parameter instanceof InputParameterLong)
        {
            this.fields.add(new InputFieldLong(panel, (InputParameterLong) parameter));
        }
        else if (parameter instanceof InputParameterInteger)
        {
            this.fields.add(new InputFieldInteger(panel, (InputParameterInteger) parameter));
        }
        else if (parameter instanceof InputParameterString)
        {
            this.fields.add(new InputFieldString(panel, parameter));
        }
        else if (parameter instanceof InputParameterDoubleScalar)
        {
            this.fields.add(new InputFieldDoubleScalar(panel, (InputParameterDoubleScalar<?, ?>) parameter));
        }
        else if (parameter instanceof InputParameterFloatScalar)
        {
            this.fields.add(new InputFieldFloatScalar(panel, (InputParameterFloatScalar<?, ?>) parameter));
        }
        else if (parameter instanceof InputParameterSelectionList<?>)
        {
            this.fields.add(new InputFieldSelectionList(panel, (InputParameterSelectionList<?>) parameter));
        }
        else if (parameter instanceof InputParameterDistDiscreteSelection)
        {
            this.fields.add(new InputFieldDistDiscrete(panel, (InputParameterDistDiscreteSelection) parameter));
        }
        else if (parameter instanceof InputParameterDistContinuousSelection)
        {
            this.fields.add(new InputFieldDistContinuous(panel, (InputParameterDistContinuousSelection) parameter));
        }
        else if (parameter instanceof InputParameterSelectionMap<?, ?>)
        {
            this.fields.add(new InputFieldSelectionMap(panel, (InputParameterSelectionMap<?, ?>) parameter));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        boolean ok = true;
        try
        {
            for (InputField field : this.fields)
            {
                if (field instanceof InputFieldDouble)
                {
                    InputFieldDouble f = (InputFieldDouble) field;
                    f.getParameter().setDoubleValue(f.getDoubleValue());
                }
                else if (field instanceof InputFieldFloat)
                {
                    InputFieldFloat f = (InputFieldFloat) field;
                    f.getParameter().setFloatValue(f.getFloatValue());
                }
                else if (field instanceof InputFieldInteger)
                {
                    InputFieldInteger f = (InputFieldInteger) field;
                    f.getParameter().setIntValue(f.getIntValue());
                }
                else if (field instanceof InputFieldLong)
                {
                    InputFieldLong f = (InputFieldLong) field;
                    f.getParameter().setLongValue(f.getLongValue());
                }
                else if (field instanceof InputFieldString)
                {
                    InputFieldString f = (InputFieldString) field;
                    ((InputParameterString) f.getParameter()).setStringValue(f.getStringValue());
                }
                else if (field instanceof InputFieldDoubleScalar)
                {
                    InputFieldDoubleScalar<?, ?> f = (InputFieldDoubleScalar<?, ?>) field;
                    f.getParameter().getDoubleParameter().setDoubleValue(f.getDoubleValue());
                    f.getParameter().getUnitParameter().setObjectValue(f.getUnit());
                    f.getParameter().setCalculatedValue(); // it will retrieve the set double value and unit
                }
                else if (field instanceof InputFieldFloatScalar)
                {
                    InputFieldFloatScalar<?, ?> f = (InputFieldFloatScalar<?, ?>) field;
                    f.getParameter().getFloatParameter().setFloatValue(f.getFloatValue());
                    f.getParameter().getUnitParameter().setObjectValue(f.getUnit());
                    f.getParameter().setCalculatedValue(); // it will retrieve the set float value and unit
                }
                else if (field instanceof InputFieldSelectionList<?>)
                {
                    InputFieldSelectionList<?> f = (InputFieldSelectionList<?>) field;
                    f.getParameter().setIndex(f.getIndex());
                }
                else if (field instanceof InputFieldDistContinuous)
                {
                    InputFieldDistContinuous f = (InputFieldDistContinuous) field;
                    f.setDistParameterValues();
                    f.getParameter().getValue().setDist();
                }
                else if (field instanceof InputFieldDistDiscrete)
                {
                    InputFieldDistDiscrete f = (InputFieldDistDiscrete) field;
                    f.setDistParameterValues();
                    f.getParameter().getValue().setDist();
                }
                else if (field instanceof InputFieldSelectionMap<?, ?>)
                {
                    InputFieldSelectionMap<?, ?> f = (InputFieldSelectionMap<?, ?>) field;
                    f.getParameter().setObjectValue(f.getValue());
                }
            }
        }
        catch (Exception exception)
        {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Data Entry Error", JOptionPane.ERROR_MESSAGE);
            ok = false;
        }
        if (ok)
        {
            setVisible(false);
            dispose();
        }
    }

    /**
     * Construct a tabbed parameter dialog that is not a part of a higher dialog.
     * @param inputParameterMap InputParameterMap; the parameter map to use
     * @return whether the data was entered correctly or not
     */
    public static boolean process(final InputParameterMap inputParameterMap)
    {
        TabbedParameterDialog dialog = new TabbedParameterDialog(inputParameterMap);
        return !dialog.stopped;
    }
}
