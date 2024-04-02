package org.opentrafficsim.animation.lane;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.BoundingBox;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RollingLaneStructure;

/**
 * LaneStructureLocatable.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LaneStructureLocatable implements OtsLocatable
{
    /** RollingLaneStructure. */
    private final RollingLaneStructure rollingLaneStructure;

    /** GTU. */
    private final Gtu gtu;

    /**
     * @param rollingLaneStructure RollingLaneStructure; the rolling lane structure
     * @param gtu Gtu; the gtu
     */
    public LaneStructureLocatable(final RollingLaneStructure rollingLaneStructure, final Gtu gtu)
    {
        this.rollingLaneStructure = rollingLaneStructure;
        this.gtu = gtu;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        LaneStructureRecord rt = this.rollingLaneStructure.getRootRecord();
        if (rt == null)
        {
            return this.gtu.getLocation();
        }
        Length position = rt.getStartDistance().neg();
        position = position.lt0() ? Length.ZERO : position;
        try
        {
            return rt.getLane().getCenterLine().getLocation(position);
        }
        catch (OtsGeometryException exception)
        {
            throw new RuntimeException("Unable to return location.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getBounds()
    {
        return new BoundingBox(1000000, 1000000);
    }

    /**
     * @return rollingLaneStructure
     */
    public final RollingLaneStructure getRollingLaneStructure()
    {
        return this.rollingLaneStructure;
    }

    /**
     * @return gtu
     */
    public final Gtu getGtu()
    {
        return this.gtu;
    }

}
