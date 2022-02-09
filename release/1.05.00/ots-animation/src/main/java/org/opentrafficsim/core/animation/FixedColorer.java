package org.opentrafficsim.core.animation;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FixedColorer stores a fixed color for drawing. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @param <D> the Drawable type
 */
public class FixedColorer<D extends Drawable> implements Colorer<D>
{
    /** the fixed color to use. */
    private final Color color;

    /** cache for colors, so they're only stored once. */
    private static final Map<Color, FixedColorer<?>> CACHE = new LinkedHashMap<>();

    /** The color white. */
    private static final FixedColorer<? extends Drawable> WHITE = new FixedColorer<>(Color.WHITE);

    /** The color light gray. */
    private static final FixedColorer<? extends Drawable> LIGHT_GRAY = new FixedColorer<>(Color.LIGHT_GRAY);

    /** The color gray. */
    private static final FixedColorer<? extends Drawable> GRAY = new FixedColorer<>(Color.GRAY);

    /** The color dark gray. */
    private static final FixedColorer<? extends Drawable> DARK_GRAY = new FixedColorer<>(Color.DARK_GRAY);

    /** The color black. */
    private static final FixedColorer<? extends Drawable> BLACK = new FixedColorer<>(Color.BLACK);

    /** The color red. */
    private static final FixedColorer<? extends Drawable> RED = new FixedColorer<>(Color.RED);

    /** The color pink. */
    private static final FixedColorer<? extends Drawable> PINK = new FixedColorer<>(Color.PINK);

    /** The color orange. */
    private static final FixedColorer<? extends Drawable> ORANGE = new FixedColorer<>(Color.ORANGE);

    /** The color yellow. */
    private static final FixedColorer<? extends Drawable> YELLOW = new FixedColorer<>(Color.YELLOW);

    /** The color green. */
    private static final FixedColorer<? extends Drawable> GREEN = new FixedColorer<>(Color.GREEN);

    /** The color magenta. */
    private static final FixedColorer<? extends Drawable> MAGENTA = new FixedColorer<>(Color.MAGENTA);

    /** The color cyan. */
    private static final FixedColorer<? extends Drawable> CYAN = new FixedColorer<>(Color.CYAN);

    /** The color blue. */
    private static final FixedColorer<? extends Drawable> BLUE = new FixedColorer<>(Color.BLUE);

    /**
     * Initialize the FixedColorer with a color.
     * @param color Color; the fixed color to use
     */
    protected FixedColorer(final Color color)
    {
        this.color = color;
        CACHE.put(color, this);
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final D drawable)
    {
        return this.color;
    }

    /**
     * Instantiate a singleton fixed colorer for a certain color.
     * @param color Color; the fixed color to use
     * @return the FixedColorer
     * @param <D> the Drawable type
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> create(final Color color)
    {
        if (CACHE.containsKey(color))
        {
            return (FixedColorer<D>) CACHE.get(color);
        }
        return new FixedColorer<D>(color);
    }

    /**
     * @param <D> the Drawable type
     * @return black color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> black()
    {
        return (FixedColorer<D>) BLACK;
    }

    /**
     * @param <D> the Drawable type
     * @return blue color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> blue()
    {
        return (FixedColorer<D>) BLUE;
    }

    /**
     * @param <D> the Drawable type
     * @return cyan color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> cyan()
    {
        return (FixedColorer<D>) CYAN;
    }

    /**
     * @param <D> the Drawable type
     * @return darkGray color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> darkGray()
    {
        return (FixedColorer<D>) DARK_GRAY;
    }

    /**
     * @param <D> the Drawable type
     * @return gray color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> gray()
    {
        return (FixedColorer<D>) GRAY;
    }

    /**
     * @param <D> the Drawable type
     * @return green color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> green()
    {
        return (FixedColorer<D>) GREEN;
    }

    /**
     * @param <D> the Drawable type
     * @return lightGray color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> lightGray()
    {
        return (FixedColorer<D>) LIGHT_GRAY;
    }

    /**
     * @param <D> the Drawable type
     * @return magenta color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> magenta()
    {
        return (FixedColorer<D>) MAGENTA;
    }

    /**
     * @param <D> the Drawable type
     * @return orange color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> orange()
    {
        return (FixedColorer<D>) ORANGE;
    }

    /**
     * @param <D> the Drawable type
     * @return pink color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> pink()
    {
        return (FixedColorer<D>) PINK;
    }

    /**
     * @param <D> the Drawable type
     * @return red color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> red()
    {
        return (FixedColorer<D>) RED;
    }

    /**
     * @param <D> the Drawable type
     * @return white color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> white()
    {
        return (FixedColorer<D>) WHITE;
    }

    /**
     * @param <D> the Drawable type
     * @return yellow color
     */
    @SuppressWarnings("unchecked")
    public static <D extends Drawable> FixedColorer<D> yellow()
    {
        return (FixedColorer<D>) YELLOW;
    }

}
