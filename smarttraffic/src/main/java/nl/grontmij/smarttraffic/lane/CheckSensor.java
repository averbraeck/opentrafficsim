/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteRoute;

/**
 * @author p070518
 */
public class CheckSensor extends AbstractSensor {

	/** */

	private static final long serialVersionUID = 20141231L;

	/** the color to display. */
	private Color color = Color.ORANGE;

	private String sensorType;

	private String nameJunction;

	private List<CheckSensor> sensorsParallel = new ArrayList<CheckSensor>();

	private HashMap<Time.Abs, Integer> statusByTime = new HashMap<Time.Abs, Integer>();

	private Integer currentStatus;

	private Time.Abs timeOfLastChange;

	private ArrayList<LaneBasedIndividualCar> gtuList = new ArrayList<>();

	private Boolean exitLaneSensor = null;

	private Boolean trafficLightSensor = null;

	/**
	 * @param lane
	 * @param longitudinalPositionFromEnd
	 * @param nameSensor
	 * @param nameJunction
	 */
	public CheckSensor(Lane lane, Length.Rel longitudinalPositionFromEnd,
			final RelativePosition.TYPE front, String nameSensor,
			final OTSDEVSSimulatorInterface simulator) {
		super(lane, longitudinalPositionFromEnd, front, nameSensor, simulator);
		try {
			new CheckSensorAnimation(this, getSimulator());
		} catch (RemoteException | NamingException exception) {
			exception.printStackTrace();
		}
		checkGtuListForDeletion();
	}

	public void checkGtuListForDeletion() {
		try {
			this.getSimulator().scheduleEventAbs(
					new Time.Abs(0.0, TimeUnit.SECOND), this, this,
					"checkGtuList", null);
		} catch (RemoteException | SimRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void checkGtuList() {
		while (!this.getGtuList().isEmpty()) {
			try {
				ScheduleCheckPulses.nearGTUfront2(this, 100);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
	}
	/**
	 * {@inheritDoc} <br>
	 * For this method, we assume that the right sensor triggered this method.
	 * In this case the sensor that indicates the front of the GTU. The code
	 * triggering the sensor therefore has to do the checking for sensor type.
	 */
	@Override
	public void trigger(final LaneBasedGTU gtu) {
		// add the gtu to the list of cars that have been triggered
		this.getGtuList().add((LaneBasedIndividualCar) gtu);
		ReportNumbers.reportPassingVehicles(GTM.outputFileVehiclesTriggered,
				gtu, "C" + this.getName(), this.getSimulator());
	}

	/*    *//**
	 * {@inheritDoc} <br>
	 * For this method, we assume that the right sensor triggered this method.
	 * In this case the sensor that indicates the front of the GTU. The code
	 * triggering the sensor therefore has to do the checking for sensor type.
	 */
	/*
	 * @Override public void trigger(final LaneBasedGTU gtu) { // add the sensor
	 * that has been triggered if
	 * (GTM.listGTUsInNetwork.get((LaneBasedIndividualCar) gtu) != null) {
	 * GTM.listGTUsInNetwork.get((LaneBasedIndividualCar) gtu).add(this); if
	 * (gtu.getId().equals(2363)) { System.out.println("stop"); }; } else {
	 * LinkedList<CheckSensor> linkedList = new LinkedList<CheckSensor>();
	 * linkedList.add(this); GTM.listGTUsInNetwork.put((LaneBasedIndividualCar)
	 * gtu, linkedList); }
	 * ReportNumbers.reportPassingVehicles(GTM.outputFileVehiclesTriggered, gtu,
	 * "C" + this.getName(), this.getSimulator()); }
	 */

	/**
	 * @return true if the sensor is less than 10 m before the end, the lane is
	 *         on one of the main routes, and the next lane does not end as part
	 *         of one of the main routes (the end node of the next lane is not
	 *         part of the main routes).
	 */
	public boolean isExitLaneSensor(List<CompleteRoute> routes) {
		if (this.exitLaneSensor == null) {
			this.exitLaneSensor = false;
			if (getLane().getLength().getSI() - getLongitudinalPositionSI() < 10.0) // less
																					// than
																					// 10.0
																					// m
																					// before
																					// end?
			{
				if (isOnMainRoute(getLane(), routes)) {
					if (getLane().nextLanes(GTM.GTUTYPE).size() > 0) {
						Lane nextLane = getLane().nextLanes(GTM.GTUTYPE)
								.iterator().next();
						if (!isOnMainRoute(nextLane, routes))
							this.exitLaneSensor = true;
					}
				}
			}
		}
		return this.exitLaneSensor;
	}

	public boolean isTrafficLightSensor() {
		if (this.trafficLightSensor == null) {
			this.trafficLightSensor = false;
			for (LaneBasedGTU gtu : getLane().getGtuList()) {
				if (gtu instanceof TrafficLight) {
					try {
						if (Math.abs(getLongitudinalPositionSI()
								- gtu.position(getLane(), gtu.getReference())
										.getSI()) < 10.0) {
							this.trafficLightSensor = true;
						}
					} catch (RemoteException | NetworkException exception) {
						exception.printStackTrace();
					}
				}
			}
		}
		return this.trafficLightSensor;
	}

	private boolean isOnMainRoute(Lane lane, List<CompleteRoute> routes) {
		boolean onMain = false;
		for (CompleteRoute route : routes) {
			if (route.containsLink(lane.getParentLink())) {
				onMain = true;
			}
		}
		return onMain;
	}

	public void changeColor(final int newColor) {
		switch (newColor) {
		case 0:
			this.setColor(Color.WHITE);
			break;

		case 1:
			this.setColor(Color.BLUE);
			break;

		default:
			break;
		}
	}

	public String getSensorType() {
		return this.sensorType;
	}

	public String getNameJunction() {
		return this.nameJunction;
	}

	public List<CheckSensor> getSensorsParallel() {
		return this.sensorsParallel;
	}

	public HashMap<Time.Abs, Integer> getStatusByTime() {
		return this.statusByTime;
	}

	public void setStatusByTime(HashMap<Time.Abs, Integer> statusByTime) {
		this.statusByTime = statusByTime;
	}

	public void addStatusByTime(Time.Abs timeNow, Integer status) {
		this.statusByTime.put(timeNow, status);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "SensorAtLane [getLane()=" + this.getLane()
				+ ", getLongitudinalPosition()="
				+ this.getLongitudinalPosition() + ", getPositionType()="
				+ this.getPositionType() + "]";
	}

	public Integer getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(Integer currentStatus) {
		this.currentStatus = currentStatus;
	}

	public Time.Abs getTimeOfLastChange() {
		return timeOfLastChange;
	}

	public void setTimeOfLastChange(Time.Abs timeOfLastChange) {
		this.timeOfLastChange = timeOfLastChange;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public ArrayList<LaneBasedIndividualCar> getGtuList() {
		return gtuList;
	}

	public void setGtuList(ArrayList<LaneBasedIndividualCar> gtuList) {
		this.gtuList = gtuList;
	}

}
