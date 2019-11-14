package org.opentrafficsim.road.network.control.rampmetering;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;

/**
 * Super class for feed-forward controller. This class contains some helper methods for sub-classes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 29, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class SingleCrossSectionSwitch implements RampMeteringSwitch
{

    /** Control interval. */
    private final Duration interval;

    /** Detectors (on downstream section). */
    private final List<Detector> detectors;

    /**
     * Constructor.
     * @param interval Duration; interval
     * @param detectors List&lt;Detector&gt;; detectors
     */
    public SingleCrossSectionSwitch(final Duration interval, final List<Detector> detectors)
    {
        Throw.whenNull(interval, "Interval may not be null.");
        Throw.when(detectors == null || detectors.size() == 0, IllegalArgumentException.class,
                "At least 1 detector is required.");
        this.interval = interval;
        this.detectors = detectors;
    }

    /** {@inheritDoc} */
    @Override
    public Duration getInterval()
    {
        return this.interval;
    }

    /**
     * Returns the mean speed over the detectors.
     * @return Speed; mean speed over the detectors
     */
    protected final Speed meanSpeed()
    {
        int n = 0;
        double value = 0.0;
        for (Detector detector : this.detectors)
        {
            if (detector.hasLastValue())
            {
                value += detector.getLastValue(Detector.MEAN_SPEED).si;
                n++;
            }
        }
        return Speed.instantiateSI(value / n);
    }

    /**
     * Returns the mean flow over the detectors.
     * @return Frequency; mean flow over the detectors
     */
    protected final Frequency meanFlow()
    {
        return totalFlow().divide(this.detectors.size());
    }
    
    /**
     * Returns the total flow over the detectors.
     * @return Frequency; total flow over the detectors
     */
    protected final Frequency totalFlow()
    {
        double value = 0.0;
        for (Detector detector : this.detectors)
        {
            if (detector.hasLastValue())
            {
                value += detector.getLastFlow().si;
            }
        }
        return Frequency.instantiateSI(value);
    }

}
