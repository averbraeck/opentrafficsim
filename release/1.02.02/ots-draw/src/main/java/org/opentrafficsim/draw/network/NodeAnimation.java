package org.opentrafficsim.draw.network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.draw.core.ClonableRenderable2DInterface;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.draw.core.TextAnimation.ScaleDependentRendering;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.introspection.DelegateIntrospection;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-10-11 22:54:04 +0200 (Thu, 11 Oct 2018) $, @version $Revision: 4696 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
@SuppressWarnings("rawtypes")
public class NodeAnimation extends Renderable2D implements ClonableRenderable2DInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /** Ensure that node animations are slightly above lane surface. */
    public static final double ZOFFSET = 0.01;

    /**
     * @param node Node; n
     * @param simulator SimulatorInterface.TimeDoubleUnit; s
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException on communication failure
     */
    @SuppressWarnings("unchecked")
    public NodeAnimation(final Node node, final SimulatorInterface.TimeDoubleUnit simulator)
            throws NamingException, RemoteException
    {
        super(new ElevatedNode(node), simulator);
        // Figure out the relevance of this node
        ScaleDependentRendering sizeLimiter = TextAnimation.RENDERWHEN1;
        for (Link link : node.getLinks())
        {
            if (link.getLinkType().getId().equals(LinkType.DEFAULTS.FREEWAY.getId()))
            {
                sizeLimiter = TextAnimation.RENDERWHEN10;
            }
        }
        this.text = new Text(node, node.getId(), 0.0f, 3.0f, TextAlignment.CENTER, Color.BLACK, simulator, sizeLimiter);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.BLACK);
        graphics.draw(new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0));
        try
        {
            double direction = getSource().getLocation().getZ();
            if (!Double.isNaN(direction))
            {
                graphics.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                GeneralPath arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
                arrow.moveTo(0.5,  -0.5);
                arrow.lineTo(2,  0);
                arrow.lineTo(0.5,  0.5);
                graphics.draw(arrow);
            }
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy() throws NamingException
    {
        super.destroy();
        this.text.destroy();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface clone(final Locatable newSource, final SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object and ElevatedNode
        return new NodeAnimation((Node) newSource, newSimulator);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "NodeAnimation [node=" + super.getSource() + "]";
    }

    /** Class for elevating the node for animation purposes. */
    public static class ElevatedNode implements Locatable, DelegateIntrospection
    {
        /** the node for introspection. */
        private final Node node;

        /** the location of the node to which the animation belongs. */
        private DirectedPoint location;

        /** the bounds of the node to which the animation belongs. */
        private Bounds bounds;

        /**
         * @param node Node; the node to which the animation belongs
         */
        public ElevatedNode(final Node node)
        {
            super();
            this.node = node;
            try
            {
                DirectedPoint p = node.getLocation();
                this.location = new DirectedPoint(p.x, p.y, p.z + ZOFFSET, p.getRotX(), p.getRotY(), p.getRotZ());
                this.bounds = node.getBounds();
            }
            catch (RemoteException exception)
            {
                CategoryLogger.always().error(exception, "Could not construct elevated node for animation");
                this.location = new DirectedPoint();
                this.bounds = new BoundingSphere();
            }
        }

        /** {@inheritDoc} */
        @Override
        public DirectedPoint getLocation() throws RemoteException
        {
            return this.location;
        }

        /** {@inheritDoc} */
        @Override
        public Bounds getBounds() throws RemoteException
        {
            return this.bounds;
        }

        /** {@inheritDoc} */
        @Override
        public Object getParentIntrospectionObject()
        {
            return this.node;
        }

    }

    /**
     * Text animation for the Node. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2018-10-11 22:54:04 +0200 (Thu, 11 Oct 2018) $, @version $Revision: 4696 $, by $Author: averbraeck $,
     * initial version Dec 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source Locatable; the object for which the text is displayed
         * @param text String; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator
         * @param scaleDependentRendering ScaleDependendentRendering; size limiter for text animation
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final SimulatorInterface.TimeDoubleUnit simulator,
                final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, 2.0f, 12.0f, 50f, simulator, scaleDependentRendering);
            setFlip(false);
            setRotate(false);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public TextAnimation clone(final Locatable newSource, final SimulatorInterface.TimeDoubleUnit newSimulator)
                throws RemoteException, NamingException
        {
            return new Text(newSource, getText(), getDx(), getDy(), getTextAlignment(), getColor(), newSimulator,
                    getScaleDependentRendering());
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "NodeAnimation.Text []";
        }
    }

}
