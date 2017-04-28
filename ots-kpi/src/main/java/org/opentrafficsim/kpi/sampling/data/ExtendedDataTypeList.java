package org.opentrafficsim.kpi.sampling.data;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.kpi.sampling.SamplingException;

import nl.tudelft.simulation.language.Throw;

/**
 * Extended data type for anything that can be captured in a list. Typically, these are non-numeric objects. 
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of value
 */
public abstract class ExtendedDataTypeList<T> extends ExtendedDataType<T, List<T>, List<T>>
{

    /**
     * Constructor setting the id.
     * @param id id
     */
    public ExtendedDataTypeList(final String id)
    {
        super(id);
    }

    /** {@inheritDoc} */
    @Override
    public T getOutputValue(List<T> output, int i) throws SamplingException
    {
        Throw.when(i < 0 || i >= output.size(), SamplingException.class, "Index %d out of range.", i);
        return output.get(i);
    }
    
    /** {@inheritDoc} */
    @Override
    public T getStorageValue(List<T> output, int i) throws SamplingException
    {
        // same format for lists
        return getOutputValue(output, i);
    }

    /** {@inheritDoc} */
    @Override
    public List<T> initializeStorage()
    {
        return new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public List<T> convert(final List<T> storage, final int size)
    {
        return storage;
    }

}
