package org.opentrafficsim.core.unit.unitsystem;

/**
 * Other (non-SI, cgs, Imperial, mts, US, ...) system, or to indicate a unit is not belonging to a system.
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 6, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Other extends UnitSystem
{
    /** */
    private static final long serialVersionUID = 20140606L;

    /**
     * protected constructor to avoid creating other (false) unit systems.
     * @param abbreviationKey the abbreviation of the unit system, such as SI
     * @param nameKey the name of the unit system, such as SI Base
     */
    protected Other(final String abbreviationKey, final String nameKey)
    {
        super(abbreviationKey, nameKey);
    }

}
