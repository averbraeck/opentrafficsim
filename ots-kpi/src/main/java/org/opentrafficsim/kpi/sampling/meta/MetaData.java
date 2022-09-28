package org.opentrafficsim.kpi.sampling.meta;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableIterator;

/**
 * Collection of objects, one object per meta data type included.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MetaData
{

    /** Meta data. */
    private final Map<FilterDataType<?>, Object> metaDataMap = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public MetaData()
    {
        //
    }

    /**
     * @param metaData MetaData; meta data to copy into new meta data
     */
    public MetaData(final MetaData metaData)
    {
        Throw.whenNull(metaData, "Meta data may not be null.");
        for (FilterDataType<?> metaDataType : metaData.metaDataMap.keySet())
        {
            this.metaDataMap.put(metaDataType, metaData.metaDataMap.get(metaDataType));
        }
    }

    /**
     * @param metaDataType MetaDataType&lt;T&gt;; meta data type
     * @param <T> class of meta data
     * @param value T; value of meta data
     */
    public final <T> void put(final FilterDataType<T> metaDataType, final T value)
    {
        Throw.whenNull(metaDataType, "Meta data type may not be null.");
        this.metaDataMap.put(metaDataType, value);
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
    public final <T> T get(final FilterDataType<T> metaDataType)
    {
        return (T) this.metaDataMap.get(metaDataType);
    }

    /**
     * @return set of meta data types
     */
    public final Set<FilterDataType<?>> getMetaDataTypes()
    {
        return this.metaDataMap.keySet();
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
    public final Iterator<Entry<FilterDataType<?>, Object>> getMetaDataIterator()
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
        MetaData other = (MetaData) obj;
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
        return "MetaData [" + this.metaDataMap + "]";
    }

}
