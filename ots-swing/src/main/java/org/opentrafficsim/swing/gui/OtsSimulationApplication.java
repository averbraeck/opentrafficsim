package org.opentrafficsim.swing.gui;

import java.awt.Frame;

import javax.swing.WindowConstants;

import org.opentrafficsim.core.dsol.OtsModelInterface;

/**
 * Window to animate an OTS simulation. The window will be maximized.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> model type
 */
public class OtsSimulationApplication<T extends OtsModelInterface> extends AppearanceApplication
{

    /** */
    private static final long serialVersionUID = 20141216L;

    /**
     * Wrap an {@link OtsModelInterface} in an {@link AppearanceApplication}.
     * @param model the model that will be shown in the window
     * @param panel simulation panel
     */
    public OtsSimulationApplication(final T model, final OtsSimulationPanel panel)
    {
        super(panel);
        setTitle("OTS | The Open Traffic Simulator | " + model.getDescription());
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
        setExitOnClose(true);
        setAppearance(getAppearance()); // update appearance of added objects
    }

    /**
     * Set exit on close.
     * @param exitOnClose whether to exit the JVM when the window is closed
     */
    public void setExitOnClose(final boolean exitOnClose)
    {
        if (exitOnClose)
        {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        else
        {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
    }

}
