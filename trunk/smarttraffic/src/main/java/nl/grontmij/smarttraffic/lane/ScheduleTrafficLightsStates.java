package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleTrafficLightsStates<ID> {

	/** The simulator that controls everything. */
	final OTSDEVSSimulatorInterface simulator;

	HashMap<String, SensorLaneST> mapSensor;

	public ScheduleTrafficLightsStates(OTSDEVSSimulatorInterface simulator,
			HashMap<String, SensorLaneST> mapSensor) throws RemoteException,
			SimRuntimeException, NetworkException, GTUException,
			NamingException {
		this.simulator = simulator;
		this.mapSensor = mapSensor;
		scheduleTrafficLightsStatesFromDetector();
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
	public void scheduleTrafficLightsStatesFromDetector()
			throws RemoteException, SimRuntimeException, GTUException,
			NetworkException, NamingException 
	{
		for (Entry<String, SensorLaneST> entry : this.mapSensor.entrySet()) {
			entry.getKey();
			SensorLaneST sensor = entry.getValue();
			HashMap<DoubleScalar.Abs<TimeUnit>, Integer> pulses = sensor
					.getStatusByTime();
			for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entryPulse : pulses
					.entrySet()) {
				TrafficLightOnOff trafficLight = new TrafficLightOnOff(
						sensor.getLane(), sensor.getLongitudinalPosition(),
						this.simulator, null);
				DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
				if (entryPulse.getValue() == 1) {
					trafficLight.setBlocked(true);
				}
				else if (entryPulse.getValue() == 0) {
					trafficLight.setBlocked(false);
				}
				trafficLight.SetColor(when);
			}

		}
	}

}
