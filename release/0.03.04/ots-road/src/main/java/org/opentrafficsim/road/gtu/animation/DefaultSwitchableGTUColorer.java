package org.opentrafficsim.road.gtu.animation;

import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.gtu.animation.VelocityGTUColorer;


/**
 * A simple way to construct a SwitchableGTUColorer set up with the "standard" set of GTUColorers. <br>
 * Do not assume that the set of GTUColorers in the result will never change.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1378 $, $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultSwitchableGTUColorer extends SwitchableGTUColorer
{
    /** The initial set of GTU colorers in the default list. */
    private static final GTUColorer[] COLORERS;

    static
    {
        COLORERS = new GTUColorer[4];
        COLORERS[0] = new IDGTUColorer();
        COLORERS[1] = new VelocityGTUColorer(new Speed.Abs(150, KM_PER_HOUR));
        COLORERS[2] =
            new AccelerationGTUColorer(new Acceleration.Abs(-4, METER_PER_SECOND_2), new Acceleration.Abs(2,
                METER_PER_SECOND_2));
        COLORERS[3] = new LaneChangeUrgeGTUColorer(new Length.Rel(10, METER), new Length.Rel(1000, METER));
    }

    /**
     * create a default list of GTU colorers.
     */
    public DefaultSwitchableGTUColorer()
    {
        super(0, DefaultSwitchableGTUColorer.COLORERS);
    }

}
