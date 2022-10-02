package org.opentrafficsim.demo;

import java.awt.Dimension;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
import nl.tudelft.simulation.language.DSOLException;

/**
 * Demonstration of a crossing with traffic lights.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class CrossingTrafficLightsSwing extends OTSSimulationApplication<CrossingTrafficLightsModel> implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a CrossingTrafficLights Swing application.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model CrossingTrafficLightsModel; the model
     * @throws OtsDrawingException on animation error
     */
    public CrossingTrafficLightsSwing(final String title, final OTSAnimationPanel panel, final CrossingTrafficLightsModel model)
            throws OtsDrawingException
    {
        super(model, panel);
        OTSRoadNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("CrossingTrafficLightsSwing");
            final CrossingTrafficLightsModel otsModel = new CrossingTrafficLightsModel(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, DEFAULT_COLORER, otsModel.getNetwork());
                CrossingTrafficLightsSwing app =
                        new CrossingTrafficLightsSwing("CrossingTrafficLights", animationPanel, otsModel);
                app.setExitOnClose(exitOnClose);
                animationPanel.enableSimulationControlButtons();
            }
            else
            {
                if (exitOnClose)
                {
                    System.exit(0);
                }
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | OtsDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }
}
