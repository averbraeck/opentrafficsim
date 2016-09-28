package org.opentrafficsim.road.network.sampling;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public class MetaData
{
    
    /** Meta data. */
    private final Map<MetaDataType<?>, Object> metaDataMap = new HashMap<>();

    /**
     * Default constructor.
     */
    public MetaData()
    {
        //
    }
    
    /**
     * @param metaData meta data to copy into new meta data
     */
    public MetaData(final MetaData metaData)
    {
        for (MetaDataType<?> metaDataType : metaData.metaDataMap.keySet())
        {
            this.metaDataMap.put(metaDataType, metaData.metaDataMap.get(metaDataType));
        }
    }
    
    /**
     * @param metaDataType meta data type
     * @param <T> class of meta data
     * @param value value of meta data
     */
    public final <T> void put(final MetaDataType<T> metaDataType, final T value)
    {
        this.metaDataMap.put(metaDataType, value);
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
    public final <T> T get(final MetaDataType<T> metaDataType)
    {
        return (T) this.metaDataMap.get(metaDataType);
    }
    
    /**
     * @return set of meta data types
     */
    public final Set<MetaDataType<?>> getMetaDataTypes()
    {
        return this.metaDataMap.keySet();
    }
    
}
