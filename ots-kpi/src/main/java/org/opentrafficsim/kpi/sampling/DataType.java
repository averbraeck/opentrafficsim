package org.opentrafficsim.kpi.sampling;

import java.util.Optional;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Abstract class for defining a type of extended or filter data.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of value
 * @param <G> GTU data type
 */
public abstract class DataType<T, G extends GtuData> implements Identifiable
{

    /** Id. */
    private final String id;

    /** Description. */
    private final String description;

    /** Type of value. */
    private final Class<T> type;

    /**
     * Constructor setting the id.
     * @param id id
     * @param description description
     * @param type type class
     */
    public DataType(final String id, final String description, final Class<T> type)
    {
        Throw.whenNull(id, "Id may nog be null.");
        Throw.whenNull(description, "Description may nog be null.");
        Throw.whenNull(type, "Type may not bee null.");
        this.id = id;
        this.description = description;
        this.type = type;
    }

    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the description.
     * @return description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the value type.
     * @return the value type
     */
    public Class<T> getType()
    {
        return this.type;
    }

    /**
     * Retrieves the value of the data of this type from a GTU.
     * @param gtu gtu to retrieve the value from
     * @return value of the data of this type from a GTU, may be empty if not applicable.
     */
    public abstract Optional<T> getValue(G gtu);

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id.hashCode();
        return result;
    }

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
        DataType<?, ?> other = (DataType<?, ?>) obj;
        if (!this.id.equals(other.id))
        {
            return false;
        }
        return true;
    }

}
