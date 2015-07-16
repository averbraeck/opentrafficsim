package org.opentrafficsim.core.locale;

import java.util.Locale;

/**
 * <p>
 * Copyright (c) 2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Jun 3, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class DefaultLocale
{
    /** The default locale to use in OpenTrafficSim. */
    private static Locale locale;

    /**
     * @return locale
     */
    public static Locale getLocale()
    {
        return DefaultLocale.locale;
    }

    /**
     * @param locale Locale; set locale
     */
    public static void setLocale(final Locale locale)
    {
        DefaultLocale.locale = locale;
    }

}
