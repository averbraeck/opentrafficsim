package org.opentrafficsim.road.network.speed;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.exceptions.Throw;

/**
 * Class to contain speed info related to various speed limit types. Instances can reflect the current speed limit situation,
 * some situation ahead, or some situation in the past.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedLimitInfo implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Set of current speed info's mapped to speed limit types. */
    private final Map<SpeedLimitType<?>, Object> speedInfoMap = new LinkedHashMap<>();

    /**
     * Adds or overwrites the speed info of the given speed limit type.
     * @param speedLimitType SpeedLimitType&lt;T&gt;; speed limit type to add info for
     * @param speedInfo T; info regarding the speed limit type
     * @param <T> class of speed info
     * @throws NullPointerException if the speed limit type or speed info is null
     */
    public final <T> void addSpeedInfo(final SpeedLimitType<T> speedLimitType, final T speedInfo)
    {
        Throw.whenNull(speedLimitType, "Speed limit type may not be null.");
        Throw.whenNull(speedInfo, "Speed info may not be null.");
        this.speedInfoMap.put(speedLimitType, speedInfo);
    }

    /**
     * Removes the speed info of given speed limit type.
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type of speed info to remove
     * @throws NullPointerException if the speed limit type is null
     */
    public final void removeSpeedInfo(final SpeedLimitType<?> speedLimitType)
    {
        Throw.whenNull(speedLimitType, "Speed limit type may not be null.");
        this.speedInfoMap.remove(speedLimitType);
    }

    /**
     * Whether speed info is present for the given speed limit type.
     * @param speedLimitType SpeedLimitType&lt;?&gt;; speed limit type
     * @return whether speed info is present for the given speed limit type
     */
    public final boolean containsType(final SpeedLimitType<?> speedLimitType)
    {
        return this.speedInfoMap.containsKey(speedLimitType);
    }

    /**
     * Returns the info regarding a specific speed limit type.
     * @param speedLimitType SpeedLimitType&lt;T&gt;; speed limit type to return info for
     * @param <T> class of speed limit type info
     * @return the speed limit type info
     * @throws NullPointerException if the speed limit type is null
     * @throws IllegalStateException if the speed limit type is not present
     */
    @SuppressWarnings("unchecked")
    public final <T> T getSpeedInfo(final SpeedLimitType<T> speedLimitType)
    {
        Throw.whenNull(speedLimitType, "Speed limit type may not be null.");
        Throw.when(!containsType(speedLimitType), IllegalStateException.class,
                "The speed limit type '%s' "
                        + "is not present in the speed limit info. Use SpeedLimitInfo.containsType() to check.",
                speedLimitType.getId());
        return (T) this.speedInfoMap.get(speedLimitType);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.speedInfoMap.hashCode();
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
        SpeedLimitInfo other = (SpeedLimitInfo) obj;
        if (!this.speedInfoMap.equals(other.speedInfoMap))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        StringBuilder stringBuilder = new StringBuilder("SpeedLimitInfo [");
        String sep = "";
        for (SpeedLimitType<?> slt : this.speedInfoMap.keySet())
        {
            stringBuilder.append(sep);
            stringBuilder.append(slt.getId());
            stringBuilder.append("=");
            stringBuilder.append(this.speedInfoMap.get(slt));
            sep = ", ";
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

}
