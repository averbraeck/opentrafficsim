package org.opentrafficsim.xml.bindings.types;

import java.awt.Color;

import org.djutils.reflection.ClassUtil;

/**
 * Expression type with Color value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class ColorType extends ExpressionType<Color>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Color> TO_TYPE = SerializableFunction.of(Color.class, ColorType::valueOf);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public ColorType(final Color value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public ColorType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * Parses {@code String} to to the right type.
     * @param str input string
     * @return parsed output
     */
    public static Color valueOf(final String str)
    {
        String colorStr = str.replaceAll("\\s", "");

        if (colorStr.startsWith("#"))
        {
            return Color.decode(colorStr);
        }

        if (colorStr.startsWith("RGB"))
        {
            String c = colorStr.substring(3).replace("(", "").replace(")", "");
            String[] rgb = c.split(",");
            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());
            return new Color(r, g, b);
        }

        try
        {
            return (Color) ClassUtil.resolveField(Color.class, colorStr).get(null);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException exception)
        {
            throw new IllegalArgumentException("Unable to parse Color " + str);
        }
    }

}
