package org.opentrafficsim.core.unit.unitsystem;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opentrafficsim.core.AvailableLocalizations;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under the following BSD-style license:<br>
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
 * @version Jun 11, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class UnitSystemLocalizationsTest
{
    /**
     * Check that all UnitSystems have valid a nameKey and a valid abbreviationKey and test those keys in all available
     * localizations.
     */
    @Test
    public void checkDefinedUnitSystems()
    {
        List<UnitSystem> unitSystems = new ArrayList<UnitSystem>();
        Field[] fields = UnitSystem.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            // System.out.println("Field[" + i + "]: " + fields[i]);
            try
            {
                UnitSystem us = (UnitSystem) fields[i].get(null);
                // System.out.println("Prints like " + us);
                // System.out.println("nameKey: " + us.getNameKey());
                unitSystems.add(us);
            }
            catch (Exception e)
            {
                // That was not a UnitSystem
            }
        }
        ArrayList<String> errors = new ArrayList<String>();
        for (String localeName : AvailableLocalizations.availableLocalizations("localeunitsystem", this.getClass().getResource("")
                .getPath()
                + "../../../../../"))
        {
            for (UnitSystem us : unitSystems)
            {
                String nameKey = us.getNameKey();
                assertTrue("nameKey is non null", null != nameKey);
                assertTrue("Name key must be non-empty", nameKey.length() > 0);
                String abbreviationKey = us.getAbbreviationKey();
                assertTrue("abbreviationKey is non null", null != abbreviationKey);
                assertTrue("Abbreviation key must be non-empty", abbreviationKey.length() > 0);
                String name = us.getName();
                String abbreviation = us.getAbbreviation();
                if (abbreviation.startsWith("!") && abbreviation.endsWith("!"))
                    errors.add(String.format("Missing translation for abbreviation %s to %s", abbreviationKey,
                            localeName));
                if (name.startsWith("!") && name.endsWith("!"))
                    errors.add(String.format("Missing translation for name %s to %s", nameKey, localeName));
            }
        }
        for (String s : errors)
            System.out.println(s);
        assertTrue("There should be no errors", errors.isEmpty());
    }
}
