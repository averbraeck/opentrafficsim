package org.opentrafficsim.animation.colorer;

import java.awt.Color;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.tactical.DesireBased;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;

/**
 * Colorer for desire from a specific incentive.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IncentiveColorer extends DesireColorer
{

    /** */
    private static final long serialVersionUID = 20170414L;

    /** Incentive class. */
    private Class<? extends Incentive> incentiveClass;

    /**
     * Constructor.
     * @param incentiveClass incentive class
     */
    public IncentiveColorer(final Class<? extends Incentive> incentiveClass)
    {
        this.incentiveClass = incentiveClass;
    }

    @Override
    public final Color getColor(final Gtu gtu)
    {
        if (!(gtu.getTacticalPlanner() instanceof DesireBased))
        {
            return NA;
        }
        Desire d = ((DesireBased) gtu.getTacticalPlanner()).getLatestDesire(this.incentiveClass);
        if (d != null)
        {
            return getColor(d.left(), d.right());
        }
        return NA;
    }

    @Override
    public final String getName()
    {
        return this.incentiveClass.getSimpleName();
    }

}
