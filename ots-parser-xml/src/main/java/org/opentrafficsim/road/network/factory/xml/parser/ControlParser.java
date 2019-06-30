package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.trafficcontrol.FixedTimeController;
import org.opentrafficsim.trafficcontrol.FixedTimeController.SignalGroup;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.CONTROL.FIXEDTIME;
import org.opentrafficsim.xml.generated.CONTROL.TRAFCOD;
import org.opentrafficsim.xml.generated.CONTROLTYPE;
import org.opentrafficsim.xml.generated.CONTROLTYPE.SIGNALGROUP;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR.MULTIPLELANE;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR.MULTIPLELANE.INTERMEDIATELANES;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR.SINGLELANE;

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
     * @throws NetworkException when sensors could not be added to the network
     */
    public static void parseControl(final OTSRoadNetwork otsNetwork, final OTSSimulatorInterface simulator,
            final List<CONTROL> controls) throws NetworkException
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
                Map<String, CONTROL.FIXEDTIME.CYCLE> cycles = new LinkedHashMap<>();
                for (CONTROL.FIXEDTIME.CYCLE cycle : fixedTime.getCYCLE())
                {
                    cycles.put(cycle.getSIGNALGROUPID(), cycle);
                }
                for (SIGNALGROUP signalGroup : fixedTime.getSIGNALGROUP())
                {
                    String signalGroupId = signalGroup.getID();
                    CONTROL.FIXEDTIME.CYCLE cycle = cycles.get(signalGroupId);
                    Duration signalGroupOffset = cycle.getOFFSET();
                    Duration preGreen = cycle.getPREGREEN() == null ? Duration.ZERO : cycle.getPREGREEN();
                    Duration green = cycle.getGREEN();
                    Duration yellow = cycle.getYELLOW();
                    List<CONTROLTYPE.SIGNALGROUP.TRAFFICLIGHT> trafficLights = signalGroup.getTRAFFICLIGHT();
                    Set<String> trafficLightIds = new LinkedHashSet<>();
                    for (CONTROLTYPE.SIGNALGROUP.TRAFFICLIGHT trafficLight : trafficLights)
                    {
                        String linkId = trafficLight.getLINK();
                        String laneId = trafficLight.getLANE();
                        String trafficLightId = trafficLight.getTRAFFICLIGHTID();
                        trafficLightIds.add(linkId + "." + laneId + "." + trafficLightId);
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

            for (TRAFCOD trafCod : control.getTRAFCOD())
            {
                for (SENSOR sensor : trafCod.getSENSOR())
                {
                    if (null != sensor.getSINGLELANE())
                    {
                        // Handle single lane sensor
                        SINGLELANE singleLaneSensor = sensor.getSINGLELANE();
                        CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(singleLaneSensor.getLINK());
                        Lane lane = (Lane) link.getCrossSectionElement(singleLaneSensor.getLANE());
                        Length entryPosition =
                                Transformer.parseLengthBeginEnd(singleLaneSensor.getENTRYPOSITION(), lane.getLength());
                        Length exitPosition =
                                Transformer.parseLengthBeginEnd(singleLaneSensor.getEXITPOSITION(), lane.getLength());
                        new TrafficLightSensor(sensor.getID(), lane, entryPosition, lane, exitPosition, null,
                                RelativePosition.FRONT, RelativePosition.REAR, simulator, Compatible.EVERYTHING);
                    }
                    else
                    {
                        // Handle sensor spanning multiple lanes
                        MULTIPLELANE multiLaneSensor = sensor.getMULTIPLELANE();
                        CrossSectionLink entryLink = (CrossSectionLink) otsNetwork.getLink(multiLaneSensor.getENTRYLINK());
                        Lane entryLane = (Lane) entryLink.getCrossSectionElement(multiLaneSensor.getENTRYLANE());
                        Length entryPosition =
                                Transformer.parseLengthBeginEnd(multiLaneSensor.getENTRYPOSITION(), entryLane.getLength());
                        CrossSectionLink exitLink = (CrossSectionLink) otsNetwork.getLink(multiLaneSensor.getEXITLINK());
                        Lane exitLane = (Lane) exitLink.getCrossSectionElement(multiLaneSensor.getEXITLANE());
                        Length exitPosition =
                                Transformer.parseLengthBeginEnd(multiLaneSensor.getEXITPOSITION(), exitLane.getLength());
                        List<Lane> intermediateLanes = new ArrayList<>();
                        for (INTERMEDIATELANES linkAndLane : multiLaneSensor.getINTERMEDIATELANES())
                        {
                            CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(linkAndLane.getLINK());
                            intermediateLanes.add((Lane) link.getCrossSectionElement(linkAndLane.getLANE()));
                        }
                        new TrafficLightSensor(sensor.getID(), entryLane, entryPosition, exitLane, exitPosition,
                                intermediateLanes, RelativePosition.FRONT, RelativePosition.REAR, simulator,
                                Compatible.EVERYTHING);
                    }
                }
                // TODO get the TrafCOD program, etc.
            }
        }
    }

}
