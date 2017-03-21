package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.vector.FloatSpeedVector;

/**
 * Extended data type for speed values.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class ExtendedDataTypeSpeed extends ExtendedDataTypeFloat<FloatSpeed, FloatSpeedVector>
{

    /**
     * Constructor setting the id.
     * @param id id
     */
    public ExtendedDataTypeSpeed(String id)
    {
        super(id);
    }
    
    /** {@inheritDoc} */
    @Override
    protected final FloatSpeed convertValue(final float value)
    {
        return FloatSpeed.createSI(value);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatSpeedVector convert(final float[] storage) throws ValueException
    {
        return new FloatSpeedVector(storage, SpeedUnit.SI, StorageType.DENSE);
    }

}
