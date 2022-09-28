package org.opentrafficsim.kpi.sampling;

/**
 * Simple column implementation.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public class SimpleColumn<T> implements Column<T>
{

    /** Id. */
    private final String id;

    /** Description. */
    private final String description;

    /** Value type. */
    private final Class<T> valueType;

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     * @param valueType Class&lt;T&gt;; value type
     */
    public SimpleColumn(final String id, final String description, final Class<T> valueType)
    {
        this.id = id;
        this.description = description;
        this.valueType = valueType;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription()
    {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public Class<T> getValueType()
    {
        return this.valueType;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SimpleColumn [id=" + id + ", description=" + description + ", valueType=" + valueType + "]";
    }

}
