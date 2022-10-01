package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.control.CACC;
import org.opentrafficsim.road.gtu.lane.control.ControlTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneRecordInterface;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedAbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * CACC perception.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CaccPerception extends LaneBasedAbstractPerceptionCategory implements LongitudinalControllerPerception
{

    /** */
    private static final long serialVersionUID = 20190312L;

    /** Onboard sensors in the form of a headway GTU type. */
    private final HeadwayGtuType sensors;

    /**
     * Constructor using default sensors with zero delay.
     * @param perception LanePerception; perception
     */
    public CaccPerception(final LanePerception perception)
    {
        this(perception, new DefaultCaccSensors());
    }

    /**
     * Constructor using specified sensors.
     * @param perception LanePerception; perception
     * @param sensors HeadwayGtuType; onboard sensor information
     */
    public CaccPerception(final LanePerception perception, final HeadwayGtuType sensors)
    {
        super(perception);
        this.sensors = sensors;
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GtuException, NetworkException, ParameterException
    {
        // lazy evaluation
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionCollectable<HeadwayGtu, LaneBasedGtu> getLeaders()
    {
        return computeIfAbsent("leaders", () -> computeLeaders());
    }

    /**
     * Computes leaders.
     * @return perception iterable for leaders
     */
    private PerceptionCollectable<HeadwayGtu, LaneBasedGtu> computeLeaders()
    {
        try
        {
            LaneStructureRecord record = getPerception().getLaneStructure().getRootRecord();
            Length pos = record.getStartDistance().neg();
            pos = pos.plus(getGtu().getFront().getDx());
            boolean ignoreIfUpstream = true;
            return new DownstreamNeighboursIterableCACC(getGtu(), record, Length.max(Length.ZERO, pos),
                    ((ControlTacticalPlanner) getGtu().getTacticalPlanner()).getSettings()
                            .getParameter(LongitudinalControllerPerception.RANGE),
                    getGtu().getFront(), this.sensors, RelativeLane.CURRENT, ignoreIfUpstream);
        }
        catch (ParameterException | GtuException exception)
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
    }

    /**
     * Extends the regular {@code DownstreamNeighborsIterable} class with skipping leaders that are not sensed by the system.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private static class DownstreamNeighboursIterableCACC extends DownstreamNeighborsIterable
    {

        /**
         * Constructor.
         * @param perceivingGtu LaneBasedGtu; perceiving GTU
         * @param root LaneRecord&lt;?&gt;; root record
         * @param initialPosition Length; position on the root record
         * @param maxDistance Length; maximum distance to search
         * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
         * @param headwayGtuType HeadwayGtuType; type of HeadwayGtu to return
         * @param lane RelativeLane; relative lane (used for a left/right distinction to prevent dead-locks)
         * @param ignoreIfUpstream boolean; whether to ignore GTU that are partially upstream of a record
         */
        @SuppressWarnings("checkstyle:parameternumber")
        DownstreamNeighboursIterableCACC(final LaneBasedGtu perceivingGtu, final LaneRecordInterface<?> root,
                final Length initialPosition, final Length maxDistance, final RelativePosition relativePosition,
                final HeadwayGtuType headwayGtuType, final RelativeLane lane, final boolean ignoreIfUpstream)
        {
            super(perceivingGtu, root, initialPosition, maxDistance, relativePosition, headwayGtuType, lane, ignoreIfUpstream);
        }

        /** {@inheritDoc} */
        @Override
        protected Entry getNext(final LaneRecordInterface<?> record, final Length position, final Integer counter)
                throws GtuException
        {
            Entry next;
            do
            {
                next = super.getNext(record, position, counter);
            }
            // skip leaders that are not the direct leader and that are not CACC
            while (next != null && first() != null && !(next.getObject().getTacticalPlanner() instanceof CACC));
            return next;
        }

    }

}
