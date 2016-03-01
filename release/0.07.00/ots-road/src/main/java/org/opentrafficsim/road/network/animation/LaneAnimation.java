package org.opentrafficsim.road.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.animation.PaintLine;
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
        if (this.color != null)
        {
            PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour(), true);
        }

        if (this.drawCenterLine)
        {
            PaintLine.paintLine(graphics, Color.RED, 0.25, lane.getLocation(), lane.getCenterLine());
            Shape startCircle =
                new Ellipse2D.Double(lane.getCenterLine().getFirst().x - lane.getLocation().x - 0.25, -lane
                    .getCenterLine().getFirst().y + lane.getLocation().y - 0.25, 0.5, 0.5);
            Shape endCircle =
                new Ellipse2D.Double(lane.getCenterLine().getLast().x - lane.getLocation().x - 0.25, -lane
                    .getCenterLine().getLast().y + lane.getLocation().y - 0.25, 0.5, 0.5);
            graphics.setColor(Color.BLUE);
            graphics.fill(startCircle);
            graphics.setColor(Color.RED);
            graphics.fill(endCircle);
        }
    }
}
