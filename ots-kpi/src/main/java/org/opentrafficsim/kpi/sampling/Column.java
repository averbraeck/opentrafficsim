package org.opentrafficsim.kpi.sampling;

import java.io.Serializable;
import java.util.Objects;

import org.djunits.Throw;
import org.opentrafficsim.base.Identifiable;

/**
 * Column identifier and descriptor.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public class Column<T> implements Identifiable, Serializable
{

    /** */
    private static final long serialVersionUID = 20230125L;

    /** Id. */
    private final String id;

    /** Description. */
    private final String description;

    /** Value type. */
    private final Class<T> valueType;

    /** Unit. */
    private final String unit;

    /**
     * Constructor.
     * @param id String; id.
     * @param description String; description.
     * @param valueType Class&lt;T&gt;; value type.
     * @param unit String; unit, may be {@code null}.
     */
    public Column(final String id, final String description, final Class<T> valueType, final String unit)
    {
        Throw.whenNull(id, "id may not be null.");
        Throw.when(id.length() == 0, IllegalArgumentException.class, "id cannot be empty");
        Throw.whenNull(description, "description may not be null.");
        Throw.whenNull(valueType, "valueType may not be null.");
        this.id = id;
        this.description = description;
        this.valueType = valueType;
        this.unit = unit;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Returns the column description.
     * @return String; column description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the type of the values in the column.
     * @return Class&lt;?&gt;; type of the values in the column
     */
    public Class<T> getValueType()
    {
        return this.valueType;
    }

    /**
     * Returns the unit of the column. Data is written an read using this unit.
     * @return String; unit of the column
     */
    public String getUnit()
    {
        return this.unit;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
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
        Column<?> other = (Column<?>) obj;
        return Objects.equals(this.id, other.id);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Column [id=" + this.id + ", description=" + this.description + ", valueType=" + this.valueType
                + (this.unit == null ? "]" : ", unit=" + this.unit + "]");
    }

}
