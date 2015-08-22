package org.opentrafficsim.core.unit.unitsystem;

/**
 * The international System of Units (SI). Base units m, kg, s, A, K, mol, cd.
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jun 6, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SIDerived extends SI
{
    /** */
    private static final long serialVersionUID = 20140606L;

    /**
     * protected constructor to avoid creating other (false) SI unit systems.
     * @param abbreviationKey the abbreviation of the unit system, such as SI
     * @param nameKey the name of the unit system, such as SI Base
     */
    protected SIDerived(final String abbreviationKey, final String nameKey)
    {
        super(abbreviationKey, nameKey);
    }

}
