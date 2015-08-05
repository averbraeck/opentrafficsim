package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleCheckPulses<ID> {

	/** The simulator that controls everything. */
	final OTSDEVSSimulatorInterface simulator;

	HashMap<String, SensorLaneST> mapSensor;

	public ScheduleCheckPulses(OTSDEVSSimulatorInterface simulator,
			HashMap<String, SensorLaneST> mapSensor) throws RemoteException,
			SimRuntimeException, NetworkException, GTUException,
			NamingException {
		this.simulator = simulator;
		this.mapSensor = mapSensor;
		scheduleCheckPulses();
	}

	/**
	 * Schedule generation of the next GTU.
	 * 
	 * @param simulator
	 * @throws SimRuntimeException
	 * @throws RemoteException
	 * @throws NamingException
	 * @throws NetworkException
	 * @throws GTUException
	 */
	public void scheduleCheckPulses() throws RemoteException,
			SimRuntimeException, GTUException, NetworkException,
			NamingException {
		for (Entry<String, SensorLaneST> entry : this.mapSensor.entrySet()) {
			entry.getKey();
			SensorLaneST sensor = entry.getValue();
			HashMap<DoubleScalar.Abs<TimeUnit>, Integer> pulses = sensor
					.getStatusByTime();
			for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entryPulse : pulses
					.entrySet()) {
				if (entryPulse.getValue() == 1) {
					DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
					this.simulator.scheduleEventAbs(when, this, this,
							"findNearestVehicles", new Object[] { sensor });
				}
			}

		}
	}

	private final void findNearestVehicles(SensorLaneST sensor)
			throws RemoteException, NetworkException {
		Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialPositions = new LinkedHashMap<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>>();
		DoubleScalar.Rel<LengthUnit> initialPosition = sensor
				.getLongitudinalPosition();
		List<SensorLaneST> parallelSensors = sensor.findParallelSensors();
		Set<Lane<?, ?>> lanes = sensor.getLane().accessibleAdjacentLanes(
				LateralDirectionality.LEFT, GTUType.ALL);
		lanes.addAll(sensor.getLane().accessibleAdjacentLanes(
				LateralDirectionality.RIGHT, GTUType.ALL));
		lanes.add(sensor.getLane());
		double minDist = Double.POSITIVE_INFINITY;
		double sX = sensor.getLocation().getX();
		double sY = sensor.getLocation().getY();
		LaneBasedGTU<?> gtuNearestBefore = null;
		LaneBasedGTU<?> gtuNearestAfter = null;
		LaneBasedGTU<?> gtuNearest = null;
		LaneBasedGTU<?> gtuMove = null;
		Lane laneBefore = null;
		Lane laneAfter = null;

		for (Lane aLane : lanes) {
			LaneBasedGTU<?> gtuBefore = aLane.getGtuBefore(initialPosition,
					RelativePosition.FRONT, this.simulator.getSimulatorTime()
							.get());
			LaneBasedGTU<?> gtuAfter = aLane.getGtuAfter(initialPosition,
					RelativePosition.FRONT, this.simulator.getSimulatorTime()
							.get());

			double x = gtuBefore.getLocation().getX();
			double y = gtuBefore.getLocation().getY();
			double dist = Math.sqrt(Math.pow(2, sX - x) + Math.pow(2, sY - y));
			if (dist < minDist) {
				gtuNearestBefore = gtuBefore;
				laneBefore = aLane;
			}
			x = gtuAfter.getLocation().getX();
			y = gtuAfter.getLocation().getY();
			dist = Math.sqrt(Math.pow(2, sX - x) + Math.pow(2, sY - y));
			if (dist < minDist) {
				gtuNearestAfter = gtuAfter;
				laneAfter = aLane;
			}
		}
		//FIXME aanpassen code hieronder!!!!!!!!!!!!!!!!!!!!! 
		gtuNearest.removeLane(laneBefore);
		laneBefore.removeGTU(gtuNearest);
        sensor.getLane().addGTU(gtuNearest, new DoubleScalar.Rel<LengthUnit>(sensor.getLongitudinalPosition().getSI()-gtuNearest.getFront().getDx().getSI(), LengthUnit.SI));
        gtuNearest.addFrontToSubsequentLane(sensor.getLane());

		// gtuBefore. ; move to another position

	}
}
