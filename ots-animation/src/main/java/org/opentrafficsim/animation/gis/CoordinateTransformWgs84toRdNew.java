package org.opentrafficsim.animation.gis;

import java.awt.geom.Point2D;
import java.io.Serializable;

import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.animation.gis.DoubleXY;
import nl.tudelft.simulation.dsol.animation.gis.FloatXY;
import nl.tudelft.simulation.dsol.animation.gis.transform.CoordinateTransform;

/**
 * Convert coordinates from WGS84 to the Dutch RD system. The coordinate transform can be offered to the gisbeans package when
 * parsing GIS coordinates.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CoordinateTransformWgs84toRdNew implements CoordinateTransform, Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** the coordinate shift dx w.r.t. the origin if not in Amersfoort. dx will be subtracted from each RD.x coordinate */
    private final double dx;

    /** the coordinate shift dy w.r.t. the origin if not in Amersfoort. dy will be subtracted from each RD.y coordinate */
    private final double dy;

    /**
     * Constructor.
     * @param dx the coordinate shift dx w.r.t. the origin if not in Amersfoort. dx will be subtracted from each RD.x coordinate
     * @param dy the coordinate shift dy w.r.t. the origin if not in Amersfoort. dy will be subtracted from each RD.y coordinate
     */
    public CoordinateTransformWgs84toRdNew(final double dx, final double dy)
    {
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public final FloatXY floatTransform(final double x, final double y)
    {
        DoubleXY d = doubleTransform(x, y);
        return new FloatXY((float) d.x(), (float) d.y());
    }

    @Override
    public final FloatXY floatTransform(final float x, final float y)
    {
        DoubleXY d = doubleTransform(x, y);
        return new FloatXY((float) d.x(), (float) d.y());
    }

    @Override
    public final DoubleXY doubleTransform(final double x, final double y)
    {
        try
        {
            Point2D c = TransformWgs84DutchRdNew.fromWgs84(x, y);
            return new DoubleXY(c.getX() - this.dx, c.getY() - this.dy);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception);
            return new DoubleXY(0, 0);
        }
    }

    @Override
    public final String toString()
    {
        return "CoordinateTransformRD [dx=" + this.dx + ", dy=" + this.dy + "]";
    }
}
