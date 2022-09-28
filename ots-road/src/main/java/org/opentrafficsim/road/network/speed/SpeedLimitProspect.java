package org.opentrafficsim.road.network.speed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;

/**
 * Prospect of speed limits ahead, both legal and otherwise (e.g. curve, speed bump).
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SpeedLimitProspect implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Spatial prospect of speed info. */
    private final SortedSet<SpeedLimitEntry<?>> prospect = new TreeSet<>();

    /** Source objects for the speed info additions. */
    private final Map<Object, SpeedLimitEntry<?>> addSources = new LinkedHashMap<>();

    /** Source objects for the speed info removals. */
    private final Map<Object, SpeedLimitEntry<?>> removeSources = new LinkedHashMap<>();

    /** Last odometer value. */
    private Length odometer;

    /**
     * Constructor.
     * @param odometer Length; odometer value
     */
    public SpeedLimitProspect(final Length odometer)
    {
        this.odometer = odometer;
    }

    /**
     * Updates the distance values.
     * @param newOdometer Length; odometer value
     */
    public void update(final Length newOdometer)
    {
        Length dx = newOdometer.minus(this.odometer);
        for (SpeedLimitEntry<?> entry : this.prospect)
        {
            entry.move(dx);
        }
    }

    /**
     * Returns whether the given source is already added in the prospect.
     * @param source Object; source
     * @return whether the given source is already added in the prospect
     */
    public final boolean containsAddSource(final Object source)
    {
        return this.addSources.containsKey(source);
    }

    /**
     * Returns whether the given source is already removed in the prospect.
     * @param source Object; source
     * @return whether the given source is already removed in the prospect
     */
    public final boolean containsRemoveSource(final Object source)
    {
        return this.removeSources.containsKey(source);
    }

    /**
     * Returns the odometer value at which the last update was performed.
     * @return Length; odometer value at which the last update was performed
     */
    public final Length getOdometer()
    {
        return this.odometer;
    }

    /**
     * Sets the speed info of a speed limit type.
     * @param distance Length; location to set info for a speed limit type
     * @param speedLimitType SpeedLimitType&lt;T&gt;; speed limit type to set the info for
     * @param speedInfo T; speed info to set
     * @param source Object; source object
     * @param <T> class of speed info
     * @throws IllegalStateException if speed info for a specific speed limit type is set or removed twice at the same distance
     * @throws IllegalStateException if speed info for a specific speed limit type is set twice with negative distance
     * @throws NullPointerException if any input is null
     */
    public final <T> void addSpeedInfo(final Length distance, final SpeedLimitType<T> speedLimitType, final T speedInfo,
            final Object source)
    {
        Throw.whenNull(distance, "Distance may not be null.");
        Throw.whenNull(speedLimitType, "Speed limit type may not be null.");
        Throw.whenNull(speedInfo, "Speed info may not be null.");
        checkAndAdd(new SpeedLimitEntry<>(distance, speedLimitType, speedInfo), source, false);
    }

    /**
     * Removes the speed info of a speed limit type.
     * @param distance Length; distance to remove speed info of a speed limit type
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type to remove speed info of
     * @param source Object; source object
     * @throws IllegalStateException if speed info for a specific speed limit type is set or removed twice at the same distance
     * @throws IllegalArgumentException if the speed limit type is {@code MAX_VEHICLE_SPEED}
     * @throws IllegalArgumentException if the distance is negative
     * @throws NullPointerException if any input is null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void removeSpeedInfo(final Length distance, final SpeedLimitType<?> speedLimitType, final Object source)
    {
        Throw.whenNull(distance, "Distance may not be null.");
        Throw.when(distance.si < 0, IllegalArgumentException.class,
                "Removing speed info in the past is not allowed. " + "Only add still active speed info.");
        Throw.whenNull(speedLimitType, "Speed limit type may not be null.");
        Throw.when(speedLimitType.equals(SpeedLimitTypes.MAX_VEHICLE_SPEED), IllegalArgumentException.class,
                "May not remove the maximum vehicle speed.");
        // null value does not comply to being a T for SpeedLimitType<T> but is separately treated
        checkAndAdd(new SpeedLimitEntry(distance, speedLimitType, null), source, true);
    }

    /**
     * Checks the speed limit entry before adding to the prospect.
     * @param speedLimitEntry SpeedLimitEntry&lt;?&gt;; speed limit entry to add
     * @param source Object; source object
     * @param remove boolean; whether the source causes a removal of info
     * @throws IllegalStateException if the speed entry forms an undefined set with any existing entry
     */
    private void checkAndAdd(final SpeedLimitEntry<?> speedLimitEntry, final Object source, final boolean remove)
    {
        for (SpeedLimitEntry<?> s : this.prospect)
        {
            if (s.getSpeedLimitType().equals(speedLimitEntry.getSpeedLimitType()))
            {
                /*
                 * For entries at the same distance, the speed limit type may not be the same, this leaves us with an undefined
                 * state as it cannot be derived which remains valid further on.
                 */
                Throw.when(s.getDistance().equals(speedLimitEntry.getDistance()), IllegalStateException.class,
                        "Info " + "of speed limit type '%s' is set twice at the same location (%s). This is undefined. "
                                + "Either remove speed info, or overwrite with new speed info.",
                        s.getSpeedLimitType(), s.getDistance());
            }
        }
        if (remove)
        {
            SpeedLimitEntry<?> prev = this.removeSources.get(source);
            if (prev != null)
            {
                this.prospect.remove(prev);
            }
            this.removeSources.put(source, speedLimitEntry);
        }
        else
        {
            SpeedLimitEntry<?> prev = this.addSources.get(source);
            if (prev != null)
            {
                this.prospect.remove(prev);
            }
            this.addSources.put(source, speedLimitEntry);
        }
        this.prospect.add(speedLimitEntry);
    }

    /**
     * Returns the distances at which a change in the prospect is present in order (upstream first). If multiple changes are
     * present at the same distance, only one distance is returned in the list.
     * @return distances at which a change in the prospect is present in order (upstream first)
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
     * Returns the distances at which a change of the given speed limit type in the prospect is present in order (most upstream
     * first). If multiple changes are present at the same distance, only one distance is returned in the list.
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type to get the distances of
     * @return distances at which a change of the given speed limit type in the prospect is present in order
     */
    public final List<Length> getDistances(final SpeedLimitType<?> speedLimitType)
    {
        return getDistancesInRange(speedLimitType, null, null);
    }

    /**
     * Returns the upstream distances at which a change of the given speed limit type in the prospect is present in order (most
     * upstream first). If multiple changes are present at the same distance, only one distance is returned in the list.
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type to get the distances of
     * @return distances at which a change of the given speed limit type in the prospect is present in order
     */
    public final List<Length> getUpstreamDistances(final SpeedLimitType<?> speedLimitType)
    {
        return getDistancesInRange(speedLimitType, null, Length.ZERO);
    }

    /**
     * Returns the downstream distances at which a change of the given speed limit type in the prospect is present in order
     * (most upstream first). If multiple changes are present at the same distance, only one distance is returned in the list.
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type to get the distances of
     * @return distances at which a change of the given speed limit type in the prospect is present in order
     */
    public final List<Length> getDownstreamDistances(final SpeedLimitType<?> speedLimitType)
    {
        return getDistancesInRange(speedLimitType, Length.ZERO, null);
    }

    /**
     * Returns the distances between limits at which a change of the given speed limit type in the prospect is present in order
     * (most upstream first). If multiple changes are present at the same distance, only one distance is returned in the list.
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type to get the distances of
     * @param min Length; minimum distance, may be {@code null} for no minimum limit
     * @param max Length; maximum distance, may be {@code null} for no maximum limit
     * @return distances at which a change of the given speed limit type in the prospect is present in order
     */
    private List<Length> getDistancesInRange(final SpeedLimitType<?> speedLimitType, final Length min, final Length max)
    {
        List<Length> list = new ArrayList<>();
        for (SpeedLimitEntry<?> speedLimitEntry : this.prospect)
        {
            if (speedLimitEntry.getSpeedLimitType().equals(speedLimitType)
                    && (min == null || speedLimitEntry.getDistance().gt(min))
                    && (max == null || speedLimitEntry.getDistance().le(max)))
            {
                list.add(speedLimitEntry.getDistance());
            }
        }
        return list;
    }

    /**
     * Returns whether the given speed limit type is changed at the given distance.
     * @param distance Length; distance to check
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type to check
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
     * @param distance Length; distance where the info changed
     * @param speedLimitType SpeedLimitType&lt;T&gt;; speed limit type
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
     * @param distance Length; where to get the speed info
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
                // for speedLimitType in speedLimitEntry is SpeedLimitType<T>, null is checked above
                setAsType(speedLimitInfo, speedLimitEntry);
            }
        }
        return speedLimitInfo;
    }

    /**
     * Returns the speed info at a location following an acceleration over some duration.
     * @param speed Speed; current speed
     * @param acceleration Acceleration; acceleration to apply
     * @param time Duration; duration of acceleration
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
     * @param speedLimitInfo SpeedLimitInfo; speed limit info to put speed info in
     * @param speedLimitEntry SpeedLimitEntry&lt;?&gt;; entry with speed limit type and speed info to set
     * @param <T> underlying speed info class depending on speed limit type
     */
    @SuppressWarnings("unchecked")
    private <T> void setAsType(final SpeedLimitInfo speedLimitInfo, final SpeedLimitEntry<?> speedLimitEntry)
    {
        SpeedLimitType<T> speedLimitType = (SpeedLimitType<T>) speedLimitEntry.getSpeedLimitType();
        T speedInfoOfType = (T) speedLimitEntry.getSpeedInfo();
        speedLimitInfo.addSpeedInfo(speedLimitType, speedInfoOfType);
    }

    /**
     * Builds speed limit info with only MAX_VEHICLE_SPEED and the given speed limit type, where the speed info is obtained at
     * the given distance.
     * @param distance Length; distance to get the speed info at
     * @param speedLimitType SpeedLimitType&lt;T&gt;; speed limit type of which to include the info
     * @param <T> class of speed info of given speed limit type
     * @return speed limit info with only MAX_VEHICLE_SPEED and the given speed limit type
     */
    public final <T> SpeedLimitInfo buildSpeedLimitInfo(final Length distance, final SpeedLimitType<T> speedLimitType)
    {
        SpeedLimitInfo out = new SpeedLimitInfo();
        out.addSpeedInfo(speedLimitType, getSpeedInfoChange(distance, speedLimitType));
        for (SpeedLimitEntry<?> speedLimitEntry : this.prospect)
        {
            if (speedLimitEntry.getDistance().gt(distance))
            {
                break;
            }
            if (speedLimitEntry.getSpeedLimitType().equals(SpeedLimitTypes.MAX_VEHICLE_SPEED))
            {
                out.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED,
                        SpeedLimitTypes.MAX_VEHICLE_SPEED.getInfoClass().cast(speedLimitEntry.getSpeedInfo()));
            }
        }
        return out;
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> class of speed info
     */
    private static class SpeedLimitEntry<T> implements Comparable<SpeedLimitEntry<?>>, Serializable
    {

        /** */
        private static final long serialVersionUID = 20160501L;

        /** Location of the speed info. */
        private Length distance;

        /** Speed limit type. */
        private final SpeedLimitType<T> speedLimitType;

        /** Speed info. */
        private final T speedInfo;

        /**
         * Constructor.
         * @param distance Length; location of the speed info
         * @param speedLimitType SpeedLimitType&lt;T&gt;; speed limit type
         * @param speedInfo T; speed info
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

        /**
         * Move the record by a given distance.
         * @param dist Length; distance to move
         */
        public final void move(final Length dist)
        {
            this.distance = this.distance.minus(dist);
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
            // order by speed limit type
            comp = this.speedLimitType.getId().compareTo(speedLimitEntry.speedLimitType.getId());
            if (comp != 0)
            {
                return comp;
            }
            // equal distance and speed limit type is not allowed, so below code is not used
            // if this requirement changes, compareTo should still work
            if (this.speedInfo == null)
            {
                if (speedLimitEntry.speedInfo == null)
                {
                    return 0; // both null
                }
                return -1; // null under non-null
            }
            else if (speedLimitEntry.speedInfo == null)
            {
                return 1; // non-null over null
            }
            return this.speedInfo.hashCode() < speedLimitEntry.speedInfo.hashCode() ? -1 : 1;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "SpeedLimitEntry [distance=" + this.distance + ", speedLimitType=" + this.speedLimitType + ", speedInfo="
                    + this.speedInfo + "]";
        }

    }

}
