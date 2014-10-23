package org.opentrafficsim.car;

import java.util.Set;

import org.opentrafficsim.core.gtu.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.GTUReferencePoint;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LaneLocation;
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
public class Car<ID> extends AbstractLaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param length the maximum length of the GTU (parallel with driving direction).
     * @param width the maximum width of the GTU (perpendicular to driving direction).
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction).
     */
    public Car(final ID id, final GTUType<?> gtuType, final DoubleScalar<LengthUnit> length,
            final DoubleScalar<LengthUnit> width, final DoubleScalar<SpeedUnit> maximumVelocity)
    {
        super(id, gtuType, length, width, maximumVelocity);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneLocation> getCurrentLocation(final GTUReferencePoint delta)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> longitudinalDistance(final GTUReferencePoint delta, final LaneLocation location)
    {
        // TODO
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar<SpeedUnit> getCurrentLongitudinalVelocity()
    {
        // TODO
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar<SpeedUnit> getCurrentLateralVelocity()
    {
        return new DoubleScalar.Rel<SpeedUnit>(0.0, SpeedUnit.METER_PER_SECOND);
    }

}
