package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import nl.grontmij.smarttraffic.lane.GTM.TestXMLModel;
import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.LaneBasedRouteGenerator;
import org.opentrafficsim.core.network.route.LaneBasedRouteNavigator;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleGenerateCars<ID> {

	/** Lane on which the generated GTUs are placed. */
	Lane<?, ?> lane;

	/** The type of GTUs generated. */
	final GTUType<ID> gtuType;

	/** The GTU following model used by all generated GTUs. */
	final GTUFollowingModel gtuFollowingModel;

	/** The lane change model used by all generated GTUs. */
	final LaneChangeModel laneChangeModel;

	/** The lane change model used by all generated GTUs. */
	final LaneBasedRouteGenerator routeGenerator;

	/** The lane change model used by all generated GTUs. */
	final LaneBasedRouteNavigator routeNavigator;

	final int generateCar;
	
	final double lengthCar;
	
	/** Initial speed of the generated GTUs. */
	DoubleScalar.Abs<SpeedUnit> initialSpeed;

	/** The GTU colorer that will be linked to each generated GTU. */
	final GTUColorer gtuColorer;

	/** The simulator that controls everything. */
	final OTSDEVSSimulatorInterface simulator;

	HashMap<String, SensorLaneST> mapSensor;

	/** Number of GTUs created. */
	int carsCreated = 0;

	public ScheduleGenerateCars(GTUType<ID> gtuType,
			GTUFollowingModel gtuFollowingModel,
			LaneChangeModel laneChangeModel,
			LaneBasedRouteGenerator routeGenerator,
			GTUColorer gtuColorer,
			OTSDEVSSimulatorInterface simulator,
			HashMap<String, SensorLaneST> mapSensor,
			LaneBasedRouteNavigator route,
			int generateCar, double lengthCar) throws RemoteException,
			SimRuntimeException, NetworkException {
		this.gtuType = gtuType;
		this.gtuFollowingModel = gtuFollowingModel;
		this.laneChangeModel = laneChangeModel;
		this.routeGenerator = routeGenerator;
		this.gtuColorer = gtuColorer;
		this.simulator = simulator;
		this.mapSensor = mapSensor;
		this.routeNavigator = route;
		this.generateCar= generateCar; 
		this.lengthCar = lengthCar;
		generateVehiclesFromDetector();
	}

	/**
	 * Schedule generation of the next GTU.
	 * 
	 * @param simulator
	 * @throws SimRuntimeException
	 * @throws RemoteException
	 */
	public void generateVehiclesFromDetector() throws RemoteException,
			SimRuntimeException {
		for (Entry<String, SensorLaneST> entry : this.mapSensor.entrySet()) {
			entry.getKey();
			SensorLaneST sensor = entry.getValue();
			HashMap<DoubleScalar.Abs<TimeUnit>, Integer> pulses = sensor
					.getStatusByTime();
			for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entryPulse : pulses
					.entrySet()) {
				this.lane = sensor.getLane();
				DoubleScalar.Rel<LengthUnit> initialPosition = sensor.getLongitudinalPosition();
				this.initialSpeed = lane.getSpeedLimit();
				if (entryPulse.getValue() == this.generateCar) {
					DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
					this.simulator.scheduleEventAbs(when, this, this,
							"generateCar", new Object[] {initialPosition});
				}
			}

		}
	}


	/**
	 * Generate one car and re-schedule this method.
	 */
	protected final void generateCar(DoubleScalar.Rel<LengthUnit> initialPosition) {
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
					new DoubleScalar.Rel<LengthUnit>(this.lengthCar, LengthUnit.METER),
					new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR),
//					this.routeGenerator.generateRouteNavigator(),
					this.routeNavigator,
					this.simulator, DefaultCarAnimation.class, this.gtuColorer);
		} catch (RemoteException | SimRuntimeException | NamingException
				| NetworkException | GTUException exception) {
			exception.printStackTrace();
		}
	}
}
