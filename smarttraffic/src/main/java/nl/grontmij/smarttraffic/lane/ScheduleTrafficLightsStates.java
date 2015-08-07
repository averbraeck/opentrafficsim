package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.lane.LaneBlockOnOff;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleTrafficLightsStates<ID> {

	/** The simulator that controls everything. */
	final OTSDEVSSimulatorInterface simulator;

	HashMap<String, SensorLaneST> mapSensor;

	HashMap<String, StopLineLane> mapSignalGroupToStopLineAtJunction;

	TrafficLightOnOff trafficLightOnOff = null;

	public ScheduleTrafficLightsStates(OTSDEVSSimulatorInterface simulator,
			HashMap<String, SensorLaneST> mapSensor,
			HashMap<String, StopLineLane> mapSignalGroupToStopLineAtJunction)
			throws RemoteException, SimRuntimeException, NetworkException,
			GTUException, NamingException {
		this.simulator = simulator;
		this.mapSensor = mapSensor;
		this.mapSignalGroupToStopLineAtJunction = mapSignalGroupToStopLineAtJunction;
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
			NetworkException, NamingException {
		for (Entry<String, StopLineLane> entry : this.mapSignalGroupToStopLineAtJunction
				.entrySet()) {
			entry.getKey();
			StopLineLane stopLine = entry.getValue();
			HashMap<DoubleScalar.Abs<TimeUnit>, Long> pulses = stopLine
					.getMapStopTrafficState();
			for (Entry<DoubleScalar.Abs<TimeUnit>, Long> entryPulse : pulses
					.entrySet()) {
				DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
				if (stopLine.getTrafficLight() != null) {							
					if (entryPulse.getValue() == 0) {
						stopLine.getTrafficLight().setBlocked(true);
						stopLine.getTrafficLight().changeFromColor(when);
					} else if (entryPulse.getValue() > 0) {
						stopLine.getTrafficLight().setBlocked(false);
						stopLine.getTrafficLight().changeFromColor(when);
					}
				}

			}

		}
	}

}
