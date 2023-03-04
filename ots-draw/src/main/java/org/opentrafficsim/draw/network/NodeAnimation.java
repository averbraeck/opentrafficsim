package org.opentrafficsim.draw.network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.introspection.DelegateIntrospection;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
@SuppressWarnings("rawtypes")
public class NodeAnimation extends Renderable2D<NodeAnimation.ElevatedNode>
        implements Renderable2DInterface<NodeAnimation.ElevatedNode>, Serializable
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /** Ensure that node animations are slightly above lane surface. */
    public static final double ZOFFSET = 0.01;

    /**
     * @param node Node; n
     * @param simulator OtsSimulatorInterface; s
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException on communication failure
     */
    public NodeAnimation(final Node node, final OtsSimulatorInterface simulator) throws NamingException, RemoteException
    {
        super(new ElevatedNode(node), simulator);
        this.text = new Text(node, node.getId(), 0.0f, 3.0f, TextAlignment.CENTER, Color.BLACK, simulator,
                TextAnimation.RENDERWHEN10);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        graphics.draw(new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0));
        double direction = getSource().getLocation().getZ();
        if (!Double.isNaN(direction))
        {
            GeneralPath arrow = new GeneralPath(Path2D.WIND_EVEN_ODD, 3);
            arrow.moveTo(0.5, -0.5);
            arrow.lineTo(2, 0);
            arrow.lineTo(0.5, 0.5);
            graphics.draw(arrow);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        this.text.destroy(contextProvider);
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
            this.node = node;
            DirectedPoint p = node.getLocation();
            this.location = new DirectedPoint(p.x, p.y, p.z + ZOFFSET, p.getRotX(), p.getRotY(), p.getRotZ());
            this.bounds = node.getBounds();
        }

        /** {@inheritDoc} */
        @Override
        public DirectedPoint getLocation()
        {
            return this.location;
        }

        /**
         * @return node
         */
        public Node getNode()
        {
            return this.node;
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
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
         * @param simulator OtsSimulatorInterface; the simulator
         * @param scaleDependentRendering ScaleDependendentRendering; size limiter for text animation
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final OtsSimulatorInterface simulator,
                final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, 2.0f, 12.0f, 50f, simulator, scaleDependentRendering);
            setFlip(false);
            setRotate(false);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "NodeAnimation.Text []";
        }
    }

}
