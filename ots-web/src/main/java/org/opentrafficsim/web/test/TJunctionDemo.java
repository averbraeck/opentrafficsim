package org.opentrafficsim.web.test;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.bounds.Bounds2d;
import org.opentrafficsim.animation.DefaultAnimationFactory;
import org.opentrafficsim.animation.gtu.colorer.DefaultSwitchableGtuColorer;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

import nl.tudelft.simulation.dsol.jetty.sse.OtsWebServer;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TJunctionDemo extends OtsWebServer
{
    /**
     * Create a T-Junction demo.
     * @param title the tile for the model
     * @param simulator DevsRealTimeAnimator.TimeDouble; the simulator
     * @param model the model
     * @throws Exception on jetty error
     */
    public TJunctionDemo(final String title, final OtsSimulatorInterface simulator, final OtsModelInterface model)
            throws Exception
    {
        super(title, simulator, new Bounds2d(-200, 200, -200, 200));
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getNetwork().getSimulator(),
                new DefaultSwitchableGtuColorer());
    }

    /**
     * Start the demo.
     * @param args args
     * @throws Exception on error
     */
    public static void main(final String[] args) throws Exception
    {
        OtsAnimator simulator = new OtsAnimator("TJunctionDemo");
        simulator.setAnimation(false);
        TJunctionModel junctionModel = new TJunctionModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), junctionModel);
        new TJunctionDemo("T-Junction demo", simulator, junctionModel);
    }
}
