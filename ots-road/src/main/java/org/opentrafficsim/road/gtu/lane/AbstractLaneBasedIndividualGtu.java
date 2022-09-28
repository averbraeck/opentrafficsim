package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.network.OTSRoadNetwork;

/**
 * Specific type of LaneBasedGtu. This class adds length, width, maximum speed and a reference to the simulator to the
 * AbstractLaneBasedGtu.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedIndividualGtu extends AbstractLaneBasedGtu2
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** The maximum length of the GTU (parallel with driving direction). */
    private final Length length;

    /** The maximum width of the GTU (perpendicular to driving direction). */
    private final Length width;

    /** The maximum speed of the GTU (in the driving direction). */
    private final Speed maximumSpeed;

    /** Distance over which the GTU should not change lane after being created. */
    private Length noLaneChangeDistance;

    /**
     * Construct a new AbstractLaneBasedIndividualGTU.
     * @param id String; the id of the GTU
     * @param gtuType GtuType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param length Length; the maximum length of the GTU (parallel with driving direction)
     * @param width Length; the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumSpeed Speed; the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSSimulatorInterface; the simulator
     * @param network OTSRoadNetwork; the network that the GTU is initially registered in
     * @throws GtuException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedIndividualGtu(final String id, final GtuType gtuType, final Length length, final Length width,
            final Speed maximumSpeed, final OTSSimulatorInterface simulator, final OTSRoadNetwork network) throws GtuException
    {
        super(id, gtuType, network);
        this.length = length;
        this.width = width;
        if (null == maximumSpeed)
        {
            throw new GtuException("maximumSpeed may not be null");
        }
        this.maximumSpeed = maximumSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getMaximumSpeed()
    {
        return this.maximumSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public final void setNoLaneChangeDistance(final Length distance)
    {
        this.noLaneChangeDistance = distance;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean laneChangeAllowed()
    {
        return this.noLaneChangeDistance == null ? true : getOdometer().gt(this.noLaneChangeDistance);
    }

}
