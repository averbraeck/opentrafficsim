package nl.tudelft.simulation.dsol.web.animation.peer;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;

import sun.awt.CausedFocusEvent.Cause;
import sun.java2d.pipe.Region;

/**
 * HTMLComponent.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class HTMLComponent implements ComponentPeer
{

    /**
     * 
     */
    public HTMLComponent()
    {
    }

    /** {@inheritDoc} */
    @Override
    public boolean isObscured()
    {
        System.out.println("HTMLComponent.isObscured()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canDetermineObscurity()
    {
        System.out.println("HTMLComponent.canDetermineObscurity()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean v)
    {
        System.out.println("HTMLComponent.setVisible(" + v + ")");
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean e)
    {
        System.out.println("HTMLComponent.setEnabled()");
    }

    /** {@inheritDoc} */
    @Override
    public void paint(Graphics g)
    {
        System.out.println("HTMLComponent.paint()");
    }

    /** {@inheritDoc} */
    @Override
    public void print(Graphics g)
    {
        System.out.println("HTMLComponent.print()");
    }

    /** {@inheritDoc} */
    @Override
    public void setBounds(int x, int y, int width, int height, int op)
    {
        System.out.println("HTMLComponent.setBounds()");
    }

    /** {@inheritDoc} */
    @Override
    public void handleEvent(AWTEvent e)
    {
        System.out.println("HTMLComponent.handleEvent(" + e.toString() + ")");
    }

    /** {@inheritDoc} */
    @Override
    public void coalescePaintEvent(PaintEvent e)
    {
        System.out.println("HTMLComponent.coalescePaintEvent()");
    }

    /** {@inheritDoc} */
    @Override
    public Point getLocationOnScreen()
    {
        System.out.println("HTMLComponent.getLocationOnScreen()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredSize()
    {
        System.out.println("HTMLComponent.getPreferredSize()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getMinimumSize()
    {
        System.out.println("HTMLComponent.getMinimumSize()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ColorModel getColorModel()
    {
        System.out.println("HTMLComponent.getColorModel()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Graphics getGraphics()
    {
        System.out.println("HTMLComponent.getGraphics()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public FontMetrics getFontMetrics(Font font)
    {
        System.out.println("HTMLComponent.getFontMetrics()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose()
    {
        System.out.println("HTMLComponent.dispose()");
    }

    /** {@inheritDoc} */
    @Override
    public void setForeground(Color c)
    {
        System.out.println("HTMLComponent.setForeground()");
    }

    /** {@inheritDoc} */
    @Override
    public void setBackground(Color c)
    {
        System.out.println("HTMLComponent.setBackground()");
    }

    /** {@inheritDoc} */
    @Override
    public void setFont(Font f)
    {
        System.out.println("HTMLComponent.setFont()");
    }

    /** {@inheritDoc} */
    @Override
    public void updateCursorImmediately()
    {
        System.out.println("HTMLComponent.updateCursorImmediately()");
    }

    /** {@inheritDoc} */
    @Override
    public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time,
            Cause cause)
    {
        System.out.println("HTMLComponent.requestFocus()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFocusable()
    {
        System.out.println("HTMLComponent.isFocusable()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(ImageProducer producer)
    {
        System.out.println("HTMLComponent.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(int width, int height)
    {
        System.out.println("HTMLComponent.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public VolatileImage createVolatileImage(int width, int height)
    {
        System.out.println("HTMLComponent.createVolatileImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean prepareImage(Image img, int w, int h, ImageObserver o)
    {
        System.out.println("HTMLComponent.prepareImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int checkImage(Image img, int w, int h, ImageObserver o)
    {
        System.out.println("HTMLComponent.checkImage()");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public GraphicsConfiguration getGraphicsConfiguration()
    {
        System.out.println("HTMLComponent.getGraphicsConfiguration()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean handlesWheelScrolling()
    {
        System.out.println("HTMLComponent.handlesWheelScrolling()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException
    {
        System.out.println("HTMLComponent.createBuffers()");
    }

    /** {@inheritDoc} */
    @Override
    public Image getBackBuffer()
    {
        System.out.println("HTMLComponent.getBackBuffer()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void flip(int x1, int y1, int x2, int y2, FlipContents flipAction)
    {
        System.out.println("HTMLComponent.flip()");
    }

    /** {@inheritDoc} */
    @Override
    public void destroyBuffers()
    {
        System.out.println("HTMLComponent.destroyBuffers()");
    }

    /** {@inheritDoc} */
    @Override
    public void reparent(ContainerPeer newContainer)
    {
        System.out.println("HTMLComponent.reparent()");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReparentSupported()
    {
        System.out.println("HTMLComponent.isReparentSupported()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void layout()
    {
        System.out.println("HTMLComponent.layout()");
    }

    /** {@inheritDoc} */
    @Override
    public void applyShape(Region shape)
    {
        System.out.println("HTMLComponent.applyShape()");
    }

    /** {@inheritDoc} */
    @Override
    public void setZOrder(ComponentPeer above)
    {
        System.out.println("HTMLComponent.setZOrder()");
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateGraphicsData(GraphicsConfiguration gc)
    {
        System.out.println("HTMLComponent.updateGraphicsData()");
        return false;
    }

}
