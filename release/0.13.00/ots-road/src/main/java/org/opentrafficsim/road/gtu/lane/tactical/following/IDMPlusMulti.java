package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 22, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class IDMPlusMulti extends AbstractIDM
{

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "IDM+multi";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Intelligent Driver Model+ with multi-leader anticipation.";
    }

    /** {@inheritDoc} */
    @Override
    protected final Acceleration combineInteractionTerm(final Acceleration aFree,
        final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, final Speed desiredSpeed,
        final Length desiredHeadway, final SortedMap<Length, Speed> leaders) throws ParameterException
    {
        Acceleration a = behavioralCharacteristics.getParameter(ParameterTypes.A);
        double aIntMulti = Double.POSITIVE_INFINITY;
        int i = 1;
        double cumulVehicleLengths = 0;
        for (Length headway : leaders.keySet())
        {
            // desired headway is scaled to the i'th leader
            // current headway is the sum of net headways (i.e. vehicle lengths of vehicles in between are subtracted)
            double sRatio =
                dynamicDesiredHeadway(behavioralCharacteristics, speed, desiredHeadway.multiplyBy(i), leaders.get(headway)).si
                    / (headway.si - cumulVehicleLengths);
            double aIntSingle = a.si * (1 - sRatio * sRatio);
            aIntMulti = aIntMulti < aIntSingle ? aIntMulti : aIntSingle;
            i++;
            cumulVehicleLengths += 0; // TODO add vehicle length corresponding to key 'headway'
        }
        return new Acceleration(aIntMulti < aFree.si ? aIntMulti : aFree.si, AccelerationUnit.SI);
    }

}
