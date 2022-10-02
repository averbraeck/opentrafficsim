package org.opentrafficsim.road.network.factory.xml.parser;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.trafficcontrol.FixedTimeController;
import org.opentrafficsim.trafficcontrol.FixedTimeController.SignalGroup;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.CONTROL.FIXEDTIME;
import org.opentrafficsim.xml.generated.CONTROL.TRAFCOD;
import org.opentrafficsim.xml.generated.CONTROL.TRAFCOD.CONSOLE;
import org.opentrafficsim.xml.generated.CONTROLTYPE;
import org.opentrafficsim.xml.generated.CONTROLTYPE.SIGNALGROUP;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR.MULTIPLELANE;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR.MULTIPLELANE.INTERMEDIATELANES;
import org.opentrafficsim.xml.generated.RESPONSIVECONTROLTYPE.SENSOR.SINGLELANE;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * NodeParser takes care of parsing the CONTROL tags for the Traffic Lights in the XML network.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
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
     * @throws IOException when a TrafCOD engine cannot be loaded
     * @throws MalformedURLException when a TrafCOD engine cannot be loaded
     * @throws TrafficControlException when a TrafCOD engine cannot be constructed for some other reason
     * @throws SimRuntimeException when a TrafCOD engine fails to initialize
     */
    public static void parseControl(final OtsRoadNetwork otsNetwork, final OtsSimulatorInterface simulator,
            final List<CONTROL> controls)
            throws NetworkException, MalformedURLException, IOException, SimRuntimeException, TrafficControlException
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
                String controllerName = trafCod.getID();
                String programString = trafCod.getPROGRAM().getValue();
                List<String> program = null == programString ? TrafCOD.loadTextFromURL(new URL(trafCod.getPROGRAMFILE()))
                        : Arrays.asList(programString.split("\n"));
                // Obtain the background image for the TrafCOD controller state display
                TRAFCOD.CONSOLE.MAP mapData = trafCod.getCONSOLE().getMAP();
                BufferedImage backgroundImage = null;
                if (null != mapData)
                {
                    String graphicsType = mapData.getTYPE();
                    String encoding = mapData.getENCODING();
                    String encodedData = mapData.getValue();
                    if (!"base64".contentEquals(encoding))
                    {
                        throw new RuntimeException("Unexpected image encoding: " + encoding);
                    }
                    byte[] imageBytes = DatatypeConverter.parseBase64Binary(encodedData);
                    switch (graphicsType)
                    {
                        case "PNG":
                            backgroundImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                            // javax.imageio.ImageIO.write(backgroundImage, "png", new File("c:\\temp\\test.png"));
                            break;

                        default:
                            throw new RuntimeException("Unexpected image type: " + graphicsType);
                    }
                }
                CONSOLE trafCODConsole = trafCod.getCONSOLE();
                if (trafCODConsole.getCOORDINATESFILE() != null)
                {
                    System.out.println("coordinates file is " + trafCODConsole.getCOORDINATESFILE());
                    throw new TrafficControlException("Loading coordinates from file not implemented yet");
                }
                String objectLocationsString = trafCODConsole.getCOORDINATES().getValue();
                List<String> displayObjectLocations = null == objectLocationsString
                        ? TrafCOD.loadTextFromURL(new URL(trafCod.getCONSOLE().getCOORDINATESFILE()))
                        : Arrays.asList(objectLocationsString.split("\n"));
                TrafCOD trafCOD = new TrafCOD(controllerName, program, simulator, backgroundImage, displayObjectLocations);
                otsNetwork.addInvisibleObject(trafCOD);
                // this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                // this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                // this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                // this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                // this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                // this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);
                // Subscribe the TrafCOD machine to trace command events that we emit
                // addListener(this.trafCOD, TrafficController.TRAFFICCONTROL_SET_TRACING);
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 8, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "XR1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TD1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TL", 11, true});
                // System.out.println("demo: emitting a SET TRACING event for all variables related to stream 11");
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] { controllerName, "", 11, true });

                // this.trafCOD.traceVariablesOfStream(TrafficController.NO_STREAM, true);
                // this.trafCOD.traceVariablesOfStream(11, true);
                // this.trafCOD.traceVariable("MRV", 11, true);
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
