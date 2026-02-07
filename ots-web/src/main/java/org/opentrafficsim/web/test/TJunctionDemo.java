package org.opentrafficsim.web.test;

import java.util.Collections;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.bounds.Bounds2d;
import org.opentrafficsim.animation.DefaultAnimationFactory;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.web.OtsWebServer;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
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
                getAnimationPanel().getGtuColorerManager(), Collections.emptyMap());
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
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), junctionModel,
                HistoryManagerDevs.noHistory(simulator));
        new TJunctionDemo("T-Junction demo", simulator, junctionModel);
    }
}
