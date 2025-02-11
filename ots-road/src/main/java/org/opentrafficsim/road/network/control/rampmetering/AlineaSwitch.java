package org.opentrafficsim.road.network.control.rampmetering;

import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.road.network.lane.object.detector.LoopDetector;

/**
 * Switch implementing the ALINEA algorithm.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AlineaSwitch extends SingleCrossSectionSwitch
{

    /** Maximum cycle time. */
    private static final Duration MAX_CYCLE_TIME = Duration.instantiateSI(15);

    /** Capacity. */
    private final Frequency capacity;

    /** Flow threshold. */
    private final Frequency flowThreshold;

    /** Speed threshold. */
    private final Speed speedThreshold = new Speed(70, SpeedUnit.KM_PER_HOUR);

    /** Cycle time. */
    private Duration cycleTime;

    /** Flow in previous time step. */
    private Frequency lastFlow;

    /**
     * Constructor.
     * @param detectors detectors
     */
    public AlineaSwitch(final List<LoopDetector> detectors)
    {
        super(Duration.instantiateSI(60.0), detectors);
        this.capacity = new Frequency(2000, FrequencyUnit.PER_HOUR).times(detectors.size());
        this.flowThreshold = new Frequency(1500, FrequencyUnit.PER_HOUR).times(detectors.size());
    }

    @Override
    public boolean isEnabled()
    {
        Frequency flow = totalFlow();
        if (meanSpeed().le(this.speedThreshold)
                || (this.lastFlow != null && flow.lt(this.lastFlow) && flow.gt(this.flowThreshold)))
        {
            this.cycleTime = Duration.instantiateSI(1.0 / this.capacity.minus(flow).si);
            this.cycleTime = Duration.min(this.cycleTime, MAX_CYCLE_TIME);
            this.lastFlow = flow;
            return true;
        }
        this.lastFlow = flow;
        return false;
    }

    @Override
    public Duration getCycleTime()
    {
        return this.cycleTime;
    }

}
