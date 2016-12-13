package org.opentrafficsim.demo.conflict;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;

import nl.tudelft.simulation.immutablecollections.ImmutableMap;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class ConflictBuilder
{

    /**
     * Empty constructor.
     */
    private ConflictBuilder()
    {
        //
    }

    /**
     * Build conflicts on network.
     * @param network network
     * @param gtuType gtu type
     * @param simulator simulator
     * @throws OTSGeometryException in case of geometry exception
     */
    static void buildConflicts(final OTSNetwork network, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator)
            throws OTSGeometryException
    {
        // create list of lanes
        ImmutableMap<String, Link> links = network.getLinkMap();
        List<Lane> lanes = new ArrayList<>();
        for (String linkId : links.keySet())
        {
            Link link = links.get(linkId);
            if (link instanceof CrossSectionLink)
            {
                for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                {
                    if (element instanceof Lane)
                    {
                        lanes.add((Lane) element);
                    }
                }
            }
        }
        buildConflicts(lanes, gtuType, simulator);
    }

    /**
     * Build conflicts on list of lanes.
     * @param lanes lanes
     * @param gtuType gtu type
     * @param simulator simulator
     * @throws OTSGeometryException in case of geometry exception
     */
    static void buildConflicts(final List<Lane> lanes, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator)
            throws OTSGeometryException
    {
        // loop Lane / GTUDirectionality combinations
        for (int i = 0; i < lanes.size(); i++)
        {
            Lane lane1 = lanes.get(i);
            GTUDirectionality[] dirs1;
            if (lane1.getDirectionality(gtuType).isForward())
            {
                dirs1 = new GTUDirectionality[] { GTUDirectionality.DIR_PLUS };
            }
            else if (lane1.getDirectionality(gtuType).isBackward())
            {
                dirs1 = new GTUDirectionality[] { GTUDirectionality.DIR_MINUS };
            }
            else
            {
                dirs1 = new GTUDirectionality[] { GTUDirectionality.DIR_PLUS, GTUDirectionality.DIR_MINUS };
            }
            for (GTUDirectionality dir1 : dirs1)
            {
                Map<Lane, GTUDirectionality> down1 = lane1.downstreamLanes(dir1, gtuType);
                Map<Lane, GTUDirectionality> up1 = lane1.upstreamLanes(dir1, gtuType);

                for (int j = i + 1; j < lanes.size(); j++)
                {
                    // quick bounding box check of both lanes
                    Lane lane2 = lanes.get(j);
                    if (!lane1.getContour().intersects(lane2.getContour()))
                    {
                        continue;
                    }

                    GTUDirectionality[] dirs2;
                    if (lane2.getDirectionality(gtuType).isForward())
                    {
                        dirs2 = new GTUDirectionality[] { GTUDirectionality.DIR_PLUS };
                    }
                    else if (lane2.getDirectionality(gtuType).isBackward())
                    {
                        dirs2 = new GTUDirectionality[] { GTUDirectionality.DIR_MINUS };
                    }
                    else
                    {
                        dirs2 = new GTUDirectionality[] { GTUDirectionality.DIR_PLUS, GTUDirectionality.DIR_MINUS };
                    }
                    for (GTUDirectionality dir2 : dirs2)
                    {
                        Map<Lane, GTUDirectionality> down2 = lane2.downstreamLanes(dir2, gtuType);
                        Map<Lane, GTUDirectionality> up2 = lane2.upstreamLanes(dir2, gtuType);

                        // see if conflict needs to be build, and build if so
                        buildConflict(lane1, dir1, down1, up1, lane2, dir2, down2, up2, gtuType, simulator);
                    }
                }
            }
        }

    }

    /**
     * Build conflict on single lane pair. Connecting lanes are determined.
     * @param lane1 lane 1
     * @param dir1 gtu direction 1
     * @param lane2 lane 2
     * @param dir2 gtu direction 2
     * @param gtuType gtu type
     * @param simulator simulator
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void buildConflict(final Lane lane1, final GTUDirectionality dir1, final Lane lane2,
            final GTUDirectionality dir2, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator)
            throws OTSGeometryException
    {
        Map<Lane, GTUDirectionality> down1 = lane1.downstreamLanes(dir1, gtuType);
        Map<Lane, GTUDirectionality> up1 = lane1.upstreamLanes(dir1, gtuType);
        Map<Lane, GTUDirectionality> down2 = lane2.downstreamLanes(dir2, gtuType);
        Map<Lane, GTUDirectionality> up2 = lane2.upstreamLanes(dir2, gtuType);
        buildConflict(lane1, dir1, down1, up1, lane2, dir2, down2, up2, gtuType, simulator);
    }

    /**
     * Build conflict on single lane pair. Connecting lanes are determined.
     * @param lane1 lane 1
     * @param dir1 gtu direction 1
     * @param down1 downstream lanes 1
     * @param up1 upstream lanes 1
     * @param lane2 lane 2
     * @param dir2 gtu direction 2
     * @param down2 downstream lane 2
     * @param up2 upstream lanes 2
     * @param gtuType gtu type
     * @param simulator simulator
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildConflict(final Lane lane1, final GTUDirectionality dir1, final Map<Lane, GTUDirectionality> down1,
            final Map<Lane, GTUDirectionality> up1, final Lane lane2, final GTUDirectionality dir2,
            final Map<Lane, GTUDirectionality> down2, final Map<Lane, GTUDirectionality> up2, final GTUType gtuType,
            final OTSDEVSSimulatorInterface simulator) throws OTSGeometryException
    {

        // find if lanes intersect
        ConflictType conflictType = null;
        boolean splitOrMerge = false;
        OTSLine3D line1 = lane1.getCenterLine();
        OTSLine3D line2 = lane2.getCenterLine();
        // merge at ends?
        for (Entry<Lane, GTUDirectionality> next1 : down1.entrySet())
        {
            for (Entry<Lane, GTUDirectionality> next2 : down2.entrySet())
            {
                if (next1.equals(next2))
                {
                    conflictType = ConflictType.MERGE;
                    splitOrMerge = true;
                }
            }
        }
        // split at starts?
        for (Entry<Lane, GTUDirectionality> prev1 : up1.entrySet())
        {
            for (Entry<Lane, GTUDirectionality> prev2 : up2.entrySet())
            {
                if (prev1.equals(prev2))
                {
                    conflictType = ConflictType.SPLIT;
                    splitOrMerge = true;
                }
            }
        }
        // if not merge or split, crossing?
        if (!splitOrMerge)
        {
            Intersection centerIntersection = Intersection.getIntersection(line1, line2);
            if (centerIntersection == null)
            {
                // no conflict
                return;
            }
            // if lanes intersect where they are longitudinally connected, this is not a crossing
            for (Lane next : down1.keySet())
            {
                if (next.equals(lane2))
                {
                    return;
                }
            }
            for (Lane prev : up1.keySet())
            {
                if (prev.equals(lane2))
                {
                    return;
                }
            }
            // reverse search not required due to symmetrical coupling of lanes
            conflictType = ConflictType.CROSSING;
        }

        // obtain fractional locations of intersections of left and right side of lanes
        double[] fractions = new double[] { dir1.isPlus() ? 1.0 : 0, dir1.isPlus() ? 0 : 1.0, dir2.isPlus() ? 1.0 : 0,
                dir2.isPlus() ? 0 : 1.0 };
        OTSLine3D left1 = line1.offsetLine(lane1.getBeginWidth().si / 2, lane1.getEndWidth().si / 2);
        OTSLine3D right1 = line1.offsetLine(-lane1.getBeginWidth().si / 2, -lane1.getEndWidth().si / 2);
        OTSLine3D left2 = line2.offsetLine(lane2.getBeginWidth().si / 2, lane2.getEndWidth().si / 2);
        OTSLine3D right2 = line2.offsetLine(-lane2.getBeginWidth().si / 2, -lane2.getEndWidth().si / 2);
        deriveFractions(fractions, left1, dir1, left2, dir2);
        deriveFractions(fractions, left1, dir1, right2, dir2);
        deriveFractions(fractions, right1, dir1, left2, dir2);
        deriveFractions(fractions, right1, dir1, right2, dir2);
        // override for split and merge (better accuracy)
        if (conflictType.equals(ConflictType.SPLIT))
        {
            fractions[0] = dir1.isPlus() ? 0.0 : 1.0;
            fractions[2] = dir2.isPlus() ? 0.0 : 1.0;
        }
        if (conflictType.equals(ConflictType.MERGE))
        {
            fractions[1] = dir1.isPlus() ? 1.0 : 0.0;
            fractions[3] = dir2.isPlus() ? 1.0 : 0.0;
        }

        // get locations and length
        Length longitudinalPosition1 = lane1.getLength().multiplyBy(fractions[0]);
        Length longitudinalPosition2 = lane2.getLength().multiplyBy(fractions[2]);
        Length length1 = lane1.getLength().multiplyBy(Math.abs(fractions[1] - fractions[0]));
        Length length2 = lane2.getLength().multiplyBy(Math.abs(fractions[3] - fractions[2]));

        // get geometries
        OTSLine3D geometry1 = getGeometry(lane1, fractions[0], fractions[1]);
        OTSLine3D geometry2 = getGeometry(lane2, fractions[2], fractions[3]);

        // determine conflict rules
        ConflictRule[] conflictRules =
                getConflictRules(lane1, longitudinalPosition1, lane2, longitudinalPosition2, conflictType);

        // make conflict
        try
        {
            Conflict.generateConflictPair(conflictType, lane1, longitudinalPosition1, length1, geometry1, conflictRules[0],
                    gtuType, lane2, longitudinalPosition2, length2, geometry2, conflictRules[1], gtuType, simulator);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Created a wrong conflict combination.", exception);
        }
    }

    /**
     * Derives fraction per combination of lane edges.
     * @param fracs fractions so far
     * @param line1 edge of lane 1
     * @param dir1 direction 1
     * @param line2 edge of lane 2
     * @param dir2 direction 2
     * @throws OTSGeometryException in case of geometry exception
     */
    private static void deriveFractions(final double[] fracs, final OTSLine3D line1, final GTUDirectionality dir1,
            final OTSLine3D line2, final GTUDirectionality dir2) throws OTSGeometryException
    {
        Intersection offsetIntersect = Intersection.getIntersection(line1, line2);
        if (offsetIntersect != null)
        {
            fracs[0] = dir1.isPlus() ? Math.min(fracs[0], offsetIntersect.getFraction1())
                    : Math.max(fracs[0], offsetIntersect.getFraction1());
            fracs[1] = dir1.isPlus() ? Math.max(fracs[1], offsetIntersect.getFraction1())
                    : Math.min(fracs[1], offsetIntersect.getFraction1());
            fracs[2] = dir2.isPlus() ? Math.min(fracs[2], offsetIntersect.getFraction2())
                    : Math.max(fracs[2], offsetIntersect.getFraction2());
            fracs[3] = dir2.isPlus() ? Math.max(fracs[3], offsetIntersect.getFraction2())
                    : Math.min(fracs[3], offsetIntersect.getFraction2());
        }
    }

    /**
     * Creates geometry for conflict.
     * @param lane lane
     * @param fStart longitudinal fraction of start
     * @param fEnd longitudinal fraction of end
     * @return geometry for conflict
     * @throws OTSGeometryException in case of geometry exception
     */
    private static OTSLine3D getGeometry(final Lane lane, final double fStart, final double fEnd) throws OTSGeometryException
    {
        OTSLine3D centerLine = lane.getCenterLine().extractFractional(fStart, fEnd);
        double startWidth = lane.getWidth(fStart).si;
        double endWidth = lane.getWidth(fEnd).si;
        OTSLine3D left = centerLine.offsetLine(startWidth / 2, endWidth / 2);
        OTSLine3D right = centerLine.offsetLine(-startWidth / 2, -endWidth / 2).reverse();
        OTSPoint3D[] points = new OTSPoint3D[left.size() + right.size()];
        System.arraycopy(left.getPoints(), 0, points, 0, left.size());
        System.arraycopy(right.getPoints(), 0, points, left.size(), right.size());
        return new OTSLine3D(points);
    }

    /**
     * Determine conflict rules.
     * @param lane1 lane 1
     * @param longitudinalPosition1 position 1
     * @param lane2 lane 2
     * @param longitudinalPosition2 position 2
     * @param conflictType conflict type
     * @return conflict rule 1 and 2
     * @throws OTSGeometryException in case of geometry exception
     */
    private static ConflictRule[] getConflictRules(final Lane lane1, final Length longitudinalPosition1, final Lane lane2,
            final Length longitudinalPosition2, final ConflictType conflictType) throws OTSGeometryException
    {
        ConflictRule[] conflictRules = new ConflictRule[2];
        Priority priority1 = lane1.getParentLink().getPriority();
        Priority priority2 = lane2.getParentLink().getPriority();
        if (conflictType.equals(ConflictType.SPLIT))
        {
            conflictRules[0] = ConflictRule.SPLIT;
            conflictRules[1] = ConflictRule.SPLIT;
        }
        else if (priority1.isAllStop() && priority2.isAllStop())
        {
            conflictRules[0] = ConflictRule.ALL_STOP;
            conflictRules[1] = ConflictRule.ALL_STOP;
        }
        else if (priority1.equals(priority2))
        {
            // based on right- or left-hand traffic
            DirectedPoint p1 = lane1.getCenterLine().getLocation(longitudinalPosition1);
            DirectedPoint p2 = lane2.getCenterLine().getLocation(longitudinalPosition2);
            double diff = p2.getRotZ() - p1.getRotZ();
            while (diff > Math.PI)
            {
                diff -= 2 * Math.PI;
            }
            while (diff < -Math.PI)
            {
                diff += 2 * Math.PI;
            }
            if (diff > 0.0)
            {
                // 2 comes from the right
                conflictRules[0] = priority1.isStop() ? ConflictRule.STOP : ConflictRule.GIVE_WAY;
                conflictRules[1] = ConflictRule.PRIORITY;
            }
            else
            {
                // 1 comes from the right
                conflictRules[0] = ConflictRule.PRIORITY;
                conflictRules[1] = priority2.isStop() ? ConflictRule.STOP : ConflictRule.GIVE_WAY;
            }
        }
        else if (priority1.isPriority() && (priority2.isNone() || priority2.isStop()))
        {
            conflictRules[0] = ConflictRule.PRIORITY;
            conflictRules[1] = priority2.isStop() ? ConflictRule.STOP : ConflictRule.GIVE_WAY;
        }
        else if (priority2.isPriority() && (priority1.isNone() || priority1.isStop()))
        {
            conflictRules[0] = priority1.isStop() ? ConflictRule.STOP : ConflictRule.GIVE_WAY;
            conflictRules[1] = ConflictRule.PRIORITY;
        }
        else if (priority1.isNone() && priority2.isStop())
        {
            conflictRules[0] = ConflictRule.PRIORITY;
            conflictRules[1] = ConflictRule.STOP;
        }
        else if (priority2.isNone() && priority1.isStop())
        {
            conflictRules[0] = ConflictRule.STOP;
            conflictRules[1] = ConflictRule.PRIORITY;
        }
        else
        {
            throw new RuntimeException("Could not sort out priority from priorities " + priority1 + " and " + priority2);
        }
        return conflictRules;
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class Intersection
    {

        /** Fraction on line 1. */
        private final double fraction1;

        /** Fraction on line 1. */
        private final double fraction2;

        /**
         * @param fraction1 fraction on line 1
         * @param fraction2 fraction on lane 2
         */
        Intersection(final double fraction1, final double fraction2)
        {
            this.fraction1 = fraction1;
            this.fraction2 = fraction2;
        }

        /**
         * @param line1 line 1
         * @param line2 line 2
         * @return intersection of lines, {@code null} if not found
         * @throws OTSGeometryException in case of geometry exception
         */
        public static Intersection getIntersection(final OTSLine3D line1, final OTSLine3D line2) throws OTSGeometryException
        {

            double cumul1 = 0.0;
            OTSPoint3D start1 = null;
            OTSPoint3D end1 = line1.get(0);
            for (int i = 0; i < line1.size() - 1; i++)
            {
                start1 = end1;
                end1 = line1.get(i + 1);

                double cumul2 = 0.0;
                OTSPoint3D start2 = null;
                OTSPoint3D end2 = line2.get(0);

                for (int j = 0; j < line2.size() - 1; j++)
                {
                    start2 = end2;
                    end2 = line2.get(j + 1);

                    OTSPoint3D p = OTSPoint3D.intersectionOfLineSegments(start1, end1, start2, end2);
                    if (p != null)
                    {
                        // segments intersect
                        double dx = p.x - start1.x;
                        double dy = p.y - start1.y;
                        double length1 = cumul1 + Math.sqrt(dx * dx + dy * dy);
                        dx = p.x - start2.x;
                        dy = p.y - start2.y;
                        double length2 = cumul2 + Math.sqrt(dx * dx + dy * dy);
                        return new Intersection(length1 / line1.getLengthSI(), length2 / line2.getLengthSI());
                    }

                    double dx = end2.x - start2.x;
                    double dy = end2.y - start2.y;
                    cumul2 += Math.sqrt(dx * dx + dy * dy);
                }

                double dx = end1.x - start1.x;
                double dy = end1.y - start1.y;
                cumul1 += Math.sqrt(dx * dx + dy * dy);
            }

            // no intersection found
            return null;
        }

        /**
         * @return fraction1.
         */
        public double getFraction1()
        {
            return this.fraction1;
        }

        /**
         * @return fraction2.
         */
        public double getFraction2()
        {
            return this.fraction2;
        }

    }

}
