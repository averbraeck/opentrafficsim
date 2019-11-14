package org.opentrafficsim.road.gtu.lane.tactical.following;

import static org.opentrafficsim.base.parameters.constraint.NumericConstraint.POSITIVE;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeInteger;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 22, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class IDMPlusMulti extends AbstractIDM
{

    /** Number of leaders considered parameter. */
    public static final ParameterTypeInteger NLEADERS =
            new ParameterTypeInteger("nLeaders", "Number of leaders in car-following model.", 2, POSITIVE);

    /**
     * Default constructor using default models for desired headway and desired speed.
     */
    public IDMPlusMulti()
    {
        super(HEADWAY, DESIRED_SPEED);
    }

    /**
     * Constructor with modular models for desired headway and desired speed.
     * @param desiredHeadwayModel DesiredHeadwayModel; desired headway model
     * @param desiredSpeedModel DesiredSpeedModel; desired speed model
     */
    public IDMPlusMulti(final DesiredHeadwayModel desiredHeadwayModel, final DesiredSpeedModel desiredSpeedModel)
    {
        super(desiredHeadwayModel, desiredSpeedModel);
    }

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
    protected final Acceleration combineInteractionTerm(final Acceleration aFree, final Parameters parameters,
            final Speed speed, final Speed desiredSpeed, final Length desiredHeadway,
            final PerceptionIterable<? extends Headway> leaders) throws ParameterException
    {
        Acceleration a = parameters.getParameter(A);
        double aIntMulti = Double.POSITIVE_INFINITY;
        int i = 1;
        double cumulVehicleLengths = 0;
        int n = parameters.getParameter(NLEADERS);
        for (Headway leader : leaders)
        {
            // desired headway is scaled to the i'th leader
            // current headway is the sum of net headways (i.e. vehicle lengths of vehicles in between are subtracted)
            double sRatio = dynamicDesiredHeadway(parameters, speed, desiredHeadway.times(i), leader.getSpeed()).si
                    / (leader.getDistance().si - cumulVehicleLengths);
            double aIntSingle = a.si * (1 - sRatio * sRatio);
            aIntMulti = aIntMulti < aIntSingle ? aIntMulti : aIntSingle;
            i++;
            if (i > n)
            {
                break;
            }
            if (leader.getLength() != null) // usage could be e.g. a dead-end
            {
                cumulVehicleLengths += leader.getLength().si;
            }
        }
        return new Acceleration(aIntMulti < aFree.si ? aIntMulti : aFree.si, AccelerationUnit.SI);
    }

}
