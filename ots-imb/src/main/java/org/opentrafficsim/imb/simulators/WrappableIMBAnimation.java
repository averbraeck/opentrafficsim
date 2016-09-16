package org.opentrafficsim.imb.simulators;

import java.awt.Rectangle;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.WrappableAnimation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Interface as an extension of WrappableAnimation to create the right type of IMBAnimator.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface WrappableIMBAnimation extends WrappableAnimation
{
    /**
     * Build the IMB animation.
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the (possibly user-modified) properties. This list must
     *            contain all the properties returned by getProperties(); any additional properties may be ignored
     * @param rect the x, y, width and height for the window to rebuild. Use null for maximized screen.
     * @param exitOnClose Use EXIT_ON_CLOSE when true, DISPOSE_ON_CLOSE when false on closing of the window.
     * @return SimpleSimulation; the new simulation
     * @throws SimRuntimeException on ???
     * @throws NetworkException on Network inconsistency
     * @throws NamingException when context for the animation cannot be created
     * @throws OTSSimulationException when the construction of the simulation, the control panel, the animation, or the charts
     *             fails
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    SimpleIMBAnimator buildAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            ArrayList<AbstractProperty<?>> properties, Rectangle rect, boolean exitOnClose)
            throws SimRuntimeException, NetworkException, NamingException, OTSSimulationException, PropertyException;

}
