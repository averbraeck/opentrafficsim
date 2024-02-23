package org.opentrafficsim.xml.bindings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.xml.bindings.types.ExpressionType;

/**
 * Superclass for adapters that parse types using static fields, including enums. This class assumes the following to be true
 * for the static fields:
 * <ul>
 * <li>The XML type has values that match the static field names exactly.</li>
 * <li>The static field names are present under the type class itself, e.g. {@code Synchronization.PASSIVE} is a
 * {@code Synchronization}.</li>
 * <li>The type class is either an {@code enum}, or its {@code toString()} method returns the field name, e.g.
 * {@code Synchronization.PASSIVE.toString()} gives {@code "PASSIVE"}.</li>
 * </ul>
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type wrapped in ExpressionType
 * @param <E> ExpressionType
 */
public abstract class StaticFieldAdapter<T, E extends ExpressionType<T>> extends ExpressionAdapter<T, E>
{

    /** Value type. */
    private final Class<T> valueType;

    /** Expression type. */
    private final Class<E> expressionType;

    /**
     * Constructor.
     * @param valueType Class&lt;T&gt;; value type.
     * @param expressionType Class&lt;E&gt;; expression type.
     */
    protected StaticFieldAdapter(final Class<T> valueType, final Class<E> expressionType)
    {
        this.valueType = valueType;
        this.expressionType = expressionType;
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final E value)
    {
        return marshal(value, (t) -> t instanceof Enum ? ((Enum<?>) t).name() : t.toString());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public E unmarshal(final String value) throws IllegalArgumentException
    {
        try
        {
            if (isExpression(value))
            {
                Constructor<E> constructor = ClassUtil.resolveConstructor(this.expressionType, new Class[] {String.class});
                return constructor.newInstance(trimBrackets(value));
            }
            Constructor<E> constructor = ClassUtil.resolveConstructor(this.expressionType, new Class[] {this.valueType});
            return constructor.newInstance((T) ClassUtil.resolveField(this.valueType, value).get(null));
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchFieldException e)
        {
            throw new IllegalArgumentException("Unable to parse value " + value + " for type " + this.expressionType, e);
        }
    }

}
