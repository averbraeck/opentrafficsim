package nl.tudelft.simulation.dsol.web.animation.peer;

import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.peer.FramePeer;

/**
 * HTMLFrame.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HTMLFrame extends HTMLWindow implements FramePeer

{

    /**
     * 
     */
    public HTMLFrame()
    {
        System.out.println("HTMLFrame.<init>");
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(String title)
    {
        System.out.println("HTMLFrame.setTitle()");
    }

    /** {@inheritDoc} */
    @Override
    public void setMenuBar(MenuBar mb)
    {
        System.out.println("HTMLFrame.setMenuBar()");
    }

    /** {@inheritDoc} */
    @Override
    public void setResizable(boolean resizeable)
    {
        System.out.println("HTMLFrame.setResizable()");
    }

    /** {@inheritDoc} */
    @Override
    public void setState(int state)
    {
        System.out.println("HTMLFrame.setState()");
    }

    /** {@inheritDoc} */
    @Override
    public int getState()
    {
        System.out.println("HTMLFrame.getState()");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void setMaximizedBounds(Rectangle bounds)
    {
        System.out.println("HTMLFrame.setMaximizedBounds()");
    }

    /** {@inheritDoc} */
    @Override
    public void setBoundsPrivate(int x, int y, int width, int height)
    {
        System.out.println("HTMLFrame.setBoundsPrivate()");
    }

    /** {@inheritDoc} */
    @Override
    public Rectangle getBoundsPrivate()
    {
        System.out.println("HTMLFrame.getBoundsPrivate()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void emulateActivation(boolean activate)
    {
        System.out.println("HTMLFrame.emulateActivation()");
    }

}
