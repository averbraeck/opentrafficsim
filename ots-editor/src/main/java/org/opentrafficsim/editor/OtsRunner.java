package org.opentrafficsim.editor;

import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box.Filler;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.djutils.exceptions.Try;
import org.opentrafficsim.animation.IconUtil;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.editor.OtsRunner.OtsRunnerModel;
import org.opentrafficsim.road.network.factory.xml.OtsXmlModel;
import org.opentrafficsim.swing.gui.AppearanceControlButton;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;

/**
 * Simulation runner for when running from editor.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OtsRunner extends OtsSimulationApplication<OtsRunnerModel>
{

    /** */
    private static final long serialVersionUID = 20231012;

    /**
     * Run a simulation.
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public OtsRunner(final OtsSimulationPanel panel, final OtsRunnerModel model)
    {
        // TODO colorer and markers based on user specification
        super(model, panel);
    }

    /**
     * Run single run from a file.
     * @param file XML file
     * @param scenario scenario, may be {@code null}
     * @param editor editor may be {@code null}
     */
    public static void runSingle(final File file, final String scenario, final OtsEditor editor)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("EditorRun");
            final OtsRunnerModel runnerModel = new OtsRunnerModel(simulator, file, scenario);
            // TODO: add OtsSimmulationPanelDecorator with contents based on Animation tag to OtsAnimationPanel constructor
            // TODO: simulationPanel.addConsoleTab() depending on Animation tag
            OtsSimulationPanel simulationPanel = new OtsSimulationPanel(runnerModel.getNetwork());
            if (editor == null)
            {
                OtsRunner app = new OtsRunner(simulationPanel, runnerModel);
                app.setExitOnClose(false);
            }
            else
            {
                // remember editor content and disable menus
                Container contentPane = editor.getContentPane();
                JMenuBar menuBar = editor.getJMenuBar();
                Map<JMenu, Boolean> wasEnabled = new LinkedHashMap<>();
                for (int i = 0; i < menuBar.getMenuCount(); i++)
                {
                    wasEnabled.put(menuBar.getMenu(i), menuBar.getMenu(i).isEnabled());
                    menuBar.getMenu(i).setEnabled(false);
                }

                // add button to menu to return to the editor
                JButton backToEditor = new AppearanceControlButton(IconUtil.of("RoadLayout24.png").imageSize(16, 16).get());
                Filler filler = new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(Short.MAX_VALUE, 0));
                backToEditor.setText("Back to editor");
                backToEditor.setMinimumSize(new Dimension(130, 16));
                backToEditor.setPreferredSize(new Dimension(130, 16));
                backToEditor.addActionListener((a) ->
                {
                    if (runnerModel.getNetwork().getSimulator().isStartingOrRunning())
                    {
                        runnerModel.getNetwork().getSimulator().stop();
                    }
                    Try.execute(() -> runnerModel.getNetwork().getSimulator().getContext().destroySubcontext("animation/2D"),
                            OtsRuntimeException.class, "Unable to destroy simulation context within editor.");
                    menuBar.remove(filler);
                    menuBar.remove(backToEditor);
                    for (int i = 0; i < menuBar.getMenuCount(); i++)
                    {
                        menuBar.getMenu(i).setEnabled(wasEnabled.get(menuBar.getMenu(i)));
                    }
                    editor.setContentPane(contentPane);
                    editor.invalidate();
                    editor.validate();
                    editor.repaint();
                });
                menuBar.add(filler);
                menuBar.add(backToEditor);
                backToEditor.transferFocus();

                editor.setContentPane(simulationPanel);
                editor.setAppearance(editor.getAppearance());
                editor.repaint();
            }
            simulationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class OtsRunnerModel extends OtsXmlModel
    {
        /**
         * Constructor.
         * @param simulator simulator
         * @param file XML file
         * @param scenario scenario, may be {@code null}
         */
        public OtsRunnerModel(final OtsSimulatorInterface simulator, final File file, final String scenario)
        {
            super(simulator, file.toString(), scenario);
        }
    }

}
