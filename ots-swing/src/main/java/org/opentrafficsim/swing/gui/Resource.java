package org.opentrafficsim.swing.gui;

import java.io.InputStream;

/**
 * Resource utility.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class Resource
{

    /** Constructor. */
    private Resource()
    {
        //
    }

    /**
     * Obtains stream for resource, either in IDE or java.
     * @param name String; name of resource
     * @return the resolved input stream
     */
    public static InputStream getResourceAsStream(final String name)
    {
        InputStream stream = Resource.class.getResourceAsStream(name);
        if (stream != null)
        {
            return stream;
        }
        stream = Resource.class.getResourceAsStream("/resources" + name);
        if (stream != null)
        {
            return stream;
        }
        throw new RuntimeException("Unable to load resource " + name);
    }

}
