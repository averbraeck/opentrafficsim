package org.opentrafficsim.road.network.control.rampmetering;

import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;

import nl.tudelft.simulation.dsol.logger.SimLogger;

/**
 * Switch implementing the RWS algorithm.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 jun. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RwsSwitch extends SingleCrossSectionSwitch
{

    /** Capacity. */
    private final Frequency capacity;

    /** Flow threshold. */
    private final Frequency flowThreshold;

    /** Speed threshold. */
    private final Speed speedThreshold = new Speed(70, SpeedUnit.KM_PER_HOUR);

    /** Red time. */
    private Duration redTime;
    
    /** Maximum red time. */
    private static Duration maxRedTime = Duration.createSI(10);

    /** Flow in previous time step. */
    private Frequency lastFlow;

    /**
     * @param detectors List&lt;Detector&gt;; detectors
     */
    public RwsSwitch(final List<Detector> detectors)
    {
        super(Duration.createSI(60.0), detectors);
        this.capacity = new Frequency(2000, FrequencyUnit.PER_HOUR).multiplyBy(detectors.size());
        this.flowThreshold = new Frequency(1500, FrequencyUnit.PER_HOUR).multiplyBy(detectors.size());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled()
    {
        Frequency flow = totalFlow();
        SimLogger.always().info("Flow is " + flow.getInUnit(FrequencyUnit.PER_HOUR));
        if (meanSpeed().le(this.speedThreshold)
                || (this.lastFlow != null && flow.gt(this.lastFlow) && flow.gt(this.flowThreshold)))
        {
            // TODO: what if flow is larger than capacity, shouldn't there be a maximum red time?
            if (flow.lt(this.capacity))
            {
                this.redTime = Duration.createSI(1.0 / this.capacity.minus(flow).si);
                this.redTime = Duration.max(this.redTime, maxRedTime);
            }
            else
            {
                this.redTime = maxRedTime;
            }
            this.lastFlow = flow;
            return true;
        }
        this.lastFlow = flow;
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Duration getRedTime()
    {
        Throw.whenNull(this.redTime, "The method isEnabled() in a RwsSwitch should set a red time.");
        return this.redTime;
    }

}
