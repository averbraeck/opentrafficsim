package org.opentrafficsim.simulationengine;

import java.awt.Rectangle;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.network.NetworkException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Requirements for demonstration that can be shown in the SuperDemo.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-23 12:51:29 +0200 (Sun, 23 Aug 2015) $, @version $Revision: 1293 $, by $Author: averbraeck $,
 * initial version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface WrappableAnimation
{
    /**
     * Build the animation.
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param properties List&lt;Property&lt;?&gt;&gt;; the (possibly user-modified) properties. This list must contain all the
     *            properties returned by getProperties(); any additional properties may be ignored
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
    OTSSimulatorInterface buildAnimator(Time startTime, Duration warmupPeriod, Duration runLength, List<Property<?>> properties,
            Rectangle rect, boolean exitOnClose)
            throws SimRuntimeException, NetworkException, NamingException, OTSSimulationException, PropertyException;

    /**
     * Restart (rebuild) the simulation.
     * @param rect the x, y, width and height for the window to rebuild. Use null for maximized screen.
     * @return SimpleSimulation; the new simulation
     * @throws SimRuntimeException on ???
     * @throws NetworkException on Network inconsistency
     * @throws NamingException when context for the animation cannot be created
     * @throws OTSSimulationException when the (re)construction of the simulation model fails
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    OTSSimulatorInterface rebuildSimulator(Rectangle rect)
            throws SimRuntimeException, NetworkException, NamingException, OTSSimulationException, PropertyException;

    /**
     * Return a very short description of the simulation.
     * @return String; short description of the simulation
     */
    String shortName();

    /**
     * Return a description of the simulation (HTML formatted).
     * @return String; HTML text describing the simulation
     */
    String description();

    /**
     * Retrieve a list of visible properties of the simulation. <br>
     * The caller can modify the returned result. If the internal format is also an ArrayList it is highly recommended to make a
     * protective copy and return that.
     * @return List&lt;Property&lt;?&gt;&gt;; the list of visible properties
     */
    List<Property<?>> getProperties();

    /**
     * Retrieve a list of properties as the user has modified them.
     * @return ArrayList&lt;Property&lt;?&gt;&gt;; the list of visible properties
     */
    List<Property<?>> getUserModifiedProperties();

    /**
     * Stop the timers and threads that are connected when disposing of this wrappable simulation.
     */
    void stopTimersThreads();

    /**
     * Set the number of the next spawned replication.
     * @param nextReplication Integer; the next replication number, or null to use the built-in auto-incrementing replication
     *            counter
     */
    void setNextReplication(Integer nextReplication);

}
