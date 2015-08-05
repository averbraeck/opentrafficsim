package nl.grontmij.smarttraffic.lane;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.generator.ListGTUGenerator;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.network.route.LaneBasedRouteGenerator;
import org.opentrafficsim.core.network.route.LaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.AbstractWrappableSimulation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.xml.sax.SAXException;

/**
 * <p>
 * 
 * @version Oct 17, 2014 <br>
 *          ======= $LastChangedDate: 2015-07-15 16:11:15 +0200 (Wed, 15 Jul
 *          2015) $, @version $Revision$, by $Author$,
 *          initial version ct 17, 2014 <br>
 *          =======
 * @version $Revision$, $LastChangedDate: 2015-07-15 16:11:15 +0200 (Wed,
 *          15 Jul 2015) $, by $Author$, initial version Oct 17,
 *          2014 <br>
 *          >>>>>>> .r1123 >>>>>>> .r1113
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander
 *         Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */

public class GTM extends AbstractWrappableSimulation {
	/**
	 * Main program.
	 * 
	 * @param args
	 *            String[]; the command line arguments (not used)
	 * @throws SimRuntimeException
	 *             should never happen
	 * @throws RemoteException
	 *             on communications failure
	 */

	public static Instant startTimeSimulation;
	public static HashMap<Lane<?, ?>, StopLineLane> mapLaneToStopLineLane = new HashMap<Lane<?, ?>, StopLineLane>();

	public static void main(final String[] args) throws RemoteException,
			SimRuntimeException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					GTM xmlModel = new GTM();
					xmlModel.buildSimulator(
							new ArrayList<AbstractProperty<?>>(), null, true);
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
		return new TestXMLModel(colorer);
	}

	/** {@inheritDoc} */
	@Override
	protected final Double makeAnimationRectangle() {
		return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
	}

	/**
	 * Model to test the XML parser.
	 * <p>
	 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600
	 * AA, Delft, the Netherlands. All rights reserved. <br>
	 * BSD-style license. See <a
	 * href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
	 * <p>
	 * 
	 * @version $Revision$, $LastChangedDate: 2015-07-15 16:11:15 +0200
	 *          (Wed, 15 Jul 2015) $, by $Author$, initial version
	 *          Jun 27, 2015 <br>
	 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander
	 *         Verbraeck</a>
	 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
	 */

	class TestXMLModel implements OTSModelInterface {
		/** */
		private static final long serialVersionUID = 20141121L;

		/** the simulator. */
		private OTSDEVSSimulatorInterface simulator;
		/** The blocking car. */
		private HashMap<Lane<?, ?>, LaneBasedIndividualCar<Integer>> blockMap = new HashMap<Lane<?, ?>, LaneBasedIndividualCar<Integer>>();

		/** Type of all GTUs. */
		private GTUType<String> gtuType = GTUType.makeGTUType("CAR");

		/** The lane change model. */
		private AbstractLaneChangeModel laneChangeModel = new Egoistic();

		/** the car following model, e.g. IDM Plus for cars. */
		private GTUFollowingModel gtuFollowingModel = new IDMPlus();

		private LaneBasedRouteGenerator routeGenerator;

		DoubleScalar.Abs<SpeedUnit> initialSpeed;

		/**
		 * @param gtuColorer
		 *            the GTUColorer to use.
		 */
		public TestXMLModel(final GTUColorer gtuColorer) {
			super();
			this.gtuColorer = gtuColorer;
		}

		/** the gtuColorer. */
		private final GTUColorer gtuColorer;

		/** {@inheritDoc} */
		@Override
		public final void constructModel(
				final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
				throws SimRuntimeException, RemoteException {
			this.simulator = (OTSDEVSSimulatorInterface) pSimulator;

			// base directory (relative to user dir)
			String dirBase = System.getProperty("user.dir")
					+ "/src/main/resources/";

			URL url = URLResource.getResource(dirBase + "model/PNH_NOORD.xml");
			XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
			OTSNetwork<?, ?, ?> network = null;
			try {
				network = nlp.build(url);
			} catch (NetworkException | ParserConfigurationException
					| SAXException | IOException | NamingException
					| GTUException | OTSGeometryException exception1) {
				exception1.printStackTrace();
			}
			@SuppressWarnings("unchecked")
			// define the type of cars
			GTUType<String> gtuType = GTUType.makeGTUType("CAR");

			// read and define detectors from the network
			HashMap<String, SensorLaneST> mapSensor = new HashMap<String, SensorLaneST>();
			HashMap<String, SensorLaneST> mapSensorGenerateCars = new HashMap<String, SensorLaneST>();
			HashMap<String, SensorLaneST> mapSensorKillCars = new HashMap<String, SensorLaneST>();
			HashMap<String, SensorLaneST> mapSensorCheckCars = new HashMap<String, SensorLaneST>();
			try {
				ReadNetworkData.readDetectors(network, mapSensor,
						mapSensorGenerateCars, mapSensorKillCars,
						mapSensorCheckCars);
			} catch (NetworkException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			// read the configuration files for VLOG (detector/signalgroup: both
			// index and name
			String dirConfigVri = "configVRI/";
			String dirLoggings = "VRI-loggings/";
			String wegNummer = "201";
			// String[] vriNummer = { "225", "231", "234", "239", "245", "249",
			// "291", "297", "302", "308", "311", "314" };
			String[] vriNummer = { "225" };

			HashMap<String, ConfigVri> configVriList = null;
			try {
				configVriList = ReadVLog.readVlogConfigFiles(dirConfigVri,
						dirBase, wegNummer, vriNummer);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// read the historical (at a later stage streaming) VLOG data
			// start met inlezen files vanaf tijdstip ....
			int year = 2015;
			int month = 6;
			int day = 1;
			long hour = 0;
			int minute = 0;
			int second = 0;
			int tenth = 0;
			Instant timeVLog = Instant.parse(String.format(
					"%04d-%02d-%02dT%02d:%02d:%02d.%02dZ", year, month, day,
					hour, minute, second, tenth));
			startTimeSimulation = timeVLog;
			ZoneOffset offset = ZoneOffset.of("-00:00");
			LocalDateTime ldt = LocalDateTime.ofInstant(timeVLog, offset);
			ldt = LocalDateTime.ofInstant(timeVLog, offset);
			// read the vlog data with both detector and signalgroup data
			try {
				ReadVLog.readVlogFiles(mapSensor, configVriList, timeVLog,
						dirBase + dirLoggings, wegNummer, vriNummer);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// connect the detector pulses to the simulator and generate Cars

			try {
				GenerateCars generateCars = new GenerateCars(gtuType,
						gtuFollowingModel, laneChangeModel, routeGenerator,
						gtuColorer, simulator, mapSensorGenerateCars);
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			try {
				ScheduleTrafficLightsStates scheduleTrafficLightStates= new ScheduleTrafficLightsStates(simulator, mapSensorGenerateCars);
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GTUException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

			// Module that provides actions if a pulse from a detector is
			// activated
			// - Generate a car (ENTRANCE)
			// - Kill a car (EXIT)
			// - Compare this pulse to vehicles in the simulation
			// (INTERMEDIATE)
			// - if no car is matched: Generate a car
			// - if matched: reposition that car, and perhaps other cars

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

		private void readTrafficLightState(OTSDEVSSimulatorInterface simulator,
				GTUColorer gtuColorer, String fileName, Lane lane, Integer id) {
			/** Reader for the event list. */
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(new File(fileName)));
				scheduleTrafficLigthState(reader, lane, id);
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			}

		}

		/**
		 * Schedule generation of the next GTU.
		 */
		private void scheduleTrafficLigthState(BufferedReader reader,
				Lane lane, Integer id) {
			try {
				String line = reader.readLine();
				while (!line.contentEquals("")) {
					String[] words = line.split(",");
					double when = 0;
					for (int i = 0; i < 4; i++) {
						words[i] = words[i].replaceAll("\\s+", "");
						if (i == 2) {
							when = java.lang.Double.parseDouble(words[i]);
						}
						if (i == 3) {
							if (words[i].contentEquals("RED")) {
								Object[] objects = new Object[2];
								objects[0] = lane;
								objects[1] = id;

								this.simulator.scheduleEventAbs(
										new DoubleScalar.Abs<TimeUnit>(when,
												TimeUnit.SECOND), this, this,
										"createBlocks", objects);

							} else if (words[i].contentEquals("GREEN")) {
								Object[] objects = new Object[1];
								objects[0] = lane;
								this.simulator.scheduleEventAbs(
										new DoubleScalar.Abs<TimeUnit>(when,
												TimeUnit.SECOND), this, this,
										"removeBlocks", objects);

							}

						}
					}
					line = reader.readLine();
				}
			} catch (NumberFormatException exception) {
				exception.printStackTrace();
				scheduleTrafficLigthState(reader, lane, id);
			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (SimRuntimeException exception) {
				exception.printStackTrace();
			}
		}

		/** {@inheritDoc} */
		@Override
		public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
				throws RemoteException {
			return this.simulator;
		}

		/**
		 * Set up the block.
		 * 
		 * @throws RemoteException
		 *             on communications failure
		 */
		protected final void createBlocks(Lane lane, Integer id)
				throws RemoteException {
			Map<Lane<?, ?>, Rel<LengthUnit>> initialPositions = new LinkedHashMap<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>>();
			initialPositions.put(lane, lane.position(0.95));
			try {
				this.blockMap.put(lane,
						new LaneBasedIndividualCar<Integer>(id, this.gtuType,
								new IDMPlus(), this.laneChangeModel,
								initialPositions,
								new DoubleScalar.Abs<SpeedUnit>(0,
										SpeedUnit.KM_PER_HOUR),
								new DoubleScalar.Rel<LengthUnit>(1.0,
										LengthUnit.METER),
								new DoubleScalar.Rel<LengthUnit>(1.8,
										LengthUnit.METER),
								new DoubleScalar.Abs<SpeedUnit>(0,
										SpeedUnit.KM_PER_HOUR),
								new LaneBasedRouteNavigator(null),
								this.simulator, DefaultCarAnimation.class,
								this.gtuColorer));

			} catch (RemoteException | SimRuntimeException | NamingException
					| NetworkException | GTUException exception) {
				exception.printStackTrace();
			}
		}

		/**
		 * Remove the block.
		 */
		protected final void removeBlocks(Lane lane) {
			this.blockMap.get(lane).destroy();
			this.blockMap.remove(lane);
		}

	}

}
