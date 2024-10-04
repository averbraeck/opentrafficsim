package org.opentrafficsim.road.network.control.rampmetering;

import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.road.network.lane.object.detector.LoopDetector;

/**
 * Switch implementing the RWS algorithm.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RwsSwitch extends SingleCrossSectionSwitch
{

    /** Maximum cycle time. */
    private static final Duration MAX_CYCLE_TIME = Duration.instantiateSI(15);

    /** Capacity. */
    private final Frequency capacity;

    /** Flow threshold. */
    private final Frequency flowThreshold;

    /** Speed threshold. */
    private final Speed speedThreshold = new Speed(70, SpeedUnit.KM_PER_HOUR);

    /** Red time. */
    private Duration cycleTime;

    /** Flow in previous time step. */
    private Frequency lastFlow;

    /**
     * @param detectors detectors
     */
    public RwsSwitch(final List<LoopDetector> detectors)
    {
        super(Duration.instantiateSI(60.0), detectors);
        this.capacity = new Frequency(2000, FrequencyUnit.PER_HOUR).times(detectors.size());
        this.flowThreshold = new Frequency(1500, FrequencyUnit.PER_HOUR).times(detectors.size());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled()
    {
        Frequency flow = totalFlow();
        CategoryLogger.always().info("Flow is " + flow.getInUnit(FrequencyUnit.PER_HOUR));
        if (meanSpeed().le(this.speedThreshold)
                || (this.lastFlow != null && flow.gt(this.lastFlow) && flow.gt(this.flowThreshold)))
        {
            if (flow.lt(this.capacity))
            {
                this.cycleTime = Duration.instantiateSI(1.0 / this.capacity.minus(flow).si);
                this.cycleTime = Duration.min(this.cycleTime, MAX_CYCLE_TIME);
            }
            else
            {
                this.cycleTime = MAX_CYCLE_TIME;
            }
            this.lastFlow = flow;
            return true;
        }
        this.lastFlow = flow;
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Duration getCycleTime()
    {
        Throw.whenNull(this.cycleTime, "The method isEnabled() in a RwsSwitch should set a cycle time.");
        return this.cycleTime;
    }

}
