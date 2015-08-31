package nl.grontmij.smarttraffic.lane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.network.lane.SinkSensor;

public class ReadNetworkData
{

    private ReadNetworkData()
    {
        // cannot be instantiated.
    }

    public static void readDetectors(OTSDEVSSimulatorInterface simulator, OTSNetwork network,
        HashMap<String, ConfigVri> configVriList, HashMap<String, AbstractSensor> mapSensor,
        HashMap<String, GenerateSensor> mapSensorGenerateCars, HashMap<String, KillSensor> mapSensorKillCars,
        HashMap<String, CheckSensor> mapSensorCheckCars)
    {
        // Detectors
        // define the detectors by type (ENTRANCE, INTERMEDIATE, EXIT)
        // Inventorize all detectors from the network, distinguished by type
        Map<?, ?> links = network.getLinkMap();
        Collection<Link> linkValues = (Collection<Link>) links.values();
        for (Link link : linkValues)
        {
            if (link instanceof CrossSectionLink)
            {
                CrossSectionLink csl = (CrossSectionLink) link;
                for (CrossSectionElement cse : csl.getCrossSectionElementList())
                {
                    if (cse instanceof Lane && !(cse instanceof NoTrafficLane))
                    {
                        Lane lane = (Lane) cse;
                        List<Sensor> sensors =
                            lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER), lane.getLength(),
                                GTM.GTUTYPE);
                        if (!sensors.isEmpty())
                        {
                        	List<SinkSensor> sinks = new ArrayList<SinkSensor>();
                        	List<KillSensor> kills = new ArrayList<KillSensor>();
                            for (Sensor sensor : sensors)
                            {
                                if (sensor instanceof SinkSensor)
                                {
                                	sinks.add((SinkSensor) sensor);
                                    KillSensor killSensor =
                                        new KillSensor(sensor.getLane(), sensor.getLongitudinalPosition(), sensor.getName(),
                                            ((SinkSensor) sensor).getSimulator());
                                    kills.add(killSensor);
                                }
                                if (sensor instanceof CheckSensor || sensor instanceof GenerateSensor
                                    || sensor instanceof KillSensor)
                                {
                                    mapSensor.put(sensor.getName(), (AbstractSensor) sensor);
                                }
                                if (sensor instanceof GenerateSensor)
                                {
                                    mapSensorGenerateCars.put(sensor.getName(), (GenerateSensor) sensor);
                                }
                                else if (sensor instanceof KillSensor)
                                {
                                    mapSensorKillCars.put(sensor.getName(), (KillSensor) sensor);
                                }
                                else if (sensor instanceof CheckSensor)
                                {
                                    mapSensorCheckCars.put(sensor.getName(), (CheckSensor) sensor);
                                }
                            }
                            if (!sinks.isEmpty()) {
                                sensors.removeAll(sinks);
                                sensors.addAll(kills);
                            }
                        }
                    }
                }
            }
        }
    }
}
