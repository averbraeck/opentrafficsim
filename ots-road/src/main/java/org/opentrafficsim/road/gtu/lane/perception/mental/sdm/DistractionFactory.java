package org.opentrafficsim.road.gtu.lane.perception.mental.sdm;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Utility to create a list of default distractions as derived by the research of Manuel Lindorfer.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DistractionFactory
{

    /** Random number stream. */
    private final StreamInterface stream;

    /** List of distractions. */
    private List<Distraction> list = new ArrayList<>();

    /**
     * Constructor.
     * @param stream random number stream
     */
    public DistractionFactory(final StreamInterface stream)
    {
        this.stream = stream;
    }

    /**
     * Adds a default distraction.
     * @param defaultDistraction default distraction
     * @param taskDemand task demand
     * @return this factory for method chaining
     */
    public final DistractionFactory addDistraction(final DefaultDistraction defaultDistraction, final double taskDemand)
    {
        return addDistraction(defaultDistraction.getId(), defaultDistraction.getDescription(),
                defaultDistraction.getFrequency(), defaultDistraction.getExposure(), defaultDistraction.getAverageDuration(),
                defaultDistraction.getStdDuration(), taskDemand);
    }

    /**
     * Adds a default distraction.
     * @param defaultDistraction default distraction
     * @param taskSupplier task supplier
     * @return this factory for method chaining
     */
    public final DistractionFactory addDistraction(final DefaultDistraction defaultDistraction, final TaskSupplier taskSupplier)
    {
        return addDistraction(defaultDistraction.getId(), defaultDistraction.getDescription(),
                defaultDistraction.getFrequency(), defaultDistraction.getExposure(), defaultDistraction.getAverageDuration(),
                defaultDistraction.getStdDuration(), taskSupplier);
    }

    /**
     * Helper method to create a distraction.
     * @param id id
     * @param description description
     * @param frequency frequency per exposed driver
     * @param exposure exposure (value in range [0...1])
     * @param averageDuration average duration
     * @param stdDuration standard deviation of duration
     * @param taskDemand task demand
     * @return this factory for method chaining
     */
    public final DistractionFactory addDistraction(final String id, final String description, final Frequency frequency,
            final double exposure, final Duration averageDuration, final Duration stdDuration, final double taskDemand)
    {
        addDistraction(id, description, frequency, exposure, averageDuration, stdDuration,
                new TaskSupplier.Constant(id, taskDemand));
        return this;
    }

    /**
     * Helper method to create a distraction.
     * @param id id
     * @param description description
     * @param frequency frequency per exposed driver
     * @param exposure exposure (value in range [0...1])
     * @param averageDuration average duration
     * @param stdDuration standard deviation of duration
     * @param taskSupplier task supplier
     * @return this factory for method chaining
     */
    public final DistractionFactory addDistraction(final String id, final String description, final Frequency frequency,
            final double exposure, final Duration averageDuration, final Duration stdDuration, final TaskSupplier taskSupplier)
    {
        this.list.add(
                new Distraction(id, description, frequency, exposure, averageDuration, stdDuration, this.stream, taskSupplier));
        return this;
    }

    /**
     * Returns the list of distractions.
     * @return list of distractions
     */
    public final List<Distraction> build()
    {
        return this.list;
    }

}
