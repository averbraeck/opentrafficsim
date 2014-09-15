package org.opentrafficsim.core.dsol;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.simtime.SimTime;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Aug 3, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSSimTimeDouble extends SimTime<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
        implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** value represents the value in milliseconds */
    private MutableDoubleScalar.Abs<TimeUnit> time;

    /**
     * @param time
     */
    public OTSSimTimeDouble(final DoubleScalar.Abs<TimeUnit> time)
    {
        super(time);
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#add(java.lang.Number)
     */
    @Override
    public void add(final DoubleScalar.Rel<TimeUnit> simTime)
    {
        this.time.add(simTime);
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#subtract(java.lang.Number)
     */
    @Override
    public void subtract(final DoubleScalar.Rel<TimeUnit> simTime)
    {
        this.time.subtract(simTime);
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#compareTo(nl.tudelft.simulation.dsol.simtime.SimTime)
     */
    @Override
    public int compareTo(final OTSSimTimeDouble simTime)
    {
        return this.time.immutable().compareTo(simTime.get());
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#setZero()
     */
    @Override
    public OTSSimTimeDouble setZero()
    {
        // TODO: this.time.setZero();
        return this;
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#copy()
     */
    @Override
    public OTSSimTimeDouble copy()
    {
        return new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(this.time.getValueInUnit(), this.time.getUnit()));
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#set(java.lang.Comparable)
     */
    @Override
    public void set(final DoubleScalar.Abs<TimeUnit> value)
    {
        this.time = value.mutable();
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#get()
     */
    @Override
    public DoubleScalar.Abs<TimeUnit> get()
    {
        return this.time.immutable();
    }

    /**
     * @see nl.tudelft.simulation.dsol.simtime.SimTime#minus(nl.tudelft.simulation.dsol.simtime.SimTime)
     */
    @Override
    public DoubleScalar.Rel<TimeUnit> minus(final OTSSimTimeDouble absoluteTime)
    {
        DoubleScalar.Rel<TimeUnit> rel = MutableDoubleScalar.minus(this.time.immutable(), absoluteTime.get()).immutable();
        return rel;
    }

}
