package nl.tudelft.simulation.dsol.web.animation.peer;

import java.awt.Component;
import java.awt.Window;
import java.awt.peer.KeyboardFocusManagerPeer;

/**
 * HTMLKeyboardFocusManagerPeer.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HTMLKeyboardFocusManagerPeer implements KeyboardFocusManagerPeer
{

    /**
     * 
     */
    public HTMLKeyboardFocusManagerPeer()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void setCurrentFocusedWindow(Window win)
    {
    }

    /** {@inheritDoc} */
    @Override
    public Window getCurrentFocusedWindow()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setCurrentFocusOwner(Component comp)
    {
    }

    /** {@inheritDoc} */
    @Override
    public Component getCurrentFocusOwner()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void clearGlobalFocusOwner(Window activeWindow)
    {
    }

}
