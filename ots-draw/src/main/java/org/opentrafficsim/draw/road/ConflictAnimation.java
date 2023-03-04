package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;

/**
 * Animate a conflict.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ConflictAnimation extends AbstractLineAnimation<Conflict> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20161207L;

    /**
     * @param source Conflict; the conflict to draw
     * @param simulator OtsSimulatorInterface; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public ConflictAnimation(final Conflict source, final OtsSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(source, simulator, .9, new Length(0.5, LengthUnit.SI));
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        Conflict conflict = this.getSource();
        Color fillColor;
        switch (conflict.conflictPriority())
        {
            case SPLIT:
                fillColor = Color.blue;
                break;

            case PRIORITY:
                fillColor = Color.green;
                break;

            case YIELD:
                fillColor = Color.orange;
                break;

            default:
                // STOP, ALL_STOP, TURN_ON_RED
                fillColor = Color.red;
                break;
        }

        graphics.setColor(fillColor);
        super.paint(graphics, observer);

        Stroke oldStroke = graphics.getStroke();

        BasicStroke stroke;
        float factor = conflict.isPermitted() ? .5f : 1f;
        if (conflict.getConflictType().equals(ConflictType.CROSSING))
        {
            stroke = new BasicStroke(.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
                    new float[] {factor * 1.0f, factor * 2.0f}, 0.0f);
        }
        else
        {
            stroke = new BasicStroke(.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
                    new float[] {factor * 1.0f, factor * 0.95f, factor * 0.1f, factor * 0.95f}, 0.0f);
        }
        graphics.setStroke(stroke);
        AffineTransform saveAT = graphics.getTransform();
        double angle = -getSource().getLocation().getRotZ();
        if (isRotate() && angle != 0.0)
        {
            graphics.rotate(-angle);
        }
        if (conflict.getGeometry() != null)
        {
            PaintPolygons.paintMultiPolygon(graphics, fillColor, conflict.getLocation(), conflict.getGeometry(), false);
            
            /*- This code may be used to visually check conflicts are correctly paired
            if (conflict.conflictPriority().isPriority())
            {
                graphics.setColor(Color.BLACK);
                DirectedPoint from = conflict.getLocation();
                DirectedPoint to = conflict.getOtherConflict().getLocation();
                graphics.setStroke(new BasicStroke(0.1f));
                Line2D line = new Line2D.Double(0, 0, to.x - from.x, from.y - to.y);
                graphics.draw(line);
            }*/
        }
        if (isRotate() && angle != 0.0)
        {
            graphics.rotate(+angle);
        }
        graphics.setStroke(oldStroke);
        graphics.setTransform(saveAT);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ConflictAnimation [getSource()=" + getSource() + "]";
    }

}
