package org.opentrafficsim.draw.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * Properties for text to identify animated objects in OTS.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 17, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TextProperties implements Serializable
{
    /** */
    private static final long serialVersionUID = 20170400L;

    /** the text alignment (LEFT, CENTER or RIGHT). */
    private final TextAlignment textAlignment;

    /** the color of the text, e.g., Color.RED. */
    private final Color color;

    /** the name of the font, e.g., specified as Font.SERIF. */
    private final String font;

    /** a map of text attributes to set, e.g., UNDERLINE or WEIGHT. */
    private final Map<TextAttribute, Object> textAttributes;

    /** the size of the font (in units of the animation, e.g., meters). */
    private final float fontSize;

    /**
     * Construct a default set of text properties for animation.
     */
    public TextProperties()
    {
        this(TextAlignment.CENTER, Color.BLACK, 1.5f);
    }

    /**
     * Construct a set of text properties for animation with alignment, color and size.
     * @param textAlignment TextAlignment; the text alignment (LEFT, CENTER or RIGHT)
     * @param color Color; the color of the text
     * @param fontSize float; the size of the font (in units of the animation, e.g., meters)
     */
    public TextProperties(final TextAlignment textAlignment, final Color color, final float fontSize)
    {
        this(textAlignment, color, fontSize, Font.SANS_SERIF, new Hashtable<>());
    }

    /**
     * Construct a set of text properties for animation with alignment, color and size.
     * @param textAlignment TextAlignment; the text alignment (LEFT, CENTER or RIGHT)
     * @param color Color; the color of the text, e.g., Color.RED
     * @param fontSize float; the size of the font (in units of the animation, e.g., meters)
     * @param font String; the name of the font, e.g., specified as Font.SERIF
     * @param textAttributes Map&lt;TextAttribute,Object&gt;; a map of text attributes to set, e.g., UNDERLINE or WEIGHT
     */
    public TextProperties(final TextAlignment textAlignment, final Color color, final float fontSize, final String font,
            final Map<TextAttribute, Object> textAttributes)
    {
        this.textAlignment = textAlignment;
        this.color = color;
        this.fontSize = fontSize;
        this.font = font;
        this.textAttributes = new Hashtable<>(textAttributes);
    }

    /**
     * @return textAlignment
     */
    public final TextAlignment getTextAlignment()
    {
        return this.textAlignment;
    }

    /**
     * @return color
     */
    public final Color getColor()
    {
        return this.color;
    }

    /**
     * @return font
     */
    public final String getFont()
    {
        return this.font;
    }

    /**
     * @return textAttributes
     */
    public final Map<TextAttribute, Object> getTextAttributes()
    {
        return this.textAttributes;
    }

    /**
     * @return fontSize
     */
    public final float getFontSize()
    {
        return this.fontSize;
    }

    /**
     * Set the weight to either WEIGHT_REGULAR or WEIGHT_BOLD.
     * @param bold boolean; whether the font is bold or regular
     */
    public final void setBold(final boolean bold)
    {
        this.textAttributes.put(TextAttribute.WEIGHT, bold ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
    }

    /**
     * Set the weight to one of multiple weight constants.
     * @param weight TextWeight; the weight of the font to use
     */
    public final void setBold(final TextWeight weight)
    {
        this.textAttributes.put(TextAttribute.WEIGHT, weight.getValue());
    }

    /**
     * Set the posture to either POSTURE_REGULAR or POSTURE_OBLIQUE (italic).
     * @param italic boolean; whether the font is italic or regular
     */
    public final void setItalic(final boolean italic)
    {
        this.textAttributes.put(TextAttribute.POSTURE, italic ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
    }

    /**
     * Set the width to WIDTH_CONDENSED, WIDTH_REGULAR, or WIDTH_EXTENDED.
     * @param width TextWidth; the TextWidth to use
     */
    public final void setWeightBold(final TextWidth width)
    {
        this.textAttributes.put(TextAttribute.WIDTH, width.getValue());
    }

    /**
     * Set the underline on or off.
     * @param underline boolean; whether the font is underlined or regular
     */
    public final void setUnderline(final boolean underline)
    {
        this.textAttributes.put(TextAttribute.UNDERLINE, underline ? TextAttribute.UNDERLINE_ON : -1);
    }

    /**
     * Set the strikethrough on or off.
     * @param strikethrough boolean; whether the font is strikethrough or regular
     */
    public final void setStrikethrough(final boolean strikethrough)
    {
        this.textAttributes.put(TextAttribute.STRIKETHROUGH, strikethrough ? TextAttribute.STRIKETHROUGH_ON : -1);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TextProperties [textAlignment=" + this.textAlignment + ", color=" + this.color + ", font=" + this.font
                + ", textAttributes=" + this.textAttributes + ", fontSize=" + this.fontSize + "]";
    }

}
