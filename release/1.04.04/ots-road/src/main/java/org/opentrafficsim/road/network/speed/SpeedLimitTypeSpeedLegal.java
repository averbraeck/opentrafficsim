package org.opentrafficsim.road.network.speed;

/**
 * Similar to {@code SpeedLimitTypeSpeed} but implements the marker interface {@code LegalSpeedLimit}.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedLimitTypeSpeedLegal extends SpeedLimitTypeSpeed implements LegalSpeedLimit
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /**
     * Constructor.
     * @param id String; id of this speed limit type, which must be unique
     * @throws NullPointerException if id is null
     */
    public SpeedLimitTypeSpeedLegal(final String id)
    {
        super(id);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpeedLimitTypeSpeedLegal [" + getId() + "]";
    }

}
