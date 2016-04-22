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
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IDMPlus extends AbstractIDM
{

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "IDM+";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Intelligent Driver Model+";
    }
    
    /** {@inheritDoc} */
    @Override
    protected final Acceleration combineInteractionTerm(final Acceleration aFree,
        final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, final Speed desiredSpeed,
        final Length.Rel desiredHeadway, final SortedMap<Length.Rel, Speed> leaders) throws ParameterException
    {
        Acceleration a = behavioralCharacteristics.getParameter(ParameterTypes.A);
        double sRatio =
            dynamicDesiredHeadway(behavioralCharacteristics, speed, desiredHeadway, leaders.get(leaders.firstKey())).si
                / leaders.firstKey().si;
        double aInt = a.si * (1 - sRatio * sRatio);
        return new Acceleration(aInt < aFree.si ? aInt : aFree.si, AccelerationUnit.SI);
    }

}
