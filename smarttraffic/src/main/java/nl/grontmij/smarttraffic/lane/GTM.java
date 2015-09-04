package nl.grontmij.smarttraffic.lane;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.xml.sax.SAXException;

/**
 * <p>
 * 
 * @version Oct 17, 2014 <br>
 * @version $Revision$, $LastChangedDate: 2015-09-01 14:45:39 +0200 (Tue,
 *          01 Sep 2015) $, by $Author$, initial version Oct 17,
 *          2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander
 *         Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */

public class GTM extends AbstractWrappableSimulationST {
	/**
	 * the start time of the simulation to be able to display and report correct
	 * times.
	 */
	public static Instant startTimeSimulation;

	/** the GTUtype to use in the model (car). */
	public static GTUType GTUTYPE;

	/**
	 * a map from the signal group name, e.g., 225_08 to the traffic lights,
	 * e.g., [225_08.1, 225_08.2, 225_08.3].
	 */
	public static Map<String, List<TrafficLight>> signalGroupToTrafficLights = new HashMap<>();

	// output files with results
	// file with the measured number of cars in the network
	public static BufferedWriter outputFileMeasures = null;

	// reflecting the VRI input configurations files
	public static BufferedWriter outputFileLogConfigVRI = null;

	// reflecting the Sensor input configurations files
	public static BufferedWriter outputFileLogReadSensor = null;

	// checking the validity of Sensor status and change
	public static BufferedWriter outputFileVlogCheckSensor = null;

	// checking the validity of Sensor status and change
	public static BufferedWriter outputFileVlogCheckTrafficLight = null;

	// Analysis behaviour
	// the observation of cars during the simulation: which are vehicles are
	// triggered and/or rescheduled at the sensors during their travel?
	public static BufferedWriter outputFileLogVehicleSimulation = null;

	// numbers for evaluation
	// The number of vehicles generated from the "generateSensors"
	public static BufferedWriter outputFileVehiclesGenerated = null;

	// The number of vehicles generated from the "checkSensors"
	public static BufferedWriter outputFileVehiclesTriggered = null;

	public static double startTimeSinceZero = 719529;

	protected static String CURRENTPROPERTIESFILENAME = "";

	public static HashMap<LaneBasedIndividualCar, LinkedList<CheckSensor>> listGTUsInNetwork = new HashMap<LaneBasedIndividualCar, LinkedList<CheckSensor>>();

	public static HashMap<LaneBasedIndividualCar, CheckSensor> gtuLastMovedAtSensor = new HashMap<LaneBasedIndividualCar, CheckSensor>();

	/**
	 * Main program. GTMModel has the model details.
	 * 
	 * @param args
	 *            String[]; the command line arguments (not used)
	 * @throws SimRuntimeException
	 *             should never happen
	 * @throws RemoteException
	 *             on communications failure
	 */
	public static void main(final String[] args) throws RemoteException,
			SimRuntimeException {
		if (args.length != 1) {
			System.err.println("Give location of properties file as argument!");
			System.exit(-1);
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					GTM xmlModel = new GTM();
					GTM.CURRENTPROPERTIESFILENAME = args[0];
					SimpleAnimator animator = xmlModel.buildSimulator(
							new ArrayList<AbstractProperty<?>>(), null, true);
					animator.getEventList().removeLast();
					animator.scheduleEventAbs(
							new DoubleScalar.Abs<TimeUnit>(Settings.getInt(
									animator, "RUNDAYS"), TimeUnit.DAY),
							(short) (SimEventInterface.MIN_PRIORITY - 1),
							animator, animator, "stop", null);
				} catch (RemoteException | SimRuntimeException
						| NamingException exception) {
					exception.printStackTrace();
				}
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public final String shortName() {
		return "TestXMLModel";
	}

	/** {@inheritDoc} */
	@Override
	public final String description() {
		return "TestXMLModel";
	}

	/** {@inheritDoc} */
	@Override
	public final void stopTimersThreads() {
		super.stopTimersThreads();
	}

	/** {@inheritDoc} */
	@Override
	protected final JPanel makeCharts() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	protected final OTSModelInterface makeModel(final GTUColorer colorer) {
		return new GTMModel();
	}

	/** {@inheritDoc} */
	@Override
	protected final Rectangle2D.Double makeAnimationRectangle() {
		return new Rectangle2D.Double(0, 3750, 380, 220);
	}

	/**
	 * Model for GTM.
	 * <p>
	 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600
	 * AA, Delft, the Netherlands. All rights reserved. <br>
	 * BSD-style license. See <a
	 * href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
	 * <p>
	 * 
	 * @version $Revision$, $LastChangedDate: 2015-09-01 14:45:39 +0200
	 *          (Tue, 01 Sep 2015) $, by $Author$, initial version
	 *          Jun 27, 2015 <br>
	 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander
	 *         Verbraeck</a>
	 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
	 */

	class GTMModel implements OTSModelInterface {
		/** */
		private static final long serialVersionUID = 20150801L;

		/** the simulator. */
		private OTSDEVSSimulatorInterface simulator;

		/** {@inheritDoc} */
		@Override
		public final void constructModel(
				final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
				throws SimRuntimeException, RemoteException {
			this.simulator = (OTSDEVSSimulatorInterface) pSimulator;

			new Settings(this.simulator, GTM.CURRENTPROPERTIESFILENAME);
			// Read properties
			// Base directory (relative to user dir)
			String dirBase = Settings.getString(this.simulator, "BASEDIR");
			// + "/" + Settings.getString(this.simulator, "PROJECTDIR");
			String networkFileName = Settings.getString(this.simulator,
					"NETWORK");
			String vLogFileName = Settings.getString(this.simulator, "VLOG");
			// read the configuration files for VLOG (detector/signalgroup: both
			// index and name
			String dirConfigVri = Settings.getString(this.simulator,
					"CONFIGDIR");// vri configuratie
			String dirLoggings = Settings.getString(this.simulator,
					"LOGGINGDIR");// VRI-loggings;
			// output;
			// het nummer van de N201 wordt gebruikt in de bestanden
			String wegNummer = Settings.getString(this.simulator, "WEGNUMMER");// map
																				// output;
			// Geef de numers van de VRI's
			// Data van "311" ontbreekt in de meetperiode. Laat weg.
			String[] vriNummer = Settings.getStringArray(this.simulator,
					"VRINUMMERS");//
			Integer startAtHour = Settings.getInt(simulator, "STARTATHOUR");
			Integer stopAtHour = Settings.getInt(simulator, "STOPATHOUR");

			// create outputMap
			String outputDir = dirBase + "/output";

			FileUtilities.checkAndCreateMap(outputDir);

			String dirExperiment = outputDir + "/"
					+ Settings.getString(this.simulator, "EXPERIMENTDIR");

			outputFileVehiclesTriggered = Output.initiateOutputFile(
					dirExperiment, this.simulator,
					"reportVehiclesPassingCheckSensors.xls", "Time\tNrCars\n");

			outputFileLogConfigVRI = Output
					.initiateOutputFile(dirExperiment, this.simulator,
							"ConfigVri.log",
							"Read config files of traffic light regulated junctions: \n");

			BufferedWriter outputFileReportNumbers = Output.initiateOutputFile(
					dirExperiment, this.simulator, "reportNumbers.xls",
					"Time\tNrCars\n");

			// Lees hier de file met het netwerk
			URL url = URLResource.getResource(dirBase + "/input/model/"
					+ networkFileName);

			// Bouw het netwerk
			XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
			OTSNetwork network = null;
			try {
				network = nlp.build(url);
				makeSignalGroupTrafficLightMap(network);
			} catch (NetworkException | ParserConfigurationException
					| SAXException | IOException | NamingException
					| GTUException | OTSGeometryException exception1) {
				exception1.printStackTrace();
			}

			// in de configVriList worden de vri configuraties opgeslagen. De
			// ConfigVri bevat de detectoren (index, naam) en de
			// signaalgroepen (index, naam)
			HashMap<String, ConfigVri> configVriList = null;
			configVriList = ConfigFile.readVlogConfigFiles(dirBase + "/"
					+ "input" + "/" + dirConfigVri + "/", wegNummer, vriNummer,
					outputFileLogConfigVRI);

			// read and define detectors from the network. in mapSensors staan
			// alle detectoren (met de naam als zoeksleutel
			HashMap<String, AbstractSensor> mapSensor = new HashMap<String, AbstractSensor>();
			// Vervolgens worden de verschillende typen ook nog in aparte
			// HashMaps opgeslagen
			HashMap<String, GenerateSensor> mapSensorGenerateCars = new HashMap<String, GenerateSensor>();
			HashMap<String, KillSensor> mapSensorKillCars = new HashMap<String, KillSensor>();
			HashMap<String, CheckSensor> mapSensorCheckCars = new HashMap<String, CheckSensor>();
			// alle detectoren uit het netwerk worden verzameld
			ReadNetworkData.readDetectors(this.simulator, network,
					configVriList, mapSensor, mapSensorGenerateCars,
					mapSensorKillCars, mapSensorCheckCars);

			// read the historical (at a later stage streaming) VLOG data
			// start met inlezen files vanaf tijdstip ....
			// nu wordt er nog data van alleen 1 juni ingelezen
			int year = 2015;
			int month = 6;
			int day = 1;
			long hour = startAtHour;
			int minute = 0;
			int second = 0;
			int tenth = 0;
			Instant timeVLog = Instant.parse(String.format(
					"%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day,
					hour, minute, second, tenth));
			startTimeSinceZero += timeVLog.getEpochSecond() / 86400;
			startTimeSimulation = Instant.parse(String.format(
					"%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day, 0,
					0, 0, 0));
			// start the simulation at 06:00, but make times relative to 00:00
			// to display the right time.
			/*
			 * read the vlog data with both detector and signalgroup. Data van
			 * alle detectoren worden nu de pulsen toegevoegd (tijdstip en
			 * waarde detectie/signaal). Deze worden opgeslagen in de mapSensor,
			 * maar tegelijkertijd ook in de mappen mapSensorGenerateCars,
			 * mapSensorKillCars en mapSensorCheckCars (omdat daar een
			 * verwijzing naar dezelfde objecten is).
			 */
			outputFileLogReadSensor = Output
					.initiateOutputFile(dirExperiment, this.simulator,
							"ReadAndCheckSensor.log",
							"Read config files of traffic light regulated junctions: \n");
			outputFileVlogCheckSensor = Output
					.initiateOutputFile(dirExperiment, this.simulator,
							"CheckVlogSensor.log",
							"Check validity Vlog data sensor: status versus change: \n");
			outputFileVlogCheckTrafficLight = Output
					.initiateOutputFile(dirExperiment, this.simulator,
							"CheckVlogTrafficLight.log",
							"Check validity Vlog data traffic lights: status versus change: \n");
			ReadVLog.readVlogZipFiles(mapSensor, configVriList, timeVLog,
					dirBase + "/" + "input" + "/" + dirLoggings + "/",
					vLogFileName, wegNummer, vriNummer, this.simulator,
					startAtHour, stopAtHour, outputFileLogReadSensor,
					outputFileVlogCheckSensor, outputFileVlogCheckTrafficLight);
			try {
				outputFileVlogCheckSensor.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				outputFileVlogCheckTrafficLight.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			// connect the detector pulses to the simulator and generate Cars
			// Module that provides actions if a pulse from a detector is
			// activated: creeren van een voertuig als een detector "af" gaat
			// (waarde wordt nul)
			GTUTYPE = GTUType.makeGTUType("CAR");
			int generateCar = 0;
			Map<String, CompleteRoute> routes = new HashMap<>();
			for (String rName : network.getRouteMap().keySet()) {
				try {
					routes.put(rName, new CompleteRoute(rName, network
							.getRouteMap().get(rName).getNodes()));
				} catch (NetworkException exception) {
					exception.printStackTrace();
				}
			}

			new ScheduleGenerateCars(GTUTYPE, this.simulator,
					mapSensorGenerateCars, generateCar, routes);

			new ReportNumbers(network, this.simulator, outputFileReportNumbers);

			outputFileMeasures = Output.initiateOutputFile(dirExperiment,
					this.simulator, "measure.xls", "Time\tSensor\tCar\n");
			Path file = Paths.get(GTM.CURRENTPROPERTIESFILENAME).getFileName();
			try {
				Files.copy(Paths.get(dirBase + "/" + "/input/" + file),
						Paths.get(dirExperiment + "/" + file),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (Settings.getBoolean(this.simulator, "MOVEVEHICLES")) {
				// - Compare the (INTERMEDIATE) pulse to vehicles in the
				// simulation
				//
				// - if no car is matched: Generate a car
				// - if matched: reposition that car, and perhaps other cars
				// - de range om te zoeken naar voertuigen:
				// ------de eerste waarde is de afstand in meters stroomOPwaarts
				// van het voertuig
				// ------de tweede waarde is de afstand in meters stroomAFwaarts
				// van het voertuig
				outputFileLogVehicleSimulation = Output.initiateOutputFile(
						dirExperiment, this.simulator,
						"vehicleDetectections.log", "Time\tRemark\n");
				try {
					new ScheduleCheckPulses(GTUTYPE, this.simulator,
							mapSensorCheckCars, Settings.getDouble(
									this.simulator, "SEARCHRANGEBACK"),
							Settings.getDouble(this.simulator,
									"SEARCHRANGEFRONT"), routes,
							outputFileLogVehicleSimulation);
				} catch (NetworkException | GTUException | NamingException e) {
					e.printStackTrace();
				}

			}

			// module that detects a car in the simulation passing a
			// detector and provide actions:
			// - if the car is too far downstream, and not matched by a
			// pulse: we assume it is not matched --> Action: delete the
			// car.
			// -

			// module for logging information
			// car: time and distance traveled since started
			// detector: log all cars that pass (ID and time)
			// road: time and ID for every car entered and exited

			// learning algorithms:
			// if the cars are traveling faster/slower than the pulses:
			// decrease/increase the maximum speed

		}

		/**
		 * Get the traffic lights with their name.
		 * 
		 * @param network
		 *            the parsed network.
		 */
		private void makeSignalGroupTrafficLightMap(final OTSNetwork network) {
			for (Link link : network.getLinkMap().values()) {
				if (link instanceof CrossSectionLink) {
					@SuppressWarnings({ "rawtypes", "unchecked" })
					List<CrossSectionElement> cseList = ((CrossSectionLink) link)
							.getCrossSectionElementList();
					for (CrossSectionElement cse : cseList) {
						if (cse instanceof Lane) {
							Lane lane = (Lane) cse;
							List<LaneBasedGTU> gtus = new ArrayList<>(
									lane.getGtuList());
							for (LaneBasedGTU gtu : gtus) {
								if (gtu instanceof TrafficLight) {
									TrafficLight trafficLight = (TrafficLight) gtu;
									String signalGroupName = trafficLight
											.getId().split("\\.")[0];
									if (!GTM.signalGroupToTrafficLights
											.containsKey(signalGroupName)) {
										GTM.signalGroupToTrafficLights.put(
												signalGroupName,
												new ArrayList<TrafficLight>());
									}
									GTM.signalGroupToTrafficLights.get(
											signalGroupName).add(trafficLight);
									// XXX hack: van verkeerslicht 311 ontbreekt
									// alle data in de meetperiode.
									// Zet de lichten dus op groen...
									if (trafficLight.getId().startsWith("311")) {
										trafficLight.changeColor(1);
									}
								}
							}
						}
					}
				}
			}
			System.out.println(GTM.signalGroupToTrafficLights);
		}

		/** {@inheritDoc} */
		@Override
		public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
				throws RemoteException {
			return this.simulator;
		}

	}

}
