package org.opentrafficsim.road.gtu.generator.od;

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
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.generator.headway.Arrivals;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.gtu.generator.headway.DemandPattern;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Utility to create vehicle generators on a network from an OD.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 nov. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class ODApplier
{

    /**
     * Utility class.
     */
    private ODApplier()
    {
        //
    }

    /**
     * Applies the OD to the network by creating vehicle generators. The map returned contains objects created for vehicle
     * generation. These are bundled in a {@code GeneratorObjects} and mapped to the vehicle generator id. Vehicle generator id
     * is equal to the origin node id. For lane-based generators the id's are appended with an ordered number (e.g. A1), where
     * the ordering is first by link id, and then right to left concerning the lateral lane position at the start of the lane.
     * For node "A" this would for example be:<br>
     * <table caption="">
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
     * @param network OTSRoadNetwork; network
     * @param od ODMatrix; OD matrix
     * @param simulator OTSSimulatorInterface; simulator
     * @param odOptions ODOptions; options for vehicle generation
     * @return Map&lt;String, GeneratorObjects&gt; map of generator id's and created generator objects mainly for testing
     * @throws ParameterException if a parameter is missing
     * @throws SimRuntimeException if this method is called after simulation time 0
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static Map<String, GeneratorObjects> applyOD(final OTSRoadNetwork network, final ODMatrix od,
            final OTSSimulatorInterface simulator, final ODOptions odOptions) throws ParameterException, SimRuntimeException
    {
        Throw.whenNull(network, "Network may not be null.");
        Throw.whenNull(od, "OD matrix may not be null.");
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(odOptions, "OD options may not be null.");
        Throw.when(!simulator.getSimulatorTime().eq0(), SimRuntimeException.class,
                "Method ODApplier.applyOD() should be invoked at simulation time 0.");

        // TODO sinks? white extension links?
        for (Node destination : od.getDestinations())
        {
            createSinksAtDestination(destination, simulator);
        }

        final Categorization categorization = od.getCategorization();
        final boolean laneBased = categorization.entails(Lane.class);
        boolean markovian = od.getCategorization().entails(GTUType.class);

        // TODO clean up stream acquiring code after task OTS-315 has been completed
        StreamInterface stream = simulator.getReplication().getStream("generation");
        if (stream == null)
        {
            stream = simulator.getReplication().getStream("default");
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

        Map<String, GeneratorObjects> output = new LinkedHashMap<>();
        for (Node origin : od.getOrigins())
        {
            // Step 1: create DemandNode trees, starting with a root for each vehicle generator
            DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> rootNode = null; // root node for each generator
            /**
             * Depending on whether the categorization is lane based or not, we either have 1 root per origin, or we have 1 root
             * per lane (i.e. 1 generator at an origin putting traffic on multiple lanes, or N generators per origin, each
             * generating traffic on 1 lane). In order to know to which root node the sub nodes belong in a loop, we store root
             * nodes by lane. Effectively, the map functions as an artificial branching of demand before the origin node, only
             * used if the categorization contains lanes. For non-lane based demand, the root node and destination node created
             * in the outer loop can simply be used.
             */
            Map<Lane, DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>> originNodePerLane = new LinkedHashMap<>();
            MarkovChain markovChain = null;
            if (!laneBased)
            {
                rootNode = new DemandNode<>(origin, stream, null);
                LinkType linkType = getLinkTypeFromNode(origin);
                if (markovian)
                {
                    MarkovCorrelation<GTUType, Frequency> correlation = odOptions.get(ODOptions.MARKOV, null, origin, linkType);
                    if (correlation != null)
                    {
                        Throw.when(!od.getCategorization().entails(GTUType.class), IllegalArgumentException.class,
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
                        rootNode.addChild(destinationNode);
                    }
                    for (Category category : categories)
                    {
                        if (laneBased)
                        {
                            // obtain or create root and destination nodes
                            Lane lane = category.get(Lane.class);
                            rootNode = originNodePerLane.get(lane);
                            if (rootNode == null)
                            {
                                rootNode = new DemandNode<>(origin, stream, null);
                                originNodePerLane.put(lane, rootNode);
                            }
                            destinationNode = rootNode.getChild(destination);
                            if (destinationNode == null)
                            {
                                markovChain = null;
                                if (markovian)
                                {
                                    MarkovCorrelation<GTUType, Frequency> correlation =
                                            odOptions.get(ODOptions.MARKOV, lane, origin, lane.getParentLink().getLinkType());
                                    if (correlation != null)
                                    {
                                        Throw.when(!od.getCategorization().entails(GTUType.class),
                                                IllegalArgumentException.class,
                                                "Markov correlation can only be used on OD categorization entailing GTU type.");
                                        markovChain = new MarkovChain(correlation); // 1 for each generator
                                    }
                                }
                                destinationNode = new DemandNode<>(destination, stream, markovChain);
                                rootNode.addChild(destinationNode);
                            }
                        }
                        DemandNode<Category, ?> categoryNode =
                                new DemandNode<>(category, od.getDemandPattern(origin, destination, category));
                        if (markovian)
                        {
                            destinationNode.addLeaf(categoryNode, category.get(GTUType.class));
                        }
                        else
                        {
                            destinationNode.addChild(categoryNode);
                        }
                    }
                }
            }

            // Step 2: gather DirectedLanePositions for each generator pertaining to each DemandNode<...>
            Map<DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>, Set<DirectedLanePosition>> initialPositions =
                    new LinkedHashMap<>();
            Map<CrossSectionLink, Double> linkWeights = null;
            if (laneBased)
            {
                for (Lane lane : originNodePerLane.keySet())
                {
                    DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> demandNode = originNodePerLane.get(lane);
                    Set<DirectedLanePosition> initialPosition = new LinkedHashSet<>();
                    try
                    {
                        initialPosition.add(lane.getParentLink().getStartNode().equals(demandNode.getObject())
                                ? new DirectedLanePosition(lane, Length.ZERO, GTUDirectionality.DIR_PLUS)
                                : new DirectedLanePosition(lane, lane.getLength(), GTUDirectionality.DIR_MINUS));
                    }
                    catch (GTUException ge)
                    {
                        throw new RuntimeException(ge);
                    }
                    initialPositions.put(demandNode, initialPosition);
                }
            }
            else
            {
                Set<DirectedLanePosition> positionSet = new LinkedHashSet<>();
                for (Link link : origin.getLinks())
                {
                    if (link.getLinkType().isConnector())
                    {
                        if (link.getStartNode().equals(origin))
                        {
                            Node connectedNode = link.getEndNode();
                            // count number of served links
                            int served = 0;
                            for (Link connectedLink : connectedNode.getLinks())
                            {
                                if (connectedLink instanceof CrossSectionLink && !connectedLink.getLinkType().isConnector())
                                {
                                    served++;
                                }
                            }
                            for (Link connectedLink : connectedNode.getLinks())
                            {
                                if (connectedLink instanceof CrossSectionLink)
                                {
                                    if (link instanceof CrossSectionLink && ((CrossSectionLink) link).getDemandWeight() != null)
                                    {
                                        if (linkWeights == null)
                                        {
                                            linkWeights = new LinkedHashMap<>();
                                        }
                                        // store weight under connected link, as this
                                        linkWeights.put(((CrossSectionLink) connectedLink),
                                                ((CrossSectionLink) link).getDemandWeight() / served);
                                    }
                                    setDirectedLanePosition((CrossSectionLink) connectedLink, connectedNode, positionSet);
                                }
                            }
                        }
                    }
                    else if (link instanceof CrossSectionLink)
                    {
                        setDirectedLanePosition((CrossSectionLink) link, origin, positionSet);
                    }
                }
                initialPositions.put(rootNode, positionSet);
            }

            // Step 3: create generator(s)
            initialPositions = sortByValue(initialPositions); // sorts by lateral position at link start
            Map<Node, Integer> originGeneratorCounts = new LinkedHashMap<>();
            for (DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root : initialPositions.keySet())
            {
                Set<DirectedLanePosition> initialPosition = initialPositions.get(root);
                // id
                Node o = root.getObject();
                String id = o.getId();
                if (laneBased)
                {
                    Integer count = originGeneratorCounts.get(o);
                    if (count == null)
                    {
                        count = 0;
                    }
                    count++;
                    id += count;
                    originGeneratorCounts.put(o, count);
                }
                // functional generation elements
                Lane lane;
                LinkType linkType;
                if (laneBased)
                {
                    lane = initialPosition.iterator().next().getLane();
                    linkType = lane.getParentLink().getLinkType();
                }
                else
                {
                    lane = null;
                    linkType = getLinkTypeFromNode(o);
                }
                HeadwayDistribution randomization = odOptions.get(ODOptions.HEADWAY_DIST, lane, o, linkType);
                ArrivalsHeadwayGenerator headwayGenerator =
                        new ArrivalsHeadwayGenerator(root, simulator, stream, randomization);
                GTUCharacteristicsGeneratorODWrapper characteristicsGenerator = new GTUCharacteristicsGeneratorODWrapper(root,
                        simulator, odOptions.get(ODOptions.GTU_TYPE, lane, o, linkType), stream);
                RoomChecker roomChecker = odOptions.get(ODOptions.ROOM_CHECKER, lane, o, linkType);
                IdGenerator idGenerator = odOptions.get(ODOptions.GTU_ID, lane, o, linkType);
                LaneBiases biases = odOptions.get(ODOptions.getLaneBiasOption(network), lane, o, linkType);
                // and finally, the generator
                try
                {
                    LaneBasedGTUGenerator generator = new LaneBasedGTUGenerator(id, headwayGenerator, characteristicsGenerator,
                            GeneratorPositions.create(initialPosition, stream, biases, linkWeights), network, simulator,
                            roomChecker, idGenerator);
                    generator.setNoLaneChangeDistance(odOptions.get(ODOptions.NO_LC_DIST, lane, o, linkType));
                    generator.setInstantaneousLaneChange(odOptions.get(ODOptions.INSTANT_LC, lane, o, linkType));
                    generator.setErrorHandler(odOptions.get(ODOptions.ERROR_HANDLER, lane, o, linkType));
                    output.put(id, new GeneratorObjects(generator, headwayGenerator, characteristicsGenerator));
                }
                catch (SimRuntimeException exception)
                {
                    // should not happen, we check that time is 0
                    throw new RuntimeException(exception);
                }
                catch (ProbabilityException exception)
                {
                    // should not happen, as we define probabilities in the headwayGenerator
                    throw new RuntimeException(exception);
                }
            }
        }
        return output;
    }

    /**
     * Create sinks at all lanes connected to a destination node. This method considers connectors too.
     * @param destination Node; destination node
     * @param simulator OTSSimulatorInterface; simulator
     */
    private static void createSinksAtDestination(final Node destination, final OTSSimulatorInterface simulator)
    {
        for (Link link : destination.getLinks())
        {
            if (link.getLinkType().isConnector() && !link.getStartNode().equals(destination))
            {
                createSinksAtDestination(link.getStartNode(), simulator);
            }
            if (link instanceof CrossSectionLink)
            {
                for (Lane lane : ((CrossSectionLink) link).getLanes())
                {
                    try
                    {
                        // if the lane already contains a SinkSensor, skip creating a new one
                        boolean sinkSensorExists = false;
                        for (Sensor sensor : lane.getSensors())
                        {
                            if (sensor instanceof SinkSensor)
                            {
                                sinkSensorExists = true;
                            }
                        }
                        if (!sinkSensorExists)
                        {
                            if (link.getEndNode().equals(destination))
                            {
                                new SinkSensor(lane, lane.getLength(), GTUDirectionality.DIR_PLUS, simulator);
                            }
                            else if (link.getStartNode().equals(destination))
                            {
                                new SinkSensor(lane, Length.ZERO, GTUDirectionality.DIR_MINUS, simulator);
                            }
                        }
                    }
                    catch (NetworkException exception)
                    {
                        // can not happen, we use Length.ZERO and lane.getLength()
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
            LinkType next = link.getLinkType();
            if (!ignoreConnectors && next.isConnector())
            {
                Node otherNode = link.getStartNode().equals(node) ? link.getEndNode() : link.getStartNode();
                next = getLinkTypeFromNode0(otherNode, true);
            }
            if (next != null && !next.isConnector())
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
    private static <K, V extends Set<DirectedLanePosition>> Map<K, V> sortByValue(final Map<K, V> map)
    {
        return map.entrySet().stream().sorted(new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(final Entry<K, V> o1, final Entry<K, V> o2)
            {
                DirectedLanePosition lanePos1 = o1.getValue().iterator().next();
                String linkId1 = lanePos1.getLane().getParentLink().getId();
                DirectedLanePosition lanePos2 = o2.getValue().iterator().next();
                String linkId2 = lanePos2.getLane().getParentLink().getId();
                int c = linkId1.compareToIgnoreCase(linkId2);
                if (c == 0)
                {
                    Length pos1 = lanePos1.getGtuDirection().isPlus() ? Length.ZERO : lanePos1.getLane().getLength();
                    Length lat1 = lanePos1.getLane().getLateralCenterPosition(pos1);
                    Length pos2 = lanePos2.getGtuDirection().isPlus() ? Length.ZERO : lanePos2.getLane().getLength();
                    Length lat2 = lanePos2.getLane().getLateralCenterPosition(pos2);
                    return lat1.compareTo(lat2);
                }
                return c;
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Adds {@code DirectedLanePosition}s to the input set, for {@code Lane}s on the given link, starting at the given
     * {@code Node}.
     * @param link CrossSectionLink; link with lanes to add positions for
     * @param node Node; node on the side where positions should be placed
     * @param positionSet Set&lt;DirectedLanePosition&gt;; set to add position to
     */
    private static void setDirectedLanePosition(final CrossSectionLink link, final Node node,
            final Set<DirectedLanePosition> positionSet)
    {
        for (Lane lane : link.getLanes())
        {
            try
            {
                positionSet.add(lane.getParentLink().getStartNode().equals(node)
                        ? new DirectedLanePosition(lane, Length.ZERO, GTUDirectionality.DIR_PLUS)
                        : new DirectedLanePosition(lane, lane.getLength(), GTUDirectionality.DIR_MINUS));
            }
            catch (GTUException ge)
            {
                throw new RuntimeException(ge);
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
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        private final List<GTUType> gtuTypes = new ArrayList<>();

        /** Number of leaf nodes for the unique GTU types. */
        private final List<Integer> gtuTypeCounts = new ArrayList<>();

        /** GTU type of leaf nodes. */
        private final Map<K, GTUType> gtuTypesPerChild = new LinkedHashMap<>();

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
         * @param gtuType GTUType; gtu type for Markov chain
         */
        public void addLeaf(final K child, final GTUType gtuType)
        {
            Throw.when(this.gtuTypes == null, IllegalStateException.class,
                    "Adding leaf with GTUType in not possible on a non-Markov node.");
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
                GTUType[] gtuTypeArray = new GTUType[this.gtuTypes.size()];
                gtuTypeArray = this.gtuTypes.toArray(gtuTypeArray);
                Frequency[] steadyState = new Frequency[this.gtuTypes.size()];
                Arrays.fill(steadyState, Frequency.ZERO);
                Map<K, Frequency> frequencies = new LinkedHashMap<>(); // stored, saves us from calculating them twice
                for (K child : this.children)
                {
                    GTUType gtuType = this.gtuTypesPerChild.get(child);
                    int index = this.gtuTypes.indexOf(gtuType);
                    Frequency f = child.getFrequency(time, true); // sliceStart = true is arbitrary
                    frequencies.put(child, f);
                    steadyState[index] = steadyState[index].plus(f);
                }
                GTUType nextGtuType = this.markov.draw(gtuTypeArray, steadyState, this.stream);
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
        private final MarkovCorrelation<GTUType, Frequency> markov;

        /** Previously returned GTU type. */
        private GTUType previousGtuType = null;

        /**
         * Constructor.
         * @param markov MarkovCorrelation&lt;GTUType, Frequency&gt;; Markov correlation for GTU type selection
         */
        MarkovChain(final MarkovCorrelation<GTUType, Frequency> markov)
        {
            this.markov = markov;
        }

        /**
         * Returns a next GTU type drawn using a Markov chain.
         * @param gtuTypes GTUType[]; GTUTypes to consider
         * @param intensities Frequency[]; frequency for each GTU type, i.e. the steady-state
         * @param stream StreamInterface; stream for random numbers
         * @return next GTU type drawn using a Markov chain
         */
        public GTUType draw(final GTUType[] gtuTypes, final Frequency[] intensities, final StreamInterface stream)
        {
            this.previousGtuType = this.markov.drawState(this.previousGtuType, gtuTypes, intensities, stream);
            return this.previousGtuType;
        }
    }

    /**
     * Characteristics generation based on OD demand.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 7 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class GTUCharacteristicsGeneratorODWrapper implements LaneBasedGTUCharacteristicsGenerator
    {

        /** Root node with origin. */
        private final DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root;

        /** Simulator. */
        private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

        /** Characteristics generator based on OD information. */
        private final GTUCharacteristicsGeneratorOD charachteristicsGenerator;

        /** Stream for random numbers. */
        private final StreamInterface randomStream;

        /**
         * @param root DemandNode&lt;Node, DemandNode&lt;Node, DemandNode&lt;Category, ?&gt;&gt;&gt;; root node with origin
         * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
         * @param charachteristicsGenerator GTUCharacteristicsGeneratorOD; characteristics generator based on OD information
         * @param randomStream StreamInterface; stream for random numbers
         */
        GTUCharacteristicsGeneratorODWrapper(final DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root,
                final DEVSSimulatorInterface.TimeDoubleUnit simulator,
                final GTUCharacteristicsGeneratorOD charachteristicsGenerator, final StreamInterface randomStream)
        {
            this.root = root;
            this.simulator = simulator;
            this.charachteristicsGenerator = charachteristicsGenerator;
            this.randomStream = randomStream;
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GTUException
        {
            // obtain node objects
            Time time = this.simulator.getSimulatorTime();
            Node origin = this.root.getObject();
            DemandNode<Node, DemandNode<Category, ?>> destinationNode = this.root.draw(time);
            Node destination = destinationNode.getObject();
            Category category = destinationNode.draw(time).getObject();
            // forward to lower-level generator
            return this.charachteristicsGenerator.draw(origin, destination, category, this.randomStream);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "GTUCharacteristicsGeneratorODWrapper [root=" + this.root + ", simulator=" + this.simulator
                    + ", charachteristicsGenerator=" + this.charachteristicsGenerator + ", randomStream=" + this.randomStream
                    + "]";
        }

    }

    /**
     * Class to contain created generator objects.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class GeneratorObjects
    {

        /** Main generator for GTU's. */
        private final LaneBasedGTUGenerator generator;

        /** Generator of headways. */
        private final Generator<Duration> headwayGenerator;

        /** Generator of GTU characteristics. */
        private final LaneBasedGTUCharacteristicsGenerator charachteristicsGenerator;

        /**
         * @param generator LaneBasedGTUGenerator; main generator for GTU's
         * @param headwayGenerator Generator&lt;Duration&gt;; generator of headways
         * @param charachteristicsGenerator LaneBasedGTUCharacteristicsGenerator; generator of GTU characteristics
         */
        public GeneratorObjects(final LaneBasedGTUGenerator generator, final Generator<Duration> headwayGenerator,
                final LaneBasedGTUCharacteristicsGenerator charachteristicsGenerator)
        {
            this.generator = generator;
            this.headwayGenerator = headwayGenerator;
            this.charachteristicsGenerator = charachteristicsGenerator;
        }

        /**
         * Returns the main generator for GTU's.
         * @return LaneBasedGTUGenerator; main generator for GTU's
         */
        public LaneBasedGTUGenerator getGenerator()
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
         * @return LaneBasedGTUCharacteristicsGenerator; generator of GTU characteristics
         */
        public LaneBasedGTUCharacteristicsGenerator getCharachteristicsGenerator()
        {
            return this.charachteristicsGenerator;
        }

    }

}
