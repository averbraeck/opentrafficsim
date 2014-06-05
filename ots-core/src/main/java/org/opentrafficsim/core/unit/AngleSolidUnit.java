package org.opentrafficsim.core.unit;

/**
 * Standard solid angle unit.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class AngleSolidUnit extends Unit<AngleSolidUnit>
{
    /** */
    private static final long serialVersionUID = 20140604L;

    /** steradian */
    public static final AngleSolidUnit STERADIAN = new AngleSolidUnit("AngleSolidUnit.steradian", "AngleSolidUnit.sr",
            1.0);

    /** square degree */
    public static final AngleSolidUnit SQUARE_DEGREE = new AngleSolidUnit("AngleSolidUnit.square_degree",
            "AngleSolidUnit.sq_deg", (Math.PI / 180.0) * (Math.PI / 180.0));

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param convertToSteradian multiply by this number to convert to steradians
     */
    public AngleSolidUnit(final String nameKey, final String abbreviationKey, final double convertToSteradian)
    {
        super(nameKey, abbreviationKey, convertToSteradian);
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert to
      * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public AngleSolidUnit(final String nameKey, final String abbreviationKey, final AngleSolidUnit referenceUnit,
            final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorToReferenceUnit);
    }

}
