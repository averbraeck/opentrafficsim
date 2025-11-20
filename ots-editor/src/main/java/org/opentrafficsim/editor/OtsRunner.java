package org.opentrafficsim.editor;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collections;

import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.editor.OtsRunner.OtsRunnerModel;
import org.opentrafficsim.road.network.factory.xml.OtsXmlModel;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;

/**
 * Simulation runner for when running from editor.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    public OtsRunner(final OtsAnimationPanel panel, final OtsRunnerModel model)
    {
        // TODO colorer and markers based on user specification
        super(model, panel, Collections.emptyMap());
    }

    /**
     * Run single run from a file.
     * @param file XML file.
     * @param scenario scenario, may be {@code null}.
     */
    public static void runSingle(final File file, final String scenario)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("EditorRun");
            final OtsRunnerModel runnerModel = new OtsRunnerModel(simulator, file, scenario);
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(runnerModel.getNetwork().getExtent(), simulator,
                    runnerModel, DEFAULT_GTU_COLORERS, runnerModel.getNetwork());
            OtsRunner app = new OtsRunner(animationPanel, runnerModel);
            app.setExitOnClose(false);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    protected void setAnimationToggles()
    {
        AnimationToggles.setIconAnimationTogglesStandard(getAnimationPanel());
    }

    /**
     * The simulation model.
     */
    public static class OtsRunnerModel extends OtsXmlModel
    {
        /** Scenario. */
        private String scenario;

        /**
         * Constructor.
         * @param simulator simulator.
         * @param file XML file.
         * @param scenario scenario, may be {@code null}.
         */
        public OtsRunnerModel(final OtsSimulatorInterface simulator, final File file, final String scenario)
        {
            super(simulator, file.toString());
            this.scenario = scenario;
        }

        @Override
        public void constructModel(final XmlParser xmlParser) throws Exception
        {
            xmlParser.setScenario(this.scenario).build();
        }
    }

}
