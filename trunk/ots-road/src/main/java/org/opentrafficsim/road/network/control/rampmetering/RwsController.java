package org.opentrafficsim.road.network.control.rampmetering;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

/**
 * RWS ramp metering traffic light controller.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 jun. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RwsController extends AbstractMeteringLightController
{

    /** Detector on the ramp. */
    private final Detector detector;
    
    /** Vehicle length. */
    private final Length vehicleLength = Length.createSI(4.0);

    /**
     * @param simulator OTSSimulatorInterface; simulator
     * @param trafficLights List&lt;TrafficLight&gt;; traffic lights
     * @param detector Detector; detector on the ramp
     */
    public RwsController(final OTSSimulatorInterface simulator, final List<TrafficLight> trafficLights, final Detector detector)
    {
        super(simulator, trafficLights);
        this.detector = detector;
    }

    /** {@inheritDoc} */
    @Override
    public void enable(final RampMeteringSwitch rampMeteringSwitch)
    {
        // TODO: What vehicle length to use? Detectors usually don't detect vehicle lengths, so this uses a fixed value.
        // TODO: This does not make sense. E.g. 5m / 25m/s = 0.2s
        Duration greenTime = Duration.createSI(3.0); // this.vehicleLength.divideBy(this.detector.getLastValue(Detector.MEAN_SPEED));
        startCycle(rampMeteringSwitch.getRedTime(), greenTime);
    }

}
