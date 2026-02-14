package org.opentrafficsim.road.network.object.detector;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.object.DetectorType;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.object.LaneBasedObject;

/**
 * A SinkDetector is a detector that deletes GTUs that hit it, if they comply to a predicate.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SinkDetector extends LaneDetector
{
    /** Predicate to use to destroy all GTUs with routes ending in the end node of this link, possibly via a connector. */
    public static final BiPredicate<SinkDetector, LaneBasedGtu> DESTINATION = new BiPredicate<>()
    {
        @Override
        public boolean test(final SinkDetector detector, final LaneBasedGtu gtu)
        {
            Optional<Route> route = gtu.getStrategicalPlanner().getRoute();
            if (route.isEmpty())
            {
                return true;
            }
            Node destination =
                    route.get().size() == 0 ? null : Try.assign(() -> route.get().destinationNode(), "Cannot happen.");
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
     * @param detectorType detector type.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SinkDetector(final Lane lane, final Length position, final DetectorType detectorType) throws NetworkException
    {
        this(lane, position, detectorType, (sink, gtu) -> true);
    }

    /**
     * Constructor.
     * @param lane the lane that triggers the deletion of the GTU.
     * @param position the position of the detector
     * @param detectorType detector type.
     * @param predicate predicate for what GTUs will be destroyed.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SinkDetector(final Lane lane, final Length position, final DetectorType detectorType,
            final BiPredicate<SinkDetector, LaneBasedGtu> predicate) throws NetworkException
    {
        super(createId(lane, position), lane, position, RelativePosition.FRONT, LaneBasedObject.makeLine(lane, position, 1.0),
                detectorType);
        this.predicate = predicate;
    }

    /**
     * Returns a unique id for the sink. The basic format is 'Sink@1.234m'. If multiple sinks are within the same rounded
     * millimeter, they will receive an additional number as 'Sink@1.234m_1'. If the number 9 is still not sufficient, the id
     * will be 'Sink.{a UUID}'.
     * @param lane lane of the sink
     * @param position position of the link
     * @return unique id for the sink
     */
    private static String createId(final Lane lane, final Length position)
    {
        String baseId = String.format(Locale.US, "Sink@%.3fm", position.si);
        String id = baseId;
        int i = 1;
        Set<String> currentSinkIds = lane.getDetectors().stream().filter((d) -> d instanceof SinkDetector).map((d) -> d.getId())
                .collect(Collectors.toSet());
        while (i < 10 && currentSinkIds.contains(id))
        {
            id = baseId + "_" + (i++);
        }
        if (i == 10)
        {
            return "Sink." + UUID.randomUUID().toString();
        }
        return id;
    }

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

    @Override
    public final String toString()
    {
        return "SinkDetector [Lane=" + this.getLane() + "]";
    }

}
