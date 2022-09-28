package org.opentrafficsim.road.network.lane.object.sensor;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A DestinationSensor is a sensor that deletes a GTU that has the node it will pass after this sensor as its destination.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class DestinationSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** the destination node for which this is the DestinationSensor. */
    private final Node destinationNode;

    /**
     * @param lane Lane; the lane that triggers the deletion of the GTU.
     * @param position Length; the position of the sensor
     * @param gtuDirectionality GTUDirectionality; GTU directionality
     * @param simulator OTSSimulatorInterface; the simulator to enable animation.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public DestinationSensor(final Lane lane, final Length position, final GTUDirectionality gtuDirectionality,
            final OTSSimulatorInterface simulator) throws NetworkException
    {
        this(lane, position, gtuDirectionality.isPlus() ? Compatible.PLUS : Compatible.MINUS, simulator);
    }

    /**
     * @param lane Lane; the lane that triggers the deletion of the GTU.
     * @param position Length; the position of the sensor
     * @param compatible Compatible; compatible GTU type and direction
     * @param simulator OTSSimulatorInterface; the simulator to enable animation.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public DestinationSensor(final Lane lane, final Length position, final Compatible compatible,
            final OTSSimulatorInterface simulator) throws NetworkException
    {
        super("DESTINATION@" + lane.getFullId(), lane, position, RelativePosition.FRONT, simulator,
                makeGeometry(lane, position, 1.0), compatible);
        this.destinationNode = compatible.equals(PLUS) || compatible.equals(EVERYTHING) ? lane.getParentLink().getEndNode()
                : lane.getParentLink().getStartNode();
    }

    /** {@inheritDoc} */
    @Override
    public final void triggerResponse(final LaneBasedGtu gtu)
    {
        try
        {
            if (gtu.getStrategicalPlanner().getRoute() == null
                    || gtu.getStrategicalPlanner().getRoute().destinationNode().equals(this.destinationNode))
            {
                gtu.destroy();
            }
        }
        catch (NetworkException exception)
        {
            getSimulator().getLogger().always().error(exception, "Error destroying GTU: {} at destination sensor: {}", gtu,
                    toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DestinationSensor [Lane=" + this.getLane() + "]";
    }

}
