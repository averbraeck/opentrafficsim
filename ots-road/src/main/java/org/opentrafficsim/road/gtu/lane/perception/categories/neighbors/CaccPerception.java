package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.exceptions.Try;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.control.Cacc;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;

/**
 * CACC perception.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CaccPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements LongitudinalControllerPerception
{

    /** Onboard sensors in the form of a headway GTU type. */
    private final PerceivedGtuType sensors;

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
    public CaccPerception(final LanePerception perception, final PerceivedGtuType sensors)
    {
        super(perception);
        this.sensors = sensors;
    }

    @Override
    public PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getLeaders()
    {
        return computeIfAbsent("leaders", () -> computeLeaders());
    }

    /**
     * Computes leaders.
     * @return perception iterable for leaders
     */
    private PerceptionCollectable<PerceivedGtu, LaneBasedGtu> computeLeaders()
    {
        final Set<Byte> firstIfNotEmpty = new LinkedHashSet<>(); // hack to let lambda expression govern a "boolean" value
        firstIfNotEmpty.add((byte) 1);
        try
        {
            Iterable<Entry<LaneBasedGtu>> leaders = new FilteredIterable<Entry<LaneBasedGtu>>(
                    getPerception().getLaneStructure().getDownstreamGtus(RelativeLane.CURRENT, RelativePosition.FRONT,
                            RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR),
                    (t) ->
                    {
                        if (!firstIfNotEmpty.isEmpty() || t.object().getTacticalPlanner() instanceof Cacc)
                        {
                            firstIfNotEmpty.clear(); // if not empty -> first leader, so include always
                            return true;
                        }
                        return false;
                    });
            return new PerceptionReiterable<LaneBasedGtu, PerceivedGtu, LaneBasedGtu>(getGtu(), leaders, (object, distance) ->
            {
                return Try.assign(
                        () -> CaccPerception.this.sensors.createPerceivedGtu(getGtu(), getGtu(), object, distance, true),
                        "ParameterException while creating PerceivedGtu");
            });
        }
        catch (ParameterException exception)
        {
            throw new OtsRuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
    }

}
