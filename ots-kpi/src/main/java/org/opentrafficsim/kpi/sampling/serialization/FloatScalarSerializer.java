package org.opentrafficsim.kpi.sampling.serialization;

import org.djunits.unit.Unit;
import org.djunits.value.vfloat.scalar.base.AbstractFloatScalar;

/**
 * ScalarSerializer (de)serializes DJUNITS float scalars.<br>
 * <br>
 * Copyright (c) 2020-2023 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project is
 * distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <U> the unit type
 * @param <S> the scalar type
 */
public class FloatScalarSerializer<U extends Unit<U>, S extends AbstractFloatScalar<U, S>> extends DoubleScalarSerializer<U, S>
{

    /**
     * Serialize an AbstractFloatScalar value to text in such a way that it can be deserialized with the corresponding
     * deserializer.
     * @param value Object; the scalar to serialize
     * @return String; a string representation of the value that can later be deserialized
     */
    @Override
    public String serialize(final S value)
    {
        return String.valueOf(value.floatValue());
    }

}
