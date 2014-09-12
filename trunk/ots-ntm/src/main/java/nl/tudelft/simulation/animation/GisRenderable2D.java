/*
 * @(#)GisRenderable2D.java Apr 30, 2014
 * 
 * Copyright (c) 2003, 2004 Delft University of Technology Jaffalaan 5, 
 * 2628 BX Delft, the Netherlands All rights reserved.
 * 
 * This software is proprietary information of Delft University of Technology
 * The code is published under the General Public License
 */
package nl.tudelft.simulation.animation;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.media.j3d.Bounds;
import javax.naming.Context;
import javax.naming.NamingException;

import nl.javel.gisbeans.map.MapInterface;
import nl.javel.gisbeans.map.mapfile.MapFileXMLParser;
import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.CartesianPoint;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.logger.Logger;
import nl.tudelft.simulation.naming.context.ContextUtil;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * This renderable draws CAD/GIS objects.
 * <p>
 * (c) copyright 2002-2005 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the
 * Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no
 * warranty.
 * 
 * @author <a href="http://www.peter-jacobs.com/index.htm">Peter Jacobs </a>
 * @version $Revision: 1.1 $ $Date: 2010/07/06 17:52:54 $
 * @since 1.5
 */
public class GisRenderable2D implements Renderable2DInterface, LocatableInterface
{
    /** the map to display */
    protected MapInterface map = null;

    /** the image cached image */
    protected BufferedImage image = null;

    /** the cached extent */
    protected Rectangle2D cachedExtent = new Rectangle2D.Double();

    /** the cached screenSize */
    protected Dimension screenSize = new Dimension();

    /**
     * the location of the image
     */
    protected DirectedPoint location = null;

    /**
     * the bounds of the image
     */
    protected Bounds bounds = null;

    /**
     * constructs a new GisRenderable2D
     * 
     * @param simulator the simulator.
     * @param mapFile the mapfile to use.
     * @throws NamingException 
     */
    public GisRenderable2D(final OTSSimulatorInterface simulator, final URL mapFile) throws NamingException
    {
        super();
        try
        {
            this.map = MapFileXMLParser.parseMapFile(mapFile);
            this.location = new DirectedPoint(new CartesianPoint(this.cachedExtent.getCenterX(), this.cachedExtent.getCenterY(), -Double.MIN_VALUE));
            this.bounds = new BoundingBox(this.cachedExtent.getWidth(), this.cachedExtent.getHeight(), 0.0);
            this.bind2Context(simulator);
        } catch (Exception exception)
        {
            Logger.warning(this, "<init>", exception);
        }
    }

    /**
     * binds a renderable2D to the context. The reason for specifying this in an independent method instead of adding
     * the code in the constructor is related to the RFE submitted by van Houten that in specific distributed context,
     * such binding must be overwritten.
     * 
     * @param simulator the simulator used for binding the object.
     * @throws NamingException 
     */
    protected void bind2Context(final OTSSimulatorInterface simulator) throws NamingException
    {
        Context context = ContextUtil.lookup(simulator.getContext(), "/animation/2D");
        ContextUtil.bind(context, this);
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface #paint(java.awt.Graphics2D,
     *      java.awt.geom.Rectangle2D, java.awt.Dimension, java.awt.image.ImageObserver)
     */
    public void paint(final Graphics2D graphics, final Rectangle2D extent, final Dimension screen, final ImageObserver observer)
    {
        try
        {
            // is the extent or the screensize still the same
            if (extent.equals(this.cachedExtent) && screen.equals(this.screenSize))
            {
                graphics.drawImage(this.image, 0, 0, null);
                return;
            }
            this.map.setExtent((Rectangle2D) extent.clone());
            this.map.getImage().setSize(screen);
            this.cacheImage();
            this.paint(graphics, extent, screen, observer);
        } catch (Exception exception)
        {
            Logger.warning(this, "paint", exception);
        }
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface#getSource()
     */
    public LocatableInterface getSource()
    {
        return this;
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.LocatableInterface#getBounds()
     */
    public Bounds getBounds()
    {
        return this.bounds;
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.LocatableInterface#getLocation()
     */
    public DirectedPoint getLocation()
    {
        return this.location;
    }

    /**
     * caches the GIS map by creating an image. This prevents continuous rendering.
     * 
     * @throws Exception on graphicsProblems and network connection failures.
     */
    private void cacheImage() throws Exception
    {
        this.image = new BufferedImage((int) this.map.getImage().getSize().getWidth(), (int) this.map.getImage().getSize().getHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D bg = this.image.createGraphics();
        this.map.drawMap(bg);
        bg.dispose();
        this.screenSize = (Dimension) this.map.getImage().getSize().clone();
        this.cachedExtent = this.map.getExtent();
        this.location = new DirectedPoint(new CartesianPoint(this.cachedExtent.getCenterX(), this.cachedExtent.getCenterY(), -Double.MIN_VALUE));
        this.bounds = new BoundingBox(this.cachedExtent.getWidth(), this.cachedExtent.getHeight(), 0.0);
    }

    /**
     * destroys an RenderableObject by unsubscribing it from the context.
     * 
     * @see nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface#destroy()
     */
    public void destroy()
    {
        try
        {
            nl.tudelft.simulation.naming.context.ContextUtil.unbindFromContext(this);
        } catch (Throwable throwable)
        {
            Logger.warning(this, "finalize", throwable);
        }
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface#contains(java.awt.geom.Point2D,
     *      java.awt.geom.Rectangle2D, java.awt.Dimension)
     */
    public boolean contains(final Point2D pointWorldCoordinates, final Rectangle2D extent, final Dimension screen)
    {
        return false;
    }
}