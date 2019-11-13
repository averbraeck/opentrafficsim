/**
 *
 */
package org.opentrafficsim.road.network.factory.vissim;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.vissim.xsd.DEFINITIONS;
import org.opentrafficsim.road.network.factory.vissim.xsd.GLOBAL;
import org.opentrafficsim.road.network.factory.vissim.xsd.GTU;
import org.opentrafficsim.road.network.factory.vissim.xsd.LINK;
import org.opentrafficsim.road.network.factory.vissim.xsd.LINK.BEZIER;
import org.opentrafficsim.road.network.factory.vissim.xsd.NETWORK;
import org.opentrafficsim.road.network.factory.vissim.xsd.NODE;
import org.opentrafficsim.road.network.factory.vissim.xsd.ObjectFactory;
import org.opentrafficsim.road.network.factory.vissim.xsd.ROADLAYOUT;
import org.opentrafficsim.road.network.factory.vissim.xsd.ROADTYPE;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;

/**
 * @author NLGUTU
 */
public class XMLNetworkWriter
{

    static void writeToXML(File file, Map<String, LinkTag> linkTags, Map<String, NodeTag> nodeTags, String sinkKillClassName,
            String sensorClassName, String trafficLightName) throws NetworkException
    {
        try
        {

            DEFINITIONS definitions = generateDefinitions();

            generateGtusAndRoadtypes(definitions);

            List<NODE> nodes = new ArrayList<>();
            generateNodes(nodes, nodeTags);

            List<LINK> links = new ArrayList<>();
            List<ROADLAYOUT> roadLayouts = new ArrayList<>();
            generateLinks(links, roadLayouts, linkTags, sinkKillClassName, sensorClassName, trafficLightName);
            definitions.getContent().addAll(roadLayouts);

            marshall(file, definitions, nodes, links);

        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }

    private static DEFINITIONS generateDefinitions()
    {
        DEFINITIONS definitions = new DEFINITIONS();
        generateGTUTypes(definitions);
        GLOBAL global = new GLOBAL();
        definitions.getContent().add(global);
        return definitions;
    }

    private static void generateGTUTypes(DEFINITIONS definitions)
    {
        List<GTUTYPE> gtuTypes = new ArrayList<>();
        GTUTYPE gtuType = new GTUTYPE();
        gtuType.setNAME("CAR");
        gtuTypes.add(gtuType);
        definitions.getContent().addAll(gtuTypes);
    }

    private static void generateGtusAndRoadtypes(DEFINITIONS definitions)
    {
        // definitions.getContent().add(gtuType);
        List<GTU> gtus = new ArrayList<>();
        GTU gtu = new GTU();
        gtu.setNAME("CAR");
        gtu.setGTUTYPE("CAR");
        gtu.setMAXSPEED("CONST(" + new Speed(140, SpeedUnit.KM_PER_HOUR).getInUnit(SpeedUnit.KM_PER_HOUR) + ") km/h");
        gtu.setLENGTH("CONST(" + new Length(4.5, LengthUnit.METER).getInUnit(LengthUnit.METER) + ") m");
        gtu.setWIDTH("CONST(" + new Length(2.0, LengthUnit.METER).getInUnit(LengthUnit.METER) + ") m");
        gtus.add(gtu);
        definitions.getContent().addAll(gtus);

        // road types
        List<ROADTYPE> roadTypes = new ArrayList<>();
        ROADTYPE roadType = new ROADTYPE();
        roadType.setDEFAULTLANEKEEPING("KEEPLANE");
        roadType.setDEFAULTOVERTAKING("NONE");
        roadType.setDEFAULTLANEWIDTH("3.5m");
        roadType.setNAME("RINGROAD");
        ROADTYPE.SPEEDLIMIT speedLimit = new ROADTYPE.SPEEDLIMIT();
        speedLimit.setGTUTYPE(gtu.getGTUTYPE());
        speedLimit.setLEGALSPEEDLIMIT(new Speed(140, SpeedUnit.KM_PER_HOUR).getInUnit(SpeedUnit.KM_PER_HOUR) + " km/h");
        roadType.getSPEEDLIMIT().add(speedLimit);
        roadTypes.add(roadType);
        definitions.getContent().addAll(roadTypes);

    }

    private static void marshall(File file, DEFINITIONS definitions, List<NODE> nodes, List<LINK> links) throws JAXBException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.opentrafficsim.road.network.factory.vissim.xsd");
        Marshaller marshaller = jaxbContext.createMarshaller();
        ObjectFactory outputFactory = new ObjectFactory();
        NETWORK networkElement = outputFactory.createNETWORK();
        networkElement.getDEFINITIONSOrIncludeOrNODE().add(definitions);
        networkElement.getDEFINITIONSOrIncludeOrNODE().addAll(nodes);
        networkElement.getDEFINITIONSOrIncludeOrNODE().addAll(links);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(networkElement, System.out);
        marshaller.marshal(networkElement, file);
    }

    private static void generateLinks(List<LINK> links, List<ROADLAYOUT> roadLayouts, Map<String, LinkTag> linkTags,
            String sinkKillClassName, String sensorClassName, String trafficLightName) throws NetworkException
    {

        Iterator<LinkTag> iter = linkTags.values().iterator();
        while (iter.hasNext())
        {
            LinkTag inputLink = iter.next();
            LINK link = new LINK();
            // set link the attributes and items
            link.setNAME(inputLink.name);
            String layoutName = "rl" + link.getNAME();
            link.setROADLAYOUTAttribute(layoutName);

            if (inputLink.arcTag != null)
            {
                LINK.ARC arc = new LINK.ARC();
                arc.setANGLE(inputLink.arcTag.angle.getInUnit(DirectionUnit.EAST_DEGREE) + " deg");
                arc.setRADIUS("" + inputLink.arcTag.radius.getInUnit(LengthUnit.METER));
                arc.setDIRECTION("" + inputLink.arcTag.direction);
                link.setARC(arc);
            }

            if (inputLink.bezierTag != null)
            {
                LINK.BEZIER bezier = new BEZIER();
                link.setBEZIER(bezier);
            }

            ROADLAYOUT rla = new ROADLAYOUT();
            rla.setROADTYPE("RINGROAD");
            rla.setNAME(layoutName);

            Iterator<Lane> lanes = inputLink.lanes.values().iterator();
            while (lanes.hasNext())
            {
                ROADLAYOUT.LANE lane = new ROADLAYOUT.LANE();
                Lane inputLane = lanes.next();
                lane.setNAME(inputLane.getId());
                lane.setWIDTH(inputLane.getBeginWidth().getInUnit(LengthUnit.METER) + "m");
                lane.setOFFSET(inputLane.getDesignLineOffsetAtBegin().getInUnit(LengthUnit.METER) + "m");
                if (inputLane.getDesignLineOffsetAtBegin().ne(inputLane.getDesignLineOffsetAtEnd()))
                {
                    double differenceOffset = inputLane.getDesignLineOffsetAtEnd().minus(inputLane.getDesignLineOffsetAtBegin())
                            .getInUnit(LengthUnit.METER);
                    link.setOFFSETEND("" + differenceOffset + "m");
                }
                if (inputLink.connector)
                {
                    lane.setCOLOR("BLACK");
                }
                else
                {
                    lane.setCOLOR("GRAY");
                }
                lane.setDIRECTION("FORWARD");
                ROADLAYOUT.LANE.SPEEDLIMIT speedLimit = new ROADLAYOUT.LANE.SPEEDLIMIT();
                try
                {
                    speedLimit.setLEGALSPEEDLIMIT(
                            inputLane.getSpeedLimit(inputLane.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE))
                                    .getInUnit(SpeedUnit.KM_PER_HOUR) + " km/h");
                }
                catch (Exception exception)
                {
                    System.err.println(exception.getMessage());
                    speedLimit.setLEGALSPEEDLIMIT("100.0 km/h");
                }
                speedLimit.setGTUTYPE("CAR");
                lane.getSPEEDLIMIT().add(speedLimit);
                rla.getLANEOrNOTRAFFICLANEOrSHOULDER().add(lane);
                for (SingleSensor inputSensor : inputLane.getSensors())
                {
                    LINK.SENSOR sensor = new LINK.SENSOR();
                    sensor.setNAME(inputSensor.getId());
                    sensor.setLANE(lane.getNAME());
                    sensor.setPOSITION(
                            Double.toString(inputSensor.getLongitudinalPosition().getInUnit(LengthUnit.METER)) + " m");
                    sensor.setTRIGGER(" " + inputSensor.getPositionType());
                    if (sensor.getNAME().startsWith("SINK@"))
                    {
                        sensor.setCLASS(sinkKillClassName);
                    }
                    else
                    {
                        sensor.setCLASS(sensorClassName);
                    }
                    link.getLANEOVERRIDEOrGENERATOROrLISTGENERATOR().add(sensor);
                }

                for (LaneBasedObject inputSimpleTrafficLight : inputLane.getLaneBasedObjects())
                {
                    LINK.TRAFFICLIGHT simpleTrafficLight = new LINK.TRAFFICLIGHT();
                    simpleTrafficLight.setNAME(inputSimpleTrafficLight.getId());
                    simpleTrafficLight.setLANE(lane.getNAME());
                    simpleTrafficLight.setPOSITION(
                            Double.toString(inputSimpleTrafficLight.getLongitudinalPosition().getInUnit(LengthUnit.METER))
                                    + " m");
                    simpleTrafficLight.setCLASS(trafficLightName);
                    link.getLANEOVERRIDEOrGENERATOROrLISTGENERATOR().add(simpleTrafficLight);
                }
                ROADLAYOUT.STRIPE stripe = new ROADLAYOUT.STRIPE();
                stripe.setTYPE("DASHED");
                stripe.setOFFSET(inputLane.getDesignLineOffsetAtBegin().minus(inputLane.getBeginWidth().divideBy(2.0))
                        .getInUnit(LengthUnit.METER) + "m");
                rla.getLANEOrNOTRAFFICLANEOrSHOULDER().add(stripe);

            }
            // link.setROADLAYOUT(rla);
            roadLayouts.add(rla);

            if (inputLink.straightTag != null)
            {
                LINK.STRAIGHT straight = new LINK.STRAIGHT();
                if (inputLink.straightTag.length != null)
                {
                    straight.setLENGTH(inputLink.straightTag.length.getInUnit(LengthUnit.METER) + " m");
                }
                link.setSTRAIGHT(straight);
            }

            if (inputLink.polyLineTag != null)
            {
                LINK.POLYLINE polyLine = new LINK.POLYLINE();
                String coordString = null;
                int length = inputLink.polyLineTag.vertices.length;
                for (int i = 0; i < length; i++)
                {
                    OTSPoint3D coord = inputLink.polyLineTag.vertices[i];
                    coordString = "(" + coord.x + "," + coord.y + "," + coord.z + ")";
                    polyLine.getINTERMEDIATEPOINTS().add(coordString);
                }
                link.setPOLYLINE(polyLine);
            }

            link.setNODESTART(inputLink.nodeStartTag.name);
            link.setNODEEND(inputLink.nodeEndTag.name);
            links.add(link);
        }
    }

    private static void generateNodes(List<NODE> nodes, Map<String, NodeTag> nodeTags)
    {
        Iterator<NodeTag> iterNode = nodeTags.values().iterator();
        while (iterNode.hasNext())
        {
            NodeTag inputNode = iterNode.next();
            NODE node = new NODE();
            node.setNAME(inputNode.name);
            node.setCOORDINATE(inputNode.coordinate.toString());
            nodes.add(node);
        }
    }

}
