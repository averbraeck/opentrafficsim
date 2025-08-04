package org.opentrafficsim.road.network.lane.object;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.draw.point.Point3d;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.StaticObject;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Speed sign.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IndicatorPoint extends StaticObject
{

    /** */
    private static final long serialVersionUID = 20170420L;

    @SuppressWarnings("checkstyle:parameternumber")
    public IndicatorPoint(final String id, PolyLine2d geometry)
            throws NetworkException
    {
        super(id, new OrientedPoint2d(0, 0), geometry, new Length(-999, LengthUnit.METER));
        init();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return 123;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IndicatorPoint ";
    }

}
