package org.opentrafficsim.core.dsol;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.eventlists.EventListInterface;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.OTS_SCALAR;

/**
 * Typed extension of the DEVSSimulatorInterface without remote exceptions and using the Time.Abs and Time.Rel arguments.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface OTSDEVSSimulatorInterface extends
    DEVSSimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>,
    OTSSimulatorInterface, OTS_SCALAR
{
    /** {@inheritDoc} */
    @Override
    boolean cancelEvent(SimEventInterface<OTSSimTimeDouble> event);

    /** {@inheritDoc} */
    @Override
    EventListInterface<OTSSimTimeDouble> getEventList();

    /** {@inheritDoc} */
    @Override
    void scheduleEvent(SimEventInterface<OTSSimTimeDouble> event) throws SimRuntimeException;

    /**
     * schedules a methodCall at a relative duration. The executionTime is thus
     * simulator.getSimulatorTime()+relativeDuration.
     * @param relativeDelay the relativeDelay in timeUnits of the simulator.
     * @param priority the priority compared to other events scheduled at the same time.
     * @param source the source of the event
     * @param target the target
     * @param method the method
     * @param args the arguments.
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    void scheduleEventRel(Time.Rel relativeDelay, short priority, Object source, Object target,
        String method, Object[] args) throws SimRuntimeException;

    /**
     * schedules a methodCall at a relative duration. The executionTime is thus
     * simulator.getSimulatorTime()+relativeDuration.
     * @param relativeDelay the relativeDelay in timeUnits of the simulator.
     * @param source the source of the event
     * @param target the target
     * @param method the method
     * @param args the arguments.
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    void scheduleEventRel(Time.Rel relativeDelay, Object source, Object target, String method,
        Object[] args) throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    void scheduleEventAbs(OTSSimTimeDouble absoluteTime, short priority, Object source, Object target, String method,
        Object[] args) throws SimRuntimeException;

    /**
     * schedules a methodCall at an absolute time.
     * @param absoluteTime the exact time to schedule the method on the simulator.
     * @param source the source of the event
     * @param target the target
     * @param method the method
     * @param args the arguments.
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    void scheduleEventAbs(Time.Abs absoluteTime, Object source, Object target, String method,
        Object[] args) throws SimRuntimeException;

    /**
     * schedules a methodCall at an absolute time.
     * @param absoluteTime the exact time to schedule the method on the simulator.
     * @param priority the priority compared to other events scheduled at the same time.
     * @param source the source of the event
     * @param target the target
     * @param method the method
     * @param args the arguments.
     * @throws SimRuntimeException whenever the event is scheduled in the past.
     */
    void scheduleEventAbs(Time.Abs absoluteTime, short priority, Object source, Object target,
        String method, Object[] args) throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    void scheduleEventAbs(OTSSimTimeDouble absoluteTime, Object source, Object target, String method, Object[] args)
        throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    void scheduleEventNow(short priority, Object source, Object target, String method, Object[] args)
        throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    void scheduleEventNow(Object source, Object target, String method, Object[] args) throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    void setEventList(EventListInterface<OTSSimTimeDouble> eventList) throws SimRuntimeException;

    /**
     * Runs the simulator up to a certain time; events at that time will not yet be executed.
     * @param when the absolute time till when we want to run the simulation
     * @throws SimRuntimeException whenever starting fails. Possible occasions include starting a started simulator
     */
    void runUpTo(final Time.Abs when) throws SimRuntimeException;
}
