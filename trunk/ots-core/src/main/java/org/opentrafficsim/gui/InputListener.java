package org.opentrafficsim.gui;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.animation.D2.GridPanel;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.animation.D2.actions.IntrospectionAction;
import nl.tudelft.simulation.dsol.animation.D2.actions.PanDownAction;
import nl.tudelft.simulation.dsol.animation.D2.actions.PanLeftAction;
import nl.tudelft.simulation.dsol.animation.D2.actions.PanRightAction;
import nl.tudelft.simulation.dsol.animation.D2.actions.PanUpAction;
import nl.tudelft.simulation.introspection.gui.IntroSpectionDialog;

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
    /** the panel to use. */
    protected ShowHideAnimationPanel panel;

    /** the mouseClicked point in screen coordinates. */
    protected Point2D mouseClicked = null;
    
    /** the logger. */
    private static Logger logger = LogManager.getLogger(InputListener.class);

    /**
     * constructs a new InputListener.
     * @param panel the panel
     */
    public InputListener(final ShowHideAnimationPanel panel)
    {
        super();
        this.panel = panel;
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent e)
    {
        // Nothing to be done.
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(final MouseEvent e)
    {
        // Nothing to be done
    }

    /** {@inheritDoc} */
    @Override
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
            this.panel.zoom(GridPanel.ZOOMFACTOR, e.getX(), e.getY());
        }
        else if (amount < 0)
        {
            this.panel.zoom(1.0 / GridPanel.ZOOMFACTOR, e.getX(), e.getY());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseDragged(final MouseEvent e)
    {
        if (e.isShiftDown())
        {
            this.setDragLine(e.getPoint());
        }
        this.panel.repaint();
    }

    /** {@inheritDoc} */
    @Override
    public void mouseMoved(final MouseEvent mouseEvent)
    {
        Point2D point = Renderable2DInterface.Util.getWorldCoordinates(mouseEvent.getPoint(), 
            this.panel.getExtent(), this.panel.getSize());
        this.panel.setWorldCoordinate(point);
        this.panel.displayWorldCoordinateToolTip();
    }

    /** {@inheritDoc} */
    @Override
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
                this.panel.zoom(GridPanel.ZOOMFACTOR, this.panel.getWidth() / 2, this.panel.getHeight() / 2);
                break;
            case KeyEvent.VK_EQUALS:
                this.panel.zoom(1.0 / GridPanel.ZOOMFACTOR, this.panel.getWidth() / 2, this.panel.getHeight() / 2);
                break;
            default:
        }
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased(final KeyEvent e)
    {
        // nothing to be done
    }

    /** {@inheritDoc} */
    @Override
    public void keyTyped(final KeyEvent e)
    {
        // nothing to be done
    }

    /**
     * What to do if the middle mouse button was released.
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
     * returns the list of selected objects at a certain mousePoint.
     * @param mousePoint the mousePoint
     * @return the selected objects
     */
    protected List<Locatable> getSelectedObjects(final Point2D mousePoint)
    {
        List<Locatable> targets = new ArrayList<Locatable>();
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
            logger.warn("getSelectedObjects", exception);
        }
        return targets;
    }

    /**
     * popsup on a mouseEvent.
     * @param e the mouseEvent
     */
    protected void popup(final MouseEvent e)
    {
        List<Locatable> targets = this.getSelectedObjects(e.getPoint());
        if (targets.size() > 0)
        {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add("Introspect");
            popupMenu.add(new JSeparator());
            for (Iterator<Locatable> i = targets.iterator(); i.hasNext();)
            {
                popupMenu.add(new IntrospectionAction(i.next()));
            }
            popupMenu.show(this.panel, e.getX(), e.getY());
        }
    }

    /**
     * edits a selected Renderable2D.
     * @param targets which are selected by the mouse.
     * @return the selected Object (e.g. the one with the highest zValue).
     */
    protected Object getSelectedObject(final List<Locatable> targets)
    {
        Object selectedObject = null;
        try
        {
            double zValue = -Double.MAX_VALUE;
            for (Locatable next : targets)
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
            logger.warn("edit", exception);
        }
        return selectedObject;
    }

    /**
     * set the drag line: a line that shows where the user is dragging.
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