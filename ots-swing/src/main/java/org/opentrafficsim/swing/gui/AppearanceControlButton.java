package org.opentrafficsim.swing.gui;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Appearance control {@link JButton}.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AppearanceControlButton extends JButton implements AppearanceControl
{
    /** */
    private static final long serialVersionUID = 20180206L;

    /**
     * Constructor.
     * @param loadIcon icon
     */
    public AppearanceControlButton(final Icon loadIcon)
    {
        super(loadIcon);
    }

    @Override
    public boolean isFont()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "AppearanceControlButton []";
    }
}
