package org.opentrafficsim.road.network.lane.object.sensor;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Sensor that prints which GTU triggers it.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class SimpleReportingSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * Construct a new SimpleReportingSensor.
     * @param lane Lane; the lane on which the new SimpleReportingSensor will be located
     * @param position Length; the position of the sensor along the lane
     * @param triggerPosition RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the sensor
     * @param id String; the id of the new SimpleReportingSensor
     * @param simulator OTSSimulatorInterface; the simulator to enable animation
     * @param compatible Compatible; object that can decide if a particular GTU type in a particular driving direction will
     *            trigger the new SimpleReportingSensor
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SimpleReportingSensor(final String id, final Lane lane, final Length position,
            final RelativePosition.TYPE triggerPosition, final OtsSimulatorInterface simulator, final Compatible compatible)
            throws NetworkException
    {
        super(id, lane, position, triggerPosition, simulator, compatible);
    }

    /** {@inheritDoc} */
    @Override
    public final void triggerResponse(final LaneBasedGtu gtu)
    {
        System.out.println(this + " triggered by " + getPositionType().getName() + " of " + gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SimpleReportingSensor []";
    }

}
