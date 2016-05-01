package org.opentrafficsim.road.network.speed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;

/**
 * Class to contain speed info related to various speed limit types. Instances can reflect the current speed limit situation, or
 * some situation ahead.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 21, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedInfo implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;
    
    /** Set of current speed limits mapped to speed limit types. */
    private final Map<SpeedLimitType<?>, Object> speedInfoMap = new HashMap<>();

    /** Whether the legal speed limits are enforced. */
    private boolean enforced;

    /** Empty constructor. */
    public SpeedInfo()
    {
        //
    }

    /**
     * Constructor.
     * @param speedInfo speed info to copy into new speed info.
     */
    public SpeedInfo(final SpeedInfo speedInfo)
    {
        for (SpeedLimitType<?> speedLimitType : speedInfo.speedInfoMap.keySet())
        {
            this.speedInfoMap.put(speedLimitType, speedInfo.speedInfoMap.get(speedLimitType));
        }
        this.enforced = speedInfo.enforced;
    }

    /**
     * Sets or overwrites the speed of the given speed limit type.
     * @param speedLimitType Speed of the given speed limit type.
     * @param speedInfo Info regarding the speed limit type.
     * @param <T> Class of speed limit type info.
     */
    public final <T> void setSpeedInfo(final SpeedLimitType<T> speedLimitType, final T speedInfo)
    {
        Throw.when(speedLimitType == null, NullPointerException.class, "Speed limit type may not be null.");
        this.speedInfoMap.put(speedLimitType, speedInfo);
    }

    /**
     * Sets or overwrites the speed of the given speed limit type.
     * @param speedLimitType Speed of the given speed limit type.
     * @param speedInfo Info regarding the speed limit type.
     * @param speedInfoClass Class of the speed info.
     * @param <T> Class of speed limit type info.
     */
    public final <T> void setSpeedInfo(final SpeedLimitType<T> speedLimitType, final T speedInfo,
        final Class<T> speedInfoClass)
    {
        this.speedInfoMap.put(speedLimitType, speedInfo);
    }

    /**
     * Clears the speed limit of given type.
     * @param speedLimitType Speed limit type to clear.
     */
    public final void clearSpeedInfo(final SpeedLimitType<?> speedLimitType)
    {
        this.speedInfoMap.remove(speedLimitType);
    }

    /**
     * Returns the info regarding a specific speed limit type.
     * @param speedLimitType Speed limit type to return info for.
     * @param <T> Class of speed limit type info.
     * @return the speed limit type info.
     */
    @SuppressWarnings("unchecked")
    public final <T> T getSpeedInfo(final SpeedLimitType<T> speedLimitType)
    {
        return (T) this.speedInfoMap.get(speedLimitType);
    }

    /**
     * Sets the legal speed limit types to enforced.
     */
    public final void setEnforced()
    {
        this.enforced = true;
    }

    /**
     * Sets the legal speed limit types to not enforced.
     */
    public final void clearEnforced()
    {
        this.enforced = false;
    }

    /**
     * Whether the legal speed limit types are enforced.
     * @return whether the legal speed limit types are enforced
     */
    public final boolean isEnforced()
    {
        return this.enforced;
    }

    /**
     * Returns the maximum vehicle speed.
     * @return Maximum vehicle speed.
     */
    public final Speed getMaximumVehicleSpeed()
    {
        return getSpeedInfo(SpeedLimitType.MAX_VEHICLE_SPEED);
    }

    /** Infinite speed used for initial comparison when deriving minimum. */
    private static final Speed INF_SPEED = new Speed(Double.POSITIVE_INFINITY, SpeedUnit.SI);

    /**
     * Returns the minimum speed of all speed limit types that are categorized as legal.
     * @return minimum speed of all speed limit types that are categorized as legal
     */
    public final Speed getLegalSpeedLimit()
    {
        Speed out = INF_SPEED;
        for (SpeedLimitType<?> slt : this.speedInfoMap.keySet())
        {
            if (slt instanceof SpeedLimitTypeLegal)
            {
                Speed spd = (Speed) this.speedInfoMap.get(slt);
                out = spd.lt(out) ? spd : out;
            }
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        StringBuilder stringBuilder = new StringBuilder("SpeedInfo [");
        stringBuilder.append(this.enforced ? "enforced" : "not enforced");
        String sep = ", ";
        for (SpeedLimitType<?> slt : this.speedInfoMap.keySet())
        {
            stringBuilder.append(sep);
            stringBuilder.append(slt.getId());
            stringBuilder.append("=");
            stringBuilder.append(this.speedInfoMap.get(slt));
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

}
