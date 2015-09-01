package org.opentrafficsim.core.gtu.animation;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;

/**
 * A simple way to construct a SwitchableGTUColorer set up with the "standard" set of GTUColorers. <br>
 * Do not assume that the set of GTUColorers in the result will never change.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
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
        COLORERS[1] = new VelocityGTUColorer(new Speed.Abs(150, SpeedUnit.KM_PER_HOUR));
        COLORERS[2] =
            new AccelerationGTUColorer(new Acceleration.Abs(-4, AccelerationUnit.METER_PER_SECOND_2), new Acceleration.Abs(
                2, AccelerationUnit.METER_PER_SECOND_2));
        COLORERS[3] =
            new LaneChangeUrgeGTUColorer(new Length.Rel(10, LengthUnit.METER), new Length.Rel(1000, LengthUnit.METER));
    }

    /**
     * create a default list of GTU colorers.
     */
    public DefaultSwitchableGTUColorer()
    {
        super(0, DefaultSwitchableGTUColorer.COLORERS);
    }

}
