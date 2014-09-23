package org.opentrafficsim.core.unit.unitsystem;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opentrafficsim.core.AvailableLocalizations;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
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
        for (String localeName : AvailableLocalizations.availableLocalizations("localeunitsystem",
                this.getClass().getResource("").getPath() + "../../../../../"))
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
                {
                    errors.add(String.format("Missing translation for abbreviation %s to %s", abbreviationKey, localeName));
                }
                if (name.startsWith("!") && name.endsWith("!"))
                {
                    errors.add(String.format("Missing translation for name %s to %s", nameKey, localeName));
                }
            }
        }
        for (String s : errors)
        {
            System.out.println(s);
        }
        assertTrue("There should be no errors in UnitSystemLocalizations", errors.isEmpty());
    }
}
