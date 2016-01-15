package org.opentrafficsim.road.network.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.animation.PaintPolygons;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneAnimation extends Renderable2D
{
    /** color of the lane. */
    private final Color color;

    /** whether to draw the center line or not. */
    private final boolean drawCenterLine;

    /**
     * @param source s
     * @param simulator s
     * @param color color of the lane.
     * @param drawCenterLine whether to draw the center line or not
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public LaneAnimation(final Lane source, final OTSSimulatorInterface simulator, final Color color,
        final boolean drawCenterLine) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = color;
        this.drawCenterLine = drawCenterLine;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        Lane lane = (Lane) getSource();
        PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour(), true);

        if (this.drawCenterLine)
        {
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(0.25f));
            PaintPolygons.paintMultiPolygon(graphics, Color.RED, lane.getLocation(), lane.getParentLink()
                .getDesignLine(), false);
            graphics.setStroke(oldStroke);
        }
    }
}
