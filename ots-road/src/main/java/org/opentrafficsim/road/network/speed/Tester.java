package org.opentrafficsim.road.network.speed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

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
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {

        SpeedLimitTypeSpeed   typeOrig = new SpeedLimitTypeSpeed("Maximum vehicle speed");
        SpeedLimitTypeSpeed   typeCopy = new SpeedLimitTypeSpeed("Maximum vehicle speed");
        SpeedLimitType<Speed> typeSupe = new SpeedLimitType<>("Maximum vehicle speed", Speed.class);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(typeOrig);
        out.close();
        byte[] buf = bos.toByteArray();
        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buf));
        SpeedLimitTypeSpeed   typeDese = (SpeedLimitTypeSpeed) in.readObject();
        in.close();

        System.out.println("typeOrig is equal to typeCopy by '==':     " + (typeOrig == typeCopy));
        System.out.println("typeOrig is equal to typeCopy by isType(): " + typeOrig.isType(typeCopy));
        System.out.println("typeCopy is equal to typeOrig by isType(): " + typeCopy.isType(typeOrig));
        System.out.println("typeOrig is equal to typeSupe by '==':     " + (typeOrig == typeSupe));
        System.out.println("typeOrig is equal to typeSupe by isType(): " + typeOrig.isType(typeSupe));
        System.out.println("typeSupe is equal to typeOrig by isType(): " + typeSupe.isType(typeOrig));
        System.out.println("typeOrig is equal to typeDese by '==':     " + (typeOrig == typeDese));
        System.out.println("typeOrig is equal to typeDese by isType(): " + typeOrig.isType(typeDese));
        System.out.println("typeDese is equal to typeOrig by isType(): " + typeDese.isType(typeOrig));       

        // Throw.when(true, DummyException0.class, "dummy text");
        // RelativeLane r = new RelativeLane(LateralDirectionality.LEFT, -2);

        SpeedLimitProspect pros = new SpeedLimitProspect();
        pros.addSpeedInfo(x(-900), SpeedLimitTypes.MAX_VEHICLE_SPEED, v(160));
        pros.addSpeedInfo(x(-800), SpeedLimitTypes.ROAD_CLASS, v(130));
        pros.addSpeedInfo(x(-700), SpeedLimitTypes.FIXED_SIGN, v(120));
        pros.addSpeedInfo(x(-600), SpeedLimitTypes.CURVATURE, new SpeedInfoCurvature(x(200)));

        pros.addSpeedInfo(x(100), SpeedLimitTypes.SPEED_BUMP, v(60));
        pros.removeSpeedInfo(x(105), SpeedLimitTypes.SPEED_BUMP);
        pros.removeSpeedInfo(x(124), SpeedLimitTypes.CURVATURE);
        pros.addSpeedInfo(x(125), SpeedLimitTypes.CURVATURE, new SpeedInfoCurvature(x(125)));
        pros.removeSpeedInfo(x(137), SpeedLimitTypes.CURVATURE);
        pros.addSpeedInfo(x(150), SpeedLimitTypes.FIXED_SIGN, v(100));
        System.out.println(pros);

        Length loc = x(0);
        System.out.println(loc + " - " + pros.getSpeedLimitInfo(loc));
        for (Length location : pros.getDistances())
        {
            System.out.println(location + " - " + pros.getSpeedLimitInfo(location));
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
