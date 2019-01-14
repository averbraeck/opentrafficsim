package org.opentrafficsim.swing.gui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.opentrafficsim.core.dsol.OTSModelInterface;

/**
 * Wrap a DSOL simulation model, or any (descendant of a) JPanel in a JFrame (wrap it in a window). The window will be
 * maximized.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-09-19 13:55:45 +0200 (Wed, 19 Sep 2018) $, @version $Revision: 4006 $, by $Author: averbraeck $,
 * initial version 16 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractOTSSwingApplication extends JFrame
{
    /** */
    private static final long serialVersionUID = 20141216L;

    /** the model. */
    private final OTSModelInterface model;

    /** whether the application has been closed or not. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean closed = false;

    /**
     * Wrap an OTSModel in a JFrame.
     * @param model OTSModelInterface; the model that will be shown in the JFrame
     * @param panel JPanel; this should be the JPanel of the simulation
     */
    public AbstractOTSSwingApplication(final OTSModelInterface model, final JPanel panel)
    {
        super();
        this.model = model;
        setTitle(model.getShortName());
        setContentPane(panel);
        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
        
        setExitOnClose(true);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent windowEvent)
            {
                AbstractOTSSwingApplication.this.closed = true;
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
    public final OTSModelInterface getModel()
    {
        return this.model;
    }

}
