package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;

import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Lane that deletes a vehicle and unregisters its animation when the vehicle enters the lanee with its front.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jan 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class SinkLane<LINKID, NODEID> extends Lane<LINKID, NODEID>
{
    /**
     * Construct a SinkLane.
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit on the new SinkLane
     * @throws NetworkException when sink sensor cannot be added to the lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SinkLane(final CrossSectionLink<LINKID, NODEID> parentLink,
        final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> beginWidth,
        final LaneType<?> laneType, final LongitudinalDirectionality directionality,
        final DoubleScalar.Abs<SpeedUnit> speedLimit) throws NetworkException, OTSGeometryException
    {
        super(parentLink, lateralOffsetAtStart, lateralOffsetAtStart, beginWidth, beginWidth, laneType, directionality,
            new DoubleScalar.Abs<FrequencyUnit>(1, FrequencyUnit.SI), speedLimit);
        getSensors().clear(); // no standard sensors...
        addSensor(new SinkSensor(this));
    }

    /**
     * sensor that deletes the GTU.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version an 30, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    private static class SinkSensor extends AbstractSensor
    {
        /** */
        private static final long serialVersionUID = 20150130L;

        /**
         * @param lane the lane that triggers the deletion of the GTU.
         */
        public SinkSensor(final Lane<?, ?> lane)
        {
            super(lane, new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER), RelativePosition.FRONT, "SINK@"
                + lane.toString());
        }

        /** {@inheritDoc} */
        @Override
        public void trigger(final LaneBasedGTU<?> gtu) throws RemoteException
        {
            gtu.destroy();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "SinkSensor [Lane=" + this.getLane() + "]";
        }
    }
}
