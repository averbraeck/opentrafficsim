package org.opentrafficsim.core.locale;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
 * @version Jun 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Localization
{
    /** filename without .properties, to be found in src/main/resources folder. */
    private final String bundleNamePrefix;

    /** the resource bundle. */
    protected ResourceBundle resourceBundle;

    /** current locale. */
    protected Locale currentLocale = null;

    /**
     * Create a Localization object.
     * @param prefix String; the prefix of the properties files to use.
     */
    public Localization(final String prefix)
    {
        this.bundleNamePrefix = prefix;
    }

    /**
     * Retrieve a string from a locale bundle. If retrieval fails the value of key string, surrounded by exclamation marks is
     * returned.
     * @param key the key for the locale in the properties file
     * @return localized string, or, if a translation could not be found return the key surrounded by exclamation marks
     */
    public final String getString(final String key)
    {
        if (this.currentLocale == null || !this.currentLocale.equals(DefaultLocale.getLocale()))
        {
            if (DefaultLocale.getLocale() == null)
            {
                DefaultLocale.setLocale(new Locale("en"));
            }
            this.currentLocale = DefaultLocale.getLocale();
            Locale.setDefault(this.currentLocale);
            this.resourceBundle = ResourceBundle.getBundle(this.bundleNamePrefix, this.currentLocale);
        }

        try
        {
            return this.resourceBundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key.substring(key.indexOf('.') + 1) + '!';
        }
    }

}
