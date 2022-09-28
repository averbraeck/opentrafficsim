package org.opentrafficsim.road.network;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.tactical.routesystem.RouteSystem;
import org.opentrafficsim.road.network.lane.LaneType;

/**
 * OTSRoadNetwork adds a number of methods to the Network class that are specific for roads, such as the LaneTypes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class OTSRoadNetwork extends OTSNetwork implements RoadNetwork
{
    /** */
    private static final long serialVersionUID = 1L;

    /** LaneTypes registered for this network. */
    private Map<String, LaneType> laneTypeMap = new LinkedHashMap<>();

    /** Route system. */
    private RouteSystem routeSystem;

    /**
     * Construction of an empty network.
     * @param id String; the network id.
     * @param addDefaultTypes add the default GtuTypes, LinkTypesand LaneTypes, or not
     * @param simulator OTSSimulatorInterface; the DSOL simulator engine
     */
    public OTSRoadNetwork(final String id, final boolean addDefaultTypes, final OTSSimulatorInterface simulator)
    {
        super(id, addDefaultTypes, simulator);
        if (addDefaultTypes)
        {
            addDefaultLaneTypes();
        }
        // TODO: not null once the route system works
        this.routeSystem = null; // new DefaultRouteSystem();
    }

    /** {@inheritDoc} */
    @Override
    public void addDefaultLaneTypes()
    {
        new LaneType("NONE", this);

        LaneType road = new LaneType("TWO_WAY_LANE", this);
        new LaneType("RURAL_ROAD", road, this);
        new LaneType("URBAN_ROAD", road, this);
        new LaneType("RESIDENTIAL_ROAD", road, this);
        road.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.ROAD_USER));

        LaneType oneWayLane = new LaneType("ONE_WAY_LANE", road, this);
        oneWayLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.ROAD_USER));
        oneWayLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));

        LaneType freeway = new LaneType("FREEWAY", oneWayLane, this);
        freeway.addIncompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));
        LaneType highway = new LaneType("HIGHWAY", oneWayLane, this);
        highway.addIncompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));

        LaneType busLane = new LaneType("BUS_LANE", this);
        busLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.BUS)); 

        LaneType mopedAndBicycleLane = new LaneType("MOPED_PATH", this);
        mopedAndBicycleLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.BICYCLE));
        mopedAndBicycleLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.MOPED));

        LaneType bicycleOnly = new LaneType("BICYCLE_PATH", mopedAndBicycleLane, this);
        bicycleOnly.addIncompatibleGtuType(getGtuType(GtuType.DEFAULTS.MOPED));

        LaneType pedestriansOnly = new LaneType("FOOTPATH", this);
        pedestriansOnly.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));
    }

    /** {@inheritDoc} */
    @Override
    public void addLaneType(final LaneType laneType)
    {
        this.laneTypeMap.put(laneType.getId(), laneType);
    }

    /** {@inheritDoc} */
    @Override
    public LaneType getLaneType(final String laneTypeId)
    {
        return this.laneTypeMap.get(laneTypeId);
    }

    /** {@inheritDoc} */
    @Override
    public LaneType getLaneType(final LaneType.DEFAULTS laneTypeEnum)
    {
        return this.laneTypeMap.get(laneTypeEnum.getId());
    }

    /** {@inheritDoc} */
    @Override
    public ImmutableMap<String, LaneType> getLaneTypes()
    {
        return new ImmutableHashMap<>(this.laneTypeMap, Immutable.WRAP);
    }

    /**
     * Sets the route system.
     * @param routeSystem RouteSystem; route system
     */
    public void setRouteSystem(final RouteSystem routeSystem)
    {
        Throw.whenNull(routeSystem, "Route system may not be null.");
        this.routeSystem = routeSystem;
    }

    /**
     * Returns the route system.
     * @return RouteSystem; route system
     */
    public RouteSystem getRouteSystem()
    {
        return this.routeSystem;
    }

}
