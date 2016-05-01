package org.opentrafficsim.road.network.speed;

/**
 * Predefined list of speed limit types.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2016 <br>
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
    public static final SpeedLimitTypeSpeed MAX_VEHICLE_SPEED = new SpeedLimitTypeSpeed("Maximum vehicle speed");

    /** Maximum legal vehicle speed limit type. */
    public static final SpeedLimitTypeLegal MAX_LEGAL_VEHICLE_SPEED = new SpeedLimitTypeLegal("Maximum legal vehicle speed");

    /** Road class speed limit type. */
    public static final SpeedLimitTypeLegal ROAD_CLASS = new SpeedLimitTypeLegal("Road class");

    /** Fixed speed sign speed limit type. */
    public static final SpeedLimitTypeLegal FIXED_SIGN = new SpeedLimitTypeLegal("Fixed sign");

    /** Dynamic speed sign speed limit type. */
    public static final SpeedLimitTypeLegal DYNAMIC_SIGN = new SpeedLimitTypeLegal("Dynamic sign");

    /** Curvature speed limit type. */
    public static final SpeedLimitType<CurvatureSpeedInfo> CURVATURE = new SpeedLimitType<CurvatureSpeedInfo>("Curvature",
        CurvatureSpeedInfo.class);

    /** Speed bump speed limit type. */
    public static final SpeedLimitTypeSpeed SPEED_BUMP = new SpeedLimitTypeSpeed("Speed bump");

}
