package org.opentrafficsim.road.network.lane.conflict;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

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

import nl.tudelft.simulation.immutablecollections.ImmutableMap;
import nl.tudelft.simulation.language.Throw;
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
// TODO use z-coordinate for intersections of lines
public final class ConflictBuilder
{

    /** Default width generator for conflicts which uses the lane width. */
    public static final DefaultWidthGenerator DEFAULT_WIDTH_GENERATOR = new DefaultWidthGenerator();

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
     * @param widthGenerator width generator
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final OTSNetwork network, final GTUType gtuType,
            final OTSDEVSSimulatorInterface simulator, final WidthGenerator widthGenerator) throws OTSGeometryException
    {
        buildConflicts(network, gtuType, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList());
    }

    /**
     * Build conflicts on network.
     * @param network network
     * @param gtuType gtu type
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param ignoreList lane combinations to ignore
     * @param permittedList lane combinations that are permitted by traffic control
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final OTSNetwork network, final GTUType gtuType,
            final OTSDEVSSimulatorInterface simulator, final WidthGenerator widthGenerator,
            final LaneCombinationList ignoreList, final LaneCombinationList permittedList) throws OTSGeometryException
    {
        // Create list of lanes
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
        buildConflicts(lanes, gtuType, simulator, widthGenerator, ignoreList, permittedList);
    }

    /**
     * Build conflicts on list of lanes.
     * @param lanes lanes
     * @param gtuType gtu type
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final List<Lane> lanes, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws OTSGeometryException
    {
        buildConflicts(lanes, gtuType, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList());
    }

    /**
     * Build conflicts on list of lanes.
     * @param lanes lanes
     * @param gtuType gtu type
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param ignoreList lane combinations to ignore
     * @param permittedList lane combinations that are permitted by traffic control
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final List<Lane> lanes, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final LaneCombinationList ignoreList, final LaneCombinationList permittedList)
            throws OTSGeometryException
    {
        // Loop Lane / GTUDirectionality combinations
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

                    Lane lane2 = lanes.get(j);
                    if (ignoreList.contains(lane1, lane2))
                    {
                        continue;
                    }
                    boolean permitted = permittedList.contains(lane1, lane2);

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
                        // See if conflict needs to be build, and build if so
                        try
                        {
                            buildConflicts(lane1, dir1, down1, up1, lane2, dir2, down2, up2, gtuType, permitted, simulator,
                                    widthGenerator);
                        }
                        catch (NetworkException ne)
                        {
                            throw new RuntimeException("Conflict build with bad combination of types / rules.", ne);
                        }
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
     * @param widthGenerator width generator
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void buildConflicts(final Lane lane1, final GTUDirectionality dir1, final Lane lane2,
            final GTUDirectionality dir2, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws OTSGeometryException
    {
        buildConflicts(lane1, dir1, lane2, dir2, gtuType, simulator, widthGenerator, false);
    }

    /**
     * Build conflict on single lane pair. Connecting lanes are determined.
     * @param lane1 lane 1
     * @param dir1 gtu direction 1
     * @param lane2 lane 2
     * @param dir2 gtu direction 2
     * @param gtuType gtu type
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param permitted conflict permitted by traffic control
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void buildConflicts(final Lane lane1, final GTUDirectionality dir1, final Lane lane2,
            final GTUDirectionality dir2, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final boolean permitted) throws OTSGeometryException
    {
        Map<Lane, GTUDirectionality> down1 = lane1.downstreamLanes(dir1, gtuType);
        Map<Lane, GTUDirectionality> up1 = lane1.upstreamLanes(dir1, gtuType);
        Map<Lane, GTUDirectionality> down2 = lane2.downstreamLanes(dir2, gtuType);
        Map<Lane, GTUDirectionality> up2 = lane2.upstreamLanes(dir2, gtuType);
        try
        {
            buildConflicts(lane1, dir1, down1, up1, lane2, dir2, down2, up2, gtuType, permitted, simulator, widthGenerator);
        }
        catch (NetworkException ne)
        {
            throw new RuntimeException("Conflict build with bad combination of types / rules.", ne);
        }
    }

    /**
     * Build conflicts on single lane pair.
     * @param lane1 lane 1
     * @param dir1 gtu direction 1
     * @param down1 downstream lanes 1
     * @param up1 upstream lanes 1
     * @param lane2 lane 2
     * @param dir2 gtu direction 2
     * @param down2 downstream lane 2
     * @param up2 upstream lanes 2
     * @param gtuType gtu type
     * @param permitted conflict permitted by traffic control
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OTSGeometryException in case of geometry exception
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildConflicts(final Lane lane1, final GTUDirectionality dir1, final Map<Lane, GTUDirectionality> down1,
            final Map<Lane, GTUDirectionality> up1, final Lane lane2, final GTUDirectionality dir2,
            final Map<Lane, GTUDirectionality> down2, final Map<Lane, GTUDirectionality> up2, final GTUType gtuType,
            final boolean permitted, final OTSDEVSSimulatorInterface simulator, final WidthGenerator widthGenerator)
            throws OTSGeometryException, NetworkException
    {

        // TODO quick bounding box check of both lanes, skip if not overlapping

        // Get left and right lines at specified width
        OTSLine3D line1 = lane1.getCenterLine();
        OTSLine3D line2 = lane2.getCenterLine();
        OTSLine3D left1 = line1.offsetLine(widthGenerator.getWidth(lane1, 0.0) / 2, widthGenerator.getWidth(lane1, 1.0) / 2);
        OTSLine3D right1 = line1.offsetLine(-widthGenerator.getWidth(lane1, 0.0) / 2, -widthGenerator.getWidth(lane1, 1.0) / 2);
        OTSLine3D left2 = line2.offsetLine(widthGenerator.getWidth(lane2, 0.0) / 2, widthGenerator.getWidth(lane2, 1.0) / 2);
        OTSLine3D right2 = line2.offsetLine(-widthGenerator.getWidth(lane2, 0.0) / 2, -widthGenerator.getWidth(lane2, 1.0) / 2);

        // Get list of all intersection fractions
        SortedSet<Intersection> intersections = Intersection.getIntersectionList(left1, left2, 0);
        intersections.addAll(Intersection.getIntersectionList(left1, right2, 1));
        intersections.addAll(Intersection.getIntersectionList(right1, left2, 2));
        intersections.addAll(Intersection.getIntersectionList(right1, right2, 3));

        // Create merge
        Iterator<Entry<Lane, GTUDirectionality>> iterator1 = down1.entrySet().iterator();
        Iterator<Entry<Lane, GTUDirectionality>> iterator2 = down2.entrySet().iterator();
        boolean merge = false;
        while (iterator1.hasNext() && !merge)
        {
            Entry<Lane, GTUDirectionality> next1 = iterator1.next();
            while (iterator2.hasNext() && !merge)
            {
                Entry<Lane, GTUDirectionality> next2 = iterator2.next();
                if (next1.equals(next2))
                {
                    // Same downstream lane, so a merge
                    double fraction1 = Double.NaN;
                    double fraction2 = Double.NaN;
                    for (Intersection intersection : intersections)
                    {
                        // Only consider left/right and right/left intersections (others may or may not be at the end)
                        if (intersection.getCombo() == 1 || intersection.getCombo() == 2)
                        {
                            fraction1 = intersection.getFraction1();
                            fraction2 = intersection.getFraction2();
                        }
                    }
                    // Remove all intersections beyond this point, these are the result of line starts/ends matching
                    Iterator<Intersection> iterator = intersections.iterator();
                    while (iterator.hasNext())
                    {
                        if (iterator.next().getFraction1() >= fraction1)
                        {
                            iterator.remove();
                        }
                    }
                    // Build conflict
                    buildMergeConflict(lane1, dir1, fraction1, lane2, dir2, fraction2, gtuType, simulator, widthGenerator,
                            permitted);
                    // Skip loop for efficiency, and do not create multiple merges in case of multiple same downstream lanes
                    merge = true;
                }
            }
        }

        // Create split
        iterator1 = up1.entrySet().iterator();
        iterator2 = up2.entrySet().iterator();
        boolean split = false;
        while (iterator1.hasNext() && !split)
        {
            Entry<Lane, GTUDirectionality> prev1 = iterator1.next();
            while (iterator2.hasNext() && !split)
            {
                Entry<Lane, GTUDirectionality> prev2 = iterator2.next();
                if (prev1.equals(prev2))
                {
                    // Same upstream lane, so a split
                    double fraction1 = Double.NaN;
                    double fraction2 = Double.NaN;
                    for (Intersection intersection : intersections)
                    {
                        // Only consider left/right and right/left intersections (others may or may not be at the start)
                        if (intersection.getCombo() == 1 || intersection.getCombo() == 2)
                        {
                            fraction1 = intersection.getFraction1();
                            fraction2 = intersection.getFraction2();
                            break; // Split so first, not last
                        }
                    }
                    // Remove all intersections up to this point, these are the result of line starts/ends matching
                    Iterator<Intersection> iterator = intersections.iterator();
                    while (iterator.hasNext())
                    {
                        if (iterator.next().getFraction1() <= fraction1)
                        {
                            iterator.remove();
                        }
                        else
                        {
                            // May skip further fraction
                            break;
                        }
                    }
                    // Build conflict
                    buildSplitConflict(lane1, dir1, fraction1, lane2, dir2, fraction2, gtuType, simulator, widthGenerator);
                    // Skip loop for efficiency, and do not create multiple splits in case of multiple same upstream lanes
                    split = true;
                }
            }
        }

        // Create crossings
        boolean[] crossed = new boolean[4];
        Iterator<Intersection> iterator = intersections.iterator();
        double f1Start = Double.NaN;
        double f2Start = Double.NaN;
        while (iterator.hasNext())
        {
            Intersection intersection = iterator.next();
            // First fraction found is start of conflict
            if (Double.isNaN(f1Start))
            {
                f1Start = intersection.getFraction1();
                f2Start = intersection.getFraction2();
            }
            // Flip crossed state of intersecting line combination
            crossed[intersection.getCombo()] = !crossed[intersection.getCombo()];
            // If all crossed or all not crossed, end of conflict
            if ((crossed[0] && crossed[1] && crossed[2] && crossed[3])
                    || (!crossed[0] && !crossed[1] && !crossed[2] && !crossed[3]))
            {
                buildCrossingConflict(lane1, dir1, f1Start, intersection.getFraction1(), lane2, dir2, f2Start,
                        intersection.getFraction2(), gtuType, simulator, widthGenerator, permitted);
                f1Start = Double.NaN;
                f2Start = Double.NaN;
            }
        }

    }

    /**
     * Build a merge conflict.
     * @param lane1 lane 1
     * @param dir1 gtu direction 1
     * @param f1start start fraction 1
     * @param lane2 lane 2
     * @param dir2 gtu direction 2
     * @param f2start start fraction 2
     * @param gtuType gtu type
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param permitted conflict permitted by traffic control
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildMergeConflict(final Lane lane1, final GTUDirectionality dir1, final double f1start,
            final Lane lane2, final GTUDirectionality dir2, final double f2start, final GTUType gtuType,
            final OTSDEVSSimulatorInterface simulator, final WidthGenerator widthGenerator, final boolean permitted)
            throws NetworkException, OTSGeometryException
    {

        // Determine lane end from direction
        double f1end = dir1.isPlus() ? 1.0 : 0.0;
        double f2end = dir2.isPlus() ? 1.0 : 0.0;

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().multiplyBy(f1start);
        Length longitudinalPosition2 = lane2.getLength().multiplyBy(f2start);
        Length length1 = lane1.getLength().multiplyBy(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().multiplyBy(Math.abs(f2end - f2start));

        // Get geometries
        OTSLine3D geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        OTSLine3D geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Determine conflict rule
        ConflictRule conflictRule;
        if (lane1.getParentLink().getPriority().isBusStop() || lane2.getParentLink().getPriority().isBusStop())
        {
            Throw.when(lane1.getParentLink().getPriority().isBusStop() && lane2.getParentLink().getPriority().isBusStop(),
                    IllegalArgumentException.class, "Merge conflict between two links with bus stop priority not supported.");
            conflictRule = new BusStopConflictRule();
        }
        else
        {
            conflictRule = new DefaultConflictRule();
        }

        // Make conflict
        Conflict.generateConflictPair(ConflictType.MERGE, conflictRule, permitted, lane1, longitudinalPosition1, length1, dir1,
                geometry1, gtuType, lane2, longitudinalPosition2, length2, dir2, geometry2, gtuType, simulator);
    }

    /**
     * Build a split conflict.
     * @param lane1 lane 1
     * @param dir1 gtu direction 1
     * @param f1end end fraction 1
     * @param lane2 lane 2
     * @param dir2 gtu direction 2
     * @param f2end end fraction 2
     * @param gtuType gtu type
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildSplitConflict(final Lane lane1, final GTUDirectionality dir1, final double f1end, final Lane lane2,
            final GTUDirectionality dir2, final double f2end, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws NetworkException, OTSGeometryException
    {

        // Determine lane start from direction
        double f1start = dir1.isPlus() ? 0.0 : 1.0;
        double f2start = dir2.isPlus() ? 0.0 : 1.0;

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().multiplyBy(f1start);
        Length longitudinalPosition2 = lane2.getLength().multiplyBy(f2start);
        Length length1 = lane1.getLength().multiplyBy(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().multiplyBy(Math.abs(f2end - f2start));

        // Get geometries
        OTSLine3D geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        OTSLine3D geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Make conflict
        Conflict.generateConflictPair(ConflictType.SPLIT, new SplitConflictRule(), false, lane1, longitudinalPosition1, length1,
                dir1, geometry1, gtuType, lane2, longitudinalPosition2, length2, dir2, geometry2, gtuType, simulator);
    }

    /**
     * Build a crossing conflict.
     * @param lane1 lane 1
     * @param dir1 gtu direction 1
     * @param f1start start fraction 1
     * @param f1end end fraction 1
     * @param lane2 lane 2
     * @param dir2 gtu direction 2
     * @param f2start start fraction 2
     * @param f2end end fraction 2
     * @param gtuType gtu type
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param permitted conflict permitted by traffic control
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildCrossingConflict(final Lane lane1, final GTUDirectionality dir1, final double f1start,
            final double f1end, final Lane lane2, final GTUDirectionality dir2, final double f2start, final double f2end,
            final GTUType gtuType, final OTSDEVSSimulatorInterface simulator, final WidthGenerator widthGenerator,
            final boolean permitted) throws NetworkException, OTSGeometryException
    {

        // Fractions may be in opposite direction, for the start location this needs to be correct
        // Note: for geometry (real order, not considering direction) and length (absolute value) this does not matter
        double f1startDirected;
        double f2startDirected;
        if ((dir1.isPlus() && f1end < f1start) || (dir1.isMinus() && f1end > f1start))
        {
            f1startDirected = f1end;
        }
        else
        {
            f1startDirected = f1start;
        }
        if ((dir2.isPlus() && f2end < f2start) || (dir2.isMinus() && f2end > f2start))
        {
            f2startDirected = f2end;
        }
        else
        {
            f2startDirected = f2start;
        }

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().multiplyBy(f1startDirected);
        Length longitudinalPosition2 = lane2.getLength().multiplyBy(f2startDirected);
        Length length1 = lane1.getLength().multiplyBy(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().multiplyBy(Math.abs(f2end - f2start));

        // Get geometries
        OTSLine3D geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        OTSLine3D geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Make conflict
        Conflict.generateConflictPair(ConflictType.CROSSING, new DefaultConflictRule(), permitted, lane1, longitudinalPosition1,
                length1, dir1, geometry1, gtuType, lane2, longitudinalPosition2, length2, dir2, geometry2, gtuType, simulator);
    }

    /**
     * Creates geometry for conflict.
     * @param lane lane
     * @param fStart longitudinal fraction of start
     * @param fEnd longitudinal fraction of end
     * @param widthGenerator width generator
     * @return geometry for conflict
     * @throws OTSGeometryException in case of geometry exception
     */
    private static OTSLine3D getGeometry(final Lane lane, final double fStart, final double fEnd,
            final WidthGenerator widthGenerator) throws OTSGeometryException
    {
        // extractFractional needs ordered fractions, irrespective of driving direction
        double f1;
        double f2;
        if (fEnd > fStart)
        {
            f1 = fStart;
            f2 = fEnd;
        }
        else
        {
            f1 = fEnd;
            f2 = fStart;
        }
        OTSLine3D centerLine = lane.getCenterLine().extractFractional(f1, f2);
        OTSLine3D left = centerLine.offsetLine(widthGenerator.getWidth(lane, f1) / 2, widthGenerator.getWidth(lane, f2) / 2);
        OTSLine3D right =
                centerLine.offsetLine(-widthGenerator.getWidth(lane, f1) / 2, -widthGenerator.getWidth(lane, f2) / 2).reverse();
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
    private static ConflictPriority[] getConflictRules(final Lane lane1, final Length longitudinalPosition1, final Lane lane2,
            final Length longitudinalPosition2, final ConflictType conflictType) throws OTSGeometryException
    {

        ConflictPriority[] conflictRules = new ConflictPriority[2];
        Priority priority1 = lane1.getParentLink().getPriority();
        Priority priority2 = lane2.getParentLink().getPriority();
        if (conflictType.equals(ConflictType.SPLIT))
        {
            conflictRules[0] = ConflictPriority.SPLIT;
            conflictRules[1] = ConflictPriority.SPLIT;
        }
        else if (priority1.isAllStop() && priority2.isAllStop())
        {
            conflictRules[0] = ConflictPriority.ALL_STOP;
            conflictRules[1] = ConflictPriority.ALL_STOP;
        }
        else if (priority1.equals(priority2))
        {
            // Based on right- or left-hand traffic
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
                conflictRules[0] = priority1.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
                conflictRules[1] = ConflictPriority.PRIORITY;
            }
            else
            {
                // 1 comes from the right
                conflictRules[0] = ConflictPriority.PRIORITY;
                conflictRules[1] = priority2.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
            }
        }
        else if (priority1.isPriority() && (priority2.isNone() || priority2.isStop()))
        {
            conflictRules[0] = ConflictPriority.PRIORITY;
            conflictRules[1] = priority2.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
        }
        else if (priority2.isPriority() && (priority1.isNone() || priority1.isStop()))
        {
            conflictRules[0] = priority1.isStop() ? ConflictPriority.STOP : ConflictPriority.GIVE_WAY;
            conflictRules[1] = ConflictPriority.PRIORITY;
        }
        else if (priority1.isNone() && priority2.isStop())
        {
            conflictRules[0] = ConflictPriority.PRIORITY;
            conflictRules[1] = ConflictPriority.STOP;
        }
        else if (priority2.isNone() && priority1.isStop())
        {
            conflictRules[0] = ConflictPriority.STOP;
            conflictRules[1] = ConflictPriority.PRIORITY;
        }
        else
        {
            throw new RuntimeException("Could not sort out priority from priorities " + priority1 + " and " + priority2);
        }
        return conflictRules;
    }

    /**
     * Intersection holds two fractions where two lines have crossed. There is also a combo to identify which lines have been
     * used to find the intersection.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class Intersection implements Comparable<Intersection>
    {

        /** Fraction on lane 1. */
        private final double fraction1;

        /** Fraction on lane 2. */
        private final double fraction2;

        /** Edge combination number. */
        private final int combo;

        /**
         * @param fraction1 fraction on lane 1
         * @param fraction2 fraction on lane 1
         * @param combo edge combination number
         */
        Intersection(final double fraction1, final double fraction2, final int combo)
        {
            this.fraction1 = fraction1;
            this.fraction2 = fraction2;
            this.combo = combo;
        }

        /**
         * @return fraction1.
         */
        public final double getFraction1()
        {
            return this.fraction1;
        }

        /**
         * @return fraction2.
         */
        public final double getFraction2()
        {
            return this.fraction2;
        }

        /**
         * @return combo.
         */
        public final int getCombo()
        {
            return this.combo;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final Intersection o)
        {
            int out = Double.compare(this.fraction1, o.fraction1);
            if (out != 0)
            {
                return out;
            }
            out = Double.compare(this.fraction2, o.fraction2);
            if (out != 0)
            {
                return out;
            }
            return Integer.compare(this.combo, o.combo);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.combo;
            long temp;
            temp = Double.doubleToLongBits(this.fraction1);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(this.fraction2);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            Intersection other = (Intersection) obj;
            if (this.combo != other.combo)
            {
                return false;
            }
            if (Double.doubleToLongBits(this.fraction1) != Double.doubleToLongBits(other.fraction1))
            {
                return false;
            }
            if (Double.doubleToLongBits(this.fraction2) != Double.doubleToLongBits(other.fraction2))
            {
                return false;
            }
            return true;
        }

        /**
         * Returns a set of intersections, sorted by the fraction on line 1.
         * @param line1 line 1
         * @param line2 line 2
         * @param combo edge combination number
         * @return set of intersections, sorted by the fraction on line 1
         * @throws OTSGeometryException in case of geometry exception
         */
        public static SortedSet<Intersection> getIntersectionList(final OTSLine3D line1, final OTSLine3D line2, final int combo)
                throws OTSGeometryException
        {
            SortedSet<Intersection> out = new TreeSet<>();

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
                        // Segments intersect
                        double dx = p.x - start1.x;
                        double dy = p.y - start1.y;
                        double length1 = cumul1 + Math.sqrt(dx * dx + dy * dy);
                        dx = p.x - start2.x;
                        dy = p.y - start2.y;
                        double length2 = cumul2 + Math.sqrt(dx * dx + dy * dy);
                        out.add(new Intersection(length1 / line1.getLengthSI(), length2 / line2.getLengthSI(), combo));
                    }

                    double dx = end2.x - start2.x;
                    double dy = end2.y - start2.y;
                    cumul2 += Math.sqrt(dx * dx + dy * dy);
                }

                double dx = end1.x - start1.x;
                double dy = end1.y - start1.y;
                cumul1 += Math.sqrt(dx * dx + dy * dy);
            }

            return out;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "Intersection [fraction1=" + this.fraction1 + ", fraction2=" + this.fraction2 + ", combo=" + this.combo
                    + "]";
        }

    }

    /**
     * Generator for width.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface WidthGenerator
    {

        /**
         * Returns the begin width of this lane.
         * @param lane Lane; lane
         * @param fraction double; fraction
         * @return begin width of this lane
         */
        default double getWidth(final Lane lane, final double fraction)
        {
            return lane.getWidth(fraction).si;
        }

    }

    /**
     * Default width generator using the lane width.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class DefaultWidthGenerator implements WidthGenerator
    {
        //
    }

    /**
     * Generator with fixed width.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class FixedWidthGenerator implements WidthGenerator
    {

        /** Fixed width. */
        private final double width;

        /**
         * Constructor with width.
         * @param width double; width
         */
        public FixedWidthGenerator(final Length width)
        {
            this.width = width.si;
        }

        /** {@inheritDoc} */
        @Override
        public final double getWidth(final Lane lane, final double fraction)
        {
            return this.width;
        }

    }

    /**
     * Generator with width factor on actual lane width.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class RelativeWidthGenerator implements WidthGenerator
    {

        /** Width factor. */
        private final double factor;

        /**
         * Constructor with width factor.
         * @param factor double; width factor
         */
        public RelativeWidthGenerator(final double factor)
        {
            this.factor = factor;
        }

        /** {@inheritDoc} */
        @Override
        public final double getWidth(final Lane lane, final double fraction)
        {
            return lane.getWidth(fraction).si * this.factor;
        }

    }

}
