package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Animate a conflict.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ConflictAnimation extends AbstractLineAnimation<ConflictData>
{

    /** */
    private static final long serialVersionUID = 20161207L;

    /** Drawable paths. */
    private final Set<Path2D.Float> paths;

    /**
     * @param source the conflict to draw
     * @param contextualized context provider
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public ConflictAnimation(final ConflictData source, final Contextualized contextualized)
            throws NamingException, RemoteException
    {
        super(source, contextualized, .9, new Length(0.5, LengthUnit.SI));
        // geometry of area (not the line) is absolute; pre-transform geometry to fit rotation of source
        this.paths = this.getSource().getContour() == null ? null
                : PaintPolygons.getPaths(getSource().getLocation(), getSource().getContour().getPointList());
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        // paint the bar that represents the line where the conflict starts, like any other AbstractLineAnimation
        Color fillColor = getSource().getColor();
        graphics.setColor(fillColor);
        super.paint(graphics, observer);

        // paint the additional area with dashed lines
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
        if (this.paths != null)
        {
            setRendering(graphics);
            PaintPolygons.paintPaths(graphics, fillColor, this.paths, false);
            resetRendering(graphics);
        }
        graphics.setStroke(oldStroke);
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
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface ConflictData extends LaneBasedObjectData
    {
        /**
         * Returns the conflict color.
         * @return conflict color.
         */
        Color getColor();

        /**
         * Whether the conflict is a crossing.
         * @return whether the conflict is a crossing.
         */
        boolean isCrossing();

        /**
         * Whether the conflict is a permitted conflict.
         * @return whether the conflict is a permitted conflict.
         */
        boolean isPermitted();
    }

}
