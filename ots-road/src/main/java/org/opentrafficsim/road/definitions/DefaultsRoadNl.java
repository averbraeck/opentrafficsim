package org.opentrafficsim.road.definitions;

import java.util.Locale;

import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.object.DetectorType;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.network.lane.LaneType;

/**
 * Road defaults for locale nl_NL.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DefaultsRoadNl extends DefaultsRoad
{

    /**
     * Constructor setting locale nl_NL.
     */
    DefaultsRoadNl()
    {
        super(new Locale("nl", "NL"));
    }

    /***************************************************************************************/
    /**************************************** LANE *****************************************/
    /***************************************************************************************/

    /** For two-way roads. */
    public static final LaneType TWO_WAY_LANE = new LaneType("NL.TWO_WAY_LANE");

    /** Provincial road (provinciaalse weg / N-weg, 80km/h). */
    public static final LaneType PROVINCIAL_ROAD = new LaneType("NL.PROVINCIAL_ROAD", TWO_WAY_LANE);

    /** Rural (landelijk, 60km/h). */
    public static final LaneType RURAL_ROAD = new LaneType("NL.RURAL_ROAD", TWO_WAY_LANE);

    /** Urban (stedelijk, 50km/h). */
    public static final LaneType URBAN_ROAD = new LaneType("NL.URBAN_ROAD", TWO_WAY_LANE);

    /** Residential (woonerf, 30km/h). */
    public static final LaneType RESIDENTIAL_ROAD = new LaneType("NL.RESIDENTIAL_ROAD", TWO_WAY_LANE);

    /** For one-way roads. */
    public static final LaneType ONE_WAY_LANE = new LaneType("NL.ONE_WAY_LANE");

    /** Freeway (snelweg, 130km/h). */
    public static final LaneType FREEWAY = new LaneType("NL.FREEWAY", ONE_WAY_LANE);

    /** Highway (autoweg, 100km/h). */
    public static final LaneType HIGHWAY = new LaneType("NL.HIGHWAY", ONE_WAY_LANE);

    /** Bus lane. */
    public static final LaneType BUS_LANE = new LaneType("NL.BUS_LANE");

    /** Moped path (fiets-/bromfietspad). */
    public static final LaneType MOPED_PATH = new LaneType("NL.MOPED_PATH");

    /** Bicycle path (fietspad). */
    public static final LaneType BICYCLE_PATH = new LaneType("NL.BICYCLE_PATH", MOPED_PATH);

    /** Footpath (voetpad). */
    public static final LaneType FOOTPATH = new LaneType("NL.FOOTPATH");

    static
    {
        TWO_WAY_LANE.addCompatibleGtuType(DefaultsNl.ROAD_USER);
        ONE_WAY_LANE.addCompatibleGtuType(DefaultsNl.ROAD_USER);
        FREEWAY.addIncompatibleGtuType(DefaultsNl.PEDESTRIAN);
        FREEWAY.addIncompatibleGtuType(DefaultsNl.BICYCLE);
        HIGHWAY.addIncompatibleGtuType(DefaultsNl.PEDESTRIAN);
        HIGHWAY.addIncompatibleGtuType(DefaultsNl.BICYCLE);
        PROVINCIAL_ROAD.addIncompatibleGtuType(DefaultsNl.PEDESTRIAN);
        PROVINCIAL_ROAD.addIncompatibleGtuType(DefaultsNl.BICYCLE);
        BUS_LANE.addCompatibleGtuType(DefaultsNl.BUS);
        MOPED_PATH.addCompatibleGtuType(DefaultsNl.BICYCLE);
        BICYCLE_PATH.addIncompatibleGtuType(DefaultsNl.MOPED);
        FOOTPATH.addCompatibleGtuType(DefaultsNl.PEDESTRIAN);
    }

    /***************************************************************************************/
    /************************************** LANE BIAS **************************************/
    /***************************************************************************************/

    public static final LaneBiases LANE_BIAS_CAR_TRUCK = new LaneBiases();

    static
    {
        LANE_BIAS_CAR_TRUCK.addBias(DefaultsNl.CAR, LaneBias.WEAK_LEFT);
        LANE_BIAS_CAR_TRUCK.addBias(DefaultsNl.TRUCK, LaneBias.TRUCK_RIGHT);
    }

}
