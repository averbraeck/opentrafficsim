package org.opentrafficsim.road.network.speed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;

/**
 * Prospect of speed limits ahead, both legal and otherwise (e.g. curve, speed bump).
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedLimitProspect implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Spatial prospect of speed info. */
    private final SortedSet<SpeedLimitEntry<?>> prospect = new TreeSet<>();

    /**
     * Sets the speed info of a speed limit type.
     * @param distance location to set info for a speed limit type
     * @param speedLimitType speed limit type to set the info for
     * @param speedInfo speed info to set
     * @param <T> class of speed info
     * @throws IllegalStateException if speed info for a specific speed limit type is set or removed twice at the same distance
     * @throws IllegalStateException if speed info for a specific speed limit type is set twice with negative distance
     * @throws NullPointerException if any input is null
     */
    public final <T> void addSpeedInfo(final Length distance, final SpeedLimitType<T> speedLimitType, final T speedInfo)
    {
        Throw.whenNull(distance, "Distance may not be null.");
        Throw.whenNull(speedLimitType, "Speed limit type may not be null.");
        Throw.whenNull(speedInfo, "Speed info may not be null.");
        checkAndAdd(new SpeedLimitEntry<T>(distance, speedLimitType, speedInfo));
    }

    /**
     * Removes the speed info of a speed limit type.
     * @param distance distance to remove speed info of a speed limit type
     * @param speedLimitType speed limit type to remove speed info of
     * @throws IllegalStateException if speed info for a specific speed limit type is set or removed twice at the same distance
     * @throws IllegalArgumentException if the speed limit type is {@code MAX_VEHICLE_SPEED}
     * @throws IllegalArgumentException if the distance is negative
     * @throws NullPointerException if any input is null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void removeSpeedInfo(final Length distance, final SpeedLimitType<?> speedLimitType)
    {
        Throw.whenNull(distance, "Distance may not be null.");
        Throw.when(distance.si < 0, IllegalArgumentException.class, "Removing speed info in the past is not allowed. "
            + "Only add still active speed info.");
        Throw.whenNull(speedLimitType, "Speed limit type may not be null.");
        Throw.when(speedLimitType.equals(SpeedLimitTypes.MAX_VEHICLE_SPEED), IllegalArgumentException.class,
            "May not remove the maximum vehicle speed.");
        // null value does not comply to being a T for SpeedLimitType<T> but is separately treated
        checkAndAdd(new SpeedLimitEntry(distance, speedLimitType, null));
    }

    /**
     * Checks the speed limit entry before adding to the prospect.
     * @param speedLimitEntry speed limit entry to add
     * @throws IllegalStateException if the speed entry forms an undefined set with any existing entry
     * @throws IllegalStateException if speed info for a specific speed limit type is set twice with negative distance
     */
    private void checkAndAdd(final SpeedLimitEntry<?> speedLimitEntry)
    {
        for (SpeedLimitEntry<?> s : this.prospect)
        {
            if (s.getSpeedLimitType().equals(speedLimitEntry.getSpeedLimitType()))
            {
                /*
                 * For entries with negative distances, the speed limit type may not be the same, as only the latest one is
                 * still valid at the current position.
                 */
                Throw.when(s.getDistance().si < 0 && speedLimitEntry.getDistance().si < 0, IllegalStateException.class,
                    "Info of speed limit type '%s' is set twice with negative distances (%s, %s). This is undefined.", s
                        .getSpeedLimitType().getId(), s.getDistance().toString(), speedLimitEntry.getDistance().toString());
                /*
                 * For entries at the same distance, the speed limit type may not be the same, this leaves us with an undefined
                 * state as it cannot be derived which remains valid further on.
                 */
                Throw.when(s.getDistance().equals(speedLimitEntry.getDistance()), IllegalStateException.class, "Info "
                    + "of speed limit type '%s' is set twice at the same location (%s). This is undefined. "
                    + "Either remove speed info, or overwrite with new speed info.", s.getSpeedLimitType(), s.getDistance());
            }
        }
        this.prospect.add(speedLimitEntry);
    }

    /**
     * Returns the distances at which a change in the prospect is present in order (closest first). If multiple changes are
     * present at the same distance, only one distance is returned in the list.
     * @return distances at which a change in the prospect is present in order (closest first)
     */
    public final List<Length> getDistances()
    {
        List<Length> list = new ArrayList<>();
        for (SpeedLimitEntry<?> speedLimitEntry : this.prospect)
        {
            list.add(speedLimitEntry.getDistance());
        }
        return list;
    }

    /**
     * Returns whether the give speed limit type is changed at the given distance.
     * @param distance distance to check
     * @param speedLimitType speed limit type to check
     * @return whether the given speed limit type is changed at the given distance
     * @throws NullPointerException if distance is null
     */
    public final boolean speedInfoChanged(final Length distance, final SpeedLimitType<?> speedLimitType)
    {
        Throw.whenNull(distance, "Distance may not be null.");
        for (SpeedLimitEntry<?> sle : this.prospect)
        {
            if (sle.getDistance().eq(distance) && sle.getSpeedLimitType().equals(speedLimitType))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the speed info of given speed limit type where it changed. If the change was removing the speed limit type (e.g.
     * end of corner), then {@code null} is returned.
     * @param distance distance where the info changed
     * @param speedLimitType speed limit type
     * @return speed info of given speed limit type where it changed, {@code null} if speed limit type is removed
     * @throws IllegalArgumentException if the speed info did not change at the given distance for the speed limit type
     * @param <T> class of the speed limit type info
     */
    public final <T> T getSpeedInfoChange(final Length distance, final SpeedLimitType<T> speedLimitType)
    {
        for (SpeedLimitEntry<?> sle : this.prospect)
        {
            if (sle.getDistance().eq(distance) && sle.getSpeedLimitType().equals(speedLimitType))
            {
                @SuppressWarnings("unchecked")
                T info = (T) sle.getSpeedInfo();
                return info;
            }
        }
        throw new IllegalArgumentException("Speed info of speed limit type '" + speedLimitType.getId()
            + "' is requested at a distance '" + distance + "' where the info is not changed.");
    }

    /**
     * Returns the speed info at a given location.
     * @param distance where to get the speed info
     * @return speed info at a given distance
     * @throws NullPointerException if distance is null
     */
    public final SpeedLimitInfo getSpeedLimitInfo(final Length distance)
    {
        Throw.whenNull(distance, "Distance may not be null.");
        SpeedLimitInfo speedLimitInfo = new SpeedLimitInfo();
        for (SpeedLimitEntry<?> speedLimitEntry : this.prospect)
        {
            // use compareTo as this also determines order in this.prospect
            if (speedLimitEntry.getDistance().compareTo(distance) > 0)
            {
                // remaining entries are further ahead
                return speedLimitInfo;
            }
            // make appropriate change to speedLimitInfo
            if (speedLimitEntry.getSpeedInfo() == null)
            {
                speedLimitInfo.removeSpeedInfo(speedLimitEntry.getSpeedLimitType());
            }
            else
            {
                // method addSpeedInfo guarantees that speedInfo in speedLimitEntry is T
                // for speedLimitType in speedLimitEntry is SpeedLimitType<T>, null is check above
                setAsType(speedLimitInfo, speedLimitEntry);
            }
        }
        return speedLimitInfo;
    }

    /**
     * Returns the speed info at a location following an acceleration over some duration.
     * @param speed current speed
     * @param acceleration acceleration to apply
     * @param time duration of acceleration
     * @return speed info at a given distance
     * @throws NullPointerException if any input is null
     */
    public final SpeedLimitInfo getSpeedLimitInfo(final Speed speed, final Acceleration acceleration, final Duration time)
    {
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        return getSpeedLimitInfo(new Length(speed.si * time.si + .5 * acceleration.si * time.si * time.si, LengthUnit.SI));
    }

    /**
     * Sets speed info for a speed limit type in speed limit info by explicitly casting the types. From the context it should be
     * certain that the speed info inside the speed limit entry matches the declared info type of the speed limit type inside
     * the entry, i.e. {@code speedLimitEntry.getSpeedLimitType() = SpeedLimitType<T>} and
     * {@code speedLimitEntry.getSpeedInfo() = T}.
     * @param speedLimitInfo speed limit info to put speed info in
     * @param speedLimitEntry entry with speed limit type and speed info to set
     * @param <T> underlying speed info class depending on speed limit type
     */
    @SuppressWarnings("unchecked")
    private <T> void setAsType(final SpeedLimitInfo speedLimitInfo, final SpeedLimitEntry<?> speedLimitEntry)
    {
        SpeedLimitType<T> speedLimitType = (SpeedLimitType<T>) speedLimitEntry.getSpeedLimitType();
        T speedInfoOfType = (T) speedLimitEntry.getSpeedInfo();
        speedLimitInfo.addSpeedInfo(speedLimitType, speedInfoOfType);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        StringBuilder stringBuilder = new StringBuilder("SpeedLimitProspect [");
        String sep = "";
        for (SpeedLimitEntry<?> sle : this.prospect)
        {
            stringBuilder.append(sep).append(sle.getDistance()).append(": ");
            if (sle.getSpeedInfo() == null)
            {
                stringBuilder.append(sle.getSpeedLimitType().getId()).append("=END");
            }
            else
            {
                stringBuilder.append(sle.getSpeedLimitType().getId()).append("=");
                stringBuilder.append(sle.getSpeedInfo());
            }
            sep = ", ";
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Stores speed limit type and it's speed info with a location.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> class of speed info
     */
    private class SpeedLimitEntry<T> implements Comparable<SpeedLimitEntry<?>>, Serializable
    {

        /** */
        private static final long serialVersionUID = 20160501L;

        /** Location of the speed info. */
        private final Length distance;

        /** Speed limit type. */
        private final SpeedLimitType<T> speedLimitType;

        /** Speed info. */
        private final T speedInfo;

        /**
         * Constructor.
         * @param distance location of the speed info
         * @param speedLimitType speed limit type
         * @param speedInfo speed info
         */
        SpeedLimitEntry(final Length distance, final SpeedLimitType<T> speedLimitType, final T speedInfo)
        {
            this.distance = distance;
            this.speedLimitType = speedLimitType;
            this.speedInfo = speedInfo;
        }

        /**
         * Returns the location of the speed info.
         * @return location of the speed info
         */
        public final Length getDistance()
        {
            return this.distance;
        }

        /**
         * Returns the speed limit type.
         * @return speed limit type
         */
        public final SpeedLimitType<T> getSpeedLimitType()
        {
            return this.speedLimitType;
        }

        /**
         * Returns the speed info.
         * @return the speed info
         */
        public final T getSpeedInfo()
        {
            return this.speedInfo;
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.distance.hashCode();
            result = prime * result + this.speedInfo.hashCode();
            result = prime * result + this.speedLimitType.hashCode();
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public final boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            SpeedLimitEntry<?> other = (SpeedLimitEntry<?>) obj;
            if (!this.distance.equals(other.distance))
            {
                return false;
            }
            if (!this.speedLimitType.equals(other.speedLimitType))
            {
                return false;
            }
            if (this.speedInfo == null)
            {
                if (other.speedInfo != null)
                {
                    return false;
                }
            }
            else if (!this.speedInfo.equals(other.speedInfo))
            {
                return false;
            }
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final SpeedLimitEntry<?> speedLimitEntry)
        {
            if (this.equals(speedLimitEntry))
            {
                return 0;
            }
            // order by distance
            int comp = this.distance.compareTo(speedLimitEntry.distance);
            if (comp != 0)
            {
                return comp;
            }
            comp = this.speedLimitType.getId().compareTo(speedLimitEntry.speedLimitType.getId());
            if (comp != 0)
            {
                return comp;
            }
            if (this.speedInfo != null && speedLimitEntry.speedInfo == null)
            {
                return 1;
            }
            else if (this.speedInfo == null && speedLimitEntry.speedInfo != null)
            {
                return -1;
            }
            return this.speedInfo.hashCode() < speedLimitEntry.speedInfo.hashCode() ? -1 : 1;
        }

    }

}
