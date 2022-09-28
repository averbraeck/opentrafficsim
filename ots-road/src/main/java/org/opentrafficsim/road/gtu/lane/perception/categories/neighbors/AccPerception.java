package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.control.ControlTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneRecord;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedAbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * ACC perception.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 12, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AccPerception extends LaneBasedAbstractPerceptionCategory implements LongitudinalControllerPerception
{

    /** */
    private static final long serialVersionUID = 20190312L;

    /** Onboard sensors in the form of a headway GTU type. */
    private final HeadwayGtuType sensors;

    /**
     * Constructor using default sensors with zero delay.
     * @param perception LanePerception; perception
     */
    public AccPerception(final LanePerception perception)
    {
        this(perception, new DefaultCaccSensors());
    }

    /**
     * Constructor using specified sensors.
     * @param perception LanePerception; perception
     * @param sensors HeadwayGtuType; headway GTU type that defines the onboard sensor information
     */
    public AccPerception(final LanePerception perception, final HeadwayGtuType sensors)
    {
        super(perception);
        this.sensors = sensors;
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GTUException, NetworkException, ParameterException
    {
        // lazy evaluation
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getLeaders()
    {
        return computeIfAbsent("leaders", () -> computeLeaders());
    }

    /**
     * Computes leaders.
     * @return perception iterable for leaders
     */
    private PerceptionCollectable<HeadwayGTU, LaneBasedGTU> computeLeaders()
    {
        try
        {
            LaneStructureRecord record = getPerception().getLaneStructure().getRootRecord();
            Length pos = record.getStartDistance().neg();
            pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx())
                    : pos.minus(getGtu().getFront().getDx());
            boolean ignoreIfUpstream = true;
            return new DownstreamNeighboursIterableACC(getGtu(), record, Length.max(Length.ZERO, pos),
                    ((ControlTacticalPlanner) getGtu().getTacticalPlanner()).getSettings()
                            .getParameter(LongitudinalControllerPerception.RANGE),
                    getGtu().getFront(), this.sensors, RelativeLane.CURRENT, ignoreIfUpstream);
        }
        catch (ParameterException | GTUException exception)
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
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 13, 2019 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class DownstreamNeighboursIterableACC extends DownstreamNeighborsIterable
    {

        /**
         * Constructor.
         * @param perceivingGtu LaneBasedGTU; perceiving GTU
         * @param root LaneRecord&lt;?&gt;; root record
         * @param initialPosition Length; position on the root record
         * @param maxDistance Length; maximum distance to search
         * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
         * @param headwayGtuType HeadwayGtuType; type of HeadwayGTU to return
         * @param lane RelativeLane; relative lane (used for a left/right distinction to prevent dead-locks)
         * @param ignoreIfUpstream boolean; whether to ignore GTU that are partially upstream of a record
         */
        @SuppressWarnings("checkstyle:parameternumber")
        DownstreamNeighboursIterableACC(final LaneBasedGTU perceivingGtu, final LaneRecord<?> root,
                final Length initialPosition, final Length maxDistance, final RelativePosition relativePosition,
                final HeadwayGtuType headwayGtuType, final RelativeLane lane, final boolean ignoreIfUpstream)
        {
            super(perceivingGtu, root, initialPosition, maxDistance, relativePosition, headwayGtuType, lane, ignoreIfUpstream);
        }

        /** {@inheritDoc} */
        @Override
        protected Entry getNext(final LaneRecord<?> record, final Length position, final Integer counter) throws GTUException
        {
            Entry next;
            do
            {
                next = super.getNext(record, position, counter);
            }
            // skip leaders that are not the direct leader and that are not CACC
            while (next != null && first() != null);
            return next;
        }

    }

}
