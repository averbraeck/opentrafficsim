package org.opentrafficsim.road.gtu.lane.perception.mental.sdm;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.Task;

import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.Throw;

/**
 * Task as seen by the Stochastic Distraction Model.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 jun. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Distraction
{

    /** Id. */
    private final String id;

    /** Description. */
    private final String description;

    /** Frequency. */
    private final Frequency frequency;

    /** Exposure (value in range [0...1]). */
    private final double exposure;

    /** Random number stream. */
    private final StreamInterface stream;

    /** Task supplier. */
    private final TaskSupplier taskSupplier;

    /** Distribution of duration. */
    private final ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> dist;

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     * @param frequency Frequency; frequency per exposed driver
     * @param exposure double; exposure (value in range [0...1])
     * @param averageDuration Duration; average duration
     * @param stdDuration Duration; standard deviation of duration
     * @param stream StreamInterface; random number stream
     * @param taskSupplier TaskSupplier; task supplier
     */
    public Distraction(final String id, final String description, final Frequency frequency, final double exposure,
            final Duration averageDuration, final Duration stdDuration, final StreamInterface stream,
            final TaskSupplier taskSupplier)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        Throw.whenNull(frequency, "Frequency may not be null.");
        Throw.whenNull(averageDuration, "Average duration may not be null.");
        Throw.whenNull(stream, "Random stream may not be null.");
        Throw.when(exposure < 0.0 || exposure > 1.0, IllegalArgumentException.class, "Exposure should be in the range [0...1]");
        this.id = id;
        this.description = description;
        this.frequency = frequency;
        this.exposure = exposure;
        this.stream = stream;
        this.taskSupplier = taskSupplier;

        double var = stdDuration.si * stdDuration.si;
        double avgSqrd = averageDuration.si * averageDuration.si;
        double mu = Math.log(avgSqrd / Math.sqrt(var + avgSqrd));
        double sigma = Math.sqrt(Math.log(var / avgSqrd + 1.0));
        this.dist = new ContinuousDistDoubleScalar.Rel<>(new DistLogNormal(stream, mu, sigma), DurationUnit.SECOND);
    }

    /**
     * Returns the id.
     * @return String; id
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the description.
     * @return String; description
     */
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the next exposure.
     * @return boolean; next exposure
     */
    public final boolean nextExposure()
    {
        return this.stream.nextDouble() <= this.exposure;
    }

    /**
     * Returns the next inter-arrival time of this secondary task.
     * @return Duration; next inter-arrival time of this secondary task
     */
    public final Duration nextInterArrival()
    {
        return Duration.createSI(-Math.log(this.stream.nextDouble()) / this.frequency.si);
    }

    /**
     * Returns the next duration of this secondary task.
     * @return Duration; next duration of this secondary task
     */
    public final Duration nextDuration()
    {
        return this.dist.draw();
    }

    /**
     * Returns a task for the given GTU.
     * @param gtu LaneBasedGTU; gtu
     * @return Task; task for given GTU
     */
    public final Task getTask(final LaneBasedGTU gtu)
    {
        return this.taskSupplier.getTask(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Distraction [id=" + this.id + ", description=" + this.description + ", frequency=" + this.frequency
                + ", exposure=" + this.exposure + "]";
    }

}
