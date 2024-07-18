package org.opentrafficsim.kpi.sampling.meta;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableIterator;

/**
 * Collection of object sets, one object set per filter data type included. This defines constraints to which filter data has to
 * comply, e.g. having any of the objects in the set, or covered all in the set, etc., depending on the filter data type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FilterDataSet
{

    /** Meta data. */
    private final Map<FilterDataType<?, ?>, Set<?>> filterDataMap = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public FilterDataSet()
    {
        //
    }

    /**
     * Constructor that copies the input.
     * @param filterDataSet FilterDataSet; set of filter data to copy into new filter data set
     */
    public FilterDataSet(final FilterDataSet filterDataSet)
    {
        Throw.whenNull(filterDataSet, "Filter data set may not be null.");
        for (FilterDataType<?, ?> filterDataType : filterDataSet.filterDataMap.keySet())
        {
            this.filterDataMap.put(filterDataType, filterDataSet.filterDataMap.get(filterDataType));
        }
    }

    /**
     * Add filter data for type.
     * @param filterDataType FilterDataType&lt;T, ?&gt;; filter data type
     * @param <T> class of filter data
     * @param values Set&lt;T&gt;; values of filter data
     */
    public final <T> void put(final FilterDataType<T, ?> filterDataType, final Set<T> values)
    {
        Throw.whenNull(filterDataType, "Filter data type may not be null.");
        Throw.whenNull(values, "Values may not be null.");
        this.filterDataMap.put(filterDataType, values);
    }

    /**
     * Returns whether the filter data type is contained.
     * @param filterDataType FilterDataType&lt;?, ?&gt;; filter data type
     * @return whether the trajectory contains the filter data of give type
     */
    public final boolean contains(final FilterDataType<?, ?> filterDataType)
    {
        return this.filterDataMap.containsKey(filterDataType);
    }

    /**
     * Returns the value set of a filter data type.
     * @param filterDataType FilterDataType&lt;T, ?&gt;; filter data type
     * @param <T> class of filter data
     * @return value of filter data
     */
    @SuppressWarnings("unchecked")
    public final <T> Set<T> get(final FilterDataType<T, ?> filterDataType)
    {
        return (Set<T>) this.filterDataMap.get(filterDataType);
    }

    /**
     * Returns the filter data types.
     * @return set of filter data types
     */
    public final Set<FilterDataType<?, ?>> getFilterDataTypes()
    {
        return new LinkedHashSet<>(this.filterDataMap.keySet());
    }

    /**
     * Returns the number of filter data entries.
     * @return number of filter data entries
     */
    public final int size()
    {
        return this.filterDataMap.size();
    }

    /**
     * Returns an iterator over the filter data types.
     * @return iterator over filter data entries, removal is not allowed
     */
    public final Iterator<Entry<FilterDataType<?, ?>, Set<?>>> getFilterDataSetIterator()
    {
        return new ImmutableIterator<>(this.filterDataMap.entrySet().iterator());
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.filterDataMap == null) ? 0 : this.filterDataMap.hashCode());
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
        FilterDataSet other = (FilterDataSet) obj;
        if (this.filterDataMap == null)
        {
            if (other.filterDataMap != null)
            {
                return false;
            }
        }
        else if (!this.filterDataMap.equals(other.filterDataMap))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "FilterDataSet [filterDataMap=" + this.filterDataMap + "]";
    }

}
