package nl.grontmij.smarttraffic.lane;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;
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

	public static void readDetectors(OTSNetwork<?, ?, ?> network, HashMap<String, Sensor> mapSensor,
			HashMap<String, Sensor> mapSensorGenerateCars,
			HashMap<String, Sensor> mapSensorKillCars,
			HashMap<String, Sensor> mapSensorCheckCars) {
		// Detectors
		// define the detectors by type (ENTRANCE, INTERMEDIATE, EXIT)
		// Inventorize all detectors from the network, distinguished by type
		Map<?, ?> links = network.getLinkMap();
		Collection<Link<?, ?>> linkValues = (Collection<Link<?, ?>>) links
				.values();
		for (Link<?, ?> link : linkValues) {
			if (link instanceof CrossSectionLink) {
				CrossSectionLink<?, ?> csl = (CrossSectionLink<?, ?>) link;
				for (CrossSectionElement cse : csl
						.getCrossSectionElementList()) {
					if (cse instanceof Lane
							&& !(cse instanceof NoTrafficLane)) {
						Lane lane = (Lane) cse;
						List<Sensor> sensors = lane
								.getSensors(
										new DoubleScalar.Rel<LengthUnit>(0,
												LengthUnit.METER), lane
												.getLength());
						for (Sensor sensor : sensors) {
							mapSensor.put(sensor.getName(), sensor);
							if (sensor.getName().startsWith("G")) {
								mapSensorGenerateCars.put(sensor.getName(),
										sensor);
							} else if (sensor.getName().startsWith("K")) {
								mapSensorKillCars.put(sensor.getName(),
										sensor);
							} else if (sensor.getName().startsWith("C")) {
								mapSensorCheckCars.put(sensor.getName(),
										sensor);
							}
						}
					}
				}
			}
		}
		
	}

}
