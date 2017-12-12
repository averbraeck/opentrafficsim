package org.opentrafficsim.road.gtu.strategical.od;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix.ODEntry;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.Throw;

/**
 * Utility to create vehicle generators on a network from an OD.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * <table>
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
     * @param network OTSNetwork; network
     * @param od ODMatrix; OD matrix
     * @param simulator OTSDEVSSimulatorInterface; simulator
     * @param odOptions ODOptions; options for vehicle generation
     * @return Map&lt;String, GeneratorObjects&gt; map of generator id's and created generator objects mainly for testing
     * @throws ParameterException if a parameter is missing
     * @throws SimRuntimeException if this method is called after simulation time 0
     */
    public static Map<String, GeneratorObjects> applyOD(final OTSNetwork network, final ODMatrix od,
            final OTSDEVSSimulatorInterface simulator, final ODOptions odOptions) throws ParameterException, SimRuntimeException
    {
        Throw.whenNull(network, "Network may not be null.");
        Throw.whenNull(od, "OD matrix may not be null.");
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(odOptions, "OD options may not be null.");
        Throw.when(!simulator.getSimulatorTime().getTime().eq0(), SimRuntimeException.class,
                "Method ODApplier.applyOD() should be invoked at simulation time 0.");

        // TODO sinks? white extension links?

        final Categorization categorization = od.getCategorization();
        final boolean laneBased = categorization.entails(Lane.class);

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

        // TODO generator locations was misunderstood; it's the lanes of 1 cross-section, not a choice for the generator to
        // select one of multiple locations. We will probably need to add this to the vehicle generator, i.e. to use a
        // Set<Set<DirectedLanePosition>>
        Map<String, GeneratorObjects> output = new HashMap<>();
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
            Map<Lane, DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>> originNodePerLane = new HashMap<>();
            if (!laneBased)
            {
                rootNode = new DemandNode<>(origin, stream);
            }
            for (Node destination : od.getDestinations())
            {
                Set<Category> categories = od.getCategories(origin, destination);
                if (!categories.isEmpty())
                {
                    DemandNode<Node, DemandNode<Category, ?>> destinationNode = null;
                    if (!laneBased)
                    {
                        destinationNode = new DemandNode<>(destination, stream);
                        rootNode.addChild(destinationNode);
                    }
                    for (Category category : categories)
                    {
                        if (laneBased)
                        {
                            // obtain or create root and destination nodes
                            Lane lane = category.get(Lane.class);
                            if (originNodePerLane.containsKey(lane))
                            {
                                rootNode = originNodePerLane.get(lane);
                                destinationNode = rootNode.getChild(destination);
                                if (destinationNode == null)
                                {
                                    destinationNode = new DemandNode<>(destination, stream);
                                    rootNode.addChild(destinationNode);
                                }
                            }
                            else
                            {
                                rootNode = new DemandNode<>(origin, stream);
                                originNodePerLane.put(lane, rootNode);
                                destinationNode = new DemandNode<>(destination, stream);
                                rootNode.addChild(destinationNode);
                            }
                        }
                        DemandNode<Category, ?> categoryNode =
                                new DemandNode<>(category, od.getODEntry(origin, destination, category));
                        destinationNode.addChild(categoryNode);
                    }
                }
            }

            // Step 2: gather DirectedLanePositions for each generator pertaining to each DemandNode<...>
            Map<DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>, Set<DirectedLanePosition>> initialPositions =
                    new HashMap<>();
            if (laneBased)
            {
                for (Lane lane : originNodePerLane.keySet())
                {
                    DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> demandNode = originNodePerLane.get(lane);
                    Set<DirectedLanePosition> initialPosition = new HashSet<>();
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

                Set<DirectedLanePosition> positionSet = new HashSet<>();
                for (Link link : origin.getLinks())
                {
                    if (link instanceof CrossSectionLink)
                    {
                        setDirectedLanePosition((CrossSectionLink) link, origin, positionSet);
                    }
                    else if (link.getLinkType().isConnector())
                    {
                        Node connectedNode = link.getStartNode().equals(origin) ? link.getEndNode() : link.getStartNode();
                        for (Link connectedLink : connectedNode.getLinks())
                        {
                            if (link instanceof CrossSectionLink)
                            {
                                setDirectedLanePosition((CrossSectionLink) connectedLink, connectedNode, positionSet);
                            }
                        }
                    }
                }
                initialPositions.put(rootNode, positionSet);
            }

            // Step 3: create generator(s)
            initialPositions = sortByValue(initialPositions); // sorts by lateral position at link start
            Map<Node, Integer> originGeneratorCounts = new HashMap<>();
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
                HeadwayRandomization randomization = odOptions.get(ODOptions.HEADWAY);
                ODHeadwayGenerator headwayGenerator = new ODHeadwayGenerator(root, simulator, stream, randomization);
                GTUColorer gtuColorer = odOptions.get(ODOptions.COLORER);
                ODCharacteristicsGenerator characteristicsGenerator =
                        new ODCharacteristicsGenerator(root, simulator, odOptions.get(ODOptions.GTU_TYPE), initialPosition);
                RoomChecker roomChecker = odOptions.get(ODOptions.ROOM);
                IdGenerator idGenerator = odOptions.get(ODOptions.ID);
                // bounds
                int maxGTUs = Integer.MAX_VALUE;
                Time startTime = Time.ZERO;
                Time endTime = Time.createSI(Double.POSITIVE_INFINITY);
                // and finally, the generator
                try
                {
                    LaneBasedGTUGenerator generator =
                            new LaneBasedGTUGenerator(id, headwayGenerator, maxGTUs, startTime, endTime, gtuColorer,
                                    characteristicsGenerator, initialPosition, network, simulator, roomChecker, idGenerator);
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
     * Returns a sorted map.
     * @param map Map; input map
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
                String linkId1 = o1.getValue().iterator().next().getLane().getParentLink().getId();
                String linkId2 = o2.getValue().iterator().next().getLane().getParentLink().getId();
                int c = linkId1.compareToIgnoreCase(linkId2);
                if (c == 0)
                {
                    Length lat1 = o1.getValue().iterator().next().getLane().getLateralCenterPosition(0.0);
                    Length lat2 = o2.getValue().iterator().next().getLane().getLateralCenterPosition(0.0);
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
     * @param positionSet Set<DirectedLanePosition>; set to add position to
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
     * To accomplish a branching of Node (origin) > Node (destination) > Category, the following generics types can be used:<br>
     * <br>
     * {@code DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>}
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
    public static class DemandNode<T, K extends DemandNode<?, ?>>
    {

        // TODO Markov chain: on average same probabilities, but after a truck higher truck probability, after car higher car
        // probability

        /** Node object. */
        private final T object;

        /** Random stream to draw child node. */
        private final StreamInterface stream;

        /** Children. */
        private final List<K> children = new ArrayList<>();

        /** Demand data. */
        private final ODEntry odEntry;

        /**
         * Constructor for branching node.
         * @param object T; node object
         * @param stream StreamInterface; random stream to draw child node
         */
        public DemandNode(final T object, final StreamInterface stream)
        {
            this.object = object;
            this.stream = stream;
            this.odEntry = null;
        }

        /**
         * Constructor for leaf node.
         * @param object T; node object
         * @param odEntry ODEntry; demand data
         */
        public DemandNode(final T object, final ODEntry odEntry)
        {
            this.object = object;
            this.stream = null;
            this.odEntry = odEntry;
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
         * Randomly draws a child node.
         * @param time Duration; simulation time
         * @return K; randomly drawn child node
         */
        public K draw(final Time time)
        {
            Throw.when(this.children.isEmpty(), RuntimeException.class, "Calling draw on a leaf node in the demand tree.");
            double[] cumulFrequencies = new double[this.children.size()];
            double sum = 0;
            int index = 0;
            for (K child : this.children)
            {
                double f = child.getFrequency(time, true).si; // sliceStart = true is arbitrary
                sum += f;
                cumulFrequencies[index] = sum;
                index++;
            }
            Throw.when(sum == 0.0, RuntimeException.class, "Draw on destination or category when demand is 0.");
            double r = this.stream.nextDouble() * sum;
            for (int i = 0; i < this.children.size(); i++)
            {
                if (r <= cumulFrequencies[i])
                {
                    return this.children.get(i);
                }
                i++;
            }
            return this.children.get(this.children.size() - 1); // due to rounding
        }

        /**
         * Returns the total demand for branching nodes, or the demand at a leaf node, at the given time.
         * @param time Time; simulation time
         * @param sliceStart boolean; whether the time is at the start of an arbitrary time slice
         * @return Frequency; returns the total demand for branching nodes, or the demand at a leaf node, at the given time
         */
        public Frequency getFrequency(final Time time, final boolean sliceStart)
        {
            if (this.odEntry != null)
            {
                return this.odEntry.getDemand(time, sliceStart);
            }
            Frequency f = new Frequency(0.0, FrequencyUnit.PER_HOUR);
            for (K child : this.children)
            {
                f = f.plus(child.getFrequency(time, sliceStart));
            }
            return f;
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

        /**
         * Returns the start time of the next time slice after the given time or {@code null} if no such slice exists.
         * @param time Time; time after which the first slice start time is requested
         * @return start time of the next time slice after the given time or {@code null} if no such slice exists
         */
        public Time nextTimeSlice(final Time time)
        {
            if (this.odEntry != null)
            {
                for (double d : this.odEntry.getTimeVector().getValuesSI())
                {
                    if (d > time.si)
                    {
                        return Time.createSI(d);
                    }
                }
                return null;
            }
            Time out = null;
            for (K child : this.children)
            {
                Time childSlice = child.nextTimeSlice(time);
                out = out == null || (childSlice != null && childSlice.lt(out)) ? childSlice : out;
            }
            return out;
        }

    }

    /**
     * Headway generation based on OD demand.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class ODHeadwayGenerator implements Generator<Duration>
    {

        /** Root node with origin. */
        private final DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root;

        /** Simulator. */
        private final OTSDEVSSimulatorInterface simulator;

        /** Random stream to draw headway. */
        private final StreamInterface stream;

        /** Random headway generator. */
        private final HeadwayRandomization randomization;

        /**
         * @param root DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>; root node with origin
         * @param simulator OTSDEVSSimulatorInterface; simulator
         * @param stream StreamInterface; random stream to draw headway
         * @param randomization Randomization; random headway generator
         */
        ODHeadwayGenerator(final DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root,
                final OTSDEVSSimulatorInterface simulator, final StreamInterface stream,
                final HeadwayRandomization randomization)
        {
            this.root = root;
            this.simulator = simulator;
            this.stream = stream;
            this.randomization = randomization;
        }

        /**
         * Returns a new headway {@code h} assuming that the previous vehicle arrived at the current time {@code t0}. The
         * vehicle thus arrives at {@code t1 = t0 + h}. This method guarantees that no vehicle arrives during periods where
         * demand is zero, while maintaining random headways based on average demand over a certain time period.<br>
         * <br>
         * The general method is to find {@code h} such that the integral of the demand pattern {@code D} from {@code t0} until
         * {@code t1} equals {@code r}: &#931;{@code D(t0 > t1) = r}. One can think of {@code r} as being 1 and representing an
         * additional vehicle to arrive. The headway {@code h} that results correlates directly to the mean demand between
         * {@code t0} and {@code t1}.<br>
         * <br>
         * The value of {@code r} always has a mean of 1, but may vary between specific vehicle arrivals depending on
         * randomization. When assuming constant headways for any given demand level, {@code r} always equals 1. For
         * exponentially distributed headways {@code r} may range anywhere between 0 and infinity.<br>
         * <br>
         * This usage of {@code r} guarantees that no vehicles arrive during periods with 0 demand. For example:
         * <ul>
         * <li>Suppose we have 0 demand between 300s and 400s.</li>
         * <li>The previous vehicle was generated at 299s.</li>
         * <li>The demand at 299s equals 1800veh/h (1 veh per 2s).</li>
         * <li>For both constant and exponentially distributed headways, the expected next vehicle arrival based on this demand
         * value alone would be 299 + 2 = 301s. This is within the 0-demand period and should not happen. It's also not
         * theoretically sound, as the demand from 299s until 301s is not 1800veh/h on average.</li>
         * <li>Using integration we find that the surface of demand from 299s until 300s equals 0.5 veh for stepwise demand, and
         * 0.25 veh for linear demand. Consequently, the vehicle will not arrive until later slices integrate to an additional
         * 0.5 veh or 0.75 veh respectively. This additional surface under the demand curve is only found after 400s.</li>
         * <li>In case the exponential headway distribution would have resulted in {@code r} < 0.5 (stepwise demand) or 0.25
         * (linear demand), a vehicle will simply arrive between 299s and 300s.</li>
         * </ul>
         * Depending on possibly varying demand patterns in child nodes of the root node, the algorithm may integrate over
         * several time slices, where a slice ends whenever the first child node has a new slice. This assures that during each
         * slice the child nodes as well as their sum have a linear demand pattern. Trapezoidal integration can be used.<br>
         * <br>
         * @return Duration; new headway
         * @throws ProbabilityException if the stored collection is empty
         * @throws ParameterException in case of a parameter exception
         */
        @Override
        public Duration draw() throws ProbabilityException, ParameterException
        {
            Time now = this.simulator.getSimulatorTime().getTime();
            // initial slice times and frequencies
            Time t1 = now;
            double f1 = this.root.getFrequency(t1, true).si;
            Time t2 = this.root.nextTimeSlice(t1);
            if (t2 == null)
            {
                return null; // no new vehicle
            }
            double f2 = this.root.getFrequency(t2, false).si;
            // next vehicle's random factor
            double rem = this.randomization.draw(this.stream);
            // integrate until rem (by reducing it to 0.0, possibly in steps per slice)
            while (rem > 0.0)
            {
                // extrapolate to find 'integration = rem' in this slice giving demand slope, this may beyond the slice length
                double dt = t2.si - t1.si;
                double t;
                if (f1 == f2) // no slope
                {
                    if (f1 > 0.0)
                    {
                        t = rem / f1; // rem = t * f1, t = rem / f1
                    }
                    else
                    {
                        t = Double.POSITIVE_INFINITY; // no demand in this slice
                    }
                }
                else
                {
                    // reverse of trapezoidal rule: rem = t * (f1 + (f1 + t * slope)) / 2
                    double slope = (f2 - f1) / dt;
                    double sqrt = 2 * slope * rem + f1 * f1;
                    if (sqrt >= 0.0)
                    {
                        t = (-f1 + Math.sqrt(sqrt)) / slope;
                    }
                    else
                    {
                        t = Double.POSITIVE_INFINITY; // not sufficient demand in this slice, with negative slope
                    }
                }
                if (t > dt)
                {
                    // next slice
                    rem -= dt * (f1 + f2) / 2; // subtract integral of this slice using trapezoidal rule
                    t1 = t2;
                    t2 = this.root.nextTimeSlice(t1);
                    if (t2 == null)
                    {
                        return null; // no new vehicle
                    }
                    f1 = this.root.getFrequency(t1, true).si; // we can't use f1 = f2 due to possible steps in demand
                    f2 = this.root.getFrequency(t2, false).si;
                }
                else
                {
                    // return resulting integration times
                    return Duration.createSI(t1.si + t - now.si);
                }
            }
            throw new RuntimeException("Exception while determining headway from DemandNode.");
        }

    }

    /**
     * Headway randomization.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum HeadwayRandomization
    {

        /** Constant headway. */
        CONSTANT
        {
            @Override
            double draw(final StreamInterface stream)
            {
                return 1.0;
            }
        },

        /** Exponential headway distribution. */
        EXPONENTIAL
        {
            @Override
            double draw(final StreamInterface stream)
            {
                return -Math.log(stream.nextDouble());
            }
        };

        /**
         * Draws a randomized headway factor.
         * @param stream StreamInterface; random number stream
         * @return randomized headway factor
         */
        abstract double draw(StreamInterface stream);

    }

    /**
     * Characteristics generation based on OD demand.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 7 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class ODCharacteristicsGenerator implements LaneBasedGTUCharacteristicsGenerator
    {

        /** Root node with origin. */
        private final DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root;

        /** Simulator. */
        private final OTSDEVSSimulatorInterface simulator;

        /** Characteristics generator based on OD information. */
        private final GTUCharacteristicsGeneratorOD charachteristicsGenerator;

        /** Initial position. */
        private final Set<DirectedLanePosition> initialPosition;

        /**
         * @param root DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>>; root node with origin
         * @param simulator OTSDEVSSimulatorInterface; simulator
         * @param charachteristicsGenerator GTUCharacteristicsGeneratorOD; characteristics generator based on OD information
         * @param initialPosition Set<DirectedLanePosition>; initial position
         */
        ODCharacteristicsGenerator(final DemandNode<Node, DemandNode<Node, DemandNode<Category, ?>>> root,
                final OTSDEVSSimulatorInterface simulator, final GTUCharacteristicsGeneratorOD charachteristicsGenerator,
                final Set<DirectedLanePosition> initialPosition)
        {
            this.root = root;
            this.simulator = simulator;
            this.charachteristicsGenerator = charachteristicsGenerator;
            this.initialPosition = initialPosition;
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GTUException
        {
            // obtain node objects
            Time time = this.simulator.getSimulatorTime().getTime();
            Node origin = this.root.getObject();
            DemandNode<Node, DemandNode<Category, ?>> destinationNode = this.root.draw(time);
            Node destination = destinationNode.getObject();
            Category category = destinationNode.draw(time).getObject();
            // forward to lower-level generator
            return this.charachteristicsGenerator.draw(origin, destination, category, this.initialPosition);
        }

    }

    /**
     * Class to contain created generator objects.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
