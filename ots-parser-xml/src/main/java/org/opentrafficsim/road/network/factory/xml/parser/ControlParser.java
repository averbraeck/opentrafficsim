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
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.detector.DetectorType;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;
import org.opentrafficsim.trafficcontrol.FixedTimeController;
import org.opentrafficsim.trafficcontrol.FixedTimeController.SignalGroup;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCod;
import org.opentrafficsim.xml.generated.Control;
import org.opentrafficsim.xml.generated.Control.FixedTime;
import org.opentrafficsim.xml.generated.Control.FixedTime.Cycle;
import org.opentrafficsim.xml.generated.Control.TrafCod.Console;
import org.opentrafficsim.xml.generated.ControlType.SignalGroup.TrafficLight;
import org.opentrafficsim.xml.generated.ResponsiveControlType.Detector;
import org.opentrafficsim.xml.generated.ResponsiveControlType.Detector.MultipleLane;
import org.opentrafficsim.xml.generated.ResponsiveControlType.Detector.MultipleLane.IntermediateLanes;
import org.opentrafficsim.xml.generated.ResponsiveControlType.Detector.SingleLane;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * NodeParser takes care of parsing the Control tags for the Traffic Lights in the XML network.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param otsNetwork RoadNetwork; network
     * @param simulator OtsSimulatorInterface; simulator
     * @param control List&lt;Control&gt;; control objects
     * @param definitions Definitions; type definitions.
     * @throws NetworkException when sensors could not be added to the network
     * @throws IOException when a TrafCOD engine cannot be loaded
     * @throws MalformedURLException when a TrafCOD engine cannot be loaded
     * @throws TrafficControlException when a TrafCOD engine cannot be constructed for some other reason
     * @throws SimRuntimeException when a TrafCOD engine fails to initialize
     */
    public static void parseControl(final RoadNetwork otsNetwork, final OtsSimulatorInterface simulator, final Control control,
            final Definitions definitions)
            throws NetworkException, MalformedURLException, IOException, SimRuntimeException, TrafficControlException
    {

        // Fixed time controllers
        for (FixedTime fixedTime : ParseUtil.getObjectsOfType(control.getFixedTimeOrTrafCod(), FixedTime.class))
        {
            String id = fixedTime.getId();
            Duration cycleTime = fixedTime.getCycleTime();
            Duration offset = fixedTime.getOffset();
            Set<SignalGroup> signalGroups = new LinkedHashSet<>();
            Map<String, Cycle> cycles = new LinkedHashMap<>();
            for (Cycle cycle : fixedTime.getCycle())
            {
                cycles.put(cycle.getSignalGroupId(), cycle);
            }
            for (org.opentrafficsim.xml.generated.ControlType.SignalGroup signalGroup : fixedTime.getSignalGroup())
            {
                String signalGroupId = signalGroup.getId();
                Cycle cycle = cycles.get(signalGroupId);
                Duration signalGroupOffset = cycle.getOffset();
                Duration preGreen = cycle.getPreGreen() == null ? Duration.ZERO : cycle.getPreGreen();
                Duration green = cycle.getGreen();
                Duration yellow = cycle.getYellow();
                List<TrafficLight> trafficLights = signalGroup.getTrafficLight();
                Set<String> trafficLightIds = new LinkedHashSet<>();
                for (TrafficLight trafficLight : trafficLights)
                {
                    String linkId = trafficLight.getLink();
                    String laneId = trafficLight.getLane();
                    String trafficLightId = trafficLight.getTrafficLightId();
                    trafficLightIds.add(linkId + "." + laneId + "." + trafficLightId);
                }
                signalGroups.add(new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, preGreen, green, yellow));
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

        for (org.opentrafficsim.xml.generated.Control.TrafCod trafCod : ParseUtil
                .getObjectsOfType(control.getFixedTimeOrTrafCod(), org.opentrafficsim.xml.generated.Control.TrafCod.class))
        {
            String controllerName = trafCod.getId();
            String programString = trafCod.getProgram().getValue();
            List<String> program = null == programString ? TrafCod.loadTextFromURL(new URL(trafCod.getProgramFile()))
                    : Arrays.asList(programString.split("\n"));
            // Obtain the background image for the TrafCOD controller state display
            org.opentrafficsim.xml.generated.Control.TrafCod.Console.Map mapData = trafCod.getConsole().getMap();
            BufferedImage backgroundImage = null;
            if (null != mapData)
            {
                String graphicsType = mapData.getType();
                String encoding = mapData.getEncoding();
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
            Console trafCODConsole = trafCod.getConsole();
            if (trafCODConsole.getCoordinatesFile() != null)
            {
                System.out.println("coordinates file is " + trafCODConsole.getCoordinatesFile());
                throw new TrafficControlException("Loading coordinates from file not implemented yet");
            }
            String objectLocationsString = trafCODConsole.getCoordinates().getValue();
            List<String> displayObjectLocations =
                    null == objectLocationsString ? TrafCod.loadTextFromURL(new URL(trafCod.getConsole().getCoordinatesFile()))
                            : Arrays.asList(objectLocationsString.split("\n"));
            TrafCod trafCOD = new TrafCod(controllerName, program, simulator, backgroundImage, displayObjectLocations);
            otsNetwork.addNonLocatedObject(trafCOD);
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
            for (Detector detector : trafCod.getDetector())
            {
                if (null != detector.getSingleLane())
                {
                    // Handle single lane detector
                    SingleLane singleLaneDetector = detector.getSingleLane();
                    CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(singleLaneDetector.getLink());
                    Lane lane = (Lane) link.getCrossSectionElement(singleLaneDetector.getLane());
                    Length entryPosition =
                            Transformer.parseLengthBeginEnd(singleLaneDetector.getEntryPosition(), lane.getLength());
                    Length exitPosition =
                            Transformer.parseLengthBeginEnd(singleLaneDetector.getExitPosition(), lane.getLength());
                    DetectorType detectorType = definitions.get(DetectorType.class, detector.getType());
                    new TrafficLightDetector(detector.getId(), lane, entryPosition, lane, exitPosition, null,
                            RelativePosition.FRONT, RelativePosition.REAR, simulator, detectorType);
                }
                else
                {
                    // Handle detector spanning multiple lanes
                    MultipleLane multiLaneDetector = detector.getMultipleLane();
                    CrossSectionLink entryLink = (CrossSectionLink) otsNetwork.getLink(multiLaneDetector.getEntryLink());
                    Lane entryLane = (Lane) entryLink.getCrossSectionElement(multiLaneDetector.getEntryLane());
                    Length entryPosition =
                            Transformer.parseLengthBeginEnd(multiLaneDetector.getEntryPosition(), entryLane.getLength());
                    CrossSectionLink exitLink = (CrossSectionLink) otsNetwork.getLink(multiLaneDetector.getExitLink());
                    Lane exitLane = (Lane) exitLink.getCrossSectionElement(multiLaneDetector.getExitLane());
                    Length exitPosition =
                            Transformer.parseLengthBeginEnd(multiLaneDetector.getExitPosition(), exitLane.getLength());
                    List<Lane> intermediateLanes = new ArrayList<>();
                    for (IntermediateLanes linkAndLane : multiLaneDetector.getIntermediateLanes())
                    {
                        CrossSectionLink link = (CrossSectionLink) otsNetwork.getLink(linkAndLane.getLink());
                        intermediateLanes.add((Lane) link.getCrossSectionElement(linkAndLane.getLane()));
                    }
                    DetectorType detectorType = definitions.get(DetectorType.class, detector.getType());
                    new TrafficLightDetector(detector.getId(), entryLane, entryPosition, exitLane, exitPosition,
                            intermediateLanes, RelativePosition.FRONT, RelativePosition.REAR, simulator, detectorType);
                }
            }
            // TODO get the TrafCOD program, etc.
        }
    }

}
