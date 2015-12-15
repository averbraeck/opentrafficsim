package org.opentrafficsim.core.gtu.plan.operational;

import static org.junit.Assert.assertEquals;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.scalar.Time.Rel;
import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test the OperationalPlan and OperationalPlanBuilder classes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Dec 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OperationalPlanTest
{
    /**
     * Test OperationalPlan.
     * @throws NetworkException 
     */
    @Test
    public void testOperationalPlan() throws NetworkException
    {
        DirectedPoint waitPoint = new DirectedPoint(12, 13, 14, 15, 16, 17);
        Time.Abs startTime = new Time.Abs(100, TimeUnit.SECOND);
        Time.Rel duration = new Time.Rel(1, TimeUnit.MINUTE);
        OperationalPlan op = new OperationalPlan(waitPoint, startTime, duration);
        assertEquals("Start speed is 0", 0, op.getStartSpeed().si, 0);
        assertEquals("End speed is 0", 0, op.getEndSpeed().si, 0);
        assertEquals("Start time is " + startTime, startTime.si, op.getStartTime().si, 0);
        assertEquals("End time is " + startTime.plus(duration), startTime.plus(duration).si, op.getEndTime().si, 0.0001);
        for (int i = 0; i <= duration.si; i++)
        {
            Time.Abs t = startTime.plus(new Time.Rel(i, TimeUnit.SECOND));
            DirectedPoint locationAtT = op.getLocation(t);
            System.out.println("Location at time " + t + " is " + locationAtT);
            //assertEquals("Distance from wait point at " + t + " is 0", 0, waitPoint.distance(locationAtT), 0.0001);
        }
    }
}
