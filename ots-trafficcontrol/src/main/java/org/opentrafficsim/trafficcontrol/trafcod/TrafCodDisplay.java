package org.opentrafficsim.trafficcontrol.trafcod;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventTypeInterface;
import org.djutils.event.ref.ReferenceType;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.NonDirectionalOccupancySensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

/**
 * Display the current state of a TrafCOD machine.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TrafCodDisplay extends JPanel implements MouseMotionListener, MouseListener
{
    /** */
    private static final long serialVersionUID = 20161115L;

    /** Background image. */
    private final BufferedImage image;

    /** The set of objects drawn on the image. */
    private Set<TrafCODObject> trafCODObjects = new LinkedHashSet<>();

    /** Store the tool tip delay so we can restore it when the mouse exits this TrafCODDisplay. */
    private final int defaultInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();

    /**
     * Construct a new TrafCODDisplay.
     * @param image BufferedImage; the background image. This constructor does <b>not</b> make a deep copy of the image.
     *            Modifications of the image after calling this constructor might have <i>interesting</i> consequences, but
     *            should not result in crashes.
     */
    public TrafCodDisplay(final BufferedImage image)
    {
        this.image = image;
        super.setPreferredSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
        addMouseMotionListener(this);
    }

    /**
     * Look up a DetectorImage.
     * @param id String; id of the DetectorImage
     * @return DetectorImage; the detector image with matching id or null.
     */
    public DetectorImage getDetectorImage(final String id)
    {
        for (TrafCODObject tco : this.trafCODObjects)
        {
            if (tco instanceof DetectorImage && ((DetectorImage) tco).getId().equals(id))
            {
                return (DetectorImage) tco;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected void paintComponent(final Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(this.image, 0, 0, null);
        for (TrafCODObject tco : this.trafCODObjects)
        {
            tco.draw((Graphics2D) g);
        }
    }

    /**
     * Add one TrafCODObject to this TrafCODDisplay.
     * @param trafCODObject TrafCODObject; the TrafCOD object that must be added
     */
    void addTrafCODObject(final TrafCODObject trafCODObject)
    {
        this.trafCODObjects.add(trafCODObject);
    }

    /** {@inheritDoc} */
    @Override
    public void mouseDragged(final MouseEvent e)
    {
        mouseMoved(e); // Do the same as in the mouse move event
    }

    /** {@inheritDoc} */
    @Override
    public void mouseMoved(final MouseEvent e)
    {
        String toolTipText = null;
        for (TrafCODObject tco : this.trafCODObjects)
        {
            toolTipText = tco.toolTipHit(e.getX(), e.getY());
            if (null != toolTipText)
            {
                break;
            }
        }
        // System.out.println("Setting tool tip text to " + toolTipText);
        setToolTipText(toolTipText);
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(final MouseEvent e)
    {
        // Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(final MouseEvent e)
    {
        // Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent e)
    {
        ToolTipManager.sharedInstance().setInitialDelay(0);
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(final MouseEvent e)
    {
        ToolTipManager.sharedInstance().setInitialDelay(this.defaultInitialDelay);
    }

}

/**
 * Interface for objects that can draw themselves onto a Graphics2D and may want to show their own tool tip text when the mouse
 * hits them.
 */
interface TrafCODObject
{
    /**
     * Draw yourself at the indicated location.
     * @param g2 Graphics2D; the graphics context
     */
    void draw(Graphics2D g2);

    /**
     * Check if the given coordinates hit the TrafCODObject. If it does return a String to be used as a tool tip text. If the
     * coordinates do not hit this TrafCODObject return null.
     * @param testX int; the x-coordinate
     * @param testY int; the y-coordinate
     * @return String; the tool tip text or null if the coordinates do not hit the TrafCodObject
     */
    String toolTipHit(int testX, int testY);

}

/**
 * Draws a detector.
 */
class DetectorImage implements TrafCODObject, EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200313L;

    /** The TrafCOD display. */
    private final TrafCodDisplay display;

    /** X-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int x;

    /** Y-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int y;

    /** Tool tip text for this detector image. */
    private final String description;

    /** String used to match this detector with the TrafCOD detector input. */
    private final String id;

    /** Fill color (used to indicate the occupancy state of the detector). */
    private Color fillColor = Color.WHITE;

    /** Size of the box that is drawn. */
    private static final int BOX_SIZE = 13;

    /** Correction to make the result match that of the C++Builder version. */
    private static final int X_OFFSET = 5;

    /** Correction to make the result match that of the C++Builder version. */
    private static final int Y_OFFSET = 5;

    /**
     * Construct a new DetectorImage.
     * @param display TrafCODDisplay; the TrafCOD display on which this detector image will be rendered
     * @param center Point2D; the center location of the detector image on the TrafCOD display
     * @param id String; id used to match this detector with the TrafCOD detector input
     * @param description String; name of the detector (displayed as tool tip text)
     */
    DetectorImage(final TrafCodDisplay display, final Point2D center, final String id, final String description)
    {
        this.display = display;
        this.x = (int) center.getX();
        this.y = (int) center.getY();
        this.id = id;
        this.description = description;
        display.addTrafCODObject(this);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(final Graphics2D g2)
    {
        g2.setColor(this.fillColor);
        g2.fillRect(X_OFFSET + this.x - BOX_SIZE / 2, Y_OFFSET + this.y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE);
        g2.setColor(Color.BLACK);
        g2.drawRect(X_OFFSET + this.x - BOX_SIZE / 2, Y_OFFSET + this.y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT))
        {
            this.fillColor = Color.BLUE;
        }
        else if (event.getType().equals(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT))
        {
            this.fillColor = Color.WHITE;
        }
        this.display.repaint();
    }

    /** {@inheritDoc} */
    @Override
    public String toolTipHit(final int testX, final int testY)
    {
        if (testX < X_OFFSET + this.x - BOX_SIZE / 2 || testX >= X_OFFSET + this.x + BOX_SIZE / 2
                || testY < Y_OFFSET - BOX_SIZE / 2 + this.y || testY >= Y_OFFSET + this.y + BOX_SIZE / 2)
        {
            return null;
        }
        return this.description;
    }

    /**
     * Retrieve the id of this DetectorImage.
     * @return String; the id of this DetectorImage
     */
    public String getId()
    {
        return this.id;
    }

}

/**
 * Draws a traffic light. <br>
 * The implementation of TrafficLight only implements setTrafficLightColor. All other methods are dummies.
 */
class TrafficLightImage implements TrafficLight, TrafCODObject
{
    /** ... */
    private static final long serialVersionUID = 20200313L;

    /** The TrafCOD display. */
    private final TrafCodDisplay display;

    /** X-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int x;

    /** Y-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int y;

    /** Tool tip text for this traffic light image. */
    private final String description;

    /** The current color. */
    private TrafficLightColor color = TrafficLightColor.BLACK;

    /**
     * Create a traffic light image.
     * @param display TrafCODDisplay; the TrafCOD display on which this traffic light image will be rendered
     * @param center Point2D; coordinates in the image where this traffic light is centered on
     * @param description String; tool tip text for the new traffic light image
     */
    TrafficLightImage(final TrafCodDisplay display, final Point2D center, final String description)
    {
        this.display = display;
        this.x = (int) center.getX();
        this.y = (int) center.getY();
        this.description = description;
        display.addTrafCODObject(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toolTipHit(final int testX, final int testY)
    {
        if (testX < this.x - DISC_SIZE / 2 || testX >= this.x + DISC_SIZE / 2 || testY < this.y - DISC_SIZE / 2
                || testY >= this.y + DISC_SIZE / 2)
        {
            return null;
        }
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Lane getLane()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLongitudinalPosition()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3D getGeometry()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Length getHeight()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getFullId()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId() throws RemoteException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType,
            final ReferenceType referenceType) throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType, final int position)
            throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType, final int position,
            final ReferenceType referenceType) throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType) throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasListeners() throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int numberOfListeners(final EventTypeInterface eventType) throws RemoteException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Set<EventTypeInterface> getEventTypesWithListeners() throws RemoteException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeListener(final EventListenerInterface listener, final EventTypeInterface eventType)
            throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public TrafficLightColor getTrafficLightColor()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setTrafficLightColor(final TrafficLightColor trafficLightColor)
    {
        this.color = trafficLightColor;
        this.display.repaint();
    }

    /** Diameter of a traffic light disk in pixels. */
    private static final int DISC_SIZE = 11;

    /** {@inheritDoc} */
    @Override
    public void draw(final Graphics2D g2)
    {
        Color lightColor;
        switch (this.color)
        {
            case BLACK:
                lightColor = Color.BLACK;
                break;

            case GREEN:
                lightColor = Color.green;
                break;

            case YELLOW:
                lightColor = Color.YELLOW;
                break;

            case RED:
                lightColor = Color.RED;
                break;

            default:
                System.err.println("Unhandled TrafficLightColor: " + this.color);
                return;
        }
        g2.setColor(lightColor);
        g2.fillOval(this.x - DISC_SIZE / 2, this.y - DISC_SIZE / 2, DISC_SIZE, DISC_SIZE);
        // System.out.println("Drawn disk in color " + lightColor);
    }

}
