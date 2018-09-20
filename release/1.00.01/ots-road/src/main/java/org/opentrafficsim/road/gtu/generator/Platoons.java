package org.opentrafficsim.road.gtu.generator;

import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.generator.od.GTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.LaneDirection;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.Throw;

/**
 * Connects with a lane-based GTU generator to disable it over some time and generate a platoon instead. Platoons are added as:
 * {@code new Platoons(...).addPlatoon(...).addGtu(...).addGtu(...).addPlatoon(...).addGtu(...).addGtu(...).start();}. Method
 * {@code addGtu(...)} may only determine a generation time if other info is set by {@code fixInfo(...)}.<br>
 * <br>
 * This class bay be used with a {@code LaneBasedGTUCharacteristicsGenerator} or {@code GTUCharacteristicsGeneratorOD}. In the 
 * former case the origin and destination nodes as well as the OD category can be {@code null}.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 sep. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Platoons
{

    /** GTU generator to disable. */
    private final LaneBasedGTUGenerator generator;

    /** Characteristics generator. */
    private final LaneBasedGTUCharacteristicsGenerator characteristics;

    /** Characteristics generator OD based. */
    private final GTUCharacteristicsGeneratorOD characteristicsOD;

    /** Simulator. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** Random number stream. */
    private final StreamInterface stream;

    /** Position to generate the GTU's at. */
    private final Set<DirectedLanePosition> position;

    /** Queue of GTU information to generate GTU's with. */
    private final Queue<PlatoonGtu> queue = new PriorityQueue<>();

    /** Start time of current platoon. */
    private Time startTime;

    /** End time of current platoon. */
    private Time endTime;

    /** Origin to use on added GTU. */
    private Node fixedOrigin;

    /** Destination to use on added GTU. */
    private Node fixedDestination;

    /** Category to use on added GTU. */
    private Category fixedCategory;

    /** Speed to use on added GTU. */
    private Speed fixedSpeed;

    /** Whether the Platoons was started, after which nothing may be added. */
    private boolean started = false;

    /**
     * Constructor.
     * @param generator LaneBasedGTUGenerator; GTU generator
     * @param characteristics LaneBasedGTUCharacteristicsGenerator; characteristics generator
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param position Set&lt;DirectedLanePosition&gt;; position
     */
    public Platoons(final LaneBasedGTUGenerator generator, final LaneBasedGTUCharacteristicsGenerator characteristics,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final Set<DirectedLanePosition> position)
    {
        this.generator = generator;
        this.characteristics = characteristics;
        this.characteristicsOD = null;
        this.simulator = simulator;
        this.stream = null;
        this.position = position;
    }

    /**
     * Constructor.
     * @param generator LaneBasedGTUGenerator; GTU generator
     * @param characteristics GTUCharacteristicsGeneratorOD; characteristics generator
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param stream StreamInterface; random number stream
     * @param position Set&lt;DirectedLanePosition&gt;; position
     */
    public Platoons(final LaneBasedGTUGenerator generator, final GTUCharacteristicsGeneratorOD characteristics,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final StreamInterface stream, final Set<DirectedLanePosition> position)
    {
        this.generator = generator;
        this.characteristics = null;
        this.characteristicsOD = characteristics;
        this.simulator = simulator;
        this.stream = stream;
        this.position = position;
    }

    /**
     * Add a platoon. The generator is disabled during the provided time frame. Individual GTU's should be added using
     * {@code addGtu}.
     * @param start Time; start time
     * @param end Time; end time
     * @return Platoons; for method chaining
     * @throws SimRuntimeException on exception
     */
    public Platoons addPlatoon(final Time start, final Time end) throws SimRuntimeException
    {
        Throw.when(this.started, IllegalStateException.class, "Cannot add a platoon after the Platoons was started.");
        Throw.whenNull(start, "Start may not be null.");
        Throw.whenNull(end, "End may not be null.");
        Set<LaneDirection> laneDirections = new LinkedHashSet<>();
        for (DirectedLanePosition pos : this.position)
        {
            laneDirections.add(pos.getLaneDirection());
        }
        this.generator.disable(start, end, laneDirections);
        this.startTime = start;
        this.endTime = end;
        return this;
    }

    /**
     * Fix all info except time for GTU's added hereafter.
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category Category; category
     * @param speed Speed; generation speed
     * @return Platoons; for method chaining
     */
    public Platoons fixInfo(final Node origin, final Node destination, final Category category, final Speed speed)
    {
        this.fixedOrigin = origin;
        this.fixedDestination = destination;
        this.fixedCategory = category;
        this.fixedSpeed = speed;
        return this;
    }

    /**
     * Add GTU to the current platoon. Per platoon, GTU may be given in any order. This method uses info set with
     * {@code fixInfo}.
     * @param time Time; time of generation
     * @return Platoons; for method chaining
     * @throws IllegalStateException if no fixed info was set using {@code fixInfo}
     */
    public Platoons addGtu(final Time time)
    {
        Throw.when(
                this.fixedOrigin == null || this.fixedDestination == null || this.fixedCategory == null
                        || this.fixedSpeed == null,
                IllegalStateException.class, "When using addGtu(Time), used fixInfo(...) before to set other info.");
        return addGtu(time, this.fixedOrigin, this.fixedDestination, this.fixedCategory, this.fixedSpeed);
    }

    /**
     * Add GTU to the current platoon. Per platoon, GTU may be given in any order.
     * @param time Time; time of generation
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category Category; category
     * @param speed Speed; generation speed
     * @return Platoons; for method chaining
     * @throws IllegalStateException if no platoon was started or time is outside of the platoon time range
     */
    public Platoons addGtu(final Time time, final Node origin, final Node destination, final Category category,
            final Speed speed)
    {
        Throw.when(this.started, IllegalStateException.class, "Cannot add a GTU after the Platoons was started.");
        Throw.when(this.startTime == null || this.endTime == null, IllegalStateException.class,
                "First call addPlatoon() before calling addGtu()");
        Throw.when(time.gt(this.endTime) || time.lt(this.startTime), IllegalArgumentException.class,
                "Time %s is not between %s and %s", time, this.startTime, this.endTime);
        this.queue.add(new PlatoonGtu(time, origin, destination, category, speed));
        return this;
    }

    /**
     * Starts the events.
     * @throws SimRuntimeException if start of first platoon is in the past
     */
    public void start() throws SimRuntimeException
    {
        this.started = true;
        if (!this.queue.isEmpty())
        {
            this.simulator.scheduleEventAbs(this.queue.peek().getTime(), this, this, "placeGtu",
                    new Object[] { this.queue.poll() });
        }
    }

    /**
     * Places the next platoon GTU and schedules the next one.
     * @param platoonGtu PlatoonGtu; info of GTU to generate
     * @throws SimRuntimeException on exception
     * @throws NamingException on exception
     * @throws GTUException on exception
     * @throws NetworkException on exception
     * @throws OTSGeometryException on exception
     * @throws ParameterException on exception
     * @throws ProbabilityException on exception
     */
    @SuppressWarnings("unused") // scheduled
    private void placeGtu(final PlatoonGtu platoonGtu) throws SimRuntimeException, NamingException, GTUException,
            NetworkException, OTSGeometryException, ProbabilityException, ParameterException
    {
        if (this.characteristicsOD == null)
        {
            this.generator.placeGtu(this.characteristics.draw(), this.position, platoonGtu.getSpeed());
        }
        else
        {
            this.generator.placeGtu(this.characteristicsOD.draw(platoonGtu.getOrigin(), platoonGtu.getDestination(),
                    platoonGtu.getCategory(), this.stream), this.position, platoonGtu.getSpeed());
        }
        start(); // next GTU
    }

    /**
     * Class containing info of a GTU to generate.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 sep. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class PlatoonGtu implements Comparable<PlatoonGtu>
    {

        /** Time to generate. */
        private final Time time;

        /** Origin. */
        private final Node origin;

        /** Destination. */
        private final Node destination;

        /** Category. */
        private final Category category;

        /** Generation speed. */
        private final Speed speed;

        /**
         * Constructor.
         * @param time Time; time to generate
         * @param origin Node; origin
         * @param destination Node; destination
         * @param category Category; category
         * @param speed Speed; generation speed
         */
        PlatoonGtu(final Time time, final Node origin, final Node destination, final Category category, final Speed speed)
        {
            this.time = time;
            this.origin = origin;
            this.destination = destination;
            this.category = category;
            this.speed = speed;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final PlatoonGtu o)
        {
            if (o == null)
            {
                return 1;
            }
            return this.time.compareTo(o.time);
        }

        /**
         * @return time.
         */
        protected Time getTime()
        {
            return this.time;
        }

        /**
         * @return origin.
         */
        protected Node getOrigin()
        {
            return this.origin;
        }

        /**
         * @return destination.
         */
        protected Node getDestination()
        {
            return this.destination;
        }

        /**
         * @return category.
         */
        protected Category getCategory()
        {
            return this.category;
        }

        /**
         * @return speed.
         */
        protected Speed getSpeed()
        {
            return this.speed;
        }

    }
}
