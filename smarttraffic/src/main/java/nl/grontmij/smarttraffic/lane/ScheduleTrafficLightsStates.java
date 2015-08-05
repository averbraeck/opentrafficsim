package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map.Entry;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleTrafficLightsStates<ID> {

	/** The simulator that controls everything. */
	final OTSDEVSSimulatorInterface simulator;

	HashMap<String, SensorLaneST> mapSensor;

	public ScheduleTrafficLightsStates(OTSDEVSSimulatorInterface simulator,
			HashMap<String, SensorLaneST> mapSensor) throws RemoteException,
			SimRuntimeException, NetworkException {
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
	 */
	public void scheduleTrafficLightsStatesFromDetector()
			throws RemoteException, SimRuntimeException {
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
							"generateBlock", null);
				}
			}

		}
	}
	
	
	

}
