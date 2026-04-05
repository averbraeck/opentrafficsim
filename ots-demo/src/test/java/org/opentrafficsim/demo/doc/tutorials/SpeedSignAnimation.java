package org.opentrafficsim.demo.doc.tutorials;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.object.LaneBasedObject;

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("all")

// @docs/08-tutorials/visualization.md#how-to-add-an-animation
public class SpeedSignAnimation extends OtsRenderable<SpeedSign>
{
    private static final double RADIUS = 1.6;

    private static final double EDGE = 1.3;

    public SpeedSignAnimation(final SpeedSign source, final OtsSimulatorInterface simulator)
    {
        super(source, simulator);
        setRotate(false);
    }

    // @docs/08-tutorials/visualization.md#how-to-add-an-animation
    @Override
    public final void paint(final Graphics2D g, final ImageObserver arg1)
    {
        Ellipse2D ellipse = new Ellipse2D.Double(-RADIUS, -RADIUS, 2 * RADIUS, 2 * RADIUS);
        g.setColor(Color.RED);
        g.fill(ellipse);
        ellipse = new Ellipse2D.Double(-EDGE, -EDGE, 2 * EDGE, 2 * EDGE);
        g.setColor(Color.WHITE);
        g.fill(ellipse);
        g.setColor(Color.BLACK);
        int speed = (int) getSource().getSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
        if (speed < 100)
        {
            g.setFont(new Font("Arial", 0, -1).deriveFont(2.0f));
        }
        else
        {
            g.setFont(new Font("Arial narrow", 0, -1).deriveFont(1.85f));
        }
        String str = Integer.toString(speed);
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(str, g);
        g.drawString(str, (float) -stringBounds.getCenterX(), (float) -stringBounds.getCenterY());
    }
}

@SuppressWarnings("all")
// Dummy class for SpeedSignAnimation
class SpeedSign extends AbstractLaneBasedObject
{

    final Speed speed;

    protected SpeedSign(final String id, final Lane lane, final Length longitudinalPosition, final Speed speed)
            throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeLine(lane, longitudinalPosition));
        this.speed = speed;
    }

    public Speed getSpeed()
    {
        return this.speed;
    }

}
