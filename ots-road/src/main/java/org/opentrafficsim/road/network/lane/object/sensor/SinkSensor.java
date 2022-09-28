package org.opentrafficsim.road.network.lane.object.sensor;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A SinkSensor is a sensor that deletes every GTU that hits it.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class SinkSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * @param lane Lane; the lane that triggers the deletion of the GTU.
     * @param position Length; the position of the sensor
     * @param gtuDirectionality GTUDirectionality; GTU directionality
     * @param simulator OTSSimulatorInterface; the simulator to enable animation.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SinkSensor(final Lane lane, final Length position, final GTUDirectionality gtuDirectionality,
            final OTSSimulatorInterface simulator) throws NetworkException
    {
        this(lane, position, gtuDirectionality.isPlus() ? Compatible.PLUS : Compatible.MINUS, simulator);
    }

    /**
     * @param lane Lane; the lane that triggers the deletion of the GTU.
     * @param position Length; the position of the sensor
     * @param compatible Compatible; compatible GTU type and direction
     * @param simulator OTSSimulatorInterface; the simulator to enable animation.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SinkSensor(final Lane lane, final Length position, final Compatible compatible,
            final OTSSimulatorInterface simulator) throws NetworkException
    {
        super("SINK@" + lane.getFullId() + "." + position, lane, position, RelativePosition.FRONT, simulator,
                makeGeometry(lane, position, 1.0), compatible);
    }

    /** {@inheritDoc} */
    @Override
    public final void triggerResponse(final LaneBasedGTU gtu)
    {
        gtu.destroy();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SinkSensor [Lane=" + this.getLane() + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SinkSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator) throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof OTSSimulatorInterface), NetworkException.class,
                "simulator should be a DEVSSimulator");
        return new SinkSensor((Lane) newCSE, getLongitudinalPosition(), getDetectedGTUTypes(),
                (OTSSimulatorInterface) newSimulator);
    }

}
