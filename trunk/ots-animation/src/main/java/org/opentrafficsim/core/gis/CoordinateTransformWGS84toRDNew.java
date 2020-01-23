package org.opentrafficsim.core.gis;

import java.awt.geom.Point2D;
import java.io.Serializable;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.logger.SimLogger;

/**
 * Convert coordinates from WGS84 to the Dutch RD system. The coordinate transform can be offered to the gisbeans package when
 * parsing GIS coordinates.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CoordinateTransformWGS84toRDNew implements CoordinateTransform, Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** the coordinate shift dx w.r.t. the origin if not in Amersfoort. dx will be subtracted from each RD.x coordinate */
    private final double dx;

    /** the coordinate shift dy w.r.t. the origin if not in Amersfoort. dy will be subtracted from each RD.y coordinate */
    private final double dy;

    /**
     * @param dx double; the coordinate shift dx w.r.t. the origin if not in Amersfoort. dx will be subtracted from each RD.x
     *            coordinate
     * @param dy double; the coordinate shift dy w.r.t. the origin if not in Amersfoort. dy will be subtracted from each RD.y
     *            coordinate
     */
    public CoordinateTransformWGS84toRDNew(final double dx, final double dy)
    {
        this.dx = dx;
        this.dy = dy;
    }

    /** {@inheritDoc} */
    @Override
    public final float[] floatTransform(final double x, final double y)
    {
        double[] d = doubleTransform(x, y);
        return new float[] {(float) d[0], (float) d[1]};
    }

    /** {@inheritDoc} */
    @Override
    public final double[] doubleTransform(final double x, final double y)
    {
        try
        {
            Point2D c = TransformWGS84DutchRDNew.fromWGS84(x, y);
            return new double[] {c.getX() - this.dx, c.getY() - this.dy};
        }
        catch (Exception exception)
        {
            SimLogger.always().error(exception);
            return new double[] {0, 0};
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CoordinateTransformRD [dx=" + this.dx + ", dy=" + this.dy + "]";
    }
}
