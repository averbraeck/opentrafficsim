package org.opentrafficsim.core.unit;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Helper class to create arbitrary SI units.
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SIUnit extends Unit<SIUnit>
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /**
     * Create an arbitrary SI unit based on a coefficient string, such as m3/cd2.
     * @param siCoefficientString String; textual description of the unit.
     */
    public SIUnit(final String siCoefficientString)
    {
        super(siCoefficientString, siCoefficientString, UnitSystem.SI_DERIVED, true);
    }

    /** {@inheritDoc} */
    @Override
    public final SIUnit getStandardUnit()
    {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return this.getAbbreviationKey().replace("SIUnit.", "");
    }

}
