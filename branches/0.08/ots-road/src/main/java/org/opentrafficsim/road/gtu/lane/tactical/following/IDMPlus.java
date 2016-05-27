package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Implementation of the IDM+. See Schakel, W.J., Knoop, V.L., and Van Arem, B. (2012), <a
 * href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model with Relaxation and
 * Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No. 2316, pp. 47-57. Note
 * in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not in the preprint
 * provided here). A list of errata for the official versions is found <a
 * href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * @author Wouter Schakel
 */
public class IDMPlus extends IDM
{

    /**
     * {@inheritDoc}
     */
    protected Acceleration followingAcceleration(LaneBasedGTU gtu, Speed speed, Speed desiredSpeed, Rel desiredHeadway,
        SortedMap<Rel, Speed> leaders) throws ParameterException
    {
        double sStar = dynamicDesiredHeadway(gtu, speed, desiredHeadway, leaders.get(leaders.firstKey())).si;
        // minimum of both terms
        Acceleration a = gtu.getBehavioralCharacteristics().getAccelerationParameter(ParameterTypes.A);
        double delta = gtu.getBehavioralCharacteristics().getParameter(DELTA);
        double aInt = a.si * (1 - (sStar / leaders.firstKey().si) * (sStar / leaders.firstKey().si));
        double aFree = a.si * (1 - Math.pow(speed.si / desiredSpeed.si, delta));
        return new Acceleration(aInt < aFree ? aInt : aFree, AccelerationUnit.SI);
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return "IDM+";
    }

    /** {@inheritDoc} */
    public String getLongName()
    {
        return "Intelligent Driver Model+";
    }

}
