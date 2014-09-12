/*
 * @(#) AnimationPanel.java Nov 3, 2003 Copyright (c) 2002-2005 Delft University
 * of Technology Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved. This software is proprietary information of Delft University of
 * Technology 
 */
package nl.tudelft.simulation.animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.vecmath.Point4i;

import nl.tudelft.simulation.animation.mouse.InputListener;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.logger.Logger;
import nl.tudelft.simulation.naming.context.ContextUtil;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * The AnimationPanel <br>
 * (c) copyright 2002-2005 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the
 * Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no
 * warranty.
 * 
 * @version $Revision: 1.2 $ $Date: 2010/08/10 11:37:49 $
 * @author <a href="http://www.peter-jacobs.com">Peter Jacobs </a>
 */
public class AnimationPanel extends GridPanel implements EventListenerInterface, NamespaceChangeListener
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the elements of this panel */
    private SortedSet<Renderable2DInterface> elements = Collections.synchronizedSortedSet(new TreeSet<Renderable2DInterface>(new Renderable2DComparator()));

    /** the simulator */
    private OTSSimulatorInterface simulator;
    
    /** the eventContext */
    private EventContext context = null;

    /** the grid must be drawn after all other elements. Therefore we must override the gridPanel.paintGrid. */
    @SuppressWarnings("hiding")
    private boolean showGrid = true;

    /** a line that helps the user to see where he is dragging */
    private Point4i dragLine = new Point4i();

    /** enable drag line */
    private boolean dragLineEnabled = false;

    /**
     * constructs a new AnimationPanel
     * 
     * @param extent the extent of the panel
     * @param size the size of the panel.
     * @param simulator the simulator of which we want to know the events for animation
     */
    public AnimationPanel(final Rectangle2D extent, final Dimension size, final OTSSimulatorInterface simulator)
    {
        super(extent, size);
        super.showGrid = false;
        InputListener listener = new InputListener(this);
        this.simulator = simulator;
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
        this.addMouseWheelListener(listener);
        this.addKeyListener(listener);
        try
        {
            simulator.addListener(this, AnimatorInterface.UPDATE_ANIMATION_EVENT);
            simulator.addListener(this, SimulatorInterface.START_REPLICATION_EVENT);
        } catch (RemoteException exception)
        {
            exception.printStackTrace();
        }

        // update with 25 frames per second if possible, and if animation is in view
        new UpdateThread().start();
    }

    /**
     * @see javax.swing.JComponent #paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(final Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        synchronized (this.elements)
        {
            for (Renderable2DInterface element : this.elements)
            {
                element.paint(g2, this.getExtent(), this.getSize(), this);
            }
        }
        if (this.showGrid)
        {
            this.drawGrid(g);
        }

        // Draw dragline
        if (this.dragLineEnabled)
        {
            g.setColor(Color.BLACK);
            g.drawLine(this.dragLine.w, this.dragLine.x, this.dragLine.y, this.dragLine.z);
            this.dragLineEnabled = false;
        }
    }

    /**
     * @see nl.tudelft.simulation.event.EventListenerInterface #notify(nl.tudelft.simulation.event.EventInterface)
     */
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getSource() instanceof AnimatorInterface && event.getType().equals(AnimatorInterface.UPDATE_ANIMATION_EVENT))
        {
            if (this.getWidth() > 0 || this.getHeight() > 0)
            {
                this.repaint();
            }
            return;
        }

        if (event.getSource() instanceof AnimatorInterface && event.getType().equals(SimulatorInterface.START_REPLICATION_EVENT))
        {
            this.elements.clear();
            try
            {
                if (this.context != null)
                {
                    this.context.removeNamingListener(this);
                }

                this.context = (EventContext) ContextUtil.lookup(this.simulator.getContext(), "/animation/2D");
                this.context.addNamingListener("", EventContext.SUBTREE_SCOPE, this);
                NamingEnumeration<Binding> list = this.context.listBindings("");
                while (list.hasMore())
                {
                    Binding binding = list.next();
                    this.objectAdded(new NamingEvent(this.context, -1, binding, binding, null));
                }
                this.repaint();
            } catch (Exception exception)
            {
                Logger.warning(this, "notify", exception);
            }
        }
    }

    /**
     * @see javax.naming.event.NamespaceChangeListener #objectAdded(javax.naming.event.NamingEvent)
     */
    public void objectAdded(final NamingEvent namingEvent)
    {
        Renderable2DInterface element = (Renderable2DInterface) namingEvent.getNewBinding().getObject();
        this.elements.add(element);
    }

    /**
     * @see javax.naming.event.NamespaceChangeListener #objectRemoved(javax.naming.event.NamingEvent)
     */
    public void objectRemoved(final NamingEvent namingEvent)
    {
        Renderable2DInterface element = (Renderable2DInterface) namingEvent.getOldBinding().getObject();
        this.elements.remove(element);
    }

    /**
     * @see javax.naming.event.NamespaceChangeListener #objectRenamed(javax.naming.event.NamingEvent)
     */
    public void objectRenamed(final NamingEvent namingEvent)
    {
        this.objectRemoved(namingEvent);
        this.objectAdded(namingEvent);
    }

    /**
     * @see javax.naming.event.NamingListener #namingExceptionThrown(javax.naming.event.NamingExceptionEvent)
     */
    public void namingExceptionThrown(final NamingExceptionEvent namingEvent)
    {
        Logger.warning(this, "namingExceptionThrown", namingEvent.getException());
    }

    /**
     * @return Returns the elements.
     */
    public SortedSet<Renderable2DInterface> getElements()
    {
        return this.elements;
    }

    /**
     * @see nl.tudelft.simulation.dsol.gui.animation2D.GridPanel#isShowGrid()
     */
    @Override
    public boolean isShowGrid()
    {
        return this.showGrid;
    }

    /**
     * @see nl.tudelft.simulation.dsol.gui.animation2D.GridPanel#showGrid(boolean)
     */
    @Override
    public synchronized void showGrid(final boolean bool)
    {
        this.showGrid = bool;
        this.repaint();
    }

    /**
     * @return returns the dragLine.
     */
    public Point4i getDragLine()
    {
        return this.dragLine;
    }

    /**
     * @return returns the dragLineEnabled.
     */
    public boolean isDragLineEnabled()
    {
        return this.dragLineEnabled;
    }

    /**
     * @param dragLineEnabled the dragLineEnabled to set.
     */
    public void setDragLineEnabled(final boolean dragLineEnabled)
    {
        this.dragLineEnabled = dragLineEnabled;
    }

    /**
     * <br>
     * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
     * reserved.
     * 
     * The MEDLABS project (Modeling Epidemic Disease with Large-scale Agent-Based Simulation) is aimed at providing
     * policy analysis tools to predict and help contain the spread of epidemics. It makes use of the DSOL simulation
     * engine and the agent-based modeling formalism. See for project information <a
     * href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>. The project is a co-operation between TU
     * Delft, Systems Engineering and Simulation Department (Netherlands) and NUDT, Simulation Engineering Department
     * (China).
     * 
     * This software is licensed under the BSD license. See license.txt in the main project.
     * 
     * @version Jun 2, 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/mzhang">Mingxin Zhang </a>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck </a>
     */
    protected class UpdateThread extends Thread
    {

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            while (true)
            {
                if (AnimationPanel.this.isShowing())
                {
                    if (AnimationPanel.this.getWidth() > 0 || AnimationPanel.this.getHeight() > 0)
                    {
                        AnimationPanel.this.repaint();
                    }
                    try
                    {
                        Thread.sleep(1000 / 25); // 25 fps
                    } catch (InterruptedException e)
                    {
                        // do nothing
                    }
                }
            }
        }
    }
}