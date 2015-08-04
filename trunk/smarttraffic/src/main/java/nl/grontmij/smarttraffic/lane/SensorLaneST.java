/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * @author p070518
 */
public class SensorLaneST extends AbstractSensor {

	/** */
	private static final long serialVersionUID = 20141231L;

	private String sensorType;
	private String nameJunction;
	private List<SensorLaneST> sensorsParallel = new ArrayList<SensorLaneST>();
	private HashMap<DoubleScalar.Rel<TimeUnit>, Integer> statusByTime= new HashMap<DoubleScalar.Rel<TimeUnit>, Integer>(); 

	public final static String ENTRANCE = "ENTRANCE";
	public final static String INTERMEDIATE = "INTERMEDIATE";
	public final static String EXIT = "EXIT";

	public String getSensorType() {
		return sensorType;
	}

	public String getNameJunction() {
		return nameJunction;
	}

	public List<SensorLaneST> getSensorsParallel() {
		return sensorsParallel;
	}

	public HashMap<DoubleScalar.Rel<TimeUnit>, Integer> getStatusByTime() {
		return statusByTime;
	}

	public void setStatusByTime(HashMap<DoubleScalar.Rel<TimeUnit>, Integer> statusByTime) {
		this.statusByTime = statusByTime;
	}

	public void addStatusByTime(DoubleScalar.Rel<TimeUnit> timeNow, Integer status) {
		this.statusByTime.put(timeNow, status);
	}

	
	/**
	 * @param lane
	 * @param longitudinalPositionFromEnd
	 * @param nameSensor
	 * @param nameJunction
	 */
	public SensorLaneST(Lane<?, ?> lane, Rel<LengthUnit> longitudinalPositionFromEnd,
			final RelativePosition.TYPE front, String sensorType,String nameSensor,
			String nameJunction) {
		super(lane, longitudinalPositionFromEnd, front, nameSensor);
		this.nameJunction = nameJunction;
		this.sensorType = sensorType;
	}

	
	
	// Method to find other parallel detectors (at the start of the simulation)
	public List<SensorLaneST> findParallelSensors() {
		// find the lane
		for (Link<?, ?> link : this.getLane().getParentLink().getStartNode()
				.getLinksOut()) {
			if (link instanceof CrossSectionLink) {
				CrossSectionLink<?, ?> csl = (CrossSectionLink<?, ?>) link;
				for (CrossSectionElement<?, ?> cse : csl.getCrossSectionElementList()) {
					if (cse instanceof Lane && !(cse instanceof NoTrafficLane)) {
						Lane<?, ?> lane = (Lane<?, ?>) cse;
						if (lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0,
								LengthUnit.METER), lane.getLength()) != null) {
							List<Sensor> sensors = lane
									.getSensors(
											new DoubleScalar.Rel<LengthUnit>(0,
													LengthUnit.METER), lane
													.getLength());
							for (Sensor sensor : sensors) {
								if (sensor instanceof SensorLaneST) {
									this.getSensorsParallel().add(
											(SensorLaneST) sensor);
								}
							}
						}

					}

				}
			}
		}
		return this.sensorsParallel;
	}

	
	/**
	 * {@inheritDoc} <br>
	 * For this method, we assume that the right sensor triggered this method.
	 * In this case the sensor that indicates the front of the GTU. The code
	 * triggering the sensor therefore has to do the checking for sensor type.
	 */
	@Override
	public void trigger(final LaneBasedGTU<?> gtu) {
		System.out.println(gtu.getSimulator() + ": detecting " + gtu.toString()
				+ " passing detector at lane " + getLane());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SensorAtLane [getLane()=" + this.getLane()
				+ ", getLongitudinalPosition()="
				+ this.getLongitudinalPosition() + ", getPositionType()="
				+ this.getPositionType() + "]";
	}

}
