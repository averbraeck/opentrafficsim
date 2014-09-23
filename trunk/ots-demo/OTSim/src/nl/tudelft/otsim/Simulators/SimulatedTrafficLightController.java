package nl.tudelft.otsim.Simulators;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Step;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.WED;


/**
 * Simulate a traffic light controller.
 * 
 * @author Peter Knoppers
 */
public class SimulatedTrafficLightController implements Step, ShutDownAble {
	private final String controllerURL;
	private ArrayList<SimulatedTrafficLight> trafficLights = new ArrayList<SimulatedTrafficLight>();
	private Map<String, SimulatedDetector> detectors = new HashMap<String, SimulatedDetector>();
	private final Scheduler scheduler;
	private PrintWriter serverWriter = null;
	private BufferedReader serverReader = null;
	Socket clientSocket = null;
	private String program = null;
	private Map<String, Boolean> currentDetectorState = new HashMap<String, Boolean>();

	/**
	 * Create a SimulatedTrafficLightController.
	 * 
	 * @param scheduler {@link Scheduler}; the scheduler for the new simulated
	 * traffic light controller
	 * @param controllerURL String; the URL needed to connect to the external
	 * control program
	 */
	public SimulatedTrafficLightController(Scheduler scheduler, String controllerURL) {
		this.scheduler = scheduler;
		this.controllerURL = controllerURL;
		if ((null != controllerURL) && (controllerURL != "")) {
			if (controllerURL.startsWith("fixed:")) {
				program = controllerURL.substring(6);
			} else {
				// Extract port number from controllerURL
				int portNumber = 6666;
				int pos = controllerURL.lastIndexOf(":");
				if (pos > 0)
					try {
						portNumber = Integer.parseInt(controllerURL.substring(pos + 1));
					} catch (NumberFormatException e2) {
						// Ignore;
					}
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket(portNumber);
				} catch (IOException e1) {
					WED.showProblem(WED.ENVIRONMENTERROR, "Cannot open port %d\r\n%s",
							portNumber, WED.exeptionStackTraceToString(e1));
					e1.printStackTrace();
				}
				// fire up an instance of the traffic control program
				try {
					Runtime.getRuntime().exec(controllerURL);
				} catch (IOException e) {
					WED.showProblem(WED.ENVIRONMENTERROR, "Cannot start external traffic control program\r\n\"%s\"\r\n%s", 
							controllerURL, WED.exeptionStackTraceToString(e));
					e.printStackTrace();
				}
				try {
					clientSocket = serverSocket.accept();
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							if (null != clientSocket)
								try {
									clientSocket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							clientSocket = null;
						}
					});
					serverWriter = new PrintWriter(clientSocket.getOutputStream(), true);
					serverReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				} catch (IOException e) {
					WED.showProblem(WED.ENVIRONMENTERROR, "Error in connection setup:\r\n%s", 
							WED.exeptionStackTraceToString(e));
					if (null != clientSocket)
						try {
							clientSocket.close();
						} catch (IOException e1) {
							// ignore exceptions during exception handling
						}
				}
				try {
					serverSocket.close();
				} catch (IOException e) {
					// ignored
				}
			}
		}
		scheduler.enqueueEvent(0, this);
	}
	
	/**
	 * Retrieve the controllerURL of this SimulatedTrafficLightController.
	 * @return String; the controllerURL of this SimulatedTrafficLightController
	 */
	public String getControllerURL_r() {
		return controllerURL;
	}
	
	/**
	 * Associate a {@link SimulatedTrafficLight} with this SimulatedTrafficLightController.
	 * @param stl {@link SimulatedTrafficLight}; the simulated traffic light to
	 * link to this SimulatedTrafficLightController
	 */
	public void addTrafficLight(SimulatedTrafficLight stl) {
		trafficLights.add(stl);
	}
	
	/**
	 * Associate a {@link SimulatedDetector} with this SimulatedTrafficLightController
	 * @param sd {@link SimulatedDetector}; the simulated detector to link to
	 * this SimulatedTrafficLightController
	 */
	public void addDetector(SimulatedDetector sd) {
		detectors.put(sd.name(), sd);
	}
	
	/**
	 * Execute one simulation step of this SimulatedTrafficLightController.
	 * @param now double; the current simulation time.
	 * @return Scheduler.SchedulerState; indication whether simulation can continue
	 */
	@Override
	public Scheduler.SchedulerState step(double now) {
		String newColors = "";
		TreeSet<String> trafficLightNames = new TreeSet<String>();
		for (SimulatedTrafficLight stl : trafficLights)
			trafficLightNames.add(stl.name());
		if (null == clientSocket) {
			if (null == program) { 
				// create a totally dumb fixed time controller
				final double yellowTime = 3;
				final double greenTime = 12;
				final double clearanceTime = 1;
				final double subCycleTime = greenTime + yellowTime + clearanceTime;
				final double cycleTime = subCycleTime * trafficLightNames.size();
				
				program = String.format(Main.locale, "%.1f", cycleTime);
				int phase = 0;
				for (String name : trafficLightNames) {
					program += String.format(Main.locale, "|%s,%.1f,%.1f,%.1f", name, 
							(phase * subCycleTime) % cycleTime,
							(phase * subCycleTime + greenTime) % cycleTime,
							(phase * subCycleTime + greenTime + yellowTime) % cycleTime);
					phase++;
				}
			}
			// Interpret the fixed time controller program
			// fixed time, on-leg-at-a-time controller
			String[] fields = program.split("[|]");
			final double cycleTime = Double.parseDouble(fields[0]);
			double phaseTime = now % cycleTime;
			for (String lightName : trafficLightNames)
			{
				int light;
				for (light = 1; light < fields.length; light++)
					if (fields[light].split(",")[0].equals(lightName))
						break;
				String[] subFields = fields[light].split(",");
				final double startGreen = Double.parseDouble(subFields[1]);
				final double startYellow = Double.parseDouble(subFields[2]);
				final double startRed = Double.parseDouble(subFields[3]);
				
				if ((phaseTime >= startRed) && (phaseTime <= startGreen))
					newColors += "r";
				else if ((phaseTime >= startGreen) && (phaseTime < startYellow))
					newColors += "g";
				else if ((phaseTime >= startYellow) && (phaseTime < startRed))
					newColors += "y";
				else if (startRed > startGreen)
					newColors += "r";
				else if (startGreen > startYellow)
					newColors += "g";
				else if (startYellow > startRed)
					newColors += "y";
				else {
					WED.showProblem(WED.ENVIRONMENTERROR, "Cannot determine color for light %s", subFields[0]);
					return Scheduler.SchedulerState.SimulatorError;
				}
			}
		} else {	// Communicate with external control program
			// send the state of the detectors
			for (SimulatedDetector sd : detectors.values()) {
				boolean newState = sd.isOccupied();
				Boolean lastTransmittedState = currentDetectorState.get(sd.name());
				//System.out.println("detector " + sd.name() + " is " + (newState ? "occupied" : "free"));
				if ((null == lastTransmittedState) || (lastTransmittedState != newState)) {
					serverWriter.println(String.format("detector %s %s", sd.name().substring(3), newState ? "occupied" : "free"));
					currentDetectorState.put(sd.name(), newState);
				}
			}
			if (now != 0.0) {
				// send a step command
				serverWriter.println("step");
			}
			String in = null;
			try {
				in = serverReader.readLine();
			} catch (IOException e) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Error reading from traffic light controller:\r\n%s\r\nTraffic lights will not change anymore", 
						WED.exeptionStackTraceToString(e));
				return killConnection();
			}
			if (null == in) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Received no data from traffic licht controller\r\nTraffic lights will not change anymore");
				return killConnection();
			}
			//System.out.println(in);
			// the letters after the first space in the received line (should) correspond one to one with our traffic lights
			int index = in.indexOf(" ") + 1;
			newColors = in.substring(index);
		}
		int pos = 0;
		for (String trafficLightName : trafficLightNames) {
			final String newColor = newColors.substring(pos, pos + 1);
			for (SimulatedTrafficLight stl : trafficLights)
				if (stl.name().equals(trafficLightName)) {
					if ("r".equals(newColor))
						stl.setColor(Color.RED);
					else if ("g".equals(newColor))
						stl.setColor(Color.GREEN);
					else if ("y".equals(newColor))
						stl.setColor(Color.YELLOW);
					else {
						WED.showProblem(WED.ENVIRONMENTERROR, "Unknown color in \"%s\" at position %d", newColors, pos);
						return killConnection();
					}
				}
			pos++;
		}
		scheduler.enqueueEvent(now + 0.1, this);
		return null;
	}
	
	private Scheduler.SchedulerState killConnection() {
		try {
			if (null != serverReader)
				serverReader.close();
			if (null != serverWriter)
				serverWriter.close();
			if (null != clientSocket)
				clientSocket.close();
		} catch (IOException e) {
			// ignore
		}
		serverReader = null;
		serverWriter = null;
		clientSocket = null;
		return Scheduler.SchedulerState.SimulatorError;
	}

	@Override
	public void ShutDown() {
		killConnection();
	}
	
}