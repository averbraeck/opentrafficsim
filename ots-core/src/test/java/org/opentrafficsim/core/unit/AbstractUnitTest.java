package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX
 * Delft, the Netherlands. All rights reserved.
 * 
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/">
 * www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is"
 * and any express or implied warranties, including, but not limited to, the
 * implied warranties of merchantability and fitness for a particular purpose
 * are disclaimed. In no event shall the copyright holder or contributors be
 * liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of
 * substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in
 * contract, strict liability, or tort (including negligence or otherwise)
 * arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * 
 * @version Jun 4, 2014 <br>
 * @author Peter Knoppers
 * @param <U> Make the test specific for this sub class of Unit
 */
public abstract class AbstractUnitTest<U extends Unit<U>>
{
    /**
     * Verify one length conversion factor to standard unit and the localization of the name and abbreviation
     * @param u Unit to check
     * @param expectedRatio Double; expected ratio
     * @param precision Double; precision of verification
     * @param expectedName String; expected name in the resources
     * @param expectedAbbreviation String; expected abbreviation in the resources
     */
    protected void checkUnitRatioNameAndAbbreviation(U u, double expectedRatio, double precision, String expectedName, String expectedAbbreviation) {
        assertEquals(String.format("one %s is about %f s", u.getNameKey(), expectedRatio), expectedRatio, u.getConversionFactorToStandardUnit(), precision);
        assertEquals(String.format("Name of %s is %s", u.getNameKey(), expectedName), expectedName, u.getName());
        assertEquals(String.format("Abbreviation of %s is %s", u.getNameKey(), expectedAbbreviation), expectedAbbreviation, u.getAbbreviation());
    }
    
    /**
     * Check the nameKey and abbreviationKey of a Unit
     * @param u Unit to check
     * @param expectedNameKey String; expected name key
     * @param expectedAbbreviationKey String; expected abbreviation key
     */
    protected void checkKeys(U u, String expectedNameKey, String expectedAbbreviationKey) {
        assertEquals("unit key", expectedNameKey, u.getNameKey());
        assertEquals("abbreviation key", expectedAbbreviationKey, u.getAbbreviationKey());
    }

}
