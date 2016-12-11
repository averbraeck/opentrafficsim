package org.opentrafficsim.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.vecmath.Point3d;
import javax.vecmath.Point4i;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.animation.D2.GridPanel;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DComparator;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.naming.context.ContextUtil;

/**
 * The AnimationPanel <br>
 * (c) copyright 2002-2005 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no warranty.
 * @version $Revision: 1.2 $ $Date: 2010/08/10 11:37:49 $
 * @author <a href="http://www.peter-jacobs.com">Peter Jacobs </a>
 */
public class ShowHideAnimationPanel extends GridPanel implements EventListenerInterface, NamespaceChangeListener
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the elements of this panel. */
    private SortedSet<Renderable2DInterface> elements =
            Collections.synchronizedSortedSet(new TreeSet<Renderable2DInterface>(new Renderable2DComparator()));

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

    /** a line that helps the user to see where he is dragging. */
    private Point4i dragLine = new Point4i();

    /** enable drag line. */
    private boolean dragLineEnabled = false;

    /** the logger. */
    private static Logger logger = LogManager.getLogger(AnimationPanel.class);

    /**
     * constructs a new AnimationPanel.
     * @param extent the extent of the panel
     * @param size the size of the panel.
     * @param simulator the simulator of which we want to know the events for animation
     */
    public ShowHideAnimationPanel(final Rectangle2D extent, final Dimension size, final SimulatorInterface<?, ?, ?> simulator)
    {
        super(extent, size);
        super.showGrid = true;
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
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final synchronized void paintComponent(final Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;

        // draw the grid.
        super.paintComponent(g2);

        // draw the animation elements.
        synchronized (this.elements)
        {
            for (Renderable2DInterface element : this.elements)
            {
                Class<? extends Locatable> locatableClass = element.getSource().getClass();
                if (!this.hiddenClasses.contains(locatableClass))
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
                    if (show)
                    {
                        element.paint(g2, this.getExtent(), this.getSize(), this);
                    }
                }
            }
        }

        // draw drag line if enabled.
        if (this.dragLineEnabled)
        {
            g.setColor(Color.BLACK);
            g.drawLine(this.dragLine.w, this.dragLine.x, this.dragLine.y, this.dragLine.z);
            this.dragLineEnabled = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getSource() instanceof AnimatorInterface
                && event.getType().equals(AnimatorInterface.UPDATE_ANIMATION_EVENT) && this.isShowing())
        {
            if (this.getWidth() > 0 || this.getHeight() > 0)
            {
                this.repaint();
            }
            return;
        }

        if (event.getSource() instanceof AnimatorInterface
                && event.getType().equals(SimulatorInterface.START_REPLICATION_EVENT))
        {
            this.elements.clear();
            try
            {
                if (this.context != null)
                {
                    this.context.removeNamingListener(this);
                }

                this.context = (EventContext) ContextUtil.lookup(this.simulator.getReplication().getContext(),
                        "/animation/2D");
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
                logger.warn("notify", exception);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void objectAdded(final NamingEvent namingEvent)
    {
        Renderable2DInterface element = (Renderable2DInterface) namingEvent.getNewBinding().getObject();
        this.elements.add(element);
    }

    /** {@inheritDoc} */
    @Override
    public void objectRemoved(final NamingEvent namingEvent)
    {
        Renderable2DInterface element = (Renderable2DInterface) namingEvent.getOldBinding().getObject();
        this.elements.remove(element);
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
        logger.warn("namingExceptionThrown", namingEvent.getException());
    }

    /**
     * Calculate the full extent based on the current positions of the objects.
     * @return the full extent of the animation.
     */
    public final synchronized Rectangle2D fullExtent()
    {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        Point3d p3dL = new Point3d();
        Point3d p3dU = new Point3d();
        try
        {
            for (Renderable2DInterface renderable : this.elements)
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
    public final synchronized void zoomAll()
    {
        this.extent = Renderable2DInterface.Util.computeVisibleExtent(fullExtent(), this.getSize());
        this.repaint();
    }

    /**
     * Set a class to be shown in the animation to true.
     * @param locatableClass the class for which the animation has to be shown.
     */
    public final void showClass(final Class<? extends Locatable> locatableClass)
    {
        this.visibilityMap.put(locatableClass, true);
        this.shownClasses.clear();
        this.hiddenClasses.clear();
        this.repaint();
    }

    /**
     * Set a class to be hidden in the animation to true.
     * @param locatableClass the class for which the animation has to be hidden.
     */
    public final void hideClass(final Class<? extends Locatable> locatableClass)
    {
        this.visibilityMap.put(locatableClass, false);
        this.shownClasses.clear();
        this.hiddenClasses.clear();
        this.repaint();
    }

    /**
     * Toggle a class to be displayed in the animation to its reverse value.
     * @param locatableClass the class for which a visible animation has to be turned off or vice versa.
     */
    public final void toggleClass(final Class<? extends Locatable> locatableClass)
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
    public final SortedSet<Renderable2DInterface> getElements()
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
     * @param dragLineEnabled the dragLineEnabled to set.
     */
    public final void setDragLineEnabled(final boolean dragLineEnabled)
    {
        this.dragLineEnabled = dragLineEnabled;
    }
}
