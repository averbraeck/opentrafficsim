package org.opentrafficsim.road.network.speed;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
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
    private final SpeedInfo currentSpeedInfo;

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
    public SpeedLimitProspect(final SpeedInfo currentSpeedInfo)
    {
        this.currentSpeedInfo = new SpeedInfo(currentSpeedInfo);
    }

    /**
     * Sets the speed info of a speed limit type.
     * @param location Location to set info for a speed limit type.
     * @param speedLimitType Speed limit type to set the info for.
     * @param speedInfo Speed info to set.
     * @param <T> class of speed info
     */
    public final <T> void setSpeedLimitTypeInfo(final Length location, final SpeedLimitType<T> speedLimitType,
        final T speedInfo)
    {
        this.prospect.add(new SpeedLimitEntry<T>(location, speedLimitType, speedInfo));
    }

    /**
     * Clears the speed info of a speed limit type.
     * @param location Location to clear a speed limit type.
     * @param speedLimitType Speed limit type to clear.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void clearSpeedLimitTypeInfo(final Length location, final SpeedLimitType<?> speedLimitType)
    {
        // We are putting a 'Clear' in an entry with speedLimitType<T> where T is not Clear, but info of class 'Clear' is
        // separately checked to clear a speedLimitType. I.e., no info is then returned that needs to be of T.
        this.prospect.add(new SpeedLimitEntry(location, speedLimitType, CLEAR));
    }

    /**
     * Sets legal speed limit types as enforced.
     * @param location Location to enforce legal speed limit types.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void setEnforced(final Length location)
    {
        // We are putting an 'Enforce' in an entry with speedLimitType<T> where T is not Enforce, but info of class 'Enforce' is
        // separately checked to set speed limits enforced. I.e., no info is then returned that needs to be of T.
        // dummy speed limit type, may not be null
        this.prospect.add(new SpeedLimitEntry(location, null, ENFORCE));
    }

    /**
     * Sets legal speed limit types as enforced.
     * @param location Location to enforce legal speed limit types.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final void clearEnforced(final Length location)
    {
        // We are putting a 'ClearEnforce' in an entry with speedLimitType<T> where T is not ClearEnforce, but info of class
        // 'ClearEnforce' is separately checked to set speed limits no longer enforced. I.e., no info is then returned that
        // needs to be of T.
        // dummy speed limit type, may not be null
        this.prospect.add(new SpeedLimitEntry(location, null, CLEAR_ENFORCE));
    }

    /**
     * Returns the speed info at a location following an acceleration over some duration.
     * @param speed current speed
     * @param acceleration acceleration to apply
     * @param time duration of acceleration
     * @return speed info at a given location
     */
    public final SpeedInfo getSpeedInfoAtLocation(final Speed speed, final Acceleration acceleration, final Duration time)
    {
        return getSpeedInfoAtLocation(new Length(speed.si * time.si + .5 * acceleration.si * time.si * time.si,
            LengthUnit.SI));
    }

    /**
     * Returns the speed info at a given location.
     * @param location where to get the speed info.
     * @return speed info at a given location
     * @param <T> variable underlying speed info class
     */
    public final <T> SpeedInfo getSpeedInfoAtLocation(final Length location)
    {
        SpeedInfo speedInfo = new SpeedInfo(this.currentSpeedInfo);
        for (SpeedLimitEntry<?> speedLimitEntry : this.prospect)
        {
            if (speedLimitEntry.getLocation().gt(location))
            {
                // remaining entries are further ahead
                return speedInfo;
            }
            if (speedLimitEntry.getSpeedInfo() instanceof Clear)
            {
                speedInfo.clearSpeedInfo(speedLimitEntry.getSpeedLimitType());
            }
            else if (speedLimitEntry.getSpeedInfo() instanceof Enforce)
            {
                speedInfo.setEnforced();
            }
            else if (speedLimitEntry.getSpeedInfo() instanceof ClearEnforce)
            {
                speedInfo.clearEnforced();
            }
            else
            {
                // Method setSpeedLimitTypeInfo() guarantees matching signature. Method clearSpeedLimitTypeInfo() does not, but
                // the result of this method is captured with "if instanceof Clear".
                @SuppressWarnings("unchecked")
                SpeedLimitType<T> speedLimitType = (SpeedLimitType<T>) speedLimitEntry.getSpeedLimitType();
                @SuppressWarnings("unchecked")
                T speedInfoOfType = (T) speedLimitEntry.getSpeedInfo();
                speedInfo.setSpeedInfo(speedLimitType, speedInfoOfType);
            }
        }
        return speedInfo;
    }

    /**
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
