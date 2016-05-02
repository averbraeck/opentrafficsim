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

/**
 * Prospect of speed limits ahead, both legal and otherwise (e.g. curve, speed bump).
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedLimitProspect implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Current speed info (at x=0). */
    private final SpeedLimitInfo currentSpeedInfo;

    /** Spatial prospect of speed limit info. */
    private final SortedSet<SpeedLimitEntry<?>> prospect = new TreeSet<SpeedLimitEntry<?>>();

    /** Info object for in the prospect that means a speed limit type needs to be cleared. */
    private static final Clear CLEAR = new Clear();

    /** Info object for in the prospect that means legal speed limit types are enforced. */
    private static final Enforce ENFORCE = new Enforce();

    /** Info object for in the prospect that means legal speed limit types are no longer enforced. */
    private static final ClearEnforce CLEAR_ENFORCE = new ClearEnforce();

    /**
     * Constructor.
     * @param currentSpeedInfo current speed info (at x=0)
     */
    public SpeedLimitProspect(final SpeedLimitInfo currentSpeedInfo)
    {
        this.currentSpeedInfo = new SpeedLimitInfo(currentSpeedInfo);
    }

    /**
     * Sets the speed info of a speed limit type.
     * @param location location to set info for a speed limit type
     * @param speedLimitType speed limit type to set the info for
     * @param speedInfo speed info to set
     * @param <T> class of speed info
     */
    public final <T> void setSpeedInfo(final Length location, final SpeedLimitType<T> speedLimitType, final T speedInfo)
    {
        this.prospect.add(new SpeedLimitEntry<T>(location, speedLimitType, speedInfo));
    }

    /**
     * Clears the speed info of a speed limit type.
     * @param location location to clear a speed limit type
     * @param speedLimitType speed limit type to clear
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void clearSpeedInfo(final Length location, final SpeedLimitType<?> speedLimitType)
    {
        // We are putting a 'Clear' in an entry with speedLimitType<T> where T is not Clear, but info of class 'Clear' is
        // separately checked to clear a speedLimitType. I.e., no info is then returned that needs to be of T.
        this.prospect.add(new SpeedLimitEntry(location, speedLimitType, CLEAR));
    }

    /**
     * Sets legal speed limit types as enforced.
     * @param location Location to enforce legal speed limit types
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void setEnforced(final Length location)
    {
        // We are putting an 'Enforce' in an entry with null for speedLimitType<T> so there is no check on T, but info of class
        // 'Enforce' is separately checked to set speed limits enforced. I.e., no info is then returned that needs to be of T.
        this.prospect.add(new SpeedLimitEntry(location, null, ENFORCE));
    }

    /**
     * Sets legal speed limit types as enforced.
     * @param location Location to enforce legal speed limit types.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void clearEnforced(final Length location)
    {
        // We are putting a 'ClearEnforce' in an entry with null for speedLimitType<T> so there is no check on T, but info of
        // class 'ClearEnforce' is separately checked to set speed limits no longer enforced. I.e., no info is then returned
        // that needs to be of T.
        this.prospect.add(new SpeedLimitEntry(location, null, CLEAR_ENFORCE));
    }

    /**
     * Returns the locations at which a change in the prospect is present in order (closest first).
     * @return locations at which a change in the prospect is present in order (closest first)
     */
    public final List<Length> getLocations()
    {
        List<Length> list = new ArrayList<>();
        for (SpeedLimitEntry<?> speedLimitEntry : this.prospect)
        {
            list.add(speedLimitEntry.getLocation());
        }
        return list;
    }

    /**
     * Returns the speed info at a location following an acceleration over some duration.
     * @param speed current speed
     * @param acceleration acceleration to apply
     * @param time duration of acceleration
     * @return speed info at a given location
     */
    public final SpeedLimitInfo getSpeedLimitInfo(final Speed speed, final Acceleration acceleration, final Duration time)
    {
        return getSpeedLimitInfo(new Length(speed.si * time.si + .5 * acceleration.si * time.si * time.si, LengthUnit.SI));
    }

    /**
     * Returns the speed info at a given location.
     * @param location where to get the speed info
     * @return speed info at a given location
     * @param <T> underlying speed info class depending on speed limit type
     */
    public final <T> SpeedLimitInfo getSpeedLimitInfo(final Length location)
    {
        SpeedLimitInfo speedLimitInfo = new SpeedLimitInfo(this.currentSpeedInfo);
        for (SpeedLimitEntry<?> speedLimitEntry : this.prospect)
        {
            // use compareTo as this also determines order in this.prospect
            if (speedLimitEntry.getLocation().compareTo(location) > 0)
            {
                // remaining entries are further ahead
                return speedLimitInfo;
            }
            // make appropriate change to speedLimitInfo
            if (speedLimitEntry.getSpeedInfo() instanceof Clear)
            {
                speedLimitInfo.clearSpeedInfo(speedLimitEntry.getSpeedLimitType());
            }
            else if (speedLimitEntry.getSpeedInfo() instanceof Enforce)
            {
                speedLimitInfo.setEnforced();
            }
            else if (speedLimitEntry.getSpeedInfo() instanceof ClearEnforce)
            {
                speedLimitInfo.clearEnforced();
            }
            else
            {
                // Method setSpeedInfo() guarantees matching signature. Methods clearSpeedInfo(), setEnforced() and
                // clearEnforced() do not, but the result of these methods is captured with the above instanceof's.
                @SuppressWarnings("unchecked")
                SpeedLimitType<T> speedLimitType = (SpeedLimitType<T>) speedLimitEntry.getSpeedLimitType();
                @SuppressWarnings("unchecked")
                T speedInfoOfType = (T) speedLimitEntry.getSpeedInfo();
                speedLimitInfo.setSpeedInfo(speedLimitType, speedInfoOfType);
            }
        }
        return speedLimitInfo;
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
        private final Length location;

        /** Speed limit type. */
        private final SpeedLimitType<T> speedLimitType;

        /** Speed info. */
        private final T speedInfo;

        /**
         * Constructor.
         * @param location location of the speed info
         * @param speedLimitType speed limit type
         * @param speedInfo speed info
         */
        SpeedLimitEntry(final Length location, final SpeedLimitType<T> speedLimitType, final T speedInfo)
        {
            this.location = location;
            this.speedLimitType = speedLimitType;
            this.speedInfo = speedInfo;
        }

        /**
         * Returns the location of the speed info.
         * @return location of the speed info
         */
        public Length getLocation()
        {
            return this.location;
        }

        /**
         * Returns the speed limit type.
         * @return speed limit type
         */
        public SpeedLimitType<T> getSpeedLimitType()
        {
            return this.speedLimitType;
        }

        /**
         * Returns the speed info.
         * @return the speed info
         */
        public T getSpeedInfo()
        {
            return this.speedInfo;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final SpeedLimitEntry<?> speedLimitEntry)
        {
            return this.location.compareTo(speedLimitEntry.location);
        }

    }

    /**
     * Info class that indicates that info regarding a speed limit type has to be cleared.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class Clear
    {
        /** Constructor. */
        Clear()
        {
            //
        }
    }

    /**
     * Info class that indicates that legal speed limits are enforced.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class Enforce
    {
        /** Constructor. */
        Enforce()
        {
            //
        }
    }

    /**
     * Info class that indicates that legal speed limits are no longer enforced.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class ClearEnforce
    {
        /** Constructor. */
        ClearEnforce()
        {
            //
        }
    }

}
