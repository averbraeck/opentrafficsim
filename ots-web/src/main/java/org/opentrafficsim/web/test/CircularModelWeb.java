package org.opentrafficsim.web.test;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.bounds.Bounds2d;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;

import nl.tudelft.simulation.dsol.jetty.sse.OTSWebServer;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;

/**
 * Test4DCrossing.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class CircularModelWeb extends OTSWebServer
{
    /**
     * @param title String; the tile for the model
     * @param simulator DEVSRealTimeAnimator.TimeDouble; the simulator
     * @param model the model
     * @throws Exception on jetty error
     */
    public CircularModelWeb(final String title, final OTSSimulatorInterface simulator, final OTSModelInterface model)
            throws Exception
    {
        super(title, simulator, new Bounds2d(-200, 200, -200, 200));
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getNetwork().getSimulator(),
                new DefaultSwitchableGTUColorer());
    }

    /**
     * @param args String[]; arguments, expected to be empty
     * @throws Exception on error
     */
    public static void main(final String[] args) throws Exception
    {
        OTSAnimator simulator = new OTSAnimator("CircularModelWeb");
        simulator.setAnimation(false);
        CircularRoadModel model = new CircularRoadModel(simulator);
        if (TabbedParameterDialog.process(model.getInputParameterMap()))
        {
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), model);
            new CircularModelWeb("Circular Road", simulator, model);
        }
    }
}
