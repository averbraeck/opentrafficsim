package nl.grontmij.smarttraffic.lane;

import java.awt.Color;
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
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.animation.DefaultSensorAnimation;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.LaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleCheckPulses<ID> {

	/** The simulator that controls everything. */
	final OTSDEVSSimulatorInterface simulator;

	HashMap<String, SensorLaneST> mapSensor;

	public ScheduleCheckPulses(OTSDEVSSimulatorInterface simulator,
			HashMap<String, SensorLaneST> mapSensor, Double[] range)
			throws RemoteException, SimRuntimeException, NetworkException,
			GTUException, NamingException {
		this.simulator = simulator;
		this.mapSensor = mapSensor;
		scheduleCheckPulses(range);
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
	public void scheduleCheckPulses(Double[] range) throws RemoteException,
			SimRuntimeException, GTUException, NetworkException,
			NamingException {
		for (Entry<String, SensorLaneST> entry : this.mapSensor.entrySet()) {
			entry.getKey();
			SensorLaneST sensor = entry.getValue();
			HashMap<DoubleScalar.Abs<TimeUnit>, Integer> pulses = sensor
					.getStatusByTime();
			for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entryPulse : pulses
					.entrySet()) {
				if (entryPulse.getValue() == 0) {
					DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
					this.simulator.scheduleEventAbs(when, this, this,
							"findNearestVehicles",
							new Object[] { sensor, range });
				}
			}

		}
	}

	private final void findNearestVehicles(SensorLaneST sensor, Double[] range)
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
		double minDistBefore = range[0];
		double minDistAfter = range[1];
		double sX = sensor.getLocation().getX();
		double sY = sensor.getLocation().getY();
		LaneBasedGTU<?> gtuNearestBefore = null;
		LaneBasedGTU<?> gtuNearestAfter = null;
		LaneBasedGTU<?> gtuNearest = null;
		LaneBasedGTU<?> gtuMove = null;
		Lane<?, ?> laneBefore = null;
		Lane<?, ?> laneAfter = null;
		double lowestDistBefore = Double.POSITIVE_INFINITY;
		double lowestDistAfter = Double.POSITIVE_INFINITY;

		for (Lane<?, ?> aLane : lanes) {
			LaneBasedGTU<?> gtuBefore = aLane.getGtuBefore(initialPosition,
					RelativePosition.FRONT, this.simulator.getSimulatorTime()
							.get());
			LaneBasedGTU<?> gtuAfter = aLane.getGtuAfter(initialPosition,
					RelativePosition.FRONT, this.simulator.getSimulatorTime()
							.get());
			double dist;

			if (gtuBefore != null && !(gtuBefore instanceof TrafficLightOnOff)) {
				double x = gtuBefore.getLocation().getX();
				double y = gtuBefore.getLocation().getY();
				dist = Math.sqrt(Math.pow(2, sX - x) + Math.pow(2, sY - y));
				if (dist < minDistBefore) {
					gtuNearestBefore = gtuBefore;
					laneBefore = aLane;
					lowestDistBefore = dist;
				}
			} else {

			}

			if (gtuAfter != null && !(gtuAfter instanceof TrafficLightOnOff)) {

				double x = gtuAfter.getLocation().getX();
				double y = gtuAfter.getLocation().getY();
				dist = Math.sqrt(Math.pow(2, sX - x) + Math.pow(2, sY - y));
				if (dist < minDistAfter) {
					gtuNearestAfter = gtuAfter;
					laneAfter = aLane;
					lowestDistAfter = dist;
				}
			}
		}
		// FIXME aanpassen code hieronder!!!!!!!!!!!!!!!!!!!!!
		if (lowestDistAfter < lowestDistBefore && gtuNearestAfter != null) {
			gtuNearest = gtuNearestAfter;
		} else if (lowestDistAfter >= lowestDistBefore
				&& gtuNearestBefore != null) {
			gtuNearest = gtuNearestBefore;
		}
		if (gtuNearest != null) {
			gtuNearest.leaveLane(laneBefore);
			sensor.getLane().addGTU(
					gtuNearest,
					new DoubleScalar.Rel<LengthUnit>(sensor
							.getLongitudinalPosition().getSI()
							- gtuNearest.getFront().getDx().getSI(),
							LengthUnit.SI));
		}
		// TODO  als er geen voertuig is gevonden: creeer dan een nieuw voertuig
/*		else {
			try {
				this.simulator
						.scheduleEventNow(this, this, "generateCar", null);
			} catch (SimRuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

	}

/*	*//**
	 * Generate one car and re-schedule this method.
	 *//*
	protected final void generateCar() {
		DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(
				0, LengthUnit.METER);
		Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialPositions = new LinkedHashMap<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>>();
		initialPositions.put(this.lane, initialPosition);
		try {
			DoubleScalar.Rel<LengthUnit> vehicleLength = new DoubleScalar.Rel<LengthUnit>(
					4, LengthUnit.METER);
			new LaneBasedIndividualCar<Integer>(
					++this.carsCreated,
					this.gtuType,
					this.gtuFollowingModel,
					this.laneChangeModel,
					initialPositions,
					this.initialSpeed,
					vehicleLength,
					new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER),
					new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR),
					// this.routeGenerator.generateRouteNavigator(),
					this.routeNavigator, this.simulator,
					DefaultCarAnimation.class, this.gtuColorer);
		} catch (RemoteException | SimRuntimeException | NamingException
				| NetworkException | GTUException exception) {
			exception.printStackTrace();
		}
	}*/

}
