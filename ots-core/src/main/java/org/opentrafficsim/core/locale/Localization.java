package org.opentrafficsim.core.locale;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Localization
{
    /** filename without .properties, to be found in src/main/resources folder. */
    private final String bundleNamePrefix;

    /** the resource bundle. */
    private ResourceBundle resourceBundle;

    /** current locale. */
    private Locale currentLocale = null;

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
