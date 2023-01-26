package org.opentrafficsim.kpi.sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.djunits.Throw;

/**
 * Table implementation that stores {@code Record}s in a {@code List}.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ListTable extends Table
{

    /** Records. */
    private List<Row> rows = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     * @param columns Collection&lt;Column&lt;?&gt;&gt;; columns
     */
    public ListTable(final String id, final String description, final Collection<Column<?>> columns)
    {
        super(id, description, columns);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Row> iterator()
    {
        return this.rows.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        return this.rows.isEmpty();
    }

    /**
     * Adds a row to the table.
     * @param data Map&lt;String, Object&gt;; data with values given per column
     * @throws IllegalArgumentException when the size or data types in the data map do not comply to the columns
     */
    public void addRow(final Map<Column<?>, Object> data)
    {
        Throw.whenNull(data, "Data may not be null.");
        Throw.when(data.size() != getNumberOfColumns(), IllegalArgumentException.class,
                "Number of data columns doesn't match number of table columns.");
        Object[] dataObjects = new Object[getNumberOfColumns()];
        for (int index = 0; index < getNumberOfColumns(); index++)
        {
            Column<?> column = getColumn(index);
            Throw.when(!data.containsKey(column), IllegalArgumentException.class, "Missing data for column %s", column.getId());
            Object value = data.get(column);
            checkValueType(column, value);
            dataObjects[index] = value;
        }
        this.rows.add(new Row(this, dataObjects));
    }

    /**
     * Adds a row to the table.
     * @param data Map&lt;String, Object&gt;; data with values given per column id
     * @throws IllegalArgumentException when the size or data types in the data map do not comply to the columns
     */
    public void addRowByColumnIds(final Map<String, Object> data)
    {
        Throw.whenNull(data, "Data may not be null.");
        Throw.when(data.size() != getNumberOfColumns(), IllegalArgumentException.class,
                "Number of data columns doesn't match number of table columns.");
        Object[] dataObjects = new Object[getNumberOfColumns()];
        for (int index = 0; index < getNumberOfColumns(); index++)
        {
            Column<?> column = getColumn(index);
            Throw.when(!data.containsKey(column.getId()), IllegalArgumentException.class, "Missing data for column %s",
                    column.getId());
            Object value = data.get(column.getId());
            checkValueType(column, value);
            dataObjects[index] = value;
        }
        this.rows.add(new Row(this, dataObjects));
    }

    /**
     * Adds a row to the table. The order in which the elements in the array are offered should be the same as the order of the
     * columns.
     * @param data Object[]; row data
     * @throws IllegalArgumentException when the size, order or data types in the {@code Object[]} do not comply to the columns
     */
    public void addRow(final Object[] data)
    {
        Throw.whenNull(data, "Data may not be null.");
        Throw.when(data.length != getNumberOfColumns(), IllegalArgumentException.class,
                "Number of data columns doesn't match number of table columns.");
        for (int index = 0; index < getNumberOfColumns(); index++)
        {
            checkValueType(getColumn(index), data[index]);
        }
        Object[] dataObjects = new Object[getNumberOfColumns()]; // safe copy
        System.arraycopy(data, 0, dataObjects, 0, getNumberOfColumns());
        this.rows.add(new Row(this, dataObjects));
    }

    /**
     * Checks whether the type of a value is suitable for a column.
     * @param column Column&lt;?&gt;; column.
     * @param value Object; value.
     */
    private void checkValueType(final Column<?> column, final Object value)
    {
        if (null != value)
        {
            Class<?> valueType = value.getClass();
            Throw.when(!column.getValueType().isAssignableFrom(valueType), IllegalArgumentException.class,
                    "Data value for column %s is not of type %s, but of type %s.", column.getId(), column.getValueType(),
                    valueType);
        }
    }

}
