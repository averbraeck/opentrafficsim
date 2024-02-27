package org.opentrafficsim.road.gtu.lane.perception.mental.sdm;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;

/**
 * Set of default distractions as derived by the research of Manuel Lindorfer. These only describe the statistics. Actual
 * {@code Distraction}s are linked to the simulation. {@code DistractionFactory} can be used to create those.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum DefaultDistraction
{
    /** Talking on cell phone. */
    TALKING_CELL_PHONE("1", "Talking on cell phone", freq(100, 0.329), 0.329, dur(92.65), dur(176.29)),

    /** Answering cell phone. */
    ANSWERING_CELL_PHONE("2", "Answering cell phone", freq(15, 0.157), 0.157, dur(7.86), dur(4.24)),

    /** Dialing cell phone. */
    DIALING_CELL_PHONE("3", "Dialing cell phone", freq(122, 0.357), 0.357, dur(12.85), dur(13.41)),

    /** Drinking. */
    DRINKING("4", "Drinking", freq(1028, 0.729), 0.729, dur(5.23), dur(7.4)),

    /** Manipulating audio controls. */
    MANIPULATING_AUDIO_CONTROLS("5", "Manipulating audio controls", freq(1539, 0.943), 0.943, dur(5.46), dur(8.63)),

    /** Smoking. */
    SMOKING("6", "Smoking", freq(45, 0.071), 0.071, dur(245.81), dur(162.95)),

    /** Reading or writing. */
    READING_WRITING("7", "Reading or writing", freq(303, 0.643), 0.643, dur(18.43), dur(29.7)),

    /** Grooming. */
    GROOMING("8", "Grooming", freq(229, 0.571), 0.571, dur(11.82), dur(29.77)),

    /** Baby distracting. */
    BABY_DISTRACTING("9", "Baby distracting", freq(114, 0.086), 0.086, dur(23.49), dur(28.39)),

    /** Child distracting. */
    CHILD_DISTRACTING("10", "Child distracting", freq(81, 0.143), 0.143, dur(25.76), dur(124.72)),

    /** Adult distracting. */
    ADULT_DISTRACTING("11", "Adult distracting", freq(48, 0.257), 0.257, dur(46.32), dur(108.49)),

    /** Conversing. */
    CONVERSING("12", "Conversing", freq(1558, 0.8), 0.8, dur(74.04), dur(234.5)),

    /** Reaching. */
    REACHING("13", "Reaching", freq(2246, 1.0), 1.0, dur(7.58), dur(36.7)),

    /** Manipulating vehicle controls. */
    MANIPULATING_VEHICLE_CONTROLS("14", "Manipulating vehicle controls", freq(2095, 1.0), 1.0, dur(4.82), dur(11.53)),

    /** Internal distraction. */
    INTERNAL_DISTRACTION("15", "Internal distraction", freq(481, 0.814), 0.814, dur(21.55), dur(46.38)),

    /** External distraction. */
    EXTERNAL_DISTRACTION("16", "External distraction", freq(659, 0.9), 0.9, dur(26.55), dur(58.78)),

    /** Preparing to eat / drink. */
    PREPARING_EAT_DRINK("17", "Preparing to eat / drink", freq(1503, 0.614), 0.614, dur(15.4), dur(34.7));

    /** Total time of data with which frequencies are determined. */
    private static final double BASELINE_DURATION_SECONDS = 207.14 * 3600.0;

    /** Id. */
    private final String id;

    /** Description. */
    private final String description;

    /** Frequency. */
    private final Frequency frequency;

    /** Exposure (value in range [0...1]). */
    private final double exposure;

    /** Average duration. */
    private final Duration averageDuration;

    /** Standard deviation of duration. */
    private final Duration stdDuration;

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     * @param frequency Frequency; frequency per exposed driver
     * @param exposure double; exposure (value in range [0...1])
     * @param averageDuration Duration; average duration
     * @param stdDuration Duration; standard deviation of duration
     */
    DefaultDistraction(final String id, final String description, final Frequency frequency, final double exposure,
            final Duration averageDuration, final Duration stdDuration)
    {
        this.id = id;
        this.description = description;
        this.frequency = frequency;
        this.exposure = exposure;
        this.averageDuration = averageDuration;
        this.stdDuration = stdDuration;
    }

    /**
     * Helper method to return a {@code Frequency} with little code.
     * @param occurrences int; number of occurrences in data
     * @param exposure double; exposure
     * @return Frequency; frequency
     */
    private static Frequency freq(final int occurrences, final double exposure)
    {
        return Frequency.instantiateSI(occurrences / (BASELINE_DURATION_SECONDS * exposure));
    }

    /**
     * Helper method to return a {@code Duration} with little code.
     * @param duration double; SI value of duration
     * @return Duration; duration
     */
    private static Duration dur(final double duration)
    {
        return Duration.instantiateSI(duration);
    }

    /**
     * Returns the id.
     * @return String; id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Returns the description.
     * @return String; description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the frequency per exposed driver.
     * @return Frequency; frequency per exposed driver
     */
    public Frequency getFrequency()
    {
        return this.frequency;
    }

    /**
     * Returns the exposure.
     * @return double; exposure
     */
    public double getExposure()
    {
        return this.exposure;
    }

    /**
     * Returns the average duration.
     * @return Duration; average duration
     */
    public Duration getAverageDuration()
    {
        return this.averageDuration;
    }

    /**
     * Returns the standard deviation of duration.
     * @return Duration; standard deviation of duration
     */
    public Duration getStdDuration()
    {
        return this.stdDuration;
    }

    /**
     * Returns a default distraction from the id.
     * @param id String; id
     * @return DefaultDistraction; default distraction from id
     */
    public static DefaultDistraction getFromId(final String id)
    {
        return values()[Integer.parseInt(id) - 1];
    }

}
