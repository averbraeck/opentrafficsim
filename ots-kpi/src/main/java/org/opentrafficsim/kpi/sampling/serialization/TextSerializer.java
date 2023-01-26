package org.opentrafficsim.kpi.sampling.serialization;

import org.djunits.value.vdouble.scalar.base.DoubleScalarInterface;
import org.djunits.value.vfloat.scalar.base.FloatScalarInterface;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.sampling.Column;

/**
 * TextSerializer defines the serialize and deserialize methods.<br>
 * <br>
 * Copyright (c) 2020-2023 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project is
 * distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> the value type
 */
public interface TextSerializer<T>
{
    /**
     * Serialize a value to text in such a way that it can be deserialized with the corresponding deserializer.
     * @param value T; the value to serialize
     * @return String; a string representation of the value that can later be deserialized
     */
    String serialize(T value);

    /**
     * Deserialize a value from text that has been created with the corresponding serializer.
     * @param type Class&lt;T&gt;; class of the value type
     * @param text String; the string to deserialize
     * @param unit String; unit with the value, may be {@code null}
     * @return T; an instance of the object created with the corresponding serializer
     */
    T deserialize(Class<T> type, String text, String unit);

    /**
     * Resolve the correct (de)serializer for the given class, and return an instance of the (de)serializer.
     * @param valueClass Class&lt;?&gt;; the class to resolve the (de)serializer for
     * @return an instance of the correct (de)serializer
     * @throws TextSerializationException when there is no corresponding (de)serializer for the class
     */
    static TextSerializer<?> resolve(final Class<?> valueClass) throws TextSerializationException
    {
        Throw.whenNull(valueClass, "valueClass cannot be null");
        if (valueClass.isPrimitive())
        {
            if (valueClass.equals(int.class))
            {
                return new IntegerSerializer();
            }
            else if (valueClass.equals(double.class))
            {
                return new DoubleSerializer();
            }
            else if (valueClass.equals(float.class))
            {
                return new FloatSerializer();
            }
            else if (valueClass.equals(long.class))
            {
                return new LongSerializer();
            }
            else if (valueClass.equals(short.class))
            {
                return new ShortSerializer();
            }
            else if (valueClass.equals(byte.class))
            {
                return new ByteSerializer();
            }
            else if (valueClass.equals(boolean.class))
            {
                return new BooleanSerializer();
            }
            else if (valueClass.equals(char.class))
            {
                return new CharacterSerializer();
            }
        }

        else if (Number.class.isAssignableFrom(valueClass))
        {
            if (valueClass.equals(Integer.class))
            {
                return new IntegerSerializer();
            }
            else if (valueClass.equals(Double.class))
            {
                return new DoubleSerializer();
            }
            else if (valueClass.equals(Float.class))
            {
                return new FloatSerializer();
            }
            else if (valueClass.equals(Long.class))
            {
                return new LongSerializer();
            }
            else if (valueClass.equals(Short.class))
            {
                return new ShortSerializer();
            }
            else if (valueClass.equals(Byte.class))
            {
                return new ByteSerializer();
            }
            else if (DoubleScalarInterface.class.isAssignableFrom(valueClass)) // AbstractScalar is a Number
            {
                return new DoubleScalarSerializer<>();
            }
            else if (FloatScalarInterface.class.isAssignableFrom(valueClass)) // AbstractScalar is a Number
            {
                return new FloatScalarSerializer<>();
            }
        }

        else if (valueClass.equals(Boolean.class))
        {
            return new BooleanSerializer();
        }

        else if (valueClass.equals(Character.class))
        {
            return new CharacterSerializer();
        }

        else if (valueClass.equals(String.class))
        {
            return new StringSerializer();
        }

        throw new TextSerializationException("Cannot resolve the Text(de)serializer for class " + valueClass.getName());
    }

    /**
     * Helper function to deal with casting when calling {@code TextSerializer.serialize()}.
     * @param <T> value type
     * @param serializer TextSerializer&lt;?&gt;; serializer
     * @param value Object; value
     * @return String; serialized value
     */
    @SuppressWarnings("unchecked")
    static <T> String serialize(final TextSerializer<?> serializer, final Object value)
    {
        return ((TextSerializer<T>) serializer).serialize((T) value);
    }

    /**
     * Helper function to deal with casting when calling {@code TextSerializer.deserialize()}.
     * @param <T> value type
     * @param serializer TextSerializer&lt;?&gt;; serializer
     * @param value String; value
     * @param column Column&lt;?&gt;; columns
     * @return T; deserialized value
     */
    @SuppressWarnings("unchecked")
    static <T> T deserialize(final TextSerializer<?> serializer, final String value, final Column<?> column)
    {
        return ((TextSerializer<T>) serializer).deserialize((Class<T>) column.getValueType(), value, column.getUnit());
    }

}
