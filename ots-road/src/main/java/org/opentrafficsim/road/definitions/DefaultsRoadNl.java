package org.opentrafficsim.road.definitions;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.StripeData;
import org.opentrafficsim.road.network.lane.StripeData.StripePhaseSync;

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

    /***************************************************************************************/
    /*************************************** STRIPES ***************************************/
    /***************************************************************************************/

    /** Standard width. */
    private static final Length CM20 = Length.instantiateSI(0.2);

    /** Standard dashes. */
    private static final LengthVector DASH = new LengthVector(new double[] {9.0, 3.0});

    /** Solid stripe. */
    public static final StripeData SOLID = new StripeData(List.of(StripeElement.continuous(CM20, Color.WHITE)), false, false);

    /** Left-permeable stripe. */
    public static final StripeData LEFT = new StripeData(List.of(StripeElement.continuous(CM20, Color.WHITE),
            StripeElement.gap(CM20), StripeElement.dashed(CM20, Color.WHITE, DASH)), true, false);

    /** Right-permable stripe. */
    public static final StripeData RIGHT = new StripeData(List.of(StripeElement.dashed(CM20, Color.WHITE, DASH),
            StripeElement.gap(CM20), StripeElement.continuous(CM20, Color.WHITE)), false, true);

    /** Dashed stripe. */
    public static final StripeData DASHED = new StripeData(List.of(StripeElement.dashed(CM20, Color.WHITE, DASH)), true, true);

    /** Double solid stripe. */
    public static final StripeData DOUBLE_SOLID = new StripeData(List.of(StripeElement.continuous(CM20, Color.WHITE),
            StripeElement.gap(CM20), StripeElement.continuous(CM20, Color.WHITE)), false, false);

    /** Double dashed stripe. */
    public static final StripeData DOUBLE_DASHED = new StripeData(List.of(StripeElement.dashed(CM20, Color.WHITE, DASH),
            StripeElement.gap(CM20), StripeElement.dashed(CM20, Color.WHITE, DASH)), true, true);

    /** Block stripe. */
    public static final StripeData BLOCK = new StripeData(
            List.of(StripeElement.dashed(CM20.times(2.0), Color.WHITE, new LengthVector(new double[] {3.0, 1.0}))), true, true);
    
    static
    {
        LEFT.setPhaseSync(StripePhaseSync.UPSTREAM);
        RIGHT.setPhaseSync(StripePhaseSync.UPSTREAM);
        DASHED.setPhaseSync(StripePhaseSync.UPSTREAM);
        DOUBLE_DASHED.setPhaseSync(StripePhaseSync.UPSTREAM);
        BLOCK.setPhaseSync(StripePhaseSync.UPSTREAM);
    }

}
