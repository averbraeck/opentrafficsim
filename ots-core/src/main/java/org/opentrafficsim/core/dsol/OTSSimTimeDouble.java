package org.opentrafficsim.core.dsol;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.simtime.SimTime;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 3, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSSimTimeDouble extends SimTime<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
        implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** value represents the value in milliseconds. */
    private MutableDoubleScalar.Abs<TimeUnit> time;

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
        this.time.incrementBy(simTime);
    }

    /** {@inheritDoc} */
    @Override
    public final void subtract(final DoubleScalar.Rel<TimeUnit> simTime)
    {
        this.time.decrementBy(simTime);
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(final OTSSimTimeDouble simTime)
    {
        return this.time.immutable().compareTo(simTime.get());
    }

    /** {@inheritDoc} */
    @Override
    public final OTSSimTimeDouble setZero()
    {
        // TODO: this.time.setZero();
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
        this.time = value.mutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> get()
    {
        return this.time.immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<TimeUnit> minus(final OTSSimTimeDouble absoluteTime)
    {
        DoubleScalar.Rel<TimeUnit> rel = DoubleScalar.minus(this.time.immutable(), absoluteTime.get()).immutable();
        return rel;
    }

}
