package org.opentrafficsim.editor.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

import org.opentrafficsim.editor.EvalWrapper;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.ScenarioWrapper;
import org.opentrafficsim.road.network.factory.xml.CircularDependencyException;

/**
 * Action listener that listens to the scenario being selected.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ScenarioActionListener implements ActionListener
{

    /** OTS editor. */
    private final OtsEditor editor;

    /** Main tabbed pane at the left-hand side. */
    private final JTabbedPane visualizationPane;

    /** Scenario selection. */
    private final JComboBox<ScenarioWrapper> scenario;

    /** Eval wrapper, which maintains input parameters and notifies all dependent objects on changes. */
    private final EvalWrapper evalWrapper;

    /**
     * Constructor.
     * @param editor OTS editor
     * @param visualizationPane main tabbed pane at the left-hand side
     * @param scenario scenario selection
     * @param evalWrapper eval wrapper, which maintains input parameters and notifies all dependent objects on changes
     */
    public ScenarioActionListener(final OtsEditor editor, final JTabbedPane visualizationPane,
            final JComboBox<ScenarioWrapper> scenario, final EvalWrapper evalWrapper)
    {
        this.editor = editor;
        this.visualizationPane = visualizationPane;
        this.scenario = scenario;
        this.evalWrapper = evalWrapper;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        try
        {
            this.evalWrapper.setDirty();
            this.evalWrapper.getEval(this.scenario.getItemAt(this.scenario.getSelectedIndex()));
            this.visualizationPane.repaint();
        }
        catch (CircularDependencyException ex)
        {
            this.editor.dialogs().showCircularInputParameters(ex.getMessage());
        }
        catch (RuntimeException ex)
        {
            this.editor.dialogs().showInvalidExpression(ex.getMessage());
        }
    }

}
