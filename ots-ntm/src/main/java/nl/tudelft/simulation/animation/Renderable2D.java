/*
 * @(#)TinyRenderable2D.java May 1, 2014
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
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.naming.Context;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.language.d2.Shape;
import nl.tudelft.simulation.language.d3.BoundsUtil;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.logger.Logger;
import nl.tudelft.simulation.naming.context.ContextUtil;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * <br>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * 
 * The MEDLABS project (Modeling Epidemic Disease with Large-scale Agent-Based Simulation) is aimed at providing policy
 * analysis tools to predict and help contain the spread of epidemics. It makes use of the DSOL simulation engine and
 * the agent-based modeling formalism. See for project information <a href="http://www.simulation.tudelft.nl/">
 * www.simulation.tudelft.nl</a>. The project is a co-operation between TU Delft, Systems Engineering and Simulation
 * Department (Netherlands) and NUDT, Simulation Engineering Department (China).
 * 
 * This software is licensed under the BSD license. See license.txt in the main project.
 * 
 * @version May 1, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/mzhang">Mingxin Zhang </a>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck </a>
 */
public abstract class Renderable2D implements Renderable2DInterface
{
    /** the locatable source */
    private final LocatableInterface source;
    
    /*-
     * Scaling is optimized for Beijing. Adapt for other cities!
     * LAT: 0.01 degree at lon 116 = 1.112 km. 
     * LON: 0.01 degree at lat 40 = 0.852 km.
     */
    
    /** scaling in x-direction */
    private static final double SCALEX = 111319.24;
    
    /** scaling in y-direction */
    private static final double SCALEY = 85897.09;
    
    /** in this case take average between x and y to get undeformed icons */
    private static final double SCALE = 0.5 * (SCALEX + SCALEY);

    /**
     * @param source the locatable object (implements LocatableInterface)
     * @param simulator the simulator used for binding the object.
     * @throws NamingException 
     */
    public Renderable2D(final LocatableInterface source, final OTSSimulatorInterface simulator) throws NamingException
    {
        this.source = source;
        this.bind2Context(simulator);
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
     * @see nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface #paint(Graphics2D, Rectangle2D,
     *      Dimension,ImageObserver)
     */
    public synchronized void paint(final Graphics2D graphics, final Rectangle2D extent, final Dimension screen, final ImageObserver observer)
    {
        try
        {
            DirectedPoint location = this.source.getLocation();
            Bounds bounds = this.source.getBounds();
            if (bounds == null) // work purely on the basis of a point
            {
                if (!extent.contains(location.to2D()))
                    return;
            } else
            {
                Rectangle2D rectangle = BoundsUtil.getIntersect(this.source.getLocation(), this.source.getBounds(), location.z);
                if (!Shape.overlaps(extent, rectangle))
                    return;
            }
            Point2D screenCoordinates = Renderable2DInterface.Util.getScreenCoordinates(location.to2D(), extent, screen);
            double scale = 1.0 / (SCALE * Renderable2DInterface.Util.getScale(extent, screen));

            // Let's translate
            graphics.translate(screenCoordinates.getX(), screenCoordinates.getY());

            // and scale
            graphics.scale(scale, scale);
            
            // Now we paint
            this.paint(graphics, observer);

            // and unscale
            graphics.scale(1.0 / scale, 1.0 / scale);
            
            // and untranslate
            graphics.translate(-screenCoordinates.getX(), -screenCoordinates.getY());

        } catch (Exception exception)
        {
            Logger.warning(this, "paint", exception);
        }
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface #contains(java.awt.geom.Point2D,
     *      java.awt.geom.Rectangle2D, java.awt.Dimension)
     */
    public boolean contains(final Point2D pointWorldCoordinates, final Rectangle2D extent, final Dimension screen)
    {
        try
        {
            if (this.source.getBounds() == null)
            {
                return pointWorldCoordinates.distance(this.source.getLocation().to2D()) < 0.001;
                // TODO for introspection, based on pixels?
            }

            Rectangle2D intersect = BoundsUtil.getIntersect(this.source.getLocation(), this.source.getBounds(), this.source.getLocation().z);
            if (intersect == null)
            {
                throw new NullPointerException(
                        "empty intersect!: location.z is not in bounds. This is probably due to a modeling error. See the javadoc of LocatableInterface.");
            }
            return intersect.contains(pointWorldCoordinates);
        } catch (Exception exception)
        {
            Logger.warning(this, "contains", exception);
            return false;
        }
    }

    /**
     * destroys an RenderableObject by unsubscribing it from the context.
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
     * draws an animation on world coordinates around [x,y=0,0]
     * 
     * @param graphics the graphics object
     * @param observer the observer
     * @throws RemoteException on network exception
     */
    public abstract void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException;

    /**
     * @see nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface#getSource()
     */
    @Override
    public LocatableInterface getSource()
    {
        return this.source;
    }
}