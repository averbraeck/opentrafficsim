package org.opentrafficsim.core.network.lane;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.LaneBlock;
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
     * @param simulator the simulator to allow animation
     * @throws NetworkException when sink sensor cannot be added to the lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SinkLane(final CrossSectionLink<LINKID, NODEID> parentLink,
        final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> beginWidth,
        final LaneType<?> laneType, final LongitudinalDirectionality directionality,
        final DoubleScalar.Abs<SpeedUnit> speedLimit, final OTSSimulatorInterface simulator) throws NetworkException,
        OTSGeometryException
    {
        super(parentLink, lateralOffsetAtStart, lateralOffsetAtStart, beginWidth, beginWidth, laneType, directionality,
            new DoubleScalar.Abs<FrequencyUnit>(1, FrequencyUnit.SI), speedLimit);
        getSensors().clear(); // no standard sensors...
        addSensor(new SinkSensor(this, simulator));
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
         * @param simulator the simulator to enable animation.
         */
        public SinkSensor(final Lane<?, ?> lane, final OTSSimulatorInterface simulator)
        {
            super(lane, new DoubleScalar.Rel<LengthUnit>(0.25, LengthUnit.METER), RelativePosition.FRONT, "SINK@"
                + lane.toString(), simulator);
            try
            {
                new SinkAnimation(this, simulator);
            }
            catch (RemoteException | NamingException exception)
            {
                exception.printStackTrace();
            }
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
    
    /**
     * sink sensor animation.
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
    private static class SinkAnimation extends Renderable2D
    {
        /** the half width left and right of the center line that is used to draw the block. */ 
        private final double halfWidth;
        
        /**
         * Construct the DefaultCarAnimation for a LaneBlock (road block).
         * @param source the Car to draw
         * @param simulator the simulator to schedule on
         * @throws NamingException in case of registration failure of the animation
         * @throws RemoteException in case of remote registration failure of the animation
         */
        public SinkAnimation(final SinkSensor source, final OTSSimulatorInterface simulator) throws NamingException,
            RemoteException
        {
            super(source, simulator);
            this.halfWidth = 0.4 * source.getLane().getWidth(0.0).getSI();
        }

        /** {@inheritDoc} */
        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
        {
            graphics.setColor(Color.YELLOW);
            Rectangle2D rectangle = new Rectangle2D.Double(-0.25, -this.halfWidth, 0.5, 2 * this.halfWidth);
            graphics.fill(rectangle);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "SinkAnimation [getSource()=" + this.getSource() + "]";
        }
    }
}
