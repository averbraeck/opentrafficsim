package org.opentrafficsim.core.network;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Lane extends CrossSectionElement
{
    /** type of lane to deduce compatibility with GTU types. */
    private final LaneType laneType;

    /** in direction of geometry, reverse, or both. */
    private final Directionality directionality;

    /** Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private DoubleScalar<FrequencyUnit> capacity;

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralStartPosition the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param beginWidth start width, positioned <i>symmetrically around</i> the lateral start position.
     * @param endWidth end width, positioned <i>symmetrically around</i> the lateral end position.
     * @param laneType type of lane to deduce compatibility with GTU types.
     * @param directionality in direction of geometry, reverse, or both.
     * @param capacity Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage).
     */
    public Lane(final CrossSectionLink<?, ?> parentLink, final DoubleScalar<LengthUnit> lateralStartPosition,
            final DoubleScalar<LengthUnit> beginWidth, final DoubleScalar<LengthUnit> endWidth, final LaneType laneType,
            final Directionality directionality, final DoubleScalar<FrequencyUnit> capacity)
    {
        super(parentLink, lateralStartPosition, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionality = directionality;
        this.capacity = capacity;
    }

    /**
     * @return capacity.
     */
    public final DoubleScalar<FrequencyUnit> getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set capacity.
     */
    public final void setCapacity(final DoubleScalar<FrequencyUnit> capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @return laneType.
     */
    public final LaneType getLaneType()
    {
        return this.laneType;
    }

    /**
     * @return directionality.
     */
    public final Directionality getDirectionality()
    {
        return this.directionality;
    }

}
