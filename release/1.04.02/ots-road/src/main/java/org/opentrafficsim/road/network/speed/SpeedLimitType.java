package org.opentrafficsim.road.network.speed;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.Type;

/**
 * Defines the type of a speed limit, resulting in different behavior.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Class of speed info that is linked to the speed limit type.
 */
public class SpeedLimitType<T> extends Type<SpeedLimitType<T>> implements Serializable, Identifiable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Id of this speed limit type. */
    private final String id;

    /** Class of the info related to this speed limit type. */
    private final Class<T> infoClass;

    /**
     * Constructor.
     * @param id String; id of this speed limit type, which must be unique
     * @param infoClass Class&lt;T&gt;; class of the info related to this speed limit type
     * @throws NullPointerException if id or info class is null
     */
    public SpeedLimitType(final String id, final Class<T> infoClass)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(infoClass, "Info class may not be null.");
        this.id = id;
        this.infoClass = infoClass;
    }

    /**
     * Returns the id.
     * @return the id
     */
    @Override
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
        if (!this.infoClass.equals(other.infoClass))
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
