package org.opentrafficsim.draw.lane;

import java.rmi.RemoteException;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RollingLaneStructure;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * LaneStructureLocatable.java. <br>
 * <br>
 * Copyright (c) 2003-2020 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LaneStructureLocatable implements Locatable
{
    /** RollingLaneStructure. */
    private final RollingLaneStructure rollingLaneStructure;

    /** GTU. */
    private final GTU gtu;

    /**
     * @param rollingLaneStructure RollingLaneStructure; the rolling lane structure
     * @param gtu GTU; the gtu
     */
    public LaneStructureLocatable(final RollingLaneStructure rollingLaneStructure, final GTU gtu)
    {
        this.rollingLaneStructure = rollingLaneStructure;
        this.gtu = gtu;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        LaneStructureRecord rt = this.rollingLaneStructure.getRootRecord();
        if (rt == null)
        {
            return this.gtu.getLocation();
        }
        Length position =
                rt.getDirection().isPlus() ? rt.getStartDistance().neg() : rt.getLane().getLength().plus(rt.getStartDistance());
        position = position.lt0() ? Length.ZERO : position;
        try
        {
            return rt.getLane().getCenterLine().getLocation(position);
        }
        catch (OTSGeometryException exception)
        {
            throw new RuntimeException("Unable to return location.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        Point3d p1 = new Point3d(-1000000, -1000000, 0.0);
        Point3d p2 = new Point3d(1000000, 1000000, 0.0);
        return new BoundingBox(p1, p2);
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
    public final GTU getGtu()
    {
        return this.gtu;
    }

}
