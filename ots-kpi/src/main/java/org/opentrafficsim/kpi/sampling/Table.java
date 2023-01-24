package org.opentrafficsim.kpi.sampling;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.Throw;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.base.Identifiable;

/**
 * Abstract table implementation taking care of the columns. Sub classes must provide an {@code Iterator} over {@code Record}s
 * and may have methods to add data.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class Table implements Iterable<Row>, Identifiable
{

    /** Id. */
    private final String id;

    /** Description. */
    private final String description;

    /** Columns. */
    private final ImmutableList<Column<?>> columns;

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     * @param columns Collection&lt;Column&lt;?&gt;&gt;; columns
     * @throws IllegalArgumentException in case of duplicate column ids.
     */
    public Table(final String id, final String description, final Collection<Column<?>> columns)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        Set<String> ids = new LinkedHashSet<>();
        columns.forEach((column) -> ids.add(column.getId()));
        Throw.when(ids.size() != columns.size(), IllegalArgumentException.class, "Duplicate column ids are not allowed.");
        this.id = id;
        this.description = description;
        this.columns = new ImmutableArrayList<>(columns);
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
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
     * Returns the list of columns.
     * @return list of columns
     */
    public ImmutableList<Column<?>> getColumns()
    {
        return this.columns;
    }

    /**
     * Return a specific column.
     * @param columnNumber int; number of the column.
     * @return Column&lt;?&gt;; column.
     * @throws IllegalArgumentException if the column number is &lt; 0 or &gt; {@code getNumberOfColumns() - 1}. 
     */
    public Column<?> getColumn(final int columnNumber)
    {
        return this.columns.get(columnNumber);
    }

    /**
     * Returns the number of columns.
     * @return number of columns
     */
    public int getNumberOfColumns()
    {
        return this.columns.size();
    }

    /**
     * Returns the number of the column in this table.
     * @param column Column&lt;?&gt;; column.
     * @return int; column number.
     * @throws IllegalArgumentException if the column is not in the table.
     */
    public int getColumnNumber(final Column<?> column)
    {
        Throw.when(!this.columns.contains(column), IllegalArgumentException.class, "Column %s is not in the table.",
                column.getId());
        return this.columns.indexOf(column);
    }

    /**
     * Returns the number of the column with given id.
     * @param columnId String; column id.
     * @return int; column number.
     * @throws IllegalArgumentException if the column is not in the table.
     */
    public int getColumnNumber(final String columnId)
    {
        for (int columnNumber = 0; columnNumber < getNumberOfColumns(); columnNumber++)
        {
            if (this.columns.get(columnNumber).getId().equals(columnId))
            {
                return columnNumber;
            }
        }
        throw new IllegalArgumentException("Column " + columnId + " is not in the table.");
    }

    /**
     * Returns whether the table is empty.
     * @return whether the table is empty
     */
    public abstract boolean isEmpty();

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Table [id=" + this.id + ", description=" + this.description + ", columns=" + this.columns + "]";
    }

}
