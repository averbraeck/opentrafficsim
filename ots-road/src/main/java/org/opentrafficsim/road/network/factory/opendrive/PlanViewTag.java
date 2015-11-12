package org.opentrafficsim.road.network.factory.opendrive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.opentrafficsim.core.geometry.Clothoid;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck
 * $, initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class PlanViewTag
{

    /** geometryTags */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<GeometryTag> geometryTags = new ArrayList<GeometryTag>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param nodeList the list of subnodes of the road node
     * @param parser the parser with the lists of information
     * @param roadTag the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parsePlanView(final NodeList nodeList, final OpenDriveNetworkLaneParser parser, final RoadTag roadTag)
            throws SAXException, NetworkException
    {
        int geometryCount = 0;
        PlanViewTag planViewTag = new PlanViewTag();
        roadTag.planViewTag = planViewTag;

        for (Node node0 : XMLParser.getNodes(nodeList, "planView"))
        {
            for (Node node : XMLParser.getNodes(node0.getChildNodes(), "geometry"))
            {
                GeometryTag geometryTag = GeometryTag.parseGeometry(node, parser);
                geometryTag.id = roadTag.id + "." + String.valueOf(geometryCount);
                geometryCount++;

                planViewTag.geometryTags.add(geometryTag);
            }
            geometryCount = 0;
            for(GeometryTag geometryTag: planViewTag.geometryTags)
            {
                if (geometryTag.spiralTag != null && geometryCount > 1)
                    interpolateSpiral(parser, planViewTag, geometryTag, geometryCount);
                if (geometryTag.arcTag != null)
                    interpolateArc(planViewTag, geometryTag, geometryCount, roadTag);
                geometryCount++;
            }
        }
        roadTag.designLine = buildDesignLine(planViewTag, roadTag);

    }

    /**
     * @param parser
     * @param planViewTag
     * @param geometryTag
     * @param geometryCount 
     * @throws NetworkException
     */
    private static void interpolateSpiral(OpenDriveNetworkLaneParser parser, PlanViewTag planViewTag,
            GeometryTag geometryTag, int geometryCount) throws NetworkException
    {
        double startCurvature = geometryTag.spiralTag.curvStart.doubleValue();
        double endCurvature = geometryTag.spiralTag.curvEnd.doubleValue();
        OTSPoint3D start = geometryTag.node.getPoint();
        Length.Rel length = geometryTag.length;

        int numSegments = 100;// (int) (length.doubleValue()/1);
        
        //if(startCurvature == 0.0d && length.doubleValue() > 0.5d)
        {
            double dy =
                    geometryTag.y.doubleValue()
                            - planViewTag.geometryTags.get(geometryCount - 1).y.doubleValue();
            double dx =
                    geometryTag.x.doubleValue()
                            - planViewTag.geometryTags.get(geometryCount - 1).x.doubleValue();

            Angle.Abs startDirection = new Angle.Abs(Math.PI *2 - Math.atan2(dy, dx), AngleUnit.RADIAN);

            OTSLine3D line =
                    Clothoid.clothoid(start, startDirection, startCurvature, endCurvature, length, new Length.Rel(0,
                            LengthUnit.SI) /* FIXME: elevation at end */, numSegments);
            
/*            line = clothoid(new OTSPoint3D(10, 10, 5), new Angle.Abs(Math.PI / 8, AngleUnit.RADIAN), new LinearDensity(0 * -0.03,
                            LinearDensityUnit.PER_METER), new LinearDensity(0.04, LinearDensityUnit.PER_METER), new Length.Rel(100,
                            LengthUnit.METER), new Length.Rel(15, LengthUnit.METER), 100);*/

            // parser.spiras.put(line.hashCode(), line);
            //geometryTag.interLine = line;
        }


    }

    /**
     * @param planViewTag
     * @param geometryTag
     * @param geometryCount 
     * @param roadTag 
     * @throws NetworkException 
     */
    private static void interpolateArc(PlanViewTag planViewTag, GeometryTag geometryTag, int geometryCount, RoadTag roadTag) throws NetworkException
    {
        double curvature = geometryTag.arcTag.curvature.doubleValue();
        OTSPoint3D start = geometryTag.node.getPoint();
        OTSPoint3D end = planViewTag.geometryTags.get(geometryCount + 1).node.getPoint();
        
        Length.Rel length = geometryTag.length;
                
        double radius = 1/curvature;
        boolean side = false;
        
        List<OTSPoint3D> line = null;
        
        if(curvature < 0)
        {
            //side = true;            
            line = generateCurve(end, start, Math.abs(radius), 0.05, true, side);            
            Collections.reverse(line);                           
        }
        else
        {
            line = generateCurve(start, end, Math.abs(radius), 0.05, true, side);                       
        }

        OTSLine3D otsLine = new OTSLine3D(line);

        geometryTag.interLine = otsLine;

    }

    
    /**
     * @param pFrom
     * @param pTo
     * @param pRadius
     * @param pMinDistance
     * @param shortest
     * @param side
     * @return list
     */
    private static List<OTSPoint3D> generateCurve(OTSPoint3D pFrom, OTSPoint3D pTo, double pRadius, double pMinDistance, boolean shortest, boolean side)
    {

        List<OTSPoint3D> pOutPut = new ArrayList<OTSPoint3D>();

        // Calculate the middle of the two given points.
        OTSPoint3D mPoint = new OTSPoint3D((pFrom.x + pTo.x)/2, (pFrom.y + pTo.y)/2);

        //System.out.println("Middle Between From and To = " + mPoint);


        // Calculate the distance between the two points
        double xDiff = pTo.x - pFrom.x;
        double yDiff = pTo.y - pFrom.y;
        double distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        //System.out.println("Distance between From and To = " + distance);

        if (pRadius * 2.0f < distance) {
            throw new IllegalArgumentException("The radius is too small! The given points wont fall on the circle.");
        }

        // Calculate the middle of the expected curve.
        float factor = (float) Math.sqrt((pRadius * pRadius) / ((pTo.x - pFrom.x) * (pTo.x - pFrom.x) + (pTo.y - pFrom.y) * (pTo.y - pFrom.y)) - 0.25f);
        double x = 0;
        double y = 0;
        if (side) {
            x = 0.5f * (pFrom.x + pTo.x) + factor * (pTo.y - pFrom.y);
            y = 0.5f * (pFrom.y + pTo.y) + factor * (pFrom.x - pTo.x);
        } else {
            x = 0.5f * (pFrom.x + pTo.x) - factor * (pTo.y - pFrom.y);
            y = 0.5f * (pFrom.y + pTo.y) - factor * (pFrom.x - pTo.x);
        }
        OTSPoint3D circleMiddlePoint = new OTSPoint3D(x, y);
        
        //System.out.println("Middle = " + circleMiddlePoint);

        // Calculate the two reference angles
        float angle1 = (float) Math.atan2(pFrom.y - circleMiddlePoint.y, pFrom.x - circleMiddlePoint.x);
        float angle2 = (float) Math.atan2(pTo.y - circleMiddlePoint.y, pTo.x - circleMiddlePoint.x);        

        // Calculate the step.
        double step = pMinDistance / pRadius;
        //System.out.println("Step = " + step);

        // Swap them if needed
        if (angle1 > angle2) {
            float temp = angle1;
            angle1 = angle2;
            angle2 = temp;

        }
        
        if((pTo.x - circleMiddlePoint.x < 0) && (pTo.y - circleMiddlePoint.y < 0) && (pFrom.y - circleMiddlePoint.y > 0) && (pFrom.x - circleMiddlePoint.x < 0))
        {
            float temp = angle1;
            angle1 = angle2;
            angle2 = temp;
            
            angle1 = (float) (angle1 -Math.PI*2);
            //angle2 = (float) (angle2 - Math.PI);
        }
        
        boolean flipped = false;
        if (!shortest) {
            if (angle2 - angle1 < Math.PI) {
                float temp = angle1;
                angle1 = angle2;
                angle2 = temp;
                angle2 += Math.PI * 2.0f;
                flipped = true;
            }
        }
        for (float f = angle1; f < angle2; f += step) {
            OTSPoint3D p = new OTSPoint3D((float) Math.cos(f) * pRadius + circleMiddlePoint.x, (float) Math.sin(f) * pRadius + circleMiddlePoint.y);
            pOutPut.add(p);
        }
/*        if (flipped ^ side) {
            pOutPut.add(pFrom);
        } else {
            pOutPut.add(pTo);
        }*/

        return pOutPut;
    }

    
    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network
     * from there.
     * @param roadTag the road tag
     * @param planViewTag the link to process
     * @return a CrossSectionLink
     * @throws NetworkException when OTSLine3D cannot be constructed
     */
    static OTSLine3D buildDesignLine(final PlanViewTag planViewTag, final RoadTag roadTag) throws NetworkException
    {
        int points = planViewTag.geometryTags.size();

        if (points < 2)
            System.err.println("No enough nodes");

        GeometryTag from = planViewTag.geometryTags.get(0);
        GeometryTag to = planViewTag.geometryTags.get(points - 1);

        // OTSPoint3D[] coordinates = new OTSPoint3D[points];
        List<OTSPoint3D> coordinates = new ArrayList<OTSPoint3D>();

        for (GeometryTag geometryTag : planViewTag.geometryTags)
        {
            // String a[] = geometryTag.id.split("\\.");

            coordinates.add(geometryTag.node.getPoint());

            if (geometryTag.interLine != null)
            {
                for (OTSPoint3D point : geometryTag.interLine.getPoints())
                {
                    OTSPoint3D lastPoint = coordinates.get(coordinates.size()-1);
                    double xDiff = lastPoint.x - point.x;
                    double yDiff = lastPoint.y - point.y;
                    double distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff); 
                    if(distance > 0.1)
                        coordinates.add(point);
                }
            }

            // coordinates[Integer.valueOf(a[1])] = new OTSPoint3D(geometryTag.x.doubleValue(),
            // geometryTag.y.doubleValue(),
            // geometryTag.hdg.doubleValue());
        }

        OTSLine3D designLine = new OTSLine3D(coordinates);
        if (roadTag.id == null)
        {
            roadTag.id = UUID.randomUUID().toString();
        }
        roadTag.startNode = from.node;
        roadTag.endNode = to.node;
        // CrossSectionLink link = new CrossSectionLink(roadTag.id, from.node, to.node, LinkType.ALL, designLine,
        // LongitudinalDirectionality.BOTH, LaneKeepingPolicy.KEEP_LANE);
        return designLine;
    }
}
