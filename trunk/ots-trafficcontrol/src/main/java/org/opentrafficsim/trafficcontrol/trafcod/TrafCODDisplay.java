package org.opentrafficsim.trafficcontrol.trafcod;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.swing.JPanel;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.NonDirectionalOccupancySensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

/**
 * Display the current state of a TrafCOD machine.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrafCODDisplay extends JPanel
{
    /** */
    private static final long serialVersionUID = 20161115L;

    /** Background image. */
    private final BufferedImage image;

    /** The set of objects drawn on the image. */
    private Set<TrafCODObject> trafCODObjects = new HashSet<>();

    /**
     * Construct a new TrafCODDisplay.
     * @param image BufferedImage; the background image
     */
    public TrafCODDisplay(final BufferedImage image)
    {
        this.image = image;
        super.setPreferredSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
    }

    @Override
    protected void paintComponent(final Graphics g)
    {
        super.paintComponent(g); 
        g.drawImage(this.image, 0, 0, null);
        for (TrafCODObject to : this.trafCODObjects)
        {
            to.draw((Graphics2D) g);
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

}

/**
 * Interface for objects that can draw themselves onto a Graphics2D.
 */
interface TrafCODObject
{
    /**
     * Draw yourself at the indicated location/
     * @param g2 Graphics2D; the graphics context
     */
    void draw(Graphics2D g2);

}

/**
 * Draws a detector.
 */
class DetectorImage implements TrafCODObject, EventListenerInterface
{
    /** The TrafCOD display. */
    private final TrafCODDisplay display;

    /** X-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int x;

    /** Y-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int y;
    
    /** Fill color (reflects with the occupancy state of the detector). */
    private Color fillColor = Color.WHITE;

    /** Size of the box that is drawn. */
    private static final int BOX_SIZE = 13;
    
    /** Correction to make the result match that of the C++Builder version. */
    private static final int xOffset = 5;
    
    /** Correction to make the result match that of the C++Builder version. */
    private static final int yOffset = 5;

    /**
     * Construct a new DetectorImage.
     * @param display TrafCODDisplay; the TrafCOD display on which this detector image will be rendered
     * @param center Point2D; the center location of the detector image on the TrafCOD display
     */
    public DetectorImage(final TrafCODDisplay display, Point2D center)
    {
        this.display = display;
        this.x = (int) center.getX();
        this.y = (int) center.getY();
        display.addTrafCODObject(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g2)
    {
        g2.setColor(this.fillColor);
        g2.fillRect(xOffset + this.x - BOX_SIZE / 2, yOffset + this.y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE);
        g2.setColor(Color.BLACK);
        g2.drawRect(xOffset + this.x - BOX_SIZE / 2, yOffset + this.y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(EventInterface event) throws RemoteException
    {
        if (event.getType().equals(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT))
        {
            this.fillColor = Color.black;
        }
        else if (event.getType().equals(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT))
        {
            this.fillColor = Color.white;
        }
        this.display.repaint();
    }
    
}

/**
 * Only implements setTrafficLightColor. All other methods are dummies.
 */
class TrafficLightImage implements TrafficLight, TrafCODObject
{
    /** The TrafCOD display. */
    private final TrafCODDisplay display;

    /** X-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int x;

    /** Y-coordinate on the TrafCOD display image where this traffic light must be drawn. */
    private final int y;

    /** The current color. */
    private TrafficLightColor color = TrafficLightColor.BLACK;

    /**
     * Create a traffic light image.
     * @param display TrafCODDisplay; the TrafCOD display on which this traffic light image will be rendered
     * @param center Point2D; coordinates in the image where this traffic light is centered on
     */
    public TrafficLightImage(final TrafCODDisplay display, Point2D center)
    {
        this.display = display;
        this.x = (int) center.getX();
        this.y = (int) center.getY();
        display.addTrafCODObject(this);
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
    public OTSLine3D getGeometry()
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
    public boolean addListener(EventListenerInterface listener, EventType eventType) throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(EventListenerInterface listener, EventType eventType, boolean weak) throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(EventListenerInterface listener, EventType eventType, short position) throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addListener(EventListenerInterface listener, EventType eventType, short position, boolean weak)
            throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeListener(EventListenerInterface listener, EventType eventType) throws RemoteException
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
    public void setTrafficLightColor(TrafficLightColor trafficLightColor)
    {
        this.color = trafficLightColor;
        this.display.repaint();
        System.out.println("Setting light color to " + trafficLightColor);
    }

    /** Diameter of a traffic light disk in pixels. */
    private static final int DISK_SIZE = 11;

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g2)
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
        g2.fillOval(this.x - DISK_SIZE / 2, this.y - DISK_SIZE / 2, DISK_SIZE, DISK_SIZE);
        // System.out.println("Drawn disk in color " + lightColor);
    }

}
