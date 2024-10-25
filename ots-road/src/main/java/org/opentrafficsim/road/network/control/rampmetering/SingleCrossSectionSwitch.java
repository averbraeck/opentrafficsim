package org.opentrafficsim.road.network.control.rampmetering;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.network.lane.object.detector.LoopDetector;

/**
 * Super class for feed-forward controller. This class contains some helper methods for sub-classes.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class SingleCrossSectionSwitch implements RampMeteringSwitch
{

    /** Control interval. */
    private final Duration interval;

    /** Detectors (on downstream section). */
    private final List<LoopDetector> detectors;

    /**
     * Constructor.
     * @param interval interval
     * @param detectors detectors
     */
    public SingleCrossSectionSwitch(final Duration interval, final List<LoopDetector> detectors)
    {
        Throw.whenNull(interval, "Interval may not be null.");
        Throw.when(detectors == null || detectors.size() == 0, IllegalArgumentException.class,
                "At least 1 detector is required.");
        this.interval = interval;
        this.detectors = detectors;
    }

    @Override
    public Duration getInterval()
    {
        return this.interval;
    }

    /**
     * Returns the mean speed over the detectors.
     * @return mean speed over the detectors
     */
    protected final Speed meanSpeed()
    {
        int n = 0;
        double value = 0.0;
        for (LoopDetector detector : this.detectors)
        {
            if (detector.hasLastValue())
            {
                value += detector.getLastValue(LoopDetector.MEAN_SPEED).si;
                n++;
            }
        }
        return Speed.instantiateSI(value / n);
    }

    /**
     * Returns the mean flow over the detectors.
     * @return mean flow over the detectors
     */
    protected final Frequency meanFlow()
    {
        return totalFlow().divide(this.detectors.size());
    }

    /**
     * Returns the total flow over the detectors.
     * @return total flow over the detectors
     */
    protected final Frequency totalFlow()
    {
        double value = 0.0;
        for (LoopDetector detector : this.detectors)
        {
            if (detector.hasLastValue())
            {
                value += detector.getLastFlow().si;
            }
        }
        return Frequency.instantiateSI(value);
    }

}
