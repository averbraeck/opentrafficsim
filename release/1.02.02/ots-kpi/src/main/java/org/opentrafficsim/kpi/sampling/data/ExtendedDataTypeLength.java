package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.AbsoluteLinearUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.Unit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;

/**
 * Extended data type for length values.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeLength<G extends GtuDataInterface>
        extends ExtendedDataTypeFloat<LengthUnit, FloatLength, FloatLengthVector, G>
{

    /**
     * Constructor setting the id.
     * @param id String; id
     */
    public ExtendedDataTypeLength(String id)
    {
        super(id);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatLength convertValue(final float value)
    {
        return FloatLength.createSI(value);
    }

    /** {@inheritDoc} */
    @Override
    protected final FloatLengthVector convert(final float[] storage) throws ValueException
    {
        return new FloatLengthVector(storage, LengthUnit.SI, StorageType.DENSE);
    }

    /** {@inheritDoc} */
    @Override
    public <AU extends AbsoluteLinearUnit<AU, RU>, RU extends Unit<RU>> FloatLength interpolate(final FloatLength value0,
            final FloatLength value1, final double f)
    {
        return FloatLength.interpolate(value0, value1, (float) f);
    }

}
