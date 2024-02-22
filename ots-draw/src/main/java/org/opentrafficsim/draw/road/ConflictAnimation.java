package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Animate a conflict.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ConflictAnimation extends AbstractLineAnimation<ConflictData>
{

    /** */
    private static final long serialVersionUID = 20161207L;

    /** Drawable paths. */
    private final Set<Path2D.Double> paths;

    /**
     * @param source ConflictData; the conflict to draw
     * @param contextualized Contextualized; context provider
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public ConflictAnimation(final ConflictData source, final Contextualized contextualized)
            throws NamingException, RemoteException
    {
        super(source, contextualized, .9, new Length(0.5, LengthUnit.SI));
        this.paths = this.getSource().getContour() == null ? null
                : PaintPolygons.getPaths(getSource().getLocation(), getSource().getContour());
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        Color fillColor = getSource().getColor();

        graphics.setColor(fillColor);
        super.paint(graphics, observer);

        Stroke oldStroke = graphics.getStroke();

        BasicStroke stroke;
        float factor = getSource().isPermitted() ? .5f : 1f;
        if (getSource().isCrossing())
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
        double angle = -getSource().getLocation().getDirZ();
        if (isRotate() && angle != 0.0)
        {
            graphics.rotate(-angle);
        }
        if (this.paths != null)
        {
            PaintPolygons.paintPaths(graphics, fillColor, this.paths, false);

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

    /**
     * ConflictData provides the information required to draw a conflict.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface ConflictData extends LaneBasedObjectData
    {
        /**
         * Returns the conflict color.
         * @return Color; conflict color.
         */
        Color getColor();

        /**
         * Returns the contour.
         * @return List&lt;Point2d&gt;; points.
         */
        List<Point2d> getContour();

        /**
         * Whether the conflict is a crossing.
         * @return boolean; whether the conflict is a crossing.
         */
        boolean isCrossing();

        /**
         * Whether the conflict is a permitted conflict.
         * @return boolean; whether the conflict is a permitted conflict.
         */
        boolean isPermitted();
    }

}
