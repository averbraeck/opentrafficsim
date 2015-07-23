/**
 * 
 */
package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * @author p070518
 *
 */
public class SensorLaneSmartTraffic extends SensorLane {

	/** */
	private static final long serialVersionUID = 20141231L;
	private static String sensorType;
	public final static String ENTRANCE = "ENTRANCE";
	public final static String INTERMEDIATE = "INTERMEDIATE";
	public final static String EXIT = "EXIT";

	/**
	 * @param lane
	 * @param longitudinalPositionFromEnd
	 * @param width
	 * @param nameSensor
	 * @param nameJunction
	 */
	public SensorLaneSmartTraffic(Lane lane,
			Rel<LengthUnit> longitudinalPositionFromEnd, Rel<LengthUnit> width,
			String nameSensor, String nameJunction) {
		super(lane, longitudinalPositionFromEnd, width, nameSensor,
				nameJunction);

	}

	// Method to find other parallel detectors (at the start of the simulation)
	public ArrayList<SensorLaneSmartTraffic> ParallelSensors() {
		// find the lane
		ArrayList<SensorLaneSmartTraffic> sensorsSmartTraffic = new ArrayList<SensorLaneSmartTraffic>();
		for (Link<?, ?> link : this.getLane().getParentLink().getStartNode()
				.getLinksOut()) {
			if (link instanceof CrossSectionLink) {
				CrossSectionLink<?, ?> csl = (CrossSectionLink<?, ?>) link;
				for (CrossSectionElement cse : csl.getCrossSectionElementList()) {
					if (cse instanceof Lane && !(cse instanceof NoTrafficLane)) {
						Lane lane = (Lane) cse;
						if (lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0,
								LengthUnit.METER), lane.getLength()) != null) {
							List<Sensor> sensors = lane
									.getSensors(
											new DoubleScalar.Rel<LengthUnit>(0,
													LengthUnit.METER),
											lane.getLength());
							for (Sensor sensor : sensors) {
								if (sensor instanceof SensorLaneSmartTraffic) {
									sensorsSmartTraffic.add((SensorLaneSmartTraffic) sensor);									
								}
							}
						}

					}

				}
			}
		}
		return sensorsSmartTraffic;
	}

	/**
	 * {@inheritDoc} <br>
	 * For this method, we assume that the right sensor triggered this method.
	 * In this case the sensor that indicates the front of the GTU. The code
	 * triggering the sensor therefore has to do the checking for sensor type.
	 */
	@Override
	public void trigger(final LaneBasedGTU<?> gtu) {
		try {
			// log vehicleID and time

			// if there is no pulse from the same external detector (including
			// the other
			// parallel ones!!): start to record "vehicle" and distance from
			// this Sensor

			System.out.println(gtu.getSimulator().getSimulatorTime().get()
					+ ": detecting " + gtu + " passing detector at lane "
					+ getLane());
		} catch (RemoteException exception) {
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

	public static String getSensorType() {
		return sensorType;
	}

	public static void setSensorType(String sensorType) {
		SensorLaneSmartTraffic.sensorType = sensorType;
	}
}
