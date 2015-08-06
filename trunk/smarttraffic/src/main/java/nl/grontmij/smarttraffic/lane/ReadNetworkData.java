package nl.grontmij.smarttraffic.lane;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.animation.DefaultSensorAnimation;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ReadNetworkData {

	private ReadNetworkData() {
		// cannot be instantiated.
	}

	public static void readDetectors(OTSDEVSSimulatorInterface simulator, OTSNetwork<?, ?, ?> network,
			HashMap<String, SensorLaneST> mapSensor,
			HashMap<String, SensorLaneST> mapSensorGenerateCars,
			HashMap<String, SensorLaneST> mapSensorKillCars,
			HashMap<String, SensorLaneST> mapSensorCheckCars)
			throws NetworkException, RemoteException, NamingException {
		// Detectors
		// define the detectors by type (ENTRANCE, INTERMEDIATE, EXIT)
		// Inventorize all detectors from the network, distinguished by type
		Map<?, ?> links = network.getLinkMap();
		Collection<Link<?, ?>> linkValues = (Collection<Link<?, ?>>) links
				.values();
		for (Link<?, ?> link : linkValues) {
			if (link instanceof CrossSectionLink) {
				CrossSectionLink<?, ?> csl = (CrossSectionLink<?, ?>) link;
				for (CrossSectionElement cse : csl.getCrossSectionElementList()) {
					if (cse instanceof Lane && !(cse instanceof NoTrafficLane)) {
						Lane lane = (Lane) cse;
						List<SensorLaneST> sensors = lane.getSensors(
								new DoubleScalar.Rel<LengthUnit>(0,
										LengthUnit.METER), lane.getLength());
						if (!sensors.isEmpty()) {
							for (Sensor sensor : sensors) {
								if (sensor instanceof SensorLaneST) {
									SensorLaneST sensorLaneST = (SensorLaneST) sensor;
									mapSensor.put(sensor.getName(), sensorLaneST);
									if (sensorLaneST.getName().startsWith("G")) {
										mapSensorGenerateCars.put(
												sensor.getName(), sensorLaneST);
								        if (simulator instanceof OTSAnimatorInterface)
								        {
												new DefaultSensorAnimation(sensor, simulator, Color.RED);
								        }

									} else if (sensorLaneST.getName().startsWith("K")) {
										mapSensorKillCars.put(sensor.getName(),
												sensorLaneST);
								        if (simulator instanceof OTSAnimatorInterface)
								        {
												new DefaultSensorAnimation(sensor, simulator, Color.ORANGE);
								        }
									} else if (sensorLaneST.getName().startsWith("C")) {
										mapSensorCheckCars.put(
												sensor.getName(), sensorLaneST);
								        if (simulator instanceof OTSAnimatorInterface)
								        {
												new DefaultSensorAnimation(sensor, simulator, Color.BLUE);
								        }										
									}

									if (sensorLaneST.getName().startsWith("G")
											|| sensorLaneST.getName().startsWith("C")) {
										// connect to signalgroup
										DoubleScalar.Rel<LengthUnit> longitudinalPositionFromEnd = new DoubleScalar.Rel<LengthUnit>(
												lane.getLength().getInUnit(
														LengthUnit.METER) - 10,
												LengthUnit.METER);
										String name = sensorLaneST.getName()
												.substring(1);
										name = name.replace(".", "");
										name = ("000" + name).substring(name
												.length());
										StopLineLane stopLine = new StopLineLane(
												lane,
												longitudinalPositionFromEnd,
												name);
										lane.addSensor(stopLine);
										GTM.mapLaneToStopLineLane.put(lane,
												stopLine);
									}
								}

							}

						}
					}
				}
			}
		}
	}
}
