package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * This is a sensor that detects GTU's on a lane, and is able to pass
 * information by sending a pulse with a time stamp.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA,
 * Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a
 * href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * 
 * @version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander
 *         Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SensorLane extends AbstractSensor {
	/** */
	private static final long serialVersionUID = 20141231L;

	private Rel<LengthUnit> width;
	private String name;
	/**
	 * Place a sensor that is triggered with the back of the GTU one ulp (see
	 * <code>Math.ulp(double d)</code>) before the end of the lane to make sure
	 * it will always be triggered, independent of the algorithm used to move
	 * the GTU.
	 * 
	 * @param lane
	 *            The lane for which this is a sensor.
	 */
	public SensorLane(final Lane lane,
			DoubleScalar.Rel<LengthUnit> longitudinalPositionFromEnd,
			DoubleScalar.Rel<LengthUnit> width,
			String name) {
		super(lane, new DoubleScalar.Rel<LengthUnit>(lane.getLength().getSI() - longitudinalPositionFromEnd.getSI() ,
                LengthUnit.METER), RelativePosition.FRONT);
		this.width = width;
		this.name = name;
	}

	/**
	 * {@inheritDoc} <br>
	 * For this method, we assume that the right sensor triggered this method.
	 * In this case the sensor that indicates the front of the GTU. The code
	 * triggering the sensor therefore has to do the checking for sensor type.
	 */
	@Override
	public final void trigger(final LaneBasedGTU<?> gtu) {
		try
		{
		    System.out.println(gtu.getSimulator().getSimulatorTime().get() + ": detecting " + gtu + " passing detector at lane " 
		            + getLane());
		}
		catch (RemoteException exception)
		{
		    exception.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("checkstyle:designforextension")
	public String toString() {
		return "SensorAtLane [getLane()=" + this.getLane()
				+ ", getLongitudinalPosition()="
				+ this.getLongitudinalPosition() + ", getPositionType()="
				+ this.getPositionType() + "]";
	}

	public Rel<LengthUnit> getWidth() {
		return width;
	}

	public void setWidth(Rel<LengthUnit> width) {
		this.width = width;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
