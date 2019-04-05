package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.trafficcontrol.FixedTimeController;
import org.opentrafficsim.trafficcontrol.FixedTimeController.SignalGroup;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.CONTROL.FIXEDTIME;
import org.opentrafficsim.xml.generated.CONTROL.FIXEDTIME.SIGNALGROUP.TRAFFICLIGHT;
import org.opentrafficsim.xml.generated.OTS;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * NodeParser takes care of parsing the CONTROL tags for the Traffic Lights in the XML network. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class ControlParser
{
    /** */
    private ControlParser()
    {
        // static class
    }

    /**
     * Creates control objects.
     * @param otsNetwork OTSRoadNetwork; network
     * @param simulator OTSSimulatorInterface; simulator
     * @param controls List&lt;CONTROL&gt;; control objects
     */
    public static void parseControl(final OTSRoadNetwork otsNetwork, final OTSSimulatorInterface simulator,
            final List<CONTROL> controls)
    {
        for (CONTROL control : controls)
        {
            // Fixed time controllers
            for (FIXEDTIME fixedTime : control.getFIXEDTIME())
            {
                String id = fixedTime.getID();
                Duration cycleTime = fixedTime.getCYCLETIME();
                Duration offset = fixedTime.getOFFSET();
                Set<SignalGroup> signalGroups = new LinkedHashSet<>();
                for (CONTROL.FIXEDTIME.SIGNALGROUP signalGroup : fixedTime.getSIGNALGROUP())
                {
                    String signalGroupId = signalGroup.getID();
                    Duration signalGroupOffset = signalGroup.getOFFSET();
                    Duration preGreen = signalGroup.getPREGREEN() == null ? Duration.ZERO : signalGroup.getPREGREEN();
                    Duration green = signalGroup.getGREEN();
                    Duration yellow = signalGroup.getYELLOW();

                    List<TRAFFICLIGHT> trafficLights = signalGroup.getTRAFFICLIGHT();
                    Set<String> trafficLightIds = new LinkedHashSet<>();
                    for (TRAFFICLIGHT trafficLight : trafficLights)
                    {
                        // TODO is linkId.trafficLight id as it is found in the network?
                        String linkId = trafficLight.getLINK();
                        String trafficLightId = trafficLight.getTRAFFICLIGHTID();
                        trafficLightIds.add(linkId + "." + trafficLightId);
                    }
                    signalGroups
                            .add(new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, preGreen, green, yellow));
                }
                try
                {
                    new FixedTimeController(id, simulator, otsNetwork, cycleTime, offset, signalGroups);
                }
                catch (SimRuntimeException exception)
                {
                    // cannot happen; parsing happens with a new simulator at time = 0
                    throw new RuntimeException(exception);
                }
            }
        }
    }

}
