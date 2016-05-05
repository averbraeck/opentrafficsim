package org.opentrafficsim.road.network.speed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.core.Throw;

/**
 * Defines the type of a speed limit, resulting in different behavior.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 29, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Class of speed info that is linked to the speed limit type.
 */
public class SpeedLimitType<T> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Map of speed limit types by id. */
    private static Map<String, SpeedLimitType<?>> speedLimitTypeMap = new HashMap<>();

    /** Id of this speed limit type, which must be unique. */
    private final String id;

    /** Class of the info related to this speed limit type. */
    private final Class<T> infoClass;

    /**
     * Constructor.
     * @param id id of this speed limit type, which must be unique
     * @param infoClass class of the info related to this speed limit type
     * @throws IllegalArgumentException if the provided id is already used
     * @throws NullPointerException if id or info class is null
     */
    public SpeedLimitType(final String id, final Class<T> infoClass)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(infoClass, "Info class may not be null.");
        Throw.when(speedLimitTypeMap.containsKey(id), IllegalArgumentException.class,
            "Speed limit type with id '%s' is already defined, id must be unique.", id);
        this.id = id;
        this.infoClass = infoClass;
        speedLimitTypeMap.put(id, this);
    }

    /**
     * Returns the id.
     * @return the id
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the class of the info related to this speed limit type.
     * @return class of the info related to this speed limit type
     */
    public final Class<T> getInfoClass()
    {
        return this.infoClass;
    }

    /**
     * Returns whether a speed limit type with given id is defined.
     * @param id id to check
     * @return whether a speed limit type with given id is defined
     */
    public static boolean isDefined(final String id)
    {
        return speedLimitTypeMap.containsKey(id);
    }

    /**
     * Obtain a speed limit type by id.
     * @param id id of speed limit type to obtain.
     * @return speed limit type by id
     * @throws IllegalStateException if no speed limit type with given id exists
     */
    public static SpeedLimitType<?> getById(final String id)
    {
        Throw.when(!isDefined(id), IllegalStateException.class,
            "Speed limit type with id '%s' is requested but not defined.", id);
        return speedLimitTypeMap.get(id);
    }
    
    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id.hashCode();
        result = prime * result + this.infoClass.hashCode();
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
        SpeedLimitType<?> other = (SpeedLimitType<?>) obj;
        if (!this.id.equals(other.id))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SpeedLimitType [" + this.id + "]";
    }

}
