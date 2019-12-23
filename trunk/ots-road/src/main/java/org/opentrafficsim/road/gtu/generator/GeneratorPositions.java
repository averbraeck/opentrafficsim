package org.opentrafficsim.road.gtu.generator;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.RoadPosition.BySpeed;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.RoadPosition.ByValue;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Helper class for vehicle generation which can draw the next GTU position to try to place a GTU. If the GTU can not be placed,
 * it should be included in a queue. This class requires the number of unplaced GTU's per lane, in order to appropriately divide
 * traffic over the lanes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class GeneratorPositions implements Locatable
{

    /** Underlying object representing the zone. */
    private final GeneratorZonePosition position;

    /** Lane biases per GTU type. */
    private final LaneBiases biases;

    /** Stream for random numbers. */
    private final StreamInterface stream;

    /** Location. */
    private final DirectedPoint location;

    /** Bounds. */
    private final Bounds bounds;

    /** Set of all positions. */
    private final Set<GeneratorLanePosition> allPositions = new LinkedHashSet<>();

    /**
     * Constructor. Private to facilitate easier creation methods using static factories, and to hide underlying classes.
     * @param position GeneratorZonePosition; underlying object representing the zone
     * @param biases LaneBiases; lane biases for GTU types
     * @param stream StreamInterface; stream for random numbers
     */
    @SuppressWarnings("synthetic-access")
    private GeneratorPositions(final GeneratorZonePosition position, final LaneBiases biases, final StreamInterface stream)
    {
        this.position = position;
        for (GeneratorLinkPosition glp : this.position.positions)
        {
            if (glp.getDirection().equals(GTUDirectionality.DIR_MINUS))
            {
                System.out.println("Hmm... GTUDirectionality is DIR_MINUS: " + glp);
            }
        }
        this.biases = biases;
        this.stream = stream;
        double x = 0.0;
        double y = 0.0;
        double xMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        int n = 0;
        for (GeneratorLinkPosition linkPosition : position.positions)
        {
            for (GeneratorLanePosition lanePosition : linkPosition.positions)
            {
                this.allPositions.add(lanePosition);
                for (DirectedLanePosition pos : lanePosition.getPosition())
                {
                    DirectedPoint point;
                    try
                    {
                        point = pos.getLane().getCenterLine().getLocation(pos.getPosition());
                    }
                    catch (OTSGeometryException exception)
                    {
                        point = new DirectedPoint(0, 0, 0);
                    }
                    x += point.x;
                    y += point.y;
                    xMin = xMin < point.x ? xMin : point.x;
                    yMin = yMin < point.y ? yMin : point.y;
                    xMax = xMax > point.x ? xMax : point.x;
                    yMax = yMax > point.y ? yMax : point.y;
                    n++;
                }
            }
        }
        this.location = new DirectedPoint(x / n, y / n, 0);
        this.bounds = new BoundingBox(new Point3d(xMin, yMin, 0.0), new Point3d(xMax, yMax, 0.0));
    }

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link. Lanes are drawn
     * without bias. Each link receives a weight equal to the number of lanes.
     * @param positions Set&lt;DirectedLanePosition&gt;; all considered positions, each lane is considered separately
     * @param stream StreamInterface; stream for random numbers
     * @return GeneratorPositions; object to draw positions from
     */
    public static GeneratorPositions create(final Set<DirectedLanePosition> positions, final StreamInterface stream)
    {
        return create(positions, stream, null, null);
    }

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link. Each link receives a
     * weight equal to the number of lanes.
     * @param positions Set&lt;DirectedLanePosition&gt;; all considered positions, each lane is considered separately
     * @param stream StreamInterface; stream for random numbers
     * @param biases LaneBiases; lane biases for GTU types
     * @return GeneratorPositions; object to draw positions from
     */
    public static GeneratorPositions create(final Set<DirectedLanePosition> positions, final StreamInterface stream,
            final LaneBiases biases)
    {
        return create(positions, stream, biases, null);
    }

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link. Lanes are drawn
     * without bias.
     * @param positions Set&lt;DirectedLanePosition&gt;; all considered positions, each lane is considered separately
     * @param stream StreamInterface; stream for random numbers
     * @param linkWeights Map&lt;CrossSectionLink, Double&gt;; weight per link direction
     * @return GeneratorPositions; object to draw positions from
     */
    public static GeneratorPositions create(final Set<DirectedLanePosition> positions, final StreamInterface stream,
            final Map<CrossSectionLink, Double> linkWeights)
    {
        return create(positions, stream, null, linkWeights);
    }

    /**
     * Create a GeneratorPositions object to draw positions from. The given positions are grouped per link.
     * @param positions Set&lt;DirectedLanePosition&gt;; all considered positions, each lane is considered separately
     * @param stream StreamInterface; stream for random numbers
     * @param biases LaneBiases; lane biases for GTU types
     * @param linkWeights Map&lt;CrossSectionLink, Double&gt;; weight per link direction
     * @return GeneratorPositions; object to draw positions from
     */
    public static GeneratorPositions create(final Set<DirectedLanePosition> positions, final StreamInterface stream,
            final LaneBiases biases, final Map<CrossSectionLink, Double> linkWeights)
    {

        // group directions per link
        Map<LinkDirection, Set<DirectedLanePosition>> linkSplit = new LinkedHashMap<>();
        for (DirectedLanePosition position : positions)
        {
            if (!linkSplit.containsKey(position.getLinkDirection()))
            {
                linkSplit.put(position.getLinkDirection(), new LinkedHashSet<>());
            }
            linkSplit.get(position.getLinkDirection()).add(position);
        }

        // create list of GeneratorLinkPositions
        List<GeneratorLinkPosition> linkPositions = new ArrayList<>();
        for (LinkDirection linkDirection : linkSplit.keySet())
        {
            List<Lane> lanes = ((CrossSectionLink) linkDirection.getLink()).getLanes();
            // let's sort the lanes by lateral position
            Collections.sort(lanes, new Comparator<Lane>()
            {
                /** {@inheritDoc} */
                @Override
                public int compare(final Lane lane1, final Lane lane2)
                {
                    Length lat1 = linkDirection.getDirection().isPlus() ? lane1.getDesignLineOffsetAtBegin()
                            : lane1.getDesignLineOffsetAtEnd().neg();
                    Length lat2 = linkDirection.getDirection().isPlus() ? lane2.getDesignLineOffsetAtBegin()
                            : lane2.getDesignLineOffsetAtEnd().neg();
                    return lat1.compareTo(lat2);
                }
            });
            // create list of GeneratorLanePositions
            List<GeneratorLanePosition> lanePositions = new ArrayList<>();
            for (DirectedLanePosition lanePosition : linkSplit.get(linkDirection))
            {
                Set<DirectedLanePosition> set = new LinkedHashSet<>();
                set.add(lanePosition);
                lanePositions.add(new GeneratorLanePosition(lanes.indexOf(lanePosition.getLane()) + 1, set,
                        (CrossSectionLink) linkDirection.getLink()));
            }
            // create the GeneratorLinkPosition
            CrossSectionLink link = (CrossSectionLink) linkDirection.getLink();
            if (linkWeights == null)
            {
                linkPositions.add(new GeneratorLinkPosition(lanePositions, link));
            }
            else
            {
                Double weight = linkWeights.get(link);
                Throw.whenNull(weight, "Using link weights for GTU generation, but no weight for link %s is defined.", link);
                linkPositions.add(new GeneratorLinkPosition(lanePositions, link, weight));
            }
        }

        // create the GeneratorZonePosition
        return new GeneratorPositions(new GeneratorZonePosition(linkPositions), biases, stream);

    }

    /**
     * Draw a new position to generate a GTU. The link is drawn by giving each link a weight equal to the number of accessible
     * lanes for the GTU type. Next, a lane is drawn using (optionally biased) weights.
     * @param gtuType GTUType; GTU type
     * @param unplaced Map&lt;CrossSectionLink, Map&lt;Integer, Integer&gt;&gt;; number of unplaced GTUs per lane. The lane
     *            number should match with {@code GeneratorLanePosition.getLaneNumber()}, where 1 is the right-most lane.
     *            Missing lanes are assumed to have no queue.
     * @param desiredSpeed Speed; desired speed, possibly used to determine the biased road position
     * @param route Route; route, may be {@code null}
     * @return GeneratorLanePosition; new position to generate a GTU
     */
    public GeneratorLanePosition draw(final GTUType gtuType, final Map<CrossSectionLink, Map<Integer, Integer>> unplaced,
            final Speed desiredSpeed, final Route route)
    {
        return this.position.draw(gtuType, this.stream, this.biases, unplaced, desiredSpeed, route);
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return this.bounds;
    }

    /**
     * Returns all underlying positions.
     * @return all underlying positions
     */
    public Set<GeneratorLanePosition> getAllPositions()
    {
        return this.allPositions;
    }

    /**
     * Returns the speed limit for the given GTU type, prior to the GTU position being determined.
     * @param gtuType GTUType; GTU type
     * @return speed limit for the given GTU type, prior to the GTU position being determined
     */
    public Speed speedLimit(final GTUType gtuType)
    {
        Speed speedLimit = null;
        for (GeneratorLanePosition pos : this.allPositions)
        {
            for (DirectedLanePosition lane : pos.getPosition())
            {
                try
                {
                    Speed limit = lane.getLane().getSpeedLimit(gtuType);
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
        }
        Throw.when(speedLimit == null, IllegalStateException.class, "No speed limit could be determined for GTUType %s.",
                gtuType);
        return speedLimit;
    }

    /**
     * Class representing a vehicle generation lane, providing elementary information for randomly drawing links and lanes.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static final class GeneratorLanePosition
    {

        /** Lane number, where 1 is the right-most lane. */
        private final int laneNumber;

        /** Position set, representing a single GTU position on the network. */
        private final Set<DirectedLanePosition> position;

        /** Link. */
        private final CrossSectionLink link;

        /**
         * Constructor.
         * @param laneNumber int; lane number, where 1 is the right-most lane
         * @param position Set&lt;DirectedLanePosition&gt;; position set, representing a single GTU position on the network
         * @param link CrossSectionLink; link
         */
        GeneratorLanePosition(final int laneNumber, final Set<DirectedLanePosition> position, final CrossSectionLink link)
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
         * @param gtuType GTUType; gtu type
         * @return boolean; whether this lane is accessible to the GTU type
         */
        boolean allows(final GTUType gtuType)
        {
            for (DirectedLanePosition pos : this.position)
            {
                if (pos.getLane().getLaneType().isCompatible(gtuType, pos.getGtuDirection()))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns the contained position set, representing a single GTU position on the network.
         * @return Set&lt;DirectedLanePosition&gt;; contained position set, representing a single GTU position on the network
         */
        Set<DirectedLanePosition> getPosition()
        {
            return this.position;
        }

        /**
         * Returns the link.
         * @return CrossSectionLink; link
         */
        CrossSectionLink getLink()
        {
            return this.link;
        }

        /**
         * Returns the direction of travel.
         * @return GTUDirectionality; direction of travel
         */
        GTUDirectionality getDirection()
        {
            return this.position.iterator().next().getGtuDirection();
        }

        /** {@inheritDoc} */
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
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static final class GeneratorLinkPosition
    {

        /** Contained lanes. */
        private final List<GeneratorLanePosition> positions;

        /** The link. */
        private final CrossSectionLink link;

        /** Weight for drawing this link. */
        private final double weight;

        /**
         * Constructor.
         * @param positions List&lt;GeneratorLanePosition&gt;; contained lanes
         * @param link CrossSectionLink; the link
         */
        GeneratorLinkPosition(final List<GeneratorLanePosition> positions, final CrossSectionLink link)
        {
            this.positions = positions;
            this.link = link;
            this.weight = -1;
        }

        /**
         * Constructor.
         * @param positions List&lt;GeneratorLanePosition&gt;; contained lanes
         * @param link CrossSectionLink; the link
         * @param weight double; weight for drawing this link
         */
        GeneratorLinkPosition(final List<GeneratorLanePosition> positions, final CrossSectionLink link, final double weight)
        {
            this.positions = positions;
            this.link = link;
            this.weight = weight;
        }

        /**
         * Return the link.
         * @return CrossSectionLink; link
         */
        CrossSectionLink getLink()
        {
            return this.link;
        }

        /**
         * Returns the weight for this link. This is either a predefined weight, or the number of lanes for the GTU type.
         * @param gtuType GTUType; GTU type
         * @return double; weight for this link
         */
        double getWeight(final GTUType gtuType)
        {
            if (this.weight < 0.0)
            {
                return getNumberOfLanes(gtuType);
            }
            return this.weight;
        }

        /**
         * Returns the number of accessible lanes for the GTU type.
         * @param gtuType GTUType; GTU type
         * @return int; number of accessible lanes for the GTU type
         */
        int getNumberOfLanes(final GTUType gtuType)
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
         * @param gtuType GTUType; GTU type
         * @param stream StreamInterface; stream for random numbers
         * @param biases LaneBiases; biases for GTU types
         * @param unplaced Map&lt;Integer, Integer&gt;; number of unplaced GTUs per lane. The lane number should match with
         *            {@code GeneratorLanePosition.getLaneNumber()}, where 1 is the right-most lane. Missing lanes are assumed
         *            to have no queue.
         * @param desiredSpeed Speed; desired speed, possibly used to determine the biased road position
         * @return GeneratorLanePosition; specific GeneratorLanePosition utilizing lane biases of GTU types
         */
        GeneratorLanePosition draw(final GTUType gtuType, final StreamInterface stream, final LaneBiases biases,
                final Map<Integer, Integer> unplaced, final Speed desiredSpeed)
        {
            Map<GeneratorLanePosition, Double> map = new LinkedHashMap<>();
            // double[] cumulWeights = new double[this.positions.size()];
            // double totalWeight = 0.0;
            for (int i = 0; i < this.positions.size(); i++)
            {
                GeneratorLanePosition lanePosition = this.positions.get(i);
                if (lanePosition.allows(gtuType))
                {
                    GTUType type = gtuType;
                    boolean found = false;
                    while (biases != null && !found && type != null)
                    {
                        if (biases.contains(type))
                        {
                            found = true;
                            int laneNum = lanePosition.getLaneNumber();
                            int unplacedTemplates = unplaced == null ? 0 : unplaced.getOrDefault(laneNum, 0);
                            double w = biases.getBias(type).calculateWeight(laneNum, getNumberOfLanes(gtuType),
                                    unplacedTemplates, desiredSpeed);
                            map.put(lanePosition, w);
                            // totalWeight += w;
                        }
                        type = type.getParent();
                    }
                    if (!found)
                    {
                        map.put(lanePosition, 1.0);
                        // totalWeight += 1.0; // no bias for this GTU type
                    }
                    // cumulWeights[i] = totalWeight;
                }
            }
            if (0 == map.size())
            {
                System.err.println("This really, really can't work...");
            }
            return Draw.drawWeighted(map, stream);
            // double r = totalWeight * stream.nextDouble();
            // for (int i = 0; i < this.positions.size(); i++)
            // {
            // if (r <= cumulWeights[i])
            // {
            // return this.positions.get(i);
            // }
            // }
            // return this.positions.get(this.positions.size() - 1);
        }

        /**
         * Returns the direction of travel.
         * @return GTUDirectionality; direction of travel
         */
        GTUDirectionality getDirection()
        {
            return this.positions.get(0).getDirection();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "GeneratorLinkPosition [positions=" + this.positions + "]";
        }

    }

    /**
     * Class representing a vehicle generation zone to provide individual generation positions.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static final class GeneratorZonePosition
    {

        /** Contained links. */
        private final List<GeneratorLinkPosition> positions;

        /**
         * Constructor.
         * @param positions List<GeneratorLinkPosition>; contained links
         */
        GeneratorZonePosition(final List<GeneratorLinkPosition> positions)
        {
            this.positions = positions;
        }
        
        /**
         * Draws a GeneratorLinkPosition using number of accessible lanes for the GTUType as weight, and a GeneratorLanePosition
         * from that.
         * @param gtuType GTUType; GTU type
         * @param stream StreamInterface; stream for random numbers
         * @param biases LaneBiases; biases for GTU types
         * @param unplaced Map&lt;CrossSectionLink, Map&lt;Integer, Integer&gt;&gt;; number of unplaced GTUs per lane. The lane
         *            number should match with {@code GeneratorLanePosition.getLaneNumber()}, where 1 is the right-most lane.
         *            Missing lanes are assumed to have no queue.
         * @param desiredSpeed Speed; desired speed, possibly used to determine the biased road position
         * @param route Route; route, may be {@code null}
         * @return GeneratorLanePosition; draws a LinkPosition using number of accessible lanes for the GTUType as weight, and a
         *         GeneratorLanePosition from that
         */
        GeneratorLanePosition draw(final GTUType gtuType, final StreamInterface stream, final LaneBiases biases,
                final Map<CrossSectionLink, Map<Integer, Integer>> unplaced, final Speed desiredSpeed, final Route route)
        {
            Map<GeneratorLinkPosition, Double> map = new LinkedHashMap<>();
            // double[] cumulWeights = new double[this.positions.size()];
            // double totalWeight = 0.0;
            for (int i = 0; i < this.positions.size(); i++)
            {
                Link link = this.positions.get(i).getLink();
                GTUDirectionality direction = this.positions.get(i).getDirection();
                if (route != null)
                {
                    int from = route.indexOf(direction.isPlus() ? link.getStartNode() : link.getEndNode());
                    int to = route.indexOf(direction.isPlus() ? link.getEndNode() : link.getStartNode());
                    if (from > -1 && to > -1 && to - from == 1)
                    {
                        map.put(this.positions.get(i), this.positions.get(i).getWeight(gtuType));
                        // totalWeight += this.positions.get(i).getWeight(gtuType); // else, no weight
                    }
                }
                else
                {
                    map.put(this.positions.get(i), this.positions.get(i).getWeight(gtuType));
                    // totalWeight += this.positions.get(i).getWeight(gtuType); // no route, consider all locations
                }
                // cumulWeights[i] = totalWeight;
            }

            GeneratorLinkPosition linkPosition = Draw.drawWeighted(map, stream);
            return linkPosition.draw(gtuType, stream, biases, unplaced.get(linkPosition.getLink()), desiredSpeed);

            // double r = totalWeight * stream.nextDouble();
            // for (int i = 0; i < this.positions.size(); i++)
            // {
            // if (r <= cumulWeights[i])
            // {
            // GeneratorLinkPosition position = this.positions.get(i);
            // return position.draw(gtuType, stream, biases, unplaced.get(position.getLink()), desiredSpeed);
            // }
            // }
            // GeneratorLinkPosition position = this.positions.get(this.positions.size() - 1);
            // return position.draw(gtuType, stream, biases, unplaced.get(position.getLink()), desiredSpeed);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "GeneratorZonePosition [positions=" + this.positions + "]";
        }

    }

    /**
     * Set of lane biases per GTU type.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static final class LaneBiases
    {

        /** Biases per GTU type. */
        private final Map<GTUType, LaneBias> biases = new LinkedHashMap<>();

        /**
         * Adds a GTU bias for randomly drawing a lane.
         * @param gtuType GTUType; gtu type
         * @param bias LaneBias; bias
         * @return LaneBiases; lane biases for method chaining
         */
        public LaneBiases addBias(final GTUType gtuType, final LaneBias bias)
        {
            Throw.whenNull(gtuType, "GTU type may not be null.");
            Throw.whenNull(bias, "Bias may not be null.");
            this.biases.put(gtuType, bias);
            return this;
        }

        /**
         * Whether a bias is defined for the given type.
         * @param gtuType GTUType; GTU type
         * @return whether a bias is defined for the given type
         */
        public boolean contains(final GTUType gtuType)
        {
            return this.biases.containsKey(gtuType);
        }

        /**
         * Returns the bias of given GTU type, or {@code Bias.None} if none defined for the GTU type.
         * @param gtuType GTUType; GTU type
         * @return Bias; bias of the GTU type
         */
        public LaneBias getBias(final GTUType gtuType)
        {
            return this.biases.getOrDefault(gtuType, LaneBias.NONE);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "LaneBiases [" + this.biases + "]";
        }

    }

    /**
     * Set of lane biases per GTU type enum, based on the GTU Types that are defined by default.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static final class LaneBiasDefaults
    {
        /** Biases per GTU type. */
        private final EnumMap<GTUType.DEFAULTS, LaneBias> biases = new EnumMap<>(GTUType.DEFAULTS.class);

        /**
         * Adds a GTU bias for randomly drawing a lane.
         * @param gtuEnum GTUType.DEFAULTS; gtu type
         * @param bias LaneBias; bias
         * @return LaneBiases; lane biases for method chaining
         */
        public LaneBiasDefaults addBias(final GTUType.DEFAULTS gtuEnum, final LaneBias bias)
        {
            Throw.whenNull(gtuEnum, "GTU type enum may not be null.");
            Throw.whenNull(bias, "Bias may not be null.");
            this.biases.put(gtuEnum, bias);
            return this;
        }

        /**
         * Whether a bias is defined for the given type.
         * @param gtuEnum GTUType; GTU type enum
         * @return whether a bias is defined for the given type
         */
        public boolean contains(final GTUType.DEFAULTS gtuEnum)
        {
            return this.biases.containsKey(gtuEnum);
        }

        /**
         * Returns the bias of given GTU type, or {@code Bias.None} if none defined for the GTU type.
         * @param gtuEnum GTUType.DEFAULTS; GTU type enum
         * @return Bias; bias of the GTU type
         */
        public LaneBias getBias(final GTUType.DEFAULTS gtuEnum)
        {
            return this.biases.getOrDefault(gtuEnum, LaneBias.NONE);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "LaneBiases [" + this.biases + "]";
        }
    }

    /**
     * Vehicle generation lateral bias. Includes a lane maximum, e.g. trucks only on 2 right-hand lanes.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static final class LaneBias
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
         * @param leftSpeed Speed; desired speed for full left bias
         * @param rightSpeed Speed; desired speed for full right bias
         * @return bias by speed with normal extent
         */
        public static LaneBias bySpeed(final Speed leftSpeed, final Speed rightSpeed)
        {
            return new LaneBias(new BySpeed(leftSpeed, rightSpeed), 2.0, Integer.MAX_VALUE);
        }

        /**
         * Returns a bias by speed with normal extent. Convenience km/h input.
         * @param leftSpeedKm double; desired speed for full left bias
         * @param rightSpeedKm double; desired speed for full right bias
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
         * @param roadPosition RoadPosition; lateral position on the road (0 = right, 0.5 = middle, 1 = left)
         * @param bias double; bias extent, lower values create more spread traffic, 0.0 causes no lane preference
         * @param stickyLanes double; number of lanes to consider in either direction, including the preferred lane
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
         * @param laneNumFromRight int; number of lane counted from right to left
         * @param numberOfLanes int; total number of lanes
         * @param numberOfUnplacedGTUs int; number of GTU's in the generation queue
         * @param desiredSpeed Speed; desired speed, possibly used to determine the biased road position
         * @return double; random draw weight for given lane
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

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 15 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface RoadPosition
    {

        /**
         * Returns the road position (0.0 = right, 1.0 = left).
         * @param desiredSpeed Speed; desired speed at the generator
         * @return road position (0.0 = right, 1.0 = left)
         */
        double getValue(Speed desiredSpeed);

        /**
         * Fixed road position.
         * <p>
         * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
         * <p>
         * @version $Revision$, $LastChangedDate$, by $Author$, initial version 15 jan. 2018 <br>
         * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
         * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
         * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
         */
        public class ByValue implements RoadPosition
        {

            /** Road position. */
            private double value;

            /**
             * Constructor.
             * @param value double; road position
             */
            public ByValue(final double value)
            {
                Throw.when(value < 0.0 || value > 1.0, IllegalArgumentException.class,
                        "Road position value should be in the range [0...1].");
                this.value = value;
            }

            /** {@inheritDoc} */
            @Override
            public double getValue(final Speed desiredSpeed)
            {
                return this.value;
            }

            /** {@inheritDoc} */
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
         * <p>
         * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
         * <p>
         * @version $Revision$, $LastChangedDate$, by $Author$, initial version 15 jan. 2018 <br>
         * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
         * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
         * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
         */
        public class BySpeed implements RoadPosition
        {

            /** Desired speed at left side of the road. */
            private Speed leftSpeed;

            /** Desired speed at the right side of the road. */
            private Speed rightSpeed;

            /**
             * Constructor.
             * @param leftSpeed Speed; desired speed at left side of the road
             * @param rightSpeed Speed; desired speed at right side of the road
             */
            public BySpeed(final Speed leftSpeed, final Speed rightSpeed)
            {
                Throw.when(leftSpeed.eq(rightSpeed), IllegalArgumentException.class,
                        "Left speed and right speed may not be equal. Use LaneBias.NONE.");
                this.leftSpeed = leftSpeed;
                this.rightSpeed = rightSpeed;
            }

            /** {@inheritDoc} */
            @Override
            public double getValue(final Speed desiredSpeed)
            {
                Throw.whenNull(desiredSpeed,
                        "Peeked desired speed from a strategical planner factory is null, while a lane bias depends on desired speed.");
                double value = (desiredSpeed.si - this.rightSpeed.si) / (this.leftSpeed.si - this.rightSpeed.si);
                return value < 0.0 ? 0.0 : (value > 1.0 ? 1.0 : value);
            }

            /** {@inheritDoc} */
            @Override
            public int hashCode()
            {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((this.leftSpeed == null) ? 0 : this.leftSpeed.hashCode());
                result = prime * result + ((this.rightSpeed == null) ? 0 : this.rightSpeed.hashCode());
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
