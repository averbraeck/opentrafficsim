package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.control.Cacc;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneStructure.Entry;

/**
 * CACC perception.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CaccPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements LongitudinalControllerPerception
{

    /** */
    private static final long serialVersionUID = 20190312L;

    /** Onboard sensors in the form of a headway GTU type. */
    private final HeadwayGtuType sensors;

    /**
     * Constructor using default sensors with zero delay.
     * @param perception perception
     */
    public CaccPerception(final LanePerception perception)
    {
        this(perception, new DefaultCaccSensors());
    }

    /**
     * Constructor using specified sensors.
     * @param perception perception
     * @param sensors onboard sensor information
     */
    public CaccPerception(final LanePerception perception, final HeadwayGtuType sensors)
    {
        super(perception);
        this.sensors = sensors;
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
            Iterator<Entry<LaneBasedGtu>> leaders = getPerception().getLaneStructure().getDownstreamGtus(RelativeLane.CURRENT,
                    RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR).iterator();
            return new AbstractPerceptionReiterable<LaneBasedGtu, HeadwayGtu, LaneBasedGtu>(getGtu())
            {
                /** {@inheritDoc} */
                @Override
                protected Iterator<AbstractPerceptionReiterable<LaneBasedGtu, HeadwayGtu,
                        LaneBasedGtu>.PrimaryIteratorEntry> primaryIterator()
                {
                    return new Iterator<>()
                    {
                        /** Next entry which is the first vehicle, or a further CACC vehicle (defined by tactical planner). */
                        private PrimaryIteratorEntry next;

                        /** To include the first vehicle always. */
                        private boolean first = true;

                        /** {@inheritDoc} */
                        @Override
                        public boolean hasNext()
                        {
                            while (leaders.hasNext() && this.next == null)
                            {
                                Entry<LaneBasedGtu> entry = leaders.next();
                                if (this.first || entry.object().getTacticalPlanner() instanceof Cacc)
                                {
                                    this.next = new PrimaryIteratorEntry(entry.object(), entry.distance());
                                }
                                this.first = false;
                            }
                            return this.next != null;
                        }

                        /** {@inheritDoc} */
                        @Override
                        public AbstractPerceptionReiterable<LaneBasedGtu, HeadwayGtu, LaneBasedGtu>.PrimaryIteratorEntry next()
                        {
                            hasNext();
                            return this.next;
                        }
                    };
                }

                /** {@inheritDoc} */
                @Override
                protected HeadwayGtu perceive(final LaneBasedGtu object, final Length distance)
                        throws GtuException, ParameterException
                {
                    return CaccPerception.this.sensors.createDownstreamGtu(getObject(), object, distance);
                }
            };
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
    }

}
