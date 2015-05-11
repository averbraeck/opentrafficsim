package org.opentrafficsim.simulationengine;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.gui.swing.DSOLPanel;

import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface SimpleSimulation
{
    /**
     * To use in a Swing application add the DSOLPanel to a JFrame.
     * @return the simulation panel (extends JPanel).
     */
    public DSOLPanel<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getPanel();
    
    /**
     * Run the simulation up to the specified time.
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the stop time.
     * @throws SimRuntimeException when the specified time lies in the past
     */
    public void runUpTo(final DoubleScalar.Abs<TimeUnit> when) throws SimRuntimeException;

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
     * @return SimEvent&lt;OTSSimTimeDouble&gt;; the event that was scheduled (the caller should save this if a need to
     *         cancel the event may arise later)
     * @throws SimRuntimeException when the <code>executionTime</code> is in the past
     */
    public SimEvent<OTSSimTimeDouble> scheduleEvent(final DoubleScalar.Abs<TimeUnit> executionTime,
            final short priority, final Object source, final Object target, final String method, final Object[] args)
            throws SimRuntimeException;

}
