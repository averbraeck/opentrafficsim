package org.opentrafficsim.road.network.lane.object.detector;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A DestinationDetector is a detector that deletes a GTU that has the node it will pass after this detector as its destination.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class DestinationDetector extends LaneDetector
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** the destination node for which this is the DestinationDetector. */
    private final Node destinationNode;

    /**
     * @param lane Lane; the lane that triggers the deletion of the GTU.
     * @param position Length; the position of the detector
     * @param simulator OtsSimulatorInterface; the simulator to enable animation.
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public DestinationDetector(final Lane lane, final Length position, final OtsSimulatorInterface simulator,
            final DetectorType detectorType) throws NetworkException
    {
        super("DESTINATION@" + lane.getFullId(), lane, position, RelativePosition.FRONT, simulator,
                makeGeometry(lane, position, 1.0), detectorType);
        this.destinationNode = lane.getParentLink().getEndNode();
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
            getSimulator().getLogger().always().error(exception, "Error destroying GTU: {} at destination detector: {}", gtu,
                    toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DestinationDetector [Lane=" + this.getLane() + "]";
    }

}
