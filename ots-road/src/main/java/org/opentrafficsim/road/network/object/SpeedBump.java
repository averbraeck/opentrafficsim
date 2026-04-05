package org.opentrafficsim.road.network.object;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.Lane;

/**
 * Speed bump. A speed plateau should be represented as two speed bumps as the location is infinitesimal.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SpeedBump extends AbstractLaneBasedObject
{

    /** Speeds. */
    private final Map<GtuType, Speed> speedMap = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param id id
     * @param lane lane
     * @param longitudinalPosition position
     * @param speeds speeds
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public SpeedBump(final String id, final Lane lane, final Length longitudinalPosition, final Map<GtuType, Speed> speeds)
            throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeLine(lane, longitudinalPosition));
        this.speedMap.putAll(speeds);
    }

    /**
     * Returns the speed for the GTU type, or any of the parent types.
     * @param gtuType GTU type
     * @return speed for the GTU type
     * @throws OtsRuntimeException if the speed bump cannot supply any speed for the GTU type
     */
    public Speed getSpeed(final GtuType gtuType)
    {
        Speed speed = this.speedMap.get(gtuType);
        if (speed == null)
        {
            GtuType parent = gtuType.getParent().orElseGet(() -> null);
            while (speed == null && parent != null)
            {
                speed = this.speedMap.get(parent);
                if (speed != null)
                {
                    // cache for specific type
                    this.speedMap.put(gtuType, speed);
                }
                parent = parent.getParent().orElseGet(() -> null);
            }
            Throw.when(speed == null, OtsRuntimeException.class, "SpeedBump %s cannot provide any speed for GTU type %s",
                    getId(), gtuType.getId());
        }
        return speed;
    }

}
