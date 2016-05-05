package org.opentrafficsim.road.network.speed;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeLength;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDM;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 30, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class Tester
{

    /**
     * 
     */
    public Tester()
    {
        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
        //Throw.when(true, DummyException0.class, "dummy text");
        //RelativeLane r = new RelativeLane(LateralDirectionality.LEFT, -2);
        
        SpeedLimitProspect pros = new SpeedLimitProspect();
        double inf = Double.POSITIVE_INFINITY;
        pros.addSpeedInfo(x(-inf), SpeedLimitTypes.MAX_VEHICLE_SPEED, v(160));
        pros.addSpeedInfo(x(-inf), SpeedLimitTypes.ROAD_CLASS, v(130));
        pros.addSpeedInfo(x(-inf), SpeedLimitTypes.FIXED_SIGN, v(120));
        pros.addSpeedInfo(x(-inf), SpeedLimitTypes.CURVATURE, new SpeedInfoCurvature(x(200)));      
        
        pros.addSpeedInfo(x(100), SpeedLimitTypes.SPEED_BUMP, v(60));
        pros.removeSpeedInfo(x(105), SpeedLimitTypes.SPEED_BUMP);
        pros.removeSpeedInfo(x(124), SpeedLimitTypes.CURVATURE);
        pros.addSpeedInfo(x(125), SpeedLimitTypes.CURVATURE, new SpeedInfoCurvature(x(125)));
        pros.removeSpeedInfo(x(137), SpeedLimitTypes.CURVATURE);
        pros.addSpeedInfo(x(150), SpeedLimitTypes.FIXED_SIGN, v(100));
        System.out.println(pros);

        Length loc = x(0);
        System.out.println(loc + " - " + pros.getSpeedLimitInfo(loc));
        for (Length position : pros.getDistances())
        {
            if (position.si > 0)
            {
                System.out.println(position + " - " + pros.getSpeedLimitInfo(position));
            }
        }

    }

    static Speed v(final double v)
    {
        return new Speed(v, SpeedUnit.KM_PER_HOUR);
    }

    static Length x(final double x)
    {
        return new Length(x, LengthUnit.SI);
    }

}
