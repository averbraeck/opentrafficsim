package org.opentrafficsim.road.od;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Connector;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.headway.Arrivals;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.gtu.generator.headway.DemandPattern;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.detector.DestinationDetector;
import org.opentrafficsim.road.network.lane.object.detector.DetectorType;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Utility to create vehicle generators on a network from an OD.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class OdApplier
{

    /**
     * Utility class.
     */
    private OdApplier()
    {
        //
    }

    /**
     * Applies the OD to the network by creating vehicle generators. The map returned contains objects created for vehicle
     * generation. These are bundled in a {@code GeneratorObjects} and mapped to the vehicle generator id. Vehicle generator id
     * is equal to the origin node id. For lane-based generators the id's are appended with an ordered number (e.g. A1), where
     * the ordering is first by link id, and then right to left concerning the lateral lane position at the start of the lane.
     * For node "A" this would for example be:<br>
     * <table >
     * <caption>&nbsp;</caption>
     * <tr>
     * <th>Generator id</th>
     * <th>Link</th>
     * <th>Lateral start offset</th>
     * </tr>
     * <tr>
     * <th>A1</th>
     * <th>AB</th>
     * <th>-1.75m</th>
     * </tr>
     * <tr>
     * <th>A2</th>
     * <th>AB</th>
     * <th>1.75m</th>
     * </tr>
     * <tr>
     * <th>A3</th>
     * <th>AC</th>
     * <th>-3.5m</th>
     * </tr>
     * <tr>
     * <th>A4</th>
     * <th>AC</th>
     * <th>0.0m</th>
     * </tr>
     * </table>
     * If the GTU generation is lane-based (i.e. {@code Lane} in the {@code Categorization}) this method creates a
     * {@code LaneBasedGtuGenerator} per lane. It will have a single source of demand data, specifying demand towards all
     * relevant destinations, and with a unique {@code MarkovChain} for the GTU type if {@code MarkovCorrelation} is defined.
     * For zone GTU generation one {@code LaneBasedGtuGenerator} is created, with one single source of demand data, specifying
     * demand towards all destinations. A single {@code MarkovChain} may be used. Traffic is distributed over possible
     * {@code Connectors} based on their link-weight, or the number of lanes of the connected links if no weight is given.
     * @param network OtsRoadNetwork; network
     * @param od OdMatrix; OD matrix
     * @param odOptions OdOptions; options for vehicle generation
     * @param detectorType DetectorType; detector type.
     * @return Map&lt;String, GeneratorObjects&gt; map of generator id's and created generator objects mainly for testing
     * @throws ParameterException if a parameter is missing
     * @throws SimRuntimeException if this method is called after simulation time 0
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static Map<String, GeneratorObjects> applyOd(final OtsRoadNetwork network, final OdMatrix od,
            final OdOptions odOptions, final DetectorType detectorType) throws ParameterException, SimRuntimeException
    {
        Throw.whenNull(network, "Network may not be null.");
        Throw.whenNull(od, "OD matrix may not be null.");
        Throw.whenNull(odOptions, "OD options may not be null.");
        OtsSimulatorInterface simulator = network.getSimulator();
        Throw.when(!simulator.getSimulatorTime().eq0(), SimRuntimeException.class,
                "Method OdApplier.applyOd() should be invoked at simulation time 0.");

        // TODO sinks? white extension links?
        for (Node destination : od.getDestinations())
        {
            createSensorsAtDestination(destination, simulator, detectorType);
        }

        // TODO clean up stream acquiring code after task OTS-315 has been completed
        StreamInterface stream = getStream(simulator);

        boolean laneBased = od.getCategorization().entails(Lane.class);
        Map<String, GeneratorObjects> output = new LinkedHashMap<>();
        for (Node origin : od.getOrigins())
        {
            Map<Lane, DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>> originNodePerLane = new LinkedHashMap<>();
            DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> originNodeZone =
                    buildDemandNodeTree(od, odOptions, stream, origin, originNodePerLane);
            Map<DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>, Set<LanePosition>> initialPositions =
                    new LinkedHashMap<>();
            Map<CrossSectionLink, Double> linkWeights = new LinkedHashMap<>();
            Map<CrossSectionLink, Node> viaNodes = new LinkedHashMap<>();
            if (laneBased)
            {
                gatherPositionsLaneBased(originNodePerLane, initialPositions);
            }
            else
            {
                initialPositions.put(originNodeZone, gatherPositionsZone(origin, linkWeights, viaNodes));
            }
            if (linkWeights.isEmpty())
            {
                linkWeights = null;
                viaNodes = null;
            }
            initialPositions = sortByValue(initialPositions); // sorts by lateral position at link start
            createGenerators(network, odOptions, simulator, laneBased, stream, output, initialPositions, linkWeights, viaNodes);
        }
        return output;
    }

    /**
     * Builds nested demand node structure (i.e. tree) for demand and GTU characteristics generation. If
     * {@code MarkovCorrelation} is specified, in case of zone GTU generation, a single {@code MarkovChain} is used for the
     * selection of GTU type and the relevant lane. In case of lane-based GTU generation, one {@code MarkovChain} is used for
     * each lane, even when multiple {@code Category}'s contain the same lane. This method loops over all destinations for the
     * given origin, and then over all categories. For lane-based GTU generation, at that level the appropriate origin node is
     * taken from the input map, or it is created in to it, and the destination demand-node coupled to that for the looped
     * destination is obtained or created, with possible {@code MarkovChain}. For zone GTU generation, the looping is a more
     * straight-forward creation of nodes from origin, and for destination and category. The result of lane-based GTU generation
     * is given in the input map, for zone GTU generation the single origin demand node is returned by the method.
     * @param od OdMatrix; OD matrix.
     * @param odOptions OdOptions; OD options.
     * @param stream StreamInterface; random number stream.
     * @param origin Node; origin node.
     * @param originNodePerLane Map&lt;Lane, DemandNode&lt;Node, DemandNode&lt;Node, DemandNode&lt;Category, ?&gt;&gt;&gt;&gt;;
     *            map of origin demand node per lane, populated for lane-based GTU generation.
     * @return DemandNode&lt;Node, DemandNode&lt;Node, DemandNode&lt;Category, ?&gt;&gt;&gt;; demand node structure for the
     *         entire generator in case of zone GTU generation.
     */
    private static DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> buildDemandNodeTree(final OdMatrix od,
            final OdOptions odOptions, final StreamInterface stream, final Node origin,
            final Map<Lane, DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>> originNodePerLane)
    {
        boolean laneBased = od.getCategorization().entails(Lane.class);
        boolean markovian = od.getCategorization().entails(GtuType.class);
        DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> demandNode = null; // for each generator, flexibly used
        MarkovChain markovChain = null;
        if (!laneBased)
        {
            demandNode = new DemandNode<>(origin, stream, null);
            LinkType linkType = getLinkTypeFromNode(origin);
            if (markovian)
            {
                MarkovCorrelation<GtuType, Frequency> correlation = odOptions.get(OdOptions.MARKOV, null, origin, linkType);
                if (correlation != null)
                {
                    Throw.when(!od.getCategorization().entails(GtuType.class), IllegalArgumentException.class,
                            "Markov correlation can only be used on OD categorization entailing GTU type.");
                    markovChain = new MarkovChain(correlation);
                }
            }
        }
        for (Node destination : od.getDestinations())
        {
            Set<Category> categories = od.getCategories(origin, destination);
            if (!categories.isEmpty())
            {
                DemandNode<Node, DemandNode<Category, ?>> destinationNode = null;
                if (!laneBased)
                {
                    destinationNode = new DemandNode<>(destination, stream, markovChain);
                    demandNode.addChild(destinationNode);
                }
                for (Category category : categories)
                {
                    if (laneBased)
                    {
                        // obtain or create root and destination nodes
                        Lane lane = category.get(Lane.class);
                        demandNode = originNodePerLane.get(lane);
                        if (demandNode == null)
                        {
                            demandNode = new DemandNode<>(origin, stream, null);
                            originNodePerLane.put(lane, demandNode);
                        }
                        destinationNode = demandNode.getChild(destination);
                        if (destinationNode == null)
                        {
                            markovChain = null;
                            if (markovian)
                            {
                                MarkovCorrelation<GtuType, Frequency> correlation =
                                        odOptions.get(OdOptions.MARKOV, lane, origin, lane.getParentLink().getType());
                                if (correlation != null)
                                {
                                    Throw.when(!od.getCategorization().entails(GtuType.class), IllegalArgumentException.class,
                                            "Markov correlation can only be used on OD categorization entailing GTU type.");
                                    markovChain = new MarkovChain(correlation); // 1 for each generator per lane
                                }
                            }
                            destinationNode = new DemandNode<>(destination, stream, markovChain);
                            demandNode.addChild(destinationNode);
                        }
                    }
                    DemandNode<Category, ?> categoryNode =
                            new DemandNode<>(category, od.getDemandPattern(origin, destination, category));
                    if (markovian)
                    {
                        destinationNode.addLeaf(categoryNode, category.get(GtuType.class));
                    }
                    else
                    {
                        destinationNode.addChild(categoryNode);
                    }
                }
            }
        }
        return demandNode;
    }

    /**
     * Returns a set of positions for GTU generation from each lane defined in demand. Stores the positions with the coupled
     * demand node.
     * @param originNodePerLane Map&lt;Lane, DemandNode&lt;Node, DemandNode&lt;Node, DemandNode&lt;Category, ?&gt;&gt;&gt;&gt;;
     *            map with a demand node per lane.
     * @param initialPositions Map&lt;DemandNode&lt;Node, DemandNode&lt;Node, DemandNode&lt;Category, ?&gt;&gt;&gt;,
     *            Set&lt;LanePosition&gt;&gt;; map with positions per demand node.
     */
    private static void gatherPositionsLaneBased(
            final Map<Lane, DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>> originNodePerLane,
            final Map<DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>, Set<LanePosition>> initialPositions)
    {
        for (Lane lane : originNodePerLane.keySet())
        {
            DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> demandNode = originNodePerLane.get(lane);
            Set<LanePosition> initialPosition = new LinkedHashSet<>();
            initialPosition.add(lane.getParentLink().getStartNode().equals(demandNode.getObject())
                    ? new LanePosition(lane, Length.ZERO) : new LanePosition(lane, lane.getLength()));
            initialPositions.put(demandNode, initialPosition);
        }
    }

    /**
     * Returns a set of positions for GTU generation from a zone. All links connected to the origin node are considered. In case
     * a link is a {@code Connector}, a link weight and via-node over that link are stored in the provided maps, for later use
     * in constructing weighted generator positions. For each {@code CrossSectionLink} attached to the via-node, or to the first
     * link if there was no {@code Connector}, positions are generated.
     * @param origin Node; origin node for the zone.
     * @param linkWeights Map&lt;CrossSectionLink, Double&gt;; link weight map to place link weights in.
     * @param viaNodes Map&lt;CrossSectionLink, Node&gt;; via node map to place via nodes in.
     * @return Set&lt;LanePosition&gt;; gathered lane positions.
     */
    private static Set<LanePosition> gatherPositionsZone(final Node origin, final Map<CrossSectionLink, Double> linkWeights,
            final Map<CrossSectionLink, Node> viaNodes)
    {
        Set<LanePosition> positionSet = new LinkedHashSet<>();
        for (Link link : origin.getLinks())
        {
            if (link instanceof Connector)
            {
                Connector connector = (Connector) link;
                if (connector.getStartNode().equals(origin))
                {
                    Node connectedNode = connector.getEndNode();
                    // count number of served links
                    int served = 0;
                    for (Link connectedLink : connectedNode.getLinks())
                    {
                        if (connectedLink instanceof CrossSectionLink)
                        {
                            served++;
                        }
                    }
                    for (Link connectedLink : connectedNode.getLinks())
                    {
                        if (connectedLink instanceof CrossSectionLink)
                        {
                            if (connector.getDemandWeight() > 0.0)
                            {
                                // store weight under connected link, as this
                                linkWeights.put(((CrossSectionLink) connectedLink), connector.getDemandWeight() / served);
                            }
                            else
                            {
                                // negative weight results in number of lanes being used
                                linkWeights.put(((CrossSectionLink) connectedLink), -1.0);
                            }
                            viaNodes.put((CrossSectionLink) connectedLink, connectedNode);
                            setLanePosition((CrossSectionLink) connectedLink, connectedNode, positionSet);
                        }
                    }
                }
            }
            else if (link instanceof CrossSectionLink)
            {
                setLanePosition((CrossSectionLink) link, origin, positionSet);
            }
        }
        return positionSet;
    }

    /**
     * Creates GTU generators. For lane-based GTU generation (i.e. {@code Lane} in the {@code Categorization}), the generators
     * will obtain an ID with the node id plus a counter. For this the initial positions need to be sorted. The link weights and
     * via nodes should be {@code null} for lane-based GTU generation. Furthermore, the lane is then used to obtain OD option
     * values possibly specified at the lane level. Other than that, for either lane-based or zone GTU generation, a
     * {@code LaneBasedGtuGenerator} is created for each initial position given.
     * @param network OtsRoadNetwork; network.
     * @param odOptions OdOptions; od options.
     * @param simulator OtsSimulatorInterface; simulator.
     * @param laneBased boolean; lane in category.
     * @param stream StreamInterface; random number stream.
     * @param output Map&lt;String, GeneratorObjects&gt;; map that output elements will be stored in.
     * @param initialPositions Map&lt;DemandNode&lt;Node, DemandNode&lt;Node, DemandNode&lt;Category, ?&gt;&gt;&gt;,
     *            Set&lt;LanePosition&gt;&gt;; sorted initial positions.
     * @param linkWeights Map&lt;CrossSectionLink, Double&gt;; weights per link, may be {@code null}.
     * @param viaNodes Map&lt;CrossSectionLink, Node&gt;; nodes to select from for zone, may be {@code null}.
     * @throws ParameterException if drawing from the inter-arrival generator fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static void createGenerators(final OtsRoadNetwork network, final OdOptions odOptions,
            final OtsSimulatorInterface simulator, final boolean laneBased, final StreamInterface stream,
            final Map<String, GeneratorObjects> output,
            final Map<DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>, Set<LanePosition>> initialPositions,
            final Map<CrossSectionLink, Double> linkWeights, final Map<CrossSectionLink, Node> viaNodes)
            throws ParameterException
    {
        Map<Node, Integer> laneGeneratorCounterForUniqueId = new LinkedHashMap<>();
        for (DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root : initialPositions.keySet())
        {
            Set<LanePosition> initialPosition = initialPositions.get(root);
            // id
            Node o = root.getObject();
            String id = o.getId();
            if (laneBased)
            {
                Integer count = laneGeneratorCounterForUniqueId.get(o);
                if (count == null)
                {
                    count = 0;
                }
                count++;
                id += count;
                laneGeneratorCounterForUniqueId.put(o, count);
            }
            // functional generation elements
            Lane lane;
            LinkType linkType;
            if (laneBased)
            {
                lane = initialPosition.iterator().next().getLane();
                linkType = lane.getParentLink().getType();
            }
            else
            {
                lane = null;
                linkType = getLinkTypeFromNode(o);
            }
            HeadwayDistribution randomization = odOptions.get(OdOptions.HEADWAY_DIST, lane, o, linkType);
            ArrivalsHeadwayGenerator headwayGenerator = new ArrivalsHeadwayGenerator(root, simulator, stream, randomization);
            LaneBasedGtuCharacteristicsGeneratorOd characteristicsGeneratorOd =
                    odOptions.get(OdOptions.GTU_TYPE, lane, o, linkType);
            LaneBasedGtuCharacteristicsGenerator characteristicsGenerator = new LaneBasedGtuCharacteristicsGenerator()
            {
                /** {@inheritDoc} */
                @Override
                public LaneBasedGtuCharacteristics draw() throws ProbabilityException, ParameterException, GtuException
                {
                    Time time = simulator.getSimulatorAbsTime();
                    Node origin = root.getObject();
                    DemandNode<Node, DemandNode<Category, ?>> destinationNode = root.draw(time);
                    Node destination = destinationNode.getObject();
                    Category category = destinationNode.draw(time).getObject();
                    return characteristicsGeneratorOd.draw(origin, destination, category, stream);
                }
            };

            RoomChecker roomChecker = odOptions.get(OdOptions.ROOM_CHECKER, lane, o, linkType);
            IdGenerator idGenerator = odOptions.get(OdOptions.GTU_ID, lane, o, linkType);
            LaneBiases biases = odOptions.get(OdOptions.LANE_BIAS, lane, o, linkType);
            // and finally, the generator
            try
            {
                LaneBasedGtuGenerator generator = new LaneBasedGtuGenerator(id, headwayGenerator, characteristicsGenerator,
                        GeneratorPositions.create(initialPosition, stream, biases, linkWeights, viaNodes), network, simulator,
                        roomChecker, idGenerator);
                generator.setNoLaneChangeDistance(odOptions.get(OdOptions.NO_LC_DIST, lane, o, linkType));
                generator.setInstantaneousLaneChange(odOptions.get(OdOptions.INSTANT_LC, lane, o, linkType));
                generator.setErrorHandler(odOptions.get(OdOptions.ERROR_HANDLER, lane, o, linkType));
                output.put(id, new GeneratorObjects(generator, headwayGenerator, characteristicsGenerator));
            }
            catch (SimRuntimeException exception)
            {
                // should not happen, we check that time is 0
                simulator.getLogger().always().error(exception);
                throw new RuntimeException(exception);
            }
            catch (ProbabilityException exception)
            {
                // should not happen, as we define probabilities in the headwayGenerator
                simulator.getLogger().always().error(exception);
                throw new RuntimeException(exception);
            }
            catch (NetworkException exception)
            {
                // should not happen, as unique ids are guaranteed by UUID
                simulator.getLogger().always().error(exception);
                throw new RuntimeException(exception);
            }
        }
    }

    /**
     * Obtains a stream for vehicle generation.
     * @param simulator OtsSimulatorInterface; simulator.
     * @return StreamInterface; stream for vehicle generation.
     */
    private static StreamInterface getStream(final OtsSimulatorInterface simulator)
    {
        StreamInterface stream = simulator.getModel().getStream("generation");
        if (stream == null)
        {
            stream = simulator.getModel().getStream("default");
            if (stream == null)
            {
                System.out
                        .println("Using locally created stream (not from the simulator) for vehicle generation, with seed 1.");
                stream = new MersenneTwister(1L);
            }
            else
            {
                System.out.println("Using stream 'default' for vehicle generation.");
            }
        }
        return stream;
    }

    /**
     * Create destination sensors at all lanes connected to a destination node. This method considers connectors too.
     * @param destination Node; destination node
     * @param simulator OtsSimulatorInterface; simulator
     * @param detectorType DetectorType; detector type.
     */
    private static void createSensorsAtDestination(final Node destination, final OtsSimulatorInterface simulator,
            final DetectorType detectorType)
    {
        for (Link link : destination.getLinks())
        {
            if (link.isConnector() && !link.getStartNode().equals(destination))
            {
                createSensorsAtDestinationNode(link.getStartNode(), simulator, detectorType);
            }
            else
            {
                createSensorsAtDestinationNode(destination, simulator, detectorType);
            }
        }
    }

    /**
     * Create sensors at all lanes connected to this node. This method does not handle connectors.
     * @param destination Node; the destination node
     * @param simulator OtsSimulatorInterface; simulator
     * @param detectorType DetectorType; detector type.
     */
    private static void createSensorsAtDestinationNode(final Node destination, final OtsSimulatorInterface simulator,
            final DetectorType detectorType)
    {
        for (Link link : destination.getLinks())
        {
            if (link instanceof CrossSectionLink)
            {
                for (Lane lane : ((CrossSectionLink) link).getLanes())
                {
                    try
                    {
                        // if the lane already contains a DestinationDetector, skip creating a new one
                        boolean destinationDetectorExists = false;
                        for (LaneDetector detector : lane.getDetectors())
                        {
                            if (detector instanceof DestinationDetector)
                            {
                                destinationDetectorExists = true;
                            }
                        }
                        if (!destinationDetectorExists)
                        {
                            if (link.getEndNode().equals(destination))
                            {
                                new DestinationDetector(lane, lane.getLength(), simulator, detectorType);
                            }
                            else if (link.getStartNode().equals(destination))
                            {
                                new DestinationDetector(lane, Length.ZERO, simulator, detectorType);
                            }
                        }
                    }
                    catch (NetworkException exception)
                    {
                        // can not happen, we use Length.ZERO and lane.getLength()
                        simulator.getLogger().always().error(exception);
                        throw new RuntimeException(exception);
                    }
                }
            }
        }
    }

    /**
     * Returns the common ancestor {@code LinkType} of all links connected to the node, moving through connectors.
     * @param node Node; origin node
     * @return common ancestor {@code LinkType} of all links connected to the node, moving through connectors
     */
    private static LinkType getLinkTypeFromNode(final Node node)
    {
        return getLinkTypeFromNode0(node, false);
    }

    /**
     * Returns the common ancestor {@code LinkType} of all links connected to the node, moving through connectors.
     * @param node Node; origin node
     * @param ignoreConnectors boolean; ignore connectors
     * @return common ancestor {@code LinkType} of all links connected to the node, moving through connectors
     */
    private static LinkType getLinkTypeFromNode0(final Node node, final boolean ignoreConnectors)
    {
        LinkType linkType = null;
        for (Link link : node.getLinks())
        {
            LinkType next = link.getType();
            if (!ignoreConnectors && link.isConnector())
            {
                Node otherNode = link.getStartNode().equals(node) ? link.getEndNode() : link.getStartNode();
                next = getLinkTypeFromNode0(otherNode, true);
            }
            if (next != null && !link.isConnector())
            {
                if (linkType == null)
                {
                    linkType = next;
                }
                else
                {
                    linkType = linkType.commonAncestor(next);
                    if (linkType == null)
                    {
                        // incompatible link types
                        return null;
                    }
                }
            }
        }
        return linkType;
    }

    /**
     * Returns a sorted map.
     * @param map Map&lt;K, V&gt;; input map
     * @param <K> key type (implemented for cleaner code only)
     * @param <V> value type (implemented for cleaner code only)
     * @return Map; sorted map
     */
    private static <K, V extends Set<LanePosition>> Map<K, V> sortByValue(final Map<K, V> map)
    {
        return map.entrySet().stream().sorted(new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(final Entry<K, V> o1, final Entry<K, V> o2)
            {
                LanePosition lanePos1 = o1.getValue().iterator().next();
                String linkId1 = lanePos1.getLane().getParentLink().getId();
                LanePosition lanePos2 = o2.getValue().iterator().next();
                String linkId2 = lanePos2.getLane().getParentLink().getId();
                int c = linkId1.compareToIgnoreCase(linkId2);
                if (c == 0)
                {
                    Length pos1 = Length.ZERO;
                    Length lat1 = lanePos1.getLane().getLateralCenterPosition(pos1);
                    Length pos2 = Length.ZERO;
                    Length lat2 = lanePos2.getLane().getLateralCenterPosition(pos2);
                    return lat1.compareTo(lat2);
                }
                return c;
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Adds {@code LanePosition}s to the input set, for {@code Lane}s on the given link, starting at the given {@code Node}.
     * @param link CrossSectionLink; link with lanes to add positions for
     * @param node Node; node on the side where positions should be placed
     * @param positionSet Set&lt;LanePosition&gt;; set to add position to
     */
    private static void setLanePosition(final CrossSectionLink link, final Node node, final Set<LanePosition> positionSet)
    {
        for (Lane lane : link.getLanes())
        {
            // TODO should be GTU type dependent.
            if (lane.getParentLink().getStartNode().equals(node))
            {
                positionSet.add(new LanePosition(lane, Length.ZERO));
            }
        }
    }

    /**
     * Node for demand tree. Based on two constructors there are 2 types of nodes:<br>
     * <ul>
     * <li>Branch nodes; with an object and a stream for randomly drawing a child node.</li>
     * <li>Leaf nodes; with an object and demand data (time, frequency, interpolation).</li>
     * </ul>
     * To accomplish a branching of Node (origin) &gt; Node (destination) &gt; Category, the following generics types can be
     * used:<br>
     * <br>
     * {@code DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>}
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <T> type of contained object
     * @param <K> type of child nodes
     */
    private static class DemandNode<T, K extends DemandNode<?, ?>> implements Arrivals
    {

        /** Node object. */
        private final T object;

        /** Random stream to draw child node. */
        private final StreamInterface stream;

        /** Children. */
        private final List<K> children = new ArrayList<>();

        /** Demand data. */
        private final DemandPattern demandPattern;

        /** Unique GTU types of leaf nodes. */
        private final List<GtuType> gtuTypes = new ArrayList<>();

        /** Number of leaf nodes for the unique GTU types. */
        private final List<Integer> gtuTypeCounts = new ArrayList<>();

        /** GTU type of leaf nodes. */
        private final Map<K, GtuType> gtuTypesPerChild = new LinkedHashMap<>();

        /** Markov chain for GTU type selection. */
        private final MarkovChain markov;

        /**
         * Constructor for branching node, with Markov selection.
         * @param object T; node object
         * @param stream StreamInterface; random stream to draw child node
         * @param markov MarkovChain; Markov chain
         */
        DemandNode(final T object, final StreamInterface stream, final MarkovChain markov)
        {
            this.object = object;
            this.stream = stream;
            this.demandPattern = null;
            this.markov = markov;
        }

        /**
         * Constructor for leaf node, without Markov selection.
         * @param object T; node object
         * @param demandPattern DemandPattern; demand data
         */
        DemandNode(final T object, final DemandPattern demandPattern)
        {
            this.object = object;
            this.stream = null;
            this.demandPattern = demandPattern;
            this.markov = null;
        }

        /**
         * Adds child to a branching node.
         * @param child K; child node
         */
        public void addChild(final K child)
        {
            this.children.add(child);
        }

        /**
         * Adds child to a branching node.
         * @param child K; child node
         * @param gtuType GtuType; gtu type for Markov chain
         */
        public void addLeaf(final K child, final GtuType gtuType)
        {
            Throw.when(this.gtuTypes == null, IllegalStateException.class,
                    "Adding leaf with GtuType in not possible on a non-Markov node.");
            addChild(child);
            this.gtuTypesPerChild.put(child, gtuType);
            if (!this.gtuTypes.contains(gtuType))
            {
                this.gtuTypes.add(gtuType);
                this.gtuTypeCounts.add(1);
            }
            else
            {
                int index = this.gtuTypes.indexOf(gtuType);
                this.gtuTypeCounts.set(index, this.gtuTypeCounts.get(index) + 1);
            }
        }

        /**
         * Randomly draws a child node.
         * @param time Time; simulation time
         * @return K; randomly drawn child node
         */
        public K draw(final Time time)
        {
            Throw.when(this.children.isEmpty(), RuntimeException.class, "Calling draw on a leaf node in the demand tree.");
            Map<K, Double> weightMap = new LinkedHashMap<>();
            if (this.markov == null)
            {
                // regular draw, loop children and collect their frequencies
                for (K child : this.children)
                {
                    double f = child.getFrequency(time, true).si; // sliceStart = true is arbitrary
                    weightMap.put(child, f);
                }
            }
            else
            {
                // markov chain draw, the markov chain only selects a GTU type, not a child node
                GtuType[] gtuTypeArray = new GtuType[this.gtuTypes.size()];
                gtuTypeArray = this.gtuTypes.toArray(gtuTypeArray);
                Frequency[] steadyState = new Frequency[this.gtuTypes.size()];
                Arrays.fill(steadyState, Frequency.ZERO);
                Map<K, Frequency> frequencies = new LinkedHashMap<>(); // stored, saves us from calculating them twice
                for (K child : this.children)
                {
                    GtuType gtuType = this.gtuTypesPerChild.get(child);
                    int index = this.gtuTypes.indexOf(gtuType);
                    Frequency f = child.getFrequency(time, true); // sliceStart = true is arbitrary
                    frequencies.put(child, f);
                    steadyState[index] = steadyState[index].plus(f);
                }
                GtuType nextGtuType = this.markov.draw(gtuTypeArray, steadyState, this.stream);
                // select only child nodes registered to the next GTU type
                for (K child : this.children)
                {
                    if (this.gtuTypesPerChild.get(child).equals(nextGtuType))
                    {
                        double f = frequencies.get(child).si;
                        weightMap.put(child, f);
                    }
                }
            }
            return Draw.drawWeighted(weightMap, this.stream);
        }

        /**
         * Returns the node object.
         * @return T; node object
         */
        public T getObject()
        {
            return this.object;
        }

        /**
         * Returns the child that pertains to specified object or {@code null} if no such child is present.
         * @param obj Object; child object
         * @return child that pertains to specified object or {@code null} if no such child is present
         */
        public K getChild(final Object obj)
        {
            for (K child : this.children)
            {
                if (child.getObject().equals(obj))
                {
                    return child;
                }
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Frequency getFrequency(final Time time, final boolean sliceStart)
        {
            if (this.demandPattern != null)
            {
                return this.demandPattern.getFrequency(time, sliceStart);
            }
            Frequency f = new Frequency(0.0, FrequencyUnit.PER_HOUR);
            for (K child : this.children)
            {
                f = f.plus(child.getFrequency(time, sliceStart));
            }
            return f;
        }

        /** {@inheritDoc} */
        @Override
        public Time nextTimeSlice(final Time time)
        {
            if (this.demandPattern != null)
            {
                return this.demandPattern.nextTimeSlice(time);
            }
            Time out = null;
            for (K child : this.children)
            {
                Time childSlice = child.nextTimeSlice(time);
                out = out == null || (childSlice != null && childSlice.lt(out)) ? childSlice : out;
            }
            return out;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "DemandNode [object=" + this.object + ", stream=" + this.stream + ", children=" + this.children
                    + ", demandPattern=" + this.demandPattern + ", gtuTypes=" + this.gtuTypes + ", gtuTypeCounts="
                    + this.gtuTypeCounts + ", gtuTypesPerChild=" + this.gtuTypesPerChild + ", markov=" + this.markov + "]";
        }

    }

    /**
     * Wrapper class around a {@code MarkovCorrelation}, including the last type. One of these should be used for each vehicle
     * generator.
     */
    private static class MarkovChain
    {
        /** Markov correlation for GTU type selection. */
        private final MarkovCorrelation<GtuType, Frequency> markov;

        /** Previously returned GTU type. */
        private GtuType previousGtuType = null;

        /**
         * Constructor.
         * @param markov MarkovCorrelation&lt;GtuType, Frequency&gt;; Markov correlation for GTU type selection
         */
        MarkovChain(final MarkovCorrelation<GtuType, Frequency> markov)
        {
            this.markov = markov;
        }

        /**
         * Returns a next GTU type drawn using a Markov chain.
         * @param gtuTypes GtuType[]; GtuTypes to consider
         * @param intensities Frequency[]; frequency for each GTU type, i.e. the steady-state
         * @param stream StreamInterface; stream for random numbers
         * @return next GTU type drawn using a Markov chain
         */
        public GtuType draw(final GtuType[] gtuTypes, final Frequency[] intensities, final StreamInterface stream)
        {
            this.previousGtuType = this.markov.drawState(this.previousGtuType, gtuTypes, intensities, stream);
            return this.previousGtuType;
        }
    }

    /**
     * Class to contain created generator objects.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class GeneratorObjects
    {

        /** Main generator for GTU's. */
        private final LaneBasedGtuGenerator generator;

        /** Generator of headways. */
        private final Generator<Duration> headwayGenerator;

        /** Generator of GTU characteristics. */
        private final LaneBasedGtuCharacteristicsGenerator characteristicsGenerator;

        /**
         * @param generator LaneBasedGtuGenerator; main generator for GTU's
         * @param headwayGenerator Generator&lt;Duration&gt;; generator of headways
         * @param characteristicsGenerator LaneBasedGtuCharacteristicsGenerator; generator of GTU characteristics
         */
        public GeneratorObjects(final LaneBasedGtuGenerator generator, final Generator<Duration> headwayGenerator,
                final LaneBasedGtuCharacteristicsGenerator characteristicsGenerator)
        {
            this.generator = generator;
            this.headwayGenerator = headwayGenerator;
            this.characteristicsGenerator = characteristicsGenerator;
        }

        /**
         * Returns the main generator for GTU's.
         * @return LaneBasedGtuGenerator; main generator for GTU's
         */
        public LaneBasedGtuGenerator getGenerator()
        {
            return this.generator;
        }

        /**
         * Returns the generator of headways.
         * @return Generator&lt;Duration&gt; generator of headways
         */
        public Generator<Duration> getHeadwayGenerator()
        {
            return this.headwayGenerator;
        }

        /**
         * Returns the generator of GTU characteristics.
         * @return LaneBasedGtuCharacteristicsGenerator; generator of GTU characteristics
         */
        public LaneBasedGtuCharacteristicsGenerator getCharachteristicsGenerator()
        {
            return this.characteristicsGenerator;
        }

    }

}
