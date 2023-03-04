package org.opentrafficsim.road.network.lane.object.detector;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Detector that prints which GTU triggers it.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class ReportingDetector extends LaneDetector
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * Construct a new ReportingDetector.
     * @param lane Lane; the lane on which the new ReportingDetector will be located
     * @param position Length; the position of the detector along the lane
     * @param triggerPosition RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the detector
     * @param id String; the id of the new ReportingDetector
     * @param simulator OtsSimulatorInterface; the simulator to enable animation
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public ReportingDetector(final String id, final Lane lane, final Length position,
            final RelativePosition.TYPE triggerPosition, final OtsSimulatorInterface simulator, final DetectorType detectorType)
            throws NetworkException
    {
        super(id, lane, position, triggerPosition, simulator, detectorType);
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
        return "ReportingDetector []";
    }

}
