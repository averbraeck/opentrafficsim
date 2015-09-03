package org.opentrafficsim.simulationengine;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface SimpleSimulation extends OTSDEVSSimulatorInterface, OTS_SCALAR
{
    /**
     * Construct and schedule a SimEvent using a DoubleScalar.Abs&lt;TimeUnit&gt; to specify the execution time.
     * @param executionTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time at which the event must happen
     * @param priority short; should be between <cite>SimEventInterface.MAX_PRIORITY</cite> and
     *            <cite>SimEventInterface.MIN_PRIORITY</cite>; most normal events should use
     *            <cite>SimEventInterface.NORMAL_PRIORITY</cite>
     * @param source Object; the object that creates/schedules the event
     * @param target Object; the object that must execute the event
     * @param method String; the name of the method of <code>target</code> that must execute the event
     * @param args Object[]; the arguments of the <code>method</code> that must execute the event
     * @return SimEvent&lt;OTSSimTimeDouble&gt;; the event that was scheduled (the caller should save this if a need to cancel
     *         the event may arise later)
     * @throws SimRuntimeException when the <code>executionTime</code> is in the past
     */
    SimEvent<OTSSimTimeDouble> scheduleEvent(final Time.Abs executionTime, final short priority, final Object source,
        final Object target, final String method, final Object[] args) throws SimRuntimeException;

}
