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
 * Collection of object sets, one object set per meta data type included. This defines constraints to which meta data has to
 * comply, e.g. having any of the objects in the set, or covered all in the set, etc., depending on the meta data type.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 25, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FilterDataSet
{

    /** Meta data. */
    private final Map<FilterDataType<?>, Set<?>> metaDataMap = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public FilterDataSet()
    {
        //
    }

    /**
     * @param metaDataSet MetaDataSet; set of meta data to copy into new meta data set
     */
    public FilterDataSet(final FilterDataSet metaDataSet)
    {
        Throw.whenNull(metaDataSet, "Meta data set may not be null.");
        for (FilterDataType<?> metaDataType : metaDataSet.metaDataMap.keySet())
        {
            this.metaDataMap.put(metaDataType, metaDataSet.metaDataMap.get(metaDataType));
        }
    }

    /**
     * @param metaDataType MetaDataType&lt;T&gt;; meta data type
     * @param <T> class of meta data
     * @param values Set&lt;T&gt;; values of meta data
     */
    public final <T> void put(final FilterDataType<T> metaDataType, final Set<T> values)
    {
        Throw.whenNull(metaDataType, "Meta data type may not be null.");
        Throw.whenNull(values, "Values may not be null.");
        this.metaDataMap.put(metaDataType, values);
    }

    /**
     * @param metaDataType MetaDataType&lt;?&gt;; meta data type
     * @return whether the trajectory contains the meta data of give type
     */
    public final boolean contains(final FilterDataType<?> metaDataType)
    {
        return this.metaDataMap.containsKey(metaDataType);
    }

    /**
     * @param metaDataType MetaDataType&lt;T&gt;; meta data type
     * @param <T> class of meta data
     * @return value of meta data
     */
    @SuppressWarnings("unchecked")
    public final <T> Set<T> get(final FilterDataType<T> metaDataType)
    {
        return (Set<T>) this.metaDataMap.get(metaDataType);
    }

    /**
     * @return set of meta data types
     */
    public final Set<FilterDataType<?>> getMetaDataTypes()
    {
        return new LinkedHashSet<>(this.metaDataMap.keySet());
    }

    /**
     * @return number of meta data entries
     */
    public final int size()
    {
        return this.metaDataMap.size();
    }

    /**
     * @return iterator over meta data entries, removal is not allowed
     */
    public final Iterator<Entry<FilterDataType<?>, Set<?>>> getFilterDataSetIterator()
    {
        return new ImmutableIterator<>(this.metaDataMap.entrySet().iterator());
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.metaDataMap == null) ? 0 : this.metaDataMap.hashCode());
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
        if (this.metaDataMap == null)
        {
            if (other.metaDataMap != null)
            {
                return false;
            }
        }
        else if (!this.metaDataMap.equals(other.metaDataMap))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "MetaDataSet [metaDataMap=" + this.metaDataMap + "]";
    }

}
