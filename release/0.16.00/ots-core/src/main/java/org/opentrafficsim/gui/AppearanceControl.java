package org.opentrafficsim.gui;

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
 * 
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface AppearanceControl
{

    /**
     * Returns whether this item has a controllable background.
     * @return whether this item has a controllable background
     */
    default boolean isBackground()
    {
        return false;
    }

    /**
     * Returns whether this item has a controllable foreground.
     * @return whether this item has a controllable foreground
     */
    default boolean isForeground()
    {
        return false;
    }

    /**
     * Returns whether this item has a controllable font.
     * @return whether this item has a controllable font
     */
    default boolean isFont()
    {
        return false;
    }

}
