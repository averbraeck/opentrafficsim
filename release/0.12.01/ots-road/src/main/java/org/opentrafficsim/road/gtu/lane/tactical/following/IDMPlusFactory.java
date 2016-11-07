package org.opentrafficsim.road.gtu.lane.tactical.following;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IDMPlusFactory implements CarFollowingModelFactory<IDMPlus>
{

    /** Single instance as it is state-less. */
    private final IDMPlus idmPlus = new IDMPlus();

    /** {@inheritDoc} */
    @Override
    public final IDMPlus generateCarFollowingModel()
    {
        return this.idmPlus;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IDMPlusFactory";
    }
    
}
