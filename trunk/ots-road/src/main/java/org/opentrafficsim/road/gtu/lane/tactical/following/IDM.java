package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;
import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;

/**
 * Implementation of the IDM. See <a
 * href=https://en.wikipedia.org/wiki/Intelligent_driver_model>https://en.wikipedia.org/wiki/Intelligent_driver_model</a>
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version 5 apr. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IDM extends AbstractIDM implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160405L;

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "IDM";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Intelligent Driver Model";
    }

    /** {@inheritDoc} */
    protected final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics, 
        final Speed speed, final Speed desiredSpeed, final Rel desiredHeadway, final SortedMap<Rel, Speed> leaders) 
                throws ParameterException
    {
        Acceleration a = behavioralCharacteristics.getParameter(ParameterTypes.A);
        Acceleration b0 = behavioralCharacteristics.getParameter(ParameterTypes.B0);
        double delta = behavioralCharacteristics.getParameter(DELTA);
        double aFree = a.si * (1 - Math.pow(speed.si / desiredSpeed.si, delta));
        // limit deceleration in free term (occurs if speed > desired speed)
        aFree = aFree > -b0.si ? aFree : -b0.si;
        double sStar = dynamicDesiredHeadway(behavioralCharacteristics, speed, desiredHeadway, 
            leaders.get(leaders.firstKey())).si;
        double aInt = -a.si * (sStar / leaders.firstKey().si) * (sStar / leaders.firstKey().si);
        return new Acceleration(aFree + aInt, AccelerationUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IDM []";
    }
}
