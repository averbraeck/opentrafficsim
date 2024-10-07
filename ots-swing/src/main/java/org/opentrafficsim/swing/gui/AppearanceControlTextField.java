package org.opentrafficsim.swing.gui;

import javax.swing.JTextField;

/**
 * Text field that ignore appearance on foreground and background.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AppearanceControlTextField extends JTextField implements AppearanceControl
{
    /** */
    private static final long serialVersionUID = 20240227L;

    /** {@inheritDoc} */
    @Override
    public boolean isForeground()
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBackground()
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "AppearanceControlTextField []";
    }
}
