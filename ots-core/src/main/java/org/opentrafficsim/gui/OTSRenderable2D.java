package org.opentrafficsim.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d2.Shape;
import nl.tudelft.simulation.language.d3.BoundsUtil;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.logger.Logger;
import nl.tudelft.simulation.naming.context.ContextUtil;

import org.opentrafficsim.core.network.NetworkException;

/**
 * The Renderable2D provides an easy accessible renderable object.
 * <p>
 * (c) copyright 2002-2005 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no warranty.
 * @version $Revision: 1.1 $ $Date: 2010/08/10 11:37:20 $
 * @author <a href="https://www.linkedin.com/in/peterhmjacobs">Peter Jacobs </a>
 */
public abstract class OTSRenderable2D implements Renderable2DInterface
{
    /**
     * Storage of the boolean flags, to prevent each flag from taking 32 bits... The initial value is binary 1011 = 0B: rotate =
     * true, flip = false, scale = true, translate = true.
     */
    protected byte flags = 0x0B;

    /**
     * whether to rotate the renderable. Flag is 1000
     */
    private static final byte ROTATE_FLAG = 0x08;

    /**
     * whether to flip the renderable after rotating 180 degrees. Flag is 0100
     */
    private static final byte FLIP_FLAG = 0x04;

    /**
     * whether to scale the renderable when zooming in or out. Flag is 0010
     */
    private static final byte SCALE_FLAG = 0x02;

    /**
     * whether to translate the renderable when panning. Flag is 0001
     */
    private static final byte TRANSLATE_FLAG = 0x01;

    /**
     * the source of the renderable. TODO Make weak reference and destroy renderable when source ceases to exist
     */
    protected final LocatableInterface source;

    /**
     * the context for (un)binding.
     */
    protected Context context;

    /**
     * constructs a new Renderable2D.
     * @param source the source
     * @param simulator the simulator
     * @throws NamingException
     * @throws RemoteException
     */
    public OTSRenderable2D(final LocatableInterface source, final SimulatorInterface<?, ?, ?> simulator)
        throws NamingException, RemoteException
    {
        this.source = source;
        if (!(simulator instanceof AnimatorInterface))
        {
            // We are currently running without animation
            return;
        }
        this.bind2Context(simulator);
    }

    /**
     * binds a renderable2D to the context. The reason for specifying this in an independent method instead of adding the code
     * in the constructor is related to the RFE submitted by van Houten that in specific distributed context, such binding must
     * be overwritten.
     * @param simulator the simulator used for binding the object.
     * @throws NamingException
     * @throws RemoteException
     */
    protected void bind2Context(final SimulatorInterface<?, ?, ?> simulator) throws NamingException, RemoteException
    {
        this.context = ContextUtil.lookup(simulator.getReplication().getContext(), "/animation/2D");
        // ContextUtil.bind(this.context, this);
        this.context.bind(""+this.hashCode(), this);
        // System.err.println("bound: " + hashCode() + " for " + toString());
    }

    /**
     * @return Returns the flip.
     */
    public boolean isFlip()
    {
        return (this.flags & FLIP_FLAG) != 0;
    }

    /**
     * @param flip The flip to set.
     */
    public void setFlip(final boolean flip)
    {
        if (flip)
            this.flags |= FLIP_FLAG;
        else
            this.flags &= (~FLIP_FLAG);
    }

    /**
     * @return Returns the rotate.
     */
    public boolean isRotate()
    {
        return (this.flags & ROTATE_FLAG) != 0;
    }

    /**
     * @param rotate The rotate to set.
     */
    public void setRotate(final boolean rotate)
    {
        if (rotate)
            this.flags |= ROTATE_FLAG;
        else
            this.flags &= (~ROTATE_FLAG);
    }

    /**
     * @return Returns the scale.
     */
    public boolean isScale()
    {
        return (this.flags & SCALE_FLAG) != 0;
    }

    /**
     * @param scale The scale to set.
     */
    public void setScale(final boolean scale)
    {
        if (scale)
            this.flags |= SCALE_FLAG;
        else
            this.flags &= (~SCALE_FLAG);
    }

    /**
     * @return Returns the translate.
     */
    public boolean isTranslate()
    {
        return (this.flags & TRANSLATE_FLAG) != 0;
    }

    /**
     * @param translate The translate to set.
     */
    public void setTranslate(final boolean translate)
    {
        if (translate)
            this.flags |= TRANSLATE_FLAG;
        else
            this.flags &= (~TRANSLATE_FLAG);
    }

    /** {@inheritDoc} */
    @Override
    public LocatableInterface getSource()
    {
        return this.source;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void paint(final Graphics2D graphics, final Rectangle2D extent, final Dimension screen,
        final ImageObserver observer)
    {
        try
        {
            DirectedPoint location = null;
            Rectangle2D rectangle = null;
            try
            {
                location = this.source.getLocation();
                rectangle = BoundsUtil.getIntersect(location, this.source.getBounds(), location.z);
                if (location == null)
                    return;
            }
            catch (NullPointerException | RemoteException e)
            {
                // ignore -- source has been deleted in the meantime (race condition)
                return;
            }
            if (!Shape.overlaps(extent, rectangle) && isTranslate())
            {
                return;
            }
            Point2D screenCoordinates = Renderable2DInterface.Util.getScreenCoordinates(location.to2D(), extent, screen);
            // Let's transform
            if (isTranslate())
            {
                graphics.translate(screenCoordinates.getX(), screenCoordinates.getY());
            }
            double scaleFactor = Renderable2DInterface.Util.getScale(extent, screen);
            if (isScale())
            {
                graphics.scale(1.0 / scaleFactor, 1.0 / scaleFactor);
            }
            double angle = -location.getRotZ();
            if (isFlip() && angle > Math.PI)
            {
                angle = angle - Math.PI;
            }
            if (isRotate() && angle != 0.0)
            {
                graphics.rotate(angle);
            }
            // Now we paint
            this.paint(graphics, observer);
            // Let's untransform
            if (isRotate() && angle != 0.0)
            {
                graphics.rotate(-angle);
            }
            if (isScale())
            {
                graphics.scale(scaleFactor, scaleFactor);
            }
            if (isTranslate())
            {
                graphics.translate(-screenCoordinates.getX(), -screenCoordinates.getY());
            }
        }
        catch (Exception exception)
        {
            Logger.warning(this, "paint", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2D pointWorldCoordinates, final Rectangle2D extent, final Dimension screen)
    {
        try
        {
            Rectangle2D intersect =
                BoundsUtil.getIntersect(this.source.getLocation(), this.source.getBounds(), this.source.getLocation().z);
            if (intersect == null)
            {
                throw new NullPointerException(
                    "empty intersect!: location.z is not in bounds. This is probably due to a modeling error. See the javadoc off LocatableInterface.");
            }
            return intersect.contains(pointWorldCoordinates);
        }
        catch (RemoteException exception)
        {
            Logger.warning(this, "contains", exception);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() throws NamingException
    {
        try
        {
            //this.context.unbind(""+this.hashCode());
            ContextUtil.unbind(this.context, this);
            // System.err.println("unbound: " + hashCode() + " for " + toString());
            // String key = resolveKey(this, this.context, "/");
            // System.err.println("unbind " + key + " for " + toString());
            // this.context.unbind(key);
            // this.context.unbind(""+this.hashCode());
        }
        catch (Exception e)
        {
            System.err.println("could not destroy animation " + hashCode() + " for " + toString());
            // e.printStackTrace();
            // TODO find out why sometimes null pointer is thrown.
        }
    }

    /**
     * resolves the key under which an object is stored in the given context.
     * @param object the object which key to resolve.
     * @param context the context.
     * @param name the name of the parent.
     * @return the key
     * @throws NamingException on lookup failure
     */
    private static String resolveKey(final Object object, final Context context, final String name)
            throws NamingException
    {
        NamingEnumeration<Binding> list = context.listBindings(name);
        while (list.hasMore())
        {
            Binding binding = list.next();
            if (binding.getObject() instanceof Context)
            {
                String result = resolveKey(object, (Context) binding.getObject(), binding.getName());
                if (result != null)
                {
                    return result;
                }
            }
            else if (binding.getObject().equals(object))
            {
                String key = context.getNameInNamespace() + "/" + binding.getName();
                return key;
            }
        }
        return null;
    }

    /**
     * resolves the name of an object under which it is accessible in the initial context.
     * @param object the object
     * @return String
     * @throws NamingException whenever the object cannot be found
     */
    public static String resolveKey(final Object object) throws NamingException
    {
        String result = resolveKey(object, new InitialContext(), "");
        if (result == null)
        {
            throw new NamingException("could not resolve " + object.toString());
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        if (this != this.source)
        {
            return super.toString() + "-OF-" + this.source.toString();
        }
        return super.toString() + "-OF-" + super.toString();
    }

    /**
     * draws an animation on a worldcoordinates around [x,y=0,0]
     * @param graphics the graphics object
     * @param observer the observer
     * @throws RemoteException on network exception
     */
    public abstract void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException;
}
