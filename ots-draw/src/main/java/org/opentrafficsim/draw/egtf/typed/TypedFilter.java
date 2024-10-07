package org.opentrafficsim.draw.egtf.typed;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.LengthVector;
import org.opentrafficsim.draw.egtf.Filter;
import org.opentrafficsim.draw.egtf.Quantity;

/**
 * Typed version of a kernel.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TypedFilter implements Filter
{

    /** Filter. */
    private final Filter filter;

    /**
     * Constructor.
     * @param filter wrapped filter
     */
    TypedFilter(final Filter filter)
    {
        this.filter = filter;
    }

    /** {@inheritDoc} */
    @Override
    public double[] getLocation()
    {
        return this.filter.getLocation();
    }

    /**
     * Returns the grid location.
     * @return grid location
     */
    public LengthVector getLocationVector()
    {
        return new LengthVector(getLocation(), LengthUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public double[] getTime()
    {
        return this.filter.getTime();
    }

    /**
     * Returns the grid time.
     * @return grid time
     */
    public DurationVector getTimeVector()
    {
        return new DurationVector(getTime(), DurationUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public double[][] getSI(final Quantity<?, ?> quantity)
    {
        return this.filter.getSI(quantity);
    }

    /** {@inheritDoc} */
    @Override
    public <K> K get(final Quantity<?, K> quantity)
    {
        return this.filter.get(quantity);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TypedFilter [filter=" + this.filter + "]";
    }

}
