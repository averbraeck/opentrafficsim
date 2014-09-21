package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.junit.Test;
import org.opentrafficsim.core.AvailableLocalizations;
import org.opentrafficsim.core.locale.DefaultLocale;
import org.reflections.Reflections;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
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
 * @version Jun 10, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class UnitLocalizationsTest
{
    /** Prefix keys of units made during testing with this string. */
    public static final String DONOTCHECKPREFIX = "~~~~DONOTCHECK";

    /**
     * Check that all defined units have all localizations.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public final void checkDefinedUnits()
    {
        final String head = "localeunit";
        Set<String> usedKeys = new HashSet<String>();
        ArrayList<String> errors = new ArrayList<String>();
        List<String> localeNames =
                AvailableLocalizations.availableLocalizations(head, this.getClass().getResource("").getPath() + "../../../../");
        for (String localeName : localeNames)
        {
            // System.out.println("Checking internationalization to " + localeName);
            DefaultLocale.setLocale(new Locale(localeName));

            Reflections reflections = new Reflections("org.opentrafficsim.core.unit");
            Set<Class<? extends Unit>> classes = reflections.getSubTypesOf(Unit.class);

            for (Class c : classes)
            {
                // System.out.println(c.getSimpleName() + ": " + Unit.getUnits(c));
                for (Object o : Unit.getUnits(c))
                {
                    Unit<?> u = (Unit<?>) o;
                    String nameKey = u.getNameKey();
                    assertTrue("Name key must be non-null", null != nameKey);
                    assertTrue("Name key must be non-empty", nameKey.length() > 0);
                    String abbreviationKey = u.getAbbreviationKey();
                    assertTrue("Abbreviation key must be non-null", null != abbreviationKey);
                    assertTrue("Abbreviation key must be non-empty", abbreviationKey.length() > 0);
                    if (nameKey.startsWith(DONOTCHECKPREFIX))
                    {
                        continue;
                    }
                    if (nameKey.equals("SIUnit.m2"))
                    {
                        continue; // FIXME: Vector and Matrix tests make these and then cause this test to fail
                    }
                    if (nameKey.equals("SIUnit.kg.m2/s2"))
                    {
                        continue; // FIXME: Vector and Matrix tests make these and then cause this test to fail
                    }
                    if (nameKey.equals("SIUnit.s"))
                    {
                        continue; // FIXME: Scalar tests make these and then cause this test to fail
                    }
                    if (nameKey.equals("SIUnit.kg2.m4/s6/A2"))
                    {
                        continue;
                    }
                    if (nameKey.equals("SIUnit.1/A"))
                    {
                        continue;
                    }
                    if (abbreviationKey.startsWith(DONOTCHECKPREFIX))
                    {
                        continue;
                    }
                    usedKeys.add(nameKey);
                    usedKeys.add(abbreviationKey);
                    String name = u.getName();
                    // assertFalse("Name may not begin AND end with an exclamation mark",
                    // name.startsWith("!") && name.endsWith("!"));
                    String abbreviation = u.getAbbreviation();
                    // System.out.println("nameKey " + nameKey + "->" + name + " abbreviationKey " + abbreviationKey
                    // + "->" + abbreviation);
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
        }
        for (String localeName : localeNames)
        {
            Properties properties = new Properties();
            String middlePart = "";
            if (!localeName.equals("en"))
            {
                middlePart = "_" + localeName;
            }

            String path = this.getClass().getResource("").getPath() + "../../../../" + head + middlePart + ".properties";
            try
            {
                FileInputStream fileInput = new FileInputStream(path);
                properties.load(fileInput);
                fileInput.close();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
            ResourceBundle.clearCache();
            Set<String> unusedKeys = new HashSet<String>();
            for (Object key : properties.keySet())
            {
                String keyString = (String) key;
                if (usedKeys.contains(keyString))
                {
                    continue;
                }
                unusedKeys.add(keyString);
            }
            for (String unusedKey : unusedKeys)
            {
                errors.add(String.format("Unused key %s for locale %s", unusedKey, localeName));
            }
        }
        for (String s : errors)
        {
            System.out.println(s);
        }
        assertTrue("There should be no errors in UnitLocalizations", errors.isEmpty());
    }

}
