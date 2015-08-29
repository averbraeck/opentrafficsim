package nl.grontmij.smarttraffic.lane;

import java.io.BufferedWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.AbstractTrafficLight;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.LaneBasedRouteNavigator;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleCheckPulses<ID> {
	/** The type of GTUs generated. */
	final GTUType gtuType;

	/** The GTU following model used by all generated GTUs. */
	final GTUFollowingModel gtuFollowingModel;

	/** The lane change model used by all generated GTUs. */
	final LaneChangeModel laneChangeModel;

	final double lengthCar;

	/** The GTU colorer that will be linked to each generated GTU. */
	final GTUColorer gtuColorer;

	/** The simulator that controls everything. */
	private final OTSDEVSSimulatorInterface simulator;

	/** the routes. */
	private final List<CompleteRoute> routes;

	HashMap<String, CheckSensor> mapSensor;

	public ScheduleCheckPulses(GTUType gtuType,
			OTSDEVSSimulatorInterface simulator,
			HashMap<String, CheckSensor> mapSensor, double backRange,
			double frontRange, List<CompleteRoute> routes,
			BufferedWriter outputFileLogVehicleSimulation)
			throws RemoteException, SimRuntimeException, NetworkException,
			GTUException, NamingException {
		this.simulator = simulator;
		this.mapSensor = mapSensor;
		this.routes = routes;
		this.gtuType = gtuType;
		this.gtuFollowingModel = new GTMIDMPlusSI();
		this.laneChangeModel = new GTMLaneChangeModel();
		this.gtuColorer = new IDGTUColorer();
		this.lengthCar = 4.5;
		scheduleCheckPulses(backRange, frontRange,
				outputFileLogVehicleSimulation);
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
	public void scheduleCheckPulses(final double backRange,
			final double frontRange,
			BufferedWriter outputFileLogVehicleSimulation)
			throws RemoteException, SimRuntimeException, GTUException,
			NetworkException, NamingException {
		for (Entry<String, CheckSensor> entry : this.mapSensor.entrySet()) {
			entry.getKey();
			CheckSensor sensor = entry.getValue();
			HashMap<DoubleScalar.Abs<TimeUnit>, Integer> pulses = sensor
					.getStatusByTime();
			for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entryPulse : pulses
					.entrySet()) {
				if (entryPulse.getValue() == 0) {
					DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
					this.simulator.scheduleEventAbs(when, this, this,
							"findNearestVehicles", new Object[] { sensor,
									backRange, frontRange,
									outputFileLogVehicleSimulation });
				}
			}

		}
		this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0,
				TimeUnit.SECOND), this, this, "checkTriggeredVehiclesList",
				new Object[] { GTM.listGTUsInNetwork, backRange,
						outputFileLogVehicleSimulation });
	}

	private final void checkTriggeredVehiclesList(
			final HashMap<LaneBasedIndividualCar, LinkedList<CheckSensor>> listGTUsInNetwork,
			final double backRange,
			BufferedWriter outputFileLogVehicleSimulation)
			throws RemoteException, NetworkException, SimRuntimeException {
		// is the sensor on an exit near a traffic light?
		ArrayList<LaneBasedIndividualCar> listGtuToDestroy = new ArrayList<LaneBasedIndividualCar>(); 
		for (Entry<LaneBasedIndividualCar, LinkedList<CheckSensor>> entry : listGTUsInNetwork
				.entrySet()) {
			// destroy the gtu if it is not detected within a certain range
			if (!entry.getValue().isEmpty()) {
				if (entry.getValue().get(0).getLocation()
						.distance(entry.getKey().getLocation()) > backRange) {
					listGtuToDestroy.add(entry.getKey());
				}
			}
			
		}
		for (LaneBasedIndividualCar gtu : listGtuToDestroy) {
			listGTUsInNetwork.remove(gtu);
			gtu.destroy();
		}
		this.simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.1,
				TimeUnit.SECOND), this, this, "checkTriggeredVehiclesList",
				new Object[] { GTM.listGTUsInNetwork, backRange,
						outputFileLogVehicleSimulation });
	}

	private final void findNearestVehicles(final CheckSensor sensor,
			final double backRange, final double frontRange,
			BufferedWriter outputFileLogVehicleSimulation)
			throws RemoteException, NetworkException {
		// is the sensor on an exit near a traffic light?
		Lane sensorLane = sensor.getLane();
		double sensorPosSI = sensor.getLongitudinalPositionSI();
		if (sensor.isExitLaneSensor(this.routes)) {
			// find near vehicle
			// first look in front (vehicles already passed??)
			LaneBasedGTU gtu = nearGTUfront2(sensor, frontRange);
			if (gtu == null) {
				gtu = nearGTUback2(sensor, backRange);
			}
			// move vehicle to assumed right position
			if (gtu != null) {
				// move GTU
				for (Object o : gtu.positions(gtu.getReference()).keySet()) {
					((LaneBasedIndividualCar) gtu).destroy();
					GTM.listGTUsInNetwork.remove(gtu);
				}
				//
				if (Settings.getBoolean(simulator, "ANIMATERAMPVEHICLES"))
					generateCar(sensor.getLane(),
					// put the car one meter in front of the sensor (so
					// the car is not triggered again!)
							new DoubleScalar.Rel<LengthUnit>(sensor
									.getLongitudinalPositionSI() + 1,
									LengthUnit.METER), Integer.parseInt(gtu
									.getId().toString()));

				try {
					outputFileLogVehicleSimulation.write("t="
							+ simulator.getSimulatorTime().get().getSI()
							+ " - gtu " + gtu + " moved onto exit lane "
							+ sensor.getLane() + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				// if no vehicle is triggered, create a new one
				try {
					generateCar(sensor.getLane(),
					// put the car one meter in front of the sensor (so
					// the car is not triggered again!)
							new DoubleScalar.Rel<LengthUnit>(sensor
									.getLongitudinalPositionSI() + 1,
									LengthUnit.METER), (++ScheduleGenerateCars.carsCreated));
					outputFileLogVehicleSimulation.write("t="
							+ simulator.getSimulatorTime().get().getSI()
							+ " - no near GTU found for exit lane "
							+ sensor.getLane() + "\n");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private final LaneBasedGTU nearGTUfront2(CheckSensor sensor, double range)
			throws RemoteException {
		boolean backwards = false;
		return nearGTU2(sensor, sensor.getLane().getParentLink(), range,
				backwards);
	}

	private final LaneBasedGTU nearGTUback2(CheckSensor sensor, double range)
			throws RemoteException {
		boolean backwards = true;
		return nearGTU2(sensor, sensor.getLane().getParentLink(), range,
				backwards);
	}

	private final LaneBasedGTU nearGTU2(CheckSensor sensor,
			CrossSectionLink link, double range, boolean backwards)
			throws RemoteException {
		if (link.getStartNode().getLocation().distance(sensor.getLocation()) > range
				&& link.getEndNode().getLocation()
						.distance(sensor.getLocation()) > range) {
			// too far away
			return null;
		}

		LaneBasedGTU nearestGTU = null;
		double distanceSI = range;
		for (Object cse : link.getCrossSectionElementList()) {
			if (cse instanceof Lane) {
				Lane lane = (Lane) cse;
				for (Object g : lane.getGtuList()) {
					LaneBasedGTU gtu = (LaneBasedGTU) g;
					if (gtu instanceof LaneBasedIndividualCar) {
						double d = gtu.getLocation().distance(
								sensor.getLocation());
						if (d < distanceSI) {
							distanceSI = d;
							if (GTM.listGTUsInNetwork
									.get((LaneBasedIndividualCar) gtu) != null
									&& !backwards) {
								nearestGTU = gtu;
								GTM.listGTUsInNetwork.remove(gtu);
							}
						}
					}
				}
			}
		}

		if (nearestGTU == null) {
			// recurse
			if (link.getStartNode().getLinksIn().size() > 0) {
				if (backwards) {
					for (Object o : link.getStartNode().getLinksIn()) {
						if (nearestGTU == null) {
							CrossSectionLink prevLink = (CrossSectionLink) o;
							nearestGTU = nearGTU2(sensor, prevLink, range,
									backwards);
						}
					}
				} else {
					if (link.getEndNode().getLinksOut().size() > 0) {
						for (Object o : link.getEndNode().getLinksOut()) {
							if (nearestGTU == null) {
								CrossSectionLink nextLink = (CrossSectionLink) o;
								nearestGTU = nearGTU2(sensor, nextLink, range,
										!backwards);
							}
						}
					}
				}
			}
		}
		return nearestGTU;
	}

	/**
	 * Generate one car and re-schedule this method if there is no space.
	 * 
	 * @throws NetworkException
	 */
	protected final void generateCar(Lane lane,
			DoubleScalar.Rel<LengthUnit> initialPosition, final int gtuNumber)
			throws NetworkException {
		// is there enough space?
		Lane nextLane = lane.nextLanes(this.gtuType).iterator().next();
		double genSpeedSI = Math.min(lane.getSpeedLimit(GTM.GTUTYPE).getSI(), nextLane
				.getSpeedLimit(GTM.GTUTYPE).getSI());
		if (!ScheduleGenerateCars.enoughSpace(lane, initialPosition.getSI(),
				this.lengthCar, genSpeedSI, this.gtuType)) {
			try {
				this.simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(
						0.25, TimeUnit.SECOND), this, this, "generateCar",
						new Object[] { lane, initialPosition, gtuNumber });
				return;
			} catch (RemoteException | SimRuntimeException exception) {
				exception.printStackTrace();
			}
		}

		Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
		initialPositions.put(lane, initialPosition);
		DoubleScalar.Abs<SpeedUnit> initialSpeed = lane.getSpeedLimit(GTM.GTUTYPE);
		DoubleScalar.Abs<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(
				Settings.getDouble(simulator, "MAXSPEED"),
				SpeedUnit.KM_PER_HOUR);
		if (initialPosition.getSI() + this.lengthCar > lane.getLength().getSI()) {
			// also register on next lane.
			if (lane.nextLanes(this.gtuType).size() == 0
					|| lane.nextLanes(this.gtuType).size() > 1) {
				System.err
						.println("lane.nextLanes().size() == 0 || lane.nextLanes().size() > 1");
				System.exit(-1);
			}
			DoubleScalar.Rel<LengthUnit> nextPos = new DoubleScalar.Rel<LengthUnit>(
					initialPosition.getSI() - lane.getLength().getSI(),
					LengthUnit.METER);
			initialPositions.put(nextLane, nextPos);
		}
		CompleteRoute route = new CompleteRoute("");
		route.addNode(nextLane.getParentLink().getStartNode());
		Node node = nextLane.getParentLink().getEndNode();
		route.addNode(node);
		while (node.getLinksOut().size() > 0) {
			CrossSectionLink link = (CrossSectionLink) node.getLinksOut()
					.iterator().next();
			node = link.getEndNode();
			route.addNode(node);
		}
		LaneBasedRouteNavigator routeNavigator = new CompleteLaneBasedRouteNavigator(
				route);
		try {
			DoubleScalar.Rel<LengthUnit> vehicleLength = new DoubleScalar.Rel<LengthUnit>(
					this.lengthCar, LengthUnit.METER);
			Class<? extends Renderable2D> animationClass = Settings.getBoolean(
					simulator, "ANIMATECARS") ? DefaultCarAnimation.class
					: null;
			LaneBasedIndividualCar gtu = new LaneBasedIndividualCar(""
					+ gtuNumber, this.gtuType, this.gtuFollowingModel,
					this.laneChangeModel, initialPositions, initialSpeed,
					vehicleLength, new DoubleScalar.Rel<LengthUnit>(2.0,
							LengthUnit.METER), maxSpeed, routeNavigator,
					this.simulator, animationClass, this.gtuColorer);
			// add this car to the list of gtu's in the network
			LinkedList<CheckSensor> linkedList = new LinkedList<CheckSensor>();
			GTM.listGTUsInNetwork.put((LaneBasedIndividualCar) gtu, linkedList);

		} catch (RemoteException | SimRuntimeException | NamingException
				| NetworkException | GTUException exception) {
			exception.printStackTrace();
		}
	}
}
