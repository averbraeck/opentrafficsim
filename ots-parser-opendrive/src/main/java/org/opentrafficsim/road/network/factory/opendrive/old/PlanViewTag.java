package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.UUID;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class PlanViewTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** GeometryTags */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<GeometryTag> geometryTags = new ArrayList<GeometryTag>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param roadTag RoadTag; the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws OTSGeometryException when parsing of the tag fails
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parsePlanView(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final RoadTag roadTag)
            throws SAXException, OTSGeometryException, NetworkException
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

                geometryTag.z = assignHeight(geometryTag, roadTag.elevationProfileTag.elevationTags);

                GeometryTag.makeOTSNode(parser.network, geometryTag);

                planViewTag.geometryTags.add(geometryTag);
            }

            GeometryTag lastGeometryTag = new GeometryTag();

            GeometryTag previousTag = planViewTag.geometryTags.get(planViewTag.geometryTags.size() - 1);

            lastGeometryTag.id = roadTag.id + "." + String.valueOf(geometryCount);

            lastGeometryTag.length = new Length(0.0, LengthUnit.METER);
            // lastGeometryTag.length = roadTag.length.minus(previousTag.s);

            lastGeometryTag.s = roadTag.length;

            lastGeometryTag.x = new Length(0.0, LengthUnit.METER);

            lastGeometryTag.x = previousTag.x.plus(previousTag.length.times(Math.cos(previousTag.hdg.si)));

            lastGeometryTag.y = new Length(0.0, LengthUnit.METER);

            lastGeometryTag.y = previousTag.y.plus(previousTag.length.times(Math.sin(previousTag.hdg.si)));

            lastGeometryTag.hdg = previousTag.hdg;

            lastGeometryTag.z = previousTag.z;

            GeometryTag.makeOTSNode(parser.network, lastGeometryTag);

            planViewTag.geometryTags.add(lastGeometryTag);

            geometryCount = 0;
            for (GeometryTag geometryTag : planViewTag.geometryTags)
            {
                if (geometryTag.spiralTag != null)
                    interpolateSpiral(parser, planViewTag, geometryTag, geometryCount);
                if (geometryTag.arcTag != null)
                    interpolateArc(planViewTag, geometryTag, geometryCount, roadTag);
                geometryCount++;
            }
        }
        roadTag.designLine = buildDesignLine(planViewTag, roadTag);

    }

    /**
     * @param geometryTag GeometryTag; the geometry tag
     * @param elevationTags NavigableMap&lt;Double,ElevationTag&gt;; the elevations
     * @return elevation the height
     */
    private static Length assignHeight(GeometryTag geometryTag, NavigableMap<Double, ElevationTag> elevationTags)
    {
        Double key = geometryTag.s.si;
        if (elevationTags.containsKey(key))
            return elevationTags.get(key).elevation;
        else
        {
            Double before = elevationTags.floorKey(key);
            Double after = elevationTags.ceilingKey(key);

            if (after == null)
                return elevationTags.get(before).elevation;

            Double factor = (key - before) / (after - before);

            Length beHeight = elevationTags.get(before).elevation;
            Length afHeight = elevationTags.get(after).elevation;

            return afHeight.minus(beHeight).times(factor).plus(beHeight);
        }
    }

    /**
     * @param parser OpenDriveNetworkLaneParser; the parser
     * @param planViewTag PlanViewTag; the plan view tag
     * @param geometryTag GeometryTag; the geometry tag
     * @param geometryCount int; counter
     * @throws OTSGeometryException if geometry is invalid
     */
    private static void interpolateSpiral(OpenDriveNetworkLaneParserOld parser, PlanViewTag planViewTag, GeometryTag geometryTag,
            int geometryCount) throws OTSGeometryException
    {
        double startCurvature = geometryTag.spiralTag.curvStart.doubleValue();
        double endCurvature = geometryTag.spiralTag.curvEnd.doubleValue();
        OTSPoint3D start = geometryTag.node.getPoint();
        Length length = geometryTag.length;

        /*
         * int numSegments = 100;// (int) (length.doubleValue()/1); OTSLine3D line = Clothoid.clothoid(start,
         * AngleUtil.normalize(new Direction(geometryTag.hdg.si, AngleUnit.RADIAN)), startCurvature, endCurvature, length, new
         * Length(0, LengthUnit.SI), numSegments);
         */

        List<OTSPoint3D> pOutPut = new ArrayList<OTSPoint3D>();

        Length x1 = geometryTag.x.plus(geometryTag.length.times(Math.cos(geometryTag.hdg.si)).times(0.5));
        Length y1 = geometryTag.y.plus(geometryTag.length.times(Math.sin(geometryTag.hdg.si)).times(0.5));

        OTSPoint3D p = new OTSPoint3D(x1.si, y1.si, 0);
        pOutPut.add(p);

        Length x2 = geometryTag.x.plus(geometryTag.length.times(Math.cos(geometryTag.hdg.si)).times(0.66));
        Length y2 = geometryTag.y.plus(geometryTag.length.times(Math.sin(geometryTag.hdg.si)).times(0.66));

        OTSPoint3D q = new OTSPoint3D(x2.si, y2.si, 0);
        pOutPut.add(q);

        OTSLine3D otsLine = new OTSLine3D(pOutPut);

        // geometryTag.interLine = otsLine;

    }

    /**
     * @param planViewTag PlanViewTag; the plan view tag
     * @param geometryTag GeometryTag; the geometry tag
     * @param geometryCount int; counter
     * @param roadTag RoadTag; the road tag
     * @throws OTSGeometryException in case geometry is invalid
     */
    private static void interpolateArc(PlanViewTag planViewTag, GeometryTag geometryTag, int geometryCount, RoadTag roadTag)
            throws OTSGeometryException
    {
        double curvature = geometryTag.arcTag.curvature.doubleValue();
        OTSPoint3D start = geometryTag.node.getPoint();
        OTSPoint3D end = planViewTag.geometryTags.get(geometryCount + 1).node.getPoint();

        Length length = geometryTag.length;

        double radius = 1 / curvature;
        boolean side = false;

        List<OTSPoint3D> line = null;

        if (curvature < 0)
        {
            // side = true;
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
     * @param pFrom OTSPoint3D; start point
     * @param pTo OTSPoint3D; end point
     * @param pRadius double; radius
     * @param pMinDistance double; minimal distance
     * @param shortest boolean; shortest or longest curve
     * @param side boolean; left or right
     * @return list of points
     */
    private static List<OTSPoint3D> generateCurve(OTSPoint3D pFrom, OTSPoint3D pTo, double pRadius, double pMinDistance,
            boolean shortest, boolean side)
    {

        List<OTSPoint3D> pOutPut = new ArrayList<OTSPoint3D>();

        // Calculate the middle of the two given points.
        OTSPoint3D mPoint = new OTSPoint3D((pFrom.x + pTo.x) / 2, (pFrom.y + pTo.y) / 2, (pFrom.z + pTo.z) / 2);

        // System.out.println("Middle Between From and To = " + mPoint);

        // Calculate the distance between the two points
        double xDiff = pTo.x - pFrom.x;
        double yDiff = pTo.y - pFrom.y;
        double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        // System.out.println("Distance between From and To = " + distance);

        if (pRadius * 2.0f < distance)
        {
            throw new IllegalArgumentException("The radius is too small! The given points wont fall on the circle.");
        }

        // Calculate the middle of the expected curve.
        double factor = Math.sqrt(
                (pRadius * pRadius) / ((pTo.x - pFrom.x) * (pTo.x - pFrom.x) + (pTo.y - pFrom.y) * (pTo.y - pFrom.y)) - 0.25f);
        double x = 0;
        double y = 0;
        if (side)
        {
            x = 0.5f * (pFrom.x + pTo.x) + factor * (pTo.y - pFrom.y);
            y = 0.5f * (pFrom.y + pTo.y) + factor * (pFrom.x - pTo.x);
        }
        else
        {
            x = 0.5f * (pFrom.x + pTo.x) - factor * (pTo.y - pFrom.y);
            y = 0.5f * (pFrom.y + pTo.y) - factor * (pFrom.x - pTo.x);
        }
        OTSPoint3D circleMiddlePoint = new OTSPoint3D(x, y, 0);

        // System.out.println("Middle = " + circleMiddlePoint);

        // Calculate the two reference angles
        double angle1 = Math.atan2(pFrom.y - circleMiddlePoint.y, pFrom.x - circleMiddlePoint.x);
        double angle2 = Math.atan2(pTo.y - circleMiddlePoint.y, pTo.x - circleMiddlePoint.x);

        // Calculate the step.
        double step = pMinDistance / pRadius;
        // System.out.println("Step = " + step);

        // Swap them if needed
        if (angle1 > angle2)
        {
            double temp = angle1;
            angle1 = angle2;
            angle2 = temp;

        }

        if ((pTo.x - circleMiddlePoint.x < 0) && (pTo.y - circleMiddlePoint.y < 0) && (pFrom.y - circleMiddlePoint.y > 0)
                && (pFrom.x - circleMiddlePoint.x < 0))
        {
            double temp = angle1;
            angle1 = angle2;
            angle2 = temp;

            angle1 = angle1 - Math.PI * 2;
            // angle2 = (float) (angle2 - Math.PI);
        }
        else if ((pTo.x - circleMiddlePoint.x > 0) && (pTo.y - circleMiddlePoint.y < 0) && (pFrom.y - circleMiddlePoint.y > 0)
                && (pFrom.x - circleMiddlePoint.x < 0))
        {
            double temp = angle1;
            angle1 = angle2;
            angle2 = temp;

            angle1 = angle1 - Math.PI * 2;
        }
        else if ((pTo.x - circleMiddlePoint.x < 0) && (pTo.y - circleMiddlePoint.y < 0) && (pFrom.y - circleMiddlePoint.y > 0)
                && (pFrom.x - circleMiddlePoint.x > 0))
        {
            double temp = angle1;
            angle1 = angle2;
            angle2 = temp;

            angle1 = angle1 - Math.PI * 2;
            // angle2 = (float) (angle2 - Math.PI);
        }
        /*
         * else if((pTo.x - circleMiddlePoint.x > 0) && (pTo.y - circleMiddlePoint.y > 0) && (pFrom.y - circleMiddlePoint.y > 0)
         * && (pFrom.x - circleMiddlePoint.x > 0)) { double temp = angle1; angle1 = angle2; angle2 = temp; angle1 = angle1
         * -Math.PI*2; //angle2 = (float) (angle2 - Math.PI); }
         */

        boolean flipped = false;
        if (!shortest)
        {
            if (angle2 - angle1 < Math.PI)
            {
                double temp = angle1;
                angle1 = angle2;
                angle2 = temp;
                angle2 += Math.PI * 2.0f;
                flipped = true;
            }
        }

        double zStep = (pTo.z - pFrom.z) / ((angle2 - angle1) / step);
        double zFirst = pFrom.z;
        for (double f = angle1; f < angle2; f += step)
        {
            zFirst = zFirst + zStep;
            OTSPoint3D p = new OTSPoint3D(Math.cos(f) * pRadius + circleMiddlePoint.x,
                    Math.sin(f) * pRadius + circleMiddlePoint.y, zFirst);

            pOutPut.add(p);
        }
        /*
         * if (flipped ^ side) { pOutPut.add(pFrom); } else { pOutPut.add(pTo); }
         */

        return pOutPut;
    }

    /**
     * Find the nodes one by one that have one coordinate defined, and one not defined, and try to build the network from there.
     * @param roadTag RoadTag; the road tag
     * @param planViewTag PlanViewTag; the link to process
     * @return a CrossSectionLink
     * @throws OTSGeometryException when OTSLine3D cannot be constructed
     */
    static OTSLine3D buildDesignLine(final PlanViewTag planViewTag, final RoadTag roadTag) throws OTSGeometryException
    {
        int points = planViewTag.geometryTags.size();

        if (points < 2)
            System.err.println("No enough nodes");

        GeometryTag from = planViewTag.geometryTags.get(0);
        GeometryTag to = planViewTag.geometryTags.get(points - 1);

        List<OTSPoint3D> coordinates = new ArrayList<OTSPoint3D>();

        for (GeometryTag geometryTag : planViewTag.geometryTags)
        {
            // String a[] = geometryTag.id.split("\\.");

            if (coordinates.size() == 0)
            {
                coordinates.add(geometryTag.node.getPoint());
            }
            else
            {
                if (geometryTag.node.getPoint().x != coordinates.get(coordinates.size() - 1).x
                        && geometryTag.node.getPoint().y != coordinates.get(coordinates.size() - 1).y)
                {
                    coordinates.add(geometryTag.node.getPoint());
                }
            }
            OTSPoint3D lastPoint = new OTSPoint3D(coordinates.get(coordinates.size() - 1));

            if (geometryTag.interLine != null)
            {
                for (OTSPoint3D point : geometryTag.interLine.getPoints())
                {
                    /*
                     * double xDiff = lastPoint.x - point.x; double yDiff = lastPoint.y - point.y; double distance =
                     * Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                     */
                    if (lastPoint.x != point.x && lastPoint.y != point.y)
                    {
                        coordinates.add(point);
                        lastPoint = point;
                    }
                }
            }

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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "PlanViewTag [geometryTags=" + this.geometryTags + "]";
    }
}
