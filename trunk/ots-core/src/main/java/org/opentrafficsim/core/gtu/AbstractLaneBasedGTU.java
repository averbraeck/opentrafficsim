package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.LaneLocation;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractLaneBasedGTU<ID> extends AbstractGTU<ID> implements LaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param length the maximum length of the GTU (parallel with driving direction).
     * @param width the maximum width of the GTU (perpendicular to driving direction).
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction).
     */
    public AbstractLaneBasedGTU(final ID id, final GTUType<?> gtuType, final DoubleScalar<LengthUnit> length,
            final DoubleScalar<LengthUnit> width, final DoubleScalar<SpeedUnit> maximumVelocity)
    {
        super(id, gtuType, length, width, maximumVelocity);
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        Set<LaneLocation> laneLocations = getCurrentLocation(new GTUReferencePoint(0.0, 0.0, 0.0));
        if (laneLocations.isEmpty())
        {
            return null;
        }
        LaneLocation laneLocation = laneLocations.iterator().next();
        LineString line = laneLocation.getLane().getOffsetLine();
        double fraction = laneLocation.getFractionalLongitudinalPosition();
        LengthIndexedLine lil = new LengthIndexedLine(line);
        Coordinate c = lil.extractPoint(fraction * line.getLength());
        Coordinate ca = (fraction <= 0.01) ? lil.extractPoint(0.0) : lil.extractPoint((fraction - 0.01) * line.getLength());
        Coordinate cb = (fraction >= 0.99) ? lil.extractPoint(1.0) : lil.extractPoint((fraction + 0.01) * line.getLength());
        double angle = Math.atan2(cb.y - ca.y, cb.x - ca.x);
        return new DirectedPoint(c.x, c.y, c.z, 0.0, 0.0, angle);
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        DirectedPoint l = getLocation();
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(l.x - dx, l.y - dy, 0.0), new Point3d(l.x + dx, l.y + dy, l.z));
    }

}
