package nl.tudelft.simulation.dsol.web.animation.D2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.media.j3d.BoundingBox;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.vecmath.Point3d;
import javax.vecmath.Point4i;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DComparator;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.dsol.web.animation.HTMLGraphics2D;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.naming.context.ContextUtil;

/**
 * The AnimationPanel to display animated (Locatable) objects. Added the possibility to witch layers on and off. By default all
 * layers will be drawn, so no changes to existing software need to be made.<br>
 * copyright (c) 2002-2018 <a href="https://simulation.tudelft.nl">Delft University of Technology </a>, the Netherlands. <br>
 * See for project information <a href="https://simulation.tudelft.nl">www.simulation.tudelft.nl </a>.
 * <p>
 * Copyright (c) 2002-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://simulation.tudelft.nl/" target="_blank"> https://simulation.tudelft.nl</a>. The DSOL
 * project is distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://simulation.tudelft.nl/dsol/3.0/license.html" target="_blank">
 * https://simulation.tudelft.nl/dsol/3.0/license.html</a>.
 * </p>
 * @author <a href="http://www.peter-jacobs.com">Peter Jacobs </a>
 */
public class HTMLAnimationPanel extends HTMLGridPanel implements EventListenerInterface, NamespaceChangeListener
{
    /** the elements of this panel. */
    private SortedSet<Renderable2DInterface<? extends Locatable>> elements =
            new TreeSet<Renderable2DInterface<? extends Locatable>>(new Renderable2DComparator());

    /** filter for types to be shown or not. */
    private Map<Class<? extends Locatable>, Boolean> visibilityMap = new HashMap<>();

    /** cache of the classes that are hidden. */
    private Set<Class<? extends Locatable>> hiddenClasses = new HashSet<>();

    /** cache of the classes that are shown. */
    private Set<Class<? extends Locatable>> shownClasses = new HashSet<>();

    /** the simulator. */
    private SimulatorInterface<?, ?, ?> simulator;

    /** the eventContext. */
    private EventContext context = null;

    /** a line that helps the user to see where s/he is dragging. */
    private Point4i dragLine = new Point4i();

    /** enable drag line. */
    private boolean dragLineEnabled = false;

    /** List of drawable objects. */
    private List<Renderable2DInterface<? extends Locatable>> elementList = new ArrayList<>();

    /** dirty flag for the list. */
    private boolean dirtyElements = false;

    /**
     * constructs a new AnimationPanel.
     * @param extent Rectangle2D; the extent of the panel
     * @param size Dimension; the size of the panel.
     * @param simulator SimulatorInterface&lt;?,?,?&gt;; the simulator of which we want to know the events for animation
     * @throws RemoteException on network error for one of the listeners
     */
    public HTMLAnimationPanel(final Rectangle2D extent, final Dimension size, final SimulatorInterface<?, ?, ?> simulator)
            throws RemoteException
    {
        super(extent, size);
        super.showGrid = true;
        this.simulator = simulator;
        simulator.addListener(this, SimulatorInterface.START_REPLICATION_EVENT);
    }

    /** {@inheritDoc} */
    @Override
    public void paintComponent(final HTMLGraphics2D g2)
    {
        // draw the grid.
        super.paintComponent(g2);

        // update drawable elements when necessary
        if (this.dirtyElements)
        {
            synchronized (this.elementList)
            {
                this.elementList.clear();
                this.elementList.addAll(this.elements);
                this.dirtyElements = false;
            }
        }

        // draw the animation elements.
        for (Renderable2DInterface<? extends Locatable> element : this.elementList)
        {
            if (isShowElement(element))
            {
                element.paint(g2, this.getExtent(), this.getSize(), this);
            }
        }

        // draw drag line if enabled.
        if (this.dragLineEnabled)
        {
            g2.setColor(Color.BLACK);
            g2.drawLine(this.dragLine.w, this.dragLine.x, this.dragLine.y, this.dragLine.z);
            this.dragLineEnabled = false;
        }
    }

    /**
     * Test whether the element needs to be shown on the screen or not.
     * @param element Renderable2DInterface&lt;? extends Locatable&gt;; the renderable element to test
     * @return whether the element needs to be shown or not
     */
    public boolean isShowElement(final Renderable2DInterface<? extends Locatable> element)
    {
        return isShowClass(element.getSource().getClass());
    }

    /**
     * Test whether a certain class needs to be shown on the screen or not. The class needs to implement Locatable, otherwise it
     * cannot be shown at all.
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class to test
     * @return whether the class needs to be shown or not
     */
    public boolean isShowClass(final Class<? extends Locatable> locatableClass)
    {
        if (this.hiddenClasses.contains(locatableClass))
        {
            return false;
        }
        else
        {
            boolean show = true;
            if (!this.shownClasses.contains(locatableClass))
            {
                for (Class<? extends Locatable> lc : this.visibilityMap.keySet())
                {
                    if (lc.isAssignableFrom(locatableClass))
                    {
                        if (!this.visibilityMap.get(lc))
                        {
                            show = false;
                        }
                    }
                }
                // add to the right cache
                if (show)
                {
                    this.shownClasses.add(locatableClass);
                }
                else
                {
                    this.hiddenClasses.add(locatableClass);
                }
            }
            return show;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getSource() instanceof AnimatorInterface
                && event.getType().equals(SimulatorInterface.START_REPLICATION_EVENT))
        {
            synchronized (this.elementList)
            {
                this.elements.clear();
                try
                {
                    if (this.context != null)
                    {
                        this.context.removeNamingListener(this);
                    }

                    this.context =
                            (EventContext) ContextUtil.lookup(this.simulator.getReplication().getContext(), "/animation/2D");
                    this.context.addNamingListener("", EventContext.SUBTREE_SCOPE, this);
                    NamingEnumeration<Binding> list = this.context.listBindings("");
                    while (list.hasMore())
                    {
                        Binding binding = list.next();
                        this.objectAdded(new NamingEvent(this.context, -1, binding, binding, null));
                    }
                    this.repaint();
                }
                catch (Exception exception)
                {
                    SimLogger.always().warn(exception, "notify");
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void objectAdded(final NamingEvent namingEvent)
    {
        @SuppressWarnings("unchecked")
        Renderable2DInterface<? extends Locatable> element =
                (Renderable2DInterface<? extends Locatable>) namingEvent.getNewBinding().getObject();
        synchronized (this.elementList)
        {
            this.elements.add(element);
            this.dirtyElements = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void objectRemoved(final NamingEvent namingEvent)
    {
        @SuppressWarnings("unchecked")
        Renderable2DInterface<? extends Locatable> element =
                (Renderable2DInterface<? extends Locatable>) namingEvent.getOldBinding().getObject();
        synchronized (this.elementList)
        {
            this.elements.remove(element);
            this.dirtyElements = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void objectRenamed(final NamingEvent namingEvent)
    {
        this.objectRemoved(namingEvent);
        this.objectAdded(namingEvent);
    }

    /** {@inheritDoc} */
    @Override
    public void namingExceptionThrown(final NamingExceptionEvent namingEvent)
    {
        SimLogger.always().warn(namingEvent.getException(), "namingExceptionThrown");
    }

    /**
     * Calculate the full extent based on the current positions of the objects.
     * @return the full extent of the animation.
     */
    public synchronized Rectangle2D fullExtent()
    {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        Point3d p3dL = new Point3d();
        Point3d p3dU = new Point3d();
        try
        {
            for (Renderable2DInterface<? extends Locatable> renderable : this.elementList)
            {
                DirectedPoint l = renderable.getSource().getLocation();
                BoundingBox b = new BoundingBox(renderable.getSource().getBounds());
                b.getLower(p3dL);
                b.getUpper(p3dU);
                minX = Math.min(minX, l.x + Math.min(p3dL.x, p3dU.x));
                minY = Math.min(minY, l.y + Math.min(p3dL.y, p3dU.y));
                maxX = Math.max(maxX, l.x + Math.max(p3dL.x, p3dU.x));
                maxY = Math.max(maxY, l.y + Math.max(p3dL.y, p3dU.y));
            }
        }
        catch (Exception e)
        {
            // ignore
        }

        minX = minX - 0.05 * Math.abs(minX);
        minY = minY - 0.05 * Math.abs(minY);
        maxX = maxX + 0.05 * Math.abs(maxX);
        maxY = maxY + 0.05 * Math.abs(maxY);

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * resets the panel to its an extent that covers all displayed objects.
     */
    public synchronized void zoomAll()
    {
        this.extent = Renderable2DInterface.Util.computeVisibleExtent(fullExtent(), this.getSize());
        this.repaint();
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be shown.
     */
    public void showClass(final Class<? extends Locatable> locatableClass)
    {
        this.visibilityMap.put(locatableClass, true);
        this.shownClasses.clear();
        this.hiddenClasses.clear();
        this.repaint();
    }

    /**
     * Set a class to be hidden in the animation to true.
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which the animation has to be hidden.
     */
    public void hideClass(final Class<? extends Locatable> locatableClass)
    {
        this.visibilityMap.put(locatableClass, false);
        this.shownClasses.clear();
        this.hiddenClasses.clear();
        this.repaint();
    }

    /**
     * Toggle a class to be displayed in the animation to its reverse value.
     * @param locatableClass Class&lt;? extends Locatable&gt;; the class for which a visible animation has to be turned off or
     *            vice versa.
     */
    public void toggleClass(final Class<? extends Locatable> locatableClass)
    {
        if (!this.visibilityMap.containsKey(locatableClass))
        {
            showClass(locatableClass);
        }
        this.visibilityMap.put(locatableClass, !this.visibilityMap.get(locatableClass));
        this.shownClasses.clear();
        this.hiddenClasses.clear();
        this.repaint();
    }

    /**
     * @return the set of animation elements.
     */
    public final SortedSet<Renderable2DInterface<? extends Locatable>> getElements()
    {
        return this.elements;
    }

    /**
     * @return returns the dragLine.
     */
    public final Point4i getDragLine()
    {
        return this.dragLine;
    }

    /**
     * @return returns the dragLineEnabled.
     */
    public final boolean isDragLineEnabled()
    {
        return this.dragLineEnabled;
    }

    /**
     * @param dragLineEnabled boolean; the dragLineEnabled to set.
     */
    public final void setDragLineEnabled(final boolean dragLineEnabled)
    {
        this.dragLineEnabled = dragLineEnabled;
    }
}
