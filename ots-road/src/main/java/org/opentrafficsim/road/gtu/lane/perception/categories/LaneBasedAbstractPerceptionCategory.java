package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Super class for all perception categories that use a {@code LaneBasedGTU}.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public abstract class LaneBasedAbstractPerceptionCategory extends AbstractPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20160811L;
    
    /** 
     * Maximum deceleration that is used to determine if a vehicle will attempt to stop for a yellow light.  <br> 
     * Derived from the report <cite>Onderzoek geeltijden</cite> by Goudappel Coffeng.
     */
    public static final Acceleration MAX_YELLOW_DECELERATION = new Acceleration(-2.8, AccelerationUnit.METER_PER_SECOND_2);

    /**
     * Maximum deceleration that is used to determine if a vehicle will attempt to stop for a red light. <br>
     * Not based on any scientific source; sorry.
     */
    public static final Acceleration MAX_RED_DECELERATION = new Acceleration(-5, AccelerationUnit.METER_PER_SECOND_2);

    /**
     * @param perception perception
     */
    public LaneBasedAbstractPerceptionCategory(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU getGtu() throws GTUException
    {
        return getPerception().getGtu();
    }
    
    /** {@inheritDoc} */
    @Override
    public final LanePerception getPerception()
    {
        return (LanePerception) super.getPerception();
    }
    
}
