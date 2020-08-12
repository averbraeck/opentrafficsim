package org.opentrafficsim.kpi.sampling;

import java.util.Collection;

import org.djunits.Throw;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;

/**
 * Abstract {@code Table} implementation taking care of the columns.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTable implements Table
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
     */
    public AbstractTable(final String id, final String description, final Collection<Column<?>> columns)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        this.id = id;
        this.description = description;
        this.columns = new ImmutableArrayList<>(columns);
    }
    
    /** {@inheritDoc} */
    @Override
    public ImmutableList<Column<?>> getColumns()
    {
        return this.columns;
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
    
}
