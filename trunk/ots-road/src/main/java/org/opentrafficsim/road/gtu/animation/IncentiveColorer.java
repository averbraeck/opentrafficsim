package org.opentrafficsim.road.gtu.animation;

import java.awt.Color;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;

/**
 * Colorer for desire from a specific incentive.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveColorer extends DesireColorer
{

    /** */
    private static final long serialVersionUID = 20170414L;

    /** Incentive class. */
    private Class<? extends Incentive> incentiveClass;

    /**
     * @param incentiveClass incentive class
     */
    public IncentiveColorer(final Class<? extends Incentive> incentiveClass)
    {
        this.incentiveClass = incentiveClass;
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final GTU gtu)
    {
        if (!(gtu.getTacticalPlanner() instanceof DesireBased))
        {
            return NA;
        }
        Desire d = ((DesireBased) gtu.getTacticalPlanner()).getLatestDesire(this.incentiveClass);
        if (d != null)
        {
            return getColor(d.getLeft(), d.getRight());
        }
        return NA;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.incentiveClass.getSimpleName();
    }

}
