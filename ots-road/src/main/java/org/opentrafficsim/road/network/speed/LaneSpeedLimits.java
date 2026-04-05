package org.opentrafficsim.road.network.speed;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Speed limits for the context of a single lane.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneSpeedLimits
{

    /** Speed limits that are applicable to GTU types. */
    private final Map<GtuType, Speed> gtuTypeSpeedLimits;

    /** Speed limit information for time windows. */
    private NavigableMap<Duration, LocalSpeedLimit> speedLimits = new TreeMap<>();

    /**
     * Constructor with no speed limit.
     */
    public LaneSpeedLimits()
    {
        this.gtuTypeSpeedLimits = new LinkedHashMap<>();
    }

    /**
     * Constructor with only a fixed speed limit.
     * @param speedLimit overall speed limit
     */
    public LaneSpeedLimits(final Speed speedLimit)
    {
        this.gtuTypeSpeedLimits = new LinkedHashMap<>();
        addSpeedLimit(speedLimit);
    }

    /**
     * Constructor with only GTU type speed limits.
     * @param gtuTypeSpeedLimits GTU type speed limits
     */
    public LaneSpeedLimits(final Map<GtuType, Speed> gtuTypeSpeedLimits)
    {
        Throw.whenNull(gtuTypeSpeedLimits, "gtuTypeSpeedLimits");
        this.gtuTypeSpeedLimits = new LinkedHashMap<>(gtuTypeSpeedLimits);
    }

    /**
     * Constructor with overall speed limit and GTU type speed limits.
     * @param speedLimit overall speed limit
     * @param gtuTypeSpeedLimits GTU type speed limits
     */
    public LaneSpeedLimits(final Speed speedLimit, final Map<GtuType, Speed> gtuTypeSpeedLimits)
    {
        Throw.whenNull(gtuTypeSpeedLimits, "gtuTypeSpeedLimits");
        this.gtuTypeSpeedLimits = new LinkedHashMap<>(gtuTypeSpeedLimits);
        addSpeedLimit(speedLimit);
    }

    /**
     * Add non-enforced constant speed limit.
     * @param speed speed limit
     * @return this object for method chaining
     */
    public LaneSpeedLimits addSpeedLimit(final Speed speed)
    {
        Throw.whenNull(speed, "speed");
        return addSpeedLimit(Duration.ZERO, speed, false, false);
    }

    /**
     * Add constant speed limit.
     * <p>
     * If enforcement is aware of the GTU type, any speed limit applicable to the GTU type (e.g. 80km/h for trucks) becomes an
     * enforced speed limit.
     * @param speed speed limit
     * @param enforced whether the speed limit is enforced
     * @param enforcedGtuTypeAware whether the speed limit enforcement is aware of GTU types
     * @return this object for method chaining
     */
    public LaneSpeedLimits addSpeedLimit(final Speed speed, final boolean enforced, final boolean enforcedGtuTypeAware)
    {
        Throw.whenNull(speed, "speed");
        return addSpeedLimit(Duration.ZERO, speed, enforced, enforcedGtuTypeAware);
    }

    /**
     * Add non-enforced speed limit. Time of day will by cycled through. For example, if a speed limit of 100km/h is added at 6h
     * and a speed limit of 130km/h is added at 19h, 100km/h applies from 06:00 till 19:00, while 130km/h applies for all other
     * hours, including between 00:00 and 06:00.
     * @param timeOfDay time-of-day when the speed limit becomes active
     * @param speed speed of the speed limit
     * @return this object for method chaining
     * @throws IllegalArgumentException when time-of-day is not in the range [0 24) hours
     */
    public LaneSpeedLimits addSpeedLimit(final Duration timeOfDay, final Speed speed)
    {
        Throw.whenNull(timeOfDay, "timeOfDay");
        Throw.whenNull(speed, "speed");
        return addSpeedLimit(timeOfDay, speed, false, false);
    }

    /**
     * Add speed limit. Time of day will by cycled through. For example, if a speed limit of 100km/h is added at 6h and a speed
     * limit of 130km/h is added at 19h, 100km/h applies from 06:00 till 19:00, while 130km/h applies for all other hours,
     * including between 00:00 and 06:00.
     * <p>
     * If enforcement is aware of the GTU type, any speed limit applicable to the GTU type (e.g. 80km/h for trucks) becomes an
     * enforced speed limit.
     * @param timeOfDay time-of-day when the speed limit becomes active
     * @param speed speed of the speed limit
     * @param enforced whether the speed limit is enforced
     * @param enforcedGtuTypeAware whether the speed limit enforcement is aware of GTU types
     * @return this object for method chaining
     * @throws IllegalArgumentException when time-of-day is not in the range [0 24) hours
     */
    public LaneSpeedLimits addSpeedLimit(final Duration timeOfDay, final Speed speed, final boolean enforced,
            final boolean enforcedGtuTypeAware)
    {
        Throw.whenNull(timeOfDay, "timeOfDay");
        Throw.whenNull(speed, "speed");
        Throw.when(timeOfDay.si < 0.0 || timeOfDay.si >= 86400.0, IllegalArgumentException.class,
                "Time of day value must be between 0 (inclusive) and 24 (exclusive) hours.");
        this.speedLimits.put(timeOfDay, new LocalSpeedLimit(new SpeedLimit(speed, enforced), enforcedGtuTypeAware));
        return this;
    }

    /**
     * Returns the speed limit.
     * @param timeOfDay time-of-day
     * @return speed limit, empty if no speed limit given
     */
    public Optional<SpeedLimit> getSpeedLimit(final Duration timeOfDay)
    {
        LocalSpeedLimit localSpeedLimit = getLocalSpeedLimit(timeOfDay);
        return Optional.ofNullable(localSpeedLimit == null ? null : localSpeedLimit.speedLimit());
    }

    /**
     * Returns the applicable speed limits for the given GTU type, position and time-of-day.
     * @param gtuType GTU type
     * @param timeOfDay time-of-day
     * @return applicable speed limits for the given GTU type, position and time-of-day
     */
    public SpeedLimits getSpeedLimits(final GtuType gtuType, final Duration timeOfDay)
    {
        LocalSpeedLimit localSpeedLimit = getLocalSpeedLimit(timeOfDay);
        Speed gtuTypeSpeed = getGtuTypeSpeedLimit(gtuType);
        if (gtuTypeSpeed != null)
        {
            return new SpeedLimits(localSpeedLimit == null ? null : localSpeedLimit.speedLimit(),
                    new SpeedLimit(gtuTypeSpeed, localSpeedLimit != null && localSpeedLimit.enforcedGtuTypeAware()
                            && localSpeedLimit.speedLimit().enforced()));
        }
        return new SpeedLimits(localSpeedLimit == null ? null : localSpeedLimit.speedLimit(), null);
    }

    /**
     * Returns the local speed limit at the given position and time-of-day.
     * @param timeOfDay time-of-day
     * @return local speed limit at the given position and time-of-day
     */
    private LocalSpeedLimit getLocalSpeedLimit(final Duration timeOfDay)
    {
        Entry<Duration, LocalSpeedLimit> todEntry = this.speedLimits.floorEntry(timeOfDay);
        // if there is information but not before the requested time-of-day, return the last information (cycles through day)
        if (todEntry == null)
        {
            if (!this.speedLimits.isEmpty())
            {
                return this.speedLimits.lastEntry().getValue();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return todEntry.getValue();
        }
    }

    /**
     * Returns the speed limit for the GTU type, or any of its parents.
     * @param gtuType GTU type
     * @return speed limit for the GTU type, or any of its parents
     */
    private Speed getGtuTypeSpeedLimit(final GtuType gtuType)
    {
        Speed speed = this.gtuTypeSpeedLimits.get(gtuType);
        if (speed != null)
        {
            return speed;
        }
        Optional<GtuType> parent = gtuType.getParent();
        if (!parent.isPresent())
        {
            this.gtuTypeSpeedLimits.put(gtuType, null);
            return null;
        }
        return getGtuTypeSpeedLimit(parent.get());
    }

    /**
     * Record of speed limit information.
     * @param speedLimit speed limit
     * @param enforcedGtuTypeAware whether the enforcement is GTU type aware
     */
    private record LocalSpeedLimit(SpeedLimit speedLimit, boolean enforcedGtuTypeAware)
    {
    }

}
