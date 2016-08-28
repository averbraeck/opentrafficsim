package org.opentrafficsim.road.gtu.lane.object.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.animation.PaintPolygons;
import org.opentrafficsim.road.gtu.lane.object.CSEBlock;

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
public class CSEBlockAnimation extends Renderable2D implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The fill color of the block. */
    private Color fillColor;

    /** The point (0,0,0). */
    private static final DirectedPoint POINT_000 = new DirectedPoint();

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param source the CSEBlock to draw
     * @param simulator the simulator to schedule on
     * @param fillColor the fill color
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public CSEBlockAnimation(final CSEBlock source, final OTSSimulatorInterface simulator, final Color fillColor)
        throws NamingException, RemoteException
    {
        super(source, simulator);
        setFillColor(fillColor);
        setTranslate(false);
        setRotate(false);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(this.fillColor);
        PaintPolygons.paintMultiPolygon(graphics, this.fillColor, POINT_000, ((CSEBlock) this.source).getGeometry(),
            true);
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
    public final void setFillColor(Color fillColor)
    {
        this.fillColor = fillColor;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CSEBlockAnimation [getSource()=" + this.getSource() + "]";
    }

}
