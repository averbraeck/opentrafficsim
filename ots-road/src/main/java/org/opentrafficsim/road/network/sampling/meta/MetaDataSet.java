package org.opentrafficsim.road.network.sampling.meta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opentrafficsim.core.immutablecollections.ImmutableIterator;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 25, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MetaDataSet
{

    /** Meta data. */
    private final Map<MetaDataType<?>, Set<?>> metaDataMap = new HashMap<>();

    /**
     * Default constructor.
     */
    public MetaDataSet()
    {
        //
    }

    /**
     * @param metaDataSet set of meta data to copy into new meta data set
     */
    public MetaDataSet(final MetaDataSet metaDataSet)
    {
        for (MetaDataType<?> metaDataType : metaDataSet.metaDataMap.keySet())
        {
            this.metaDataMap.put(metaDataType, metaDataSet.metaDataMap.get(metaDataType));
        }
    }

    /**
     * @param metaDataType meta data type
     * @param <T> class of meta data
     * @param values values of meta data
     */
    public final <T> void put(final MetaDataType<T> metaDataType, final Set<T> values)
    {
        this.metaDataMap.put(metaDataType, values);
    }

    /**
     * @param metaDataType meta data type
     * @return whether the trajectory contains the meta data of give type
     */
    public final boolean contains(final MetaDataType<?> metaDataType)
    {
        return this.metaDataMap.containsKey(metaDataType);
    }

    /**
     * @param metaDataType meta data type
     * @param <T> class of meta data
     * @return value of meta data
     */
    @SuppressWarnings("unchecked")
    public final <T> Set<T> get(final MetaDataType<T> metaDataType)
    {
        return (Set<T>) this.metaDataMap.get(metaDataType);
    }

    /**
     * @return set of meta data types
     */
    public final Set<MetaDataType<?>> getMetaDataTypes()
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
    public final Iterator<Entry<MetaDataType<?>, Set<?>>> getMetaDataSetIterator()
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
        MetaDataSet other = (MetaDataSet) obj;
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
    
}
