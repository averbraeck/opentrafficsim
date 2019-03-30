package org.opentrafficsim.road.gtu.lane.perception.mental.sdm;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Utility to create a list of default distractions as derived by the research of Manuel Lindorfer.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 jun. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DistractionFactory
{

    /** Random number stream. */
    private final StreamInterface stream;

    /** List of distractions. */
    private List<Distraction> list = new ArrayList<>();

    /**
     * Constructor.
     * @param stream StreamInterface; random number stream
     */
    public DistractionFactory(final StreamInterface stream)
    {
        this.stream = stream;
    }

    /**
     * Adds a default distraction.
     * @param defaultDistraction DefaultDistraction; default distraction
     * @param taskDemand double; task demand
     * @return DistractionFactory; this factory for method chaining
     */
    public final DistractionFactory addDistraction(final DefaultDistraction defaultDistraction, final double taskDemand)
    {
        return addDistraction(defaultDistraction.getId(), defaultDistraction.getDescription(),
                defaultDistraction.getFrequency(), defaultDistraction.getExposure(), defaultDistraction.getAverageDuration(),
                defaultDistraction.getStdDuration(), taskDemand);
    }

    /**
     * Helper method to create a distraction.
     * @param id String; id
     * @param description String; description
     * @param frequency Frequency; frequency per exposed driver
     * @param exposure double; exposure (value in range [0...1])
     * @param averageDuration Duration; average duration
     * @param stdDuration Duration; standard deviation of duration
     * @param taskDemand double; task demand
     * @return DistractionFactory; this factory for method chaining
     */
    public final DistractionFactory addDistraction(final String id, final String description, final Frequency frequency,
            final double exposure, final Duration averageDuration, final Duration stdDuration, final double taskDemand)
    {
        this.list.add(new Distraction(id, description, frequency, exposure, averageDuration, stdDuration, this.stream,
                new TaskSupplier.Constant(taskDemand)));
        return this;
    }

    /**
     * Returns the list of distractions.
     * @return List; list of distractions
     */
    public final List<Distraction> build()
    {
        return this.list;
    }

}