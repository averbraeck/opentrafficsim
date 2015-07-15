package org.opentrafficsim.core.unit.unitsystem;

/**
 * The centimeter-gram-second system. Electromagnetic units (ESU).
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionJun 6, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class CGS_EMU extends CGS
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /**
     * protected constructor to avoid creating other (false) unit systems.
     * @param abbreviationKey the abbreviation of the unit system, such as SI
     * @param nameKey the name of the unit system, such as SI Base
     */
    protected CGS_EMU(final String abbreviationKey, final String nameKey)
    {
        super(abbreviationKey, nameKey);
    }

}
