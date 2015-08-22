package nl.grontmij.smarttraffic.lane;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Sensor;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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
                            lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER), lane.getLength());
                        if (!sensors.isEmpty())
                        {

                            for (Sensor sensor : sensors)
                            {
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
                        }
                    }
                }
            }
        }
    }
}
