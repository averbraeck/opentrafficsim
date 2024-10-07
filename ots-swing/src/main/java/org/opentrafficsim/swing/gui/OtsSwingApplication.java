package org.opentrafficsim.swing.gui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.opentrafficsim.animation.gtu.colorer.DefaultSwitchableGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.dsol.OtsModelInterface;

/**
 * Wrap a DSOL simulation model, or any (descendant of a) JPanel in a JFrame (wrap it in a window). The window will be
 * maximized.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @param <T> model type
 */
public class OtsSwingApplication<T extends OtsModelInterface> extends AppearanceApplication
{
    /** */
    private static final long serialVersionUID = 20141216L;

    /** Single instance of default colorer, reachable from various places. */
    public static final GtuColorer DEFAULT_COLORER = new DefaultSwitchableGtuColorer();

    /** the model. */
    private final T model;

    /** whether the application has been closed or not. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean closed = false;

    /**
     * Wrap an OtsModel in a JFrame. Uses a default GTU colorer.
     * @param model the model that will be shown in the JFrame
     * @param panel this should be the JPanel of the simulation
     */
    public OtsSwingApplication(final T model, final JPanel panel)
    {
        super(panel);
        this.model = model;
        setTitle("OTS | The Open Traffic Simulator | " + model.getDescription());
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);

        setExitOnClose(true);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent windowEvent)
            {
                OtsSwingApplication.this.closed = true;
                super.windowClosing(windowEvent);
            }
        });
    }

    /**
     * Return the initial 'home' extent for the animation. The 'Home' button returns to this extent. Override this method when a
     * smaller or larger part of the infra should be shown. In the default setting, all currently visible objects are shown.
     * @return the initial and 'home' rectangle for the animation.
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Rectangle2D makeAnimationRectangle()
    {
        return this.model.getNetwork().getExtent();
    }

    /**
     * @param exitOnClose set exitOnClose
     */
    public final void setExitOnClose(final boolean exitOnClose)
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

    /**
     * @return closed
     */
    public final boolean isClosed()
    {
        return this.closed;
    }

    /**
     * @return model
     */
    public final T getModel()
    {
        return this.model;
    }

}
