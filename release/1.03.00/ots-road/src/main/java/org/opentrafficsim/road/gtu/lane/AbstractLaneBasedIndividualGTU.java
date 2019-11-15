package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.road.network.OTSRoadNetwork;

/**
 * Specific type of LaneBasedGTU. This class adds length, width, maximum speed and a reference to the simulator to the
 * AbstractLaneBasedGTU.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedIndividualGTU extends AbstractLaneBasedGTU2
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
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param length Length; the maximum length of the GTU (parallel with driving direction)
     * @param width Length; the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumSpeed Speed; the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSSimulatorInterface; the simulator
     * @param network OTSRoadNetwork; the network that the GTU is initially registered in
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedIndividualGTU(final String id, final GTUType gtuType, final Length length, final Length width,
            final Speed maximumSpeed, final OTSSimulatorInterface simulator, final OTSRoadNetwork network) throws GTUException
    {
        super(id, gtuType, simulator, network);
        this.length = length;
        this.width = width;
        if (null == maximumSpeed)
        {
            throw new GTUException("maximumSpeed may not be null");
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
