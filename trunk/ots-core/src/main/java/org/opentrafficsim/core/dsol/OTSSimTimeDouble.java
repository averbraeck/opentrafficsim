package org.opentrafficsim.core.dsol;

import java.io.Serializable;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.simtime.SimTime;

/**
 * OTS uses a DoubleScalar.Abs&lt;TimeUnit&gt; to represent simulation start time and a DoubleScalar.Rel&lt;timeUnit&gt; to
 * represent the warmup time and total duration of a simulation.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 3, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSSimTimeDouble extends SimTime<Time, Duration, OTSSimTimeDouble> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The time. */
    private Time time;

    /**
     * @param time DoubleSclaar.Abs&lt;TimeUnit&gt;
     */
    public OTSSimTimeDouble(final Time time)
    {
        super(time);
    }

    /** {@inheritDoc} */
    @Override
    public final void add(final Duration simTime)
    {
        this.time = this.time.plus(simTime);
    }

    /** {@inheritDoc} */
    @Override
    public final void subtract(final Duration simTime)
    {
        this.time = this.time.minus(simTime);
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(final OTSSimTimeDouble simTime)
    {
        return this.time.compareTo(simTime.get());
    }

    /** {@inheritDoc} */
    @Override
    public final OTSSimTimeDouble setZero()
    {
        this.time = Time.ZERO;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSSimTimeDouble copy()
    {
        return new OTSSimTimeDouble(new Time(this.time.getInUnit(), this.time.getUnit()));
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final Time value)
    {
        this.time = value;
    }

    /** {@inheritDoc} */
    @Override
    public final Time get()
    {
        return this.time;
    }

    /**
     * @return the time as a strongly typed time.
     */
    public final Time getTime()
    {
        return new Time(this.time.si, TimeUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final Duration minus(final OTSSimTimeDouble absoluteTime)
    {
        Duration rel = this.time.minus(absoluteTime.get());
        return rel;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSSimTimeDouble [time=" + this.time + "]";
    }

}
