package org.opentrafficsim.road.network.lane.conflict;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableIterator;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableMap.ImmutableEntry;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Default width generator for conflicts which uses 80% of the lane width. */
    public static final WidthGenerator DEFAULT_WIDTH_GENERATOR = new RelativeWidthGenerator(0.8);

    private static int numberMergeConflicts = 0;

    private static int numberSplitConflicts = 0;

    private static int numberCrossConflicts = 0;

    /**
     * Empty constructor.
     */
    private ConflictBuilder()
    {
        //
    }

    /**
     * Build conflicts on network.
     * @param network OTSRoadNetwork; network
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final OTSRoadNetwork network, final GTUType gtuType,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator)
            throws OTSGeometryException
    {
        buildConflicts(network, gtuType, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList());
    }

    /**
     * Build conflicts on network.
     * @param network OTSRoadNetwork; network
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @param ignoreList LaneCombinationList; lane combinations to ignore
     * @param permittedList LaneCombinationList; lane combinations that are permitted by traffic control
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final OTSRoadNetwork network, final GTUType gtuType,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator,
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
     * @param lanes List&lt;Lane&gt;; lanes
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final List<Lane> lanes, final GTUType gtuType,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator)
            throws OTSGeometryException
    {
        buildConflicts(lanes, gtuType, simulator, widthGenerator, new LaneCombinationList(), new LaneCombinationList());
    }

    /**
     * Build conflicts on list of lanes.
     * @param lanes List&lt;Lane&gt;; list of Lanes
     * @param gtuType GTUType; the GTU type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param widthGenerator WidthGenerator; the width generator
     * @param ignoreList LaneCombinationList; lane combinations to ignore
     * @param permittedList LaneCombinationList; lane combinations that are permitted by traffic control
     * @throws OTSGeometryException in case of geometry exception
     */
    public static void buildConflicts(final List<Lane> lanes, final GTUType gtuType,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator,
            final LaneCombinationList ignoreList, final LaneCombinationList permittedList) throws OTSGeometryException
    {
        // Loop Lane / GTUDirectionality combinations
        long totalCombinations = ((long) lanes.size()) * ((long) lanes.size() - 1) / 2;
        System.out.println("GENERATING CONFLICTS. " + totalCombinations + " COMBINATIONS");
        long lastReported = 0;
        Map<Lane, OTSLine3D> leftEdges = new LinkedHashMap<>();
        Map<Lane, OTSLine3D> rightEdges = new LinkedHashMap<>();

        for (int i = 0; i < lanes.size(); i++)
        {
            long combinationsDone = totalCombinations - ((long) (lanes.size() - i)) * ((long) (lanes.size() - i)) / 2;
            if (combinationsDone / 100000000 > lastReported)
            {
                SimLogger.always()
                        .debug(String.format("generating conflicts at %.0f%%", 100.0 * combinationsDone / totalCombinations));
                lastReported = combinationsDone / 100000000;
            }
            Lane lane1 = lanes.get(i);
            for (GTUDirectionality dir1 : lane1.getLaneType().getDirectionality(gtuType).getDirectionalities())
            {
                ImmutableMap<Lane, GTUDirectionality> down1 = lane1.downstreamLanes(dir1, gtuType);
                ImmutableMap<Lane, GTUDirectionality> up1 = lane1.upstreamLanes(dir1, gtuType);

                for (int j = i + 1; j < lanes.size(); j++)
                {
                    Lane lane2 = lanes.get(j);
                    if (ignoreList.contains(lane1, lane2))
                    {
                        continue;
                    }
                    boolean permitted = permittedList.contains(lane1, lane2);

                    for (GTUDirectionality dir2 : lane2.getLaneType().getDirectionality(gtuType).getDirectionalities())
                    {
                        ImmutableMap<Lane, GTUDirectionality> down2 = lane2.downstreamLanes(dir2, gtuType);
                        ImmutableMap<Lane, GTUDirectionality> up2 = lane2.upstreamLanes(dir2, gtuType);
                        // See if conflict needs to be build, and build if so
                        try
                        {
                            buildConflicts(lane1, dir1, down1, up1, lane2, dir2, down2, up2, gtuType, permitted, simulator,
                                    widthGenerator, leftEdges, rightEdges);
                        }
                        catch (NetworkException ne)
                        {
                            throw new RuntimeException("Conflict build with bad combination of types / rules.", ne);
                        }
                    }
                }
            }
        }
        System.out.println("MERGE CONFLICTS = " + numberMergeConflicts);
        System.out.println("SPLIT CONFLICTS = " + numberSplitConflicts);
        System.out.println("CROSS CONFLICTS = " + numberCrossConflicts);
    }

    /**
     * Build conflict on single lane pair. Connecting lanes are determined.
     * @param lane1 Lane; lane 1
     * @param dir1 GTUDirectionality; gtu direction 1
     * @param lane2 Lane; lane 2
     * @param dir2 GTUDirectionality; gtu direction 2
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void buildConflicts(final Lane lane1, final GTUDirectionality dir1, final Lane lane2,
            final GTUDirectionality dir2, final GTUType gtuType, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final WidthGenerator widthGenerator) throws OTSGeometryException
    {
        buildConflicts(lane1, dir1, lane2, dir2, gtuType, simulator, widthGenerator, false);
    }

    /**
     * Build conflict on single lane pair. Connecting lanes are determined.
     * @param lane1 Lane; lane 1
     * @param dir1 GTUDirectionality; gtu direction 1
     * @param lane2 Lane; lane 2
     * @param dir2 GTUDirectionality; gtu direction 2
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @param permitted boolean; conflict permitted by traffic control
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void buildConflicts(final Lane lane1, final GTUDirectionality dir1, final Lane lane2,
            final GTUDirectionality dir2, final GTUType gtuType, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final WidthGenerator widthGenerator, final boolean permitted) throws OTSGeometryException
    {
        ImmutableMap<Lane, GTUDirectionality> down1 = lane1.downstreamLanes(dir1, gtuType);
        ImmutableMap<Lane, GTUDirectionality> up1 = lane1.upstreamLanes(dir1, gtuType);
        ImmutableMap<Lane, GTUDirectionality> down2 = lane2.downstreamLanes(dir2, gtuType);
        ImmutableMap<Lane, GTUDirectionality> up2 = lane2.upstreamLanes(dir2, gtuType);
        try
        {
            buildConflicts(lane1, dir1, down1, up1, lane2, dir2, down2, up2, gtuType, permitted, simulator, widthGenerator,
                    new LinkedHashMap<>(), new LinkedHashMap<>());
        }
        catch (NetworkException ne)
        {
            throw new RuntimeException("Conflict build with bad combination of types / rules.", ne);
        }
    }

    /**
     * Build conflicts on single lane pair.
     * @param lane1 Lane; lane 1
     * @param dir1 GTUDirectionality; gtu direction 1
     * @param down1 Map&lt;Lane,GTUDirectionality&gt;; downstream lanes 1
     * @param up1 Map&lt;Lane,GTUDirectionality&gt;; upstream lanes 1
     * @param lane2 Lane; lane 2
     * @param dir2 GTUDirectionality; gtu direction 2
     * @param down2 Map&lt;Lane,GTUDirectionality&gt;; downstream lane 2
     * @param up2 Map&lt;Lane,GTUDirectionality&gt;; upstream lanes 2
     * @param gtuType GTUType; gtu type
     * @param permitted boolean; conflict permitted by traffic control
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @param leftEdges Map<Lane, OTSLine3D>; cache of left edge lines
     * @param rightEdges Map<Lane, OTSLine3D>; cache of right edge lines
     * @throws OTSGeometryException in case of geometry exception
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildConflicts(final Lane lane1, final GTUDirectionality dir1,
            final ImmutableMap<Lane, GTUDirectionality> down1, final ImmutableMap<Lane, GTUDirectionality> up1,
            final Lane lane2, final GTUDirectionality dir2, final ImmutableMap<Lane, GTUDirectionality> down2,
            final ImmutableMap<Lane, GTUDirectionality> up2, final GTUType gtuType, final boolean permitted,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator,
            final Map<Lane, OTSLine3D> leftEdges, final Map<Lane, OTSLine3D> rightEdges)
            throws OTSGeometryException, NetworkException
    {

        // Quick contour check, skip if not overlapping
        if (!lane1.getContour().intersects(lane2.getContour()))
        {
            return;
        }

        // Get left and right lines at specified width
        // TODO: we cache, but the width generator may be different
        OTSLine3D left1 = leftEdges.get(lane1);
        OTSLine3D right1 = rightEdges.get(lane1);
        if (null == left1)
        {
            OTSLine3D line1 = lane1.getCenterLine();
            left1 = line1.offsetLine(widthGenerator.getWidth(lane1, 0.0) / 2, widthGenerator.getWidth(lane1, 1.0) / 2);
            leftEdges.put(lane1, left1);
            right1 = line1.offsetLine(-widthGenerator.getWidth(lane1, 0.0) / 2, -widthGenerator.getWidth(lane1, 1.0) / 2);
            rightEdges.put(lane1, right1);
        }
        OTSLine3D left2 = leftEdges.get(lane2);
        OTSLine3D right2 = rightEdges.get(lane2);
        if (null == left2)
        {
            OTSLine3D line2 = lane2.getCenterLine();
            left2 = line2.offsetLine(widthGenerator.getWidth(lane2, 0.0) / 2, widthGenerator.getWidth(lane2, 1.0) / 2);
            leftEdges.put(lane2, left2);
            right2 = line2.offsetLine(-widthGenerator.getWidth(lane2, 0.0) / 2, -widthGenerator.getWidth(lane2, 1.0) / 2);
            rightEdges.put(lane2, right2);
        }

        // Get list of all intersection fractions
        SortedSet<Intersection> intersections = Intersection.getIntersectionList(left1, left2, 0);
        intersections.addAll(Intersection.getIntersectionList(left1, right2, 1));
        intersections.addAll(Intersection.getIntersectionList(right1, left2, 2));
        intersections.addAll(Intersection.getIntersectionList(right1, right2, 3));

        // Create merge
        ImmutableIterator<ImmutableEntry<Lane, GTUDirectionality>> iterator1 = down1.entrySet().iterator();
        ImmutableIterator<ImmutableEntry<Lane, GTUDirectionality>> iterator2 = down2.entrySet().iterator();
        boolean merge = false;
        while (iterator1.hasNext() && !merge)
        {
            ImmutableEntry<Lane, GTUDirectionality> next1 = iterator1.next();
            while (iterator2.hasNext() && !merge)
            {
                ImmutableEntry<Lane, GTUDirectionality> next2 = iterator2.next();
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
                        SimLogger.always().warn("Fixing fractions of merge conflict");
                        fraction1 = 0;
                        fraction2 = 0;
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
            ImmutableEntry<Lane, GTUDirectionality> prev1 = iterator1.next();
            while (iterator2.hasNext() && !split)
            {
                ImmutableEntry<Lane, GTUDirectionality> prev2 = iterator2.next();
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
                        SimLogger.always().warn("Fixing fractions of split conflict");
                        fraction1 = 1;
                        fraction2 = 1;
                    }
                    // Build conflict
                    buildSplitConflict(lane1, dir1, fraction1, lane2, dir2, fraction2, gtuType, simulator, widthGenerator);
                    // Skip loop for efficiency, and do not create multiple splits in case of multiple same upstream lanes
                    split = true;
                }
            }
        }

        // Create crossings
        if (!lane1.getParentLink().equals(lane2.getParentLink())) // tight inner-curves with dedicated Bezier ignored
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
                    if (dir2.isMinus())
                    {
                        double f2Temp = f2Start;
                        f2Start = f2End;
                        f2End = f2Temp;
                    }
                    if (Double.isNaN(f1Start) || Double.isNaN(f2Start) || Double.isNaN(f2End))
                    {
                        SimLogger.always().warn("NOT YET Fixing fractions of crossing conflict");
                    }
                    buildCrossingConflict(lane1, dir1, f1Start, intersection.getFraction1(), lane2, dir2, f2Start, f2End,
                            gtuType, simulator, widthGenerator, permitted);
                    f1Start = Double.NaN;
                    f2Start = Double.NaN;
                    f2End = Double.NaN;
                }
            }
        }

    }

    /**
     * Build a merge conflict.
     * @param lane1 Lane; lane 1
     * @param dir1 GTUDirectionality; gtu direction 1
     * @param f1start double; start fraction 1
     * @param lane2 Lane; lane 2
     * @param dir2 GTUDirectionality; gtu direction 2
     * @param f2start double; start fraction 2
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @param permitted boolean; conflict permitted by traffic control
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildMergeConflict(final Lane lane1, final GTUDirectionality dir1, final double f1start,
            final Lane lane2, final GTUDirectionality dir2, final double f2start, final GTUType gtuType,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator, final boolean permitted)
            throws NetworkException, OTSGeometryException
    {

        // Determine lane end from direction
        double f1end = dir1.isPlus() ? 1.0 : 0.0;
        double f2end = dir2.isPlus() ? 1.0 : 0.0;

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().times(f1start);
        Length longitudinalPosition2 = lane2.getLength().times(f2start);
        Length length1 = lane1.getLength().times(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().times(Math.abs(f2end - f2start));

        // Get geometries
        OTSLine3D geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        OTSLine3D geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Determine conflict rule
        ConflictRule conflictRule;
        if (lane1.getParentLink().getPriority().isBusStop() || lane2.getParentLink().getPriority().isBusStop())
        {
            Throw.when(lane1.getParentLink().getPriority().isBusStop() && lane2.getParentLink().getPriority().isBusStop(),
                    IllegalArgumentException.class, "Merge conflict between two links with bus stop priority not supported.");
            conflictRule = new BusStopConflictRule(simulator);
        }
        else
        {
            conflictRule = new DefaultConflictRule();
        }

        // Make conflict
        Conflict.generateConflictPair(ConflictType.MERGE, conflictRule, permitted, lane1, longitudinalPosition1, length1, dir1,
                geometry1, gtuType, lane2, longitudinalPosition2, length2, dir2, geometry2, gtuType, simulator);

        numberMergeConflicts++;
    }

    /**
     * Build a split conflict.
     * @param lane1 Lane; lane 1
     * @param dir1 GTUDirectionality; gtu direction 1
     * @param f1end double; end fraction 1
     * @param lane2 Lane; lane 2
     * @param dir2 GTUDirectionality; gtu direction 2
     * @param f2end double; end fraction 2
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildSplitConflict(final Lane lane1, final GTUDirectionality dir1, final double f1end, final Lane lane2,
            final GTUDirectionality dir2, final double f2end, final GTUType gtuType,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator)
            throws NetworkException, OTSGeometryException
    {

        // Determine lane start from direction
        double f1start = dir1.isPlus() ? 0.0 : 1.0;
        double f2start = dir2.isPlus() ? 0.0 : 1.0;

        // Get locations and length
        Length longitudinalPosition1 = lane1.getLength().times(f1start);
        Length longitudinalPosition2 = lane2.getLength().times(f2start);
        Length length1 = lane1.getLength().times(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().times(Math.abs(f2end - f2start));

        // Get geometries
        OTSLine3D geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        OTSLine3D geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Make conflict
        Conflict.generateConflictPair(ConflictType.SPLIT, new SplitConflictRule(), false, lane1, longitudinalPosition1, length1,
                dir1, geometry1, gtuType, lane2, longitudinalPosition2, length2, dir2, geometry2, gtuType, simulator);
        
        numberSplitConflicts++;
    }

    /**
     * Build a crossing conflict.
     * @param lane1 Lane; lane 1
     * @param dir1 GTUDirectionality; gtu direction 1
     * @param f1start double; start fraction 1
     * @param f1end double; end fraction 1
     * @param lane2 Lane; lane 2
     * @param dir2 GTUDirectionality; gtu direction 2
     * @param f2start double; start fraction 2
     * @param f2end double; end fraction 2
     * @param gtuType GTUType; gtu type
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param widthGenerator WidthGenerator; width generator
     * @param permitted boolean; conflict permitted by traffic control
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     * @throws OTSGeometryException in case of geometry exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void buildCrossingConflict(final Lane lane1, final GTUDirectionality dir1, final double f1start,
            final double f1end, final Lane lane2, final GTUDirectionality dir2, final double f2start, final double f2end,
            final GTUType gtuType, final DEVSSimulatorInterface.TimeDoubleUnit simulator, final WidthGenerator widthGenerator,
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
        Length longitudinalPosition1 = lane1.getLength().times(f1startDirected);
        Length longitudinalPosition2 = lane2.getLength().times(f2startDirected);
        Length length1 = lane1.getLength().times(Math.abs(f1end - f1start));
        Length length2 = lane2.getLength().times(Math.abs(f2end - f2start));

        // Get geometries
        OTSLine3D geometry1 = getGeometry(lane1, f1start, f1end, widthGenerator);
        OTSLine3D geometry2 = getGeometry(lane2, f2start, f2end, widthGenerator);

        // Determine conflict rule
        ConflictRule conflictRule;
        if (lane1.getParentLink().getPriority().isBusStop() || lane2.getParentLink().getPriority().isBusStop())
        {
            Throw.when(lane1.getParentLink().getPriority().isBusStop() && lane2.getParentLink().getPriority().isBusStop(),
                    IllegalArgumentException.class, "Merge conflict between two links with bus stop priority not supported.");
            conflictRule = new BusStopConflictRule(simulator);
        }
        else
        {
            conflictRule = new DefaultConflictRule();
        }

        // Make conflict
        Conflict.generateConflictPair(ConflictType.CROSSING, conflictRule, permitted, lane1, longitudinalPosition1, length1,
                dir1, geometry1, gtuType, lane2, longitudinalPosition2, length2, dir2, geometry2, gtuType, simulator);
        
        numberCrossConflicts++;
    }

    /**
     * Creates geometry for conflict.
     * @param lane Lane; lane
     * @param fStart double; longitudinal fraction of start
     * @param fEnd double; longitudinal fraction of end
     * @param widthGenerator WidthGenerator; width generator
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
        if (f1 == f2)
        {
            SimLogger.always().debug("f1 (" + f1 + ") equals f2 (" + f2 + "); problematic lane is " + lane.toString());
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
     * Intersection holds two fractions where two lines have crossed. There is also a combo to identify which lines have been
     * used to find the intersection.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
         * @param fraction1 double; fraction on lane 1
         * @param fraction2 double; fraction on lane 1
         * @param combo int; edge combination number
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
         * @param line1 OTSLine3D; line 1
         * @param line2 OTSLine3D; line 2
         * @param combo int; edge combination number
         * @return set of intersections, sorted by the fraction on line 1
         * @throws OTSGeometryException in case of geometry exception
         */
        public static SortedSet<Intersection> getIntersectionList(final OTSLine3D line1, final OTSLine3D line2, final int combo)
                throws OTSGeometryException
        {
            SortedSet<Intersection> out = new TreeSet<>();
            // if (!line1.getBounds().intersect(line2.getBounds()))
            // {
            // return out;
            // }
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
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
        double getWidth(Lane lane, double fraction);

    }

    /**
     * Generator with fixed width.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
         * @param width Length; width
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
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "RelativeWidthGenerator [factor=" + this.factor + "]";
        }

    }

}
