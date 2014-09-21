package org.opentrafficsim.core.unit;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Helper class to create arbitrary SI units.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version Jun 15, 2014 <br>
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
