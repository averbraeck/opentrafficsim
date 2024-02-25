package org.opentrafficsim.draw.network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djutils.base.Identifiable;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;
import org.opentrafficsim.draw.road.OtsRenderable;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws NodeData.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class NodeAnimation extends OtsRenderable<NodeData>
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /**
     * @param node NodeData; node data.
     * @param contextualized Contextualized; context provider
     * @throws NamingException when animation context cannot be found.
     * @throws RemoteException on communication failure
     */
    public NodeAnimation(final NodeData node, final Contextualized contextualized) throws NamingException, RemoteException
    {
        super(node, contextualized);
        this.text = new Text(node, node::getId, 0.0f, 3.0f, TextAlignment.CENTER, Color.BLACK, contextualized,
                TextAnimation.RENDERWHEN10);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        graphics.draw(new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0));
        double direction = getSource().getLocation().getDirZ();
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

    /**
     * Text animation for the Node. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation<NodeData>
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source NodeData; the object for which the text is displayed
         * @param text Supplier&lt;String&gr;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider
         * @param scaleDependentRendering ScaleDependendentRendering; size limiter for text animation
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public Text(final NodeData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized,
                final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, 2.0f, 12.0f, 50f, contextualized, scaleDependentRendering);
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

    /**
     * NodeData provides the information required to draw a node.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface NodeData extends OtsLocatable, Identifiable
    {
        /** {@inheritDoc} */
        @Override
        public OrientedPoint2d getLocation();

        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.NODE.getZ();
        }
    }

}
