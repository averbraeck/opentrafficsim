package org.opentrafficsim.base;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Resource utility.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param name name of resource
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

    /**
     * Obtains URI for resource, either in IDE or java.
     * @param name name of resource
     * @return the resolved URI
     * @throws URISyntaxException when the file name is malformed.
     */
    public static URI getResourceAsUri(final String name) throws URISyntaxException
    {
        InputStream stream = Resource.class.getResourceAsStream(name);
        if (stream != null)
        {
            return Resource.class.getResource(name).toURI();
        }
        stream = Resource.class.getResourceAsStream("/resources" + name);
        if (stream != null)
        {
            return Resource.class.getResource("/resources" + name).toURI();
        }
        throw new RuntimeException("Unable to load resource " + name);

    }

}
