package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;

/**
 * Implementation of the IDM. See <a
 * href=https://en.wikipedia.org/wiki/Intelligent_driver_model>https://en.wikipedia.org/wiki/Intelligent_driver_model</a>
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version 5 apr. 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IDM extends AbstractIDM
{
    
    /**
     * Default constructor using default models for desired headway and desired speed.
     */
    public IDM()
    {
        super(HEADWAY, DESIRED_SPEED);
    }
    
    /**
     * Constructor with modular models for desired headway and desired speed.
     * @param desiredHeadwayModel desired headway model
     * @param desiredSpeedModel desired speed model
     */
    public IDM(final DesiredHeadwayModel desiredHeadwayModel, final DesiredSpeedModel desiredSpeedModel)
    {
        super(desiredHeadwayModel, desiredSpeedModel);
    }
    
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
    @Override
    protected final Acceleration combineInteractionTerm(final Acceleration aFree,
            final Parameters parameters, final Speed speed, final Speed desiredSpeed,
            final Length desiredHeadway, final SortedMap<Length, Speed> leaders) throws ParameterException
    {
        Acceleration a = parameters.getParameter(A);
        double sRatio =
                dynamicDesiredHeadway(parameters, speed, desiredHeadway, leaders.get(leaders.firstKey())).si
                        / leaders.firstKey().si;
        double aInt = -a.si * sRatio * sRatio;
        return Acceleration.createSI(aFree.si + aInt);
    }

}
