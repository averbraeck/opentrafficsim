package org.opentrafficsim.swing.gui;

import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;

/**
 * A {@link TabbedContentPane} which ignores appearance (it has too much colors looking ugly / becoming unreadable).
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class AppearanceControlTabbedContentPane extends TabbedContentPane implements AppearanceControl
{

    /** */
    private static final long serialVersionUID = 20180206L;

    /**
     * Constructor.
     * @param tabPlacement tabPlacement
     */
    public AppearanceControlTabbedContentPane(final int tabPlacement)
    {
        super(tabPlacement);
    }

    @Override
    public boolean isForeground()
    {
        return true;
    }

    @Override
    public boolean isFont()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "AppearanceControlTabbedContentPane []";
    }

}
