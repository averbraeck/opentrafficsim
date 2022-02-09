package org.opentrafficsim.road.network.speed;

/**
 * Predefined list of speed limit types.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@SuppressWarnings("checkstyle:finalclass")
public class SpeedLimitTypes
{

    /** Do not create instance. */
    private SpeedLimitTypes()
    {
        //
    }

    /** Maximum vehicle speed limit type. */
    public static final SpeedLimitTypeSpeed MAX_VEHICLE_SPEED;

    /** Maximum legal vehicle speed limit type. */
    public static final SpeedLimitTypeSpeedLegal MAX_LEGAL_VEHICLE_SPEED;

    /** Road class speed limit type. */
    public static final SpeedLimitTypeSpeedLegal ROAD_CLASS;

    /** Fixed speed sign speed limit type. */
    public static final SpeedLimitTypeSpeedLegal FIXED_SIGN;

    /** Dynamic speed sign speed limit type. */
    public static final SpeedLimitTypeSpeedLegal DYNAMIC_SIGN;

    /** Curvature speed limit type. */
    public static final SpeedLimitType<SpeedInfoCurvature> CURVATURE;

    /** Speed bump speed limit type with design speed as info. */
    public static final SpeedLimitTypeSpeed SPEED_BUMP;

    static
    {
        MAX_VEHICLE_SPEED = new SpeedLimitTypeSpeed("Maximum vehicle speed");
        MAX_LEGAL_VEHICLE_SPEED = new SpeedLimitTypeSpeedLegal("Maximum legal vehicle speed");
        ROAD_CLASS = new SpeedLimitTypeSpeedLegal("Road class");
        FIXED_SIGN = new SpeedLimitTypeSpeedLegal("Fixed sign");
        DYNAMIC_SIGN = new SpeedLimitTypeSpeedLegal("Dynamic sign");
        CURVATURE = new SpeedLimitType<>("Curvature", SpeedInfoCurvature.class);
        SPEED_BUMP = new SpeedLimitTypeSpeed("Speed bump");
    }

}
