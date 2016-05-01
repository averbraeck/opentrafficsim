package org.opentrafficsim.road.network.speed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the type of a speed limit, resulting in different behavior.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 29, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Class of object type that is linked to the speed limit type.
 */
public class SpeedLimitType<T> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Map of speed limit types by id. */
    private static Map<String, SpeedLimitType<?>> speedLimitTypeMap = new HashMap<String, SpeedLimitType<?>>();

    /** Id of this speed limit type, which must be unique. */
    private final String id;

    /** Class of the info related to this speed limit type. */
    private final Class<T> infoClass;

    /**
     * Constructor.
     * @param id id of this speed limit type, which must be unique
     * @param infoClass class of the info related to this speed limit type
     */
    public SpeedLimitType(final String id, final Class<T> infoClass)
    {
        if (speedLimitTypeMap.containsKey(id))
        {
            throw new RuntimeException("Speed limit type with id '" + id + "' is already defined, id must be unique.");
        }
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
     * @return class of the info related to this speed limit type.
     */
    public final Class<T> getInfoClass()
    {
        return this.infoClass;
    }

    /**
     * Returns whether a speed limit type with given id is defined.
     * @param id Id to check.
     * @return whether a speed limit type with given id is defined
     */
    public static boolean isDefined(final String id)
    {
        return speedLimitTypeMap.containsKey(id);
    }

    /**
     * Obtain a speed limit type by id.
     * @param id Id of speed limit type to obtain.
     * @return speed limit type by id
     */
    public static SpeedLimitType<?> getById(final String id)
    {
        if (!isDefined(id))
        {
            throw new RuntimeException("Speed limit type with id '" + id + "' is requested but not defined.");
        }
        return speedLimitTypeMap.get(id);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.id;
    }

}
