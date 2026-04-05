package org.opentrafficsim.road.network.speed;

/**
 * Speed limit for an instantaneous context of a single GTU. Two speed limits are included to capture nuances in adherence
 * behavior. For example, normally trucks may drive 80-90km/h based on a vehicle type speed limit of 80km/h on any road with a
 * speed limit higher than 80km/h. But on roads with lower speed limits adherence can be more strict. Although a truck driver
 * could drive 85km/h (5km/h faster than the GTU type limit) on a freeway with a speed limit of 100km/h, on a road with a speed
 * limit of 60km/h the truck driver may adhere to that fully. For a truck driver with low GTU type speed limit adherence (e.g.
 * 95km/h), an enforced lane-level speed limit of 90km/h can be more constraining.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param laneSpeedLimit lane-level speed limit, may be {@code null}
 * @param gtuTypeSpeedLimit GTU type level speed limit, may be {@code null}
 */
public record SpeedLimits(SpeedLimit laneSpeedLimit, SpeedLimit gtuTypeSpeedLimit)
{
}
