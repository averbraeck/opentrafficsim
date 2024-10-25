package org.opentrafficsim.editor;

import java.awt.Dimension;
import java.io.File;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.editor.OtsRunner.OtsRunnerModel;
import org.opentrafficsim.road.network.RoadNetwork;
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
        super(model, panel);
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
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), runnerModel);
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(runnerModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, runnerModel, DEFAULT_COLORER, runnerModel.getNetwork());
            OtsRunner app = new OtsRunner(animationPanel, runnerModel);
            app.setExitOnClose(false);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException | DsolException exception)
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
    public static class OtsRunnerModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20231012L;

        /** File. */
        private File file;

        /** Scenario. */
        private String scenario;

        /** The network. */
        private RoadNetwork network;

        /**
         * Constructor.
         * @param simulator simulator.
         * @param file XML file.
         * @param scenario scenario, may be {@code null}.
         */
        public OtsRunnerModel(final OtsSimulatorInterface simulator, final File file, final String scenario)
        {
            super(simulator);
            this.file = file;
            this.scenario = scenario;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                this.network = new RoadNetwork("EditorNetwork", getSimulator());
                XmlParser parser = new XmlParser(this.network).setUrl(this.file.toURI().toURL());
                if (this.scenario != null)
                {
                    parser.setScenario(this.scenario);
                }
                parser.build();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }

    }
}
