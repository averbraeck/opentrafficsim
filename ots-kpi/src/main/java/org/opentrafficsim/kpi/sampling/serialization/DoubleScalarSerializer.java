package org.opentrafficsim.kpi.sampling.serialization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.Unit;
import org.djunits.value.AbstractScalar;

/**
 * ScalarSerializer (de)serializes DJUNITS double scalars.<br>
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
public class DoubleScalarSerializer<U extends Unit<U>, S extends AbstractScalar<U, S>> implements TextSerializer<S>
{
    /** cache of the retrieved valueOf(String) methods for scalars based on the stored string. */
    private static Map<String, Method> valueOfMethodCache = new LinkedHashMap<>();

    /**
     * Serialize an AbstractScalar value to text in such a way that it can be deserialized with the corresponding deserializer.
     * @param value Object; the scalar to serialize
     * @return String; a string representation of the value that can later be deserialized
     */
    @Override
    public String serialize(final S value)
    {
        return String.valueOf(value.doubleValue());
    }

    /**
     * Deserialize a String to the correct AbstractScalar value. The method caches the valueOf(String) method for repeated use.
     * @param text String; the text to deserialize
     * @return S; the reconstructed scalar
     */
    @SuppressWarnings("unchecked")
    @Override
    public S deserialize(final Class<S> type, final String text, final String unit)
    {
        try
        {
            Method valueOfMethod = valueOfMethodCache.get(type.getName());
            if (valueOfMethod == null)
            {
                valueOfMethod = type.getDeclaredMethod("valueOf", String.class);
                valueOfMethodCache.put(type.getName(), valueOfMethod);
            }
            return (S) valueOfMethod.invoke(null, text + unit);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException exception)
        {
            throw new RuntimeException(exception);
        }
    }

}
