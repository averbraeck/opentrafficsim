package org.opentrafficsim.road.gtu.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.data.ListTable;
import org.djutils.data.Row;
import org.djutils.data.Table;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.GeneratorLanePosition;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.IdsWithCharacteristics;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.Placement;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Injections can be used to have a large degree of control over GTU generation. Depending on the information provided in an
 * injections table, this class may be used in conjunction with {@code LaneBasedGtuGenerator} as a:
 * <ol>
 * <li>{@code Generator<Duration>} for inter-arrival times</li>
 * <li>{@code LaneBasedGtuCharacteristicsGenerator} through {@code asLaneBasedGtuCharacteristicsGenerator}</li>
 * <li>{@code GeneratorPositions}</li>
 * <li>{@code RoomChecker}</li>
 * <li>{@code Supplier<String>} for GTU ids</li>
 * </ol>
 * Note that there are various {@code asXxx()} methods to supply a view of injections as the components mentioned above.
 * <p>
 * It is assumed that for each next GTU, first an inter-arrival time is requested. Functions 2 and 3 will not check order and
 * simply return information from the current row in the injections table. Function 4 and 5 are tracked independently and
 * asynchronous with the rest, as these occur at later times when GTUs are (attempted to be) placed.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Injections
{

    /** Time column id. */
    public static final String TIME_COLUMN = "time";

    /** Id column id. */
    public static final String ID_COLUMN = "id";

    /** GTU type column id. */
    public static final String GTU_TYPE_COLUMN = "gtuType";

    /** Position (on lane) column id. */
    public static final String POSITION_COLUMN = "position";

    /** Lane column id. */
    public static final String LANE_COLUMN = "lane";

    /** Link column id. */
    public static final String LINK_COLUMN = "link";

    /** Speed column id. */
    public static final String SPEED_COLUMN = "speed";

    /** Origin column id. */
    public static final String ORIGIN_COLUMN = "origin";

    /** Destination column id. */
    public static final String DESTINATION_COLUMN = "destination";

    /** Route column id. */
    public static final String ROUTE_COLUMN = "route";

    /** Length column id. */
    public static final String LENGTH_COLUMN = "length";

    /** Width column id. */
    public static final String WIDTH_COLUMN = "width";

    /** Maximum speed column id. */
    public static final String MAX_SPEED_COLUMN = "maxSpeed";

    /** Maximum acceleration column id. */
    public static final String MAX_ACCELERATION_COLUMN = "maxAcceleration";

    /** Maximum deceleration column id. */
    public static final String MAX_DECELERATION_COLUMN = "maxDeceleration";

    /** Front column id. */
    public static final String FRONT_COLUMN = "front";

    /** Network. */
    private final Network network;

    /** GTU types per their id. */
    private final ImmutableMap<String, GtuType> gtuTypes;

    /** GTU characteristics generator. */
    private final BiFunction<GtuType, StreamInterface, Optional<GtuTemplate>> gtuCharacteristicsGenerator;

    /** Strategical planner factory. */
    private final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory;

    /** Critical time-to-collision for GTU placement. */
    private final Duration timeToCollision;

    /** Random number stream. */
    private final StreamInterface stream;

    /** Stored column numbers for present columns in injection table. */
    private final Map<String, Integer> columnNumbers = new LinkedHashMap<>();

    /** Separate iterator to obtain the id, as this is requested asynchronously with the other characteristics. */
    private final Iterator<Row> idIterator;

    /** Separate iterator to obtain the speed, as this is requested asynchronously with the other characteristics. */
    private final Iterator<Row> speedIterator;

    /** Next speed to generate next GTU with. */
    private Speed nextSpeed;

    /** Iterator over all injections. */
    private final Iterator<Row> characteristicsIterator;

    /** Current row with characteristics from injection. */
    private Row characteristicsRow;

    /** Previous arrival time to calculate inter-arrival time. */
    private Duration previousArrival = Duration.ZERO;

    /** Positions per link, lane and position (on lane). */
    private MultiKeyMap<GeneratorLanePosition> lanePositions;

    /** All lane positions, returned as {@code GeneratorPositions}. */
    private Set<GeneratorLanePosition> allLanePositions;

    /** Cached characteristics generator, to always return the same. */
    private LaneBasedGtuCharacteristicsGenerator characteristicsGenerator;

    /** Boolean to check inter-arrival time and characteristics drawing consistency. */
    private boolean readyForCharacteristicsDraw = false;

    /**
     * Constructor. Depending on what information is provided in the injections table, some arguments may or should not be
     * {@code null}. In particular:
     * <ul>
     * <li>"time": always required, allows the {@code Injections} to be used as a {@code Generator<Duration>}.</li>
     * <li>"id": allows the {@code Injections} to be used as a {@code Supplier<String>} for GTU ids.</li>
     * <li>"position", "lane", "link": allows the {@code Injections} to be used as a {@code GeneratorPositions}, requires
     * <b>network</b>.</li>
     * <li>"speed": allows the {@code Injections} to be used as a {@code RoomChecker}, requires <b>timeToCollision</b>.</li>
     * <li><i>all other columns</i>: allows the {@code Injections} to be used as a {@code LaneBasedGtuCharacteristicsGenerator}
     * through {@code asLaneBasedGtuCharacteristicsGenerator()}, requires <b>gtuTypes</b>, <b>network</b>,
     * <b>strategicalPlannerFactory</b> and <b>stream</b>; gtuCharacteristicsGenerator may then be null.
     * </ul>
     * Time should be in increasing order. If length is provided, but no front, front will be 75% of the length.
     * @param table table with at least a "time" column.
     * @param network network, may be {@code null}.
     * @param gtuTypes GTU types, as obtained from {@code Definitions}, may be {@code null}.
     * @param gtuCharacteristicsGenerator generator of GTU characteristics, may be {@code null}.
     * @param strategicalPlannerFactory strategical planner factory, may be {@code null}.
     * @param stream random number stream, may be {@code null}.
     * @param timeToCollision critical time-to-collision to allow GTU generation, may be {@code null}.
     * @throws IllegalArgumentException when the right arguments are not provided for the columns in the injection table.
     */
    public Injections(final Table table, final Network network, final ImmutableMap<String, GtuType> gtuTypes,
            final BiFunction<GtuType, StreamInterface, Optional<GtuTemplate>> gtuCharacteristicsGenerator,
            final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory, final StreamInterface stream,
            final Duration timeToCollision) throws IllegalArgumentException
    {
        Throw.whenNull(table, "Table may not be null.");
        Table sortedTable = sortTable(table);
        this.idIterator = sortedTable.iterator();
        this.speedIterator = sortedTable.iterator();
        this.characteristicsIterator = sortedTable.iterator();
        this.network = network;
        this.gtuTypes = gtuTypes == null ? new ImmutableLinkedHashMap<>(Collections.emptyMap()) : gtuTypes;
        this.gtuCharacteristicsGenerator = gtuCharacteristicsGenerator;
        this.strategicalPlannerFactory = strategicalPlannerFactory;
        this.timeToCollision = timeToCollision;
        this.stream = stream;

        sortedTable.getColumns().forEach((c) -> this.columnNumbers.put(c.getId(), sortedTable.getColumnNumber(c)));
        boolean needStrategicalPlannerFactory = checkColumnTypesNeedStrategicalPlannerFactory(sortedTable);
        Throw.when(needStrategicalPlannerFactory && (gtuTypes == null || gtuTypes.isEmpty()), IllegalArgumentException.class,
                "Injection table contains columns that require GTU types.");
        Throw.when(needStrategicalPlannerFactory && strategicalPlannerFactory == null, IllegalArgumentException.class,
                "Injection table contains columns that require a strategical planner factory.");
        Throw.when(needStrategicalPlannerFactory && network == null, IllegalArgumentException.class,
                "Injection table contains columns that require a network.");
        Throw.when(needStrategicalPlannerFactory && stream == null, IllegalArgumentException.class,
                "Injection table contains columns that require a stream of random numbers.");
        Throw.when(!this.columnNumbers.containsKey(TIME_COLUMN), IllegalArgumentException.class,
                "Injection table contains no time column.");

        createLanePositions(sortedTable);
    }

    /**
     * Makes sure the table is sorted by the time column.
     * @param table input table.
     * @return table sorted by time column.
     */
    private static Table sortTable(final Table table)
    {
        int timeColumn = table.getColumnNumber(TIME_COLUMN);
        Iterator<Row> iterator = table.iterator();
        Duration prev = iterator.hasNext() ? (Duration) iterator.next().getValue(timeColumn) : null;
        while (iterator.hasNext())
        {
            Duration next = (Duration) iterator.next().getValue(timeColumn);
            if (next.lt(prev))
            {
                // data is not in order
                List<Row> data = new ArrayList<>();
                for (Row row : table)
                {
                    data.add(row);
                }
                Collections.sort(data, new Comparator<Row>()
                {
                    @Override
                    public int compare(final Row o1, final Row o2)
                    {
                        return ((Duration) o1.getValue(timeColumn)).compareTo((Duration) o2.getValue(timeColumn));
                    }
                });
                ListTable out = new ListTable(table.getId(), table.getDescription(), table.getColumns().toList());
                for (Row row : data)
                {
                    out.addRow(row.getValues());
                }
                return out;
            }
            prev = next;
        }
        return table;
    }

    /**
     * Checks whether all columns have the right value type.
     * @param table injection table.
     * @return whether columns are present that require a strategical planner factory in order to be processed.
     */
    private boolean checkColumnTypesNeedStrategicalPlannerFactory(final Table table)
    {
        boolean needStrategicalPlannerFactory = false;
        for (Entry<String, Integer> entry : this.columnNumbers.entrySet())
        {
            Class<?> needClass;
            switch (entry.getKey())
            {
                case TIME_COLUMN:
                    needClass = Duration.class;
                    break;
                case ID_COLUMN:
                case LANE_COLUMN:
                case LINK_COLUMN:
                    needClass = String.class;
                    break;
                case GTU_TYPE_COLUMN:
                case ORIGIN_COLUMN:
                case DESTINATION_COLUMN:
                case ROUTE_COLUMN:
                    needClass = String.class;
                    needStrategicalPlannerFactory = true;
                    break;
                case SPEED_COLUMN:
                    needClass = Speed.class;
                    break;
                case MAX_SPEED_COLUMN:
                    needClass = Speed.class;
                    needStrategicalPlannerFactory = true;
                    break;
                case POSITION_COLUMN:
                    needClass = Length.class;
                    break;
                case LENGTH_COLUMN:
                case WIDTH_COLUMN:
                case FRONT_COLUMN:
                    needClass = Length.class;
                    needStrategicalPlannerFactory = true;
                    break;
                case MAX_ACCELERATION_COLUMN:
                case MAX_DECELERATION_COLUMN:
                    needClass = Acceleration.class;
                    needStrategicalPlannerFactory = true;
                    break;
                default:
                    Logger.ots().info("Column " + entry.getKey() + " for GTU injection not supported. It is ignored.");
                    needClass = null;
            }
            if (needClass != null)
            {
                Class<?> columnValueClass = table.getColumn(entry.getValue()).getValueType();
                Throw.when(!needClass.isAssignableFrom(columnValueClass), IllegalArgumentException.class,
                        "Column %s has value type %s, but type %s is required.", entry.getKey(), columnValueClass, needClass);
            }
        }
        return needStrategicalPlannerFactory;
    }

    /**
     * Creates all the lane positions for GTU generation.
     * @param table injection table.
     */
    private void createLanePositions(final Table table)
    {
        if (this.columnNumbers.containsKey(POSITION_COLUMN) && this.columnNumbers.containsKey(LANE_COLUMN)
                && this.columnNumbers.containsKey(LINK_COLUMN))
        {
            this.lanePositions = new MultiKeyMap<>(String.class, String.class, Length.class);
            this.allLanePositions = new LinkedHashSet<>();
            for (Row row : table)
            {
                String linkId = (String) row.getValue(this.columnNumbers.get(LINK_COLUMN));
                Link link = this.network.getLink(linkId).orElseThrow(
                        () -> new IllegalArgumentException("Link " + linkId + " in injections is not in the network."));
                Throw.when(!(link instanceof CrossSectionLink), IllegalArgumentException.class,
                        "Injection table contains link that is not a CrossSectionLink.");

                String laneId = (String) row.getValue(this.columnNumbers.get(LANE_COLUMN));
                // get and sort lanes to get the lane number (1 = right-most lane)
                List<Lane> lanes = ((CrossSectionLink) link).getLanes();
                Collections.sort(lanes, new Comparator<Lane>()
                {
                    @Override
                    public int compare(final Lane o1, final Lane o2)
                    {
                        return o1.getOffsetAtBegin().compareTo(o2.getOffsetAtBegin());
                    }
                });
                int laneNumber = 0;
                for (int i = 0; i < lanes.size(); i++)
                {
                    if (lanes.get(i).getId().equals(laneId))
                    {
                        laneNumber = i + 1;
                        break;
                    }
                }
                Throw.when(laneNumber == 0, IllegalArgumentException.class,
                        "Injection table contains lane %s on link %s, but the link has no such lane.", laneId, linkId);

                Length position = (Length) row.getValue(this.columnNumbers.get(POSITION_COLUMN));
                Throw.when(position.lt0() || position.gt(lanes.get(laneNumber - 1).getLength()), IllegalArgumentException.class,
                        "Injection table contains position %s on lane %s on link %s, but the position is negative or "
                                + "beyond the length of the lane.",
                        position, laneId, linkId);

                GeneratorLanePosition generatorLanePosition = new GeneratorLanePosition(laneNumber,
                        new LanePosition(lanes.get(laneNumber - 1), position), (CrossSectionLink) link);
                if (this.allLanePositions.add(generatorLanePosition))
                {
                    this.lanePositions.put(generatorLanePosition, linkId, laneId, position);
                }
            }
        }
        else if (this.columnNumbers.containsKey(POSITION_COLUMN) || this.columnNumbers.containsKey(LANE_COLUMN)
                || this.columnNumbers.containsKey(LINK_COLUMN))
        {
            throw new IllegalArgumentException(
                    "For injections to be used as GeneratorPositions, define a link, lane and position (on lane) column."
                            + " Only partial information is found.");
        }
    }

    /**
     * Returns whether the column of given id is present.
     * @param columnId column id.
     * @return whether the column of given id is present.
     */
    public boolean hasColumn(final String columnId)
    {
        return this.columnNumbers.containsKey(columnId);
    }

    /**
     * Returns an Supplier&lt;String&gt; view as id supplier of injections.
     * @return Supplier&lt;String&gt; view as id supplier of injections
     */
    public Supplier<String> asIdSupplier()
    {
        return new IdsWithCharacteristics()
        {
            @Override
            public String get()
            {
                // This method implements Supplier<String> as an id generator.
                Throw.when(!Injections.this.idIterator.hasNext(), NoSuchElementException.class, "No more ids to draw.");
                Throw.when(!Injections.this.columnNumbers.containsKey(ID_COLUMN), IllegalStateException.class,
                        "Using Injections as id generator, but the injection table has no id column.");
                return (String) Injections.this.idIterator.next().getValue(Injections.this.columnNumbers.get(ID_COLUMN));
            }

            @Override
            public boolean hasIds()
            {
                return hasColumn(ID_COLUMN);
            }
        };
    }

    /**
     * Returns a Supplier&lt;Duration&gt; view to supply inter-arrival time of injections.
     * @return Supplier&lt;Duration&gt; view to supply inter-arrival time of injections
     */
    public Supplier<Duration> asArrivalsSupplier()
    {
        return new Supplier<Duration>()
        {
            @Override
            public synchronized Duration get()
            {
                if (!Injections.this.characteristicsIterator.hasNext())
                {
                    return null; // stops LaneBasedGtuGenerator
                }
                Injections.this.characteristicsRow = Injections.this.characteristicsIterator.next();
                Injections.this.readyForCharacteristicsDraw = true;
                Duration t = (Duration) getCharacteristic(TIME_COLUMN);
                Throw.when(t.lt(Injections.this.previousArrival), IllegalStateException.class,
                        "Arrival times in injection not increasing.");
                Duration interArrivalTime = t.minus(Injections.this.previousArrival);
                Injections.this.previousArrival = t;
                return interArrivalTime;
            }
        };
    }

    /**
     * Returns a characteristics generator view of the injections, as used by {@code LaneBasedGtuGenerator}. This requires at
     * the least that a GTU type column, a strategical planner factory, a network, and a stream of random numbers are provided.
     * @return characteristics generator view of the injections.
     */
    public LaneBasedGtuCharacteristicsGenerator asLaneBasedGtuCharacteristicsGenerator()
    {
        if (this.characteristicsGenerator == null)
        {
            Throw.when(!this.columnNumbers.containsKey(GTU_TYPE_COLUMN), IllegalStateException.class,
                    "A GTU type column is required for generation of characteristics.");

            this.characteristicsGenerator = new LaneBasedGtuCharacteristicsGenerator()
            {
                /** Default characteristics, generated as needed. */
                private GtuCharacteristics defaultCharacteristics;

                @Override
                public LaneBasedGtuCharacteristics draw() throws ParameterException, GtuException
                {
                    synchronized (Injections.this)
                    {
                        Throw.when(Injections.this.characteristicsRow == null, IllegalStateException.class,
                                "Must draw inter-arrival time before drawing GTU characteristics.");
                        Throw.when(!Injections.this.readyForCharacteristicsDraw, IllegalStateException.class,
                                "Should not draw GTU characteristics again before inter-arrival time was drawn in between.");
                        Injections.this.readyForCharacteristicsDraw = false;
                        GtuType gtuType = Injections.this.gtuTypes.get((String) getCharacteristic(GTU_TYPE_COLUMN));

                        Length length = (Length) assureCharacteristic(LENGTH_COLUMN, gtuType, (g) -> g.getLength());
                        Length width = (Length) assureCharacteristic(WIDTH_COLUMN, gtuType, (g) -> g.getWidth());
                        Speed maxSpeed = (Speed) assureCharacteristic(MAX_SPEED_COLUMN, gtuType, (g) -> g.getMaximumSpeed());
                        Acceleration maxAcceleration = (Acceleration) assureCharacteristic(MAX_ACCELERATION_COLUMN, gtuType,
                                (g) -> g.getMaximumAcceleration());
                        Acceleration maxDeceleration = (Acceleration) assureCharacteristic(MAX_DECELERATION_COLUMN, gtuType,
                                (g) -> g.getMaximumDeceleration());
                        this.defaultCharacteristics = null; // reset for next draw
                        Length front = Injections.this.columnNumbers.containsKey(FRONT_COLUMN)
                                ? (Length) getCharacteristic(FRONT_COLUMN) : length.times(0.75);
                        GtuCharacteristics characteristics = new GtuCharacteristics(gtuType, length, width, maxSpeed,
                                maxAcceleration, maxDeceleration, front);

                        Route route = Injections.this.columnNumbers.containsKey(ROUTE_COLUMN)
                                ? (Route) Injections.this.network.getRoute((String) getCharacteristic(ROUTE_COLUMN)).get()
                                : null;
                        Node origin = Injections.this.columnNumbers.containsKey(ORIGIN_COLUMN)
                                ? (Node) Injections.this.network.getNode((String) getCharacteristic(ORIGIN_COLUMN)).get()
                                : null;
                        Node destination = Injections.this.columnNumbers.containsKey(DESTINATION_COLUMN)
                                ? (Node) Injections.this.network.getNode((String) getCharacteristic(DESTINATION_COLUMN)).get()
                                : null;
                        return new LaneBasedGtuCharacteristics(characteristics, Injections.this.strategicalPlannerFactory,
                                route, origin, destination, VehicleModel.MINMAX);
                    }
                }

                /**
                 * Tries to obtain a column value. If it is not provided, takes the value from generated default
                 * characteristics.
                 * @param column characteristic column name.
                 * @param gtuType GTU type of the GTU to be generated.
                 * @param supplier takes value from default characteristics.
                 * @return object value for the characteristic.
                 * @throws GtuException; if there are no default characteristics for the GTU type, but these are required.
                 */
                private Object assureCharacteristic(final String column, final GtuType gtuType,
                        final Function<GtuCharacteristics, ?> supplier) throws GtuException
                {
                    if (Injections.this.columnNumbers.containsKey(column))
                    {
                        return getCharacteristic(column);
                    }
                    if (this.defaultCharacteristics == null)
                    {
                        this.defaultCharacteristics = Injections.this.gtuCharacteristicsGenerator
                                .apply(gtuType, Injections.this.stream).orElseThrow(() -> new OtsRuntimeException(
                                        "Unable to et GTU characteristics for GTU type " + gtuType.getId()))
                                .get();
                    }
                    return supplier.apply(this.defaultCharacteristics);
                }
            };
        }
        return this.characteristicsGenerator;
    }

    /**
     * Returns a GeneratorPositions view of injections.
     * @return GeneratorPositions view of injections
     */
    public GeneratorPositions asGeneratorPositions()
    {
        return new GeneratorPositions()
        {
            @Override
            public GeneratorLanePosition draw(final GtuType gtuType, final LaneBasedGtuCharacteristics characteristics,
                    final Map<CrossSectionLink, Map<Integer, Integer>> unplaced) throws GtuException
            {
                Throw.when(Injections.this.lanePositions == null, IllegalStateException.class,
                        "Injection table without position, lane and link column cannot be used to draw generator positions.");
                String link = (String) getCharacteristic(LINK_COLUMN);
                String lane = (String) getCharacteristic(LANE_COLUMN);
                Length position = (Length) getCharacteristic(POSITION_COLUMN);
                return Injections.this.lanePositions.get(link, lane, position);
            }

            @Override
            public Set<GeneratorLanePosition> getAllPositions()
            {
                Throw.when(Injections.this.lanePositions == null, IllegalStateException.class,
                        "Injection table without position, lane and link column cannot be used to draw generator positions.");
                return Injections.this.allLanePositions;
            }
        };
    }

    /**
     * Returns RoomChecker view of injections.
     * @return RoomChecker view of injections
     */
    public RoomChecker asRoomChecker()
    {
        return new RoomChecker()
        {
            /**
             * Returns placement for injected GTUs, as used by {@code LaneBasedGtuGenerator}. This needs speed to be provided in
             * the injections, and a minimum time-to-collision value. Besides the time-to-collision value, the minimum headway
             * for a successful placement is t*v + 3m, where t = 1s and v the generation speed.
             * @param leaders leaders, usually 1, possibly more after a branch
             * @param characteristics characteristics of the proposed new GTU
             * @param since time since the GTU wanted to arrive
             * @param initialPosition initial position
             * @return maximum safe speed, or Placement.NO if a GTU with the specified characteristics cannot be placed at the
             *         current time
             * @throws NetworkException this method may throw a NetworkException if it encounters an error in the network
             *             structure
             * @throws GtuException on parameter exception
             */
            @Override
            public Placement canPlace(final SortedSet<PerceivedGtu> leaders, final LaneBasedGtuCharacteristics characteristics,
                    final Duration since, final LanePosition initialPosition) throws NetworkException, GtuException
            {
                Throw.when(!Injections.this.columnNumbers.containsKey(SPEED_COLUMN), IllegalStateException.class,
                        "Injection table without speed cannot be used to determine a GTU placement.");
                Throw.when(Injections.this.timeToCollision == null, IllegalStateException.class,
                        "Injections used to place GTUs, but no acceptable time-to-collision is provided.");
                if (Injections.this.nextSpeed == null)
                {
                    Throw.when(!Injections.this.speedIterator.hasNext(), NoSuchElementException.class,
                            "No more speed to draw.");
                    Injections.this.nextSpeed = (Speed) Injections.this.speedIterator.next()
                            .getValue(Injections.this.columnNumbers.get(SPEED_COLUMN));
                }
                if (leaders.isEmpty())
                {
                    // no leaders: free
                    Placement placement = new Placement(Injections.this.nextSpeed, initialPosition);
                    Injections.this.nextSpeed = null;
                    return placement;
                }
                PerceivedGtu leader = leaders.first();
                if ((Injections.this.nextSpeed.le(leader.getSpeed()) || leader.getDistance()
                        .divide(Injections.this.nextSpeed.minus(leader.getSpeed())).gt(Injections.this.timeToCollision))
                        && leader.getDistance().gt(Injections.this.nextSpeed.times(new Duration(1.0, DurationUnit.SI))
                                .plus(new Length(3.0, LengthUnit.SI))))
                {
                    Placement placement = new Placement(Injections.this.nextSpeed, initialPosition);
                    Injections.this.nextSpeed = null;
                    return placement;
                }
                return Placement.NO;
            }
        };
    }

    /**
     * Shorthand to retrieve a column value from the current characteristics row.
     * @param column characteristic column name.
     * @return object value for the characteristic.
     */
    private Object getCharacteristic(final String column)
    {
        return this.characteristicsRow.getValue(this.columnNumbers.get(column));
    }

}
