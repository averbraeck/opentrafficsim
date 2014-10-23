package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractGTU<ID> implements GTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** the id of the GTU, could be String or Integer. */
    private final ID id;

    /** the type of GTU, e.g. TruckType, CarType, BusType. */
    private final GTUType<?> gtuType;

    /** the maximum length of the GTU (parallel with driving direction). */
    private final DoubleScalar<LengthUnit> length;

    /** the maximum width of the GTU (perpendicular to driving direction). */
    private final DoubleScalar<LengthUnit> width;

    /** the maximum speed of the GTU (in the driving direction). */
    private final DoubleScalar<SpeedUnit> maximumVelocity;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param length the maximum length of the GTU (parallel with driving direction).
     * @param width the maximum width of the GTU (perpendicular to driving direction).
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction).
     */
    public AbstractGTU(final ID id, final GTUType<?> gtuType, final DoubleScalar<LengthUnit> length,
            final DoubleScalar<LengthUnit> width, final DoubleScalar<SpeedUnit> maximumVelocity)
    {
        super();
        this.id = id;
        this.gtuType = gtuType;
        this.length = length;
        this.width = width;
        this.maximumVelocity = maximumVelocity;
    }

    /** {@inheritDoc} */
    @Override
    public final ID getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar<LengthUnit> getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar<LengthUnit> getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final GTUType<?> getGTUType()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar<SpeedUnit> getMaximumVelocity()
    {
        return this.maximumVelocity;
    }
}
