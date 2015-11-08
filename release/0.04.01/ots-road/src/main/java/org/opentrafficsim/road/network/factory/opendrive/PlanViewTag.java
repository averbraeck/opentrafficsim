package org.opentrafficsim.road.network.factory.opendrive;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.AngleUtil;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.opentrafficsim.core.geometry.Clothoid;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkType;
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
            for (Node node : XMLParser.getNodes(node0.getChildNodes(), "geometry"))
            {
                GeometryTag geometryTag = GeometryTag.parseGeometry(node, parser);
                geometryTag.id = roadTag.id + "." + String.valueOf(geometryCount);
                geometryCount++;

                planViewTag.geometryTags.add(geometryTag);

                if (geometryTag.spiralTag != null && geometryCount != 1)
                    interpolateSpiral(parser, planViewTag, geometryTag);
                if (geometryTag.arcTag != null)
                    interpolateArc(planViewTag, geometryTag);
            }
        roadTag.designLine = buildDesignLine(planViewTag, roadTag);

    }

    /**
     * @param parser
     * @param planViewTag
     * @param geometryTag
     * @throws NetworkException
     */
    private static void interpolateSpiral(OpenDriveNetworkLaneParser parser, PlanViewTag planViewTag,
            GeometryTag geometryTag) throws NetworkException
    {
        double startCurvature = geometryTag.spiralTag.curvStart.doubleValue();
        double endCurvature = geometryTag.spiralTag.curvEnd.doubleValue();
        OTSPoint3D start = geometryTag.node.getPoint();
        Length.Rel length = geometryTag.length;

        int numSegments = 64;// (int) (length.doubleValue()/1);
        
        if(startCurvature == 0.0d && length.doubleValue() > 0.5d)
        {
            double dy =
                    geometryTag.y.doubleValue()
                            - planViewTag.geometryTags.get(planViewTag.geometryTags.size() - 2).y.doubleValue();
            double dx =
                    geometryTag.x.doubleValue()
                            - planViewTag.geometryTags.get(planViewTag.geometryTags.size() - 2).x.doubleValue();

            Angle.Abs startDirection = AngleUtil.normalize(new Angle.Abs(Math.PI - Math.atan2(dy, dx), AngleUnit.SI));

            OTSLine3D line =
                    Clothoid.clothoid(start, startDirection, startCurvature, endCurvature, length, new Length.Rel(0,
                            LengthUnit.SI) /* FIXME: elevation at end */, numSegments);

            // parser.spiras.put(line.hashCode(), line);
            //geometryTag.interLine = line;
        }


    }

    /**
     * @param planViewTag
     * @param geometryTag
     */
    private static void interpolateArc(PlanViewTag planViewTag, GeometryTag geometryTag)
    {
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
                int i = 0;
                int j = geometryTag.length.divideBy(1.0).intValue();
                for (OTSPoint3D point : geometryTag.interLine.getPoints())
                {
                    if (i % j == 0)
                        coordinates.add(point);
                    i++;
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
        // LaneKeepingPolicy.KEEP_LANE);
        return designLine;
    }
}
