package org.opentrafficsim.swing.gui;

import java.awt.Color;

/**
 * Contains a background color, foreground color and a font name, to be set throughout all components.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum Appearance
{

    /** MOTUS mimic. Grid not visible. */
    MOTUS("Motus", new Color(236, 233, 216), Color.BLACK, Color.BLACK, "Verdana"),

    /** Green. */
    FOSIM("Fosim", new Color(240, 240, 240), Color.BLACK, new Color(0, 128, 0), "Verdana"),

    /** Dark. */
    DARK("Dark", new Color(96, 96, 96), Color.WHITE, Color.DARK_GRAY, "Verdana"),

    /** Gray. */
    GRAY("Gray", Color.LIGHT_GRAY, Color.BLACK, new Color(96, 96, 96), "Verdana"),

    /** Bright. */
    BRIGHT("Bright", Color.LIGHT_GRAY, Color.BLACK, Color.WHITE, "Verdana"),

    /** Legacy, as the initial OTS had. */
    LEGACY("Legacy", new Color(238, 238, 238), Color.BLACK, Color.WHITE, "Dialog"),

    /** Red. */
    RED("Red", new Color(208, 192, 192), Color.RED.darker().darker(), new Color(208, 192, 192).darker(), "Verdana"),

    /** Green. */
    GREEN("Green", new Color(192, 208, 192), Color.GREEN.darker().darker(), new Color(192, 208, 192).darker(), "Verdana"),

    /** Blue. */
    BLUE("Blue", new Color(192, 192, 208), Color.BLUE.darker().darker(), new Color(192, 192, 208).darker(), "Verdana");

    /** Name. */
    private final String name;

    /** Background color. */
    private final Color background;

    /** Foreground color. */
    private final Color foreground;

    /** Backdrop color. (network panel) */
    private final Color backdrop;

    /** font name. */
    private final String font;

    /**
     * Constructor.
     * @param name String; name
     * @param background Color; background color
     * @param foreground Color; foreground color
     * @param backdrop Color; backdrop color (network panel)
     * @param font String; font name
     */
    Appearance(final String name, final Color background, final Color foreground, final Color backdrop, final String font)
    {
        this.name = name;
        this.background = background;
        this.foreground = foreground;
        this.backdrop = backdrop;
        this.font = font;
    }

    /**
     * Returns the name.
     * @return String; name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Returns the background color.
     * @return Color; color
     */
    public final Color getBackground()
    {
        return this.background;
    }

    /**
     * Returns the foreground color.
     * @return Color; color
     */
    public final Color getForeground()
    {
        return this.foreground;
    }

    /**
     * Returns the backdrop color.
     * @return Color; color
     */
    public final Color getBackdrop()
    {
        return this.backdrop;
    }

    /**
     * Returns the font name.
     * @return String; font name
     */
    public final String getFont()
    {
        return this.font;
    }

}
