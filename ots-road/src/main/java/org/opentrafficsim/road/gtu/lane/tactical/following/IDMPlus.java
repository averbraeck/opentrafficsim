package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.drivercharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterTypes;

/**
 * Implementation of the IDM+. See Schakel, W.J., Knoop, V.L., and Van Arem, B. (2012), <a
 * href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model with Relaxation and
 * Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No. 2316, pp. 47-57. Note
 * in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not in the preprint
 * provided here). A list of errata for the official versions is found <a
 * href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
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
public class IDMPlus extends AbstractIDM
{

    /** {@inheritDoc} */
    @Override
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
        double aInt = a.si * (1 - (sStar / leaders.firstKey().si) * (sStar / leaders.firstKey().si));
        // minimum of both terms
        return new Acceleration(aInt < aFree ? aInt : aFree, AccelerationUnit.SI);
    }

    /** {@inheritDoc} */
    public final String getName()
    {
        return "IDM+";
    }

    /** {@inheritDoc} */
    public final String getLongName()
    {
        return "Intelligent Driver Model+";
    }

}
