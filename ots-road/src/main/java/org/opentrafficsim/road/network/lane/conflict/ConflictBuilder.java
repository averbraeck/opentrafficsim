package org.opentrafficsim.road.network.lane.conflict;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.pmw.tinylog.Level;

/**
 * Conflict builder allows automatic generation of conflicts. This happens based on the geometry of lanes. Parallel execution
 * allows this algorithm to run faster. There are two parallel implementations:
 * <ul>
 * <li>Small; between two lanes.</li>
 * <li>Big; between one particular lane, and all lanes further in a list (i.e. similar to a triangular matrix procedure).</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @see <a href="https://opentrafficsim.org/manual/99-appendices/conflict-areas/">Generation of conflics</a>
 */
// TODO use z-coordinate for intersections of lines
// TODO use remove big parallel type, and use fibers for small tasks.
public final class ConflictBuilder
{
    /** number of merge onflicts. */
    private static AtomicInteger numberMergeConflicts = new AtomicInteger(0);

    /** number of split onflicts. */
    private static AtomicInteger numberSplitConflicts = new AtomicInteger(0);

    /** number of cross onflicts. */
    private static AtomicInteger numberCrossConflicts = new AtomicInteger(0);

    /** Default width generator for conflicts which uses 80% of the lane width. */
    public static final WidthGenerator DEFAULT_WIDTH_GENERATOR = new RelativeWidthGenerator(0.8);

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
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflicts(final RoadNetwork network, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws OtsGeometryException
    {
        buildConflicts(network, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList());
    }

    /**
     * Build conflicts on network.
     * @param network network
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param ignoreList lane combinations to ignore
     * @param permittedList lane combinations that are permitted by traffic control
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflicts(final RoadNetwork network, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final LaneCombinationList ignoreList, final LaneCombinationList permittedList)
            throws OtsGeometryException
    {
        buildConflicts(getLanes(network), simulator, widthGenerator, ignoreList, permittedList, null);
    }

    /**
     * Returns all the lanes in the network.
     * @param network network.
     * @return list if all lanes.
     */
    private static List<Lane> getLanes(final RoadNetwork network)
    {
        ImmutableMap<String, Link> links = network.getLinkMap();
        List<Lane> lanes = new ArrayList<>();
        for (String linkId : links.keySet())
        {
            Link link = links.get(linkId);
            if (link instanceof CrossSectionLink)
            {
                for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                {
                    if (element instanceof Lane lane && !(element instanceof Shoulder))
                    {
                        lanes.add((Lane) element);
                    }
                }
            }
        }
        return lanes;
    }

    /**
     * Build conflicts on list of lanes.
     * @param lanes lanes
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflicts(final List<Lane> lanes, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws OtsGeometryException
    {
        buildConflicts(lanes, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList(), null);
    }

    /**
     * Build conflicts on list of lanes.
     * @param lanes list of Lanes
     * @param simulator the simulator
     * @param widthGenerator the width generator
     * @param ignoreList lane combinations to ignore
     * @param permittedList lane combinations that are permitted by traffic control
     * @param conflictId identification of the conflict (null value permitted)
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflicts(final List<Lane> lanes, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final LaneCombinationList ignoreList, final LaneCombinationList permittedList,
            final String conflictId) throws OtsGeometryException
    {
        long totalCombinations = ((long) lanes.size()) * ((long) lanes.size() - 1) / 2;
        simulator.getLogger().always().trace("GENERATING CONFLICTS (NON-PARALLEL MODE). {} COMBINATIONS", totalCombinations);
        long lastReported = 0;
        Map<Lane, OtsLine2d> leftEdges = new LinkedHashMap<>();
        Map<Lane, OtsLine2d> rightEdges = new LinkedHashMap<>();

        for (int i = 0; i < lanes.size(); i++)
        {
            long combinationsDone = totalCombinations - ((long) (lanes.size() - i)) * ((long) (lanes.size() - i)) / 2;
            if (combinationsDone / 100000000 > lastReported)
            {
                simulator.getLogger().always()
                        .debug(String.format(
                                "generating conflicts at %.0f%% (generated %d merge conflicts, %d split "
                                        + "conflicts, %d crossing conflicts)",
                                100.0 * combinationsDone / totalCombinations, numberMergeConflicts.get(),
                                numberSplitConflicts.get(), numberCrossConflicts.get()));
                lastReported = combinationsDone / 100000000;
            }
            Lane lane1 = lanes.get(i);
            Set<Lane> down1 = lane1.nextLanes(null);
            Set<Lane> up1 = lane1.prevLanes(null);

            for (int j = i + 1; j < lanes.size(); j++)
            {
                Lane lane2 = lanes.get(j);
                if (ignoreList.contains(lane1, lane2))
                {
                    continue;
                }
                boolean permitted = permittedList.contains(lane1, lane2);

                Set<Lane> down2 = lane2.nextLanes(null);
                Set<Lane> up2 = lane2.prevLanes(null);
                // See if conflict needs to be build, and build if so
                try
                {
                    buildConflicts(lane1, down1, up1, lane2, down2, up2, permitted, simulator, widthGenerator, leftEdges,
                            rightEdges, true, conflictId);
                }
                catch (NetworkException ne)
                {
                    throw new RuntimeException("Conflict build with bad combination of types / rules.", ne);
                }
            }
        }
        simulator.getLogger().always()
                .trace(String.format(
                        "generating conflicts complete (generated %d merge conflicts, %d split "
                                + "conflicts, %d crossing conflicts)",
                        numberMergeConflicts.get(), numberSplitConflicts.get(), numberCrossConflicts.get()));
    }

    /**
     * Build conflict on single lane pair. Connecting lanes are determined.
     * @param lane1 lane 1
     * @param lane2 lane 2
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OtsGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void buildConflicts(final Lane lane1, final Lane lane2, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws OtsGeometryException
    {
        buildConflicts(lane1, lane2, simulator, widthGenerator, false);
    }

    /**
     * Build conflict on single lane pair. Connecting lanes are determined.
     * @param lane1 lane 1
     * @param lane2 lane 2
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param permitted conflict permitted by traffic control
     * @throws OtsGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void buildConflicts(final Lane lane1, final Lane lane2, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final boolean permitted) throws OtsGeometryException
    {
        Set<Lane> down1 = lane1.nextLanes(null);
        Set<Lane> up1 = lane1.prevLanes(null);
        Set<Lane> down2 = lane2.nextLanes(null);
        Set<Lane> up2 = lane2.prevLanes(null);
        try
        {
            buildConflicts(lane1, down1, up1, lane2, down2, up2, permitted, simulator, widthGenerator, new LinkedHashMap<>(),
                    new LinkedHashMap<>(), true, null);
        }
        catch (NetworkException ne)
        {
            throw new RuntimeException("Conflict build with bad combination of types / rules.", ne);
        }
    }

    /**
     * Build conflicts on single lane pair.
     * @param lane1 lane 1
     * @param down1 downstream lanes 1
     * @param up1 upstream lanes 1
     * @param lane2 lane 2
     * @param down2 downstream lane 2
     * @param up2 upstream lanes 2
     * @param permitted conflict permitted by traffic control
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param leftEdges cache of left edge lines
     * @param rightEdges cache of right edge lines
     * @param intersectionCheck indicate whether we have to do a contour intersection check still
     * @param conflictId identification of the conflict (may be null)
     * @throws OtsGeometryException in case of geometry exception
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings({"checkstyle:parameternumber", "checkstyle:methodlength"})
    static void buildConflicts(final Lane lane1, final Set<Lane> down1, final Set<Lane> up1, final Lane lane2,
            final Set<Lane> down2, final Set<Lane> up2, final boolean permitted, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final Map<Lane, OtsLine2d> leftEdges, final Map<Lane, OtsLine2d> rightEdges,
            final boolean intersectionCheck, final String conflictId) throws OtsGeometryException, NetworkException
    {
        // Quick contour check, skip if not overlapping -- Don't repeat if it has taken place
        if (intersectionCheck)
        {
            if (!lane1.getContour().intersects(lane2.getContour()))
            {
                return;
            }
        }

        // TODO: we cache, but the width generator may be different

        String paddedConflictId = null == conflictId ? "" : (" in conflict group " + conflictId);
        // Get left and right lines at specified width
        OtsLine2d left1;
        OtsLine2d right1;
        synchronized (lane1)
        {
            left1 = leftEdges.get(lane1);
            right1 = rightEdges.get(lane1);
            OtsLine2d line1 = lane1.getCenterLine();
            if (null == left1)
            {
                left1 = line1.offsetLine(widthGenerator.getWidth(lane1, 0.0) / 2, widthGenerator.getWidth(lane1, 1.0) / 2);
                leftEdges.put(lane1, left1);
            }
            if (null == right1)
            {
                right1 = line1.offsetLine(-widthGenerator.getWidth(lane1, 0.0) / 2, -widthGenerator.getWidth(lane1, 1.0) / 2);
                rightEdges.put(lane1, right1);
            }
        }

        OtsLine2d left2;
        OtsLine2d right2;
        synchronized (lane2)
        {
            left2 = leftEdges.get(lane2);
            right2 = rightEdges.get(lane2);
            OtsLine2d line2 = lane2.getCenterLine();
            if (null == left2)
            {
                left2 = line2.offsetLine(widthGenerator.getWidth(lane2, 0.0) / 2, widthGenerator.getWidth(lane2, 1.0) / 2);
                leftEdges.put(lane2, left2);
            }
            if (null == right2)
            {
                right2 = line2.offsetLine(-widthGenerator.getWidth(lane2, 0.0) / 2, -widthGenerator.getWidth(lane2, 1.0) / 2);
                rightEdges.put(lane2, right2);
            }
        }

        // Get list of all intersection fractions
        SortedSet<Intersection> intersections = Intersection.getIntersectionList(left1, left2, 0);
        intersections.addAll(Intersection.getIntersectionList(left1, right2, 1));
        intersections.addAll(Intersection.getIntersectionList(right1, left2, 2));
        intersections.addAll(Intersection.getIntersectionList(right1, right2, 3));

        // Create merge
        Iterator<Lane> iterator1 = down1.iterator();
        Iterator<Lane> iterator2 = down2.iterator();
        boolean merge = false;
        while (iterator1.hasNext() && !merge)
        {
            Lane next1 = iterator1.next();
            while (iterator2.hasNext() && !merge)
            {
                Lane next2 = iterator2.next();
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
                    if (Double.isNaN(fraction1))
                    {
                        simulator.getLogger().always().info("Fixing fractions of merge conflict{}", paddedConflictId);
                        fraction1 = 0;
                        fraction2 = 0;
                    }
                    // Build conflict
                    buildMergeConflict(lane1, fraction1, lane2, fraction2, simulator, widthGenerator, permitted);
                    // Skip loop for efficiency, and do not create multiple merges in case of multiple same downstream lanes
                    merge = true;
                }
            }
        }

        // Create split
        iterator1 = up1.iterator();
        iterator2 = up2.iterator();
        boolean split = false;
        while (iterator1.hasNext() && !split)
        {
            Lane prev1 = iterator1.next();
            while (iterator2.hasNext() && !split)
            {
                Lane prev2 = iterator2.next();
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
                    if (Double.isNaN(fraction1))
                    {
                        simulator.getLogger().always().info("Fixing fractions of split conflict{}", paddedConflictId);
                        fraction1 = 1;
                        fraction2 = 1;
                    }
                    // Build conflict
                    buildSplitConflict(lane1, fraction1, lane2, fraction2, simulator, widthGenerator);
                    // Skip loop for efficiency, and do not create multiple splits in case of multiple same upstream lanes
                    split = true;
                }
            }
        }

        // Create crossings
        if (!lane1.getLink().equals(lane2.getLink())) // tight inner-curves with dedicated Bezier ignored
        {
            boolean[] crossed = new boolean[4];
            Iterator<Intersection> iterator = intersections.iterator();
            double f1Start = Double.NaN;
            double f2Start = Double.NaN;
            double f2End = Double.NaN;
            while (iterator.hasNext())
            {
                Intersection intersection = iterator.next();
                // First fraction found is start of conflict
                if (Double.isNaN(f1Start))
                {
                    f1Start = intersection.getFraction1();
                }
                f2Start = Double.isNaN(f2Start) ? intersection.getFraction2() : Math.min(f2Start, intersection.getFraction2());
                f2End = Double.isNaN(f2End) ? intersection.getFraction2() : Math.max(f2End, intersection.getFraction2());
                // Flip crossed state of intersecting line combination
                crossed[intersection.getCombo()] = !crossed[intersection.getCombo()];
                // If all crossed or all not crossed, end of conflict
                if ((crossed[0] && crossed[1] && crossed[2] && crossed[3])
                        || (!crossed[0] && !crossed[1] && !crossed[2] && !crossed[3]))
                {
                    if (Double.isNaN(f1Start) || Double.isNaN(f2Start) || Double.isNaN(f2End))
                    {
                        simulator.getLogger().always().warn("NOT YET Fixing fractions of crossing conflict{}",
                                paddedConflictId);
                    }
                    buildCrossingConflict(lane1, f1Start, intersection.getFraction1(), lane2, f2Start, f2End, simulator,
                            widthGenerator, permitted);
                    f1Start = Double.NaN;
                    f2Start = Double.NaN;
                    f2End = Double.NaN;
                }
            }
        }

    }

    /**
     * Build a merge conflict.
     * @param lane1 lane 1
     * @param f1start start fraction 1
     * @param lane2 lane 2
     * @param f2start start fraction 2
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param permitted conflict permitted by traffic control
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OtsGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildMergeConflict(final Lane lane1, final double f1start, final Lane lane2, final double f2start,
            final OtsSimulatorInterface simulator, final WidthGenerator widthGenerator, final boolean permitted)
            throws NetworkException, OtsGeometryException
    {

        // Determine lane end from direction
        double f1end = 1.0;
        double f2end = 1.0;

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().times(f1start);
        Length longitudinalPosition2 = lane2.getLength().times(f2start);
        Length length1 = lane1.getLength().times(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().times(Math.abs(f2end - f2start));

        // Get geometries
        Polygon2d geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        Polygon2d geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Determine conflict rule
        ConflictRule conflictRule;
        if (lane1.getLink().getPriority().isBusStop() || lane2.getLink().getPriority().isBusStop())
        {
            Throw.when(lane1.getLink().getPriority().isBusStop() && lane2.getLink().getPriority().isBusStop(),
                    IllegalArgumentException.class, "Merge conflict between two links with bus stop priority not supported.");
            // TODO: handle bus priority on the model side
            conflictRule = new BusStopConflictRule(simulator, DefaultsNl.BUS);
        }
        else
        {
            conflictRule = new DefaultConflictRule();
        }

        // Make conflict
        Conflict.generateConflictPair(ConflictType.MERGE, conflictRule, permitted, lane1, longitudinalPosition1, length1,
                geometry1, lane2, longitudinalPosition2, length2, geometry2, simulator);

        numberMergeConflicts.incrementAndGet();
    }

    /**
     * Build a split conflict.
     * @param lane1 lane 1
     * @param f1end end fraction 1
     * @param lane2 lane 2
     * @param f2end end fraction 2
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OtsGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildSplitConflict(final Lane lane1, final double f1end, final Lane lane2, final double f2end,
            final OtsSimulatorInterface simulator, final WidthGenerator widthGenerator)
            throws NetworkException, OtsGeometryException
    {

        // Determine lane start from direction
        double f1start = 0.0;
        double f2start = 0.0;

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().times(f1start);
        Length longitudinalPosition2 = lane2.getLength().times(f2start);
        Length length1 = lane1.getLength().times(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().times(Math.abs(f2end - f2start));

        // Get geometries
        Polygon2d geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        Polygon2d geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Make conflict
        Conflict.generateConflictPair(ConflictType.SPLIT, new SplitConflictRule(), false, lane1, longitudinalPosition1, length1,
                geometry1, lane2, longitudinalPosition2, length2, geometry2, simulator);

        numberSplitConflicts.incrementAndGet();
    }

    /**
     * Build a crossing conflict.
     * @param lane1 lane 1
     * @param f1start start fraction 1
     * @param f1end end fraction 1
     * @param lane2 lane 2
     * @param f2start start fraction 2
     * @param f2end end fraction 2
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param permitted conflict permitted by traffic control
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OtsGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildCrossingConflict(final Lane lane1, final double f1start, final double f1end, final Lane lane2,
            final double f2start, final double f2end, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final boolean permitted) throws NetworkException, OtsGeometryException
    {

        // Fractions may be in opposite direction, for the start location this needs to be correct
        // Note: for geometry (real order, not considering direction) and length (absolute value) this does not matter
        double f1startDirected;
        double f2startDirected;
        if (f1end < f1start)
        {
            f1startDirected = f1end;
        }
        else
        {
            f1startDirected = f1start;
        }
        if (f2end < f2start)
        {
            f2startDirected = f2end;
        }
        else
        {
            f2startDirected = f2start;
        }

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().times(f1startDirected);
        Length longitudinalPosition2 = lane2.getLength().times(f2startDirected);
        Length length1 = lane1.getLength().times(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().times(Math.abs(f2end - f2start));

        // Get geometries
        Polygon2d geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        Polygon2d geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Determine conflict rule
        ConflictRule conflictRule;
        if (lane1.getLink().getPriority().isBusStop() || lane2.getLink().getPriority().isBusStop())
        {
            Throw.when(lane1.getLink().getPriority().isBusStop() && lane2.getLink().getPriority().isBusStop(),
                    IllegalArgumentException.class, "Merge conflict between two links with bus stop priority not supported.");
            // TODO: handle bus priority on the model side
            conflictRule = new BusStopConflictRule(simulator, DefaultsNl.BUS);
        }
        else
        {
            conflictRule = new DefaultConflictRule();
        }

        // Make conflict
        Conflict.generateConflictPair(ConflictType.CROSSING, conflictRule, permitted, lane1, longitudinalPosition1, length1,
                geometry1, lane2, longitudinalPosition2, length2, geometry2, simulator);

        numberCrossConflicts.incrementAndGet();
    }

    /**
     * Creates geometry for conflict.
     * @param lane lane
     * @param fStart longitudinal fraction of start
     * @param fEnd longitudinal fraction of end
     * @param widthGenerator width generator
     * @return geometry for conflict
     * @throws OtsGeometryException in case of geometry exception
     */
    private static Polygon2d getGeometry(final Lane lane, final double fStart, final double fEnd,
            final WidthGenerator widthGenerator) throws OtsGeometryException
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
        if (Math.abs(f1 - f2) < 1E-8)
        {
            lane.getLink().getSimulator().getLogger().always()
                    .debug("f1 (" + f1 + ") equals f2 (" + f2 + "); problematic lane is " + lane.toString());
            // Fix up
            if (f1 > 0)
            {
                f1 = f1 - f1 / 1000;
            }
            else
            {
                f2 = f2 + f2 / 1000;
            }
        }
        OtsLine2d centerLine = lane.getCenterLine().extractFractional(f1, f2);
        OtsLine2d left = centerLine.offsetLine(widthGenerator.getWidth(lane, f1) / 2, widthGenerator.getWidth(lane, f2) / 2);
        OtsLine2d right =
                centerLine.offsetLine(-widthGenerator.getWidth(lane, f1) / 2, -widthGenerator.getWidth(lane, f2) / 2).reverse();
        Point2d[] points = new Point2d[left.size() + right.size()];
        System.arraycopy(left.getPoints(), 0, points, 0, left.size());
        System.arraycopy(right.getPoints(), 0, points, left.size(), right.size());
        return new Polygon2d(points);
    }

    /**
     * Intersection holds two fractions where two lines have crossed. There is also a combo to identify which lines have been
     * used to find the intersection.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
         * @throws OtsGeometryException in case of geometry exception
         */
        public static SortedSet<Intersection> getIntersectionList(final OtsLine2d line1, final OtsLine2d line2, final int combo)
                throws OtsGeometryException
        {
            SortedSet<Intersection> out = new TreeSet<>();
            double cumul1 = 0.0;
            Point2d start1 = null;
            Point2d end1 = line1.get(0);
            for (int i = 0; i < line1.size() - 1; i++)
            {
                start1 = end1;
                end1 = line1.get(i + 1);

                double cumul2 = 0.0;
                Point2d start2 = null;
                Point2d end2 = line2.get(0);

                for (int j = 0; j < line2.size() - 1; j++)
                {
                    start2 = end2;
                    end2 = line2.get(j + 1);

                    Point2d p = Point2d.intersectionOfLineSegments(start1, end1, start2, end2);
                    if (p != null)
                    {
                        // Segments intersect
                        double dx = p.x - start1.x;
                        double dy = p.y - start1.y;
                        double length1 = cumul1 + Math.hypot(dx, dy);
                        dx = p.x - start2.x;
                        dy = p.y - start2.y;
                        double length2 = cumul2 + Math.hypot(dx, dy);
                        out.add(new Intersection(length1 / line1.getLength().si, length2 / line2.getLength().si, combo));
                    }

                    double dx = end2.x - start2.x;
                    double dy = end2.y - start2.y;
                    cumul2 += Math.hypot(dx, dy);
                }

                double dx = end1.x - start1.x;
                double dy = end1.y - start1.y;
                cumul1 += Math.hypot(dx, dy);
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
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface WidthGenerator
    {

        /**
         * Returns the begin width of this lane.
         * @param lane lane
         * @param fraction fraction
         * @return begin width of this lane
         */
        double getWidth(Lane lane, double fraction);

    }

    /**
     * Generator with fixed width.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class FixedWidthGenerator implements WidthGenerator
    {

        /** Fixed width. */
        private final double width;

        /**
         * Constructor with width.
         * @param width width
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

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "FixedWidthGenerator [width=" + this.width + "]";
        }

    }

    /**
     * Generator with width factor on actual lane width.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class RelativeWidthGenerator implements WidthGenerator
    {

        /** Width factor. */
        private final double factor;

        /**
         * Constructor with width factor.
         * @param factor width factor
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

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RelativeWidthGenerator [factor=" + this.factor + "]";
        }

    }

    /* ******************************************************************************************************************** */
    /* ******************************************************************************************************************** */
    /* ******************************************************************************************************************** */
    /* ********************************************* PARALLEL IMPLEMENTATION ********************************************** */
    /* ******************************************************************************************************************** */
    /* ******************************************************************************************************************** */
    /* ******************************************************************************************************************** */

    /**
     * Build conflicts on network; parallel implementation.
     * @param network network
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflictsParallel(final RoadNetwork network, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws OtsGeometryException
    {
        buildConflictsParallel(network, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList());
    }

    /**
     * Build conflicts on network; parallel implementation.
     * @param network network
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param ignoreList lane combinations to ignore
     * @param permittedList lane combinations that are permitted by traffic control
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflictsParallel(final RoadNetwork network, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final LaneCombinationList ignoreList, final LaneCombinationList permittedList)
            throws OtsGeometryException
    {
        buildConflictsParallelBig(getLanes(network), simulator, widthGenerator, ignoreList, permittedList);
    }

    /**
     * Build conflicts on list of lanes; parallel implementation.
     * @param lanes lanes
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflictsParallel(final List<Lane> lanes, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator) throws OtsGeometryException
    {
        buildConflictsParallelBig(lanes, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList());
    }

    /**
     * Build conflicts on list of lanes; parallel implementation. Small jobs.
     * @param lanes list of Lanes
     * @param simulator the simulator
     * @param widthGenerator the width generator
     * @param ignoreList lane combinations to ignore
     * @param permittedList lane combinations that are permitted by traffic control
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflictsParallelSmall(final List<Lane> lanes, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final LaneCombinationList ignoreList, final LaneCombinationList permittedList)
            throws OtsGeometryException
    {
        long totalCombinations = ((long) lanes.size()) * ((long) lanes.size() - 1) / 2;
        System.out.println("PARALLEL GENERATING OF CONFLICTS (SMALL JOBS). " + totalCombinations + " COMBINATIONS");
        long lastReported = 0;
        Map<Lane, OtsLine2d> leftEdges = new LinkedHashMap<>();
        Map<Lane, OtsLine2d> rightEdges = new LinkedHashMap<>();

        // make a threadpool and execute buildConflicts for all records
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("USING " + cores + " CORES");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
        AtomicInteger numberOfJobs = new AtomicInteger(0);
        final int maxqueue = 2 * cores;

        for (int i = 0; i < lanes.size(); i++)
        {
            long combinationsDone = totalCombinations - ((long) (lanes.size() - i)) * ((long) (lanes.size() - i - 1)) / 2;
            if (combinationsDone / 100000000 > lastReported)
            {
                simulator.getLogger().always()
                        .debug(String.format(
                                "generating conflicts at %.0f%% (generated %d merge conflicts, %d split "
                                        + "conflicts, %d crossing conflicts)",
                                100.0 * combinationsDone / totalCombinations, numberMergeConflicts.get(),
                                numberSplitConflicts.get(), numberCrossConflicts.get()));
                lastReported = combinationsDone / 100000000;
            }
            Lane lane1 = lanes.get(i);
            Set<Lane> down1 = lane1.nextLanes(null);
            Set<Lane> up1 = lane1.prevLanes(null);

            for (int j = i + 1; j < lanes.size(); j++)
            {
                Lane lane2 = lanes.get(j);
                if (ignoreList.contains(lane1, lane2))
                {
                    continue;
                }
                // Quick contour check, skip if non-overlapping envelopes
                try
                {
                    if (!lane1.getContour().intersects(lane2.getContour()))
                    {
                        continue;
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Contour problem - lane1 = [" + lane1.getFullId() + "], lane2 = [" + lane2.getFullId()
                            + "]; skipped");
                    continue;
                }

                boolean permitted = permittedList.contains(lane1, lane2);

                while (numberOfJobs.get() > maxqueue) // keep max maxqueue jobs in the pool
                {
                    try
                    {
                        Thread.sleep(1);
                    }
                    catch (InterruptedException exception)
                    {
                        // ignore
                    }
                }
                numberOfJobs.incrementAndGet();
                Set<Lane> down2 = lane2.nextLanes(null);
                Set<Lane> up2 = lane2.prevLanes(null);
                ConflictBuilderRecordSmall cbr = new ConflictBuilderRecordSmall(lane1, down1, up1, lane2, down2, up2, permitted,
                        simulator, widthGenerator, leftEdges, rightEdges);
                executor.execute(new CbrTaskSmall(numberOfJobs, cbr));
            }
        }

        long time = System.currentTimeMillis();
        // wait max 60 sec for last maxqueue jobs
        while (numberOfJobs.get() > 0 && System.currentTimeMillis() - time < 60000)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException exception)
            {
                // ignore
            }
        }

        executor.shutdown();
        while (!executor.isTerminated())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException exception)
            {
                // ignore
            }
        }

        simulator.getLogger().always()
                .debug(String.format(
                        "generating conflicts complete (generated %d merge conflicts, %d split "
                                + "conflicts, %d crossing conflicts)",
                        numberMergeConflicts.get(), numberSplitConflicts.get(), numberCrossConflicts.get()));
    }

    /**
     * Build conflicts on list of lanes; parallel implementation. Big jobs.
     * @param lanes list of Lanes
     * @param simulator the simulator
     * @param widthGenerator the width generator
     * @param ignoreList lane combinations to ignore
     * @param permittedList lane combinations that are permitted by traffic control
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflictsParallelBig(final List<Lane> lanes, final OtsSimulatorInterface simulator,
            final WidthGenerator widthGenerator, final LaneCombinationList ignoreList, final LaneCombinationList permittedList)
            throws OtsGeometryException
    {
        long totalCombinations = ((long) lanes.size()) * ((long) lanes.size() - 1) / 2;
        System.out.println("PARALLEL GENERATING OF CONFLICTS (BIG JOBS). " + totalCombinations + " COMBINATIONS");
        long lastReported = 0;
        Map<Lane, OtsLine2d> leftEdges = new LinkedHashMap<>();
        Map<Lane, OtsLine2d> rightEdges = new LinkedHashMap<>();

        // make a threadpool and execute buildConflicts for all records
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("USING " + cores + " CORES");
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
        AtomicInteger numberOfJobs = new AtomicInteger(0);
        final int maxqueue = 200;

        for (int i = 0; i < lanes.size(); i++)
        {
            long combinationsDone = totalCombinations - ((long) (lanes.size() - i)) * ((long) (lanes.size() - i - 1)) / 2;
            if (combinationsDone / 100000000 > lastReported)
            {
                simulator.getLogger().always()
                        .debug(String.format(
                                "generating conflicts at %.0f%% (generated %d merge conflicts, %d split "
                                        + "conflicts, %d crossing conflicts)",
                                100.0 * combinationsDone / totalCombinations, numberMergeConflicts.get(),
                                numberSplitConflicts.get(), numberCrossConflicts.get()));
                lastReported = combinationsDone / 100000000;
            }

            while (numberOfJobs.get() > maxqueue) // keep max maxqueue jobs in the pool
            {
                try
                {
                    Thread.sleep(0, 10);
                }
                catch (InterruptedException exception)
                {
                    // ignore
                }
            }
            numberOfJobs.incrementAndGet();

            ConflictBuilderRecordBig cbr = new ConflictBuilderRecordBig(i, lanes, ignoreList, permittedList, simulator,
                    widthGenerator, leftEdges, rightEdges);
            executor.execute(new CbrTaskBig(numberOfJobs, cbr));

        }

        long time = System.currentTimeMillis();
        // wait max 60 sec for last maxqueue jobs
        while (numberOfJobs.get() > 0 && System.currentTimeMillis() - time < 60000)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException exception)
            {
                // ignore
            }
        }

        executor.shutdown();
        while (!executor.isTerminated())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException exception)
            {
                // ignore
            }
        }

        simulator.getLogger().always()
                .debug(String.format(
                        "generating conflicts complete (generated %d merge conflicts, %d split "
                                + "conflicts, %d crossing conflicts)",
                        numberMergeConflicts.get(), numberSplitConflicts.get(), numberCrossConflicts.get()));
    }

    /**
     * Build conflicts on network using only the groups of links that have been identified as candidates with conflicts;
     * parallel implementation.
     * @param network network
     * @param conflictCandidateMap the map of the conflicting links to implement
     * @param simulator simulator
     * @param widthGenerator width generator
     * @throws OtsGeometryException in case of geometry exception
     */
    public static void buildConflictsParallel(final RoadNetwork network, final Map<String, Set<Link>> conflictCandidateMap,
            final OtsSimulatorInterface simulator, final WidthGenerator widthGenerator) throws OtsGeometryException
    {
        for (String conflictId : conflictCandidateMap.keySet())
        {
            // System.out.println(conflictId);
            List<Lane> lanes = new ArrayList<>();
            for (Link link : conflictCandidateMap.get(conflictId))
            {
                if (link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (element instanceof Lane lane && !(element instanceof Shoulder))
                        {
                            lanes.add((Lane) element);
                        }
                    }
                }
            }
            // TODO: make parallel
            // simulator.getLogger().setAllLogLevel(Level.WARNING);
            buildConflicts(lanes, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList(), conflictId);
            simulator.getLogger().setAllLogLevel(Level.DEBUG);
        }
    }

    /**
     * Small conflict builder task. A small task is finding all conflicts between two lanes.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    static class CbrTaskSmall implements Runnable
    {
        /** Small conflict builder record. */
        final ConflictBuilderRecordSmall cbr;

        /** Number of jobs to do. */
        final AtomicInteger nrOfJobs;

        /**
         * Constructor.
         * @param nrOfJobs number of jobs to do.
         * @param cbr the record to execute.
         */
        CbrTaskSmall(final AtomicInteger nrOfJobs, final ConflictBuilderRecordSmall cbr)
        {
            this.nrOfJobs = nrOfJobs;
            this.cbr = cbr;
        }

        /** {@inheritDoc} */
        @Override
        public void run()
        {
            try
            {
                buildConflicts(this.cbr.lane1, this.cbr.down1, this.cbr.up1, this.cbr.lane2, this.cbr.down2, this.cbr.up2,
                        this.cbr.permitted, this.cbr.simulator, this.cbr.widthGenerator, this.cbr.leftEdges,
                        this.cbr.rightEdges, false, null);
            }
            catch (NetworkException | OtsGeometryException ne)
            {
                throw new RuntimeException("Conflict build with bad combination of types / rules.", ne);
            }
            this.nrOfJobs.decrementAndGet();
        }
    }

    /**
     * Small conflict builder record. Small means this holds the information to create conflicts between two lanes.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param lane1 lane 1
     * @param down1 downstream lanes 1
     * @param up1 upstream lanes 1
     * @param lane2 lane 2
     * @param down2 downstream lane 2
     * @param up2 upstream lanes 2
     * @param permitted conflict permitted by traffic control
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param leftEdges cache of left edge lines
     * @param rightEdges cache of right edge lines
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    static record ConflictBuilderRecordSmall(Lane lane1, Set<Lane> down1, Set<Lane> up1, Lane lane2, Set<Lane> down2,
            Set<Lane> up2, boolean permitted, OtsSimulatorInterface simulator, WidthGenerator widthGenerator,
            Map<Lane, OtsLine2d> leftEdges, Map<Lane, OtsLine2d> rightEdges)
    {
    }

    /**
     * Large conflict builder task. A large task is finding all conflicts between one particular lane, and all lanes further in
     * a list.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    static class CbrTaskBig implements Runnable
    {
        /** Big conflict builder record. */
        final ConflictBuilderRecordBig cbr;

        /** Number of jobs to do. */
        final AtomicInteger nrOfJobs;

        /**
         * Constructor.
         * @param nrOfJobs number of jobs to do.
         * @param cbr the record to execute.
         */
        CbrTaskBig(final AtomicInteger nrOfJobs, final ConflictBuilderRecordBig cbr)
        {
            this.nrOfJobs = nrOfJobs;
            this.cbr = cbr;
        }

        /** {@inheritDoc} */
        @Override
        public void run()
        {
            try
            {
                Lane lane1 = this.cbr.lanes.get(this.cbr.starti);
                Set<Lane> up1 = lane1.prevLanes(null);
                Set<Lane> down1 = lane1.nextLanes(null);
                for (int j = this.cbr.starti + 1; j < this.cbr.lanes.size(); j++)
                {
                    Lane lane2 = this.cbr.lanes.get(j);
                    if (this.cbr.ignoreList.contains(lane1, lane2))
                    {
                        continue;
                    }
                    // Quick contour check, skip if non-overlapping envelopes
                    try
                    {
                        if (!lane1.getContour().intersects(lane2.getContour()))
                        {
                            continue;
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("Contour problem - lane1 = [" + lane1.getFullId() + "], lane2 = ["
                                + lane2.getFullId() + "]; skipped");
                        continue;
                    }

                    boolean permitted = this.cbr.permittedList.contains(lane1, lane2);

                    Set<Lane> down2 = lane2.nextLanes(null);
                    Set<Lane> up2 = lane2.prevLanes(null);

                    try
                    {
                        buildConflicts(lane1, down1, up1, lane2, down2, up2, permitted, this.cbr.simulator,
                                this.cbr.widthGenerator, this.cbr.leftEdges, this.cbr.rightEdges, false, null);
                    }
                    catch (NetworkException | OtsGeometryException ne)
                    {
                        lane2.getLink().getSimulator().getLogger().always().error(ne,
                                "Conflict build with bad combination of types / rules.");
                    }
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            this.nrOfJobs.decrementAndGet();
        }
    }

    /**
     * Big conflict builder record. Big means this holds the information to create conflicts between one particular lane, and
     * all lanes further in a list.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param starti the start index
     * @param lanes List of lanes
     * @param ignoreList list of lane combinations to ignore
     * @param permittedList list of lane combinations to permit
     * @param simulator simulator
     * @param widthGenerator width generator
     * @param leftEdges cache of left edge lines
     * @param rightEdges cache of right edge lines
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    static record ConflictBuilderRecordBig(int starti, List<Lane> lanes, LaneCombinationList ignoreList,
            LaneCombinationList permittedList, OtsSimulatorInterface simulator, WidthGenerator widthGenerator,
            Map<Lane, OtsLine2d> leftEdges, Map<Lane, OtsLine2d> rightEdges)
    {
    }

}
