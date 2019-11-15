package org.opentrafficsim.web.test;

import java.awt.geom.Rectangle2D;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;

import nl.tudelft.simulation.dsol.jetty.sse.OTSWebServer;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TJunctionDemo extends OTSWebServer
{
    /**
     * Create a T-Junction demo.
     * @param title String; the tile for the model
     * @param simulator DEVSRealTimeClock.TimeDouble; the simulator
     * @param model the model
     * @throws Exception on jetty error
     */
    public TJunctionDemo(final String title, final OTSSimulatorInterface simulator, final OTSModelInterface model)
            throws Exception
    {
        super(title, simulator, new Rectangle2D.Double(-200, -200, 400, 400));
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), simulator, new DefaultSwitchableGTUColorer());
    }

    /**
     * Start the demo.
     * @param args args
     * @throws Exception on error
     */
    public static void main(final String[] args) throws Exception
    {
        OTSAnimator simulator = new OTSAnimator();
        simulator.setAnimation(false);
        TJunctionModel junctionModel = new TJunctionModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), junctionModel);
        new TJunctionDemo("T-Junction demo", simulator, junctionModel);
    }
}
