package org.opentrafficsim.core.dsol;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.simtime.SimTime;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Time;

/**
 * OTS uses a DoubleScalar.Abs&lt;TimeUnit&gt; to represent simulation start time and a DoubleScalar.Rel&lt;timeUnit&gt; to
 * represent the warmup time and total duration of a simulation.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 3, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSSimTimeDouble extends SimTime<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
        implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The time. */
    private DoubleScalar.Abs<TimeUnit> time;

    /**
     * @param time DoubleSclaar.Abs&lt;TimeUnit&gt;
     */
    public OTSSimTimeDouble(final DoubleScalar.Abs<TimeUnit> time)
    {
        super(time);
    }

    /** {@inheritDoc} */
    @Override
    public final void add(final DoubleScalar.Rel<TimeUnit> simTime)
    {
        this.time = this.time.plus(simTime);
    }

    /** {@inheritDoc} */
    @Override
    public final void subtract(final DoubleScalar.Rel<TimeUnit> simTime)
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
        // TODO this.time.setZero();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSSimTimeDouble copy()
    {
        return new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(this.time.getInUnit(), this.time.getUnit()));
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final DoubleScalar.Abs<TimeUnit> value)
    {
        this.time = value;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> get()
    {
        return this.time;
    }

    /**
     * @return the time as a strongly typed time.
     */
    public final Time.Abs getTime()
    {
        return new Time.Abs(this.time.si, TimeUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<TimeUnit> minus(final OTSSimTimeDouble absoluteTime)
    {
        DoubleScalar.Rel<TimeUnit> rel = DoubleScalar.minus(this.time, absoluteTime.get());
        return rel;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSSimTimeDouble [time=" + this.time + "]";
    }

}
