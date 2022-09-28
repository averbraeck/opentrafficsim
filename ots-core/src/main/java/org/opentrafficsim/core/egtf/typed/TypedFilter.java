package org.opentrafficsim.core.egtf.typed;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.egtf.Filter;
import org.opentrafficsim.core.egtf.Quantity;

/**
 * Typed version of a kernel.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TypedFilter implements Filter
{

    /** Filter. */
    private final Filter filter;

    /**
     * Constructor.
     * @param filter Filter; wrapped filter
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
     * @return LengthVector; grid location
     */
    public LengthVector getLocationVector()
    {
        return Try.assign(() -> DoubleVector.instantiate(getLocation(), LengthUnit.SI, StorageType.DENSE),
                "Exception while creating LengthVector");
    }

    /** {@inheritDoc} */
    @Override
    public double[] getTime()
    {
        return this.filter.getTime();
    }

    /**
     * Returns the grid time.
     * @return DurationVector; grid time
     */
    public DurationVector getTimeVector()
    {
        return Try.assign(() -> DoubleVector.instantiate(getTime(), DurationUnit.SI, StorageType.DENSE),
                "Exception while creating DurationVector");
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
