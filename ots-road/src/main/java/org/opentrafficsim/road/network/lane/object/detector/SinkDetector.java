package org.opentrafficsim.road.network.lane.object.detector;

import java.util.Locale;
import java.util.function.BiPredicate;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * A SinkDetector is a detector that deletes GTUs that hit it, if they comply to a predicate.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SinkDetector extends LaneDetector
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** Predicate to use to destroy all GTUs with routes ending in the end node of this link, possibly via a connector. */
    public static final BiPredicate<SinkDetector, LaneBasedGtu> DESTINATION = new BiPredicate<>()
    {
        /** {@inheritDoc} */
        @Override
        public boolean test(final SinkDetector detector, final LaneBasedGtu gtu)
        {
            Route route = gtu.getStrategicalPlanner().getRoute();
            if (route == null)
            {
                return true;
            }
            Node destination = route.size() == 0 ? null : Try.assign(() -> route.destinationNode(), "Cannot happen.");
            Link link = detector.getLane().getLink();
            if (link.getEndNode().equals(destination))
            {
                return true;
            }
            for (Link nextLink : Try.assign(() -> link.getEndNode().nextLinks(gtu.getType(), link), "Cannot happen"))
            {
                if (nextLink.isConnector() && nextLink.getEndNode().equals(destination))
                {
                    return true;
                }
            }
            return false;
        }
    };

    /** Predicate to select GTUs to destroy. */
    private BiPredicate<SinkDetector, LaneBasedGtu> predicate;

    /**
     * Constructor. All GTUs matching the {@code DetectorType} will be removed.
     * @param lane the lane that triggers the deletion of the GTU.
     * @param position the position of the detector
     * @param simulator the simulator to enable animation.
     * @param detectorType detector type.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SinkDetector(final Lane lane, final Length position, final OtsSimulatorInterface simulator,
            final DetectorType detectorType) throws NetworkException
    {
        this(lane, position, simulator, detectorType, (sink, gtu) -> true);
    }

    /**
     * Constructor.
     * @param lane the lane that triggers the deletion of the GTU.
     * @param position the position of the detector
     * @param simulator the simulator to enable animation.
     * @param detectorType detector type.
     * @param predicate predicate for what GTUs will be destroyed.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SinkDetector(final Lane lane, final Length position, final OtsSimulatorInterface simulator,
            final DetectorType detectorType, final BiPredicate<SinkDetector, LaneBasedGtu> predicate) throws NetworkException
    {
        super(String.format(Locale.US, "Sink@%.3fm", position.si), lane, position, RelativePosition.FRONT, simulator,
                LaneBasedObject.makeGeometry(lane, position, 1.0), detectorType);
        this.predicate = predicate;
    }

    /** {@inheritDoc} */
    @Override
    public final void triggerResponse(final LaneBasedGtu gtu)
    {
        if (willDestroy(gtu))
        {
            gtu.destroy();
        }
    }

    /**
     * Returns whether the GTU will be removed by this sink.
     * @param gtu gtu.
     * @return whether the GTU will be removed by this sink.
     */
    public boolean willDestroy(final LaneBasedGtu gtu)
    {
        return isCompatible(gtu.getType()) && this.predicate.test(this, gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SinkDetector [Lane=" + this.getLane() + "]";
    }

}
