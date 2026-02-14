package org.opentrafficsim.road.gtu.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.RoadPosition.BySpeed;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.RoadPosition.ByValue;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.network.CrossSectionLink;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LanePosition;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Helper class for vehicle generation which can draw the next GTU position to try to place a GTU. If the GTU can not be placed,
 * it should be included in a queue. This class requires the number of unplaced GTU's per lane, in order to appropriately divide
 * traffic over the lanes.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface GeneratorPositions
{

    /**
     * Draw a new position to generate a GTU.
     * @param gtuType GTU type.
     * @param characteristics characteristics of the generated GTU.
     * @param unplaced number of unplaced GTUs per lane, counting from the right and starting at 1.
     * @return new position to generate a GTU.
     * @throws GtuException when the underlying structure is inconsistent for drawing
     */
    GeneratorLanePosition draw(GtuType gtuType, LaneBasedGtuCharacteristics characteristics,
            Map<CrossSectionLink, Map<Integer, Integer>> unplaced) throws GtuException;

    /**
     * Returns all underlying positions.
     * @return all underlying positions.
     */
    Set<GeneratorLanePosition> getAllPositions();

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link. Lanes are drawn
     * without bias. Each link receives a weight equal to the number of lanes.
     * @param positions all considered positions, each lane is considered separately
     * @param stream stream for random numbers
     * @return object to draw positions from
     */
    static GeneratorPositions create(final Set<LanePosition> positions, final StreamInterface stream)
    {
        return create(positions, stream, null, null, null);
    }

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link. Each link receives a
     * weight equal to the number of lanes.
     * @param positions all considered positions, each lane is considered separately
     * @param stream stream for random numbers
     * @param biases lane biases for GTU types
     * @return object to draw positions from
     */
    static GeneratorPositions create(final Set<LanePosition> positions, final StreamInterface stream, final LaneBiases biases)
    {
        return create(positions, stream, biases, null, null);
    }

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link. Lanes are drawn
     * without bias.
     * @param positions all considered positions, each lane is considered separately
     * @param stream stream for random numbers
     * @param linkWeights weight per link direction
     * @param viaNodes nodes connectors feed to for each link where GTU's will be generated
     * @return object to draw positions from
     */
    static GeneratorPositions create(final Set<LanePosition> positions, final StreamInterface stream,
            final Map<CrossSectionLink, Double> linkWeights, final Map<CrossSectionLink, Node> viaNodes)
    {
        return create(positions, stream, null, linkWeights, viaNodes);
    }

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link.
     * @param positions all considered positions, each lane is considered separately
     * @param stream stream for random numbers
     * @param laneBiases lane biases for GTU types
     * @param linkWeights weight per link
     * @param viaNodes nodes connectors feed to for each link where GTU's will be generated
     * @return object to draw positions from
     */
    static GeneratorPositions create(final Set<LanePosition> positions, final StreamInterface stream,
            final LaneBiases laneBiases, final Map<CrossSectionLink, Double> linkWeights,
            final Map<CrossSectionLink, Node> viaNodes)
    {

        // group directions per link
        Map<Link, Set<LanePosition>> linkSplit = new LinkedHashMap<>();
        for (LanePosition position : positions)
        {
            linkSplit.computeIfAbsent(position.lane().getLink(), (link) -> new LinkedHashSet<>()).add(position);
        }

        // create list of GeneratorLinkPositions
        List<GeneratorLinkPosition> linkPositions = new ArrayList<>();
        Set<GeneratorLanePosition> allLanePositions = new LinkedHashSet<>();
        for (Link splitLink : linkSplit.keySet())
        {
            List<Lane> lanes = ((CrossSectionLink) splitLink).getLanes();
            // let's sort the lanes by lateral position
            Collections.sort(lanes, new Comparator<Lane>()
            {
                @Override
                public int compare(final Lane lane1, final Lane lane2)
                {
                    Length lat1 = lane1.getOffsetAtBegin();
                    Length lat2 = lane2.getOffsetAtBegin();
                    return lat1.compareTo(lat2);
                }
            });
            // create list of GeneratorLanePositions
            List<GeneratorLanePosition> lanePositions = new ArrayList<>();
            for (LanePosition lanePosition : linkSplit.get(splitLink))
            {
                lanePositions.add(new GeneratorLanePosition(lanes.indexOf(lanePosition.lane()) + 1, lanePosition,
                        (CrossSectionLink) splitLink));
            }
            allLanePositions.addAll(lanePositions);
            // create the GeneratorLinkPosition
            if (linkWeights == null)
            {
                linkPositions.add(new GeneratorLinkPosition(lanePositions, splitLink, stream, laneBiases));
            }
            else
            {
                Double weight = linkWeights.get(splitLink);
                Throw.whenNull(weight, "Using link weights for GTU generation, but no weight for link %s is defined.",
                        splitLink);
                linkPositions.add(new GeneratorLinkPosition(lanePositions, splitLink, stream, laneBiases, weight,
                        viaNodes.get(splitLink)));
            }
        }

        // create the GeneratorZonePosition
        GeneratorZonePosition position = new GeneratorZonePosition(linkPositions);
        return new GeneratorPositions()
        {
            @Override
            public GeneratorLanePosition draw(final GtuType gtuType, final LaneBasedGtuCharacteristics characteristics,
                    final Map<CrossSectionLink, Map<Integer, Integer>> unplaced) throws GtuException
            {
                GeneratorLinkPosition linkPosition =
                        position.draw(gtuType, stream, characteristics.getDestination(), characteristics.getRoute());
                Speed desiredSpeed = characteristics.getStrategicalPlannerFactory()
                        .peekDesiredSpeed(gtuType, linkPosition.speedLimit(gtuType), characteristics.getMaximumSpeed())
                        .orElseGet(() -> linkPosition.speedLimit(gtuType));
                return linkPosition.draw(gtuType, unplaced.get(linkPosition.getLink()), desiredSpeed);
            }

            @Override
            public Set<GeneratorLanePosition> getAllPositions()
            {
                return allLanePositions;
            }
        };
    }

    /**
     * Class representing a vehicle generation lane, providing elementary information for randomly drawing links and lanes.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    final class GeneratorLanePosition
    {

        /** Lane number, where 1 is the right-most lane. */
        private final int laneNumber;

        /** Position. */
        private final LanePosition position;

        /** Link. */
        private final CrossSectionLink link;

        /**
         * Constructor.
         * @param laneNumber lane number, where 1 is the right-most lane
         * @param position position set, representing a single GTU position on the network
         * @param link link
         */
        GeneratorLanePosition(final int laneNumber, final LanePosition position, final CrossSectionLink link)
        {
            this.laneNumber = laneNumber;
            this.position = position;
            this.link = link;
        }

        /**
         * Returns the lane number, where 1 is the right-most lane.
         * @return lane number, where 1 is the right-most lane
         */
        int getLaneNumber()
        {
            return this.laneNumber;
        }

        /**
         * Returns whether this lane is accessible to the GTU type.
         * @param gtuType gtu type
         * @return whether this lane is accessible to the GTU type
         */
        boolean allows(final GtuType gtuType)
        {
            return this.position.lane().getType().isCompatible(gtuType);
        }

        /**
         * Returns the contained position set, representing a single GTU position on the network.
         * @return contained position set, representing a single GTU position on the network
         */
        LanePosition getPosition()
        {
            return this.position;
        }

        /**
         * Returns the link.
         * @return link
         */
        CrossSectionLink getLink()
        {
            return this.link;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.laneNumber, this.link, this.position);
        }

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
            GeneratorLanePosition other = (GeneratorLanePosition) obj;
            return this.laneNumber == other.laneNumber && Objects.equals(this.link, other.link)
                    && Objects.equals(this.position, other.position);
        }

        @Override
        public String toString()
        {
            return "GeneratorLanePosition [laneNumber=" + this.laneNumber + ", position=" + this.position + ", link="
                    + this.link + "]";
        }

    }

    /**
     * Class representing a vehicle generation link to provide individual generation positions.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    final class GeneratorLinkPosition
    {

        /** Contained lanes. */
        private final List<GeneratorLanePosition> positions;

        /** The link. */
        private final Link link;

        /** Random stream. */
        private final StreamInterface stream;

        /** Lane bias. */
        private final LaneBiases laneBiases;

        /** Weight for drawing this link. */
        private final double weight;

        /** Node by which a connector connects, may be {@code null}. */
        private final Node viaNode;

        /**
         * Constructor.
         * @param positions contained lanes
         * @param link the link
         * @param stream stream
         * @param laneBiases lane biases
         */
        GeneratorLinkPosition(final List<GeneratorLanePosition> positions, final Link link, final StreamInterface stream,
                final LaneBiases laneBiases)
        {
            this.positions = positions;
            this.link = link;
            this.stream = stream;
            this.laneBiases = laneBiases;
            this.weight = -1;
            this.viaNode = null;
        }

        /**
         * Constructor.
         * @param positions contained lanes
         * @param link the link
         * @param stream stream
         * @param laneBiases lane biases
         * @param weight weight for drawing this link
         * @param viaNode node by which a connector connects
         */
        GeneratorLinkPosition(final List<GeneratorLanePosition> positions, final Link link, final StreamInterface stream,
                final LaneBiases laneBiases, final double weight, final Node viaNode)
        {
            this.positions = positions;
            this.link = link;
            this.stream = stream;
            this.laneBiases = laneBiases;
            this.weight = weight;
            this.viaNode = viaNode;
        }

        /**
         * Return the link.
         * @return link
         */
        Link getLink()
        {
            return this.link;
        }

        /**
         * Returns the weight for this link. This is either a predefined weight, or the number of lanes for the GTU type.
         * @param gtuType GTU type
         * @return weight for this link
         */
        double getWeight(final GtuType gtuType)
        {
            if (this.weight < 0.0)
            {
                return getNumberOfLanes(gtuType);
            }
            return this.weight;
        }

        /**
         * Returns the node by which a connector connects.
         * @return the node by which a connector connects
         */
        Node getViaNode()
        {
            return this.viaNode;
        }

        /**
         * Returns the number of accessible lanes for the GTU type.
         * @param gtuType GTU type
         * @return number of accessible lanes for the GTU type
         */
        int getNumberOfLanes(final GtuType gtuType)
        {
            int numberOfLanes = 0;
            for (GeneratorLanePosition lanePosition : this.positions)
            {
                if (lanePosition.allows(gtuType))
                {
                    numberOfLanes++;
                }
            }
            return numberOfLanes;
        }

        /**
         * Draws a specific GeneratorLanePosition utilizing lane biases of GTU types.
         * @param gtuType GTU type
         * @param unplaced number of unplaced GTUs per lane. The lane number should match with
         *            {@code GeneratorLanePosition.getLaneNumber()}, where 1 is the right-most lane. Missing lanes are assumed
         *            to have no queue.
         * @param desiredSpeed desired speed, possibly used to determine the biased road position
         * @return specific GeneratorLanePosition utilizing lane biases of GTU types
         */
        GeneratorLanePosition draw(final GtuType gtuType, final Map<Integer, Integer> unplaced, final Speed desiredSpeed)
        {
            Map<GeneratorLanePosition, Double> map = new LinkedHashMap<>();
            for (int i = 0; i < this.positions.size(); i++)
            {
                GeneratorLanePosition lanePosition = this.positions.get(i);
                if (lanePosition.allows(gtuType))
                {
                    GtuType type = gtuType;
                    boolean found = false;
                    while (this.laneBiases != null && !found && type != null)
                    {
                        if (this.laneBiases.contains(type))
                        {
                            found = true;
                            int laneNum = lanePosition.getLaneNumber();
                            int unplacedTemplates = unplaced == null ? 0 : unplaced.getOrDefault(laneNum, 0);
                            double w = this.laneBiases.getBias(type).calculateWeight(laneNum, getNumberOfLanes(gtuType),
                                    unplacedTemplates, desiredSpeed);
                            map.put(lanePosition, w);
                        }
                        type = type.getParent().orElse(null);
                    }
                    if (!found)
                    {
                        map.put(lanePosition, 1.0);
                    }
                }
            }
            if (0 == map.size())
            {
                Logger.ots().error("This really, really can't work...");
            }
            return Draw.drawWeighted(map, this.stream);
        }

        @Override
        public String toString()
        {
            return "GeneratorLinkPosition [positions=" + this.positions + "]";
        }

        /**
         * Get speed limit.
         * @param gtuType GTU type
         * @return speed limit
         */
        public Speed speedLimit(final GtuType gtuType)
        {
            Speed speedLimit = null;
            for (GeneratorLanePosition pos : this.positions)
            {
                try
                {
                    Speed limit = pos.getPosition().lane().getSpeedLimit(gtuType);
                    if (speedLimit == null || limit.lt(speedLimit))
                    {
                        speedLimit = limit;
                    }
                }
                catch (NetworkException exception)
                {
                    // ignore
                }
            }
            Throw.when(speedLimit == null, IllegalStateException.class, "No speed limit could be determined for GtuType %s.",
                    gtuType);
            return speedLimit;
        }

    }

    /**
     * Class representing a vehicle generation zone to provide individual generation positions.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    final class GeneratorZonePosition
    {

        /** Contained links. */
        private final List<GeneratorLinkPosition> positions;

        /**
         * Constructor.
         * @param positions contained links
         */
        GeneratorZonePosition(final List<GeneratorLinkPosition> positions)
        {
            this.positions = positions;
        }

        /**
         * Draws a GeneratorLinkPosition using number of accessible lanes for the GtuType as weight, and a GeneratorLanePosition
         * from that.
         * @param gtuType GTU type
         * @param stream stream for random numbers
         * @param destination destination node
         * @param route route, may be {@code null}
         * @return draws a LinkPosition using number of accessible lanes for the GtuType as weight, and a GeneratorLanePosition
         *         from that
         */
        GeneratorLinkPosition draw(final GtuType gtuType, final StreamInterface stream, final Node destination,
                final Route route)
        {
            Map<GeneratorLinkPosition, Double> map = new LinkedHashMap<>();
            for (int i = 0; i < this.positions.size(); i++)
            {
                GeneratorLinkPosition glp = this.positions.get(i);
                Link link = glp.getLink();
                if (route != null)
                {
                    int from = route.indexOf(link.getStartNode());
                    int to = route.indexOf(link.getEndNode());
                    if (from > -1 && to > -1 && to - from == 1)
                    {
                        map.put(glp, glp.getWeight(gtuType));
                    }
                }
                else
                {
                    // let's check whether any route is possible over this link
                    if (glp.getViaNode() != null)
                    {
                        Route r;
                        try
                        {
                            r = glp.getViaNode().getNetwork().getShortestRouteBetween(gtuType, glp.getViaNode(), destination);
                        }
                        catch (NetworkException exception)
                        {
                            r = null;
                        }
                        if (r != null)
                        {
                            map.put(glp, glp.getWeight(gtuType));
                        }
                    }
                    else
                    {
                        map.put(glp, glp.getWeight(gtuType));
                    }
                }
            }
            return Draw.drawWeighted(map, stream);
        }

        @Override
        public String toString()
        {
            return "GeneratorZonePosition [positions=" + this.positions + "]";
        }

    }

    /**
     * Set of lane biases per GTU type.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    final class LaneBiases
    {

        /** Biases per GTU type. */
        private final Map<GtuType, LaneBias> biases = new LinkedHashMap<>();

        /**
         * Constructor.
         */
        public LaneBiases()
        {
            //
        }

        /**
         * Adds a GTU bias for randomly drawing a lane.
         * @param gtuType gtu type
         * @param bias bias
         * @return lane biases for method chaining
         */
        public LaneBiases addBias(final GtuType gtuType, final LaneBias bias)
        {
            Throw.whenNull(gtuType, "GTU type may not be null.");
            Throw.whenNull(bias, "Bias may not be null.");
            this.biases.put(gtuType, bias);
            return this;
        }

        /**
         * Whether a bias is defined for the given type.
         * @param gtuType GTU type
         * @return whether a bias is defined for the given type
         */
        public boolean contains(final GtuType gtuType)
        {
            return this.biases.containsKey(gtuType);
        }

        /**
         * Returns the bias of given GTU type, or {@code Bias.None} if none defined for the GTU type.
         * @param gtuType GTU type
         * @return bias of the GTU type
         */
        public LaneBias getBias(final GtuType gtuType)
        {
            return this.biases.getOrDefault(gtuType, LaneBias.NONE);
        }

        @Override
        public String toString()
        {
            return "LaneBiases [" + this.biases + "]";
        }

    }

    /**
     * Vehicle generation lateral bias. Includes a lane maximum, e.g. trucks only on 2 right-hand lanes.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    final class LaneBias
    {

        /** No bias. */
        public static final LaneBias NONE = new LaneBias(new ByValue(0.0), 0.0, Integer.MAX_VALUE);

        /** Weak left-hand bias, 2nd left lane contains 50% relative to left most lane, in free traffic. */
        public static final LaneBias WEAK_LEFT = new LaneBias(new ByValue(1.0), 1.0, Integer.MAX_VALUE);

        /** Left-hand bias, 2nd left lane contains 25% relative to left most lane, in free traffic. */
        public static final LaneBias LEFT = new LaneBias(new ByValue(1.0), 2.0, Integer.MAX_VALUE);

        /** Strong left-hand bias, 2nd left lane contains 3.125% relative to left most lane, in free traffic. */
        public static final LaneBias STRONG_LEFT = new LaneBias(new ByValue(1.0), 5.0, Integer.MAX_VALUE);

        /** Weak middle bias, 2nd left lane contains 50% relative to left most lane, in free traffic. */
        public static final LaneBias WEAK_MIDDLE = new LaneBias(new ByValue(0.5), 1.0, Integer.MAX_VALUE);

        /** Middle bias, 2nd left lane contains 25% relative to left most lane, in free traffic. */
        public static final LaneBias MIDDLE = new LaneBias(new ByValue(0.5), 2.0, Integer.MAX_VALUE);

        /** Strong middle bias, 2nd left lane contains 3.125% relative to left most lane, in free traffic. */
        public static final LaneBias STRONG_MIDDLE = new LaneBias(new ByValue(0.5), 5.0, Integer.MAX_VALUE);

        /** Weak right-hand bias, 2nd right lane contains 50% relative to right most lane, in free traffic. */
        public static final LaneBias WEAK_RIGHT = new LaneBias(new ByValue(0.0), 1.0, Integer.MAX_VALUE);

        /** Right-hand bias, 2nd right lane contains 25% relative to right most lane, in free traffic. */
        public static final LaneBias RIGHT = new LaneBias(new ByValue(0.0), 2.0, Integer.MAX_VALUE);

        /** Strong right-hand bias, 2nd right lane contains 3.125% relative to right most lane, in free traffic. */
        public static final LaneBias STRONG_RIGHT = new LaneBias(new ByValue(0.0), 5.0, Integer.MAX_VALUE);

        /** Strong right-hand bias, limited to a maximum of 2 lanes. */
        public static final LaneBias TRUCK_RIGHT = new LaneBias(new ByValue(0.0), 5.0, 2);

        /**
         * Returns a bias by speed with normal extent.
         * @param leftSpeed desired speed for full left bias
         * @param rightSpeed desired speed for full right bias
         * @return bias by speed with normal extent
         */
        public static LaneBias bySpeed(final Speed leftSpeed, final Speed rightSpeed)
        {
            return new LaneBias(new BySpeed(leftSpeed, rightSpeed), 2.0, Integer.MAX_VALUE);
        }

        /**
         * Returns a bias by speed with normal extent. Convenience km/h input.
         * @param leftSpeedKm desired speed for full left bias
         * @param rightSpeedKm desired speed for full right bias
         * @return bias by speed with normal extent
         */
        public static LaneBias bySpeed(final double leftSpeedKm, final double rightSpeedKm)
        {
            return bySpeed(new Speed(leftSpeedKm, SpeedUnit.KM_PER_HOUR), new Speed(rightSpeedKm, SpeedUnit.KM_PER_HOUR));
        }

        /** Provider of position on the road (0 = full left, 1 = full right). */
        private final RoadPosition roadPosition;

        /** Bias extent. */
        private final double bias;

        /** Number of lanes to consider in either direction, including the preferred lane. */
        private final double stickyLanes;

        /**
         * Constructor.
         * @param roadPosition lateral position on the road (0 = right, 0.5 = middle, 1 = left)
         * @param bias bias extent, lower values create more spread traffic, 0.0 causes no lane preference
         * @param stickyLanes number of lanes to consider in either direction, including the preferred lane
         */
        public LaneBias(final RoadPosition roadPosition, final double bias, final double stickyLanes)
        {
            Throw.when(bias < 0.0, IllegalArgumentException.class, "Bias should be positive or 0.");
            Throw.when(stickyLanes < 1.0, IllegalArgumentException.class, "Sticky lanes should be 1.0 or larger.");
            this.roadPosition = roadPosition;
            this.bias = bias;
            this.stickyLanes = stickyLanes;
        }

        /**
         * Returns a random draw weight for given lane. The weight is calculated as:
         *
         * <pre>
         * weight = { 0,                               d &gt;= number of sticky lanes
         *          { 1 / ((d + 1)^bias * (m + 1)),    otherwise
         *
         * where,
         *      d:      lane deviation from lateral bias position
         *      bias:   bias extent
         *      m:      number of unplaced GTU's
         * </pre>
         *
         * The formula makes sure that all lanes have equal weight for <i>bias</i> &#61; 0, given an equal number of unplaced
         * GTU's <i>m</i>. The bias can be seen to result in this: for each GTU on the 2nd lane, there are 2^(<i>bias</i> - 1)
         * GTU's on the 1st lane. In numbers: 1 vs. 1 for <i>bias</i> &#61; 0, 1 vs. 2 for <i>bias</i> &#61; 1, 1 vs. 4 for
         * <i>bias</i> &#61; 2, 1 vs. 8 for <i>bias</i> &#61; 3, etc.<br>
         * <br>
         * Division by <i>m</i> + 1 makes sure traffic distributes over the lanes in case of spillback, or otherwise too high
         * demand on a particular lane. The weight for lanes with more unplaced GTU's simply reduces. This effect balances out
         * with the bias, meaning that for a strong bias, GTU's are still likely to be generated on the biased lanes. Given a
         * relatively strong bias of <i>bias</i> &#61; 5, the weight for the 1st and 2nd lane becomes equal if the 2nd lane has
         * no unplaced GTU's, while the 1st lane has 31 unplaced GTU's.<br>
         * <br>
         * Lane deviation <i>d</i> is calculated as <i>d</i> &#61; abs(<i>latBiasLane</i> - <i>laneNumFromRight</i>). Here,
         * <i>latBiasLane</i> &#61; 1 + <i>roadPosition</i>*(<i>numberOfLanes</i> - 1), i.e. ranging from 1 to 4 on a 4-lane
         * road. For lanes that are beyond the number of sticky lanes, the weight is always 0.<br>
         * <br>
         * @param laneNumFromRight number of lane counted from right to left
         * @param numberOfLanes total number of lanes
         * @param numberOfUnplacedGTUs number of GTU's in the generation queue
         * @param desiredSpeed desired speed, possibly used to determine the biased road position
         * @return random draw weight for given lane
         */
        public double calculateWeight(final int laneNumFromRight, final int numberOfLanes, final int numberOfUnplacedGTUs,
                final Speed desiredSpeed)
        {
            double d = Math.abs((1.0 + this.roadPosition.getValue(desiredSpeed) * (numberOfLanes - 1.0)) - laneNumFromRight);
            if (d >= this.stickyLanes)
            {
                return 0.0;
            }
            return 1.0 / (Math.pow(d + 1.0, this.bias) * (numberOfUnplacedGTUs + 1.0));
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(this.bias);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((this.roadPosition == null) ? 0 : this.roadPosition.hashCode());
            temp = Double.doubleToLongBits(this.stickyLanes);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

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
            LaneBias other = (LaneBias) obj;
            if (Double.doubleToLongBits(this.bias) != Double.doubleToLongBits(other.bias))
            {
                return false;
            }
            if (this.roadPosition == null)
            {
                if (other.roadPosition != null)
                {
                    return false;
                }
            }
            else if (!this.roadPosition.equals(other.roadPosition))
            {
                return false;
            }
            if (Double.doubleToLongBits(this.stickyLanes) != Double.doubleToLongBits(other.stickyLanes))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Bias [roadPosition=" + this.roadPosition + ", bias=" + this.bias + ", stickyLanes=" + this.stickyLanes
                    + "]";
        }

    }

    /**
     * Interface for preferred road position for a lane bias.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    interface RoadPosition
    {

        /**
         * Returns the road position (0.0 = right, 1.0 = left).
         * @param desiredSpeed desired speed at the generator
         * @return road position (0.0 = right, 1.0 = left)
         */
        double getValue(Speed desiredSpeed);

        /**
         * Fixed road position.
         */
        class ByValue implements RoadPosition
        {

            /** Road position. */
            private double value;

            /**
             * Constructor.
             * @param value road position
             */
            public ByValue(final double value)
            {
                Throw.when(value < 0.0 || value > 1.0, IllegalArgumentException.class,
                        "Road position value should be in the range [0...1].");
                this.value = value;
            }

            @Override
            public double getValue(final Speed desiredSpeed)
            {
                return this.value;
            }

            @Override
            public int hashCode()
            {
                final int prime = 31;
                int result = 1;
                long temp;
                temp = Double.doubleToLongBits(this.value);
                result = prime * result + (int) (temp ^ (temp >>> 32));
                return result;
            }

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
                ByValue other = (ByValue) obj;
                if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value))
                {
                    return false;
                }
                return true;
            }

        }

        /**
         * Road position based on desired speed.
         */
        class BySpeed implements RoadPosition
        {

            /** Desired speed at left side of the road. */
            private Speed leftSpeed;

            /** Desired speed at the right side of the road. */
            private Speed rightSpeed;

            /**
             * Constructor.
             * @param leftSpeed desired speed at left side of the road
             * @param rightSpeed desired speed at right side of the road
             */
            public BySpeed(final Speed leftSpeed, final Speed rightSpeed)
            {
                Throw.when(leftSpeed.eq(rightSpeed), IllegalArgumentException.class,
                        "Left speed and right speed may not be equal. Use LaneBias.NONE.");
                this.leftSpeed = leftSpeed;
                this.rightSpeed = rightSpeed;
            }

            @Override
            public double getValue(final Speed desiredSpeed)
            {
                Throw.whenNull(desiredSpeed, "Peeked desired speed from a strategical planner factory is null, "
                        + "while a lane bias depends on desired speed.");
                double value = (desiredSpeed.si - this.rightSpeed.si) / (this.leftSpeed.si - this.rightSpeed.si);
                return value < 0.0 ? 0.0 : (value > 1.0 ? 1.0 : value);
            }

            @Override
            public int hashCode()
            {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((this.leftSpeed == null) ? 0 : this.leftSpeed.hashCode());
                result = prime * result + ((this.rightSpeed == null) ? 0 : this.rightSpeed.hashCode());
                return result;
            }

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
                BySpeed other = (BySpeed) obj;
                if (this.leftSpeed == null)
                {
                    if (other.leftSpeed != null)
                    {
                        return false;
                    }
                }
                else if (!this.leftSpeed.equals(other.leftSpeed))
                {
                    return false;
                }
                if (this.rightSpeed == null)
                {
                    if (other.rightSpeed != null)
                    {
                        return false;
                    }
                }
                else if (!this.rightSpeed.equals(other.rightSpeed))
                {
                    return false;
                }
                return true;
            }

        }

    }

}
