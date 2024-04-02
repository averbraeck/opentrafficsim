package org.opentrafficsim.swing.gui;

/**
 * This interface allows on screen items to <b>not</b> obtain colors and/or the font from an {@code Appearance}. This can be
 * useful for items where considering only a background and a foreground color is not sufficient, possibly creating unreadable
 * or otherwise unpleasant on screen items.<br>
 * <br>
 * The default implementation of the methods in this interface return {@code false}, meaning the {@code Appearance} is
 * completely ignored. In order to ignore it only partially, some methods should be overridden to return true. An example of
 * this is given below, which will only allow the font of the {@code Appearance}. It also shows a convenient way to implement
 * this interface when using default on screen items using a local class.
 * 
 * <pre>
 * class AppearanceControlComboBox&lt;T&gt; extends JComboBox&lt;T&gt; implements AppearanceControl
 * {
 *     public boolean isFont()
 *     {
 *         return true;
 *     }
 * }
 * 
 * JComboBox&lt;String&gt; comboBox = new AppearanceControlComboBox&lt;&gt;();
 * </pre>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface AppearanceControl extends nl.tudelft.simulation.dsol.swing.gui.appearance.AppearanceControl
{

    /** Default font size. */
    public static int DEFAULT_FONT_SIZE = 12;

    /**
     * Returns the font size. May be {@code null} to ignore font size.
     * @return Integer; font size, may be {@code null} to ignore font size from appearance.
     */
    default Integer getFontSize()
    {
        return DEFAULT_FONT_SIZE;
    }

}
