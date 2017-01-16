package org.opentrafficsim.road.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.animation.ClonableRenderable2DInterface;
import org.opentrafficsim.core.animation.TextAlignment;
import org.opentrafficsim.core.animation.TextAnimation;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.animation.PaintPolygons;
import org.opentrafficsim.road.network.animation.LaneAnimation.Text;
import org.opentrafficsim.road.network.lane.object.LaneBlock;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Draw a road block.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBlockAnimation extends Renderable2D implements ClonableRenderable2DInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The half width left and right of the center line that is used to draw the block. */
    private final double halfWidth;

    /** The fill color of the block. */
    private Color fillColor;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param source the CSEBlock to draw
     * @param simulator the simulator to schedule on
     * @param fillColor the fill color
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public LaneBlockAnimation(final LaneBlock source, final OTSSimulatorInterface simulator, final Color fillColor)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        setFillColor(fillColor);
        this.halfWidth = 0.45 * source.getLane().getWidth(source.getLongitudinalPosition()).getSI();
        this.text = new Text(source, source.getLane().getParentLink().getId() + "." + source.getLane().getId() + source.getId(),
                0.0f, (float) this.halfWidth + 0.2f, TextAlignment.CENTER, Color.BLACK, simulator);
    }
    
    /**
     * @return text.
     */
    public final Text getText()
    {
        return this.text;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(this.fillColor);
        Rectangle2D rectangle = new Rectangle2D.Double(-0.25, -this.halfWidth, 0.5, 2 * this.halfWidth);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy() throws NamingException
    {
        super.destroy();
        this.text.destroy();
    }

    /**
     * @return fillColor
     */
    public final Color getFillColor()
    {
        return this.fillColor;
    }

    /**
     * @param fillColor set fillColor
     */
    public final void setFillColor(final Color fillColor)
    {
        this.fillColor = fillColor;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
            throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new LaneBlockAnimation((LaneBlock) newSource, newSimulator, this.fillColor);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CSEBlockAnimation [getSource()=" + this.getSource() + "]";
    }

    /**
     * Text animation for the LaneBlock. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Dec 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20170116L;

        /**
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param simulator the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final OTSSimulatorInterface simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, simulator);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public TextAnimation clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
                throws RemoteException, NamingException
        {
            return new Text(newSource, getText(), getDx(), getDy(), getTextAlignment(), getColor(), newSimulator);
        }
    }

}
