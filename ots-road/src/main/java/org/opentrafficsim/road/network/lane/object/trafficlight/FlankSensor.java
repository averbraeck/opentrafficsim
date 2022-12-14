package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;

/**
 * Embedded sensors used by a TrafficLightSensor.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FlankSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20161104L;

    /** The parent that must be informed of all flanks. */
    private final TrafficLightSensor parent;

    /**
     * Construct a new FlankSensor.
     * @param id String; the name of the new FlankSensor
     * @param lane Lane; the lane of the new FlankSensor
     * @param longitudinalPosition Length; the longitudinal position of the new FlankSensor
     * @param positionType TYPE; the position on the GTUs that triggers the new FlankSensor
     * @param simulator OTSSimulatorInterface; the simulator engine
     * @param parent TrafficLightSensor; the traffic light sensor that deploys this FlankSensor
     * @param compatible Compatible; object that determines if a GTU is detectable by the new FlankSensor
     * @throws NetworkException when the network is inconsistent
     */
    public FlankSensor(final String id, final Lane lane, final Length longitudinalPosition, final TYPE positionType,
            final OtsSimulatorInterface simulator, final TrafficLightSensor parent, final Compatible compatible)
            throws NetworkException
    {
        super(id, lane, longitudinalPosition, positionType, simulator, compatible);
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    protected final void triggerResponse(final LaneBasedGtu gtu)
    {
        this.parent.signalDetection(this, gtu);
    }

    /**
     * Return the parent (TrafficLightSensor) of this FlankSensor.
     * @return TrafficLightSensor; the parent of this flank sensor
     */
    public final TrafficLightSensor getParent()
    {
        return this.parent;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "FlankSensor [parent=" + this.parent.getId() + "]";
    }

}
