package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.DurationUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;

/**
 * Extended data type for duration values.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeDuration<G extends GtuDataInterface>
        extends ExtendedDataTypeFloat<DurationUnit, FloatDuration, FloatDurationVector, G>
{

    /**
     * Constructor setting the id.
     * @param id id
     */
    public ExtendedDataTypeDuration(String id)
    {
        super(id);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatDuration convertValue(final float value)
    {
        return FloatDuration.createSI(value);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatDurationVector convert(final float[] storage) throws ValueException
    {
        return new FloatDurationVector(storage, DurationUnit.SI, StorageType.DENSE);
    }

}
