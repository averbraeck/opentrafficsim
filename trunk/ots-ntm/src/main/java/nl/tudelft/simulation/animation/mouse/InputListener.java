/*
 * @(#) InputListener.java Mar 2, 2004 Copyright (c) 2002-2005 Delft University
 * of Technology Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved. This software is proprietary information of Delft University of
 * Technology 
 */
package nl.tudelft.simulation.animation.mouse;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.vecmath.Point4i;

import nl.tudelft.simulation.animation.AnimationPanel;
import nl.tudelft.simulation.animation.GridPanel;
import nl.tudelft.simulation.animation.actions.IntrospectionAction;
import nl.tudelft.simulation.animation.actions.PanDownAction;
import nl.tudelft.simulation.animation.actions.PanLeftAction;
import nl.tudelft.simulation.animation.actions.PanRightAction;
import nl.tudelft.simulation.animation.actions.PanUpAction;
import nl.tudelft.simulation.animation.actions.ZoomInAction;
import nl.tudelft.simulation.animation.actions.ZoomOutAction;
import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.introspection.gui.IntroSpectionDialog;
import nl.tudelft.simulation.logger.Logger;

/**
 * A InputListener <br>
 * (c) copyright 2002-2005 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the
 * Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no
 * warranty.
 * @version $Revision: 1.2 $ $Date: 2010/08/10 11:37:49 $
 * @author <a href="http://www.peter-jacobs.com">Peter Jacobs </a>
 */
public class InputListener implements MouseListener, MouseWheelListener, MouseMotionListener, KeyListener
{
    /** the panel to use */
    protected AnimationPanel panel;

    /** the mouseClicked point in screen coordinates */
    protected Point2D mouseClicked = null;

    /** the formatter */
    // private NumberFormat formatter = NumberFormat.getInstance();

    /**
     * constructs a new InputListener
     * @param application the application
     * @param panel the panel
     */
    public InputListener(final AnimationPanel panel)
    {
        super();
        this.panel = panel;
    }

    /**
     * @see java.awt.event.MouseListener #mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(final MouseEvent e)
    {
        this.panel.requestFocus();
        this.mouseClicked = e.getPoint();
        if (!e.isPopupTrigger())
        {
            Object selected = this.getSelectedObject(this.getSelectedObjects(e.getPoint()));
            if (selected != null)
            {
                new IntroSpectionDialog(selected);
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(final MouseEvent e)
    {
        this.panel.requestFocus();
        this.mouseClicked = e.getPoint();
        if (e.isPopupTrigger())
        {
            this.popup(e);
            return;
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(final MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            this.popup(e);
        }
        else
        {
            // Pan if either shift is down or the left mouse button is used.
            if ((e.isShiftDown()) || (e.getButton() == MouseEvent.BUTTON1))
            {
                this.pan(this.mouseClicked, e.getPoint());
                this.panel.repaint();
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(final MouseEvent e)
    {
        // Nothing to be done.
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(final MouseEvent e)
    {
        // Nothing to be done
    }

    /**
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    public void mouseWheelMoved(final MouseWheelEvent e)
    {
        // Use mouse wheel to zoom
        int amount = e.getUnitsToScroll();
        if (amount > 0)
        {
            /*- 
            Set the center of the map to the current position of the mouse when zooming in
            double scale = Renderable2DInterface.Util.getScale(this.panel.getExtent(), this.panel.getSize());
            Rectangle2D.Double extent = (Rectangle2D.Double) this.panel.getExtent();
            double dx = e.getX() - this.panel.getWidth() / 2;
            double dy = e.getY() + this.panel.getHeight() / 2;
            extent.setRect((extent.getMinX() + dx * scale), (extent.getMinY() + dy * scale), 
                extent.getWidth(), extent.getHeight()); 
             */
            this.panel.zoom(GridPanel.IN, 0.95 * amount);
        }
        else if (amount < 0)
        {
            this.panel.zoom(GridPanel.OUT, 0.95 * -amount);
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(final MouseEvent e)
    {
        if (e.isShiftDown())
        {
            this.setDragLine(e.getPoint());
        }
        this.panel.repaint();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(final MouseEvent mouseEvent)
    {
        /*-
        Point2D point = Renderable2DInterface.Util.getWorldCoordinates(mouseEvent.getPoint(), 
            this.panel.getExtent(), this.panel.getSize());
        String worldPoint = "null";
        if (point != null)
        {
            worldPoint = "world(x=" + this.formatter.format(point.getX()) + " ; y="
                + this.formatter.format(point.getY()) + ")";
        }
        String mousePoint = "screen(x=" + this.formatter.format(mouseEvent.getPoint().getX()) + " ; y=" 
            + this.formatter.format(mouseEvent.getPoint().getY()) + ")";
        this.panel.setToolTipText(worldPoint + "  " + mousePoint);
         */
    }

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(final KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_LEFT:
                new PanLeftAction(this.panel).actionPerformed(new ActionEvent(this, 0, "LEFT"));
                break;
            case KeyEvent.VK_RIGHT:
                new PanRightAction(this.panel).actionPerformed(new ActionEvent(this, 0, "RIGHT"));
                break;
            case KeyEvent.VK_UP:
                new PanUpAction(this.panel).actionPerformed(new ActionEvent(this, 0, "UP"));
                break;
            case KeyEvent.VK_DOWN:
                new PanDownAction(this.panel).actionPerformed(new ActionEvent(this, 0, "DOWN"));
                break;
            case KeyEvent.VK_MINUS:
                new ZoomOutAction(this.panel).actionPerformed(new ActionEvent(this, 0, "OUT"));
                break;
            case KeyEvent.VK_EQUALS:
                new ZoomInAction(this.panel).actionPerformed(new ActionEvent(this, 0, "IN"));
                break;
            default:
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(final KeyEvent e)
    {
        // nothing to be done
    }

    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(final KeyEvent e)
    {
        // nothing to be done
    }

    /**
     * What to do if the middle mouse button was released
     * @param mouseClickedPoint the point where the mouse was clicked
     * @param mouseReleasedPoint the point where the mouse was released
     */
    protected void pan(final Point2D mouseClickedPoint, final Point2D mouseReleasedPoint)
    {
        // Drag extend to new location
        double dx = mouseReleasedPoint.getX() - mouseClickedPoint.getX();
        double dy = mouseReleasedPoint.getY() - mouseClickedPoint.getY();
        double scale = Renderable2DInterface.Util.getScale(this.panel.getExtent(), this.panel.getSize());

        Rectangle2D.Double extent = (Rectangle2D.Double) this.panel.getExtent();
        extent.setRect((extent.getMinX() - dx * scale), (extent.getMinY() + dy * scale), extent.getWidth(),
                extent.getHeight());
    }

    /**
     * returns the list of selected objects at a certain mousePoint
     * @param mousePoint the mousePoint
     * @return the selected objects
     */
    protected List<LocatableInterface> getSelectedObjects(final Point2D mousePoint)
    {
        List<LocatableInterface> targets = new ArrayList<LocatableInterface>();
        try
        {
            Point2D point =
                    Renderable2DInterface.Util.getWorldCoordinates(mousePoint, this.panel.getExtent(),
                            this.panel.getSize());
            for (Renderable2DInterface renderable : this.panel.getElements())
            {
                if (renderable.contains(point, this.panel.getExtent(), this.panel.getSize()))
                {
                    targets.add(renderable.getSource());
                }
            }
        }
        catch (Exception exception)
        {
            Logger.warning(this, "getSelectedObjects", exception);
        }
        return targets;
    }

    /**
     * popsup on a mouseEvent
     * @param e the mouseEvent
     */
    protected void popup(final MouseEvent e)
    {
        List<LocatableInterface> targets = this.getSelectedObjects(e.getPoint());
        if (targets.size() > 0)
        {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add("Introspect");
            popupMenu.add(new JSeparator());
            for (Iterator<LocatableInterface> i = targets.iterator(); i.hasNext();)
            {
                popupMenu.add(new IntrospectionAction(i.next()));
            }
            popupMenu.show(this.panel, e.getX(), e.getY());
        }
    }

    /**
     * edits a selected Renderable2D
     * @param targets which are selected by the mouse.
     * @return the selected Object (e.g. the one with the highest zValue).
     */
    protected Object getSelectedObject(final List<LocatableInterface> targets)
    {
        Object selectedObject = null;
        try
        {
            double zValue = -Double.MAX_VALUE;
            for (LocatableInterface next : targets)
            {
                double z = next.getLocation().z;
                if (z > zValue)
                {
                    zValue = z;
                    selectedObject = next;
                }
            }
        }
        catch (RemoteException exception)
        {
            Logger.warning(this, "edit", exception);
        }
        return selectedObject;
    }

    /**
     * set the drag line: a line that shows where the user is dragging
     * @param mousePosition the position of the mouse pointer
     */
    private void setDragLine(final Point2D mousePosition)
    {
        if ((mousePosition != null) && (this.mouseClicked != null))
        {
            Point4i dragLine = this.panel.getDragLine();
            dragLine.w = (int) mousePosition.getX();
            dragLine.x = (int) mousePosition.getY();
            dragLine.y = (int) this.mouseClicked.getX();
            dragLine.z = (int) this.mouseClicked.getY();
            this.panel.setDragLineEnabled(true);
        }
    }
}